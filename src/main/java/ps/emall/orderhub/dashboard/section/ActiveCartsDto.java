package ps.emall.orderhub.dashboard.section;

import ps.emall.orderhub.cart.CartDto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActiveCartsDto {
    private List<CartDto> carts; // all ACTIVE carts for this customer
}
