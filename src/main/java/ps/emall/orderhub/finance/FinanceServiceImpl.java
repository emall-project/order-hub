package ps.emall.orderhub.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.delivery.DeliveryRepository;
import ps.emall.orderhub.delivery.DeliveryStatus;
import ps.emall.orderhub.order.ShopOrderRepository;
import ps.emall.orderhub.order.ShopOrderStatus;
import ps.emall.orderhub.order.item.OrderItemRepository;
import ps.emall.orderhub.order.item.OrderItemStatus;
import ps.emall.orderhub.returnrequest.ReturnRequestRepository;
import ps.emall.orderhub.returnrequest.ReturnRequestStatus;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceServiceImpl implements FinanceService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final DeliveryRepository deliveryRepository;

    @Override
    public FinanceOverviewDto getFinanceOverview() {
        Map<ShopOrderStatus, Long> orderCounts = toEnumCountMap(
                ShopOrderStatus.class,
                shopOrderRepository.countAllGroupedByStatus()
        );
        Map<DeliveryStatus, Long> deliveryCounts = toEnumCountMap(
                DeliveryStatus.class,
                deliveryRepository.countAllGroupedByStatus()
        );
        Map<ReturnRequestStatus, Long> returnCounts = toEnumCountMap(
                ReturnRequestStatus.class,
                returnRequestRepository.countAllGroupedByStatus()
        );
        Map<OrderItemStatus, Long> itemCounts = toEnumCountMap(
                OrderItemStatus.class,
                orderItemRepository.countAllGroupedByStatus()
        );

        return FinanceOverviewDto.builder()
                .totalOrders(orderCounts.values().stream().mapToLong(Long::longValue).sum())
                .deliveredOrders(count(orderCounts, ShopOrderStatus.DELIVERED))
                .failedDeliveries(count(deliveryCounts, DeliveryStatus.FAILED))
                .pendingReturnRequests(count(returnCounts, ReturnRequestStatus.PENDING))
                .approvedReturnRequests(count(returnCounts, ReturnRequestStatus.APPROVED))
                .rejectedReturnRequests(count(returnCounts, ReturnRequestStatus.REJECTED))
                .itemsReadyForPayout(count(itemCounts, OrderItemStatus.READY_FOR_PAYOUT))
                .itemsInHoldingWindow(count(itemCounts, OrderItemStatus.HOLDING))
                .itemsReturnRejected(count(itemCounts, OrderItemStatus.RETURN_REJECTED))
                .note("All amounts are recorded only. No payment gateway is connected.")
                .build();
    }

    @Override
    public ShopPayoutDto getShopPayout(Long shopId) {
        Map<OrderItemStatus, Long> itemCounts = toEnumCountMap(
                OrderItemStatus.class,
                orderItemRepository.countByShopIdGroupedByStatus(shopId)
        );
        Map<ReturnRequestStatus, Long> returnCounts = toEnumCountMap(
                ReturnRequestStatus.class,
                returnRequestRepository.countByShopIdGroupedByStatus(shopId)
        );

        BigDecimal readyAmount = orZero(orderItemRepository.sumReadyForPayoutByShopId(shopId));
        BigDecimal earnedAmount = orZero(orderItemRepository.sumEarnedAmountByShopId(shopId));
        BigDecimal deliveredTotal = orZero(shopOrderRepository.sumDeliveredTotalByShopId(shopId));

        return ShopPayoutDto.builder()
                .shopId(shopId)
                .itemsReadyForPayout(count(itemCounts, OrderItemStatus.READY_FOR_PAYOUT))
                .itemsReturnRejected(count(itemCounts, OrderItemStatus.RETURN_REJECTED))
                .itemsInHoldingWindow(count(itemCounts, OrderItemStatus.HOLDING))
                .readyForPayoutAmount(readyAmount)
                .earnedAmount(earnedAmount)
                .totalDeliveredAmount(deliveredTotal)
                .pendingReturnRequests(count(returnCounts, ReturnRequestStatus.PENDING))
                .approvedReturnRequests(count(returnCounts, ReturnRequestStatus.APPROVED))
                .rejectedReturnRequests(count(returnCounts, ReturnRequestStatus.REJECTED))
                .note("earnedAmount includes READY_FOR_PAYOUT + RETURN_REJECTED items. No real transfer has occurred.")
                .build();
    }

    @Override
    public Map<String, Long> getOrderItemStatusDistribution() {
        return toStringCountMap(toEnumCountMap(
                OrderItemStatus.class,
                orderItemRepository.countAllGroupedByStatus()
        ));
    }

    @Override
    public Map<String, Long> getShopReturnStats(Long shopId) {
        return toStringCountMap(toEnumCountMap(
                ReturnRequestStatus.class,
                returnRequestRepository.countByShopIdGroupedByStatus(shopId)
        ));
    }

    private <E extends Enum<E>> Map<E, Long> toEnumCountMap(Class<E> enumType, java.util.List<Object[]> rows) {
        Map<E, Long> counts = new EnumMap<>(enumType);

        for (E value : enumType.getEnumConstants()) {
            counts.put(value, 0L);
        }

        if (rows == null) {
            return counts;
        }

        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }

            counts.put(enumType.cast(row[0]), ((Number) row[1]).longValue());
        }

        return counts;
    }

    private <E extends Enum<E>> Map<String, Long> toStringCountMap(Map<E, Long> counts) {
        Map<String, Long> result = new LinkedHashMap<>();
        counts.forEach((key, value) -> result.put(key.name(), value));
        return result;
    }

    private <E extends Enum<E>> long count(Map<E, Long> counts, E key) {
        return counts.getOrDefault(key, 0L);
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
