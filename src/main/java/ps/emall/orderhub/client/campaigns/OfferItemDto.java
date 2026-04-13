package ps.emall.orderhub.client.campaigns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferItemDto {

    private Long offerItemId;
    private Long offerId;
    private Long productId;
    private OfferSummaryDto offer;
    private List<VariantPriceDto> variantPrices;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OfferSummaryDto {
        private Long offerId;
        private String title;
        private String discountType;
        private BigDecimal discountValue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariantPriceDto {
        private Long variantId;
        private String variantName;
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
        private Boolean isDefault;
        private String discountType;
        private BigDecimal discountValue;
    }
}