package ps.emall.orderhub.delivery;

import ps.emall.orderhub.cart.CartMapper;
import ps.emall.orderhub.cart.CartRepository;
import ps.emall.orderhub.order.ShopOrder;
import ps.emall.orderhub.order.ShopOrderRepository;

import java.util.List;
import java.util.stream.Collectors;

public class DeliveryMapper {

    private DeliveryMapper() {}

    public static DeliveryDto toDto(Delivery entity) {
        if (entity == null) return null;
        return DeliveryDto.builder()
                .deliveryId(entity.getDeliveryId())
                .cartId(entity.getCartId())
                .deliveryCompanyId(entity.getDeliveryCompanyId())
                .trackingId(entity.getTrackingId())
                .status(entity.getStatus())
                .failureReason(entity.getFailureReason())
                .sentAt(entity.getSentAt())
                .deliveredAt(entity.getDeliveredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static DeliveryDto toDtoWithContext(Delivery entity,
                                               ShopOrderRepository shopOrderRepository,
                                               CartRepository cartRepository) {
        DeliveryDto dto = toDto(entity);
        if (dto == null) return null;

        List<Long> orderIds = shopOrderRepository.findByCartId(entity.getCartId())
                .stream()
                .map(ShopOrder::getShopOrderId)
                .collect(Collectors.toList());
        dto.setShopOrderIds(orderIds);

        cartRepository.findById(entity.getCartId())
                .ifPresent(cart -> dto.setDeliveryInfo(CartMapper.toDeliveryInfoDto(cart)));

        return dto;
    }
}
