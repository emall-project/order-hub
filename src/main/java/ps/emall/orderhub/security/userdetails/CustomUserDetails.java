package ps.emall.orderhub.security.userdetails;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String fullName;
    private final String roleCode;
    private final List<Long> shopIds;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId, String username, String fullName,
                             String roleCode, List<Long> shopIds) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.roleCode = roleCode;
        this.shopIds = shopIds != null ? shopIds : Collections.emptyList();
        this.authorities = List.of(new SimpleGrantedAuthority(roleCode));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}