package ps.emall.orderhub.returnrequest;

import ps.emall.orderhub.order.ShopOrderMapper;

public class ReturnRequestMapper {

    private ReturnRequestMapper() {}

    public static ReturnRequestDto toDto(ReturnRequest entity) {
        if (entity == null) return null;

        Long shopOrderId = entity.getOrderItem() != null
                && entity.getOrderItem().getShopOrder() != null
                ? entity.getOrderItem().getShopOrder().getShopOrderId() : null;

        Long cartId = entity.getOrderItem() != null
                && entity.getOrderItem().getShopOrder() != null
                ? entity.getOrderItem().getShopOrder().getCartId() : null;

        return ReturnRequestDto.builder()
                .returnRequestId(entity.getReturnRequestId())
                .orderItemId(entity.getOrderItem() != null
                        ? entity.getOrderItem().getOrderItemId() : null)
                .shopOrderId(shopOrderId)
                .cartId(cartId)
                .customerId(entity.getCustomerId())
                .shopId(entity.getShopId())
                .reason(entity.getReason())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .imageUuid(entity.getImageUuid())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static ReturnRequestDto toDtoWithOrderItem(ReturnRequest entity) {
        ReturnRequestDto dto = toDto(entity);

        if (dto == null) return null;
        if (entity.getOrderItem() != null) {
            dto.setOrderItem(ShopOrderMapper.toItemDto(entity.getOrderItem()));
        }

        return dto;
    }

    public static ReturnRequestSummaryDto toSummaryDto(ReturnRequest entity) {
        if (entity == null) return null;

        return ReturnRequestSummaryDto.builder()
                .returnRequestId(entity.getReturnRequestId())
                .status(entity.getStatus())
                .reason(entity.getReason())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
