package ps.emall.orderhub.order.item;

public enum OrderItemStatus {
    CREATED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    HOLDING,              // 3-day return window is open
    READY_FOR_PAYOUT,     // window passed, no return submitted
    RETURN_REQUESTED,     // customer submitted return
    RETURN_APPROVED,      // store approved the return
    RETURN_REJECTED,      // store rejected the return
    DELIVERY_FAILED
}
