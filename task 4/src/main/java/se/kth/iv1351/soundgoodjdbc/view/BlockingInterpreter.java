package se.kth.iv1351.soundgoodjdbc.view;

import java.util.List;
import java.util.Scanner;
import se.kth.iv1351.soundgoodjdbc.controller.Controller;
import se.kth.iv1351.soundgoodjdbc.model.InstrumentDTO;

/**
 * Reads and interprets user commands. This command interpreter is blocking, the user
 * interface does not react to user input while a command is being executed.
 */
public class BlockingInterpreter {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private final Controller ctrl;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all operations.
     * 
     * @param ctrl The controller used by this instance.
     */
    public BlockingInterpreter(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Stops the command interpreter.
     */
    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands. This method will not return until the
     * UI has been stopped. The UI is stopped either when the user gives the
     * "quit" command, or when the method <code>stop()</code> is called.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        while (keepReceivingCmds) {
            try {
                printMenu(); // Show the menu before each command
                int choice = Integer.parseInt(readNextLine());

                switch (choice) {
                    case 1: // List available instruments
                        System.out.print("Enter instrument type: ");
                        String type = readNextLine();
                        if (type == null || type.isEmpty()) {
                            System.out.println("Instrument type cannot be empty.");
                            break;
                        }
                        List<? extends InstrumentDTO> instruments = ctrl.listAvailableInstruments(type);
                        if (instruments.isEmpty()) {
                            System.out.println("No available instruments of type: " + type);
                        } else {
                            System.out.println("Available instruments:");
                            for (InstrumentDTO instrument : instruments) {
                                System.out.println(instrument);
                            }
                        }
                        break;
                    case 2: // Rent an instrument
                        System.out.print("Enter student ID: ");
                        int studentId = Integer.parseInt(readNextLine());
                        System.out.print("Enter instrument ID: ");
                        String instrumentId = readNextLine();
                        System.out.print("Enter rental price ID: ");
                        String rentalPriceId = readNextLine();
                        ctrl.rentInstrument(studentId, instrumentId, rentalPriceId);
                        System.out.println("Instrument rented successfully.");
                        break;
                    case 3: // Terminate a rental
                        System.out.print("Enter rental ID: ");
                        String rentalId = readNextLine();
                        ctrl.terminateRental(rentalId);
                        System.out.println("Rental terminated successfully.");
                        break;
                    case 4: // List rentals for a student
                        System.out.print("Enter student ID: ");
                        int rentalStudentId = Integer.parseInt(readNextLine());
                        List<String> rentals = ctrl.listRentalsForStudent(rentalStudentId);
                        if (rentals.isEmpty()) {
                            System.out.println("No rentals found for student ID: " + rentalStudentId);
                        } else {
                            System.out.println("Rentals:");
                            for (String rental : rentals) {
                                System.out.println(rental);
                            }
                        }
                        break;
                    case 5: // Exit the program
                        keepReceivingCmds = false;
                        System.out.println("Exiting... Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number corresponding to a menu option.");
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void printMenu() {
        System.out.println("1. List available instruments");
        System.out.println("2. Rent an instrument");
        System.out.println("3. Terminate a rental");
        System.out.println("4. Check student rentals");
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");
    }

    private String readNextLine() {
        System.out.print(PROMPT);
        return console.nextLine().trim();
    }
}