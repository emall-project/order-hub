package ps.emall.orderhub.cart.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCartItemRequest {

    @NotNull(message = "cartItem.quantity.notnull")
    @Min(value = 1, message = "cartItem.quantity.min")
    private Integer quantity;
}
