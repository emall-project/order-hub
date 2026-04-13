package ps.emall.orderhub.dashboard.section;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerOrderKpiDto {
    private long totalOrders;
    private long activeOrders;  // NEW + PREPARING + READY_FOR_PICKUP + OUT_FOR_DELIVERY
    private long deliveredOrders;
    private long returnedOrders; // orders with at least one return request
}
