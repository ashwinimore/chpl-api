package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class ListingGraphDaoTest extends TestCase {

	@Autowired private ListingGraphDAO listingGraphDao;	
	private static JWTAuthenticatedUser authUser;
	
	@Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

	@BeforeClass
	public static void setUpClass() throws Exception {
		authUser = new JWTAuthenticatedUser();
		authUser.setFirstName("Admin");
		authUser.setId(-2L);
		authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
	}

	@Test
	@Transactional
	@Rollback(true)
	public void testGetLargestIcsValue() {
		List<Long> listingIds = new ArrayList<Long>();
		listingIds.add(5L);
		listingIds.add(6L);
		
		Integer largestIcs = listingGraphDao.getLargestIcs(listingIds);
		assertNotNull(largestIcs);
		assertEquals(2, largestIcs.longValue());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetLargestIcsValueWithNullIcsCodes() {
		List<Long> listingIds = new ArrayList<Long>();
		listingIds.add(5L);
		listingIds.add(17L);
		
		Integer largestIcs = listingGraphDao.getLargestIcs(listingIds);
		assertNotNull(largestIcs);
		assertEquals(1, largestIcs.longValue());
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetParentListings() {
		List<CertifiedProductDetailsDTO> parents = listingGraphDao.getParents(5L);
		assertNotNull(parents);
		assertEquals(2, parents.size());
		for(CertifiedProductDetailsDTO parent : parents) {
			switch(parent.getId().intValue()) {
			case 6:
			case 9:
				break;
			default: 
				fail("Expected 6 and 9 but found " + parent.getId().intValue());
			}
		}
	}
	
	@Test
	@Transactional
	@Rollback(true)
	public void testGetChildListings() {
		List<CertifiedProductDetailsDTO> children = listingGraphDao.getChildren(9L);
		assertNotNull(children);
		assertEquals(1, children.size());
		for(CertifiedProductDetailsDTO child : children) {
			switch(child.getId().intValue()) {
			case 5:
				break;
			default: 
				fail("Expected 5 but found " + child.getId().intValue());
			}
		}
	}
}
