package ps.emall.orderhub.order.shop_order;

import org.springframework.http.HttpStatus;
import ps.emall.orderhub.common.exception.EMallsException;
import ps.emall.orderhub.common.message.MessageKey;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;

public final class ShopOrderExceptions {

    private ShopOrderExceptions() {}

    public static EMallsException shopOrderNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.SHOP_ORDER_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("shopOrderId", MessageKey.SHOP_ORDER_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException shopOrderDoesNotBelongToCustomer() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.SHOP_ORDER_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("shopOrderId", MessageKey.SHOP_ORDER_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException shopOrderDoesNotBelongToShop() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .message(MessageKey.SHOP_ORDER_DOES_NOT_BELONG_TO_SHOP.getKey())
                .errorCode(List.of(new ErrorCode("shopOrderId", MessageKey.SHOP_ORDER_DOES_NOT_BELONG_TO_SHOP.getKey())))
                .build();
    }

    public static EMallsException invalidStatusTransition(ShopOrderStatus from, ShopOrderStatus to) {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.SHOP_ORDER_INVALID_STATUS_TRANSITION.getKey())
                .errorCode(List.of(new ErrorCode("status",
                        "Cannot transition from " + from + " to " + to)))
                .build();
    }

    public static EMallsException orderItemNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.ORDER_ITEM_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("orderItemId", MessageKey.ORDER_ITEM_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException shopNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.SHOP_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("shopId", MessageKey.SHOP_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException shopNotActive() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.SHOP_NOT_ACTIVE.getKey())
                .errorCode(List.of(new ErrorCode("shopId", MessageKey.SHOP_NOT_ACTIVE.getKey())))
                .build();
    }
}
