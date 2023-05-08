package gov.healthit.chpl.questionableactivity.dto;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityProductEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class QuestionableActivityProductDTO extends QuestionableActivityDTO {
    private Long productId;
    private Product product;

    public QuestionableActivityProductDTO(QuestionableActivityProductEntity entity) {
        super(entity);
        this.productId = entity.getProductId();
        if (entity.getProduct() != null) {
            this.product = entity.getProduct().toDomain();
        }
    }

    public Class<?> getActivityObjectClass() {
        return Product.class;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
