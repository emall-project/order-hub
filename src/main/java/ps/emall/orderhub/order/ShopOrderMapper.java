package ps.emall.orderhub.order;

import ps.emall.orderhub.cart.CartMapper;
import ps.emall.orderhub.cart.CartRepository;
import ps.emall.orderhub.order.item.OrderItem;
import ps.emall.orderhub.order.item.OrderItemDto;
import ps.emall.orderhub.returnrequest.ReturnRequest;
import ps.emall.orderhub.returnrequest.ReturnRequestMapper;

import java.util.List;

public class ShopOrderMapper {

    private ShopOrderMapper() {}

    public static ShopOrderDto toDto(ShopOrder entity) {
        if (entity == null) return null;

        List<OrderItemDto> itemDtos = entity.getItems() == null ? List.of()
                : entity.getItems().stream().map(ShopOrderMapper::toItemDto).toList();

        return ShopOrderDto.builder()
                .shopOrderId(entity.getShopOrderId())
                .cartId(entity.getCartId())
                .shopId(entity.getShopId())
                .mallId(entity.getMallId())
                .customerId(entity.getCustomerId())
                .total(entity.getTotal())
                .status(entity.getStatus())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ShopOrderDto toDtoWithDeliveryInfo(ShopOrder entity,
                                                     CartRepository cartRepository) {
        ShopOrderDto dto = toDto(entity);
        if (dto == null) return null;

        cartRepository.findById(entity.getCartId())
                .ifPresent(cart -> dto.setDeliveryInfo(CartMapper.toDeliveryInfoDto(cart)));

        return dto;
    }

    public static OrderItemDto toItemDto(OrderItem entity) {
        if (entity == null) return null;

        return OrderItemDto.builder()
                .orderItemId(entity.getOrderItemId())
                .shopOrderId(entity.getShopOrder() != null
                        ? entity.getShopOrder().getShopOrderId() : null)
                .cartId(entity.getShopOrder() != null
                        ? entity.getShopOrder().getCartId() : null)
                .mallId(entity.getShopOrder() != null
                        ? entity.getShopOrder().getMallId() : null)
                .shopId(entity.getShopId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .variantId(entity.getVariantId())
                .variantName(entity.getVariantName())
                .unitPrice(entity.getUnitPrice())
                .quantity(entity.getQuantity())
                .lineTotal(entity.getLineTotal())
                .status(entity.getStatus())
                .holdingExpiresAt(entity.getHoldingExpiresAt())
                .hasReturnRequest(entity.getHasReturnRequest())
                .build();
    }

    public static OrderItemDto toItemDtoWithReturn(OrderItem entity,
                                                   ReturnRequest returnRequest) {
        OrderItemDto dto = toItemDto(entity);
        if (dto == null) return null;
        if (returnRequest != null) {
            dto.setReturnRequest(ReturnRequestMapper.toSummaryDto(returnRequest));
        }
        return dto;
    }

}
