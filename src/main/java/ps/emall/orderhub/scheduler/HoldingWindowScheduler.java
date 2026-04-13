package ps.emall.orderhub.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.order.item.OrderItem;
import ps.emall.orderhub.order.item.OrderItemRepository;
import ps.emall.orderhub.order.item.OrderItemStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that runs every 10 minutes.
 *
 * Finds all OrderItems in HOLDING status whose 3-day window has expired
 * without a return request being submitted, and promotes them to READY_FOR_PAYOUT.
 *
 * READY_FOR_PAYOUT means: the store is entitled to the payment for this item.
 * No actual money movement happens in this MVP — the status is a record
 * for the finance dashboard to display.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HoldingWindowScheduler {

    private final OrderItemRepository orderItemRepository;

    @Scheduled(fixedDelayString = "${order.holding.scheduler.interval-ms:600000}")
    @Transactional
    public void promoteExpiredHoldingItems() {
        LocalDateTime now = LocalDateTime.now();

        List<OrderItem> expiredItems = orderItemRepository.findExpiredHoldingItems(now);

        if (expiredItems.isEmpty()) {
            log.debug("HoldingWindowScheduler: no expired holding items found");
            return;
        }

        log.info("HoldingWindowScheduler: promoting {} expired holding item(s) to READY_FOR_PAYOUT",
                expiredItems.size());

        for (OrderItem item : expiredItems) {
            item.setStatus(OrderItemStatus.READY_FOR_PAYOUT);
            orderItemRepository.save(item);
            log.info("Item promoted to READY_FOR_PAYOUT: orderItemId={}, shopId={}, holdingExpiredAt={}",
                    item.getOrderItemId(), item.getShopId(), item.getHoldingExpiresAt());
        }
    }
}
