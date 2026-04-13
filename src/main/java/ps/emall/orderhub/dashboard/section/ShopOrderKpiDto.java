package ps.emall.orderhub.dashboard.section;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderKpiDto {
    private long totalOrders;
    private long newOrders;
    private long preparingOrders;
    private long readyForPickup;
    private long outForDelivery;
    private long deliveredOrders;
}
