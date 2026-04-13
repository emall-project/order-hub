package ps.emall.orderhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ps.emall.orderhub.security.userdetails.CustomUserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SecurityContextUtil {

    private SecurityContextUtil() {}

    // ==================== Get Authentication ====================

    public static Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(CustomUserDetails.class::isInstance)
                .map(CustomUserDetails.class::cast);
    }

    // ==================== Get User Fields ====================

    public static Long getCurrentUserId() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getUserId)
                .orElseThrow(() -> new SecurityException("User is not authenticated"));
    }

    public static Long getCurrentUserIdOrNull() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getUserId)
                .orElse(null);
    }

    public static String getCurrentUsername() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getUsername)
                .orElseThrow(() -> new SecurityException("User is not authenticated"));
    }

    public static String getCurrentUsernameOrSystem() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getUsername)
                .orElse("SYSTEM");
    }

    public static String getCurrentRole() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getRoleCode)
                .orElseThrow(() -> new SecurityException("User is not authenticated"));
    }

    // Returns the current shop owner's shop IDs (empty list for other roles).
    public static List<Long> getCurrentShopIds() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getShopIds)
                .orElse(Collections.emptyList());
    }

    // ==================== Role Checks ====================

    public static boolean isAdmin() {
        return hasAuthority(SecurityConstants.ROLE_ADMIN);
    }

    public static boolean isCustomer() {
        return hasAuthority(SecurityConstants.ROLE_CUSTOMER);
    }

    public static boolean isShopOwner() {
        return hasAuthority(SecurityConstants.ROLE_SHOP_OWNER);
    }

    public static boolean hasAuthority(String authority) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals(authority)))
                .orElse(false);
    }

    public static boolean hasAnyAuthority(String... authorities) {
        for (String authority : authorities) {
            if (hasAuthority(authority)) return true;
        }
        return false;
    }

    // ==================== Ownership Checks ====================

    /**
     * Returns true if the current authenticated shop owner owns the given shopId.
     * Admins always pass.
     */
    public static boolean isShopOwnerOf(Long shopId) {
        if (isAdmin()) return true;
        if (!isShopOwner()) return false;
        return getCurrentShopIds().contains(shopId);
    }

    public static boolean isShopOwnerOfNotAdmin(Long shopId) {
        if (isShopOwner()) {
            return getCurrentShopIds().contains(shopId);
        } else return false;
    }


    // ==================== Authentication Checks ====================

    public static boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }
}