package ps.emall.orderhub.order.order;

import lombok.*;
import ps.emall.orderhub.common.phone_number.PhoneNumberDto;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDeliveryInfoDto {
    private Long cityId;
    private String deliveryName;
    private PhoneNumberDto deliveryPhone;
    private String deliveryNote;
    private String deliveryLocation;
    private BigDecimal deliveryFee;
}