package launchpad.security

import launchpad.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class PermissionBasedUserDetails implements UserDetails {
    private final User user
    private final Set<GrantedAuthority> authorities

    PermissionBasedUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user
        this.authorities = authorities
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities
    }

    @Override
    String getPassword() {
        return user.password
    }

    @Override
    String getUsername() {
        return user.username
    }

    @Override
    boolean isAccountNonExpired() {
        return !user.isAccountExpired
    }

    @Override
    boolean isAccountNonLocked() {
        return !user.isAccountLocked
    }

    @Override
    boolean isCredentialsNonExpired() {
        return !user.isCredentialsExpired
    }

    @Override
    boolean isEnabled() {
        return user.isEnabled
    }
}
