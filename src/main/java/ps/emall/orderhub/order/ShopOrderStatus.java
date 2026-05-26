package ps.emall.orderhub.order;

public enum ShopOrderStatus {
    NEW,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CUSTOMER_REJECTED,   // delivery failed — customer refused
    NO_RESPONSE          // delivery failed — no one answered
}
