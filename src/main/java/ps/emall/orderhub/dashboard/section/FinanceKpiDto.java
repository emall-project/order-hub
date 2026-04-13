package ps.emall.orderhub.dashboard.section;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinanceKpiDto {
    private long itemsInHolding;
    private long itemsReadyForPayout;
    private long itemsReturnRejected;
}
