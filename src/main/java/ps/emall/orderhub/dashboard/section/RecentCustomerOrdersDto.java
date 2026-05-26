package ps.emall.orderhub.dashboard.section;

import ps.emall.orderhub.order.ShopOrderDto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentCustomerOrdersDto {
    private List<ShopOrderDto> orders; // last 10
}
