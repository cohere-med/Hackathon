package queens.imr.pojo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
@Component
public class ReportTemplates 
{
	private  Map<String, Object> reportTemplateOne = new HashMap<String, Object>();
	private String imagePath ;
	private String imageKey;
	public String getImageKey() {
		return imageKey;
	}

	public void setImageKey(String imageKey) {
		this.imageKey = imageKey;
	}

	public Map<String, Object> getReportTemplateOne() 
	{
		return reportTemplateOne;
	}

	public void setReportTemplateOne(Map<String, Object> reportTemplateOne) 
	{
		this.reportTemplateOne = reportTemplateOne;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}


}
