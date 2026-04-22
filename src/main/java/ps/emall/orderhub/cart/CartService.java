package ps.emall.orderhub.cart;

import org.springframework.data.domain.Pageable;
import ps.emall.orderhub.cart.item.AddToCartRequest;
import ps.emall.orderhub.cart.item.UpdateCartItemRequest;
import ps.emall.orderhub.common.page.PaginatedResponse;

import java.util.List;

public interface CartService {

    /**
     * The core "add to cart" operation.
     *
     * If the customer has no ACTIVE cart for the given mall, one is created
     * automatically. If they already have one, items are added to it.
     * A customer can have one ACTIVE cart per mall at a time, but can have
     * active carts in different malls simultaneously.
     */
    CartDto addItem(Long customerId, AddToCartRequest request);

    // Read

    CartDto getActiveCartForMall(Long customerId, Long mallId);

    CartDto getCartById(Long cartId, Long customerId);

    PaginatedResponse<CartDto> getMyCartHistory(Long customerId, Pageable pageable);

    List<CartDto> getAllActiveCartsForCustomer(Long customerId);

    // Modify active cart

    CartDto updateDeliveryDetails(Long cartId, Long customerId, CartDto details);

    CartDto updateItemQuantity(Long customerId, Long cartItemId, UpdateCartItemRequest request);

    CartDto removeItem(Long customerId, Long cartItemId);

    CartDto clearCart(Long customerId, Long mallId);

    CartDto cancelCart(Long customerId, Long mallId);

    // System

    // Called by ShopOrderServiceImpl after checkout -> locks the cart
    void markCheckedOut(Long cartId);

    // Admin
    PaginatedResponse<CartDto> getAllCarts(CartSpec spec, Pageable pageable);
}
