package ps.emall.orderhub.dashboard.section;

import ps.emall.orderhub.returnrequest.ReturnRequestDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingReturnsDto {
    private List<ReturnRequestDto> returns; // all PENDING for this shop
}
