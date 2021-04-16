package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;

@Repository(value = "certificationResultDetailsDAO")
public class CertificationResultDetailsDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertificationResultDetailsDAO.class);

    private static final String BASE_SQL = "SELECT DISTINCT cr "
            + "FROM CertificationResultDetailsEntity cr "
            + "LEFT OUTER JOIN FETCH cr.certificationCriterion cc "
            + "LEFT OUTER JOIN FETCH cc.certificationEdition ed "
            + "LEFT OUTER JOIN FETCH cr.certificationResultTestData crtd "
            + "LEFT OUTER JOIN FETCH crtd.testData td "
            + "LEFT OUTER JOIN FETCH cr.certificationResultTestFunctionalities crtf "
            + "LEFT OUTER JOIN FETCH crtf.testFunctionality tf "
            + "LEFT OUTER JOIN FETCH cr.certificationResultTestProcedures crtp "
            + "LEFT OUTER JOIN FETCH crtp.testProcedure tp "
            + "LEFT OUTER JOIN FETCH cr.certificationResultTestTools crtt "
            + "LEFT OUTER JOIN FETCH crtt.testTool tt "
            + "LEFT OUTER JOIN FETCH cr.certificationResultTestStandards crts "
            + "LEFT OUTER JOIN FETCH cr.certificationResultAdditionalSoftware cras "
            + "WHERE cr.deleted = false "
            + "AND (crtd.deleted = false OR crtd.deleted IS NULL) "
            + "AND (crtf.deleted = false OR crtf.deleted IS NULL) "
            + "AND (crtp.deleted = false OR crtp.deleted IS NULL) "
            + "AND (crtt.deleted = false OR crtt.deleted IS NULL) "
            + "AND (crts.deleted = false OR crts.deleted IS NULL) "
            + "AND (cras.deleted = false OR cras.deleted IS NULL) ";

    @Transactional
    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(
            final Long certifiedProductId) throws EntityRetrievalException {
        List<CertificationResultDetailsEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertificationResultDetailsDTO> dtos = new ArrayList<CertificationResultDetailsDTO>(entities.size());

        for (CertificationResultDetailsEntity entity : entities) {
            dtos.add(new CertificationResultDetailsDTO(entity));
        }
        return dtos;
    }

    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductIdSED(
            final Long certifiedProductId) throws EntityRetrievalException {
        List<CertificationResultDetailsEntity> entities = getEntitiesByCertifiedProductIdSED(certifiedProductId);
        List<CertificationResultDetailsDTO> dtos = new ArrayList<CertificationResultDetailsDTO>(entities.size());

        for (CertificationResultDetailsEntity entity : entities) {
            dtos.add(new CertificationResultDetailsDTO(entity));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<CertificationResultDetailsDTO> getByUrl(String url, UrlType urlType) {
        String queryStr = BASE_SQL;
        switch (urlType) {
        case API_DOCUMENTATION:
            queryStr += "AND crd.apiDocumentation = :url";
            break;
        case EXPORT_DOCUMENTATION:
            queryStr += "AND crd.exportDocumentation = :url";
            break;
        case DOCUMENTATION:
            queryStr += "AND crd.documentationUrl = :url";
            break;
        case USE_CASES:
            queryStr += "AND crd.useCases = :url";
            break;
        default:
            break;
        }
        Query query = entityManager.createQuery(queryStr, CertificationResultDetailsEntity.class);
        query.setParameter("url", url);
        List<CertificationResultDetailsEntity> entities = query.getResultList();
        List<CertificationResultDetailsDTO> resultDtos = new ArrayList<CertificationResultDetailsDTO>();
        for (CertificationResultDetailsEntity entity : entities) {
            resultDtos.add(new CertificationResultDetailsDTO(entity));
        }
        return resultDtos;
    }

    public List<CertificationResultDetailsDTO> getCertificationResultsForSurveillanceListing(Surveillance surv) {
        List<CertificationResultDetailsDTO> certResults = null;
        if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
            try {
                certResults = getCertificationResultDetailsByCertifiedProductId(surv.getCertifiedProduct().getId());
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find cert results for certified product " + surv.getCertifiedProduct().getId(),
                        ex);
            }
        }
        return certResults;
    }

    private List<CertificationResultDetailsEntity> getEntitiesByCertifiedProductId(final Long productId) throws EntityRetrievalException {

        Query query = entityManager.createQuery(BASE_SQL
                        + "AND crd.certifiedProductId = :entityid ",
                CertificationResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CertificationResultDetailsEntity> result = query.getResultList();

        return result;
    }

    private List<CertificationResultDetailsEntity> getEntitiesByCertifiedProductIdSED(final Long productId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(BASE_SQL
                        + "AND crd.success = true "
                        + "AND crd.sed = true ",
                CertificationResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CertificationResultDetailsEntity> result = query.getResultList();

        return result;
    }

    public List<CertificationResultDetailsDTO> getAllCertResultsForListing(Long listingId) {
        Query query = entityManager.createQuery(BASE_SQL
                + "AND cr.certifiedProductId = :listingId",
                CertificationResultDetailsEntity.class);

        query.setParameter("listingId", listingId);

        List<CertificationResultDetailsEntity> result = query.getResultList();
        if (result == null) {
            return null;
        } else {
            return result.stream()
                    .map(entity -> new CertificationResultDetailsDTO(entity))
                    .collect(Collectors.toList());
        }
    }
}
