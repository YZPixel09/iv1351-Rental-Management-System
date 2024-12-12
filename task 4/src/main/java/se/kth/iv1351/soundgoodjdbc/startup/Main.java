package se.kth.iv1351.soundgoodjdbc.startup;


import se.kth.iv1351.soundgoodjdbc.controller.Controller;
import se.kth.iv1351.soundgoodjdbc.view.BlockingInterpreter;
import se.kth.iv1351.soundgoodjdbc.model.InstrumentException;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize the controller, which connects to the database via the DAO
            Controller controller = new Controller();
            
            // Start the command interpreter with the controller
            BlockingInterpreter interpreter = new BlockingInterpreter(controller);
            System.out.println("\n--- Welcome to Soundgood Music Rental System ---");
            interpreter.handleCmds();
        } catch (InstrumentException e) {
            System.err.println("Could not initialize the application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}