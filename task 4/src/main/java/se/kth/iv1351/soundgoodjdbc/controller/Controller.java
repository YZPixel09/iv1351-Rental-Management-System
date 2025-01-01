package se.kth.iv1351.soundgoodjdbc.controller;
/**
 * Controller class bridging the gap between the view and the model.
 */
import java.util.List;
import se.kth.iv1351.soundgoodjdbc.integration.InstrumentDAO;
import se.kth.iv1351.soundgoodjdbc.integration.InstrumentDBException;
import se.kth.iv1351.soundgoodjdbc.model.InstrumentDTO;
import se.kth.iv1351.soundgoodjdbc.model.InstrumentException;


public class Controller {
    private final InstrumentDAO instrumentDAO;

    /**
     * Creates a new instance, and initializes the DAO connection.
     *
     * @throws InstrumentException If unable to connect to the database.
     */
    public Controller() throws InstrumentException {
        try {
            this.instrumentDAO = new InstrumentDAO();
        } catch (InstrumentDBException e) {
            throw new InstrumentException("Could not initialize database connection.", e);
        }
    }

    /**
     * Lists all available instruments of a given type.
     *
     * @param type The type of instrument to list.
     * @return A list of available instruments.
     * @throws InstrumentException If unable to retrieve instruments.
     */
    public List<? extends InstrumentDTO> listAvailableInstruments(String type) throws InstrumentException {
        String failureMsg = "Could not list available instruments of type: " + type;

        if (type == null || type.isEmpty()) {
            throw new InstrumentException("Instrument type cannot be null or empty.");
        }

        try {
            return instrumentDAO.findAvailableInstruments(type);
        } catch (InstrumentDBException e) {
            throw new InstrumentException(failureMsg, e);
        }
    }

    /**
     * Rents an instrument for a student.
     *
     * @param studentId    The ID of the student renting the instrument.
     * @param instrumentId The ID of the instrument to rent.
     * @param rentalPriceId The ID of the rental price to apply.
     * @throws InstrumentException If unable to rent the instrument (e.g., database trigger
 *      detects rule violations or an error occurs in the DAO).
     */
    public void rentInstrument(int studentId, String instrumentId, String rentalPriceId) throws InstrumentException {
        String failureMsg = "Could not rent instrument with ID: " + instrumentId + " for student: " + studentId;

        if (instrumentId == null || rentalPriceId == null) {
            throw new InstrumentException("Instrument ID and rental price ID cannot be null.");
        }

        try {
           // Delegates the rental creation to the DAO. 
           // Database trigger ensures rental rules (e.g., max active rentals) are enforced.
            instrumentDAO.createRentalOnInstrument(studentId, instrumentId, rentalPriceId);
        } catch (InstrumentDBException e) {
            throw new InstrumentException(failureMsg, e);
        }
    }

    /**
     * Terminates a rental.
     *
     * @param rentalId The ID of the rental to terminate.
     * @throws InstrumentException If unable to terminate the rental.
     */
    public void terminateRental(String rentalId) throws InstrumentException {
        String failureMsg = "Could not terminate rental with ID: " + rentalId;

        if (rentalId == null || rentalId.isEmpty()) {
            throw new InstrumentException("Rental ID cannot be null or empty.");
        }

        try {
            instrumentDAO.deleteRental(rentalId);
        } catch (InstrumentDBException e) {
            throw new InstrumentException(failureMsg, e);
        }
    }

    /**
     * Lists all rentals for a specific student.
     *
     * @param studentId The ID of the student whose rentals to list.
     * @return A list of rentals for the student.
     * @throws InstrumentException If unable to retrieve the student's rentals.
     */
    public List<String> listRentalsForStudent(int studentId) throws InstrumentException {
        String failureMsg = "Could not list rentals for student ID: " + studentId;

        try {
            return instrumentDAO.findRentalsByStudent(studentId);
        } catch (InstrumentDBException e) {
            throw new InstrumentException(failureMsg, e);
        }
    }
}
