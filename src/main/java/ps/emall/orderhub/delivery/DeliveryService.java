package ps.emall.orderhub.delivery;

import org.springframework.data.domain.Pageable;
import ps.emall.orderhub.common.page.PaginatedResponse;

import java.util.List;
import java.util.Map;

public interface DeliveryService {

    DeliveryDto getByCartId(Long cartId);

    DeliveryDto getByCartIdForCustomer(Long cartId, Long customerId);

    DeliveryDto getById(Long deliveryId);

    PaginatedResponse<DeliveryDto> getAll(DeliverySpec spec, Pageable pageable);

    PaginatedResponse<DeliveryDto> getByStatus(DeliveryStatus status, Pageable pageable);

    List<DeliveryDto> getMyDeliveries(Long customerId);

    // Admin advances delivery through states
    DeliveryDto markSent(Long deliveryId);

    DeliveryDto markOnTheWay(Long deliveryId);

    DeliveryDto markDelivered(Long deliveryId);

    DeliveryDto markFailed(Long deliveryId, MarkFailedRequest request);

    Map<DeliveryStatus, Long> getDeliveryCountsByStatus();
}
