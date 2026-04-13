package ps.emall.orderhub.delivery;

import lombok.*;
import ps.emall.orderhub.cart.CartDeliveryInfoDto;
import ps.emall.orderhub.cart.CartDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryDto {
    private Long deliveryId;

    private Long cartId;
    private CartDto cart;

    private Long deliveryCompanyId;
    private String trackingId;
    private DeliveryStatus status;
    private String failureReason;

    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Long> shopOrderIds;
    private CartDeliveryInfoDto deliveryInfo;

}
