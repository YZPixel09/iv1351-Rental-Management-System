package se.kth.iv1351.soundgoodjdbc.integration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import se.kth.iv1351.soundgoodjdbc.model.Instrument;
import se.kth.iv1351.soundgoodjdbc.model.InstrumentDTO;


public class InstrumentDAO {
    private static final String INSTRUMENT_TABLE = "instrument";
    private static final String RENTAL_PRICE_HISTORY_TABLE = "rental_price_history";
    private static final String INSTRUMENT_RENTAL_TABLE = "instrument_rental";

    // Columns for instrument table
    private static final String INSTRUMENT_ID = "instrument_id";
    private static final String INSTRUMENT_TYPE = "instrument_type";
    private static final String INSTRUMENT_BRAND = "instrument_brand";
    private static final String AVAILABLE_STOCK = "available_stock";

    // Columns for rental price history
    private static final String PRICE = "price";
    private static final String IS_CURRENT = "is_current";

    // Columns for instrument rental
    private static final String RENTAL_ID = "rental_id";
    private static final String RENTAL_START_TIME = "rental_start_time";
    private static final String LEASE_EXPIRY_TIME = "lease_expiry_time";
    private static final String STUDENT_ID = "student_id";
    private static final String RENTAL_PRICE_ID = "rental_price_id";

    private Connection connection;

    // Prepared statements
    private PreparedStatement findAvailableInstrumentsStmtLockingForUpdate;
    private PreparedStatement createRentalStmt;
    private PreparedStatement findRentalCountByStudentStmt;
    private PreparedStatement updateRentalExpiryStmt;
    private PreparedStatement findRentalsByStudentStmtLockingForUpdate;

    /**
     * Creates a new DAO instance.
     */
    public InstrumentDAO() throws InstrumentDBException {
        try {
            connectToDatabase();
            prepareStatements();
        } catch (SQLException e) {
            throw new InstrumentDBException("Could not connect to the datasource.", e);
        }
    }

