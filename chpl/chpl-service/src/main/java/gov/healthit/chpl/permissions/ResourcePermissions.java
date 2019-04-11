package gov.healthit.chpl.permissions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ResourcePermissions {
    private PermissionEvaluator permissionEvaluator;
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyDAO acbDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private TestingLabDAO atlDAO;
    private UserDAO userDAO;

    @Autowired
    public ResourcePermissions(final PermissionEvaluator permissionEvaluator,
            final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final CertificationBodyDAO acbDAO,
            final UserTestingLabMapDAO userTestingLabMapDAO, final TestingLabDAO atlDAO,
            final ErrorMessageUtil errorMessageUtil, final UserDAO userDAO) {

        this.permissionEvaluator = permissionEvaluator;
        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.acbDAO = acbDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.atlDAO = atlDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.userDAO = userDAO;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByName(final String userName) throws UserRetrievalException {
        return userDAO.getByName(userName);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnAcb(final CertificationBodyDTO acb) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByAcbId(acb.getId());

        for (UserCertificationBodyMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnAtl(final TestingLabDTO atl) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByAtlId(atl.getId());

        for (UserTestingLabMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                acbs = acbDAO.findAll();
            } else {
                List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(user.getId());
                for (UserCertificationBodyMapDTO dto : dtos) {
                    acbs.add(dto.getCertificationBody());
                }
            }
        }
        return acbs;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForUser(final Long userID) {
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userID);
        for (UserCertificationBodyMapDTO dto : dtos) {
            acbs.add(dto.getCertificationBody());
        }
        return acbs;
    }

    @Transactional(readOnly = true)
    public List<TestingLabDTO> getAllAtlsForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<TestingLabDTO> atls = new ArrayList<TestingLabDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                atls = atlDAO.findAll();
            } else {
                List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(user.getId());
                for (UserTestingLabMapDTO dto : dtos) {
                    atls.add(dto.getTestingLab());
                }
            }
        }
        return atls;
    }

    @Transactional(readOnly = true)
    public List<TestingLabDTO> getAllAtlsForUser(final Long userId) {
        List<TestingLabDTO> atls = new ArrayList<TestingLabDTO>();
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);
        for (UserTestingLabMapDTO dto : dtos) {
            atls.add(dto.getTestingLab());
        }
        return atls;
    }

    @Transactional(readOnly = true)
    public CertificationBodyDTO getAcbIfPermissionById(final Long id) throws EntityRetrievalException {
        try {
            acbDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("acb.notFound"));
        }

        List<CertificationBodyDTO> dtos = getAllAcbsForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<CertificationBodyDTO>() {
            @Override
            public boolean evaluate(final CertificationBodyDTO object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Transactional(readOnly = true)
    public TestingLabDTO getAtlIfPermissionById(final Long id) throws EntityRetrievalException {
        try {
            acbDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("atl.notFound"));
        }

        List<TestingLabDTO> dtos = getAllAtlsForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<TestingLabDTO>() {
            @Override
            public boolean evaluate(final TestingLabDTO object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Transactional(readOnly = true)
    public UserPermissionDTO getRoleByUserId(final Long userId) {
        try {
            UserDTO user = userDAO.getById(userId);
        return user.getPermission();
        } catch (UserRetrievalException ex) { }
        return null;
    }

    /**
     * Determines if the current user has permissions to access
     * the account of the passed-in user.
     * Rules are: Admin and Onc can access all users.
     * Acb can access any other ROLE_ACB user who is also on their ACB.
     * Atl can access any other ROLE_ATL user who is also on their ATL.
     * All users can access themselves.
     * @param user
     * @return
     */
    @Transactional(readOnly = true)
    public boolean hasPermissionOnUser(final UserDTO user) {
        if (isUserRoleAdmin() || isUserRoleOnc()
                || permissionEvaluator.hasPermission(AuthUtil.getCurrentUser(), user,
                        BasePermission.ADMINISTRATION)) {
            return true;
        } else if (isUserRoleAcbAdmin()) {
            //is the user being checked on any of the same ACB(s) that the current user is on?
            List<CertificationBodyDTO> currUserAcbs = getAllAcbsForCurrentUser();
            List<CertificationBodyDTO> otherUserAcbs = getAllAcbsForUser(user.getId());
            for (CertificationBodyDTO currUserAcb : currUserAcbs) {
                for (CertificationBodyDTO otherUserAcb : otherUserAcbs) {
                    if (currUserAcb.getId().equals(otherUserAcb.getId())) {
                        return true;
                    }
                }
            }
        } else if (isUserRoleAtlAdmin()) {
            //is the user being checked on any of the same ATL(s) that the current user is on?
            List<TestingLabDTO> currUserAtls = getAllAtlsForCurrentUser();
            List<TestingLabDTO> otherUserAtls = getAllAtlsForUser(user.getId());
            for (TestingLabDTO currUserAtl : currUserAtls) {
                for (TestingLabDTO otherUserAtl : otherUserAtls) {
                    if (currUserAtl.getId().equals(otherUserAtl.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isUserRoleAdmin() {
        return doesUserHaveRole(Authority.ROLE_ADMIN);
    }

    public boolean isUserRoleOnc() {
        return doesUserHaveRole(Authority.ROLE_ONC);
    }

    public boolean isUserRoleCmsStaff() {
        return doesUserHaveRole(Authority.ROLE_CMS_STAFF);
    }

    public boolean isUserRoleAcbAdmin() {
        return doesUserHaveRole(Authority.ROLE_ACB);
    }

    public boolean isUserRoleAtlAdmin() {
        return doesUserHaveRole(Authority.ROLE_ATL);
    }

    public boolean isUserRoleUserCreator() {
        return doesUserHaveRole(Authority.ROLE_USER_CREATOR);
    }

    public boolean isUserRoleUserAuthenticator() {
        return doesAuthenticationHaveRole(Authority.ROLE_USER_AUTHENTICATOR);
    }

    public boolean isUserRoleInvitedUserCreator() {
        return doesAuthenticationHaveRole(Authority.ROLE_INVITED_USER_CREATOR);
    }

    public boolean isUserAnonymous() {
        return AuthUtil.getCurrentUser() == null;
    }

    private boolean doesUserHaveRole(final String authority) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return false;
        }

        UserPermissionDTO role = getRoleByUserId(user.getId());
        if (role == null) {
            return false;
        }
        return role.getAuthority().equalsIgnoreCase(authority);
    }

    private boolean doesAuthenticationHaveRole(final String authority) {
        Authentication auth = AuthUtil.getCurrentAuthentication();
        if (auth == null) {
            return false;
        }

        for (GrantedAuthority role : auth.getAuthorities()) {
            if (role.getAuthority().contentEquals(authority)) {
                return true;
            }
        }
        return false;
    }

}
