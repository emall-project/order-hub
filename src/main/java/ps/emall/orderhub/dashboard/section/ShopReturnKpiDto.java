package ps.emall.orderhub.dashboard.section;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopReturnKpiDto {
    private long pendingReturns;
    private long approvedReturns;
    private long rejectedReturns;
}
