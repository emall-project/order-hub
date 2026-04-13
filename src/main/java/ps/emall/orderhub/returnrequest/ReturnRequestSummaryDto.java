package ps.emall.orderhub.returnrequest;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnRequestSummaryDto {
    private Long returnRequestId;
    private ReturnRequestStatus status;
    private String reason;
    private String rejectionReason;
    private LocalDateTime createdAt;
}