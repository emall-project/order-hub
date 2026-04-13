package ps.emall.orderhub.dashboard.section;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryKpiDto {
    private long totalDeliveries;
    private long pendingDeliveries; // CREATED + SENT + ON_THE_WAY
    private long deliveredCount;
    private long failedCount;
}
