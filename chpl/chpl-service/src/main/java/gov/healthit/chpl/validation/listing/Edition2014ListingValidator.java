package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.CertificationDateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ChplNumberReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.FieldLengthReviewer;
import gov.healthit.chpl.validation.listing.reviewer.InheritedCertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.SedG3Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestFunctionalityReviewer;
import gov.healthit.chpl.validation.listing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnattestedCriteriaWithDataReviewer;
import gov.healthit.chpl.validation.listing.reviewer.UnsupportedCharacterReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.RequiredData2014Reviewer;

/**
 * Validation interface for any listing that is already uploaded and confirmed on the CHPL.
 * @author kekey
 *
 */
@Component
public abstract class Edition2014ListingValidator extends Validator {
    @Autowired protected ChplNumberReviewer chplNumberReviewer;
    @Autowired protected InheritedCertificationStatusReviewer icsReviewer;
    @Autowired protected DeveloperStatusReviewer devStatusReviewer;
    @Autowired protected UnsupportedCharacterReviewer unsupportedCharacterReviewer;
    @Autowired protected FieldLengthReviewer fieldLengthReviewer;
    @Autowired protected RequiredData2014Reviewer requiredFieldReviewer;
    @Autowired protected SedG3Reviewer sedG3Reviewer;
    @Autowired protected CertificationStatusReviewer certStatusReviewer;
    @Autowired protected CertificationDateReviewer certDateReviewer;
    @Autowired protected UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    @Autowired protected TestToolReviewer ttReviewer;
    @Autowired protected TestFunctionalityReviewer tfReviewer;
    
    protected List<Reviewer> reviewers;

    public Edition2014ListingValidator() {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(icsReviewer);
        reviewers.add(devStatusReviewer);
        reviewers.add(unsupportedCharacterReviewer);
        reviewers.add(fieldLengthReviewer);
        reviewers.add(requiredFieldReviewer);
        reviewers.add(sedG3Reviewer);
        reviewers.add(certStatusReviewer);
        reviewers.add(certDateReviewer);
        reviewers.add(unattestedCriteriaWithDataReviewer);
        reviewers.add(ttReviewer);
        reviewers.add(tfReviewer);
    }
}
