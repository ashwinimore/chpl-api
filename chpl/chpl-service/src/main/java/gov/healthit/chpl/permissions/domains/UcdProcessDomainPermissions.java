package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ucdProcess.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.ucdProcess.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.ucdProcess.UpdateActionPermissions;

@Component
public class UcdProcessDomainPermissions extends DomainPermissions {

    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public UcdProcessDomainPermissions(
            @Qualifier("ucdProcessDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("ucdProcessUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("ucdProcessCreateActionPermissions") CreateActionPermissions createActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }

}
