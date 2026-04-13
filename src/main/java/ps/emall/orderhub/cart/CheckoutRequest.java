package ps.emall.orderhub.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ps.emall.orderhub.common.phone_number.PhoneNumberDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutRequest {

    @NotNull(message = "checkout.cityId.notnull")
    @Positive(message = "checkout.cityId.positive")
    private Long cityId;

    @NotBlank(message = "checkout.deliveryName.notblank")
    @Size(min = 2, max = 100, message = "checkout.deliveryName.size")
    private String deliveryName;

    @NotNull(message = "checkout.deliveryPhone.notnull")
    @Valid
    private PhoneNumberDto deliveryPhone;

    @NotBlank(message = "checkout.deliveryLocation.notblank")
    @Size(min = 5, max = 255, message = "checkout.deliveryLocation.size")
    private String deliveryLocation;

    @Size(max = 255, message = "checkout.deliveryNote.size")
    private String deliveryNote;
}
