package queens.imr.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import queens.imr.config.UserDAO;
import queens.imr.db.Store;
import queens.imr.entitiy.Patient;

@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "templates")
public class ReadJsonFiles 
{
	
	@Value("${configdata.path}")
	private String path;
	private JdbcTemplate dbTemplate;
	private Map<String, String> tenant = new HashMap<>();
	@Autowired
	public UserDAO userDAO;
	@Autowired
	private Store store;
	public Map<String, String> getTenant() 
	{
		return tenant;
	}
	private static final Logger LOGGER = LoggerFactory.getLogger(ReadJsonFiles.class);
	@Scheduled(fixedRate = 5000)
    public void executeTask2() 
	{
		JSONParser parser = new JSONParser();
		 
        try
        {
            File dir = new File(path);
            FileFilter fileFilter = new WildcardFileFilter("*patient.json");
            FileFilter fileFilterAdmit = new WildcardFileFilter("*admit.json");
            File[] files = dir.listFiles(fileFilter);
            File[] filesAdmit = dir.listFiles(fileFilterAdmit);
            Object patient = null;
            Object admit = null;
            for (int i = 0; i < files.length; i++) 
            {
               patient = parser.parse(new FileReader(files[i]));
               admit = parser.parse(new FileReader(filesAdmit[i]));
              String addmissionType=files[i].getName().replaceFirst("[.][^.]+$", "");
              LOGGER.info("addmissionType"+addmissionType);
            JSONObject jsonObject = new JSONObject(patient.toString());
            JSONObject jsonObjectAdmit = new JSONObject(admit.toString());
            String name = (String) jsonObject.get("name");
            String mobile = (String) jsonObject.get("mobile");
            String token = (String) jsonObject.get("token");
            String templateName = tenant.get(token);
            LOGGER.info("jsonObject --- jsonObjectAdmit"+jsonObject+jsonObjectAdmit);
            LOGGER.info("templateName"+templateName);
    		if (templateName != null && !StringUtils.isEmpty(token)) 
    		{	
                jsonObjectAdmit.put("patientDetails", jsonObject);
    			LOGGER.info("jsonObjectAdmit"+jsonObjectAdmit);
    			dbTemplate = userDAO.getTemplate(templateName);
    			if(dbTemplate ==null) 
    			{
    				throw new NullPointerException();
    			}
    			LOGGER.info("\nSetting up DB template");
    			store.setTemplate(dbTemplate);			
    			Patient responseObj = store.getOnlyPatient(name,mobile);
    			if(responseObj ==null)
    			{
    				LOGGER.info("PAtient Doesnot exists so have to create a new patient");
    				LOGGER.info("Creating the Patient");
    				store.createPatient(name,mobile,jsonObjectAdmit,addmissionType);
    				
    			}	
    			else
    			{
    				LOGGER.info("Patient Exists ...Updating the Patient");
    				store.updatePatient(name,mobile,jsonObjectAdmit,addmissionType);
    			}
    			LOGGER.info("\n--------------Queued data API completed ------------");

    		 }
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
      //  System.out.println(Thread.currentThread().getName()+" The Task2 executed at "+ new Date());
    }

}
