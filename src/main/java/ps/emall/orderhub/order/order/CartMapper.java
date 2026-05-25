package ps.emall.orderhub.order.order;

import ps.emall.orderhub.cart.Cart;
import ps.emall.orderhub.cart.CartDto;
import ps.emall.orderhub.cart.item.CartItem;
import ps.emall.orderhub.cart.item.CartItemDto;
import ps.emall.orderhub.common.phone_number.PhoneNumberMapper;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    private CartMapper() {}

    public static CartDto toDto(Cart entity) {
        if (entity == null) return null;

        List<CartItemDto> itemDtos = entity.getItems() == null ? List.of()
                : entity.getItems().stream().map(CartMapper::toItemDto).toList();

        BigDecimal total = entity.getTotalAmount() != null ? entity.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal fee = entity.getDeliveryFee()  != null ? entity.getDeliveryFee()  : BigDecimal.ZERO;

        return CartDto.builder()
                .cartId(entity.getCartId())
                .mallId(entity.getMallId())
                .customerId(entity.getCustomerId())
                .cityId(entity.getCityId())
                .deliveryFee(fee)
                .totalAmount(total)
                .grandTotal(total.add(fee))
                .deliveryName(entity.getDeliveryName())
                .deliveryPhone(PhoneNumberMapper.fromPhoneString(entity.getDeliveryPhone()))
                .deliveryNote(entity.getDeliveryNote())
                .deliveryLocation(entity.getDeliveryLocation())
                .status(entity.getStatus())
                .items(itemDtos)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static CartItemDto toItemDto(CartItem entity) {
        if (entity == null) return null;

        BigDecimal effective = entity.getEffectiveUnitPrice();
        BigDecimal lineTotal = effective.multiply(BigDecimal.valueOf(entity.getQuantity()));

        return CartItemDto.builder()
                .cartItemId(entity.getCartItemId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .variantId(entity.getVariantId())
                .variantName(entity.getVariantName())
                .storeId(entity.getStoreId())
                .mallId(entity.getMallId())
                .basePrice(entity.getBasePrice())
                .discountedPrice(entity.getDiscountedPrice())
                .effectiveUnitPrice(effective)
                .quantity(entity.getQuantity())
                .lineTotal(lineTotal)
                .offerId(entity.getOfferId())
                .build();
    }

    public static CartDeliveryInfoDto toDeliveryInfoDto(Cart cart) {
        if (cart == null) return null;

        return CartDeliveryInfoDto.builder()
                .cityId(cart.getCityId())
                .deliveryName(cart.getDeliveryName())
                .deliveryPhone(PhoneNumberMapper.fromPhoneString(cart.getDeliveryPhone()))
                .deliveryNote(cart.getDeliveryNote())
                .deliveryLocation(cart.getDeliveryLocation())
                .deliveryFee(cart.getDeliveryFee())
                .build();
    }
}
