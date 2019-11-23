package queens.imr.app;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import queens.imr.controller.ReadJsonFiles;
import queens.imr.controller.patientFetchController;



@SpringBootApplication
public class App {
    public static void main(String[] args)
    {
      //  SpringApplication.run(SpringBootApp.class, args);
   	 ConfigurableApplicationContext configurableApplicationContext = new SpringApplicationBuilder(patientFetchController.class).run(args);//SpringApplication.run(FlowsheetController.class, args);
   	 configurableApplicationContext.start();
   	 configurableApplicationContext.getBean(ReadJsonFiles.class);

    }

    @Bean
    public HealthIndicator dbHealthIndicator() {
        return new HealthIndicator() {
        //	@Override
            public Health health() {
                return Health.status(Status.UP).withDetail("DB Health", "Check").build();
            }
        };
    }    
}
