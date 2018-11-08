package gov.healthit.chpl.listener;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheReplacer;
import gov.healthit.chpl.caching.ListingsCollectionCacheUpdater;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.UpdateDevelopersRequest;
import gov.healthit.chpl.domain.UpdateProductsRequest;
import gov.healthit.chpl.domain.UpdateVersionsRequest;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component
@Aspect
public class ListingCollectionCacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(MeaningfulUseUploadListingCollectionCacheRefreshListener.class);
    @Autowired
    private ListingsCollectionCacheUpdater cacheUpdater;

    /**
     * After a developer is updated refresh the listings collection cache.
     * @param developerInfo
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.DeveloperController.updateDeveloper(..)) && "
            + "args(developerInfo,..) || "
            + "execution(* gov.healthit.chpl.web.controller.DeveloperController.updateDeveloperDeprecated(..)) && "
            + "args(developerInfo,..)")
    @Async
    public void afterDeveloperUpdate(final UpdateDevelopersRequest developerInfo) {
        LOGGER.debug("A developer was updated. Refreshing listings collection cache. ");
        cacheUpdater.refreshCache();
    }

    /**
     * After a product is updated refresh the listings collection cache.
     * @param productInfo
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductController.updateProduct(..)) && "
            + "args(productInfo,..) || "
            + "execution(* gov.healthit.chpl.web.controller.ProductController.updateProductDeprecated(..)) && "
            + "args(productInfo,..)")
    @Async
    public void afterProductUpdate(final UpdateProductsRequest productInfo) {
        LOGGER.debug("A product was updated. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After a version is updated refresh the listings collection cache.
     * @param versionInfo
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.ProductVersionController.updateVersion(..)) && "
            + "args(versionInfo,..) || "
            + "execution(* gov.healthit.chpl.web.controller.ProductVersionController.updateVersionDeprecated(..)) && "
            + "args(versionInfo,..)")
    @Async
    public void afterVersionUpdate(final UpdateVersionsRequest versionInfo) {
        LOGGER.debug("A version was updated. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After an acb is updated refresh the listings collection cache.
     * @param acbInfo
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertificationBodyController.updateAcb(..)) && "
            + "args(acbInfo,..) || "
            + "execution(* gov.healthit.chpl.web.controller.CertificationBodyController.updateAcbDeprecated(..)) && "
            + "args(acbInfo,..)")
    @Async
    public void afterCertificationBodyUpdate(final CertificationBody acbInfo) {
        LOGGER.debug("An ACB was updated. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After a surveillance is created refresh the listings collection cache.
     * @param survToInsert
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.SurveillanceController.createSurveillance(..)) && "
            + "args(survToInsert,..) || "
            + "execution(* gov.healthit.chpl.web.controller.SurveillanceController.createSurveillanceDeprecated(..)) && "
            + "args(survToInsert,..)")
    @Async
    public void afterSurveillanceCreation(final Surveillance survToInsert) {
        LOGGER.debug("A surveillance was created. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After a surveillance is updated refresh the listings collection cache.
     * @param survToUpdate
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.SurveillanceController.updateSurveillance(..)) && "
            + "args(survToUpdate,..) || "
            + "execution(* gov.healthit.chpl.web.controller.SurveillanceController.updateSurveillanceDeprecated(..)) && "
            + "args(survToUpdate,..)")
    @Async
    public void afterSurveillanceUpdate(final Surveillance survToUpdate) {
        LOGGER.debug("A surveillance was updated. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After a surveillance is deleted refresh the listings collection cache.
     * @param survToInsert
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.SurveillanceController.deleteSurveillance(..)) && "
            + "args(surveillanceId,requestBody,..) || "
            + "execution(* gov.healthit.chpl.web.controller.SurveillanceController.deleteSurveillanceDeprecated(..)) && "
            + "args(surveillanceId,requestBody,..)")
    @Async
    public void afterSurveillanceDeletion(final Long surveillanceId, 
            final SimpleExplainableAction requestBody) {
        LOGGER.debug("A surveillance was deleted. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After a new listings is confirmed refresh the listings collection cache.
     * @param pendingCp
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.confirmPendingCertifiedProduct(..)) && "
            + "args(pendingCp,..) || "
            + "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.confirmPendingCertifiedProductDeprecated(..)) && "
            + "args(pendingCp,..)")
    @Async
    public void afterListingConfirm(final PendingCertifiedProductDetails pendingCp) {
        LOGGER.debug("A listing was confirmed. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }

    /**
     * After a listing is updated refresh the listings collection cache.
     * @param updateRequest
     */
    @AfterReturning(
            "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProduct(..)) && "
            + "args(updateRequest,..) || "
            + "execution(* gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProductDeprecated(..)) && "
            + "args(updateRequest,..)")
    @Async
    public void afterListingUpdate(final ListingUpdateRequest updateRequest) {
        LOGGER.debug("A listing was updated. Refreshing listings collection cache.");
        cacheUpdater.refreshCache();
    }
}
