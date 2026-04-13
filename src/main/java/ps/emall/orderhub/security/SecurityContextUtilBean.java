package ps.emall.orderhub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("auth")
@RequiredArgsConstructor
public class SecurityContextUtilBean {

    public boolean isAdmin() {
        return SecurityContextUtil.isAdmin();
    }

    public boolean isCustomer() {
        return SecurityContextUtil.isCustomer();
    }

    public boolean isShopOwner() {
        return SecurityContextUtil.isShopOwner();
    }

    public Long getCurrentUserId() {
        return SecurityContextUtil.getCurrentUserId();
    }

    public String getCurrentUsername() {
        return SecurityContextUtil.getCurrentUsername();
    }

    /**
     * True if the current user is ADMIN, or is a SHOP_OWNER who owns the given shop.
     * Use this in @PreAuthorize for any shop-scoped write operation.
     */
    public boolean isAdminOrShopOwnerOf(Long shopId) {
        return SecurityContextUtil.isShopOwnerOf(shopId);
    }

    public boolean isShopOwnerOf(Long shopId) {
        return SecurityContextUtil.isShopOwnerOfNotAdmin(shopId);
    }


    public boolean adminOnly() {
        return SecurityContextUtil.isAdmin();
    }
}