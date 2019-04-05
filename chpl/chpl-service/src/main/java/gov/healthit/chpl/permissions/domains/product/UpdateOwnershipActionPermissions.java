package gov.healthit.chpl.permissions.domains.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productUpdateOwnershipActionPermissions")
public class UpdateOwnershipActionPermissions extends ActionPermissions {

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDao;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ProductDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ProductDTO dto = (ProductDTO) obj;
            if (isDeveloperActive(dto.getDeveloperId())) {
                return doesCurrentUserHaveAccessToAllOfDevelopersListings(dto.getDeveloperId());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean doesCurrentUserHaveAccessToAllOfDevelopersListings(Long developerId) {
        List<CertifiedProductDetailsDTO> cpDtos = certifiedProductDao.findByDeveloperId(developerId);
        for (CertifiedProductDetailsDTO cpDto : cpDtos) {
            if (!isAcbValidForCurrentUser(cpDto.getCertificationBodyId())) {
                return false;
            }
        }
        return true;
    }

    private boolean isDeveloperActive(Long developerId) {
        try {
            DeveloperDTO developerDto = developerDao.getById(developerId);
            if (developerDto != null && developerDto.getStatus() != null && developerDto.getStatus().getStatus()
                    .getStatusName().equals(DeveloperStatusType.Active.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (EntityRetrievalException e) {
            return false;
        }
    }
}
