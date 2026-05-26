package ps.emall.orderhub.order;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.emall.orderhub.cart.Cart;
import ps.emall.orderhub.cart.CartExceptions;
import ps.emall.orderhub.cart.CartRepository;
import ps.emall.orderhub.cart.CartService;
import ps.emall.orderhub.cart.CartStatus;
import ps.emall.orderhub.cart.CheckoutRequest;
import ps.emall.orderhub.cart.item.CartItem;
import ps.emall.orderhub.client.accounts.AccountsClient;
import ps.emall.orderhub.client.accounts.AccountsCityResponse;
import ps.emall.orderhub.client.accounts.AccountsShopResponse;
import ps.emall.orderhub.client.accounts.CityDto;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.common.phone_number.PhoneNumberMapper;
import ps.emall.orderhub.delivery.Delivery;
import ps.emall.orderhub.delivery.DeliveryRepository;
import ps.emall.orderhub.delivery.DeliveryStatus;
import ps.emall.orderhub.order.item.OrderItem;
import ps.emall.orderhub.order.item.OrderItemRepository;
import ps.emall.orderhub.order.item.OrderItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShopOrderServiceImpl implements ShopOrderService {

    private static final int HOLDING_DAYS = 3;

    // Only allowed forward transitions that a store owner can trigger
    private static final Map<ShopOrderStatus, ShopOrderStatus> NEXT_STATUS = Map.of(
            ShopOrderStatus.NEW, ShopOrderStatus.PREPARING,
            ShopOrderStatus.PREPARING, ShopOrderStatus.READY_FOR_PICKUP,
            ShopOrderStatus.READY_FOR_PICKUP, ShopOrderStatus.OUT_FOR_DELIVERY,
            ShopOrderStatus.OUT_FOR_DELIVERY,ShopOrderStatus.DELIVERED
    );

    // Maps order status to the matching item status
    private static final Map<ShopOrderStatus, OrderItemStatus> ITEM_STATUS_MAP = Map.of(
            ShopOrderStatus.NEW, OrderItemStatus.CREATED,
            ShopOrderStatus.PREPARING, OrderItemStatus.PREPARING,
            ShopOrderStatus.READY_FOR_PICKUP, OrderItemStatus.READY_FOR_PICKUP,
            ShopOrderStatus.OUT_FOR_DELIVERY, OrderItemStatus.OUT_FOR_DELIVERY,
            ShopOrderStatus.DELIVERED, OrderItemStatus.DELIVERED
    );

    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final DeliveryRepository deliveryRepository;
    private final AccountsClient accountsClient;
    private final ShopOrderEnrichmentService enrichmentService;

    // Checkout

    @Override
    public List<ShopOrderDto> checkout(Long customerId, Long mallId, CheckoutRequest request) {

        // Fetch active cart for this specific mall
        Cart cart = cartRepository
                .findByCustomerIdAndMallIdAndStatus(customerId, mallId, CartStatus.ACTIVE)
                .orElseThrow(CartExceptions::cartNotFound);

        // Cart must have items
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw CartExceptions.cartIsEmptyForCheckout();
        }

        // Guard — should never happen but prevents double-checkout
        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            throw CartExceptions.cartAlreadyCheckedOut();
        }

        // Apply delivery details from the checkout popup to the cart.
        // City lookup also fetches the live delivery fee from the Accounts service.
        CityDto city = fetchCityOrThrow(request.getCityId());
        cart.setCityId(city.getCityId());
        cart.setDeliveryFee(city.getBaseFee());
        cart.setDeliveryName(request.getDeliveryName());
        cart.setDeliveryPhone(PhoneNumberMapper.toPhoneString(request.getDeliveryPhone()));
        cart.setDeliveryLocation(request.getDeliveryLocation());
        cart.setDeliveryNote(request.getDeliveryNote());
        cartRepository.save(cart);

        // Group cart items by storeId — one ShopOrder per store
        Map<Long, List<CartItem>> itemsByStore = cart.getItems().stream()
                .collect(Collectors.groupingBy(CartItem::getStoreId));

        List<ShopOrder> createdOrders = new ArrayList<>();

        for (Map.Entry<Long, List<CartItem>> entry : itemsByStore.entrySet()) {
            Long storeId = entry.getKey();
            List<CartItem> storeItems = entry.getValue();

            // Validate shop is still active at checkout time
            validateShopActive(storeId);

            // Calculate store total from snapshotted effective prices
            BigDecimal storeTotal = storeItems.stream()
                    .map(item -> item.getEffectiveUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ShopOrder order = ShopOrder.builder()
                    .cartId(cart.getCartId())
                    .shopId(storeId)
                    .mallId(cart.getMallId())
                    .customerId(customerId)
                    .total(storeTotal)
                    .status(ShopOrderStatus.NEW)
                    .build();

            // Build order items — prices are snapshotted here and never change
            List<OrderItem> orderItems = storeItems.stream()
                    .map(cartItem -> OrderItem.builder()
                            .shopOrder(order)
                            .shopId(storeId)
                            .productId(cartItem.getProductId())
                            .productName(cartItem.getProductName())
                            .variantId(cartItem.getVariantId())
                            .variantName(cartItem.getVariantName())
                            .unitPrice(cartItem.getEffectiveUnitPrice())
                            .quantity(cartItem.getQuantity())
                            .status(OrderItemStatus.CREATED)
                            .hasReturnRequest(false)
                            .build())
                    .collect(Collectors.toList());

            order.setItems(orderItems);
            ShopOrder saved = shopOrderRepository.save(order);
            createdOrders.add(saved);
            log.info("ShopOrder created: shopOrderId={}, storeId={}, customerId={}, total={}",
                    saved.getShopOrderId(), storeId, customerId, storeTotal);
        }

        // Create one Delivery record linked to the entire cart
        Delivery delivery = Delivery.builder()
                .cartId(cart.getCartId())
                .status(DeliveryStatus.CREATED)
                .build();
        deliveryRepository.save(delivery);
        log.info("Delivery record created for cartId={}", cart.getCartId());

        // Lock the cart — frees the customer's active-cart slot immediately
        cartService.markCheckedOut(cart.getCartId());

        return createdOrders.stream()
                .map(ShopOrderMapper::toDto)
                .map(enrichmentService::enrich)
                .collect(Collectors.toList());
    }

    // Customer

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ShopOrderDto> getMyOrders(Long customerId, Pageable pageable) {
        Page<ShopOrderDto> page = shopOrderRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(ShopOrderMapper::toDto)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderDto getMyOrderById(Long shopOrderId, Long customerId) {
        ShopOrder order = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(ShopOrderExceptions::shopOrderNotFound);

        if (!order.getCustomerId().equals(customerId)) {
            throw ShopOrderExceptions.shopOrderDoesNotBelongToCustomer();
        }
        return enrichmentService.enrich(ShopOrderMapper.toDto(order));
    }

    // Store owner

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ShopOrderDto> getOrdersByShop(Long shopId,
                                                            ShopOrderSpec spec,
                                                            Pageable pageable) {

        Specification<ShopOrder> shopFilter = (root, query, cb) ->
                cb.equal(root.get("shopId"), shopId);

        Specification<ShopOrder> combined = Specification.where(shopFilter).and(spec);

        Page<ShopOrderDto> page = shopOrderRepository.findAll(combined, pageable)
                .map(ShopOrderMapper::toDto)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ShopOrderDto> getOrdersByShopAndStatus(Long shopId,
                                                                     ShopOrderStatus status,
                                                                     Pageable pageable) {
        Page<ShopOrderDto> page = shopOrderRepository
                .findByShopIdAndStatus(shopId, status, pageable)
                .map(ShopOrderMapper::toDto)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderDto getOrderByIdForShop(Long shopOrderId, Long shopId) {
        ShopOrder order = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(ShopOrderExceptions::shopOrderNotFound);

        if (!order.getShopId().equals(shopId)) {
            throw ShopOrderExceptions.shopOrderDoesNotBelongToShop();
        }

        return enrichmentService.enrich(ShopOrderMapper.toDto(order));
    }

    @Override
    @Transactional
    public ShopOrderDto advanceStatus(Long shopOrderId, Long shopId) {
        ShopOrder order = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(ShopOrderExceptions::shopOrderNotFound);

        if (!order.getShopId().equals(shopId)) {
            throw ShopOrderExceptions.shopOrderDoesNotBelongToShop();
        }

        ShopOrderStatus current = order.getStatus();
        ShopOrderStatus next = NEXT_STATUS.get(current);

        if (next == null) {
            throw ShopOrderExceptions.invalidStatusTransition(current, null);
        }

        // Store owner can only advance up to READY_FOR_PICKUP.
        // OUT_FOR_DELIVERY and DELIVERED are set by the admin via the delivery endpoint.
        if (next == ShopOrderStatus.OUT_FOR_DELIVERY || next == ShopOrderStatus.DELIVERED) {
            throw ShopOrderExceptions.invalidStatusTransition(current, next);
        }

        order.setStatus(next);

        OrderItemStatus itemStatus = ITEM_STATUS_MAP.get(next);
        if (itemStatus != null) {
            orderItemRepository.updateStatusByShopOrderId(shopOrderId, itemStatus);
        }

        ShopOrder saved = shopOrderRepository.save(order);
        log.info("ShopOrder status advanced: shopOrderId={}, {} -> {}", shopOrderId, current, next);
        return enrichmentService.enrich(ShopOrderMapper.toDto(saved));
    }

    // Admin

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ShopOrderDto> getAllOrders(ShopOrderSpec spec, Pageable pageable) {
        Page<ShopOrderDto> page = shopOrderRepository.findAll(spec, pageable)
                .map(ShopOrderMapper::toDto)
                .map(enrichmentService::enrich);

        return PaginatedResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopOrderDto getOrderByIdForAdmin(Long shopOrderId) {
        ShopOrder order = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(ShopOrderExceptions::shopOrderNotFound);

        return enrichmentService.enrich(ShopOrderMapper.toDto(order));
    }

    @Override
    @Transactional
    public ShopOrderDto overrideStatus(Long shopOrderId, ShopOrderStatus targetStatus, String reason) {
        ShopOrder order = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(ShopOrderExceptions::shopOrderNotFound);

        ShopOrderStatus previous = order.getStatus();
        order.setStatus(targetStatus);

        OrderItemStatus itemStatus = ITEM_STATUS_MAP.get(targetStatus);
        if (itemStatus != null) {
            orderItemRepository.updateStatusByShopOrderId(shopOrderId, itemStatus);
        }

        if (targetStatus == ShopOrderStatus.DELIVERED) {
            openHoldingWindowForOrder(order);
        }

        ShopOrder saved = shopOrderRepository.save(order);
        log.warn("ShopOrder status OVERRIDDEN by admin: shopOrderId={}, {} -> {}, reason={}",
                shopOrderId, previous, targetStatus, reason);
        return enrichmentService.enrich(ShopOrderMapper.toDto(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ShopOrderStatus, Long> getOrderCountsByStatus() {
        Map<ShopOrderStatus, Long> counts = new EnumMap<>(ShopOrderStatus.class);

        for (ShopOrderStatus status : ShopOrderStatus.values()) {
            counts.put(status, shopOrderRepository.countByStatus(status));
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ShopOrderStatus, Long> getOrderCountsByStatusForShop(Long shopId) {
        Map<ShopOrderStatus, Long> counts = new EnumMap<>(ShopOrderStatus.class);

        for (ShopOrderStatus status : ShopOrderStatus.values()) {
            counts.put(status, shopOrderRepository.countByShopIdAndStatus(shopId, status));
        }
        return counts;
    }

    // Called by DeliveryServiceImpl

    @Transactional
    public void onDeliveryCompleted(Long cartId) {
        List<ShopOrder> orders = shopOrderRepository.findByCartId(cartId);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(HOLDING_DAYS);

        for (ShopOrder order : orders) {
            order.setStatus(ShopOrderStatus.DELIVERED);
            shopOrderRepository.save(order);

            for (OrderItem item : order.getItems()) {
                item.setStatus(OrderItemStatus.HOLDING);
                item.setHoldingExpiresAt(expiresAt);
                orderItemRepository.save(item);
            }
            log.info("ShopOrder DELIVERED + HOLDING opened: shopOrderId={}, expiresAt={}",
                    order.getShopOrderId(), expiresAt);
        }
    }

    @Transactional
    public void onDeliveryFailed(Long cartId, boolean customerRejected) {
        List<ShopOrder> orders = shopOrderRepository.findByCartId(cartId);
        ShopOrderStatus failStatus = customerRejected
                ? ShopOrderStatus.CUSTOMER_REJECTED
                : ShopOrderStatus.NO_RESPONSE;

        for (ShopOrder order : orders) {
            order.setStatus(failStatus);
            for (OrderItem item : order.getItems()) {
                item.setStatus(OrderItemStatus.DELIVERY_FAILED);
            }
            shopOrderRepository.save(order);
            log.info("ShopOrder marked as {}: shopOrderId={}", failStatus, order.getShopOrderId());
        }
    }

    // Helpers

    private void openHoldingWindowForOrder(ShopOrder order) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(HOLDING_DAYS);
        for (OrderItem item : order.getItems()) {
            item.setStatus(OrderItemStatus.HOLDING);
            item.setHoldingExpiresAt(expiresAt);
            orderItemRepository.save(item);
        }
    }

    private void validateShopActive(Long shopId) {
        try {
            AccountsShopResponse shop = accountsClient.getShopById(shopId);
            if(shop == null || shop.getData() == null) {
                throw ShopOrderExceptions.shopNotFound();
            }
            if (Boolean.FALSE.equals(shop.getData().getIsActive())) {
                throw ShopOrderExceptions.shopNotActive();
            }
        } catch (FeignException.NotFound e) {
            throw ShopOrderExceptions.shopNotFound();
        } catch (FeignException e) {
            log.warn("Accounts service unreachable for shopId={}, status={}. Allowing checkout.",
                    shopId, e.status());
        }
    }

    private CityDto fetchCityOrThrow(Long cityId) {
        try {
            System.out.println("HIII");
            AccountsCityResponse response = accountsClient.getCityById(cityId);
            if (response == null || response.getData() == null) {
                System.out.println("HELLO");
                throw CartExceptions.cityNotFound();
            }
            CityDto city = response.getData();
            if (Boolean.FALSE.equals(city.getIsActive())) {
                throw CartExceptions.cityNotFound();
            }
            return city;
        } catch (FeignException.NotFound e) {
            throw CartExceptions.cityNotFound();
        } catch (FeignException e) {
            log.error("Accounts service unreachable for cityId={}, status={}", cityId, e.status());
            throw CartExceptions.cityNotFound();
        }
    }
}
