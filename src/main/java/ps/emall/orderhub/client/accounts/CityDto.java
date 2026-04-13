package ps.emall.orderhub.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityDto {
    private Long cityId;
    private String name;
    private BigDecimal baseFee;
    private Boolean isActive;
}
