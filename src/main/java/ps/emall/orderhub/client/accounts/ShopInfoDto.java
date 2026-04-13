package ps.emall.orderhub.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopInfoDto {
    private Long shopId;
    private String name;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private Boolean isActive;
}
