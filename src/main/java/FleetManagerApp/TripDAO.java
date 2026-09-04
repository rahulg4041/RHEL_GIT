package FleetManagerApp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TripDAO {
    private final Connection conn;

    public TripDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Database Action: Inserts a new trip registry entry with an auto-generated sequence ID.
     * @return the newly generated trip_id primary key.
     */
    public long createTrip(long driverId, long vehicleId, double startOdometer) throws Exception {
        String sql = "INSERT INTO TRIPS (driver_id, vehicle_id, start_odometer, trip_status) " +
                "VALUES (?, ?, ?, 'ACTIVE')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, driverId);
            pstmt.setLong(2, vehicleId);
            pstmt.setDouble(3, startOdometer);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating trip failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating trip failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Database Action: Retrieves a trip's structural details by its ID for validation checking.
     */
    public Trip getTripById(long tripId) throws Exception {
        String sql = "SELECT trip_id, driver_id, vehicle_id, start_odometer, end_odometer, trip_status " +
                "FROM TRIPS WHERE trip_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Trip trip = new Trip();
                    trip.setTripId(rs.getLong("trip_id"));
                    trip.setDriverId(rs.getLong("driver_id"));
                    trip.setVehicleId(rs.getLong("vehicle_id"));
                    trip.setStartOdometer(rs.getDouble("start_odometer"));
                    trip.setEndOdometer(rs.getDouble("end_odometer"));
                    trip.setTripStatus(rs.getString("trip_status"));
                    return trip;
                }
            }
        }
        return null;
    }

    /**
     * Database Action: Updates an active trip record to mark it complete and logs the final mileage.
     */
    public void completeTrip(long tripId, double endOdometer) throws Exception {
        String sql = "UPDATE TRIPS SET end_odometer = ?, trip_status = 'COMPLETED' WHERE trip_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, endOdometer);
            pstmt.setLong(2, tripId);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Target Trip ID " + tripId + " does not exist.");
            }
        }
    }
}