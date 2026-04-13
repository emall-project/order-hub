package ps.emall.orderhub.common.exception;


import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;

@Getter
public class EMallsException extends RuntimeException {
    private final List<ErrorCode> errorCode;
    private final List<Object> params;
    private final HttpStatus httpStatus;

    @Builder
    public EMallsException(
            List<ErrorCode> errorCode,
            List<Object> params,
            HttpStatus httpStatus,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
        this.httpStatus = httpStatus;
    }
}
