package ps.emall.orderhub.order.item;

import lombok.*;
import ps.emall.orderhub.client.accounts.MallInfoDto;
import ps.emall.orderhub.client.accounts.ShopInfoDto;
import ps.emall.orderhub.client.catalog.ProductInfoDto;
import ps.emall.orderhub.returnrequest.ReturnRequestSummaryDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDto {
    private Long orderItemId;
    private Long shopOrderId;
    private Long cartId;
    private Long mallId;
    private Long shopId;

    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    private OrderItemStatus status;
    private LocalDateTime holdingExpiresAt;
    private Boolean hasReturnRequest;

    private ReturnRequestSummaryDto returnRequest;

    private ShopInfoDto shopInfo;
    private MallInfoDto mallInfo;
    private ProductInfoDto productInfo;
    private ProductInfoDto.VariantPriceInfoDto variantInfo;
}
