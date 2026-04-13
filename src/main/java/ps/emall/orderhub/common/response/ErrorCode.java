package ps.emall.orderhub.common.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorCode {
    private final String field;
    private final String message;
}
