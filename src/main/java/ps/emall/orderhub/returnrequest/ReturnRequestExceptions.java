package ps.emall.orderhub.returnrequest;

import org.springframework.http.HttpStatus;
import ps.emall.orderhub.common.exception.EMallsException;
import ps.emall.orderhub.common.message.MessageKey;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;

public final class ReturnRequestExceptions {

    private ReturnRequestExceptions() {}

    public static EMallsException returnRequestNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.RETURN_REQUEST_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("returnRequestId",
                        MessageKey.RETURN_REQUEST_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException returnRequestAlreadyExists() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .message(MessageKey.RETURN_REQUEST_ALREADY_EXISTS.getKey())
                .errorCode(List.of(new ErrorCode("orderItemId",
                        MessageKey.RETURN_REQUEST_ALREADY_EXISTS.getKey())))
                .build();
    }

    public static EMallsException itemNotInHoldingStatus() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.RETURN_REQUEST_ITEM_NOT_IN_HOLDING.getKey())
                .errorCode(List.of(new ErrorCode("orderItemId",
                        MessageKey.RETURN_REQUEST_ITEM_NOT_IN_HOLDING.getKey())))
                .build();
    }

    public static EMallsException returnAlreadyProcessed() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.RETURN_REQUEST_ALREADY_PROCESSED.getKey())
                .errorCode(List.of(new ErrorCode("returnRequestId",
                        MessageKey.RETURN_REQUEST_ALREADY_PROCESSED.getKey())))
                .build();
    }

    public static EMallsException rejectionReasonRequired() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.RETURN_REJECTION_REASON_REQUIRED.getKey())
                .errorCode(List.of(new ErrorCode("rejectionReason",
                        MessageKey.RETURN_REJECTION_REASON_REQUIRED.getKey())))
                .build();
    }

    public static EMallsException returnDoesNotBelongToCustomer() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.RETURN_REQUEST_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("returnRequestId",
                        MessageKey.RETURN_REQUEST_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException returnDoesNotBelongToShop() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .message(MessageKey.RETURN_DOES_NOT_BELONG_TO_SHOP.getKey())
                .errorCode(List.of(new ErrorCode("returnRequestId",
                        MessageKey.RETURN_DOES_NOT_BELONG_TO_SHOP.getKey())))
                .build();
    }

    public static EMallsException imageNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message("returnRequest.image.not.found")
                .errorCode(List.of(new ErrorCode("imageUuid", "returnRequest.image.not.found")))
                .build();
    }

    public static EMallsException invalidFileType() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message("returnRequest.image.invalid.type")
                .errorCode(List.of(new ErrorCode("imageUuid", "returnRequest.image.invalid.type")))
                .build();
    }

    public static EMallsException mediaServiceUnavailable() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .message("returnRequest.media.service.unavailable")
                .errorCode(List.of(new ErrorCode("imageUuid", "returnRequest.media.service.unavailable")))
                .build();
    }
}
