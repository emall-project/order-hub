package ps.emall.orderhub.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ps.emall.orderhub.cart.item.CartItemDto;
import ps.emall.orderhub.client.accounts.CityDto;
import ps.emall.orderhub.client.accounts.MallInfoDto;
import ps.emall.orderhub.client.accounts.UserInfoDto;
import ps.emall.orderhub.common.phone_number.PhoneNumberDto;
import ps.emall.orderhub.order.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDto {

    private Long cartId;
    private Long mallId;
    private Long customerId;

    @Positive(message = "cart.cityId.positive")
    private Long cityId;

    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private BigDecimal grandTotal;

    @Size(min = 2, max = 100, message = "cart.deliveryName.size")
    private String deliveryName;

    @Valid
    private PhoneNumberDto deliveryPhone;

    @Size(max = 255, message = "cart.deliveryNote.size")
    private String deliveryNote;

    @Size(min = 5, max = 255, message = "cart.deliveryLocation.size")
    private String deliveryLocation;

    private OrderStatus status;

    @Valid
    private List<CartItemDto> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MallInfoDto mallInfo;
    private UserInfoDto customerInfo;
    private CityDto cityInfo;
}
