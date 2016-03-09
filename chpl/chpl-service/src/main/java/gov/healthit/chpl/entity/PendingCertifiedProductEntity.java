package gov.healthit.chpl.entity;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;


/** 
 * Object mapping for hibernate-handled table: certified_product.
 * A product that has been Certified
 *
 * @author autogenerated / cwatson
 */

@Entity
@Table(name = "pending_certified_product")
public class PendingCertifiedProductEntity {
	
	/**
	 * fields we generate mostly from spreadsheet values
	 */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column( name = "pending_certified_product_id", nullable = false  )
	private Long id;
    
    @Column(name = "practice_type_id")
    private Long practiceTypeId;
    
    @Column(name = "vendor_id")
    private Long developerId;
    
    @Column(name = "vendor_contact_id")
    private Long developerContactId;
    
    @Basic( optional = true )
	@OneToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_address_id", unique=true, nullable = true)
	private AddressEntity developerAddress;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_version_id")
    private Long productVersionId;
    
    @Column(name = "certification_edition_id")
    private Long certificationEditionId;
    
    @Column(name = "certification_body_id")
    private Long certificationBodyId;
    
    @Column(name = "product_classification_id")
    private Long productClassificationId;
    
    @Column(name = "testing_lab_id")
    private Long testingLabId;
    
	@Basic(optional = false) 
	@Column( name = "certification_status_id" , nullable = false)
	private Long status;
	
    /**
    * fields directly from the spreadsheet
    **/
    @Column(name = "unique_id")
    private String uniqueId;
    
    @Column(name = "record_status")
    private String recordStatus;
    
    @Column(name = "practice_type")
    private String practiceType;
    
    @Column(name = "testing_lab_name")
    private String testingLabName;
    
