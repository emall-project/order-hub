package ps.emall.orderhub.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ps.emall.orderhub.security.SecurityConstants;
import ps.emall.orderhub.security.jwt.JwtService;
import ps.emall.orderhub.security.userdetails.CustomUserDetails;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isPublicUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());

        try {
            if (jwtService.isTokenValid(jwt) && jwtService.isAccessToken(jwt)) {
                String username = jwtService.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Long userId = jwtService.extractUserId(jwt);
                    String fullName = jwtService.extractFullName(jwt);
                    String role = jwtService.extractRole(jwt);
                    List<Long> shopIds = jwtService.extractShopIds(jwt);

                    CustomUserDetails userDetails =
                            new CustomUserDetails(userId, username, fullName, role, shopIds);

                    List<GrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority(role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, authorities);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicUrl(HttpServletRequest request) {
        String path = request.getServletPath();
        return Arrays.stream(SecurityConstants.PUBLIC_URLS)
                .anyMatch(url -> {
                    if (url.endsWith("/**")) {
                        return path.startsWith(url.replace("/**", ""));
                    }
                    return path.equals(url);
                });
    }
}