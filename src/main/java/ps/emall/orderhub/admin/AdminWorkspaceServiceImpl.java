package ps.emall.orderhub.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.dashboard.DashboardService;
import ps.emall.orderhub.delivery.DeliveryService;
import ps.emall.orderhub.finance.FinanceService;
import ps.emall.orderhub.order.ShopOrderService;
import ps.emall.orderhub.returnrequest.ReturnRequestService;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminWorkspaceServiceImpl implements AdminWorkspaceService {

    private final DashboardService dashboardService;
    private final ShopOrderService shopOrderService;
    private final ReturnRequestService returnRequestService;
    private final DeliveryService deliveryService;
    private final FinanceService financeService;

    @Override
    public AdminWorkspaceDto getWorkspace() {
        return AdminWorkspaceDto.builder()
                .dashboard(dashboardService.getAdminDashboard())
                .orderStats(toStringKeyMap(shopOrderService.getOrderCountsByStatus()))
                .returnStats(toStringKeyMap(returnRequestService.getAllReturnStats()))
                .deliveryStats(toStringKeyMap(deliveryService.getDeliveryCountsByStatus()))
                .finance(financeService.getFinanceOverview())
                .itemDistribution(financeService.getOrderItemStatusDistribution())
                .generatedAt(Instant.now())
                .build();
    }

    private <E extends Enum<E>> Map<String, Long> toStringKeyMap(Map<E, Long> counts) {
        return counts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }
}
