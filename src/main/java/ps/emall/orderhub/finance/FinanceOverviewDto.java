package ps.emall.orderhub.finance;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinanceOverviewDto {
    private long totalOrders;
    private long deliveredOrders;
    private long failedDeliveries;
    private long pendingReturnRequests;
    private long approvedReturnRequests;
    private long rejectedReturnRequests;
    private long itemsReadyForPayout;
    private long itemsInHoldingWindow;
    // Items whose return was rejected — store keeps payment, counted as earned.
    private long itemsReturnRejected;
    private String note;
}
