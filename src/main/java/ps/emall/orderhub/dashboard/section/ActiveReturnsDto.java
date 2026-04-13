package ps.emall.orderhub.dashboard.section;

import ps.emall.orderhub.returnrequest.ReturnRequestDto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActiveReturnsDto {
    private List<ReturnRequestDto> returns; // PENDING returns for this customer
}
