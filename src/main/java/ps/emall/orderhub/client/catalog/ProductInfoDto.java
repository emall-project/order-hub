package ps.emall.orderhub.client.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import ps.emall.orderhub.client.media.FileDto;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfoDto {

    private Long productId;
    private String name;
    private String slug;
    private String shortDescription;
    private String categoryName;
    private String brandName;
    private Boolean isActive;
    private Long storeId;
    private Long mallId;
    private FileDto medium;
    private List<VariantPriceInfoDto> variants;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VariantPriceInfoDto {
        private Long variantId;
        private String variantName;
        private BigDecimal basePrice;
        private Boolean isDefault;
    }
}
