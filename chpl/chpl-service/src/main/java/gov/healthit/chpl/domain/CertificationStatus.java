package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class CertificationStatus implements Serializable{
	
	private static final long serialVersionUID = 818896721132619130L;

	private Long id;
	
	private String name;
	
	public CertificationStatus(Map<String, Object> certStatus){
		this.id = Long.valueOf(certStatus.get("id").toString());
		this.name = certStatus.get("name").toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

}