package gov.healthit.chpl.compliance.surveillance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "surveillance_type")
public class SurveillanceTypeEntity extends EntityAudit {
    private static final long serialVersionUID = 621711789507836881L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    public SurveillanceType buildSurveillanceType() {
        SurveillanceType st = new SurveillanceType();
        st.setId(this.getId());
        st.setName(this.getName());
        return st;
    }
}
