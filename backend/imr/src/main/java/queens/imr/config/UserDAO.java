package queens.imr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class UserDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplateFirst;
    
    @Autowired
    private JdbcTemplate jdbcTemplateOne;

    @Autowired
    private JdbcTemplate jdbcTemplateTwo;    

    @Autowired
    private JdbcTemplate jdbcTemplateThree;
    
    @Autowired
    private JdbcTemplate jdbcTemplateFour;
    
	public JdbcTemplate getTemplate(String templateName) 
	{
		LOGGER.info("Template : "+templateName);
		if(StringUtils.isEmpty(templateName)) {return null;}
		switch(templateName)
		{
		case "jdbcTemplateFirst":
			LOGGER.info("jdbcTemplateFirst");
			return jdbcTemplateFirst;
			
		case "jdbcTemplateOne":
			LOGGER.info("jdbcTemplateOne");

			return jdbcTemplateOne;
			
		case "jdbcTemplateTwo":
			LOGGER.info("jdbcTemplateTwo");

			return jdbcTemplateTwo;
			
		case "jdbcTemplateThree":
			LOGGER.info("jdbcTemplateThree");

			return jdbcTemplateThree;
			
		case "jdbcTemplateFour":
			LOGGER.info("jdbcTemplateFour");

			return jdbcTemplateFour;
			
		default:
			LOGGER.info("Default : ");
			break;
		}
		LOGGER.info("return null : ");

		return null;
	}
}
