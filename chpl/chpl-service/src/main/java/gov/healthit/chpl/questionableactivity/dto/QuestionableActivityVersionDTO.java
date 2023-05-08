package gov.healthit.chpl.questionableactivity.dto;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityVersionEntity;
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
public class QuestionableActivityVersionDTO extends QuestionableActivityDTO {
    private Long versionId;
    private ProductVersionDTO version;

    public QuestionableActivityVersionDTO(QuestionableActivityVersionEntity entity) {
        super(entity);
        this.versionId = entity.getVersionId();
        if (entity.getVersion() != null) {
            this.version = new ProductVersionDTO(entity.getVersion());
        }
    }

    public Class<?> getActivityObjectClass() {
        return ProductVersionDTO.class;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public ProductVersionDTO getVersion() {
        return version;
    }

    public void setVersion(ProductVersionDTO version) {
        this.version = version;
    }
}
