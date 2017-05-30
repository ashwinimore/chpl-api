package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.listing.ListingToListingMapEntity;

@Repository(value="listingGraphDao")
public class ListingGraphDAOImpl extends BaseDAOImpl implements ListingGraphDAO {
	
	public ListingToListingMapDTO createListingMap(ListingToListingMapDTO toCreate) {
		ListingToListingMapEntity entity = new ListingToListingMapEntity();
		entity.setChildId(toCreate.getChildId());
		entity.setParentId(toCreate.getParentId());
		entity.setDeleted(false);
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entityManager.persist(entity);
		entityManager.flush();
		
		return new ListingToListingMapDTO(entity);
	}
	
	public void deleteListingMap(ListingToListingMapDTO toDelete) {
		ListingToListingMapEntity entity = entityManager.find(ListingToListingMapEntity.class, toDelete.getId());
		if(entity != null) {
			entity.setDeleted(true);
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entityManager.merge(entity);
			entityManager.flush();
		}
	}
	
	/**
	 * Get the maximum ICS value from the proposed parent nodes
	 * @param listingIds proposed parent nodes
	 * @return largest ICS value
	 */
	public Integer getLargestIcs(List<Long> listingIds) {
		Query query = entityManager.createQuery( "SELECT MAX(listing.icsCode) "
				+ "FROM CertifiedProductEntity listing "
				+ "WHERE listing.id IN (:listingIds)", String.class );
		query.setParameter("listingIds", listingIds);
		String result = (String)query.getSingleResult();
		return new Integer(result);
	}
	
	/**
	 * Find the first-level parent nodes of a specific listing
	 * @param listingId Listing to find the parents of
	 * @return The first-level parents (can have multiple)
	 */
	public List<CertifiedProductDetailsDTO> getParents(Long listingId) {
		Query query = entityManager.createQuery( "SELECT listingMap.parent "
				+ "FROM ListingToListingMapEntity listingMap "
				+ "WHERE listingMap.childId = :childId", CertifiedProductDetailsEntity.class );
		query.setParameter("childId", listingId);
		List<CertifiedProductDetailsEntity> parentEntities = query.getResultList();
		
		List<CertifiedProductDetailsDTO> result = new ArrayList<CertifiedProductDetailsDTO>();
		for(CertifiedProductDetailsEntity parentEntity : parentEntities) {
			result.add(new CertifiedProductDetailsDTO(parentEntity));
		}
		return result;
	}
	
	/**
	 * Find the first-level child nodes of a specific listing
	 * @param listingId Listing to find the children of
	 * @return The first-level children (can have multiple)
	 */
	public List<CertifiedProductDetailsDTO> getChildren(Long listingId) {
		Query query = entityManager.createQuery( "SELECT listingMap.child "
				+ "FROM ListingToListingMapEntity listingMap "
				+ "WHERE listingMap.parentId = :parentId", CertifiedProductDetailsEntity.class );
		query.setParameter("parentId", listingId);
		List<CertifiedProductDetailsEntity> childEntities = query.getResultList();
		
		List<CertifiedProductDetailsDTO> result = new ArrayList<CertifiedProductDetailsDTO>();
		for(CertifiedProductDetailsEntity childEntity : childEntities) {
			result.add(new CertifiedProductDetailsDTO(childEntity));
		}
		return result;
	}
}
