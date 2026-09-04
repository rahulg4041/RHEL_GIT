package FleetManagerApp;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/fleet")
public class FleetApiController {

    private final VehicleDAO vehicleDAO;

    public FleetApiController(VehicleDAO vehicleDAO) {
        this.vehicleDAO = vehicleDAO;
    }

    @GetMapping("/status")
    public String getWebHealthCheck() {
        return "🚚 Fleet Management Web API Server is online and running!";
    }

    // GET: Fetch all vehicles
    @GetMapping("/vehicles")
    public List<Vehicle> getAllVehicles() {
        try {
            return vehicleDAO.getAllVehicles();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // GET: Fetch vehicle by ID
    @GetMapping("/vehicles/{id}")
    public Vehicle getVehicleById(@PathVariable("id") long id) {
        try {
            return vehicleDAO.getVehicleById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // POST: Register a new vehicle
    @PostMapping("/vehicles")
    public String registerVehicle(@RequestBody Map<String, Object> payload) {
        try {
            String make = (String) payload.get("make");
            String model = (String) payload.get("model");
            int year = payload.get("year") != null ? Integer.parseInt(payload.get("year").toString()) : 2025;

            String licensePlate = (String) payload.get("licensePlate");
            if (licensePlate == null || licensePlate.trim().isEmpty()) {
                licensePlate = "TN-" + (int)(Math.random() * 9000 + 1000);
            }

            vehicleDAO.registerNewVehicle(make, model, year, licensePlate);
            return "SUCCESS: Registered " + make + " " + model + " [" + licensePlate + "]";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    // POST: Start a Trip (Calls VehicleDAO)
    @PostMapping("/trips/start")
    public String startTrip(@RequestBody Map<String, Object> payload) {
        try {
            long vehicleId = Long.parseLong(payload.get("vehicleId").toString());
            long driverId = Long.parseLong(payload.get("driverId").toString());
            double startOdo = Double.parseDouble(payload.get("startOdometer").toString());

            vehicleDAO.startTrip(vehicleId, driverId, startOdo);
            return "SUCCESS: Trip started for Vehicle ID " + vehicleId;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    // POST: End a Trip (Calls VehicleDAO)
    @PostMapping("/trips/end")
    public String endTrip(@RequestBody Map<String, Object> payload) {
        try {
            long vehicleId = Long.parseLong(payload.get("vehicleId").toString());
            double endOdo = Double.parseDouble(payload.get("endOdometer").toString());

            vehicleDAO.endTrip(vehicleId, endOdo);
            return "SUCCESS: Trip completed for Vehicle ID " + vehicleId;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    // POST: Log Vehicle Expense
    @PostMapping("/expenses")
    public String logExpense(@RequestBody Map<String, Object> payload) {
        try {
            long vehicleId = Long.parseLong(payload.get("vehicleId").toString());
            String type = (String) payload.get("expenseType");
            String desc = (String) payload.get("description");
            double amount = Double.parseDouble(payload.get("amount").toString());

            vehicleDAO.recordExpense(vehicleId, type, desc, amount);
            return "SUCCESS: Expense of ₹" + amount + " logged for Vehicle ID " + vehicleId;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    // GET: Fleet Comparative Performance Overview Report (Option 3 Data)
    @GetMapping("/reports/overview")
    public List<FleetReport> getOverviewReport() {
        try {
            return vehicleDAO.getFleetOverviewReportData();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    // GET: Fetch expense list for a specific vehicle
    @GetMapping("/vehicles/{id}/expenses")
    public List<Expense> getExpensesByVehicle(@PathVariable("id") long id) {
        try {
            return vehicleDAO.getExpensesByVehicleId(id);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}