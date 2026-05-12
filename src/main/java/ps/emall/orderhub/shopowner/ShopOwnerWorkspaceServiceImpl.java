package ps.emall.orderhub.shopowner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.dashboard.DashboardService;
import ps.emall.orderhub.finance.FinanceService;
import ps.emall.orderhub.order.ShopOrderService;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopOwnerWorkspaceServiceImpl implements ShopOwnerWorkspaceService {

    private final DashboardService dashboardService;
    private final FinanceService financeService;
    private final ShopOrderService shopOrderService;

    @Override
    public ShopOwnerWorkspaceDto getWorkspace(Long shopId) {
        return ShopOwnerWorkspaceDto.builder()
                .shopId(shopId)
                .dashboard(dashboardService.getShopDashboard(shopId))
                .payout(financeService.getShopPayout(shopId))
                .returnStats(financeService.getShopReturnStats(shopId))
                .orderStats(shopOrderService.getOrderCountsByStatusForShop(shopId).entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().name(),
                                Map.Entry::getValue,
                                (first, second) -> first,
                                LinkedHashMap::new
                        )))
                .generatedAt(Instant.now())
                .build();
    }
}
