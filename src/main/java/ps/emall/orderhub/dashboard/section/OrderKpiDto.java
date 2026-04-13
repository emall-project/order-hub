package ps.emall.orderhub.dashboard.section;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderKpiDto {
    private long totalOrders;
    private long newOrders;
    private long ordersInProgress;  // PREPARING + READY_FOR_PICKUP + OUT_FOR_DELIVERY
    private long deliveredOrders;
    private long failedOrders;      // CUSTOMER_REJECTED + NO_RESPONSE
}
