package ps.emall.orderhub.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MallInfoDto {
    private Long mallId;
    private String name;
    private String location;
    private String status;
    private Boolean isActive;
}