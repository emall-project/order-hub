package ps.emall.orderhub.dashboard;

import lombok.*;
import ps.emall.orderhub.dashboard.section.ActiveCartsDto;
import ps.emall.orderhub.dashboard.section.ActiveReturnsDto;
import ps.emall.orderhub.dashboard.section.CustomerOrderKpiDto;
import ps.emall.orderhub.dashboard.section.RecentCustomerOrdersDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDashboardDto {

    private Long customerId;
    private CustomerOrderKpiDto orderKpis;
    private ActiveCartsDto activeCarts;
    private RecentCustomerOrdersDto recentOrders;
    private ActiveReturnsDto activeReturns;
}