package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.review.Reviewer;

/**
 * A concrete validation implementation that does not check for any errors.
 * 2011 listings are not checked against validation requirements.
 * @author kekey
 *
 */
@Component
public class AllowedListingValidator {

    private List<Reviewer> reviewers;

    public AllowedListingValidator() {
        reviewers = new ArrayList<Reviewer>();
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
