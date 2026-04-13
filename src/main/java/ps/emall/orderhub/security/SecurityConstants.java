package ps.emall.orderhub.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    // ==================== JWT ====================
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // ==================== Token Claims ====================
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String CLAIM_SHOP_IDS = "shopIds";

    // ==================== Token Types ====================
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    public static final String TOKEN_TYPE_TEMP = "TEMP";

    // ==================== Roles ====================
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_SHOP_OWNER = "ROLE_SHOP_OWNER";

    // ==================== Public Endpoints ====================
    public static final String[] PUBLIC_URLS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/actuator/**",
            "/error",
    };
}