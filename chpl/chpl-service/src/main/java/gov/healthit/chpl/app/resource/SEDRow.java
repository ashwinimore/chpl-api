package gov.healthit.chpl.app.resource;

import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;

public class SEDRow {
	
	private CertifiedProductDetailsDTO details;
	private TestTask testTask;
	private String criteria;
	private TestParticipant testParticipant;
	
	public CertifiedProductDetailsDTO getDetails() {
		return details;
	}
	public void setDetails(CertifiedProductDetailsDTO details) {
		this.details = details;
	}
	public TestTask getTestTask() {
		return testTask;
	}
	public void setTestTask(TestTask testTask) {
		this.testTask = testTask;
	}
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	public TestParticipant getTestParticipant() {
		return testParticipant;
	}
	public void setTestParticipant(TestParticipant testParticipant) {
		this.testParticipant = testParticipant;
	}
}