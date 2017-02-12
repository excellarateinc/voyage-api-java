package voyage.test

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class TestAuthentication implements Authentication {
    Collection<? extends GrantedAuthority> authorities = []
    Object credentials
    Object details
    String principal = "test"
    private boolean isAuthenticated = true
    String name = "test"

    @Override
    boolean isAuthenticated() {
        return isAuthenticated
    }

    @Override
    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated
    }
}
