package ps.emall.orderhub.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.common.response.EMallsResponseEntity;
import ps.emall.orderhub.security.SecurityContextUtilBean;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final SecurityContextUtilBean auth;

    @GetMapping
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<PaginatedResponse<DeliveryDto>> getAll(
            DeliverySpec spec, Pageable pageable) {
        return EMallsResponseEntity.ok(deliveryService.getAll(spec, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<PaginatedResponse<DeliveryDto>> getByStatus(
            @PathVariable DeliveryStatus status,
            Pageable pageable) {
        return EMallsResponseEntity.ok(deliveryService.getByStatus(status, pageable));
    }

    @GetMapping("/{deliveryId}")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<DeliveryDto> getById(@PathVariable @Positive Long deliveryId) {
        return EMallsResponseEntity.ok(deliveryService.getById(deliveryId));
    }

    @GetMapping("/cart/{cartId}")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<DeliveryDto> getByCartId(@PathVariable @Positive Long cartId) {
        return EMallsResponseEntity.ok(deliveryService.getByCartId(cartId));
    }

    @GetMapping("/me/cart/{cartId}")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<DeliveryDto> getMyDeliveryByCartId(@PathVariable @Positive Long cartId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(deliveryService.getByCartIdForCustomer(cartId, customerId));
    }

    @GetMapping("/me")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<List<DeliveryDto>> getMyDeliveries() {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(deliveryService.getMyDeliveries(customerId));
    }

    @PatchMapping("/{deliveryId}/sent")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<DeliveryDto> markSent(@PathVariable @Positive Long deliveryId) {
        return EMallsResponseEntity.ok(deliveryService.markSent(deliveryId));
    }

    @PatchMapping("/{deliveryId}/on-the-way")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<DeliveryDto> markOnTheWay(@PathVariable @Positive Long deliveryId) {
        return EMallsResponseEntity.ok(deliveryService.markOnTheWay(deliveryId));
    }

    @PatchMapping("/{deliveryId}/delivered")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<DeliveryDto> markDelivered(@PathVariable @Positive Long deliveryId) {
        return EMallsResponseEntity.ok(deliveryService.markDelivered(deliveryId));
    }

    @PatchMapping("/{deliveryId}/failed")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<DeliveryDto> markFailed(
            @PathVariable @Positive Long deliveryId,
            @RequestBody @Valid MarkFailedRequest request) {
        return EMallsResponseEntity.ok(deliveryService.markFailed(deliveryId, request));
    }

    @GetMapping("/stats")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<Map<DeliveryStatus, Long>> getStats() {
        return EMallsResponseEntity.ok(deliveryService.getDeliveryCountsByStatus());
    }
}
