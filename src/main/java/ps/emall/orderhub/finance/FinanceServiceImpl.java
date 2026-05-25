package ps.emall.orderhub.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.delivery.DeliveryRepository;
import ps.emall.orderhub.delivery.DeliveryStatus;
import ps.emall.orderhub.order.shop_order.ShopOrderRepository;
import ps.emall.orderhub.order.shop_order.ShopOrderStatus;
import ps.emall.orderhub.order.item.OrderItemRepository;
import ps.emall.orderhub.order.item.OrderItemStatus;
import ps.emall.orderhub.returnrequest.ReturnRequestRepository;
import ps.emall.orderhub.returnrequest.ReturnRequestStatus;

import java.math.BigDecimal;
import java.util.HashMap;
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
        long totalOrders = shopOrderRepository.count();
        long deliveredOrders = shopOrderRepository.countByStatus(ShopOrderStatus.DELIVERED);
        long failedDeliveries = deliveryRepository.countByStatus(DeliveryStatus.FAILED);
        long pendingReturns = returnRequestRepository.countByStatus(ReturnRequestStatus.PENDING);
        long approvedReturns = returnRequestRepository.countByStatus(ReturnRequestStatus.APPROVED);
        long rejectedReturns = returnRequestRepository.countByStatus(ReturnRequestStatus.REJECTED);
        long readyForPayout = orderItemRepository.countByStatus(OrderItemStatus.READY_FOR_PAYOUT);
        long holdingItems = orderItemRepository.countByStatus(OrderItemStatus.HOLDING);
        long returnRejected = orderItemRepository.countByStatus(OrderItemStatus.RETURN_REJECTED);

        return FinanceOverviewDto.builder()
                .totalOrders(totalOrders)
                .deliveredOrders(deliveredOrders)
                .failedDeliveries(failedDeliveries)
                .pendingReturnRequests(pendingReturns)
                .approvedReturnRequests(approvedReturns)
                .rejectedReturnRequests(rejectedReturns)
                .itemsReadyForPayout(readyForPayout)
                .itemsInHoldingWindow(holdingItems)
                .itemsReturnRejected(returnRejected)
                .note("All amounts are recorded only. No payment gateway is connected.")
                .build();
    }

    @Override
    public ShopPayoutDto getShopPayout(Long shopId) {
        long readyItems = orderItemRepository.countByShopIdAndStatus(shopId, OrderItemStatus.READY_FOR_PAYOUT);
        long returnRejected = orderItemRepository.countByShopIdAndStatus(shopId, OrderItemStatus.RETURN_REJECTED);
        long holdingItems = orderItemRepository.countByShopIdAndStatus(shopId, OrderItemStatus.HOLDING);

        BigDecimal readyAmount = orZero(orderItemRepository.sumReadyForPayoutByShopId(shopId));
        BigDecimal earnedAmount = orZero(orderItemRepository.sumEarnedAmountByShopId(shopId));
        BigDecimal deliveredTotal = orZero(shopOrderRepository.sumDeliveredTotalByShopId(shopId));

        long pendingReturns = returnRequestRepository.countByShopIdAndStatus(shopId, ReturnRequestStatus.PENDING);
        long approvedReturns = returnRequestRepository.countByShopIdAndStatus(shopId, ReturnRequestStatus.APPROVED);
        long rejectedReturns = returnRequestRepository.countByShopIdAndStatus(shopId, ReturnRequestStatus.REJECTED);

        return ShopPayoutDto.builder()
                .shopId(shopId)
                .itemsReadyForPayout(readyItems)
                .itemsReturnRejected(returnRejected)
                .itemsInHoldingWindow(holdingItems)
                .readyForPayoutAmount(readyAmount)
                .earnedAmount(earnedAmount)
                .totalDeliveredAmount(deliveredTotal)
                .pendingReturnRequests(pendingReturns)
                .approvedReturnRequests(approvedReturns)
                .rejectedReturnRequests(rejectedReturns)
                .note("earnedAmount includes READY_FOR_PAYOUT + RETURN_REJECTED items. No real transfer has occurred.")
                .build();
    }

    @Override
    public Map<String, Long> getOrderItemStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();

        for (OrderItemStatus status : OrderItemStatus.values()) {
            distribution.put(status.name(), orderItemRepository.countByStatus(status));
        }
        return distribution;
    }

    @Override
    public Map<String, Long> getShopReturnStats(Long shopId) {
        Map<String, Long> stats = new HashMap<>();

        for (ReturnRequestStatus status : ReturnRequestStatus.values()) {
            stats.put(status.name(), returnRequestRepository.countByShopIdAndStatus(shopId, status));
        }
        return stats;
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}