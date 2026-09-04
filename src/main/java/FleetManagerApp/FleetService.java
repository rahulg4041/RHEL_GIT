package FleetManagerApp;
import java.sql.Connection;

public class FleetService {
    private final Connection conn;
    private final VehicleDAO vehicleDao;
    private final DriverDAO driverDao;
    private final TripDAO tripDao;

    public FleetService(Connection conn) {
        this.conn = conn;
        this.vehicleDao = new VehicleDAO(conn);
        this.driverDao = new DriverDAO(conn);
        this.tripDao = new TripDAO(conn);
    }

    /**
     * Business Workflow: Registers a driver, automatically finds an available vehicle,
     * pulls its current odometer mileage from history, and dispatches the assets.
     */
    public void registerDriverAndStartTrip(String firstName, String lastName, String licenseNumber, String phoneNumber) throws Exception {
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1. Auto-lookup an available vehicle in the yard
            long vehicleId = vehicleDao.findAvailableVehicleId();
            if (vehicleId == -1) {
                throw new IllegalStateException("❌ Dispatch Aborted: All fleet vehicles are currently ON_TRIP!");
            }

            // 2. AUTOMATIC EXTRACTION: Fetch the latest end odometer reading to use as the start mileage
            double autoStartOdometer = vehicleDao.getLatestOdometer(vehicleId);

            // 3. Register the driver entity
            long driverId = driverDao.insertDriver(firstName, lastName, licenseNumber, phoneNumber);
            System.out.println("   [Transaction Step 1] Registered Driver ID: " + driverId);

            // 4. Create the relational trip entry using the automatically retrieved mileage
            long tripId = tripDao.createTrip(driverId, vehicleId, autoStartOdometer);
            System.out.println("   [Transaction Step 2] Relational Trip entry created (Auto-Start Odo: " + autoStartOdometer + " km).");

            // 5. Update vehicle status to lock it out on the road
            vehicleDao.updateStatus(vehicleId, "ON_TRIP");
            System.out.println("   [Transaction Step 3] Vehicle ID " + vehicleId + " marked as ON_TRIP.");

            conn.commit();
            System.out.println("   [Transaction Status] 🎉 SUCCESS! All operational states committed permanently.");

        } catch (Exception e) {
            System.out.println("   [Transaction Status] 🚨 FAILURE DETECTED! Rolling back dispatch operations...");
            try {
                conn.rollback();
            } catch (Exception rollbackEx) {
                System.err.println("⚠️ Rollback validation error: " + rollbackEx.getMessage());
            }
            throw new Exception("Workflow dispatch aborted: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Business Workflow: Closes out an active trip with transaction checks, ensuring
     * final odometer inputs are logical before updating asset availability.
     */
    public void endTripWorkflow(long tripId, long vehicleId, double endOdometer) throws Exception {
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            // 1. Fetch trip details to run cross-layer data validation checks
            Trip currentTrip = tripDao.getTripById(tripId);
            if (currentTrip == null) {
                throw new IllegalArgumentException("Target Trip ID " + tripId + " could not be resolved.");
            }

            // Odometer Business Rule Guardrail
            if (endOdometer < currentTrip.getStartOdometer()) {
                throw new IllegalArgumentException("Final odometer (" + endOdometer + " km) cannot be less than the starting odometer (" + currentTrip.getStartOdometer() + " km).");
            }

            // 2. Complete the trip in the database
            tripDao.completeTrip(tripId, endOdometer);
            System.out.println("   [Transaction Step 1] Trip ID " + tripId + " marked as COMPLETED.");

            // 3. Re-introduce vehicle back into the available pool
            vehicleDao.updateStatus(vehicleId, "AVAILABLE");
            System.out.println("   [Transaction Step 2] Vehicle ID " + vehicleId + " reset to AVAILABLE status.");

            conn.commit();
            System.out.println("   [Transaction Status] 🎉 SUCCESS! Completion workflow committed permanently.");

        } catch (Exception e) {
            System.out.println("   [Transaction Status] 🚨 FAILURE DETECTED! Rolling back closeout operations...");
            try {
                conn.rollback();
            } catch (Exception rollbackEx) {
                System.err.println("⚠️ Rollback validation error: " + rollbackEx.getMessage());
            }
            throw new Exception("Workflow closeout aborted: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }
}