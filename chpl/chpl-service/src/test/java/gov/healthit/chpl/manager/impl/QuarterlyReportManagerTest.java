package gov.healthit.chpl.manager.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.builder.QuarterlyReportBuilderXlsx;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class QuarterlyReportManagerTest extends TestCase {

    private static JWTAuthenticatedUser adminUser, oncUser, acbUser, atlUser, cmsUser;

    @Autowired
    private SurveillanceReportManager reportManager;

    @Autowired
    private SurveillanceManager survManager;

    @Autowired
    private QuarterlyReportDAO quarterlyReportDao;

    @Autowired
    private QuarterDAO quarterDao;


    @Autowired
    private SurveillanceDAO survDao;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private QuarterlyReportBuilderXlsx reportBuilder;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        oncUser = new JWTAuthenticatedUser();
        oncUser.setFullName("ONC User");
        oncUser.setId(6L);
        oncUser.setFriendlyName("Onc");
        oncUser.setSubjectName("oncUser");
        oncUser.getPermissions().add(new GrantedPermission("ROLE_ONC"));

        acbUser = new JWTAuthenticatedUser();
        acbUser.setFullName("Test");
        acbUser.setId(3L);
        acbUser.setFriendlyName("User3");
        acbUser.setSubjectName("testUser3");
        acbUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));

        atlUser = new JWTAuthenticatedUser();
        atlUser.setFullName("ATL");
        atlUser.setId(4L);
        atlUser.setFriendlyName("User");
        atlUser.setSubjectName("atlUser");
        atlUser.getPermissions().add(new GrantedPermission("ROLE_ATL"));

        cmsUser = new JWTAuthenticatedUser();
        cmsUser.setFullName("CMS User");
        cmsUser.setId(5L);
        cmsUser.setFriendlyName("User");
        cmsUser.setSubjectName("cmsStaffUser");
        cmsUser.getPermissions().add(new GrantedPermission("ROLE_CMS_STAFF"));
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createReportTest() throws EntityCreationException, EntityRetrievalException {
        createReport();
    }

    @Test(expected = EntityCreationException.class)
    @Rollback(true)
    @Transactional
    public void createExclusionListingNotRelevantTest() throws EntityCreationException,
        InvalidArgumentsException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO createdReport = createReport();
        QuarterlyReportExclusionDTO exclusion = reportManager.createQuarterlyReportExclusion(createdReport, 1L, "Test");
        assertNotNull(exclusion);
        assertNotNull(exclusion.getId());
        assertTrue(exclusion.getId() > 0);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createExclusion() throws EntityCreationException,
        InvalidArgumentsException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long listingId = 1L;
        String reason = "test";
        QuarterlyReportDTO createdReport = createReport();
        //need a relevant surveillance
        createSurveillance(listingId, new Date(createdReport.getStartDate().getTime() + (24*60*60*1000)));
        QuarterlyReportExclusionDTO exclusion = reportManager.createQuarterlyReportExclusion(createdReport, listingId, reason);
        assertNotNull(exclusion);
        assertNotNull(exclusion.getId());
        assertTrue(exclusion.getId() > 0);
        assertEquals(listingId, exclusion.getListingId());
        assertEquals(reason, exclusion.getReason());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityCreationException.class)
    @Rollback(true)
    @Transactional
    public void createExclusionAlreadyExists() throws EntityCreationException,
        InvalidArgumentsException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO createdReport = createReport();
        //need a relevant surveillance
        createSurveillance(1L, new Date(createdReport.getStartDate().getTime() + (24*60*60*1000)));
        //original exclusion
        reportManager.createQuarterlyReportExclusion(createdReport, 1L, "original");
        //duplicate exclusion
        reportManager.createQuarterlyReportExclusion(createdReport, 1L, "duplicate");
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingAcb()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        QuarterDTO quarter = quarterDao.getById(1L);
        toCreate.setQuarter(quarter);
        toCreate.setYear(2019);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingYear()
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        QuarterDTO quarter = quarterDao.getById(1L);
        toCreate.setQuarter(quarter);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        toCreate.setAcb(acb);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportMissingQuarter() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        toCreate.setAcb(acb);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = InvalidArgumentsException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportInvalidQuarter() throws EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        toCreate.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("BOGUS");
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportWithoutQuarterId()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String quarterName = "Q1";

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        toCreate.setAcb(acb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName(quarterName);
        toCreate.setQuarter(quarter);
        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(toCreate);
        assertNotNull(createdReport);
        assertNotNull(createdReport.getId());
        assertTrue(createdReport.getId() > 0);
        assertEquals(toCreate.getYear(), createdReport.getYear());
        assertNotNull(createdReport.getAcb());
        assertNotNull(createdReport.getAcb().getId());
        assertEquals(toCreate.getAcb().getId(), createdReport.getAcb().getId());
        assertNotNull(createdReport.getQuarter());
        assertEquals(createdReport.getQuarter().getName(), quarterName);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsAnonymousUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsAtlUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(atlUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsOncUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(oncUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportAsCmsUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(cmsUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportForBadAcbAsAcbUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-2L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        reportManager.createQuarterlyReport(toCreate);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void createQuarterlyReportForAllowedAcbAsAcbUser()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        QuarterlyReportDTO created = reportManager.createQuarterlyReport(toCreate);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangePrioritizedElementSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedPrioritizedElementSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setPrioritizedElementSummary(updatedPrioritizedElementSummary);
        QuarterlyReportDTO updatedReport = reportManager.updateQuarterlyReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getPrioritizedElementSummary());
        assertEquals(updatedPrioritizedElementSummary, updatedReport.getPrioritizedElementSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeReactiveSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedReactiveSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setReactiveSummary(updatedReactiveSummary);
        QuarterlyReportDTO updatedReport = reportManager.updateQuarterlyReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getReactiveSummary());
        assertEquals(updatedReactiveSummary, updatedReport.getReactiveSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateReportChangeTransparencyDisclosureSummary() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        String updatedTransparencyDisclosureSummary = "new summary";
        QuarterlyReportDTO report = createReport();
        report.setTransparencyDisclosureSummary(updatedTransparencyDisclosureSummary);
        QuarterlyReportDTO updatedReport = reportManager.updateQuarterlyReport(report);
        assertNotNull(updatedReport);
        assertNotNull(updatedReport.getTransparencyDisclosureSummary());
        assertEquals(updatedTransparencyDisclosureSummary, updatedReport.getTransparencyDisclosureSummary());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void updateExclusionChangeReason() throws EntityCreationException,
        InvalidArgumentsException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long listingId = 1L;
        String reason = "test";
        String changedReason = "updated test";
        QuarterlyReportDTO createdReport = createReport();
        //need a relevant surveillance
        createSurveillance(listingId, new Date(createdReport.getStartDate().getTime() + (24*60*60*1000)));
        QuarterlyReportExclusionDTO exclusion = reportManager.createQuarterlyReportExclusion(createdReport, listingId, reason);
        assertNotNull(exclusion);
        assertNotNull(exclusion.getId());
        QuarterlyReportExclusionDTO updatedExclusion =
                reportManager.updateQuarterlyReportExclusion(createdReport, listingId, changedReason);
        assertNotNull(updatedExclusion);
        assertNotNull(updatedExclusion.getId());
        assertTrue(updatedExclusion.getId() > 0);
        assertEquals(listingId, updatedExclusion.getListingId());
        assertEquals(changedReason, updatedExclusion.getReason());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityRetrievalException.class)
    @Rollback(true)
    @Transactional
    public void deleteReport() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO report = createReport();
        reportManager.deleteQuarterlyReport(report.getId());
        reportManager.getQuarterlyReport(report.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void deleteExclusion() throws EntityCreationException,
        InvalidArgumentsException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        Long listingId = 1L;
        String reason = "test";
        QuarterlyReportDTO createdReport = createReport();
        //need a relevant surveillance
        createSurveillance(listingId, new Date(createdReport.getStartDate().getTime() + (24*60*60*1000)));
        QuarterlyReportExclusionDTO exclusion = reportManager.createQuarterlyReportExclusion(createdReport, listingId, reason);
        assertNotNull(exclusion);
        assertNotNull(exclusion.getId());
        reportManager.deleteQuarterlyReportExclusion(createdReport.getId(), listingId);
        List<QuarterlyReportRelevantListingDTO> relevantListings =
                reportManager.getRelevantListings(createdReport);
        assertNotNull(relevantListings);
        assertTrue(relevantListings.size() > 0);
        for (QuarterlyReportRelevantListingDTO relevantListing : relevantListings) {
            assertFalse(relevantListing.isExcluded());
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getById() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO report = createReport();
        QuarterlyReportDTO fetchedReport = reportManager.getQuarterlyReport(report.getId());
        assertNotNull(fetchedReport);
        assertEquals(report.getId(), fetchedReport.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = EntityRetrievalException.class)
    @Transactional
    public void getByIdDoesNotExist() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        reportManager.getQuarterlyReport(-100L);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getAsAcbHasReport() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        QuarterlyReportDTO report = createReport();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<QuarterlyReportDTO> acbReports = reportManager.getQuarterlyReports();
        assertNotNull(acbReports);
        assertEquals(1, acbReports.size());
        assertEquals(report.getId(), acbReports.get(0).getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void getAsAcbHasNoReports() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        List<QuarterlyReportDTO> acbReports = reportManager.getQuarterlyReports();
        assertNotNull(acbReports);
        assertEquals(0, acbReports.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Rollback(true)
    @Transactional
    public void writeQuarterlyReportAsExcelWorkbook()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-1L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. ");
        toCreate.setReactiveSummary("test reactive element summary");
        toCreate.setPrioritizedElementSummary("test prioritized element summary");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary");
        QuarterlyReportDTO created = reportManager.createQuarterlyReport(toCreate);

        QuarterlyReportDTO fetchedReport = reportManager.getQuarterlyReport(created.getId());
        Workbook workbook = reportBuilder.buildXlsx(fetchedReport);
        assertNotNull(workbook);

        //uncomment to write report
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("quarterly.xlsx");
            workbook.write(outputStream);
        } catch(final Exception ex) {
            fail(ex.getMessage());
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test(expected = AccessDeniedException.class)
    @Rollback(true)
    @Transactional
    public void writeQuarterlyReportAsExcelWorkbook_AcbNotAllowed()
            throws EntityRetrievalException, EntityCreationException, InvalidArgumentsException,
            UserRetrievalException, IOException {
        SecurityContextHolder.getContext().setAuthentication(acbUser);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName("Q1");
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(2019);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(-4L);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary("In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. In order to meet its obligation to conduct reactive surveillance, the ONC-ACB undertook the following activities and implemented the following measures to ensure that it was able to systematically obtain, synthesize and act on all facts and circumstances that would cause a reasonable person to question the ongoing compliance of any certified Complete EHR or certified Health IT Module. ");
        toCreate.setReactiveSummary("test reactive element summary");
        toCreate.setPrioritizedElementSummary("test prioritized element summary");
        toCreate.setTransparencyDisclosureSummary("test transparency and disclosure summary");
        QuarterlyReportDTO created = reportManager.createQuarterlyReport(toCreate);

        reportManager.exportQuarterlyReportAsBackgroundJob(created.getId());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private QuarterlyReportDTO createReport() throws EntityCreationException, EntityRetrievalException {
        return createReport(-1L, 2019, 1L);
    }

    private QuarterlyReportDTO createReport(final Long acbId, final Integer year, final Long quarterId) throws EntityCreationException, EntityRetrievalException {
        QuarterDTO quarter = quarterDao.getById(quarterId);
        String activitiesOutcomesSummary = "summary";
        String prioritizedElementSummary = "test";
        String reactiveSummary = "test";
        String transparencyDisclosureSummary = "test";
        QuarterlyReportDTO toCreate = new QuarterlyReportDTO();
        toCreate.setYear(year);
        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setId(acbId);
        toCreate.setAcb(acb);
        toCreate.setQuarter(quarter);
        toCreate.setActivitiesOutcomesSummary(activitiesOutcomesSummary);
        toCreate.setPrioritizedElementSummary(prioritizedElementSummary);
        toCreate.setReactiveSummary(reactiveSummary);
        toCreate.setTransparencyDisclosureSummary(transparencyDisclosureSummary);
        QuarterlyReportDTO created = quarterlyReportDao.create(toCreate);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals(activitiesOutcomesSummary, created.getActivitiesOutcomesSummary());
        assertEquals(prioritizedElementSummary, created.getPrioritizedElementSummary());
        assertEquals(reactiveSummary, created.getReactiveSummary());
        assertEquals(transparencyDisclosureSummary, created.getTransparencyDisclosureSummary());
        assertEquals(year, created.getYear());
        assertNotNull(created.getAcb());
        assertNotNull(created.getAcb().getId());
        assertEquals(acbId, created.getAcb().getId());
        assertNotNull(created.getQuarter());
        assertEquals(quarter.getId(), created.getQuarter().getId());
        return created;
    }

    private void createSurveillance(final Long listingId, final Date startDate) throws EntityRetrievalException {
        Surveillance surv = new Surveillance();

        CertifiedProductDTO cpDto = cpDao.getById(listingId);
        CertifiedProduct cp = new CertifiedProduct();
        cp.setId(cpDto.getId());
        cp.setChplProductNumber(cp.getChplProductNumber());
        cp.setEdition(cp.getEdition());
        surv.setCertifiedProduct(cp);
        surv.setStartDate(startDate);
        surv.setRandomizedSitesUsed(10);
        SurveillanceType type = survDao.findSurveillanceType("Randomized");
        surv.setType(type);

        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setRequirement("170.314 (a)(1)");
        SurveillanceRequirementType reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req.setType(reqType);
        SurveillanceResultType resType = survDao.findSurveillanceResultType("No Non-Conformity");
        req.setResult(resType);
        surv.getRequirements().add(req);

        SurveillanceRequirement req2 = new SurveillanceRequirement();
        req2.setRequirement("170.314 (a)(2)");
        reqType = survDao.findSurveillanceRequirementType("Certified Capability");
        req2.setType(reqType);
        resType = survDao.findSurveillanceResultType("Non-Conformity");
        req2.setResult(resType);
        surv.getRequirements().add(req2);

        SurveillanceNonconformity nc = new SurveillanceNonconformity();
        nc.setCapApprovalDate(new Date());
        nc.setCapMustCompleteDate(new Date());
        nc.setCapStartDate(new Date());
        nc.setDateOfDetermination(new Date());
        nc.setDeveloperExplanation("Something");
        nc.setFindings("Findings!");
        nc.setSitesPassed(2);
        nc.setNonconformityType("170.314 (a)(2)");
        nc.setSummary("summary");
        nc.setTotalSites(5);
        SurveillanceNonconformityStatus ncStatus = survDao.findSurveillanceNonconformityStatusType("Open");
        nc.setStatus(ncStatus);
        req2.getNonconformities().add(nc);

        Long insertedId;
        try {
            insertedId = survManager.createSurveillance(-1L, surv);
            assertNotNull(insertedId);
            Surveillance got = survManager.getById(insertedId);
            assertNotNull(got);
            assertNotNull(got.getCertifiedProduct());
            assertEquals(cpDto.getId(), got.getCertifiedProduct().getId());
            assertEquals(cpDto.getChplProductNumber(), got.getCertifiedProduct().getChplProductNumber());
            assertEquals(surv.getRandomizedSitesUsed(), got.getRandomizedSitesUsed());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
    }
}
