package ps.emall.orderhub.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ps.emall.orderhub.cart.item.AddToCartRequest;
import ps.emall.orderhub.cart.item.UpdateCartItemRequest;
import ps.emall.orderhub.common.page.PaginatedResponse;
import ps.emall.orderhub.common.response.EMallsResponseEntity;
import ps.emall.orderhub.order.shop_order.ShopOrderDto;
import ps.emall.orderhub.order.shop_order.ShopOrderService;
import ps.emall.orderhub.security.SecurityContextUtilBean;

import java.util.List;

@Validated
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ShopOrderService shopOrderService;
    private final SecurityContextUtilBean auth;

    // Add to cart

    @PostMapping("/items")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<CartDto> addItem(@RequestBody @Valid AddToCartRequest request) {
        Long customerId = auth.getCurrentUserId();
        CartDto updated = cartService.addItem(customerId, request);
        return EMallsResponseEntity.ok(updated);
    }

    // View cart

    @GetMapping("/me/mall/{mallId}")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<CartDto> getMyCartForMall(
            @PathVariable @Positive Long mallId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(cartService.getCartForMall(customerId, mallId));
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("@auth.isCustomer() or @auth.isAdmin()")
    public EMallsResponseEntity<CartDto> getCartById(@PathVariable @Positive Long cartId) {
        Long customerId = auth.isAdmin() ? null : auth.getCurrentUserId();
        return EMallsResponseEntity.ok(cartService.getCartById(cartId, customerId));
    }


    @GetMapping("/me/active")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<List<CartDto>> getMyActiveCarts() {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(cartService.getAllActiveCartsForCustomer(customerId));
    }

    // Modify items

    @PatchMapping("/items/{cartItemId}/quantity")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<CartDto> updateItemQuantity(
            @PathVariable @Positive Long cartItemId,
            @RequestBody @Valid UpdateCartItemRequest request) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(cartService.updateItemQuantity(customerId, cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<CartDto> removeItem(@PathVariable @Positive Long cartItemId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(cartService.removeItem(customerId, cartItemId));
    }

    @DeleteMapping("/me/mall/{mallId}/items")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<CartDto> clearCart(@PathVariable @Positive Long mallId) {
        Long customerId = auth.getCurrentUserId();
        return EMallsResponseEntity.ok(cartService.clearCart(customerId, mallId));
    }

    // Checkout

    @PostMapping("/me/mall/{mallId}/checkout")
    @PreAuthorize("@auth.isCustomer()")
    public EMallsResponseEntity<List<ShopOrderDto>> checkout(
            @PathVariable @Positive Long mallId,
            @RequestBody @Valid CheckoutRequest request) {
        Long customerId = auth.getCurrentUserId();
        List<ShopOrderDto> orders = shopOrderService.checkout(customerId, mallId, request);
        return EMallsResponseEntity.created(orders);
    }

    // Admin

    @GetMapping("/admin")
    @PreAuthorize("@auth.isAdmin()")
    public EMallsResponseEntity<PaginatedResponse<CartDto>> getAllCarts(
            CartSpec spec, Pageable pageable) {
        return EMallsResponseEntity.ok(cartService.getAllCarts(spec, pageable));
    }
}
