package ps.emall.orderhub.finance;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.common.response.EMallsResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceDashboardController {

    private final FinanceService financeService;

    @GetMapping("/overview")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<FinanceOverviewDto> getOverview() {
        return EMallsResponseEntity.ok(financeService.getFinanceOverview());
    }

    @GetMapping("/shops/{shopId}/payout")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ShopPayoutDto> getShopPayout(
            @PathVariable @Positive Long shopId) {
        return EMallsResponseEntity.ok(financeService.getShopPayout(shopId));
    }

    @GetMapping("/items/distribution")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<Map<String, Long>> getItemDistribution() {
        return EMallsResponseEntity.ok(financeService.getOrderItemStatusDistribution());
    }

    @GetMapping("/shops/{shopId}/return-stats")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<Map<String, Long>> getShopReturnStats(
            @PathVariable @Positive Long shopId) {
        return EMallsResponseEntity.ok(financeService.getShopReturnStats(shopId));
    }
}