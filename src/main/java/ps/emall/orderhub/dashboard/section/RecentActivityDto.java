package ps.emall.orderhub.dashboard.section;

import java.util.List;
import lombok.*;
import ps.emall.orderhub.delivery.DeliveryDto;
import ps.emall.orderhub.order.shop_order.ShopOrderDto;
import ps.emall.orderhub.returnrequest.ReturnRequestDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentActivityDto {
    private List<ShopOrderDto> recentOrders;    // last 10 orders
    private List<ReturnRequestDto> recentReturns; // last 10 returns
    private List<DeliveryDto> recentDeliveries;   // last 10 deliveries
}
