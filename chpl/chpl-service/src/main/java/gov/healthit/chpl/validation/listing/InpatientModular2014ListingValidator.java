package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientG1G2RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientRequiredTestToolReviewer;

@Component
public class InpatientModular2014ListingValidator extends Edition2014ListingValidator {
    @Autowired protected InpatientG1G2RequiredData2014Reviewer g1g2Reviewer;
    @Autowired protected InpatientRequiredTestToolReviewer ttReviewer;
    
    public InpatientModular2014ListingValidator() {
        super();
        reviewers.add(g1g2Reviewer);
        reviewers.add(ttReviewer);
    }
    
    @Override
    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
