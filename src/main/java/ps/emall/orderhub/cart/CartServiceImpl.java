package ps.emall.orderhub.cart;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.cart.item.*;
import ps.emall.orderhub.client.accounts.AccountsClient;
import ps.emall.orderhub.client.accounts.AccountsCityResponse;
import ps.emall.orderhub.client.accounts.AccountsUserResponse;
import ps.emall.orderhub.client.accounts.CityDto;
import ps.emall.orderhub.client.campaigns.CampaignsClient;
import ps.emall.orderhub.client.campaigns.CampaignsOfferResponse;
import ps.emall.orderhub.client.campaigns.OfferItemDto;
import ps.emall.orderhub.client.catalog.CatalogClient;
import ps.emall.orderhub.client.catalog.CatalogResponse;
import ps.emall.orderhub.client.catalog.ProductInfoDto;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.common.phone_number.PhoneNumberMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;
    private final CampaignsClient campaignsClient;
    private final AccountsClient accountsClient;
    private final CartEnrichmentService enrichmentService;

    // ─── Core operation ────────────────────────────────────────────────────────

    @Override
    public CartDto addItem(Long customerId, AddToCartRequest request) {


        ProductInfoDto product = fetchProductOrThrow(request.getProductId());

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw CartExceptions.productNotActive();
        }

        ProductInfoDto.VariantPriceInfoDto variant =
                findVariantOrThrow(product, request.getVariantId());

        if (product.getMallId() != null && !product.getMallId().equals(request.getMallId())) {
            throw CartExceptions.productDoesNotBelongToMall();
        }

        Cart cart = cartRepository
                .findByCustomerIdAndMallId(customerId, request.getMallId())
                .orElseGet(() -> createEmptyCart(customerId, request.getMallId()));

        if (cartItemRepository.existsByCart_CartIdAndVariantId(cart.getCartId(), request.getVariantId())) {
            throw CartExceptions.duplicateVariantInCart();
        }

        // Snapshot base price
        BigDecimal basePrice = variant.getBasePrice();
        BigDecimal discountedPrice = null;
        Long offerId = null;

        try {
            CampaignsOfferResponse campaignsResponse =
                    campaignsClient.getActiveOfferForProduct(request.getProductId());

            if (campaignsResponse != null && campaignsResponse.getData() != null) {
                OfferItemDto offerItem = campaignsResponse.getData();
                if (offerItem.getVariantPrices() != null) {
                    for (OfferItemDto.VariantPriceDto vp : offerItem.getVariantPrices()) {
                        if (request.getVariantId().equals(vp.getVariantId())
                                && vp.getDiscountedPrice() != null) {
                            discountedPrice = vp.getDiscountedPrice();
                            offerId = offerItem.getOfferId();
                            break;
                        }
                    }
                }
            }
        } catch (FeignException.NotFound e) {
            log.warn("Campaigns 404 for productId={}", request.getProductId());
        } catch (FeignException e) {
            log.debug("Campaigns unreachable for productId={}, status={}", request.getProductId(), e.status());
        } catch (Exception e) {
            log.debug("Could not fetch discount for productId={}: {}", request.getProductId(), e.getMessage());
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .productId(product.getProductId())
                .productName(product.getName())
                .variantId(variant.getVariantId())
                .variantName(variant.getVariantName())
                .storeId(product.getStoreId())
                .mallId(product.getMallId())
                .basePrice(basePrice)
                .discountedPrice(discountedPrice)
                .quantity(request.getQuantity())
                .offerId(offerId)
                .build();

        CartItem savedItem = cartItemRepository.save(item);
        cart.getItems().add(savedItem);

        // Refresh OTHER items in the cart before recalculating
        refreshCartPrices(cart);

        cart.recalculateTotal();
        Cart saved = cartRepository.save(cart);

        log.info("Item added to cart: cartId={}, productId={}, variantId={}, offerId={}",
                saved.getCartId(), request.getProductId(), request.getVariantId(), offerId);

        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    @Override
    // Note: NOT readOnly — refreshCartPrices may write
    public CartDto getCartForMall(Long customerId, Long mallId) {
        Cart cart = cartRepository
                .findByCustomerIdAndMallId(customerId, mallId)
                .orElseThrow(CartExceptions::cartNotFound);

        refreshCartPrices(cart);

        return enrichmentService.enrich(CartMapper.toDto(cart));
    }

    @Override
    // Note: NOT readOnly — refreshCartPrices may write
    public CartDto getCartById(Long cartId, Long customerId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(CartExceptions::cartNotFound);

        if (customerId != null && !cart.getCustomerId().equals(customerId)) {
            throw CartExceptions.cartNotFound();
        }

        refreshCartPrices(cart);

        return enrichmentService.enrich(CartMapper.toDto(cart));
    }

    @Override
    // Note: NOT readOnly — refreshCartPrices may write
    public List<CartDto> getAllActiveCartsForCustomer(Long customerId) {
        List<Cart> carts = cartRepository.findAllByCustomerId(customerId);
        carts.forEach(this::refreshCartPrices);
        return carts.stream()
                .map(CartMapper::toDto)
                .map(enrichmentService::enrich)
                .collect(Collectors.toList());
    }

    // ─── Modify active cart ────────────────────────────────────────────────────


    @Override
    public CartDto updateItemQuantity(Long customerId, Long cartItemId,
                                      UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(CartExceptions::cartItemNotFound);

        Cart cart = item.getCart();

        if (!cart.getCustomerId().equals(customerId)) {
            throw CartExceptions.cartItemNotInCart();
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        // Refresh ALL items (including this one) so prices are current before total
        refreshCartPrices(cart);

        cart.recalculateTotal();
        Cart saved = cartRepository.save(cart);

        log.info("Cart item quantity updated: cartItemId={}, newQuantity={}",
                cartItemId, request.getQuantity());
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    @Override
    public CartDto removeItem(Long customerId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(CartExceptions::cartItemNotFound);

        Cart cart = item.getCart();

        if (!cart.getCustomerId().equals(customerId)) {
            throw CartExceptions.cartItemNotInCart();
        }

        cart.getItems().remove(item);

        // Refresh remaining items before recalculating total
        refreshCartPrices(cart);

        cart.recalculateTotal();
        Cart saved = cartRepository.save(cart);

        log.info("Item removed from cart: cartId={}, cartItemId={}", cart.getCartId(), cartItemId);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    @Override
    public CartDto clearCart(Long customerId, Long mallId) {
        Cart cart = cartRepository
                .findByCustomerIdAndMallId(customerId, mallId)
                .orElseThrow(CartExceptions::cartNotFound);

        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);

        Cart saved = cartRepository.save(cart);
        log.info("Cart cleared: cartId={}, mallId={}", cart.getCartId(), mallId);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }


    // ─── System ────────────────────────────────────────────────────────────────


    // ─── Admin ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CartDto> getAllCarts(CartSpec spec, Pageable pageable) {
        return PaginatedResponse.of(
                cartRepository.findAll(spec, pageable)
                        .map(CartMapper::toDto)
                        .map(enrichmentService::enrich)
        );
    }

    // ─── Price refresh ─────────────────────────────────────────────────────────

    /**
     * For every item in the cart, calls Campaigns service to check the current
     * active offer. If the offer changed (new offer, offer removed, offer replaced),
     * updates discountedPrice and offerId on the item and saves it.
     *
     * After the loop, if anything changed, recalculateTotal() is called by the caller.
     *
     * Graceful: if Campaigns is unreachable for an item, that item is skipped
     * and its price stays as-is — no exception is thrown to the user.
     */
    private void refreshCartPrices(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) return;

        boolean anyChanged = false;

        for (CartItem item : cart.getItems()) {
            BigDecimal newDiscountedPrice = null;
            Long newOfferId = null;
            boolean fetchSucceeded = false;

            try {
                CampaignsOfferResponse response =
                        campaignsClient.getActiveOfferForProduct(item.getProductId());

                fetchSucceeded = true;

                if (response != null && response.getData() != null) {
                    OfferItemDto offerItem = response.getData();
                    if (offerItem.getVariantPrices() != null) {
                        for (OfferItemDto.VariantPriceDto vp : offerItem.getVariantPrices()) {
                            if (item.getVariantId().equals(vp.getVariantId())
                                    && vp.getDiscountedPrice() != null) {
                                newDiscountedPrice = vp.getDiscountedPrice();
                                newOfferId = offerItem.getOfferId();
                                break;
                            }
                        }
                    }
                }

            } catch (FeignException.NotFound e) {
                // No active offer for this product — treat as "no discount"
                fetchSucceeded = true;
                log.debug("No active offer for productId={}", item.getProductId());
            } catch (FeignException e) {
                log.debug("Campaigns unreachable for productId={}, skipping refresh. status={}",
                        item.getProductId(), e.status());
                // fetchSucceeded = false → skip this item, keep old price
            } catch (Exception e) {
                log.debug("Unexpected error refreshing price for productId={}: {}",
                        item.getProductId(), e.getMessage());
                // fetchSucceeded = false → skip this item, keep old price
            }

            if (!fetchSucceeded) continue;

            // Check if anything actually changed before touching the DB
            boolean offerChanged =
                    !Objects.equals(newOfferId, item.getOfferId()) ||
                            !Objects.equals(newDiscountedPrice, item.getDiscountedPrice());

            if (offerChanged) {
                log.info("Offer changed for cartItemId={} variantId={}: offerId {} → {}, discountedPrice {} → {}",
                        item.getCartItemId(), item.getVariantId(),
                        item.getOfferId(), newOfferId,
                        item.getDiscountedPrice(), newDiscountedPrice);

                item.setDiscountedPrice(newDiscountedPrice);
                item.setOfferId(newOfferId);
                cartItemRepository.save(item);
                anyChanged = true;
            }
        }

        if (anyChanged) {
            cart.recalculateTotal();
            cartRepository.save(cart);
            log.info("Cart prices refreshed and total recalculated: cartId={}", cart.getCartId());
        }
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private Cart createEmptyCart(Long customerId, Long mallId) {
        Cart cart = Cart.builder()
                .customerId(customerId)
                .mallId(mallId)
                .totalAmount(BigDecimal.ZERO)
                .build();
        Cart saved = cartRepository.save(cart);
        log.info("Auto-created cart: cartId={}, customerId={}, mallId={}",
                saved.getCartId(), customerId, mallId);
        return saved;
    }

    private Cart getActiveCartOwnedBy(Long cartId, Long customerId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(CartExceptions::cartNotFound);

        if (!cart.getCustomerId().equals(customerId)) {
            throw CartExceptions.cartNotFound();
        }
        return cart;
    }

    private ProductInfoDto fetchProductOrThrow(Long productId) {
        try {
            CatalogResponse response = catalogClient.getProductInfo(productId);
            if (response == null || response.getData() == null) {
                throw CartExceptions.productNotFoundInCatalog();
            }
            return response.getData();
        } catch (FeignException.NotFound e) {
            throw CartExceptions.productNotFoundInCatalog();
        } catch (FeignException e) {
            log.error("Catalog unreachable for productId={}, status={}", productId, e.status());
            throw CartExceptions.productNotFoundInCatalog();
        }
    }

    private ProductInfoDto.VariantPriceInfoDto findVariantOrThrow(
            ProductInfoDto product, Long variantId) {
        if (product.getVariants() == null) throw CartExceptions.variantNotFoundInCatalog();
        return product.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(CartExceptions::variantNotFoundInCatalog);
    }

    private CityDto fetchCityOrThrow(Long cityId) {
        try {
            AccountsCityResponse response = accountsClient.getCityById(cityId);
            if (response == null || response.getData() == null) {
                throw CartExceptions.cityNotFound();
            }
            CityDto city = response.getData();
            if (Boolean.FALSE.equals(city.getIsActive())) {
                throw CartExceptions.cityNotFound();
            }
            return city;
        } catch (FeignException.NotFound e) {
            throw CartExceptions.cityNotFound();
        } catch (FeignException e) {
            log.error("Accounts unreachable for cityId={}, status={}", cityId, e.status());
            throw CartExceptions.cityNotFound();
        }
    }
}