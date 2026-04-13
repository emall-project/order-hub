package ps.emall.orderhub.common.phone_number;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhoneNumber {

    private String prefix;
    private String number;

    public String fullNumber() {
        return prefix + number;
    }
}
