package ps.emall.orderhub.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarkFailedRequest {

    @NotBlank(message = "delivery.failureReason.notblank")
    @Size(min = 3, max = 500, message = "delivery.failureReason.size")
    private String failureReason;

    // true = customer refused, false = no one answered
    private boolean customerRejected;
}
