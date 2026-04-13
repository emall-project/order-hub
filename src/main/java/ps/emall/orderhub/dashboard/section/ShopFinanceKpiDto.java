package ps.emall.orderhub.dashboard.section;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopFinanceKpiDto {
    private long itemsReadyForPayout;
    private long itemsReturnRejected;
    private long itemsInHolding;
    private BigDecimal readyForPayoutAmount;
    private BigDecimal earnedAmount;
    private BigDecimal totalDeliveredAmount;
}
