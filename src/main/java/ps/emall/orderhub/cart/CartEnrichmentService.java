package ps.emall.orderhub.cart;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ps.emall.orderhub.cart.item.CartItemDto;
import ps.emall.orderhub.client.accounts.*;
import ps.emall.orderhub.client.campaigns.CampaignsClient;
import ps.emall.orderhub.client.campaigns.CampaignsOfferResponse;
import ps.emall.orderhub.client.campaigns.OfferInfoDto;
import ps.emall.orderhub.client.campaigns.OfferItemDto;
import ps.emall.orderhub.client.catalog.CatalogClient;
import ps.emall.orderhub.client.catalog.CatalogResponse;
import ps.emall.orderhub.client.catalog.ProductInfoDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartEnrichmentService {

    private final AccountsClient accountsClient;
    private final CatalogClient catalogClient;
    private final CampaignsClient campaignsClient;

    // CartDto enrichment

    public CartDto enrich(CartDto dto) {
        if (dto == null) return null;

        dto.setMallInfo(fetchMallInfo(dto.getMallId()));
        dto.setCustomerInfo(fetchCustomerInfo(dto.getCustomerId()));

        if (dto.getCityId() != null) {
            dto.setCityInfo(fetchCityInfo(dto.getCityId()));
        }

        if (dto.getItems() != null) {
            dto.getItems().forEach(this::enrichItem);
        }

        return dto;
    }

    public List<CartDto> enrich(List<CartDto> dtos) {
        if (dtos == null) return List.of();
        dtos.forEach(this::enrich);
        return dtos;
    }

    // CartItemDto enrichment

    public CartItemDto enrichItem(CartItemDto item) {
        if (item == null) return null;

        ProductInfoDto productInfo = fetchProductInfo(item.getProductId());
        item.setProductInfo(productInfo);

        if (productInfo != null && item.getVariantId() != null) {
            item.setVariantInfo(findVariant(productInfo, item.getVariantId()));
        }

        item.setStoreInfo(fetchShopInfo(item.getStoreId()));
        item.setMallInfo(fetchMallInfo(item.getMallId()));

        if (item.getOfferId() != null) {
            item.setOfferInfo(fetchOfferInfo(item.getProductId(), item.getVariantId()));
        }

        return item;
    }

    private MallInfoDto fetchMallInfo(Long mallId) {
        if (mallId == null) return null;
        try {
            AccountsMallResponse response = accountsClient.getMallById(mallId);
            return (response != null) ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Mall not found in Accounts service: mallId={}", mallId);
        } catch (FeignException e) {
            log.debug("Accounts service unavailable when fetching mallId={}, status={}", mallId, e.status());
        } catch (Exception e) {
            log.debug("Unexpected error fetching mallId={}: {}", mallId, e.getMessage());
        }
        return null;
    }

    private UserInfoDto fetchCustomerInfo(Long customerId) {
        if (customerId == null) return null;
        try {
            AccountsUserResponse response = accountsClient.getUserById(customerId);
            return (response != null) ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Customer not found in Accounts service: customerId={}", customerId);
        } catch (FeignException e) {
            log.debug("Accounts service unavailable when fetching customerId={}, status={}", customerId, e.status());
        } catch (Exception e) {
            log.debug("Unexpected error fetching customerId={}: {}", customerId, e.getMessage());
        }
        return null;
    }

    private CityDto fetchCityInfo(Long cityId) {
        if (cityId == null) return null;
        try {
            AccountsCityResponse response = accountsClient.getCityById(cityId);
            return (response != null) ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("City not found in Accounts service: cityId={}", cityId);
        } catch (FeignException e) {
            log.debug("Accounts service unavailable when fetching cityId={}, status={}", cityId, e.status());
        } catch (Exception e) {
            log.debug("Unexpected error fetching cityId={}: {}", cityId, e.getMessage());
        }
        return null;
    }

    private ShopInfoDto fetchShopInfo(Long shopId) {
        if (shopId == null) return null;
        try {
            AccountsShopResponse response = accountsClient.getShopById(shopId);
            return (response != null) ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Shop not found in Accounts service: shopId={}", shopId);
        } catch (FeignException e) {
            log.debug("Accounts service unavailable when fetching shopId={}, status={}", shopId, e.status());
        } catch (Exception e) {
            log.debug("Unexpected error fetching shopId={}: {}", shopId, e.getMessage());
        }
        return null;
    }

    private ProductInfoDto fetchProductInfo(Long productId) {
        if (productId == null) return null;
        try {
            CatalogResponse response = catalogClient.getProductInfo(productId);
            return (response != null) ? response.getData() : null;
        } catch (FeignException.NotFound e) {
            log.debug("Product not found in Catalog service: productId={}", productId);
        } catch (FeignException e) {
            log.debug("Catalog service unavailable when fetching productId={}, status={}", productId, e.status());
        } catch (Exception e) {
            log.debug("Unexpected error fetching productId={}: {}", productId, e.getMessage());
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

    private OfferInfoDto fetchOfferInfo(Long productId, Long variantId) {
        if (productId == null) return null;
        try {
            CampaignsOfferResponse response = campaignsClient.getActiveOfferForProduct(productId);
            if (response == null || response.getData() == null) return null;

            OfferItemDto item = response.getData();
            if (item.getOffer() == null) return null;

            OfferItemDto.OfferSummaryDto summary = item.getOffer();
            return OfferInfoDto.builder()
                    .offerId(summary.getOfferId())
                    .title(summary.getTitle())
                    .discountType(summary.getDiscountType())
                    .discountValue(summary.getDiscountValue())
                    .build();

        } catch (FeignException.NotFound e) {
            log.debug("No active offer found in Campaigns for productId={}", productId);
        } catch (FeignException e) {
            log.debug("Campaigns service unavailable for productId={}, status={}", productId, e.status());
        } catch (Exception e) {
            log.debug("Unexpected error fetching offer for productId={}: {}", productId, e.getMessage());
        }
        return null;
    }
}
