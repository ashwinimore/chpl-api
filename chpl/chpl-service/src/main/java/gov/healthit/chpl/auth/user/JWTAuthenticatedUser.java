package gov.healthit.chpl.auth.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dto.auth.UserDTO;

public class JWTAuthenticatedUser implements User {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String subjectName;
    private String fullName;
    private String friendlyName;
    private Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
    private final boolean accountExpired = false;
    private final boolean accountLocked = false;
    private final boolean credentialsExpired = false;
    private final boolean accountEnabled = true;
    private boolean passwordResetRequired = false;
    private boolean authenticated = true;
    private UserDTO impersonatingUser;

    /** Default constructor. */
    public JWTAuthenticatedUser() {
        this.subjectName = null;
    }

    public JWTAuthenticatedUser(final String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(final String subject) {
        this.subjectName = subject;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Set<GrantedPermission> getPermissions() {
        return this.permissions;
    }

    public void addPermission(final GrantedPermission permission) {
        this.permissions.add(permission);
    }

    public void addPermission(final String permissionValue) {
        GrantedPermission permission = new GrantedPermission(permissionValue);
        this.permissions.add(permission);
    }

    @Override
    public void removePermission(final String permissionValue) {
        this.permissions.remove(new GrantedPermission(permissionValue));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.getPermissions();
    }

    @Override
    public Object getCredentials() {
        return this.getPassword();
    }

    @Override
    public Object getDetails() {
        return this;
    }

    @Override
    public Object getPrincipal() {
        return this.getName();
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(final boolean arg0) throws IllegalArgumentException {
        this.authenticated = arg0;
    }

    public UserDTO getImpersonatingUser() {
        return impersonatingUser;
    }

    public void setImpersonatingUser(final UserDTO impersonatingUser) {
        this.impersonatingUser = impersonatingUser;
    }

    @Override
    public String getName() {
        return subjectName;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return subjectName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public boolean getPasswordResetRequired() {
        return passwordResetRequired;
    }

    @Override
    public void setPasswordResetRequired(final boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }
}
