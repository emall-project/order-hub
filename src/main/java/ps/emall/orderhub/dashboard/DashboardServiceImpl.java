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
        return AdminDashboardDto.builder()
                .orderKpis(buildAdminOrderKpis())
                .deliveryKpis(buildDeliveryKpis())
                .returnKpis(buildReturnKpis())
                .financeKpis(buildFinanceKpis())
                .orderStatusBreakdown(buildOrderStatusBreakdown())
                .itemStatusBreakdown(buildItemStatusBreakdown())
                .deliveryStatusBreakdown(buildDeliveryStatusBreakdown())
                .recentActivity(buildRecentActivity())
                .build();
    }

    private OrderKpiDto buildAdminOrderKpis() {
        long inProgress = shopOrderRepository.countByStatus(ShopOrderStatus.PREPARING)
                + shopOrderRepository.countByStatus(ShopOrderStatus.READY_FOR_PICKUP)
                + shopOrderRepository.countByStatus(ShopOrderStatus.OUT_FOR_DELIVERY);

        long failed = shopOrderRepository.countByStatus(ShopOrderStatus.CUSTOMER_REJECTED)
                + shopOrderRepository.countByStatus(ShopOrderStatus.NO_RESPONSE);

        return OrderKpiDto.builder()
                .totalOrders(shopOrderRepository.count())
                .newOrders(shopOrderRepository.countByStatus(ShopOrderStatus.NEW))
                .ordersInProgress(inProgress)
                .deliveredOrders(shopOrderRepository.countByStatus(ShopOrderStatus.DELIVERED))
                .failedOrders(failed)
                .build();
    }

    private DeliveryKpiDto buildDeliveryKpis() {
        long pending = deliveryRepository.countByStatus(DeliveryStatus.CREATED)
                + deliveryRepository.countByStatus(DeliveryStatus.SENT)
                + deliveryRepository.countByStatus(DeliveryStatus.ON_THE_WAY);

        return DeliveryKpiDto.builder()
                .totalDeliveries(deliveryRepository.count())
                .pendingDeliveries(pending)
                .deliveredCount(deliveryRepository.countByStatus(DeliveryStatus.DELIVERED))
                .failedCount(deliveryRepository.countByStatus(DeliveryStatus.FAILED))
                .build();
    }

    private ReturnKpiDto buildReturnKpis() {
        return ReturnKpiDto.builder()
                .totalReturns(returnRequestRepository.count())
                .pendingReturns(returnRequestRepository.countByStatus(ReturnRequestStatus.PENDING))
                .approvedReturns(returnRequestRepository.countByStatus(ReturnRequestStatus.APPROVED))
                .rejectedReturns(returnRequestRepository.countByStatus(ReturnRequestStatus.REJECTED))
                .build();
    }

    private FinanceKpiDto buildFinanceKpis() {
        return FinanceKpiDto.builder()
                .itemsInHolding(orderItemRepository.countByStatus(OrderItemStatus.HOLDING))
                .itemsReadyForPayout(orderItemRepository.countByStatus(OrderItemStatus.READY_FOR_PAYOUT))
                .itemsReturnRejected(orderItemRepository.countByStatus(OrderItemStatus.RETURN_REJECTED))
                .build();
    }

    private OrderStatusBreakdownDto buildOrderStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (ShopOrderStatus s : ShopOrderStatus.values()) {
            breakdown.put(s.name(), shopOrderRepository.countByStatus(s));
        }
        return new OrderStatusBreakdownDto(breakdown);
    }

    private ItemStatusBreakdownDto buildItemStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (OrderItemStatus s : OrderItemStatus.values()) {
            breakdown.put(s.name(), orderItemRepository.countByStatus(s));
        }
        return new ItemStatusBreakdownDto(breakdown);
    }

    private DeliveryStatusBreakdownDto buildDeliveryStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (DeliveryStatus s : DeliveryStatus.values()) {
            breakdown.put(s.name(), deliveryRepository.countByStatus(s));
        }
        return new DeliveryStatusBreakdownDto(breakdown);
    }

    private RecentActivityDto buildRecentActivity() {
        List<ShopOrderDto> recentOrders = shopOrderRepository
                .findAll(RECENT_10).stream()
                .map(ShopOrderMapper::toDto)
                .map(shopOrderEnrichmentService::enrich)
                .collect(Collectors.toList());

        List<ReturnRequestDto> recentReturns = returnRequestRepository
                .findAll(RECENT_10).stream()
                .map(ReturnRequestMapper::toDto)
                .map(returnEnrichmentService::enrich)
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
        return ShopDashboardDto.builder()
                .shopId(shopId)
                .orderKpis(buildShopOrderKpis(shopId))
                .returnKpis(buildShopReturnKpis(shopId))
                .financeKpis(buildShopFinanceKpis(shopId))
                .orderStatusBreakdown(buildShopOrderStatusBreakdown(shopId))
                .recentOrders(buildRecentShopOrders(shopId))
                .pendingReturns(buildPendingReturns(shopId))
                .build();
    }

    private ShopOrderKpiDto buildShopOrderKpis(Long shopId) {
        return ShopOrderKpiDto.builder()
                .totalOrders(shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.NEW)
                        + shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.PREPARING)
                        + shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.READY_FOR_PICKUP)
                        + shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.OUT_FOR_DELIVERY)
                        + shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.DELIVERED))
                .newOrders(shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.NEW))
                .preparingOrders(shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.PREPARING))
                .readyForPickup(shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.READY_FOR_PICKUP))
                .outForDelivery(shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.OUT_FOR_DELIVERY))
                .deliveredOrders(shopOrderRepository.countByShopIdAndStatus(shopId, ShopOrderStatus.DELIVERED))
                .build();
    }

    private ShopReturnKpiDto buildShopReturnKpis(Long shopId) {
        return ShopReturnKpiDto.builder()
                .pendingReturns(returnRequestRepository.countByShopIdAndStatus(shopId, ReturnRequestStatus.PENDING))
                .approvedReturns(returnRequestRepository.countByShopIdAndStatus(shopId, ReturnRequestStatus.APPROVED))
                .rejectedReturns(returnRequestRepository.countByShopIdAndStatus(shopId, ReturnRequestStatus.REJECTED))
                .build();
    }

    private ShopFinanceKpiDto buildShopFinanceKpis(Long shopId) {
        return ShopFinanceKpiDto.builder()
                .itemsReadyForPayout(orderItemRepository.countByShopIdAndStatus(shopId, OrderItemStatus.READY_FOR_PAYOUT))
                .itemsReturnRejected(orderItemRepository.countByShopIdAndStatus(shopId, OrderItemStatus.RETURN_REJECTED))
                .itemsInHolding(orderItemRepository.countByShopIdAndStatus(shopId, OrderItemStatus.HOLDING))
                .readyForPayoutAmount(orZero(orderItemRepository.sumReadyForPayoutByShopId(shopId)))
                .earnedAmount(orZero(orderItemRepository.sumEarnedAmountByShopId(shopId)))
                .totalDeliveredAmount(orZero(shopOrderRepository.sumDeliveredTotalByShopId(shopId)))
                .build();
    }

    private OrderStatusBreakdownDto buildShopOrderStatusBreakdown(Long shopId) {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        for (ShopOrderStatus s : ShopOrderStatus.values()) {
            breakdown.put(s.name(), shopOrderRepository.countByShopIdAndStatus(shopId, s));
        }
        return new OrderStatusBreakdownDto(breakdown);
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

    private java.math.BigDecimal orZero(java.math.BigDecimal v) {
        return v != null ? v : java.math.BigDecimal.ZERO;
    }
}
