package ps.emall.orderhub.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.cart.*;
import ps.emall.orderhub.client.catalog.CatalogClient;
import ps.emall.orderhub.client.catalog.CatalogProductsResponse;
import ps.emall.orderhub.client.catalog.ProductIdsRequest;
import ps.emall.orderhub.client.catalog.ProductLightDto;
import ps.emall.orderhub.dashboard.section.*;
import ps.emall.orderhub.delivery.*;
import ps.emall.orderhub.order.*;
import ps.emall.orderhub.order.item.*;
import ps.emall.orderhub.returnrequest.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final PageRequest RECENT_10 = PageRequest.of(0, 10,
            Sort.by(Sort.Direction.DESC, "createdAt"));

    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final DeliveryRepository deliveryRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ShopOrderEnrichmentService shopOrderEnrichmentService;
    private final ReturnRequestEnrichmentService returnEnrichmentService;
    private final CatalogClient catalogClient;

    // Admin

    @Override
    public AdminDashboardDto getAdminDashboard() {
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

        return AdminDashboardDto.builder()
                .orderKpis(buildAdminOrderKpis(orderCounts))
                .deliveryKpis(buildDeliveryKpis(deliveryCounts))
                .returnKpis(buildReturnKpis(returnCounts))
                .financeKpis(buildFinanceKpis(itemCounts))
                .orderStatusBreakdown(new OrderStatusBreakdownDto(toStringCountMap(orderCounts)))
                .itemStatusBreakdown(new ItemStatusBreakdownDto(toStringCountMap(itemCounts)))
                .deliveryStatusBreakdown(new DeliveryStatusBreakdownDto(toStringCountMap(deliveryCounts)))
                .recentActivity(buildRecentActivity())
                .build();
    }

    private OrderKpiDto buildAdminOrderKpis(Map<ShopOrderStatus, Long> counts) {
        long inProgress = count(counts, ShopOrderStatus.PREPARING)
                + count(counts, ShopOrderStatus.READY_FOR_PICKUP)
                + count(counts, ShopOrderStatus.OUT_FOR_DELIVERY);

        long failed = count(counts, ShopOrderStatus.CUSTOMER_REJECTED)
                + count(counts, ShopOrderStatus.NO_RESPONSE);

        return OrderKpiDto.builder()
                .totalOrders(counts.values().stream().mapToLong(Long::longValue).sum())
                .newOrders(count(counts, ShopOrderStatus.NEW))
                .ordersInProgress(inProgress)
                .deliveredOrders(count(counts, ShopOrderStatus.DELIVERED))
                .failedOrders(failed)
                .build();
    }

    private DeliveryKpiDto buildDeliveryKpis(Map<DeliveryStatus, Long> counts) {
        long pending = count(counts, DeliveryStatus.CREATED)
                + count(counts, DeliveryStatus.SENT)
                + count(counts, DeliveryStatus.ON_THE_WAY);

        return DeliveryKpiDto.builder()
                .totalDeliveries(counts.values().stream().mapToLong(Long::longValue).sum())
                .pendingDeliveries(pending)
                .deliveredCount(count(counts, DeliveryStatus.DELIVERED))
                .failedCount(count(counts, DeliveryStatus.FAILED))
                .build();
    }

    private ReturnKpiDto buildReturnKpis(Map<ReturnRequestStatus, Long> counts) {
        return ReturnKpiDto.builder()
                .totalReturns(counts.values().stream().mapToLong(Long::longValue).sum())
                .pendingReturns(count(counts, ReturnRequestStatus.PENDING))
                .approvedReturns(count(counts, ReturnRequestStatus.APPROVED))
                .rejectedReturns(count(counts, ReturnRequestStatus.REJECTED))
                .build();
    }

    private FinanceKpiDto buildFinanceKpis(Map<OrderItemStatus, Long> counts) {
        return FinanceKpiDto.builder()
                .itemsInHolding(count(counts, OrderItemStatus.HOLDING))
                .itemsReadyForPayout(count(counts, OrderItemStatus.READY_FOR_PAYOUT))
                .itemsReturnRejected(count(counts, OrderItemStatus.RETURN_REJECTED))
                .build();
    }

    private RecentActivityDto buildRecentActivity() {
        List<ShopOrderDto> recentOrders = shopOrderRepository
                .findAll(RECENT_10).stream()
                .map(ShopOrderMapper::toDto)
                .collect(Collectors.toList());

        List<ReturnRequestDto> recentReturns = returnRequestRepository
                .findAll(RECENT_10).stream()
                .map(ReturnRequestMapper::toDto)
                .collect(Collectors.toList());

        List<DeliveryDto> recentDeliveries = deliveryRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)).stream()
                .map(DeliveryMapper::toDto)
                .collect(Collectors.toList());

        return RecentActivityDto.builder()
                .recentOrders(recentOrders)
                .recentReturns(recentReturns)
                .recentDeliveries(recentDeliveries)
                .build();
    }

    // Shop owner

    @Override
    public ShopDashboardDto getShopDashboard(Long shopId) {
        Map<ShopOrderStatus, Long> orderCounts = toEnumCountMap(
                ShopOrderStatus.class,
                shopOrderRepository.countByShopIdGroupedByStatus(shopId)
        );
        Map<ReturnRequestStatus, Long> returnCounts = toEnumCountMap(
                ReturnRequestStatus.class,
                returnRequestRepository.countByShopIdGroupedByStatus(shopId)
        );
        Map<OrderItemStatus, Long> itemCounts = toEnumCountMap(
                OrderItemStatus.class,
                orderItemRepository.countByShopIdGroupedByStatus(shopId)
        );

        return ShopDashboardDto.builder()
                .shopId(shopId)
                .orderKpis(buildShopOrderKpis(orderCounts))
                .returnKpis(buildShopReturnKpis(returnCounts))
                .financeKpis(buildShopFinanceKpis(shopId, itemCounts))
                .orderStatusBreakdown(new OrderStatusBreakdownDto(toStringCountMap(orderCounts)))
                .recentOrders(buildRecentShopOrders(shopId))
                .pendingReturns(buildPendingReturns(shopId))
                .build();
    }

    private ShopOrderKpiDto buildShopOrderKpis(Map<ShopOrderStatus, Long> counts) {
        return ShopOrderKpiDto.builder()
                .totalOrders(count(counts, ShopOrderStatus.NEW)
                        + count(counts, ShopOrderStatus.PREPARING)
                        + count(counts, ShopOrderStatus.READY_FOR_PICKUP)
                        + count(counts, ShopOrderStatus.OUT_FOR_DELIVERY)
                        + count(counts, ShopOrderStatus.DELIVERED))
                .newOrders(count(counts, ShopOrderStatus.NEW))
                .preparingOrders(count(counts, ShopOrderStatus.PREPARING))
                .readyForPickup(count(counts, ShopOrderStatus.READY_FOR_PICKUP))
                .outForDelivery(count(counts, ShopOrderStatus.OUT_FOR_DELIVERY))
                .deliveredOrders(count(counts, ShopOrderStatus.DELIVERED))
                .build();
    }

    private ShopReturnKpiDto buildShopReturnKpis(Map<ReturnRequestStatus, Long> counts) {
        return ShopReturnKpiDto.builder()
                .pendingReturns(count(counts, ReturnRequestStatus.PENDING))
                .approvedReturns(count(counts, ReturnRequestStatus.APPROVED))
                .rejectedReturns(count(counts, ReturnRequestStatus.REJECTED))
                .build();
    }

    private ShopFinanceKpiDto buildShopFinanceKpis(Long shopId, Map<OrderItemStatus, Long> counts) {
        return ShopFinanceKpiDto.builder()
                .itemsReadyForPayout(count(counts, OrderItemStatus.READY_FOR_PAYOUT))
                .itemsReturnRejected(count(counts, OrderItemStatus.RETURN_REJECTED))
                .itemsInHolding(count(counts, OrderItemStatus.HOLDING))
                .readyForPayoutAmount(orZero(orderItemRepository.sumReadyForPayoutByShopId(shopId)))
                .earnedAmount(orZero(orderItemRepository.sumEarnedAmountByShopId(shopId)))
                .totalDeliveredAmount(orZero(shopOrderRepository.sumDeliveredTotalByShopId(shopId)))
                .build();
    }

    private RecentShopOrdersDto buildRecentShopOrders(Long shopId) {
        List<ShopOrderDto> orders = shopOrderRepository
                .findByShopIdOrderByCreatedAtDesc(shopId, RECENT_10).stream()
                .map(ShopOrderMapper::toDto)
                .map(shopOrderEnrichmentService::enrich)
                .collect(Collectors.toList());
        return new RecentShopOrdersDto(orders);
    }

    private PendingReturnsDto buildPendingReturns(Long shopId) {
        List<ReturnRequestDto> returns = returnRequestRepository
                .findByShopIdAndStatus(shopId, ReturnRequestStatus.PENDING).stream()
                .map(ReturnRequestMapper::toDto)
                .map(returnEnrichmentService::enrich)
                .collect(Collectors.toList());
        return new PendingReturnsDto(returns);
    }

    // Customer

    @Override
    public CustomerDashboardDto getCustomerDashboard(Long customerId) {
        return CustomerDashboardDto.builder()
                .customerId(customerId)
                .orderKpis(buildCustomerOrderKpis(customerId))
                .activeCarts(buildActiveCarts(customerId))
                .recentOrders(buildRecentCustomerOrders(customerId))
                .activeReturns(buildActiveReturns(customerId))
                .build();
    }

    private CustomerOrderKpiDto buildCustomerOrderKpis(Long customerId) {
        List<ShopOrder> all = shopOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        long active = all.stream().filter(o ->
                o.getStatus() == ShopOrderStatus.NEW ||
                        o.getStatus() == ShopOrderStatus.PREPARING ||
                        o.getStatus() == ShopOrderStatus.READY_FOR_PICKUP ||
                        o.getStatus() == ShopOrderStatus.OUT_FOR_DELIVERY
        ).count();
        long delivered = all.stream()
                .filter(o -> o.getStatus() == ShopOrderStatus.DELIVERED).count();
        long withReturns = returnRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(r -> r.getOrderItem().getShopOrder().getShopOrderId())
                .distinct().count();

        return CustomerOrderKpiDto.builder()
                .totalOrders(all.size())
                .activeOrders(active)
                .deliveredOrders(delivered)
                .returnedOrders(withReturns)
                .build();
    }

    private ActiveCartsDto buildActiveCarts(Long customerId) {
        List<CartDto> carts = cartRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .filter(c -> c.getStatus() == CartStatus.ACTIVE)
                .map(CartMapper::toDto)
                .collect(Collectors.toList());
        return new ActiveCartsDto(carts);
    }

    private RecentCustomerOrdersDto buildRecentCustomerOrders(Long customerId) {
        List<ShopOrderDto> orders = shopOrderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, RECENT_10).stream()
                .map(ShopOrderMapper::toDto)
                .map(shopOrderEnrichmentService::enrich)
                .collect(Collectors.toList());
        return new RecentCustomerOrdersDto(orders);
    }

    private ActiveReturnsDto buildActiveReturns(Long customerId) {
        List<ReturnRequestDto> returns = returnRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .filter(r -> r.getStatus() == ReturnRequestStatus.PENDING)
                .map(ReturnRequestMapper::toDto)
                .map(returnEnrichmentService::enrich)
                .collect(Collectors.toList());
        return new ActiveReturnsDto(returns);
    }

    @Override
    public List<ProductInsightDto> getMostOrderedProducts(Long shopId, Integer limit) {
        int safeLimit = normalizeLimit(limit);
        List<Long> productIds = findMostOrderedProductIds(shopId, safeLimit);
        if (productIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> quantityByProductId = getOrderedQuantityMap(shopId, productIds);

        return fetchProducts(productIds).stream()
                .map(product -> toProductInsight(product, quantityByProductId))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<ProductOrderRankDto> getPublicMostOrderedProducts(Integer limit) {
        int safeLimit = normalizeLimit(limit);
        List<Long> productIds = findMostOrderedProductIds(null, safeLimit);
        Map<Long, Long> quantityByProductId = getOrderedQuantityMap(null, productIds);

        return productIds.stream()
                .map(productId -> ProductOrderRankDto.builder()
                        .productId(productId)
                        .orderedQuantity(quantityByProductId.getOrDefault(productId, 0L))
                        .build())
                .toList();
    }

    @Override
    public List<ProductInsightDto> getDiscountedOrderedProducts(Long shopId, Integer limit) {
        int safeLimit = normalizeLimit(limit);
        List<Long> productIds = findOrderedProductIds(shopId);
        if (productIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> quantityByProductId = getOrderedQuantityMap(shopId, productIds);

        return fetchProducts(productIds).stream()
                .filter(product -> Boolean.TRUE.equals(product.getHasDiscount()))
                .map(product -> toProductInsight(product, quantityByProductId))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(ProductInsightDto::getOrderedQuantity, Comparator.reverseOrder())
                        .thenComparing(insight -> insight.getProduct().getId()))
                .limit(safeLimit)
                .toList();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }

        return Math.min(limit, 50);
    }

    private List<Long> findMostOrderedProductIds(Long shopId, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        if (shopId == null) {
            return orderItemRepository.findMostOrderedProductIds(page);
        }

        return orderItemRepository.findMostOrderedProductIdsByShopId(shopId, page);
    }

    private List<Long> findOrderedProductIds(Long shopId) {
        if (shopId == null) {
            return orderItemRepository.findOrderedProductIds();
        }

        return orderItemRepository.findOrderedProductIdsByShopId(shopId);
    }

    private List<ProductLightDto> fetchProducts(List<Long> productIds) {
        CatalogProductsResponse response = catalogClient.getProductsByIds(
                ProductIdsRequest.builder()
                        .productIds(productIds)
                        .build()
        );

        if (response == null || response.getData() == null) {
            return List.of();
        }

        return response.getData();
    }

    private Map<Long, Long> getOrderedQuantityMap(Long shopId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = shopId == null
                ? orderItemRepository.sumQuantityByProductIds(productIds)
                : orderItemRepository.sumQuantityByShopIdAndProductIds(shopId, productIds);

        return rows.stream()
                .filter(row -> row.length >= 2 && row[0] != null && row[1] != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue(),
                        (left, right) -> left,
                        HashMap::new
                ));
    }

    private ProductInsightDto toProductInsight(
            ProductLightDto product,
            Map<Long, Long> quantityByProductId
    ) {
        if (product == null || product.getId() == null) {
            return null;
        }

        return ProductInsightDto.builder()
                .product(product)
                .orderedQuantity(quantityByProductId.getOrDefault(product.getId(), 0L))
                .build();
    }

    private <E extends Enum<E>> Map<E, Long> toEnumCountMap(Class<E> enumType, List<Object[]> rows) {
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

    private java.math.BigDecimal orZero(java.math.BigDecimal v) {
        return v != null ? v : java.math.BigDecimal.ZERO;
    }
}
