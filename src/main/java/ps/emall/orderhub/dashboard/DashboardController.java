package ps.emall.orderhub.dashboard;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.common.response.EMallsResponseEntity;
import ps.emall.orderhub.dashboard.section.ProductInsightDto;
import ps.emall.orderhub.security.SecurityContextUtilBean;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityContextUtilBean auth;

    // Admin

    @GetMapping("/admin")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<AdminDashboardDto> getAdminDashboard() {
        return EMallsResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    // Shop owner

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ShopDashboardDto> getShopDashboard(
            @PathVariable @Positive Long shopId) {
        return EMallsResponseEntity.ok(dashboardService.getShopDashboard(shopId));
    }

    // Customer

    @GetMapping("/customer")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<CustomerDashboardDto> getCustomerDashboard() {
       Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(dashboardService.getCustomerDashboard(customerId));
    }

    @GetMapping("/products/most-ordered")
    @PreAuthorize("@auth.isAdmin() or (#shopId != null and @auth.isShopOwnerOf(#shopId))")
    public EMallsResponseEntity<List<ProductInsightDto>> getMostOrderedProducts(
            @RequestParam(required = false) @Positive Long shopId,
            @RequestParam(defaultValue = "10") @Positive Integer limit) {
        return EMallsResponseEntity.ok(dashboardService.getMostOrderedProducts(shopId, limit));
    }

    @GetMapping("/products/discounted")
    @PreAuthorize("@auth.isAdmin() or (#shopId != null and @auth.isShopOwnerOf(#shopId))")
    public EMallsResponseEntity<List<ProductInsightDto>> getDiscountedOrderedProducts(
            @RequestParam(required = false) @Positive Long shopId,
            @RequestParam(defaultValue = "10") @Positive Integer limit) {
        return EMallsResponseEntity.ok(dashboardService.getDiscountedOrderedProducts(shopId, limit));
    }
}
