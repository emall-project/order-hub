package ps.emall.orderhub.dashboard.section;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnKpiDto {
    private long totalReturns;
    private long pendingReturns;
    private long approvedReturns;
    private long rejectedReturns;
}
