package ps.emall.orderhub.returnrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RejectReturnRequest {

    @NotBlank(message = "returnRequest.rejectionReason.notblank")
    @Size(min = 3, max = 500, message = "returnRequest.rejectionReason.size")
    private String rejectionReason;
}
