package FleetManagerApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleetWebApplication {
    public static void main(String[] args) {
        // This single line starts the embedded Tomcat server on port 8080
        SpringApplication.run(FleetWebApplication.class, args);
    }
}