package ps.emall.orderhub.admin;

import lombok.*;
import ps.emall.orderhub.dashboard.AdminDashboardDto;
import ps.emall.orderhub.finance.FinanceOverviewDto;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminWorkspaceDto {
    private AdminDashboardDto dashboard;
    private Map<String, Long> orderStats;
    private Map<String, Long> returnStats;
    private Map<String, Long> deliveryStats;
    private FinanceOverviewDto finance;
    private Map<String, Long> itemDistribution;
    private Instant generatedAt;
}
