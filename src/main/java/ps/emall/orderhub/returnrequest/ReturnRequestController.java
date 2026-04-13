package ps.emall.orderhub.returnrequest;

import jakarta.validation.Valid;
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
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final SecurityContextUtilBean auth;

    // Customer

    @PostMapping
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<ReturnRequestDto> submit(
            @RequestBody @Valid ReturnRequestDto dto) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.created(returnRequestService.submit(customerId, dto));
    }

    @GetMapping("/me")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<PaginatedResponse<ReturnRequestDto>> getMyReturns(
            Pageable pageable) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(
                returnRequestService.getMyReturnRequests(customerId, pageable));
    }

    @GetMapping("/me/{returnRequestId}")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<ReturnRequestDto> getMyReturnById(
            @PathVariable @Positive Long returnRequestId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(
                returnRequestService.getMyReturnById(returnRequestId, customerId));
    }

    @GetMapping("/me/order-item/{orderItemId}")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<ReturnRequestDto> getByOrderItemId(
            @PathVariable @Positive Long orderItemId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(
                returnRequestService.getByOrderItemId(orderItemId, customerId));
    }

    // Store owner

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<PaginatedResponse<ReturnRequestDto>> getReturnsByShop(
            @PathVariable @Positive Long shopId,
            ReturnRequestSpec spec,
            Pageable pageable) {
        return EMallsResponseEntity.ok(
                returnRequestService.getReturnsByShop(shopId, spec, pageable));
    }

    @GetMapping("/shop/{shopId}/status/{status}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<PaginatedResponse<ReturnRequestDto>> getReturnsByShopAndStatus(
            @PathVariable @Positive Long shopId,
            @PathVariable ReturnRequestStatus status,
            Pageable pageable) {
        return EMallsResponseEntity.ok(
                returnRequestService.getReturnsByShopAndStatus(shopId, status, pageable));
    }

    @GetMapping("/shop/{shopId}/{returnRequestId}")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ReturnRequestDto> getReturnByIdForShop(
            @PathVariable @Positive Long shopId,
            @PathVariable @Positive Long returnRequestId) {
        return EMallsResponseEntity.ok(
                returnRequestService.getReturnByIdForShop(returnRequestId, shopId));
    }

    @PatchMapping("/shop/{shopId}/{returnRequestId}/approve")
    @PreAuthorize("@auth.isShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ReturnRequestDto> approve(
            @PathVariable @Positive Long shopId,
            @PathVariable @Positive Long returnRequestId) {
        return EMallsResponseEntity.ok(returnRequestService.approve(returnRequestId, shopId));
    }

    @PatchMapping("/shop/{shopId}/{returnRequestId}/reject")
    @PreAuthorize("@auth.isShopOwnerOf(#shopId)")
    public EMallsResponseEntity<ReturnRequestDto> reject(
            @PathVariable @Positive Long shopId,
            @PathVariable @Positive Long returnRequestId,
            @RequestBody @Valid RejectReturnRequest request) {
        return EMallsResponseEntity.ok(
                returnRequestService.reject(returnRequestId, shopId, request));
    }

    @GetMapping("/shop/{shopId}/stats")
    @PreAuthorize("@auth.isAdminOrShopOwnerOf(#shopId)")
    public EMallsResponseEntity<Map<ReturnRequestStatus, Long>> getShopReturnStats(
            @PathVariable @Positive Long shopId) {
        return EMallsResponseEntity.ok(returnRequestService.getReturnStatsByShop(shopId));
    }

    // Admin

    @GetMapping("/admin")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<PaginatedResponse<ReturnRequestDto>> getAllReturns(
            ReturnRequestSpec spec, Pageable pageable) {
        return EMallsResponseEntity.ok(returnRequestService.getAllReturns(spec, pageable));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<PaginatedResponse<ReturnRequestDto>> getAllReturnsByStatus(
            @PathVariable ReturnRequestStatus status,
            Pageable pageable) {
        return EMallsResponseEntity.ok(
                returnRequestService.getAllReturnsByStatus(status, pageable));
    }

    @GetMapping("/admin/{returnRequestId}")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<ReturnRequestDto> getReturnByIdForAdmin(
            @PathVariable @Positive Long returnRequestId) {
        return EMallsResponseEntity.ok(
                returnRequestService.getReturnByIdForAdmin(returnRequestId));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<Map<ReturnRequestStatus, Long>> getAdminReturnStats() {
        return EMallsResponseEntity.ok(returnRequestService.getAllReturnStats());
    }
}
