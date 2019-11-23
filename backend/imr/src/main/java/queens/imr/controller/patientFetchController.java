package queens.imr.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



import queens.imr.db.Store;
import queens.imr.entitiy.Patient;
import queens.imr.config.UserDAO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.jasperreports.engine.JasperPrint;

@Controller
@EnableAutoConfiguration
@EnableAsync
@RestController
@ComponentScan("queens.imr")
@Component
@ConfigurationProperties(prefix = "templates")
@Api(value = "queens")
@CrossOrigin(methods = { RequestMethod.GET, RequestMethod.PUT, RequestMethod.POST, RequestMethod.DELETE,
		RequestMethod.OPTIONS, RequestMethod.HEAD })
@RequestMapping(value = "/api", produces = { MediaType.APPLICATION_JSON_VALUE,MediaType.IMAGE_JPEG_VALUE })

public class patientFetchController 
{
	static final Logger LOGGER = LoggerFactory.getLogger(patientFetchController.class);	
	private Map<String, String> tenant = new HashMap<>();
	private JdbcTemplate dbTemplate;
	@Autowired
	public UserDAO userDAO;
	@Autowired
	private Store store;
	public Map<String, String> getTenant() 
	{
		return tenant;
	}
	@Value("${configdata.reportxml}")
	private String reportxml;
	@ApiOperation(value = "Get all patients", response = ResponseEntity.class)		  
	@RequestMapping(value = "/getAllPatients", method = RequestMethod.GET)
	@ResponseBody ResponseEntity<String>  getAllPatient() {
		
		LOGGER.info("\n--------------Get All Patient  API invoked------------");
		String token = "kauveryhealthcare.com";
		LOGGER.info("Token -- "+token.toString());
		String templateName = tenant.get(token);
		if (templateName != null && !StringUtils.isEmpty(token)) 
		{				
			dbTemplate = userDAO.getTemplate(templateName);
			if(dbTemplate ==null) 
				{
				throw new NullPointerException();
				}
			LOGGER.info("\nSetting up DB template");
			store.setTemplate(dbTemplate);			
			JSONObject responseObj = store.getAllPatient();
			if(responseObj !=null)
			{
	        store.setTemplate(null); 
	        return new ResponseEntity<String>(responseObj.toString(), HttpStatus.OK);
			}	
			LOGGER.info("\n--------------Queued data API completed ------------");

		} 
		else 
		{
			LOGGER.error("Error while reading resource details,  ");
		}	
		store.setTemplate(null); 
		return new ResponseEntity<String>("{\"OutputDetails\":[]}", HttpStatus.OK);
	}
	@ApiOperation(value = "Get all patients", response = ResponseEntity.class)		  
	@RequestMapping(value = "/getPatient", method = RequestMethod.POST)
	@ResponseBody ResponseEntity<String>  getOnlyPatient(HttpEntity<String> httpEntity) {
		
		LOGGER.info("\n--------------Get All Patient  API invoked------------");
		String token = "kauveryhealthcare.com";
		LOGGER.info("Token -- "+token.toString());
		String templateName = tenant.get(token);
		if (templateName != null && !StringUtils.isEmpty(token)) 
		{				
			dbTemplate = userDAO.getTemplate(templateName);
			if(dbTemplate ==null) 
				{
				throw new NullPointerException();
				}
			LOGGER.info("\nSetting up DB template");
			store.setTemplate(dbTemplate);	
			String requestBody = httpEntity.getBody();	
			LOGGER.debug("requestBody"+requestBody);
			JSONObject jsonRequestObj = new JSONObject(requestBody); 
			String patientName= (String) jsonRequestObj.get("name");
			String mobileNumber = (String)jsonRequestObj.get("mobile");
			JSONObject responseObj = store.getOnePatient(patientName,mobileNumber);
			if(responseObj !=null)
			{
	        store.setTemplate(null); 
	        return new ResponseEntity<String>(responseObj.toString(), HttpStatus.OK);
			}	
			LOGGER.info("\n--------------Queued data API completed ------------");

		} 
		else 
		{
			LOGGER.error("Error while reading resource details,  ");
		}	
		store.setTemplate(null); 
		return new ResponseEntity<String>("{\"OutputDetails\":[]}", HttpStatus.OK);
	}
	@ApiOperation(value = "Will generate the pdf report in base64 encoded format based on the identifier value.", response = ResponseEntity.class)
	@RequestMapping(value = "/report", method = RequestMethod.POST)
	@ResponseBody
	ResponseEntity<String> saveReport                                                     
	(HttpEntity<String> httpEntity,
			HttpServletRequest request) 
	{
		LOGGER.info("\n\n--------------Report Generating API invoked------------"); 
		JSONObject response=new JSONObject();
		try
		{
			String token = "kauveryhealthcare.com";
			String templateName = tenant.get(token);
			LOGGER.info("\n\ntemplateName received and it is not null--> " + templateName);
			dbTemplate = userDAO.getTemplate(templateName);
			LOGGER.info("\nDB template selection completed"+dbTemplate);
			if (dbTemplate != null) 
			{
				LOGGER.info("\nDB template not null");
				store.setTemplate(dbTemplate);
				String requestBody = httpEntity.getBody();	
				LOGGER.debug("requestBody"+requestBody);
				JSONObject jsonRequestObj = new JSONObject(requestBody); 
				String patientName= (String) jsonRequestObj.get("name");
				String mobileNumber = (String)jsonRequestObj.get("mobile");
				LOGGER.info("\n\n\n----fetching data from db--->");
				JSONObject responseObj = store.getOnePatient(patientName,mobileNumber);
				if(responseObj!=null && responseObj.length()>0 )
				{
					JasperPrint staticJasperPrint = null;
					LOGGER.info("\n\n---- Starting the Jasper Print pdf----");
					staticJasperPrint = store.exportFile(responseObj,reportxml);
					LOGGER.info("\n\nGot the all the print variables---");
					if(staticJasperPrint!=null ) 
					{
						ResponseEntity<String> responseVariable = store.pdfGenerator(staticJasperPrint,responseObj);	
						return responseVariable; 
					}
					else
					{
						LOGGER.error("\nError while fetching report parameters and hence the JasperPrint is Empty.");							
						response.put("ErrorReason", "Error in fetching report parameters and hence the JasperPrint is Empty so couldnot generate the report ");
						response.put("code", "500 Internal Server Error");
						store.setTemplate(null);
						return  new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
				else
				{
					LOGGER.error("\nIdentifier doesn't exists in the db.");	
					 response.put("ErrorReason", "Identifier doesn't exists in the db.");
					 response.put("code", "500 Internal Server Error");
					 store.setTemplate(null);
					 return  new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);	
				}
			}			
			else
			{
				LOGGER.error("\nError while connecting to db.");
				response.put("ErrorReason", "Error while connecting to the database and collecting resource details.");
				response.put("code", "500 Internal Server Error");
				store.setTemplate(null);
				return  new ResponseEntity<String>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);	
			}				
		}
		catch(NullPointerException ex)
		{
			LOGGER.error("\nFile could not be uploaded!!! Error in fetching data for jasper print from the database. ");
			ex.printStackTrace();
			response.put("ErrorReason", "File could not be uploaded!!! Error in fetching data from the database.");
			response.put("code", "500 Internal Server Error");
			store.setTemplate(null);
			return new ResponseEntity<String>("Data could not be uploaded!!! Error in fetching the AI urls", HttpStatus.INTERNAL_SERVER_ERROR);		
		}
					
	}

}
