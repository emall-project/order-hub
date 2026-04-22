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

    // Core operation

    /**
     * "Add to cart" — the single entry point for the customer clicking
     * "Add to cart" on a product page.
     *
     * Flow:
     *  1. Validate customer exists and is active (soft — allows if accounts unreachable)
     *  2. Validate product + variant from Catalog service
     *  3. Validate product.mallId matches the requested mallId (product must belong to this mall)
     *  4. Look for an existing ACTIVE cart for this customer + mall combo
     *     → If found: use it
     *     → If not found: create a new empty cart for this mall
     *  5. Block duplicate variant (same variant already in this cart)
     *  6. Snapshot basePrice from catalog + discountedPrice from campaigns (graceful if unavailable)
     *  7. Add item, recalculate cart total, save
     *
     * A customer can have one ACTIVE cart per mall at a time.
     * They can have active carts in multiple different malls simultaneously.
     */
    @Override
    public CartDto addItem(Long customerId, AddToCartRequest request) {

        validateCustomerExists(customerId);

        // Step 2: Validate product + variant from Catalog service
        ProductInfoDto product = fetchProductOrThrow(request.getProductId());

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw CartExceptions.productNotActive();
        }

        ProductInfoDto.VariantPriceInfoDto variant =
                findVariantOrThrow(product, request.getVariantId());

        // Step 3: Verify the product belongs to the mall the customer is shopping in.
        // This prevents a customer from accidentally adding a Ramallah mall product
        // into their Nablus mall cart.
        if (product.getMallId() != null && !product.getMallId().equals(request.getMallId())) {
            throw CartExceptions.productDoesNotBelongToMall();
        }

        // Step 4: Find or create ACTIVE cart for this customer + mall
        Cart cart = cartRepository
                .findByCustomerIdAndMallIdAndStatus(customerId, request.getMallId(), CartStatus.ACTIVE)
                .orElseGet(() -> createEmptyCart(customerId, request.getMallId()));

        // Step 5: Block duplicate variant in the same cart
        if (cartItemRepository.existsByCart_CartIdAndVariantId(cart.getCartId(), request.getVariantId())) {
            throw CartExceptions.duplicateVariantInCart();
        }

        // Step 6: Snapshot price and inject discount from Campaigns service
        BigDecimal basePrice = variant.getBasePrice();
        BigDecimal discountedPrice = null;
        Long offerId = null;

        try {
            CampaignsOfferResponse campaignsResponse =
                    campaignsClient.getActiveOfferForProduct(request.getProductId());

            if (campaignsResponse != null && campaignsResponse.getData() != null) {
                OfferItemDto offerItem = campaignsResponse.getData();

                if (offerItem.getVariantPrices() != null) {
                    offerItem.getVariantPrices().stream()
                            .filter(vp -> vp.getVariantId() != null
                                    && vp.getVariantId().equals(request.getVariantId())
                                    && vp.getDiscountedPrice() != null)
                            .findFirst()
                            .ifPresent(vp -> {
                            });

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
            log.warn("Campaigns service returned 404 for productId={}. Check URL config.", request.getProductId());
        } catch (FeignException e) {
            log.debug("Campaigns service unreachable for productId={}. Adding at base price. status={}",
                    request.getProductId(), e.status());
        } catch (Exception e) {
            log.debug("Could not fetch discount for productId={}: {}", request.getProductId(), e.getMessage());
        }

        // Step 7: Build item and add to cart
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
        cart.recalculateTotal();

        Cart saved = cartRepository.save(cart);
        log.info("Item added to cart: cartId={}, mallId={}, productId={}, variantId={}, wasNewCart={}",
                saved.getCartId(), request.getMallId(),
                request.getProductId(), request.getVariantId(),
                cart.getCreatedAt() == null);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    // Read

    @Override
    @Transactional(readOnly = true)
    public CartDto getActiveCartForMall(Long customerId, Long mallId) {
        Cart cart = cartRepository
                .findByCustomerIdAndMallIdAndStatus(customerId, mallId, CartStatus.ACTIVE)
                .orElseThrow(CartExceptions::cartNotFound);

        return enrichmentService.enrich(CartMapper.toDto(cart));
    }

    @Override
    @Transactional(readOnly = true)
    public CartDto getCartById(Long cartId, Long customerId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(CartExceptions::cartNotFound);

        // Admin passes customerId = null — skip ownership check
        if (customerId != null && !cart.getCustomerId().equals(customerId)) {
            throw CartExceptions.cartNotFound();
        }
        return enrichmentService.enrich(CartMapper.toDto(cart));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CartDto> getMyCartHistory(Long customerId, Pageable pageable) {
        return PaginatedResponse.of(
                cartRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                        .map(CartMapper::toDto)
                        .map(enrichmentService::enrich)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartDto> getAllActiveCartsForCustomer(Long customerId) {
        return cartRepository.findAllByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .stream()
                .map(CartMapper::toDto)
                .map(enrichmentService::enrich)
                .collect(Collectors.toList());
    }

    // Modify active cart

    @Override
    public CartDto updateDeliveryDetails(Long cartId, Long customerId, CartDto details) {
        Cart cart = getActiveCartOwnedBy(cartId, customerId);

        // Re-fetch delivery fee only if city changed
        if (details.getCityId() != null
                && !details.getCityId().equals(cart.getCityId())) {
            CityDto city = fetchCityOrThrow(details.getCityId());
            cart.setCityId(city.getCityId());
            cart.setDeliveryFee(city.getBaseFee());
        }

        if (details.getDeliveryName() != null) cart.setDeliveryName(details.getDeliveryName());
        if (details.getDeliveryPhone() != null) cart.setDeliveryPhone(PhoneNumberMapper.toPhoneString(details.getDeliveryPhone()));
        if (details.getDeliveryNote() != null) cart.setDeliveryNote(details.getDeliveryNote());
        if (details.getDeliveryLocation() != null) cart.setDeliveryLocation(details.getDeliveryLocation());

        Cart saved = cartRepository.save(cart);
        log.info("Cart delivery details updated: cartId={}", cartId);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    @Override
    public CartDto updateItemQuantity(Long customerId, Long cartItemId,
                                      UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(CartExceptions::cartItemNotFound);

        Cart cart = item.getCart();

        // Verify ownership — item must belong to this customer's cart
        if (!cart.getCustomerId().equals(customerId)) {
            throw CartExceptions.cartItemNotInCart();
        }
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw CartExceptions.cartNotActive();
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

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
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw CartExceptions.cartNotActive();
        }

        cart.getItems().remove(item);
        cart.recalculateTotal();

        Cart saved = cartRepository.save(cart);
        log.info("Item removed from cart: cartId={}, cartItemId={}", cart.getCartId(), cartItemId);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    @Override
    public CartDto clearCart(Long customerId, Long mallId) {
        Cart cart = cartRepository
                .findByCustomerIdAndMallIdAndStatus(customerId, mallId, CartStatus.ACTIVE)
                .orElseThrow(CartExceptions::cartNotFound);

        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);

        Cart saved = cartRepository.save(cart);
        log.info("Cart cleared: cartId={}, mallId={}", cart.getCartId(), mallId);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    @Override
    public CartDto cancelCart(Long customerId, Long mallId) {
        Cart cart = cartRepository
                .findByCustomerIdAndMallIdAndStatus(customerId, mallId, CartStatus.ACTIVE)
                .orElseThrow(CartExceptions::cartNotFound);

        cart.setStatus(CartStatus.CANCELLED);
        Cart saved = cartRepository.save(cart);
        log.info("Cart cancelled: cartId={}, mallId={}, customerId={}", cart.getCartId(), mallId, customerId);
        return enrichmentService.enrich(CartMapper.toDto(saved));
    }

    // System

    @Override
    public void markCheckedOut(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(CartExceptions::cartNotFound);

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw CartExceptions.cartNotActive();
        }

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);
        log.info("Cart marked as checked out: cartId={}", cartId);
    }

    // Admin

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<CartDto> getAllCarts(CartSpec spec, Pageable pageable) {
        return PaginatedResponse.of(
                cartRepository.findAll(spec, pageable)
                        .map(CartMapper::toDto)
                        .map(enrichmentService::enrich)
        );
    }

    // Private helpers

    private Cart createEmptyCart(Long customerId, Long mallId) {
        Cart cart = Cart.builder()
                .customerId(customerId)
                .mallId(mallId)
                .deliveryFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .status(CartStatus.ACTIVE)
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
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw CartExceptions.cartNotActive();
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
            log.error("Catalog service unreachable for productId={}, status={}", productId, e.status());
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
            log.error("Accounts service unreachable for cityId={}, status={}", cityId, e.status());
            throw CartExceptions.cityNotFound();
        }
    }

    private void validateCustomerExists(Long customerId) {
        try {
            AccountsUserResponse response = accountsClient.getUserById(customerId);
            if (response == null || response.getData() == null) {
                throw CartExceptions.customerNotFound();
            }
            if (Boolean.FALSE.equals(response.getData().getIsActive())) {
                throw CartExceptions.customerNotActive();
            }
        } catch (FeignException.NotFound e) {
            throw CartExceptions.customerNotFound();
        } catch (FeignException e) {
            log.warn("Accounts service unreachable for customerId={}, status={}. Allowing request.",
                    customerId, e.status());
        }
    }
}
