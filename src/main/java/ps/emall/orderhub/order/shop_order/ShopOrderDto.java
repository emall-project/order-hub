package ps.emall.orderhub.order.shop_order;

import lombok.*;
import ps.emall.orderhub.cart.CartDeliveryInfoDto;
import ps.emall.orderhub.client.accounts.MallInfoDto;
import ps.emall.orderhub.client.accounts.ShopInfoDto;
import ps.emall.orderhub.client.accounts.UserInfoDto;
import ps.emall.orderhub.order.item.OrderItemDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopOrderDto {
    private Long shopOrderId;
    private Long cartId;
    private Long shopId;
    private Long mallId;
    private Long customerId;

    private BigDecimal total;
    private ShopOrderStatus status;

    private List<OrderItemDto> items;

    private CartDeliveryInfoDto deliveryInfo;
    private String storeName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ShopInfoDto shopInfo;
    private MallInfoDto mallInfo;
    private UserInfoDto customerInfo;
}
