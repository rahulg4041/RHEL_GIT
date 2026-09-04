package FleetManagerApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;

public class FleetManagerApp {

    // MySQL connection settings for your Azure VM container
    // Use DNS when running locally from your PC; change host to "localhost" if running directly on the Azure VM.
    private static final String DB_HOST = System.getenv("DB_HOST") != null
            ? System.getenv("DB_HOST")
            : "rahul-devops-lab.centralindia.cloudapp.azure.com";

    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "fleet_db";

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "fleet_user";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "UserPassword123!";

    public static void main(String[] args) {
        System.out.println("Connecting to MySQL Database on " + DB_HOST + "...");

        try {
            // Explicitly load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found! Check your pom.xml dependencies.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("🎉 SUCCESS! Connected to the fleet_db MySQL schema!");

            // Instantiate DAOs and Services
            VehicleDAO vehicleDAO = new VehicleDAO(conn);
            FleetService fleetService = new FleetService(conn);
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("\n=============================================");
                System.out.println("🚚 FLEET MANAGEMENT SYSTEM MENU");
                System.out.println("=============================================");
                System.out.println("1. 🛫 Dispatch Driver & Start New Trip");
                System.out.println("2. 🛬 Close Out Active Trip");
                System.out.println("3. 📊 View Cross-Fleet Performance Report");
                System.out.println("4. 🚗 Register New Fleet Vehicle");
                System.out.println("5. 💸 Log Operational Expense");
                System.out.println("6. ❌ Exit Application");
                System.out.print("👉 Select an operational choice (1-6): ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        System.out.println("\n--- Initiating Booking Workflow ---");
                        System.out.print("Enter Driver First Name: ");
                        String firstName = scanner.nextLine().trim();

                        System.out.print("Enter Driver Last Name: ");
                        String lastName = scanner.nextLine().trim();

                        // Generates a clean pseudo-random unique license token
                        String license = "DL-TN07-" + (System.currentTimeMillis() % 100000);

                        System.out.print("Enter Contact Phone Number: ");
                        String phone = scanner.nextLine().trim();

                        try {
                            fleetService.registerDriverAndStartTrip(firstName, lastName, license, phone);
                        } catch (Exception e) {
                            System.out.println("\n❌ WORKFLOW ERROR: " + e.getMessage());
                        }
                        break;

                    case "2":
                        System.out.println("\n--- Trip Close Out Workflow ---");
                        vehicleDAO.printActiveTripsQuickReference();

                        System.out.print("\nEnter active TRIP ID to close out: ");
                        long closeTripId = Long.parseLong(scanner.nextLine().trim());

                        System.out.print("Enter active VEHICLE ID associated with trip: ");
                        long closeVehicleId = Long.parseLong(scanner.nextLine().trim());

                        System.out.print("Enter Final Odometer Reading at Return (km): ");
                        double endOdometer = Double.parseDouble(scanner.nextLine().trim());

                        try {
                            fleetService.endTripWorkflow(closeTripId, closeVehicleId, endOdometer);
                        } catch (Exception e) {
                            System.out.println("\n❌ WORKFLOW ERROR: " + e.getMessage());
                        }
                        break;

                    case "3":
                        System.out.println("\n--- Fetching Live Database Analytics ---");
                        vehicleDAO.printFleetOverviewReport();
                        vehicleDAO.printActiveTripsQuickReference();
                        break;

                    case "4":
                        System.out.println("\n--- Vehicle Procurement Registration ---");
                        System.out.print("Enter Manufacturer/Make (e.g., Maruti Suzuki): ");
                        String make = scanner.nextLine().trim();
                        System.out.print("Enter Model Name (e.g., Grand Vitara): ");
                        String model = scanner.nextLine().trim();
                        System.out.print("Enter License Plate Number (e.g., TN-07-XX-9999): ");
                        String plate = scanner.nextLine().trim();

                        try {
                            vehicleDAO.registerNewVehicle(make, model, plate);
                        } catch (Exception e) {
                            System.out.println("\n❌ REGISTRATION ERROR: " + e.getMessage());
                        }
                        break;

                    case "5":
                        System.out.println("\n--- Log Operational Expense Overhead ---");
                        System.out.print("Enter Vehicle ID to charge: ");
                        long expVehicleId = Long.parseLong(scanner.nextLine().trim());

                        System.out.print("Enter Expense Type (e.g., FUEL, MAINTENANCE, TOLL): ");
                        String expType = scanner.nextLine().trim().toUpperCase();

                        System.out.print("Enter Expense Description (e.g., HP Bunk Refuel, Brake Pad Change): ");
                        String desc = scanner.nextLine().trim();

                        System.out.print("Enter Expense Amount (₹): ");
                        double amt = Double.parseDouble(scanner.nextLine().trim());

                        try {
                            vehicleDAO.recordExpense(expVehicleId, expType, desc, amt);
                        } catch (Exception e) {
                            System.out.println("\n❌ EXPENSE RECORDING ERROR: " + e.getMessage());
                        }
                        break;

                    case "6":
                        System.out.println("\n🔌 Shutting down fleet database context connections. Goodbye!");
                        running = false;
                        break;

                    default:
                        System.out.println("\n⚠️ Invalid choice! Please enter a value between 1 and 6.");
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("\n🚨 CRITICAL ERROR INITIALIZING DATABASE DRIVERS!");
            e.printStackTrace();
        }
    }
}