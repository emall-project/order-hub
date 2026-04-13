package ps.emall.orderhub.client.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaResponse<T> {
    private List<ErrorCode> errorCodes;
    private String status;
    private String message;
    private T data;
}