    @Column(name="vendor_name")
    private String developerName;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "product_version")
    private String productVersion;
    
    @Column(name = "certification_edition")
    private String certificationEdition;
    
    @Column(name = "acb_certification_id")
    private String acbCertificationId;
    
    @Column(name = "certification_body_name")
    private String certificationBodyName;
    
    @Column(name = "product_classification_name")
    private String productClassificationName;

    @Column(name = "certification_date")
    private Date certificationDate;
    
    @Column(name = "vendor_street_address")
    private String developerStreetAddress;
    
    @Column(name = "vendor_transparency_attestation")
	@Type(type = "gov.healthit.chpl.entity.PostgresEnumType" , parameters ={@org.hibernate.annotations.Parameter(name = "enumClassName",value = "gov.healthit.chpl.entity.AttestationType")} )
	private AttestationType transparencyAttestation;
    
    @Column(name = "vendor_transparency_attestation_url")
    private String transparencyAttestationUrl;
    
    @Column(name = "vendor_city")
    private String developerCity;
    
    @Column(name = "vendor_state")
    private String developerState;
    
    @Column(name = "vendor_zip_code")
    private String developerZipCode;
    
    @Column(name = "vendor_website")
    private String developerWebsite;
    
    @Column(name = "vendor_email")
    private String developerEmail;

    @Column(name = "vendor_contact_name")
    private String developerContactName;
    
    @Column(name = "vendor_phone")
    private String developerPhoneNumber;
    
    @Column(name = "test_report_url")
    private String reportFileLocation;
    
    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

	@Column(name = "ics")
	private Boolean ics;
	
	@Column(name = "terms_of_use_url")
	private String termsOfUse;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertifiedProductId")
	@Basic( optional = false )
	@Column( name = "pending_certified_product_id", nullable = false  )
	private Set<PendingCertificationResultEntity> certificationCriterion;
    
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertifiedProductId")
	@Basic( optional = false )
	@Column( name = "pending_certified_product_id", nullable = false  )
	private Set<PendingCqmCriterionEntity> cqmCriterion;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="pendingCertifiedProductId")
	@Basic( optional = false )
	@Column( name = "pending_certified_product_id", nullable = false  )
	private Set<PendingCertifiedProductQmsStandardEntity> qmsStandards;
	
	@Transient
	private boolean hasQms;
	
	@Basic( optional = false )
	@Column( name = "last_modified_date", nullable = false  )
	private Date lastModifiedDate;
	
	@Basic( optional = false )
	@Column( name = "last_modified_user", nullable = false  )
	private Long lastModifiedUser;
	
	@Basic( optional = false )
	@Column( name = "creation_date", nullable = false  )
	private Date creationDate;
	
	@Basic( optional = false )
	@Column(name = "deleted", nullable = false  )
	private Boolean deleted;
	
	public PendingCertifiedProductEntity() {
		certificationCriterion = new HashSet<PendingCertificationResultEntity>();
		cqmCriterion = new HashSet<PendingCqmCriterionEntity>();
		qmsStandards = new HashSet<PendingCertifiedProductQmsStandardEntity>();
	} 

	public PendingCertifiedProductEntity(Long id) {
		this.id = id;
		certificationCriterion = new HashSet<PendingCertificationResultEntity>();
		cqmCriterion = new HashSet<PendingCqmCriterionEntity>();
		qmsStandards = new HashSet<PendingCertifiedProductQmsStandardEntity>();
	}
	
	@Transient
	public Class<?> getClassType() {
		return PendingCertifiedProductEntity.class;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPracticeTypeId() {
		return practiceTypeId;
	}

	public void setPracticeTypeId(Long practiceTypeId) {
		this.practiceTypeId = practiceTypeId;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getProductVersionId() {
		return productVersionId;
	}

	public void setProductVersionId(Long productVersionId) {
		this.productVersionId = productVersionId;
	}

	public Long getCertificationEditionId() {
		return certificationEditionId;
	}

	public void setCertificationEditionId(Long certificationEditionId) {
		this.certificationEditionId = certificationEditionId;
	}

	public Long getCertificationBodyId() {
		return certificationBodyId;
	}

	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}

	public Long getProductClassificationId() {
		return productClassificationId;
	}

	public void setProductClassificationId(Long productClassificationId) {
		this.productClassificationId = productClassificationId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}

	public String getPracticeType() {
		return practiceType;
	}

	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public String getCertificationEdition() {
		return certificationEdition;
	}

	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}

	public String getAcbCertificationId() {
		return acbCertificationId;
	}

	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}

	public String getCertificationBodyName() {
		return certificationBodyName;
	}

	public void setCertificationBodyName(String certificationBodyName) {
		this.certificationBodyName = certificationBodyName;
	}

	public String getProductClassificationName() {
		return productClassificationName;
	}

	public void setProductClassificationName(String productClassificationName) {
		this.productClassificationName = productClassificationName;
	}

	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}

	public String getDeveloperStreetAddress() {
		return developerStreetAddress;
	}

	public void setDeveloperStreetAddress(String developerStreetAddress) {
		this.developerStreetAddress = developerStreetAddress;
	}

	public String getDeveloperCity() {
		return developerCity;
	}

	public void setDeveloperCity(String developerCity) {
		this.developerCity = developerCity;
	}

	public String getDeveloperState() {
		return developerState;
	}

	public void setDeveloperState(String developerState) {
		this.developerState = developerState;
	}

	public String getDeveloperZipCode() {
		return developerZipCode;
	}

	public void setDeveloperZipCode(String developerZipCode) {
		this.developerZipCode = developerZipCode;
	}

	public String getDeveloperWebsite() {
		return developerWebsite;
	}

	public void setDeveloperWebsite(String developerWebsite) {
		this.developerWebsite = developerWebsite;
	}

	public String getDeveloperEmail() {
		return developerEmail;
	}

	public void setDeveloperEmail(String developerEmail) {
		this.developerEmail = developerEmail;
	}

	public String getReportFileLocation() {
		return reportFileLocation;
	}

	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}

	public Set<PendingCertificationResultEntity> getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(Set<PendingCertificationResultEntity> certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public Set<PendingCqmCriterionEntity> getCqmCriterion() {
		return cqmCriterion;
	}

	public void setCqmCriterion(Set<PendingCqmCriterionEntity> cqmCriterion) {
		this.cqmCriterion = cqmCriterion;
	}

	public AddressEntity getDeveloperAddress() {
		return developerAddress;
	}

	public void setDeveloperAddress(AddressEntity developerAddress) {
		this.developerAddress = developerAddress;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public Boolean getIcs() {
		return ics;
	}

	public void setIcs(Boolean ics) {
		this.ics = ics;
	}

	public Long getTestingLabId() {
		return testingLabId;
	}

	public void setTestingLabId(Long testingLabId) {
		this.testingLabId = testingLabId;
	}

	public String getTestingLabName() {
		return testingLabName;
	}

	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}

	public String getDeveloperContactName() {
		return developerContactName;
	}

	public void setDeveloperContactName(String developerContactName) {
		this.developerContactName = developerContactName;
	}

	public String getDeveloperPhoneNumber() {
		return developerPhoneNumber;
	}

	public void setDeveloperPhoneNumber(String developerPhoneNumber) {
		this.developerPhoneNumber = developerPhoneNumber;
	}

	public String getSedReportFileLocation() {
		return sedReportFileLocation;
	}

	public void setSedReportFileLocation(String sedReportFileLocation) {
		this.sedReportFileLocation = sedReportFileLocation;
	}

	public Set<PendingCertifiedProductQmsStandardEntity> getQmsStandards() {
		return qmsStandards;
	}

	public void setQmsStandards(Set<PendingCertifiedProductQmsStandardEntity> qmsStandards) {
		this.qmsStandards = qmsStandards;
	}

	public boolean isHasQms() {
		return hasQms;
	}

	public void setHasQms(boolean hasQms) {
		this.hasQms = hasQms;
	}

	public String getTermsOfUse() {
		return termsOfUse;
	}

	public void setTermsOfUse(String termsOfUse) {
		this.termsOfUse = termsOfUse;
	}

	public AttestationType getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(AttestationType transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}

	public Long getDeveloperContactId() {
		return developerContactId;
	}

	public void setDeveloperContactId(Long developerContactId) {
		this.developerContactId = developerContactId;
	}

	public String getTransparencyAttestationUrl() {
		return transparencyAttestationUrl;
	}

	public void setTransparencyAttestationUrl(String transparencyAttestationUrl) {
		this.transparencyAttestationUrl = transparencyAttestationUrl;
	}
}
