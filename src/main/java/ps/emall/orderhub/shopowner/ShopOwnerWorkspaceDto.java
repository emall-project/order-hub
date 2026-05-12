package ps.emall.orderhub.shopowner;

import lombok.*;
import ps.emall.orderhub.dashboard.ShopDashboardDto;
import ps.emall.orderhub.finance.ShopPayoutDto;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOwnerWorkspaceDto {
    private Long shopId;
    private ShopDashboardDto dashboard;
    private ShopPayoutDto payout;
    private Map<String, Long> returnStats;
    private Map<String, Long> orderStats;
    private Instant generatedAt;
}
