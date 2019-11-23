package queens.imr.entitiy;

public class Notes
{
	private String patient_id;
	private String nurses_notes;
	private String doc_notes;
	public String getPatient_id() {
		return patient_id;
	}
	public void setPatient_id(String patient_id) {
		this.patient_id = patient_id;
	}
	public String getNurses_notes() {
		return nurses_notes;
	}
	public void setNurses_notes(String nurses_notes) {
		this.nurses_notes = nurses_notes;
	}
	public String getDoc_notes() {
		return doc_notes;
	}
	public void setDoc_notes(String doc_notes) {
		this.doc_notes = doc_notes;
	}
	@Override
	public String toString() {
		return "Notes [patient_id=" + patient_id + ", nurses_notes=" + nurses_notes + ", doc_notes=" + doc_notes + "]";
	}
	
	

}
