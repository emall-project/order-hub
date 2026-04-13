package ps.emall.orderhub.common.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageKey {

    // ==================== Cart ====================
    CART_NOT_FOUND("cart.not.found"),
    CART_ALREADY_ACTIVE("cart.already.active"),
    CART_NOT_ACTIVE("cart.not.active"),
    CART_ITEM_NOT_FOUND("cart.item.not.found"),
    CART_ITEM_NOT_IN_CART("cart.item.not.in.cart"),
    CART_ITEM_DUPLICATE_VARIANT("cart.item.duplicate.variant"),
    CART_EMPTY_CHECKOUT("cart.empty.checkout"),
    CART_ALREADY_CHECKED_OUT("cart.already.checked.out"),

    // ==================== Order ====================
    SHOP_ORDER_NOT_FOUND("shopOrder.not.found"),
    SHOP_ORDER_DOES_NOT_BELONG_TO_CUSTOMER("shopOrder.does.not.belong.to.customer"),
    SHOP_ORDER_DOES_NOT_BELONG_TO_SHOP("shopOrder.does.not.belong.to.shop"),
    SHOP_ORDER_INVALID_STATUS_TRANSITION("shopOrder.invalid.status.transition"),
    SHOP_ORDER_ALREADY_DELIVERED("shopOrder.already.delivered"),
    ORDER_ITEM_NOT_FOUND("orderItem.not.found"),

    // ==================== Delivery ====================
    DELIVERY_NOT_FOUND("delivery.not.found"),
    DELIVERY_ALREADY_DELIVERED("delivery.already.delivered"),
    DELIVERY_ALREADY_FAILED("delivery.already.failed"),
    DELIVERY_INVALID_STATUS_TRANSITION("delivery.invalid.status.transition"),
    DELIVERY_FAILURE_REASON_REQUIRED("delivery.failureReason.required"),
    DELIVERY_NOT_LINKED_TO_CART("delivery.not.linked.to.cart"),

    // ==================== Return ====================
    RETURN_REQUEST_NOT_FOUND("returnRequest.not.found"),
    RETURN_REQUEST_ALREADY_EXISTS("returnRequest.already.exists"),
    RETURN_REQUEST_ITEM_NOT_IN_HOLDING("returnRequest.item.not.in.holding"),
    RETURN_REQUEST_ALREADY_PROCESSED("returnRequest.already.processed"),
    RETURN_REJECTION_REASON_REQUIRED("returnRequest.rejectionReason.required"),
    RETURN_DOES_NOT_BELONG_TO_CUSTOMER("returnRequest.does.not.belong.to.customer"),
    RETURN_DOES_NOT_BELONG_TO_SHOP("returnRequest.does.not.belong.to.shop"),

    // ==================== External validation ====================
    PRODUCT_NOT_FOUND_IN_CATALOG("product.not.found.in.catalog"),
    PRODUCT_NOT_ACTIVE("product.not.active"),
    VARIANT_NOT_FOUND_IN_CATALOG("variant.not.found.in.catalog"),
    SHOP_NOT_FOUND("shop.not.found"),
    SHOP_NOT_ACTIVE("shop.not.active"),
    CUSTOMER_NOT_FOUND("customer.not.found"),
    CUSTOMER_NOT_ACTIVE("customer.not.active"),
    CATALOG_SERVICE_UNAVAILABLE("catalog.service.unavailable"),
    ACCOUNTS_SERVICE_UNAVAILABLE("accounts.service.unavailable"),

    // ==================== City / delivery fee ====================
    CITY_NOT_FOUND("city.not.found"),

    // ==================== HTTP ====================
    HTTP_BAD_REQUEST("http.bad.request"),
    HTTP_MESSAGE_NOT_READABLE("http.message.not.readable"),


    // ==================== Phone Errors ====================
    PHONE_NUMBER_INVALID("phone.number.invalid"),
    PHONE_PREFIX_INVALID("phone.prefix.invalid"),
    PHONE_NUMBER_REQUIRED("phone.number.required");

    private final String key;
}
