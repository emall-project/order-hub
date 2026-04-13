package ps.emall.orderhub.finance;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopPayoutDto {
    private Long shopId;

    // Items that completed the 3-day holding window with no return request.
    private long itemsReadyForPayout;

    /**
     * Items where customer submitted a return but the store REJECTED it.
     * Store keeps both the item and the payment — this is earned revenue.
     */
    private long itemsReturnRejected;

    // Items currently inside the 3-day holding window — outcome not yet known.
    private long itemsInHoldingWindow;

    // SUM of unitPrice × quantity for READY_FOR_PAYOUT items only.
    private BigDecimal readyForPayoutAmount;

    /**
     * SUM of unitPrice × quantity for READY_FOR_PAYOUT + RETURN_REJECTED items.
     * This is the total amount the store has definitively earned.
     */
    private BigDecimal earnedAmount;

    // SUM of ShopOrder totals with status DELIVERED (gross, before returns).
    private BigDecimal totalDeliveredAmount;

    private long pendingReturnRequests;
    private long approvedReturnRequests;
    private long rejectedReturnRequests;
    private String note;
}
