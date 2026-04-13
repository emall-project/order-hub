package ps.emall.orderhub.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@JsonPropertyOrder({
        "status",
        "message",
        "errorCodes",
        "data"
})
public class EMallsResponseEntity<T> {
    List<ErrorCode> errorCodes;
    private HttpStatus status;
    private String message;

    private T data;

    public static <T> EMallsResponseEntity<T> of(
            HttpStatus status,
            String message,
            T data
    ) {
        return EMallsResponseEntity.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity map(final HttpStatus status, final T data) {
        return EMallsResponseEntity.builder()
                .status(status)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity ok(final T data) {
        return EMallsResponseEntity.builder()
                .status(HttpStatus.OK)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity created(final T data) {
        return EMallsResponseEntity.builder()
                .status(HttpStatus.CREATED)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity noContent(final T data) {
        return EMallsResponseEntity.builder()
                .status(HttpStatus.NO_CONTENT)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity forbidden(final T data) {
        return EMallsResponseEntity.builder()
                .status(HttpStatus.FORBIDDEN)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity notFound(final T data) {
        return EMallsResponseEntity.builder()
                .status(HttpStatus.NOT_FOUND)
                .data(data)
                .build();
    }

    public static <T> EMallsResponseEntity unauthorized(final T data) {
        return EMallsResponseEntity.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .data(data)
                .build();
    }

}
