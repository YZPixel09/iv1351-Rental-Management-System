package se.kth.iv1351.soundgoodjdbc.view;

/**
 * Defines all commands that can be performed by a user of the instrument rental system.
 */
public enum Command {
    /**
     * Lists all available instruments of a specified type.
     */
    LIST_AVAILABLE,
    /**
     * Rents an instrument for a student.
     */
    RENT,
    /**
     * Terminates a rental.
     */
    TERMINATE_RENTAL,
    /**
     * Lists all rentals for a specific student.
     */
    LIST_RENTALS,
    /**
     * Lists all commands.
     */
    HELP,
    /**
     * Exits the application.
     */
    QUIT,
    /**
     * None of the valid commands above was specified.
     */
    ILLEGAL_COMMAND
}