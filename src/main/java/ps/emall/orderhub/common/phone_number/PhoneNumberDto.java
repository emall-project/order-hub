package ps.emall.orderhub.common.phone_number;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneNumberDto {

    @Builder.Default
    private String prefix = "+972";

    @NotBlank
    @Pattern(
            regexp = "^05(9|6|4)\\d{7}$",
            message = "phone.number.invalid"
    )
    private String number;

    public String fullNumber() {
        return prefix + number;
    }
}