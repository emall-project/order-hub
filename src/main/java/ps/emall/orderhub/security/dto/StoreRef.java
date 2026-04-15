package ps.emall.orderhub.security.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class StoreRef {
    private Long storeId;
    private Long mallId;
}