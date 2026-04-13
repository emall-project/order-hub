package ps.emall.orderhub.delivery;

import org.springframework.http.HttpStatus;
import ps.emall.orderhub.common.exception.EMallsException;
import ps.emall.orderhub.common.message.MessageKey;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;

public final class DeliveryExceptions {

    private DeliveryExceptions() {}

    public static EMallsException deliveryNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.DELIVERY_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("deliveryId", MessageKey.DELIVERY_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException deliveryAlreadyDelivered() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.DELIVERY_ALREADY_DELIVERED.getKey())
                .errorCode(List.of(new ErrorCode("status", MessageKey.DELIVERY_ALREADY_DELIVERED.getKey())))
                .build();
    }

    public static EMallsException deliveryAlreadyFailed() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.DELIVERY_ALREADY_FAILED.getKey())
                .errorCode(List.of(new ErrorCode("status", MessageKey.DELIVERY_ALREADY_FAILED.getKey())))
                .build();
    }

    public static EMallsException invalidStatusTransition(DeliveryStatus from, DeliveryStatus to) {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.DELIVERY_INVALID_STATUS_TRANSITION.getKey())
                .errorCode(List.of(new ErrorCode("status",
                        "Cannot transition delivery from " + from + " to " + to)))
                .build();
    }

    public static EMallsException failureReasonRequired() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.DELIVERY_FAILURE_REASON_REQUIRED.getKey())
                .errorCode(List.of(new ErrorCode("failureReason",
                        MessageKey.DELIVERY_FAILURE_REASON_REQUIRED.getKey())))
                .build();
    }
}
