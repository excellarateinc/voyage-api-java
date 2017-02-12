package voyage.security

import voyage.security.permission.Permission
import voyage.security.permission.PermissionService
import voyage.security.user.User
import voyage.security.user.UserService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// TODO Create a unit test for this class

@Service
@Transactional(readOnly = true)
class PermissionBasedUserDetailsService implements UserDetailsService {
    private final UserService userService
    private final PermissionService permissionService

    PermissionBasedUserDetailsService(UserService userService, PermissionService permissionService) {
        this.userService = userService
        this.permissionService = permissionService
    }

    @Override
    PermissionBasedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username)
        if (!user || !user.isEnabled) {
            throw new UsernameNotFoundException("User ${username} was not found.")
        }
        return new PermissionBasedUserDetails(user, getAuthorities(user))
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<SimpleGrantedAuthority> authorities = [] as Set<SimpleGrantedAuthority>
        Iterable<Permission> permissions = permissionService.findAllByUser(user.id)
        permissions?.each { permission ->
            authorities.add(new SimpleGrantedAuthority(permission.name))
        }
        return authorities
    }
}
