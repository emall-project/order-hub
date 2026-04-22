package ps.emall.orderhub.returnrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import ps.emall.orderhub.client.accounts.ShopInfoDto;
import ps.emall.orderhub.client.accounts.UserInfoDto;
import ps.emall.orderhub.client.media.FileDto;
import ps.emall.orderhub.order.item.OrderItemDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnRequestDto {

    private Long returnRequestId;

    @NotNull(message = "returnRequest.orderItemId.notnull")
    @Positive(message = "returnRequest.orderItemId.positive")
    private Long orderItemId;

    private Long shopOrderId;
    private Long cartId;

    private Long customerId;
    private Long shopId;

    @NotBlank(message = "returnRequest.reason.notblank")
    @Size(min = 3, max = 500, message = "returnRequest.reason.size")
    private String reason;

    @Size(max = 2000, message = "returnRequest.description.size")
    private String description;

    private ReturnRequestStatus status;

    private String rejectionReason;

    @NotNull(message = "returnRequest.imageUuid.notnull")
    private UUID imageUuid;
    private FileDto image;

    private OrderItemDto orderItem;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserInfoDto customerInfo;
    private ShopInfoDto shopInfo;
}
