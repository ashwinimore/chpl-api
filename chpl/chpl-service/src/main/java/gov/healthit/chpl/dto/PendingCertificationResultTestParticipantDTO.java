package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.PendingCertificationResultTestParticipantEntity;

public class PendingCertificationResultTestParticipantDTO {
	private Long id;
	private Long pendingCertificationResultId;
	private String uniqueId;
	private String gender;
	private Integer age;
	private Long educationTypeId;
	private String occupation;
	private Integer professionalExperienceMonths;
	private Integer computerExperienceMonths;
	private Integer productExperienceMonths;
	private String assistiveTechnologyNeeds;
	
	public PendingCertificationResultTestParticipantDTO() {}
	
	public PendingCertificationResultTestParticipantDTO(PendingCertificationResultTestParticipantEntity entity) {
		this.setId(entity.getId());
		this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
		this.uniqueId = entity.getUniqueId();
		this.age = entity.getAge();
		this.gender = entity.getGender();
		this.educationTypeId = entity.getEducationTypeId();
		this.occupation = entity.getOccupation();
		this.professionalExperienceMonths = entity.getProfessionalExperienceMonths();
		this.computerExperienceMonths = entity.getComputerExperienceMonths();
		this.productExperienceMonths = entity.getProductExperienceMonths();
		this.assistiveTechnologyNeeds = entity.getAssistiveTechnologyNeeds();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getPendingCertificationResultId() {
		return pendingCertificationResultId;
	}

	public void setPendingCertificationResultId(Long pendingCertificationResultId) {
		this.pendingCertificationResultId = pendingCertificationResultId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Long getEducationTypeId() {
		return educationTypeId;
	}

	public void setEducationTypeId(Long educationTypeId) {
		this.educationTypeId = educationTypeId;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public Integer getProfessionalExperienceMonths() {
		return professionalExperienceMonths;
	}

	public void setProfessionalExperienceMonths(Integer professionalExperienceMonths) {
		this.professionalExperienceMonths = professionalExperienceMonths;
	}

	public Integer getComputerExperienceMonths() {
		return computerExperienceMonths;
	}

	public void setComputerExperienceMonths(Integer computerExperienceMonths) {
		this.computerExperienceMonths = computerExperienceMonths;
	}

	public Integer getProductExperienceMonths() {
		return productExperienceMonths;
	}

	public void setProductExperienceMonths(Integer productExperienceMonths) {
		this.productExperienceMonths = productExperienceMonths;
	}

	public String getAssistiveTechnologyNeeds() {
		return assistiveTechnologyNeeds;
	}

	public void setAssistiveTechnologyNeeds(String assistiveTechnologyNeeds) {
		this.assistiveTechnologyNeeds = assistiveTechnologyNeeds;
	}
}
