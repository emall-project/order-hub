package ps.emall.orderhub.dashboard.section;

import lombok.*;
import ps.emall.orderhub.client.catalog.ProductLightDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductInsightDto {
    private ProductLightDto product;
    private Long orderedQuantity;
}
