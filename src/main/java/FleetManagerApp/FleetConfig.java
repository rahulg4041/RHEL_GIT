package FleetManagerApp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
public class FleetConfig {

    @Value("${fleet.db.url}")
    private String dbUrl;

    @Value("${fleet.db.user}")
    private String dbUser;

    @Value("${fleet.db.password}")
    private String dbPassword;

    @Bean
    public Connection databaseConnection() throws Exception {
        System.out.println("🔌 Spring Boot initializing MySQL Database connection context...");

        // Ensure MySQL Driver is registered
        Class.forName("com.mysql.cj.jdbc.Driver");

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    @Bean
    public VehicleDAO vehicleDAO(Connection conn) {
        return new VehicleDAO(conn);
    }

    @Bean
    public FleetService fleetService(Connection conn) {
        return new FleetService(conn);
    }
}