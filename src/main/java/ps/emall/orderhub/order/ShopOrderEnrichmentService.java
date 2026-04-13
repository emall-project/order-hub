package ps.emall.orderhub.order;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ps.emall.orderhub.cart.CartMapper;
import ps.emall.orderhub.cart.CartRepository;
import ps.emall.orderhub.client.accounts.*;
import ps.emall.orderhub.client.catalog.CatalogClient;
import ps.emall.orderhub.client.catalog.CatalogResponse;
import ps.emall.orderhub.client.catalog.ProductInfoDto;
import ps.emall.orderhub.order.item.OrderItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopOrderEnrichmentService {

    private final AccountsClient accountsClient;
    private final CatalogClient catalogClient;
    private final CartRepository cartRepository;

    public ShopOrderDto enrich(ShopOrderDto dto) {
        if (dto == null) return null;

        dto.setShopInfo(fetchShopInfo(dto.getShopId()));
        dto.setMallInfo(fetchMallInfo(dto.getMallId()));
        dto.setCustomerInfo(fetchCustomerInfo(dto.getCustomerId()));

        // Inject delivery info from cart if not already set
        if (dto.getDeliveryInfo() == null && dto.getCartId() != null) {
            cartRepository.findById(dto.getCartId())
                    .ifPresent(cart -> dto.setDeliveryInfo(
                            CartMapper.toDeliveryInfoDto(cart)));
        }

        if (dto.getItems() != null) {
            dto.getItems().forEach(item -> enrichItem(item, dto.getMallId()));
        }

        return dto;
    }

    public OrderItemDto enrichItem(OrderItemDto item, Long mallId) {
        if (item == null) return null;

        item.setShopInfo(fetchShopInfo(item.getShopId()));

        MallInfoDto mall = fetchMallInfo(mallId != null ? mallId : item.getMallId());
        item.setMallInfo(mall);

        ProductInfoDto product = fetchProductInfo(item.getProductId());
        item.setProductInfo(product);

        if (product != null && item.getVariantId() != null) {
            item.setVariantInfo(findVariant(product, item.getVariantId()));
        }

        return item;
    }

    private ShopInfoDto fetchShopInfo(Long shopId) {
        if (shopId == null) return null;
        try {
            AccountsShopResponse response = accountsClient.getShopById(shopId);
            return response != null ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Shop not found: shopId={}", shopId);
        } catch (FeignException e) {
            log.debug("Accounts unavailable fetching shopId={}, status={}", shopId, e.status());
        } catch (Exception e) {
            log.debug("Error fetching shopId={}: {}", shopId, e.getMessage());
        }
        return null;
    }

    private MallInfoDto fetchMallInfo(Long mallId) {
        if (mallId == null) return null;
        try {
            AccountsMallResponse response = accountsClient.getMallById(mallId);
            return response != null ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Mall not found: mallId={}", mallId);
        } catch (FeignException e) {
            log.debug("Accounts unavailable fetching mallId={}, status={}", mallId, e.status());
        } catch (Exception e) {
            log.debug("Error fetching mallId={}: {}", mallId, e.getMessage());
        }
        return null;
    }

    private UserInfoDto fetchCustomerInfo(Long customerId) {
        if (customerId == null) return null;
        try {
            AccountsUserResponse response = accountsClient.getUserById(customerId);
            return response != null ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Customer not found: customerId={}", customerId);
        } catch (FeignException e) {
            log.debug("Accounts unavailable fetching customerId={}, status={}", customerId, e.status());
        } catch (Exception e) {
            log.debug("Error fetching customerId={}: {}", customerId, e.getMessage());
        }
        return null;
    }

    private ProductInfoDto fetchProductInfo(Long productId) {
        if (productId == null) return null;
        try {
            CatalogResponse response = catalogClient.getProductInfo(productId);
            return response != null ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Product not found: productId={}", productId);
        } catch (FeignException e) {
            log.debug("Catalog unavailable fetching productId={}, status={}", productId, e.status());
        } catch (Exception e) {
            log.debug("Error fetching productId={}: {}", productId, e.getMessage());
        }
        return null;
    }

    private ProductInfoDto.VariantPriceInfoDto findVariant(ProductInfoDto product, Long variantId) {
        if (product.getVariants() == null || variantId == null) return null;
        return product.getVariants().stream()
                .filter(v -> variantId.equals(v.getVariantId()))
                .findFirst()
                .orElse(null);
    }
}