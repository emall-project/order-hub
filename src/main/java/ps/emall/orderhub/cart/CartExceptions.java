package ps.emall.orderhub.cart;

import org.springframework.http.HttpStatus;
import ps.emall.orderhub.common.exception.EMallsException;
import ps.emall.orderhub.common.message.MessageKey;
import ps.emall.orderhub.common.response.ErrorCode;

import java.util.List;

public final class CartExceptions {

    private CartExceptions() {}

    public static EMallsException cartNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.CART_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("cartId", MessageKey.CART_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException cartAlreadyActive() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .message(MessageKey.CART_ALREADY_ACTIVE.getKey())
                .errorCode(List.of(new ErrorCode("customerId", MessageKey.CART_ALREADY_ACTIVE.getKey())))
                .build();
    }

    public static EMallsException cartNotActive() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.CART_NOT_ACTIVE.getKey())
                .errorCode(List.of(new ErrorCode("cartId", MessageKey.CART_NOT_ACTIVE.getKey())))
                .build();
    }

    public static EMallsException cartItemNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.CART_ITEM_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("cartItemId", MessageKey.CART_ITEM_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException cartItemNotInCart() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.CART_ITEM_NOT_IN_CART.getKey())
                .errorCode(List.of(new ErrorCode("cartItemId", MessageKey.CART_ITEM_NOT_IN_CART.getKey())))
                .build();
    }

    public static EMallsException duplicateVariantInCart() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .message(MessageKey.CART_ITEM_DUPLICATE_VARIANT.getKey())
                .errorCode(List.of(new ErrorCode("variantId", MessageKey.CART_ITEM_DUPLICATE_VARIANT.getKey())))
                .build();
    }

    public static EMallsException cartIsEmptyForCheckout() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.CART_EMPTY_CHECKOUT.getKey())
                .errorCode(List.of(new ErrorCode("items", MessageKey.CART_EMPTY_CHECKOUT.getKey())))
                .build();
    }

    public static EMallsException cartAlreadyCheckedOut() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.CART_ALREADY_CHECKED_OUT.getKey())
                .errorCode(List.of(new ErrorCode("cartId", MessageKey.CART_ALREADY_CHECKED_OUT.getKey())))
                .build();
    }

    public static EMallsException productNotFoundInCatalog() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.PRODUCT_NOT_FOUND_IN_CATALOG.getKey())
                .errorCode(List.of(new ErrorCode("productId", MessageKey.PRODUCT_NOT_FOUND_IN_CATALOG.getKey())))
                .build();
    }

    public static EMallsException productNotActive() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.PRODUCT_NOT_ACTIVE.getKey())
                .errorCode(List.of(new ErrorCode("productId", MessageKey.PRODUCT_NOT_ACTIVE.getKey())))
                .build();
    }

    public static EMallsException productDoesNotBelongToMall() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message("cart.product.does.not.belong.to.mall")
                .errorCode(List.of(new ErrorCode("mallId", "cart.product.does.not.belong.to.mall")))
                .build();
    }

    public static EMallsException variantNotFoundInCatalog() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.VARIANT_NOT_FOUND_IN_CATALOG.getKey())
                .errorCode(List.of(new ErrorCode("variantId", MessageKey.VARIANT_NOT_FOUND_IN_CATALOG.getKey())))
                .build();
    }

    public static EMallsException cityNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.CITY_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("cityId", MessageKey.CITY_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException customerNotFound() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .message(MessageKey.CUSTOMER_NOT_FOUND.getKey())
                .errorCode(List.of(new ErrorCode("customerId", MessageKey.CUSTOMER_NOT_FOUND.getKey())))
                .build();
    }

    public static EMallsException customerNotActive() {
        return EMallsException.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message(MessageKey.CUSTOMER_NOT_ACTIVE.getKey())
                .errorCode(List.of(new ErrorCode("customerId", MessageKey.CUSTOMER_NOT_ACTIVE.getKey())))
                .build();
    }
}
