package ps.emall.orderhub.client.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import ps.emall.orderhub.client.media.FileDto;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductLightDto {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private Long defaultVariantId;
    private BigDecimal basePrice;
    private Boolean hasDiscount;
    private BigDecimal discountedPrice;
    private FileDto medium;
    private String categoryName;
    private String brandName;
    private Boolean isActive;
    private Long variantsCount;
}
