package ps.emall.orderhub.cart.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ps.emall.orderhub.client.accounts.MallInfoDto;
import ps.emall.orderhub.client.accounts.ShopInfoDto;
import ps.emall.orderhub.client.campaigns.OfferInfoDto;
import ps.emall.orderhub.client.catalog.ProductInfoDto;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDto {

    private Long cartItemId;

    private Long productId;
    private String productName;

    private Long variantId;
    private String variantName;

    private Long storeId;
    private Long mallId;

    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private BigDecimal effectiveUnitPrice;

    @NotNull(message = "cartItem.quantity.notnull")
    @Min(value = 1, message = "cartItem.quantity.min")
    private Integer quantity;

    private BigDecimal lineTotal;
    private Long offerId;

    private ProductInfoDto productInfo;
    private ProductInfoDto.VariantPriceInfoDto variantInfo;
    private ShopInfoDto storeInfo;
    private MallInfoDto mallInfo;
    private OfferInfoDto offerInfo;
}
