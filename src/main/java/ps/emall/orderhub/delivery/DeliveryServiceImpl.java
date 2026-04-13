package ps.emall.orderhub.delivery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.cart.CartRepository;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.order.ShopOrderRepository;
import ps.emall.orderhub.order.ShopOrderServiceImpl;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ShopOrderServiceImpl shopOrderService;
    private final ShopOrderRepository shopOrderRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getByCartId(Long cartId, Long customerId) {
        Delivery delivery = deliveryRepository.findByCartId(cartId)
                .orElseThrow(DeliveryExceptions::deliveryNotFound);

        if (customerId != null) {
            cartRepository.findById(cartId)
                    .filter(cart -> cart.getCustomerId().equals(customerId))
                    .orElseThrow(DeliveryExceptions::deliveryNotFound);
        }

        return DeliveryMapper.toDtoWithContext(delivery, shopOrderRepository, cartRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getById(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(DeliveryExceptions::deliveryNotFound);

        return DeliveryMapper.toDtoWithContext(delivery, shopOrderRepository, cartRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DeliveryDto> getAll(DeliverySpec spec, Pageable pageable) {
        Page<DeliveryDto> page = deliveryRepository.findAll(spec, pageable)
                .map(d -> DeliveryMapper.toDtoWithContext(d, shopOrderRepository, cartRepository));

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DeliveryDto> getByStatus(DeliveryStatus status, Pageable pageable) {
        Page<DeliveryDto> page = deliveryRepository.findByStatus(status, pageable)
                .map(d -> DeliveryMapper.toDtoWithContext(d, shopOrderRepository, cartRepository));

        return PaginatedResponse.of(page);
    }

    @Override
    public DeliveryDto markSent(Long deliveryId) {
        Delivery delivery = findAndGuardTerminal(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.CREATED) {
            throw DeliveryExceptions.invalidStatusTransition(delivery.getStatus(), DeliveryStatus.SENT);
        }

        delivery.setStatus(DeliveryStatus.SENT);
        delivery.setSentAt(LocalDateTime.now());
        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery marked SENT: deliveryId={}, cartId={}", deliveryId, delivery.getCartId());
        return DeliveryMapper.toDtoWithContext(saved, shopOrderRepository, cartRepository);
    }

    @Override
    public DeliveryDto markOnTheWay(Long deliveryId) {
        Delivery delivery = findAndGuardTerminal(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.SENT) {
            throw DeliveryExceptions.invalidStatusTransition(delivery.getStatus(), DeliveryStatus.ON_THE_WAY);
        }

        delivery.setStatus(DeliveryStatus.ON_THE_WAY);
        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery marked ON_THE_WAY: deliveryId={}, cartId={}", deliveryId, delivery.getCartId());
        return DeliveryMapper.toDtoWithContext(saved, shopOrderRepository, cartRepository);
    }

    @Override
    public DeliveryDto markDelivered(Long deliveryId) {
        Delivery delivery = findAndGuardTerminal(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.ON_THE_WAY) {
            throw DeliveryExceptions.invalidStatusTransition(delivery.getStatus(), DeliveryStatus.DELIVERED);
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        Delivery saved = deliveryRepository.save(delivery);

        // Cascade: mark all linked shop orders DELIVERED and open the holding window
        shopOrderService.onDeliveryCompleted(delivery.getCartId());

        log.info("Delivery marked DELIVERED: deliveryId={}, cartId={}", deliveryId, delivery.getCartId());
        return DeliveryMapper.toDtoWithContext(saved, shopOrderRepository, cartRepository);
    }

    @Override
    public DeliveryDto markFailed(Long deliveryId, MarkFailedRequest request) {
        Delivery delivery = findAndGuardTerminal(deliveryId);

        // Can mark failed from SENT or ON_THE_WAY — driver couldn't complete delivery
        if (delivery.getStatus() != DeliveryStatus.SENT
                && delivery.getStatus() != DeliveryStatus.ON_THE_WAY) {
            throw DeliveryExceptions.invalidStatusTransition(delivery.getStatus(), DeliveryStatus.FAILED);
        }

        if (request.getFailureReason() == null || request.getFailureReason().isBlank()) {
            throw DeliveryExceptions.failureReasonRequired();
        }

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setFailureReason(request.getFailureReason());
        Delivery saved = deliveryRepository.save(delivery);

        // Cascade: mark all linked shop orders with the appropriate failure status
        shopOrderService.onDeliveryFailed(delivery.getCartId(), request.isCustomerRejected());

        log.info("Delivery marked FAILED: deliveryId={}, cartId={}, reason={}",
                deliveryId, delivery.getCartId(), request.getFailureReason());
        return DeliveryMapper.toDtoWithContext(saved, shopOrderRepository, cartRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<DeliveryStatus, Long> getDeliveryCountsByStatus() {
        Map<DeliveryStatus, Long> counts = new EnumMap<>(DeliveryStatus.class);

        for (DeliveryStatus status : DeliveryStatus.values()) {
            counts.put(status, deliveryRepository.countByStatus(status));
        }
        return counts;
    }

    // Helpers

    private Delivery findAndGuardTerminal(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(DeliveryExceptions::deliveryNotFound);

        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw DeliveryExceptions.deliveryAlreadyDelivered();
        }
        if (delivery.getStatus() == DeliveryStatus.FAILED) {
            throw DeliveryExceptions.deliveryAlreadyFailed();
        }
        return delivery;
    }
}
