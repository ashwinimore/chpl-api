package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ProductVersionManager {
    ProductVersionDTO getById(Long id) throws EntityRetrievalException;
    ProductVersionDTO getById(Long id, boolean allowDeleted) throws EntityRetrievalException;

    List<ProductVersionDTO> getAll();

    List<ProductVersionDTO> getByProduct(Long productId);

    List<ProductVersionDTO> getByProducts(List<Long> productIds);

    ProductVersionDTO create(ProductVersionDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    ProductVersionDTO update(ProductVersionDTO dto)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    ProductVersionDTO merge(List<Long> versionIdsToMerge, ProductVersionDTO toCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
}
