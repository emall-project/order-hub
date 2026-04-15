package ps.emall.orderhub.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ps.emall.orderhub.security.SecurityConstants;
import ps.emall.orderhub.security.dto.StoreRef;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // ==================== Extract Claims ====================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Object raw = extractClaim(token, claims -> claims.get(SecurityConstants.CLAIM_USER_ID));
        if (raw instanceof Integer) return ((Integer) raw).longValue();
        if (raw instanceof Long) return (Long) raw;
        return null;
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get(SecurityConstants.CLAIM_ROLE, String.class));
    }

    public String extractFullName(String token) {
        return extractClaim(token, claims -> claims.get(SecurityConstants.CLAIM_FULL_NAME, String.class));
    }

    public Integer extractAge(String token) {
        Object raw = extractClaim(token, claims -> claims.get(SecurityConstants.CLAIM_AGE));
        if (raw instanceof Integer) return (Integer) raw;
        return null;
    }

    public String extractGender(String token) {
        return extractClaim(token, claims ->
                claims.get(SecurityConstants.CLAIM_GENDER, String.class));
    }

    @SuppressWarnings("unchecked")
    public List<StoreRef> extractShopIds(String token) {
        Object raw = extractClaim(token, claims -> claims.get(SecurityConstants.CLAIM_SHOP_IDS));
        if (raw == null) return Collections.emptyList();
        List<?> list = (List<?>) raw;
        return list.stream()
                .map(item -> {
                    Map<?, ?> map = (Map<?, ?>) item;
                    Long storeId = toLong(map.get("storeId"));
                    Long mallId  = toLong(map.get("mallId"));
                    return new StoreRef(storeId, mallId);
                })
                .toList();
    }

    private Long toLong(Object v) {
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof Long)    return (Long) v;
        return null;
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(SecurityConstants.CLAIM_TOKEN_TYPE, String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ==================== Validation ====================

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isAccessToken(String token) {
        return SecurityConstants.TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
    }

    // ==================== Internal ====================

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}