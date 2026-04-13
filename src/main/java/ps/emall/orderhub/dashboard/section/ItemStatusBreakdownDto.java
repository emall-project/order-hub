package ps.emall.orderhub.dashboard.section;

import java.util.Map;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemStatusBreakdownDto {
    private Map<String, Long> byStatus;
}
