package ps.emall.orderhub.cart.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartRequest {

    @NotNull(message = "cart.mallId.notnull")
    @Positive(message = "cart.mallId.positive")
    private Long mallId;

    @NotNull(message = "cart.productId.notnull")
    @Positive(message = "cart.productId.positive")
    private Long productId;

    @NotNull(message = "cart.variantId.notnull")
    @Positive(message = "cart.variantId.positive")
    private Long variantId;

    @NotNull(message = "cartItem.quantity.notnull")
    @Min(value = 1, message = "cartItem.quantity.min")
    private Integer quantity;
}
