package ps.emall.orderhub.dashboard;

import lombok.*;
import ps.emall.orderhub.dashboard.section.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardDto {
    private OrderKpiDto orderKpis;
    private DeliveryKpiDto deliveryKpis;
    private ReturnKpiDto returnKpis;
    private FinanceKpiDto financeKpis;
    private OrderStatusBreakdownDto orderStatusBreakdown;
    private ItemStatusBreakdownDto itemStatusBreakdown;
    private DeliveryStatusBreakdownDto deliveryStatusBreakdown;
    private RecentActivityDto recentActivity;
}