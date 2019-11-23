package queens.imr.db;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import queens.imr.controller.patientFetchController;
import queens.imr.entitiy.Notes;
import queens.imr.entitiy.Patient;


import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import queens.imr.pojo.ReportTemplates;

@Component
@Service
public class Store
{
	@Autowired
	public  ReportTemplates  reportTemplates;
	
	static final Logger LOGGER = LoggerFactory.getLogger(Store.class);	
	@Value("${jrxml.fileName}")
	private String fileName;
	private JdbcTemplate db;
	public void setTemplate(JdbcTemplate db) 
	{
	    this.db = db;
	}
	public JSONObject getAllPatient() 
	{
		List<Patient> outputDetailsLst = findAllPatient();
		JSONObject jsonobj =  new JSONObject();
		jsonobj.put("OutputDetailsLst", outputDetailsLst);
		return jsonobj;
	}
	private List<Patient> findAllPatient() 
	{
		String getRequestQry = "select * from queens.patient_details";
		return db.query(getRequestQry, new PatientMapper());	
		}	
	private static final class PatientMapper implements RowMapper<Patient> {
        public Patient mapRow(ResultSet rs, int rowNum) throws SQLException         {
        	LOGGER.info(rs+"rs");
        	Patient patient = new Patient();
        	patient.setPatientId(rs.getInt("patient_id"));
        	patient.setName(rs.getString("name"));
        	patient.setMobile(rs.getString("mobile"));
        	patient.setPatient_details(rs.getString("patient_details"));
        	patient.setBedname(rs.getString("bedname"));
        	patient.setDoctorname(rs.getString("doctorname"));
        	patient.setImage(rs.getString("image")); 
        	patient.setAdmission(rs.getString("admission"));
            return patient;
       
        }
    }
	public JSONObject getOnePatient(String patientName, String mobileNumber) 
	{
		List<Patient> outputDetailsLst = findOnePatient(patientName,mobileNumber);
		JSONObject jsonobj =  new JSONObject();
		jsonobj.put("OutputDetailsLst", outputDetailsLst);
		return jsonobj;
	}
	private List<Patient> findOnePatient(String patientName, String mobileNumber) 
	{
		String getRequestQry = "select * from queens.patient_details where name =? and mobile =?";
		return db.query(getRequestQry, new PatientMapper(),patientName,mobileNumber);	
		}	
	public Patient getOnlyPatient(String patientName, String mobileNumber) 
	{
		Patient outputDetailsLst =null;
		try 
		{
		 outputDetailsLst = findOnlyPatient(patientName,mobileNumber);
		}
		catch(Exception e)
		{
			LOGGER.error("Exception in getByRequestIdSingleObj");
			e.printStackTrace();
		}
		return outputDetailsLst;
	}
	private Patient findOnlyPatient(String patientName,String mobileNumber) 
	{

		String getRequestQry = "select * from queens.patient_details where name =? and mobile =?";
		LOGGER.info(getRequestQry);
		return db.queryForObject(getRequestQry, new PatientMapper(),patientName,mobileNumber);
		
		
	}
	public void createPatient(String name, String mobile, JSONObject jsonObjectAdmit, String addmissionType) 
	{
		LOGGER.info("NOW CREATING THE NEW PATIENT");
		String INPUT_DETAILS_INSERT ="insert into queens.patient_details(name,mobile,patient_details,bedname,admission,doctorname) "
				+ "SELECT ?,?,?::json,?,?,doctorname from  queens.doctor_details ORDER BY RANDOM() LIMIT 1";
		int status = db.update(INPUT_DETAILS_INSERT, new Object[] {name,mobile ,jsonObjectAdmit.toString(),mobile,addmissionType});
		LOGGER.info("Status---"+ status);
	}
	public void createDocProg(String mobile, Object objProgress) 
	{
		LOGGER.info("NOW CREATING THE NEW PATIENT");
		String INPUT_DETAILS_INSERT ="insert into queens.progress_notes(patient_id,nurse_pnotes,doc_pnotes) "
				+ "values(?,?::jsonb,?::jsonb)ON CONFLICT (patient_id) " + 
				"DO NOTHING";
		int status = db.update(INPUT_DETAILS_INSERT, new Object[] {mobile ,objProgress.toString(),objProgress.toString()});
		LOGGER.info("Status---"+ status);
		
	}
	public int updatePatient(String name, String mobile, JSONObject jsonObjectAdmit, String addmissionType) 
	{
		LOGGER.info("NOW Updating THE Exsisting PATIENT");
		String INPUT_DETAILS_UPDATE ="update queens.patient_details set name=?,mobile=?,patient_details=?::json,bedname=?,admission=? where mobile=?";
		int inputDbStatus = db.update(INPUT_DETAILS_UPDATE, new Object[] {name,mobile,jsonObjectAdmit.toString(),mobile,addmissionType,mobile});
		return inputDbStatus;
	}
	public JasperPrint exportFile(JSONObject responseObj, String reportxml) 
	{
		LOGGER.info("\nFetching format and parameters for the report......");	
		 try
		 {
			 LOGGER.info("\nCreated connection to the db for the report......");			 
			 //path of jrxml file
			 String path=getJrxml(fileName,reportxml);
			 LOGGER.info("\nPath of the jrxml file-----> "+path);
			 if(path!=null)
			 {
				 LOGGER.info("\nPath of the jrxml file-----> "+path);				 
				 LOGGER.info("\n\n\nCompiling report ... ");
				 String status = getTechData(responseObj); 
				 LOGGER.info("\n\n\n setData-----> "+status);
				 if(status.equalsIgnoreCase("Success"))
				 {
					 LOGGER.info("\nFetched all report parameters......");					 
					 InputStream input = new FileInputStream(new File(path));
				     JasperDesign jasperDesign = JRXmlLoader.load(input);
					 JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
					 
					 JasperPrint print = JasperFillManager.fillReport(jasperReport, reportTemplates.getReportTemplateOne(), db.getDataSource().getConnection());
					 LOGGER.info("\nFilled report with parameters fetched......"+print.getName());	
					return print;
				 }
				 else
				 {
					 LOGGER.error("Failed to fetch report parameters as the status of getting data failed!!!");
					 return null;
				 }				 
			 }
			 else
			 {
				 LOGGER.error("Failed to fetch jrxml path.");
				 return null;				 
			 }
		 }
		 catch(JRException | SQLException |IOException ex)
		  {
				LOGGER.error("\nData could not be fetched!!!As an exception has occured due to compiling the report!!!");
				ex.printStackTrace();
				return null;	
		  }		  		  
	}
	private String getTechData(JSONObject responseObj) 
	{
		Map<String, Object> reportTemplateOne = reportTemplates.getReportTemplateOne();
		LOGGER.info("responseObj"+responseObj);
		JSONArray OutputDetailsLst = (JSONArray) responseObj.get("OutputDetailsLst");
		LOGGER.info("OutputDetailsLst- "+OutputDetailsLst);
		for(Object i : OutputDetailsLst )
		{
			JSONObject dataDetails = (JSONObject) i;
			LOGGER.info("dataDetails"+dataDetails);
			reportTemplateOne.put("name",dataDetails.has("name")? dataDetails.get("name"):"NA");
			JSONObject patient_details = new JSONObject(StringEscapeUtils.unescapeJava(dataDetails.get("patient_details").toString()));
			LOGGER.info("patient_details "+patient_details);
			reportTemplateOne.put("HR",patient_details.has("HR")? patient_details.get("HR"):"NA");
			reportTemplateOne.put("diasbp",patient_details.has("diasbp")? patient_details.get("diasbp"):"NA");
			reportTemplateOne.put("Pulse",patient_details.has("Pulse")? patient_details.get("Pulse"):"NA");
			reportTemplateOne.put("sysbp",patient_details.has("sysbp")? patient_details.get("sysbp"):"NA");
			JSONObject patientDetails = (JSONObject) patient_details.get("patientDetails");
			LOGGER.info("patientDetails"+patientDetails);
			reportTemplateOne.put("complaints",patientDetails.has("complaints")? patientDetails.get("complaints"):"NA");
			reportTemplateOne.put("ci",patientDetails.has("Criticality_Index")? patientDetails.get("Criticality_Index"):"NA");
			reportTemplateOne.put("age",patientDetails.has("age")? patientDetails.get("age"):"NA");
			reportTemplateOne.put("gender",patientDetails.has("gender")? patientDetails.get("gender"):"NA");
			reportTemplates.setReportTemplateOne(reportTemplateOne);

		}
         String statusForReceivedData = "Success";
         return statusForReceivedData;
	}
	public String getJrxml(String fileName, String xmlPath) throws IOException {
		// to load application's properties, we use this class
		LOGGER.info("Loading jrxml urls for processing the report template ... ");
		//Properties mainProperties = new Properties();
		//FileInputStream file = null;
		if (StringUtils.isEmpty(fileName)) {
			return null;
		}

		String path = xmlPath + fileName.trim();
		LOGGER.info("Path--->"+path);
		try {
			LOGGER.info("Path--->"+path);
			LOGGER.info("Report file loaded successfully");
			return path;
		} catch (Exception ex) {
			LOGGER.error("Exception while loading file : ");
			return null;
		}
	}
	public ResponseEntity<String> pdfGenerator(JasperPrint staticJasperPrint, JSONObject responseObj) 
	{
		JSONObject response=new JSONObject();
		try
		{
			LOGGER.info("\nPDF Generation Started.");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JasperExportManager.exportReportToPdfStream(staticJasperPrint, bos);
			byte[] bytesData = bos.toByteArray();
			LOGGER.info("\nStarted fetching report path.");
			String base64EncodedPdf = Base64.getEncoder().encodeToString(bytesData);
			LOGGER.info("\nBase64encoded pdf generated.");	
			LOGGER.info("\nPDF Report Generated.");										
											
				response.put("file_content", base64EncodedPdf);
				return  new ResponseEntity<String>(response.toString(), HttpStatus.OK);	
		}
		catch(NullPointerException | JRException ex )
		{
			ex.printStackTrace();
			LOGGER.error("Report couldnot be exported by the JasperExportManager");			
			return  new ResponseEntity<String>("Report couldnot be exported by the JasperExportManager", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		public List<Notes> findAllNotes() 
		{

			String getRequestQry = "select * from queens.progress_notes";
			LOGGER.info(getRequestQry);
			return db.query(getRequestQry, new NotesMapper());			
			
		}
		

		private static final class NotesMapper implements RowMapper<Notes> {
		    public Notes mapRow(ResultSet rs, int rowNum) throws SQLException        
		    {	        	
		    	
		    LOGGER.info(rs+"rs");
			Notes notes = new Notes();
			notes.setPatient_id(rs.getString("patient_id"));
			notes.setDoc_notes(rs.getString("doc_pnotes"));
			notes.setNurses_notes(rs.getString("nurse_pnotes"));
		    return notes;

		}
		}
		
}
