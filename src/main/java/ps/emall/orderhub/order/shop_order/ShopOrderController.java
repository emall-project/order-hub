package ps.emall.orderhub.order.shop_order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.common.response.EMallsResponseEntity;
import ps.emall.orderhub.security.SecurityContextUtilBean;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class ShopOrderController {

    private final ShopOrderService shopOrderService;
    private final SecurityContextUtilBean auth;

    // Customer

    @GetMapping("/me")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<PaginatedResponse<ShopOrderDto>> getMyOrders(Pageable pageable) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(shopOrderService.getMyOrders(customerId, pageable));
    }

    @GetMapping("/me/{shopOrderId}")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<ShopOrderDto> getMyOrderById(
            @PathVariable @Positive Long shopOrderId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(shopOrderService.getMyOrderById(shopOrderId, customerId));
    }

    // Store owner

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<PaginatedResponse<ShopOrderDto>> getOrdersByShop(
            @PathVariable @Positive Long shopId,
            ShopOrderSpec spec,
            Pageable pageable) {
        return EMallsResponseEntity.ok(shopOrderService.getOrdersByShop(shopId, spec, pageable));
    }

    @GetMapping("/shop/{shopId}/status/{status}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<PaginatedResponse<ShopOrderDto>> getOrdersByShopAndStatus(
            @PathVariable @Positive Long shopId,
            @PathVariable ShopOrderStatus status,
            Pageable pageable) {
        return EMallsResponseEntity.ok(
                shopOrderService.getOrdersByShopAndStatus(shopId, status, pageable));
    }

    @GetMapping("/shop/{shopId}/{shopOrderId}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ShopOrderDto> getOrderByIdForShop(
            @PathVariable @Positive Long shopId,
            @PathVariable @Positive Long shopOrderId) {
        return EMallsResponseEntity.ok(shopOrderService.getOrderByIdForShop(shopOrderId, shopId));
    }

    @PatchMapping("/shop/{shopId}/{shopOrderId}/advance")
    @PreAuthorize("@auth.isShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ShopOrderDto> advanceStatus(
            @PathVariable @Positive Long shopId,
            @PathVariable @Positive Long shopOrderId) {
        return EMallsResponseEntity.ok(shopOrderService.advanceStatus(shopOrderId, shopId));
    }

    @GetMapping("/shop/{shopId}/stats")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<Map<ShopOrderStatus, Long>> getShopStats(
            @PathVariable @Positive Long shopId) {
        return EMallsResponseEntity.ok(shopOrderService.getOrderCountsByStatusForShop(shopId));
    }

    // Admin

    @GetMapping("/admin")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<PaginatedResponse<ShopOrderDto>> getAllOrders(
            ShopOrderSpec spec, Pageable pageable) {
        return EMallsResponseEntity.ok(shopOrderService.getAllOrders(spec, pageable));
    }

    @GetMapping("/admin/{shopOrderId}")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<ShopOrderDto> getOrderByIdForAdmin(
            @PathVariable @Positive Long shopOrderId) {
        return EMallsResponseEntity.ok(shopOrderService.getOrderByIdForAdmin(shopOrderId));
    }

    @PatchMapping("/admin/{shopOrderId}/override")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<ShopOrderDto> overrideStatus(
            @PathVariable @Positive Long shopOrderId,
            @RequestParam @NotNull ShopOrderStatus targetStatus,
            @RequestParam @NotBlank String reason) {
        return EMallsResponseEntity.ok(shopOrderService.overrideStatus(shopOrderId, targetStatus, reason));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<Map<ShopOrderStatus, Long>> getAdminStats() {
        return EMallsResponseEntity.ok(shopOrderService.getOrderCountsByStatus());
    }
}
