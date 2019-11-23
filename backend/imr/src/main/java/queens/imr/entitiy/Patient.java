package queens.imr.entitiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Patient 
{

private int patientId;
private String admission;
private String mobile ;
private String patient_details;
private String bedname;
private String doctorname;
private String image;
private String name;
@JsonInclude(Include.NON_NULL)
public int getPatientId() {
	return patientId;
}
public void setPatientId(int patientId) {
	this.patientId = patientId;
}
@JsonInclude(Include.NON_NULL)
public String getMobile() {
	return mobile;
}
public void setMobile(String mobile) {
	this.mobile = mobile;
}
@JsonInclude(Include.NON_NULL)
public String getPatient_details() {
	return patient_details;
}
public void setPatient_details(String patient_details) {
	this.patient_details = patient_details;
}
@JsonInclude(Include.NON_NULL)
public String getBedname() {
	return bedname;
}
public void setBedname(String bedname) {
	this.bedname = bedname;
}
@JsonInclude(Include.NON_NULL)
public String getDoctorname() {
	return doctorname;
}
public void setDoctorname(String doctorname) {
	this.doctorname = doctorname;
}
@JsonInclude(Include.NON_NULL)
public String getImage() {
	return image;
}
public void setImage(String image) {
	this.image = image;
}
@JsonInclude(Include.NON_NULL)
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getAdmission() {
	return admission;
}
public void setAdmission(String admission) {
	this.admission = admission;
}

}


