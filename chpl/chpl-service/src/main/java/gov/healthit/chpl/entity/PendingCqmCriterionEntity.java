package gov.healthit.chpl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="pending_cqm_criterion")
public class PendingCqmCriterionEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_cqm_critereon_idGenerator")
	@Column( name = "pending_cqm_criterion_id", nullable = false  )
	@SequenceGenerator(name = "pending_cqm_critereon_idGenerator", 
		sequenceName = "pending_cqm_criterion_pending_cqm_criterion_id_seq")
	private Long id;
	
	@Column(name="cqm_criterion_id")
	private Long cqmCriterionId;
	
	@Column(name="pending_certified_product_id")
	private Long pendingCertifiedProductId;;

	@Column(name = "meets_criteria")
	private Boolean meetsCriteria;

	@Transient private String cqmNumber;
	@Transient private String cmsId;
	@Transient private String title;
	@Transient private String nqfNumber;
	@Transient private String version;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}

	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public Boolean getMeetsCriteria() {
		return meetsCriteria;
	}

	public void setMeetsCriteria(Boolean meetsCriteria) {
		this.meetsCriteria = meetsCriteria;
	}

	public String getCqmNumber() {
		return cqmNumber;
	}

	public void setCqmNumber(String cqmNumber) {
		this.cqmNumber = cqmNumber;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNqfNumber() {
		return nqfNumber;
	}

	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