    /**
     * Connects to the database.
     */
    private void connectToDatabase() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5433/soundgood",
                "postgres", "my_password"); // Replace with your credentials
        connection.setAutoCommit(false);
    }

    /**
     * Prepares all SQL statements.
     */
    private void prepareStatements() throws SQLException {
        findAvailableInstrumentsStmtLockingForUpdate = connection.prepareStatement(
    "SELECT i." + INSTRUMENT_ID + ", i." + INSTRUMENT_TYPE + ", i." + INSTRUMENT_BRAND +
    ", rph." + PRICE + ", i." + AVAILABLE_STOCK +
    " FROM " + INSTRUMENT_TABLE + " i " +
    "JOIN " + RENTAL_PRICE_HISTORY_TABLE + " rph ON i." + INSTRUMENT_ID + " = rph." + INSTRUMENT_ID +
    " WHERE i." + INSTRUMENT_TYPE + " = ? AND i." + AVAILABLE_STOCK + " > 0 AND rph." + IS_CURRENT + " = TRUE " +
    "FOR NO KEY UPDATE"
       );

        

        findRentalCountByStudentStmt = connection.prepareStatement(
            "SELECT COUNT(*) AS rental_count FROM " + INSTRUMENT_RENTAL_TABLE +
            " WHERE " + STUDENT_ID + " = ? AND " + LEASE_EXPIRY_TIME + " > NOW()"
        );

        createRentalStmt = connection.prepareStatement(
            "INSERT INTO " + INSTRUMENT_RENTAL_TABLE +
            " (" + RENTAL_ID + ", " + RENTAL_START_TIME + ", " + LEASE_EXPIRY_TIME + ", " + RENTAL_PRICE_ID + ", " + INSTRUMENT_ID + ", " + STUDENT_ID + ") " +
            "VALUES (?, ?, ?, ?, ?, ?)"
        );


        updateRentalExpiryStmt = connection.prepareStatement(
            "UPDATE " + INSTRUMENT_RENTAL_TABLE + " SET " + LEASE_EXPIRY_TIME + " = NOW() WHERE " + RENTAL_ID + " = ?"
        );

        findRentalsByStudentStmtLockingForUpdate = connection.prepareStatement(
            "SELECT " + RENTAL_ID + ", " + INSTRUMENT_ID + ", " + LEASE_EXPIRY_TIME +
            " FROM " + INSTRUMENT_RENTAL_TABLE + " WHERE " + STUDENT_ID + " = ? FOR NO KEY UPDATE"
        );
    }

    /**
     * Finds all available instruments of a given type.
     */
    public List<InstrumentDTO> findAvailableInstruments(String type) throws InstrumentDBException {
        String failureMsg = "Failed to find available instruments of type: " + type;
        ResultSet result = null;
        List<InstrumentDTO> instruments = new ArrayList<>();
        try {
            findAvailableInstrumentsStmtLockingForUpdate.setString(1, type);
            result = findAvailableInstrumentsStmtLockingForUpdate.executeQuery();
            while (result.next()) {
                instruments.add(new Instrument(
                    result.getString(INSTRUMENT_ID),
                    result.getString(INSTRUMENT_TYPE),
                    result.getString(INSTRUMENT_BRAND),
                    result.getDouble(PRICE),
                    result.getInt(AVAILABLE_STOCK)
                ));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            if (result != null) {
                closeResultSet(failureMsg, result);
            }
        }
        return instruments;
    }

    /**
     * Finds how many instruments a student is currently renting.
     */
    public int findRentalCountByStudent(int studentId) throws InstrumentDBException {
        String failureMsg = "Failed to find rental count for student: " + studentId;
        ResultSet rs = null;
        int rentalCount = 0;
        try {
            findRentalCountByStudentStmt.setInt(1, studentId);
            rs = findRentalCountByStudentStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rental_count");
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            if (rs != null) {
                closeResultSet(failureMsg, rs);
            }
        }
        return rentalCount;
    }

    /**
     * Creates a new rental.
     */
    public void createRentalOnInstrument(int studentId, String instrumentId, String rentalPriceId,int rentalDuration) throws InstrumentDBException {
        String failureMsg = "Failed to rent instrument: " + instrumentId + " for student: " + studentId;

        try {
            // Insert the new rental record
            createRentalStmt.setString(1, generateUniqueId());
            createRentalStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // Current time
            createRentalStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis() + rentalDuration * 30L * 24 * 60 * 60 * 1000)); // Expiry time
            createRentalStmt.setString(4, rentalPriceId);
            createRentalStmt.setString(5, instrumentId);
            createRentalStmt.setInt(6, studentId);
            createRentalStmt.executeUpdate();

            connection.commit();

        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Terminates a rental.
     */
    public void deleteRental(String rentalId) throws InstrumentDBException {
        String failureMsg = "Failed to terminate rental: " + rentalId;

        try {
            // Update rental record to terminate
            updateRentalExpiryStmt.setString(1, rentalId);
            updateRentalExpiryStmt.executeUpdate();
   
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        }
    }

    /**
     * Finds all rentals for a specific student, including their status (active or terminated).
     */
    public List<String> findRentalsByStudent(int studentId) throws InstrumentDBException {
        String failureMsg = "Failed to find rentals for student: " + studentId;
        ResultSet result = null;
        List<String> rentals = new ArrayList<>();
        try {
            findRentalsByStudentStmtLockingForUpdate.setInt(1, studentId);
            result = findRentalsByStudentStmtLockingForUpdate.executeQuery();

           
            while (result.next()) {
                String status = result.getTimestamp(LEASE_EXPIRY_TIME).after(new Timestamp(System.currentTimeMillis())) ? "Active" : "Terminated";
                rentals.add("Rental ID: " + result.getString(RENTAL_ID) +
                            ", Instrument ID: " + result.getString(INSTRUMENT_ID) +
                            ", Status: " + status);
            }

            connection.commit();
        } catch (SQLException e) {
            handleException(failureMsg, e);
        } finally {
            if (result != null) {
                closeResultSet(failureMsg, result);
            }
        }
        return rentals;
    }
    /**
     * Handles exceptions and rolls back transactions.
     */
    private void handleException(String failureMsg, Exception cause) throws InstrumentDBException {
        try {
            connection.rollback();
        } catch (SQLException rollbackExc) {
            failureMsg += " Also failed to rollback transaction: " + rollbackExc.getMessage();
        }
        throw new InstrumentDBException(failureMsg, cause);
    }

    /**
     * Closes the given ResultSet.
     */
    private void closeResultSet(String failureMsg, ResultSet result) throws InstrumentDBException {
        try {
            result.close();
        } catch (Exception e) {
            throw new InstrumentDBException(failureMsg + " Could not close ResultSet.", e);
        }
    }

    /**
     * Commits the transaction.
     */
    public void commit() throws InstrumentDBException {
        try {
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to commit", e);
        }
    }

    /**
     * Generates a unique ID.
     */
    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
}