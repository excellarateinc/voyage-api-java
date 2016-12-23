package launchpad.security

import launchpad.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

class PermissionBasedUserDetails implements UserDetails {
    private String password
    private String username
    private Set<GrantedAuthority> authorities
    private boolean accountNonExpired
    private boolean accountNonLocked
    private boolean credentialsNonExpired
    private boolean enabled

    PermissionBasedUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.username = user.username
        this.password = user.password
        this.enabled = user.isEnabled
        this.accountNonExpired = !user.isAccountExpired
        this.credentialsNonExpired = !user.isCredentialsExpired
        this.accountNonLocked = !user.isAccountLocked
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities))
    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection")

        // Ensure array iteration order is predictable (as per UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<GrantedAuthority>(new AuthorityComparator())
        authorities.each { grantedAuthority ->
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements")
            sortedAuthorities.add(grantedAuthority)
        }
        return sortedAuthorities
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {
        int compare(GrantedAuthority g1, GrantedAuthority g2) {
            if (!g2.authority) {
                return -1
            }

            if (!g1.authority) {
                return 1
            }

            return g1.authority <=> g2.authority
        }
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities
    }

    @Override
    String getPassword() {
        return password
    }

    @Override
    String getUsername() {
        return username
    }

    @Override
    boolean isAccountNonExpired() {
        return accountNonExpired
    }

    @Override
    boolean isAccountNonLocked() {
        return accountNonLocked
    }

    @Override
    boolean isCredentialsNonExpired() {
        return credentialsNonExpired
    }

    @Override
    boolean isEnabled() {
        return enabled
    }
}