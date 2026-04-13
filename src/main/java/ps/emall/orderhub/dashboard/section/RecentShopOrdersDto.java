package ps.emall.orderhub.dashboard.section;

import lombok.*;
import ps.emall.orderhub.order.ShopOrderDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentShopOrdersDto {
    private List<ShopOrderDto> orders; // last 10
}
