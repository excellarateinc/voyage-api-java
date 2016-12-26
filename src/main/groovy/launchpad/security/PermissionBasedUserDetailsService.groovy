package launchpad.security

import launchpad.permission.Permission
import launchpad.permission.PermissionService
import launchpad.user.User
import launchpad.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PermissionBasedUserDetailsService implements UserDetailsService {
    private final UserService userService
    private final PermissionService permissionService

    @Autowired
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
