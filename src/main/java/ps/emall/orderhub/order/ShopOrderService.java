package ps.emall.orderhub.order;

import org.springframework.data.domain.Pageable;
import ps.emall.orderhub.cart.CheckoutRequest;
import ps.emall.orderhub.common.page.PaginatedResponse;

import java.util.List;
import java.util.Map;

public interface ShopOrderService {

    // Checkout
    List<ShopOrderDto> checkout(Long customerId, Long mallId, CheckoutRequest checkoutRequest);

    // Customer
    PaginatedResponse<ShopOrderDto> getMyOrders(Long customerId, Pageable pageable);

    ShopOrderDto getMyOrderById(Long shopOrderId, Long customerId);

    // Store owner
    PaginatedResponse<ShopOrderDto> getOrdersByShop(Long shopId, ShopOrderSpec spec,
                                                     Pageable pageable);

    PaginatedResponse<ShopOrderDto> getOrdersByShopAndStatus(Long shopId, ShopOrderStatus status,
                                                              Pageable pageable);

    ShopOrderDto getOrderByIdForShop(Long shopOrderId, Long shopId);

    ShopOrderDto advanceStatus(Long shopOrderId, Long shopId);

    Map<ShopOrderStatus, Long> getOrderCountsByStatusForShop(Long shopId);

    // Admin
    PaginatedResponse<ShopOrderDto> getAllOrders(ShopOrderSpec spec, Pageable pageable);

    ShopOrderDto getOrderByIdForAdmin(Long shopOrderId);

    ShopOrderDto overrideStatus(Long shopOrderId, ShopOrderStatus targetStatus, String reason);

    Map<ShopOrderStatus, Long> getOrderCountsByStatus();
}
