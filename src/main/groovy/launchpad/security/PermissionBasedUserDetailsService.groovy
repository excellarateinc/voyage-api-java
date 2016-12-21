package launchpad.security

import launchpad.role.Role
import launchpad.user.User
import launchpad.user.UserRole
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
    @Autowired
    private UserService userService

    @Override
    PermissionBasedUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username)
        if (!user || !user.isEnabled) {
            throw new UsernameNotFoundException("User ${username} was not found.")
        }
        return new PermissionBasedUserDetails(user, getAuthorities(user.userRoles))
    }

    private static class SimpleGrantedAuthorityComparator implements Comparator<SimpleGrantedAuthority> {
        @Override
        int compare(SimpleGrantedAuthority o1, SimpleGrantedAuthority o2) {
            return o1 == o2 ? 0 : -1
        }
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(Set<UserRole> userRoles) {
        Set<SimpleGrantedAuthority> authList = new TreeSet<SimpleGrantedAuthority>(new SimpleGrantedAuthorityComparator())
        userRoles.each { userRole ->
            authList.add(new SimpleGrantedAuthority(userRole.role.authority))
            authList.addAll(getGrantedAuthorities(userRole.role))
        }
        return authList
    }

    private static Set<SimpleGrantedAuthority> getGrantedAuthorities(Role role) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<SimpleGrantedAuthority>()
        role.rolePermissions.each { rolePermission ->
            authorities.add(new SimpleGrantedAuthority(rolePermission.permission.name))
        }
        return authorities
    }
}