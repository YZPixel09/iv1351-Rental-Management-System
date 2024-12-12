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
    private PreparedStatement findAvailableInstrumentsStmt;
    private PreparedStatement createRentalStmt;
    private PreparedStatement findAvailableInstrumentByIdLockingForUpdateStmt;
    private PreparedStatement findRentalCountByStudentStmt;
    private PreparedStatement updateRentalExpiryStmt;
    private PreparedStatement updateInstrumentStockAfterRentalStmt;
    private PreparedStatement findRentalsByStudentStmt;
    private PreparedStatement decreaseInstrumentStockStmt;

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
        findAvailableInstrumentsStmt = connection.prepareStatement(
            "SELECT i." + INSTRUMENT_ID + ", i." + INSTRUMENT_TYPE + ", i." + INSTRUMENT_BRAND +
            ", rph." + PRICE + ", i." + AVAILABLE_STOCK +
            " FROM " + INSTRUMENT_TABLE + " i " +
            "JOIN " + RENTAL_PRICE_HISTORY_TABLE + " rph ON i." + INSTRUMENT_ID + " = rph." + INSTRUMENT_ID +
            " WHERE i." + INSTRUMENT_TYPE + " = ? AND i." + AVAILABLE_STOCK + " > 0 AND rph." + IS_CURRENT + " = TRUE"
        );

        findAvailableInstrumentByIdLockingForUpdateStmt = connection.prepareStatement(
            "SELECT " + AVAILABLE_STOCK + " FROM " + INSTRUMENT_TABLE +
            " WHERE " + INSTRUMENT_ID + " = ? AND " + AVAILABLE_STOCK + " > 0 FOR NO KEY UPDATE"
        );

        findRentalCountByStudentStmt = connection.prepareStatement(
            "SELECT COUNT(*) AS rental_count FROM " + INSTRUMENT_RENTAL_TABLE +
            " WHERE " + STUDENT_ID + " = ? AND " + LEASE_EXPIRY_TIME + " > NOW()"
        );

        createRentalStmt = connection.prepareStatement(
            "INSERT INTO " + INSTRUMENT_RENTAL_TABLE +
            " (" + RENTAL_ID + ", " + RENTAL_START_TIME + ", " + LEASE_EXPIRY_TIME + ", " + RENTAL_PRICE_ID + ", " + INSTRUMENT_ID + ", " + STUDENT_ID + ") " +
            "VALUES (?, NOW(), NOW() + INTERVAL '30 days', ?, ?, ?)"
        );

         decreaseInstrumentStockStmt = connection.prepareStatement(
           "UPDATE " + INSTRUMENT_TABLE + " SET " + AVAILABLE_STOCK + " = " + AVAILABLE_STOCK + " - 1 WHERE " + INSTRUMENT_ID + " = ?"
        );

        updateRentalExpiryStmt = connection.prepareStatement(
            "UPDATE " + INSTRUMENT_RENTAL_TABLE + " SET " + LEASE_EXPIRY_TIME + " = NOW() WHERE " + RENTAL_ID + " = ?"
        );

        updateInstrumentStockAfterRentalStmt = connection.prepareStatement(
            "UPDATE " + INSTRUMENT_TABLE +
            " SET " + AVAILABLE_STOCK + " = " + AVAILABLE_STOCK + " + 1 " +
            "WHERE " + INSTRUMENT_ID + " = (" +
            "SELECT " + INSTRUMENT_ID + " FROM " + INSTRUMENT_RENTAL_TABLE +
            " WHERE " + RENTAL_ID + " = ?)"
        );

        findRentalsByStudentStmt = connection.prepareStatement(
            "SELECT " + RENTAL_ID + ", " + INSTRUMENT_ID + ", " + LEASE_EXPIRY_TIME +
            " FROM " + INSTRUMENT_RENTAL_TABLE + " WHERE " + STUDENT_ID + " = ?"
        );
    }

    /**
     * Finds all available instruments of a given type.
     */
    public List<InstrumentDTO> findAvailableInstruments(String type) throws InstrumentDBException {
        String failureMsg = "Failed to find available instruments of type: " + type;
        ResultSet result = null;

        try {
            findAvailableInstrumentsStmt.setString(1, type);
            result = findAvailableInstrumentsStmt.executeQuery();
            List<InstrumentDTO> instruments = new ArrayList<>();
            while (result.next()) {
                instruments.add(new Instrument(
                    result.getString(INSTRUMENT_ID),
                    result.getString(INSTRUMENT_TYPE),
                    result.getString(INSTRUMENT_BRAND),
                    result.getDouble(PRICE),
                    result.getInt(AVAILABLE_STOCK)
                ));
            }
            return instruments;
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
            return null;
        } finally {
            if (result != null) {
                closeResultSet(failureMsg, result);
            }
        }
    }

    /**
     * Finds how many instruments a student is currently renting.
     */
    public int findRentalCountByStudent(int studentId) throws InstrumentDBException {
        String failureMsg = "Failed to find rental count for student: " + studentId;
        ResultSet rs = null;
        try {
            findRentalCountByStudentStmt.setInt(1, studentId);
            rs = findRentalCountByStudentStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rental_count");
            }
            return 0;
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
            return 0; // Unreachable
        } finally {
            if (rs != null) {
                closeResultSet(failureMsg, rs);
            }
        }
    }

    /**
     * Creates a new rental record in the database after ensuring the instrument is available.
     */
    public void createRentalOnInstrument(int studentId, String instrumentId, String rentalPriceId) throws InstrumentDBException {
        String failureMsg = "Failed to rent instrument: " + instrumentId + " for student: " + studentId;

        try {
            connection.setAutoCommit(false);

            // Check instrument availability and lock the row
            findAvailableInstrumentByIdLockingForUpdateStmt.setString(1, instrumentId);
            try (ResultSet rs = findAvailableInstrumentByIdLockingForUpdateStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Instrument is not available.");
                }
            }

            // Insert the new rental record
            createRentalStmt.setString(1, generateUniqueId());
            createRentalStmt.setString(2, rentalPriceId);
            createRentalStmt.setString(3, instrumentId);
            createRentalStmt.setInt(4, studentId);
            createRentalStmt.executeUpdate();

            // Update instrument stock
            decreaseInstrumentStockStmt.setString(1, instrumentId);
            decreaseInstrumentStockStmt.executeUpdate();

            commit();
        } catch (SQLException sqle) {
            handleException(failureMsg, sqle);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new InstrumentDBException("Failed to reset auto-commit", e);
            }
        }
    }

    /**
     * Terminates a rental by updating its lease expiry time and the instrument's stock.
     */
    public void deleteRental(String rentalId) throws InstrumentDBException {
        String failureMsg = "Failed to terminate rental: " + rentalId;

        try {
            connection.setAutoCommit(false);

            // Update rental record to terminate
            updateRentalExpiryStmt.setString(1, rentalId);
            updateRentalExpiryStmt.executeUpdate();

            // Update instrument stock after rental termination
            updateInstrumentStockAfterRentalStmt.setString(1, rentalId);
            updateInstrumentStockAfterRentalStmt.executeUpdate();

            commit();
        } catch (SQLException e) {
            handleException(failureMsg, e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new InstrumentDBException("Failed to reset auto-commit", e);
            }
        }
    }

    /**
     * Finds all rentals for a specific student, including their status (active or terminated).
     */
    public List<String> findRentalsByStudent(int studentId) throws InstrumentDBException {
        String failureMsg = "Failed to find rentals for student: " + studentId;
        ResultSet result = null;

        try {
            findRentalsByStudentStmt.setInt(1, studentId);
            result = findRentalsByStudentStmt.executeQuery();

            List<String> rentals = new ArrayList<>();
            while (result.next()) {
                String status = result.getTimestamp(LEASE_EXPIRY_TIME).after(new Timestamp(System.currentTimeMillis())) ? "Active" : "Terminated";
                rentals.add("Rental ID: " + result.getString(RENTAL_ID) +
                            ", Instrument ID: " + result.getString(INSTRUMENT_ID) +
                            ", Status: " + status);
            }

            return rentals;
        } catch (SQLException e) {
            handleException(failureMsg, e);
            return null; // Unreachable
        } finally {
            if (result != null) {
                closeResultSet(failureMsg, result);
            }
        }
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
    private void commit() throws InstrumentDBException {
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