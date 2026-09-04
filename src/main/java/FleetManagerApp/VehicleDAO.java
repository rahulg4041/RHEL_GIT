package FleetManagerApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {
    private final Connection conn;

    public VehicleDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Helper Method: Provides access to the active database connection context.
     */
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * Database Action: Finds the first available vehicle ID in the yard.
     * Returns -1 if no vehicles are available.
     */
    public long findAvailableVehicleId() throws Exception {
        // MySQL uses LIMIT 1 instead of FETCH FIRST 1 ROWS ONLY
        String sql = "SELECT vehicle_id FROM vehicles WHERE status = 'AVAILABLE' LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("vehicle_id");
            }
        }
        return -1;
    }

    /**
     * Business Logic: Fetches the last recorded odometer reading for a vehicle.
     * Defaults to 0.0 if the vehicle has no previous trip history.
     */
    public double getLatestOdometer(long vehicleId) throws Exception {
        String sql = "SELECT MAX(end_odometer) as last_odo FROM trips WHERE vehicle_id = ? AND end_odometer IS NOT NULL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double lastOdo = rs.getDouble("last_odo");
                    return lastOdo > 0 ? lastOdo : 0.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Database Action: Updates a vehicle's operational status.
     */
    public void updateStatus(long vehicleId, String status) throws Exception {
        String sql = "UPDATE vehicles SET status = ? WHERE vehicle_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setLong(2, vehicleId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Legacy 3-Parameter Method (Used by CLI / Main App)
     * Passes a default year (2025) to the primary method.
     */
    public void registerNewVehicle(String make, String model, String licensePlate) throws Exception {
        registerNewVehicle(make, model, 2025, licensePlate);
    }

    /**
     * Primary 4-Parameter Method (Used by Web API Controller & CLI)
     */
    public void registerNewVehicle(String make, String model, int year, String licensePlate) throws Exception {
        String dummyVin = "VIN" + System.currentTimeMillis() + (int) (Math.random() * 100);
        String sql = "INSERT INTO vehicles (make, model, manufacture_year, license_plate, vin, status) VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, make);
            pstmt.setString(2, model);
            pstmt.setInt(3, year);
            pstmt.setString(4, licensePlate);
            pstmt.setString(5, dummyVin);
            pstmt.executeUpdate();
            System.out.println("   [Database Status] 🎉 SUCCESS! Vehicle " + make + " " + model + " [" + licensePlate + "] registered with VIN " + dummyVin);
        }
    }

    /**
     * Database Action: Records a business expense against a specific vehicle.
     */
    public void recordExpense(long vehicleId, String expenseType, String description, double amount) throws Exception {
        // MySQL uses NOW() instead of Oracle SYSDATE
        String sql = "INSERT INTO expenses (vehicle_id, expense_type, description, amount, expense_date) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, vehicleId);
            pstmt.setString(2, expenseType);
            pstmt.setString(3, description);
            pstmt.setDouble(4, amount);

            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Target vehicle ID " + vehicleId + " does not exist.");
            }
            System.out.println("   [Database Status] 🎉 SUCCESS! Expense of ₹" + amount + " logged for Vehicle ID " + vehicleId + ".");
        }
    }

    /**
     * Web API Action: Fetches all vehicles from the database as a List of Vehicle objects.
     */
    public List<Vehicle> getAllVehicles() throws Exception {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, make, model, manufacture_year, status FROM vehicles ORDER BY vehicle_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Vehicle v = new Vehicle();
                v.setId(rs.getInt("vehicle_id"));
                v.setMake(rs.getString("make"));
                v.setModel(rs.getString("model"));
                v.setYear(rs.getInt("manufacture_year"));
                v.setStatus(rs.getString("status"));
                list.add(v);
            }
        }
        return list;
    }

    /**
     * Web API Action: Fetches a single vehicle record by ID.
     */
    public Vehicle getVehicleById(long id) throws Exception {
        String sql = "SELECT vehicle_id, make, model, manufacture_year, status FROM vehicles WHERE vehicle_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Vehicle v = new Vehicle();
                    v.setId(rs.getInt("vehicle_id"));
                    v.setMake(rs.getString("make"));
                    v.setModel(rs.getString("model"));
                    v.setYear(rs.getInt("manufacture_year"));
                    v.setStatus(rs.getString("status"));
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Web API Action: Fetches cross-fleet performance summary data.
     */
    public List<FleetReport> getFleetOverviewReportData() throws Exception {
        List<FleetReport> reportList = new ArrayList<>();
        // IFNULL replaces Oracle NVL; stored function get_total_vehicle_expense replicated in MySQL
        String sql = "SELECT v.vehicle_id, v.make, v.model, v.license_plate, v.status, " +
                "COUNT(t.trip_id) as total_trips, " +
                "IFNULL(SUM(CASE WHEN t.end_odometer IS NOT NULL THEN (t.end_odometer - t.start_odometer) ELSE 0 END), 0) as total_kms, " +
                "IFNULL(get_total_vehicle_expense(v.vehicle_id), 0.0) as total_expenses " +
                "FROM vehicles v LEFT JOIN trips t ON v.vehicle_id = t.vehicle_id " +
                "GROUP BY v.vehicle_id, v.make, v.model, v.license_plate, v.status " +
                "ORDER BY v.vehicle_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FleetReport r = new FleetReport();
                r.setId(rs.getInt("vehicle_id"));
                r.setVehicleModel(rs.getString("make") + " " + rs.getString("model"));
                r.setPlate(rs.getString("license_plate"));
                r.setStatus(rs.getString("status"));
                r.setTotalTrips(rs.getInt("total_trips"));

                double kms = rs.getDouble("total_kms");
                double expenses = rs.getDouble("total_expenses");

                r.setTotalKms(kms);
                r.setTotalExpenses(expenses);
                r.setCostPerKm(kms > 0 ? (expenses / kms) : 0.0);

                reportList.add(r);
            }
        }
        return reportList;
    }

    /**
     * Diagnostic View: Prints the live performance report grid to the console.
     */
    public void printFleetOverviewReport() throws Exception {
        String sql = "SELECT v.vehicle_id, v.make, v.model, v.license_plate, v.status, " +
                "COUNT(t.trip_id) as total_trips, " +
                "IFNULL(SUM(CASE WHEN t.end_odometer IS NOT NULL THEN (t.end_odometer - t.start_odometer) ELSE 0 END), 0) as total_kms, " +
                "IFNULL(get_total_vehicle_expense(v.vehicle_id), 0.0) as total_expenses " +
                "FROM vehicles v LEFT JOIN trips t ON v.vehicle_id = t.vehicle_id " +
                "GROUP BY v.vehicle_id, v.make, v.model, v.license_plate, v.status " +
                "ORDER BY v.vehicle_id";

        System.out.println("\n=========================================================================================");
        System.out.println("   📊 CROSS-FLEET COMPARATIVE PERFORMANCE REPORT                                         ");
        System.out.println("=========================================================================================");
        System.out.printf("%-4s | %-20s | %-13s | %-12s | %-6s | %-12s | %-10s\n",
                "ID", "Vehicle Model", "Plate", "Status", "Trips", "Distance", "Cost / KM");
        System.out.println("-----------------------------------------------------------------------------------------");

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("vehicle_id");
                String modelStr = rs.getString("make") + " " + rs.getString("model");
                String plate = rs.getString("license_plate");
                String status = rs.getString("status");
                int trips = rs.getInt("total_trips");
                double kms = rs.getDouble("total_kms");
                double expenses = rs.getDouble("total_expenses");

                String statusEmoji = "AVAILABLE".equals(status) ? "🟢 AVAILABLE" : "⚠️ ON TRIP";
                double costPerKm = kms > 0 ? (expenses / kms) : 0.0;

                System.out.printf("%-4d | %-20s | %-13s | %-12s | %-6d | %-7.1f km | ₹%-8.2f\n",
                        id, modelStr, plate, statusEmoji, trips, kms, costPerKm);
            }
        }
        System.out.println("=========================================================================================");
    }

    /**
     * Reference View: Prints a quick list of active trips with their starting mileage.
     */
    public void printActiveTripsQuickReference() throws Exception {
        // Use CONCAT() instead of Oracle's || string concatenation
        String sql = "SELECT t.trip_id, t.vehicle_id, v.make, v.model, v.license_plate, t.start_odometer, " +
                "CONCAT(d.first_name, ' ', d.last_name) as driver_name " +
                "FROM trips t JOIN vehicles v ON t.vehicle_id = v.vehicle_id JOIN drivers d ON t.driver_id = d.driver_id " +
                "WHERE t.trip_status = 'ACTIVE'";

        System.out.println("\n📌 LIVE ACTIVE TRIPS (🚨 Use these details to close out):");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-10s | %-20s | %-13s | %-12s | %-15s\n",
                "TRIP ID", "VEHICLE ID", "Vehicle Model", "Plate", "START ODO", "Driver");
        System.out.println("-----------------------------------------------------------------------------------------");

        boolean hasActive = false;
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                hasActive = true;
                System.out.printf("%-10d | %-10d | %-20s | %-13s | %-7.1f km  | %-15s\n",
                        rs.getLong("trip_id"),
                        rs.getLong("vehicle_id"),
                        rs.getString("make") + " " + rs.getString("model"),
                        rs.getString("license_plate"),
                        rs.getDouble("start_odometer"),
                        rs.getString("driver_name"));
            }
        }
        if (!hasActive) {
            System.out.println("   🟢 No vehicles are currently out on a trip right now.");
        }
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    public void startTrip(long vehicleId, long driverId, double startOdometer) throws Exception {
        updateStatus(vehicleId, "ON_TRIP");
        // Use NOW() instead of SYSDATE
        String sql = "INSERT INTO trips (vehicle_id, driver_id, start_odometer, trip_status, start_time) VALUES (?, ?, ?, 'ACTIVE', NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, vehicleId);
            pstmt.setLong(2, driverId);
            pstmt.setDouble(3, startOdometer);
            pstmt.executeUpdate();
        }
    }

    public void endTrip(long vehicleId, double endOdometer) throws Exception {
        // Use NOW() instead of SYSDATE
        String sql = "UPDATE trips SET end_odometer = ?, trip_status = 'COMPLETED', end_time = NOW() WHERE vehicle_id = ? AND trip_status = 'ACTIVE'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, endOdometer);
            pstmt.setLong(2, vehicleId);
            pstmt.executeUpdate();
        }
        updateStatus(vehicleId, "AVAILABLE");
    }

    /**
     * Web API Action: Fetches all logged expenses for a specific vehicle.
     */
    public List<Expense> getExpensesByVehicleId(long vehicleId) throws Exception {
        List<Expense> list = new ArrayList<>();
        // DATE_FORMAT replaces Oracle TO_CHAR()
        String sql = "SELECT expense_id, vehicle_id, expense_type, description, amount, " +
                "DATE_FORMAT(expense_date, '%Y-%m-%d %H:%i') as formatted_date " +
                "FROM expenses WHERE vehicle_id = ? ORDER BY expense_id DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Expense e = new Expense();
                    e.setExpenseId(rs.getLong("expense_id"));
                    e.setVehicleId(rs.getLong("vehicle_id"));
                    e.setExpenseType(rs.getString("expense_type"));
                    e.setDescription(rs.getString("description"));
                    e.setAmount(rs.getDouble("amount"));
                    e.setExpenseDate(rs.getString("formatted_date"));
                    list.add(e);
                }
            }
        }
        return list;
    }
}