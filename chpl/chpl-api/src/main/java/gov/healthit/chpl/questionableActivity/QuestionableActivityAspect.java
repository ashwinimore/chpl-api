package gov.healthit.chpl.questionableActivity;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Component
@Aspect
public class QuestionableActivityAspect implements EnvironmentAware {
    @Autowired private Environment env;
    @Autowired private MessageSource messageSource;
    @Autowired private CertificationResultRules certResultRules;
    @Autowired private CertifiedProductDetailsManager cpdManager;
    @Autowired private QuestionableActivityDAO questionableActivityDao;
    @Autowired private CertifiedProductDAO listingDao;
    @Autowired private DeveloperQuestionableActivityProvider developerQuestionableActivityProvider;
    @Autowired private ProductQuestionableActivityProvider productQuestionableActivityProvider;
    @Autowired private VersionQuestionableActivityProvider versionQuestionableActivityProvider;
    @Autowired private ListingQuestionableActivityProvider listingQuestionableActivityProvider;
    @Autowired private CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider;

    private List<QuestionableActivityTriggerDTO> triggerTypes;
    private long listingActivityThresholdMillis = -1;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    public QuestionableActivityAspect() {
    }

    @Override
    public void setEnvironment(final Environment e) {
        this.env = e;
        String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
        int activityThresholdDays = Integer.parseInt(activityThresholdDaysStr);
        listingActivityThresholdMillis = activityThresholdDays * MILLIS_PER_DAY;

        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    @Before("execution(* "
            + "gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProductDeprecated(..)) && "
            + "args(updateRequest,..)")
    public void checkReasonProvidedIfRequiredOnListingUpdateDeprecated(final ListingUpdateRequest updateRequest)
            throws EntityRetrievalException, MissingReasonException {
        checkReasonProvidedIfRequiredOnListingUpdate(updateRequest);
    }

    @Before("execution(* gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProduct(..)) && "
            + "args(updateRequest,..)")
    public void checkReasonProvidedIfRequiredOnListingUpdate(final ListingUpdateRequest updateRequest)
            throws EntityRetrievalException, MissingReasonException {
        CertifiedProductSearchDetails newListing = updateRequest.getListing();
        CertifiedProductSearchDetails origListing = cpdManager.getCertifiedProductDetails(newListing.getId());
        List<QuestionableActivityListingDTO> activities;

        QuestionableActivityListingDTO activity = listingQuestionableActivityProvider
                .check2011EditionUpdated(origListing, newListing);
        if (activity != null && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }

        activities = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
        if (activities.size() > 0 && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }

        activities = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
        if (activities.size() > 0 && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }

        activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
        if (activity != null
                && newListing.getCurrentStatus().getStatus().getName().toUpperCase(Locale.ENGLISH).equals(
                        CertificationStatusType.Active.getName().toUpperCase(Locale.ENGLISH))
                &&  StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale()),
                    origListing.getCurrentStatus().getStatus().getName()));
        }
    }

    @Before("execution(* gov.healthit.chpl.web.controller.SurveillanceController.deleteSurveillance(..)) && "
            + "args(surveillanceId,requestBody,..)")
    public void checkReasonProvidedIfRequiredOnSurveillanceUpdate(final Long surveillanceId,
            final SimpleExplainableAction requestBody)
                    throws MissingReasonException {
        if (surveillanceId != null && (requestBody == null
                ||  StringUtils.isEmpty(requestBody.getReason()))) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("surveillance.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }
    }

    /**
     * Any activity added with a reason would be handled here.
     * @param concept
     * @param objectId
     * @param activityDescription
     * @param originalData
     * @param newData
     * @param reason
     */
    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..)) && "
            + "args(concept,objectId,activityDescription,originalData,newData,reason,..)")
    public void checkQuestionableActivityWithReason(final ActivityConcept concept,
            final Long objectId, String activityDescription, final Object originalData,
            final Object newData, final String reason) {
        if (originalData == null || newData == null
                || !originalData.getClass().equals(newData.getClass())
                || Util.getCurrentUser() == null) {
            return;
        }

        //all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = Util.getCurrentUser().getId();

        if (originalData instanceof CertifiedProductSearchDetails
                && newData instanceof CertifiedProductSearchDetails) {
            CertifiedProductSearchDetails origListing = (CertifiedProductSearchDetails) originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails) newData;

            //look for any of the listing questionable activity
            checkListingQuestionableActivity(origListing, newListing, activityDate, activityUser, reason);

            //check for cert result questionable activity
            //outside of the acceptable activity threshold

            //get confirm date of the listing to check against the threshold
            Date confirmDate = listingDao.getConfirmDate(origListing.getId());
            if (confirmDate != null && newListing.getLastModifiedDate() != null
                    && (newListing.getLastModifiedDate().longValue()
                            - confirmDate.getTime() > listingActivityThresholdMillis)) {

                //look for certification result questionable activity
                if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0
                        && newListing.getCertificationResults() != null
                        && newListing.getCertificationResults().size() > 0) {

                    //all cert results are in the details so find matches based on the
                    //original and new criteria number fields
                    for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                        for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                            if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                                checkCertificationResultQuestionableActivity(origCertResult, newCertResult,
                                        activityDate, activityUser, reason);
                            }
                        }
                    }
                }
            }
        }
    }

    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..)) && "
            + "args(concept,objectId,activityDescription,originalData,newData,..)")
    public void checkQuestionableActivity(final ActivityConcept concept,
            final Long objectId, final String activityDescription, final Object originalData, final Object newData) {
        if (originalData == null || newData == null
                || !originalData.getClass().equals(newData.getClass())
                || Util.getCurrentUser() == null) {
            return;
        }

        //all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = Util.getCurrentUser().getId();

        if (originalData instanceof DeveloperDTO && newData instanceof DeveloperDTO) {
            DeveloperDTO origDeveloper = (DeveloperDTO) originalData;
            DeveloperDTO newDeveloper = (DeveloperDTO) newData;
            checkDeveloperQuestionableActivity(origDeveloper, newDeveloper, activityDate, activityUser);
        } else if (originalData instanceof ProductDTO && newData instanceof ProductDTO) {
            ProductDTO origProduct = (ProductDTO) originalData;
            ProductDTO newProduct = (ProductDTO) newData;
            checkProductQuestionableActivity(origProduct, newProduct, activityDate, activityUser);
        } else if (originalData instanceof ProductVersionDTO && newData instanceof ProductVersionDTO) {
            ProductVersionDTO origVersion = (ProductVersionDTO) originalData;
            ProductVersionDTO newVersion = (ProductVersionDTO) newData;
            checkVersionQuestionableActivity(origVersion, newVersion, activityDate, activityUser);
        }
    }

    /**
     * checks for developer name changes, current status change, or status history change (add remove and edit)
     * @param origDeveloper
     * @param newDeveloper
     * @param activityDate
     * @param activityUser
     */
    private void checkDeveloperQuestionableActivity(final DeveloperDTO origDeveloper, final DeveloperDTO newDeveloper,
            final Date activityDate, final Long activityUser) {
        QuestionableActivityDeveloperDTO devActivity = null;
        List<QuestionableActivityDeveloperDTO> devActivities = null;

        devActivity = developerQuestionableActivityProvider.checkNameUpdated(origDeveloper, newDeveloper);
        if (devActivity != null) {
            createDeveloperActivity(devActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED);
        }

        devActivity = developerQuestionableActivityProvider.checkCurrentStatusChanged(origDeveloper, newDeveloper);
        if (devActivity != null) {
            createDeveloperActivity(devActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryAdded(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED);
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryRemoved(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED);
        }

        devActivities = developerQuestionableActivityProvider.checkStatusHistoryItemEdited(
                origDeveloper.getStatusEvents(), newDeveloper.getStatusEvents());
        for (QuestionableActivityDeveloperDTO currDevActivity : devActivities) {
            createDeveloperActivity(currDevActivity, newDeveloper.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED);
        }
    }

    /**
     * checks for product name change, current owner change, owner history change (add remove and edit)
     * @param origProduct
     * @param newProduct
     * @param activityDate
     * @param activityUser
     */
    private void checkProductQuestionableActivity(final ProductDTO origProduct, final ProductDTO newProduct,
            final Date activityDate, final Long activityUser) {
        QuestionableActivityProductDTO productActivity = null;
        List<QuestionableActivityProductDTO> productActivities = null;

        productActivity = productQuestionableActivityProvider.checkNameUpdated(origProduct, newProduct);
        if (productActivity != null) {
            createProductActivity(productActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED);
        }

        productActivity = productQuestionableActivityProvider.checkCurrentOwnerChanged(origProduct, newProduct);
        if (productActivity != null) {
            createProductActivity(productActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryAdded(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryRemoved(origProduct.getOwnerHistory(),
                newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_REMOVED);
        }

        productActivities = productQuestionableActivityProvider.checkOwnerHistoryItemEdited(
                origProduct.getOwnerHistory(), newProduct.getOwnerHistory());
        for (QuestionableActivityProductDTO currProductActivity : productActivities) {
            createProductActivity(currProductActivity, newProduct.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_EDITED);
        }
    }

    /**
     * checks for version name change
     * @param origVersion
     * @param newVersion
     * @param activityDate
     * @param activityUser
     */
    private void checkVersionQuestionableActivity(final ProductVersionDTO origVersion,
            final ProductVersionDTO newVersion, final Date activityDate, final Long activityUser) {
        QuestionableActivityVersionDTO activity
        = versionQuestionableActivityProvider.checkNameUpdated(origVersion, newVersion);
        if (activity != null) {
            createVersionActivity(activity, origVersion.getId(), activityDate,
                    activityUser, QuestionableActivityTriggerConcept.VERSION_NAME_EDITED);
        }
    }

    /**
     * checks for various listing changes - add or remove certs and cqms, deleting surveillance,
     * editing certification date.
     * @param origListing
     * @param newListing
     * @param activityDate
     * @param activityUser
     */
    private void checkListingQuestionableActivity(final CertifiedProductSearchDetails origListing,
            final CertifiedProductSearchDetails newListing, final Date activityDate, final Long activityUser,
            final String activityReason) {
        QuestionableActivityListingDTO activity = listingQuestionableActivityProvider.check2011EditionUpdated(
                origListing, newListing);
        if (activity != null) {
            createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                    QuestionableActivityTriggerConcept.EDITION_2011_EDITED, activityReason);
        } else {
            //it wasn't a 2011 update, check for any changes that are questionable at any time
            activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(
                    CertificationStatusType.WithdrawnByDeveloperUnderReview, origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT, activityReason);
            }
            activity = listingQuestionableActivityProvider.checkCertificationStatusHistoryUpdated(
                    origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY, activityReason);
            }
            activity = listingQuestionableActivityProvider.checkTestingLabChanged(
                    origListing, newListing);
            if (activity != null) {
                createListingActivity(activity, origListing.getId(), activityDate, activityUser,
                        QuestionableActivityTriggerConcept.TESTING_LAB_CHANGED, activityReason);
            }
            //finally check for other changes that are only questionable
            //outside of the acceptable activity threshold

            //get the confirm date of the listing to check against the threshold
            Date confirmDate = listingDao.getConfirmDate(origListing.getId());
            if (confirmDate != null && newListing.getLastModifiedDate() != null
                    && (newListing.getLastModifiedDate().longValue()
                            - confirmDate.getTime() > listingActivityThresholdMillis)) {
                activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
                if (activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT,
                            activityReason);
                }
                activity = listingQuestionableActivityProvider.checkCertificationStatusDateUpdated(
                        origListing, newListing);
                if (activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT,
                            activityReason);
                }
                activity = listingQuestionableActivityProvider.checkSurveillanceDeleted(origListing, newListing);
                if (activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED,
                            activityReason);
                }

                List<QuestionableActivityListingDTO> activities = listingQuestionableActivityProvider
                        .checkCqmsAdded(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CQM_ADDED,
                            activityReason);
                }

                activities = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CQM_REMOVED,
                            activityReason);
                }

                activities = listingQuestionableActivityProvider.checkCertificationsAdded(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CRITERIA_ADDED,
                            activityReason);
                }

                activities = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
                for (QuestionableActivityListingDTO currActivity : activities) {
                    createListingActivity(currActivity, origListing.getId(), activityDate,
                            activityUser, QuestionableActivityTriggerConcept.CRITERIA_REMOVED,
                            activityReason);
                }
            }
        }
    }

    /**
     * checks for changes to listing certification results; g1/g2 boolean changes, macra measures added
     * or removed for g1/g2, or changes to gap
     * @param origCertResult
     * @param newCertResult
     * @param activityDate
     * @param activityUser
     */
    private void checkCertificationResultQuestionableActivity(final CertificationResult origCertResult,
            final CertificationResult newCertResult, final Date activityDate, final Long activityUser,
            final String activityReason) {
        QuestionableActivityCertificationResultDTO certActivity = null;
        List<QuestionableActivityCertificationResultDTO> certActivities = null;

        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG1SuccessUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG2SuccessUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.GAP)) {
            certActivity = certResultQuestionableActivityProvider.checkGapUpdated(origCertResult, newCertResult);
            if (certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.GAP_EDITED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG1MacraMeasuresAdded(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G1_MEASURE_ADDED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG1MacraMeasuresRemoved(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG2MacraMeasuresAdded(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G2_MEASURE_ADDED, activityReason);
            }
        }
        if (certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            certActivities = certResultQuestionableActivityProvider
                    .checkG2MacraMeasuresRemoved(origCertResult, newCertResult);
            for (QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate,
                        activityUser, QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED, activityReason);
            }
        }
    }

    private void createListingActivity(final QuestionableActivityListingDTO activity, final Long listingId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger,
            final String activityReason) {
        activity.setListingId(listingId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createCertificationActivity(final QuestionableActivityCertificationResultDTO activity,
            final Long certResultId, final Date activityDate, final Long activityUser,
            final QuestionableActivityTriggerConcept trigger, final String activityReason) {
        activity.setCertResultId(certResultId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setReason(activityReason);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createDeveloperActivity(final QuestionableActivityDeveloperDTO activity, final Long developerId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger) {
        activity.setDeveloperId(developerId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createProductActivity(final QuestionableActivityProductDTO activity, final Long productId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger) {
        activity.setProductId(productId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private void createVersionActivity(final QuestionableActivityVersionDTO activity, final Long versionId,
            final Date activityDate, final Long activityUser, final QuestionableActivityTriggerConcept trigger) {
        activity.setVersionId(versionId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        QuestionableActivityTriggerDTO triggerDto = getTrigger(trigger);
        activity.setTriggerId(triggerDto.getId());
        questionableActivityDao.create(activity);
    }

    private QuestionableActivityTriggerDTO getTrigger(final QuestionableActivityTriggerConcept trigger) {
        QuestionableActivityTriggerDTO result = null;
        for (QuestionableActivityTriggerDTO currTrigger : triggerTypes) {
            if (trigger.getName().equalsIgnoreCase(currTrigger.getName())) {
                result = currTrigger;
            }
        }
        return result;
    }
}