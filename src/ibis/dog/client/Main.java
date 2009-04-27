package ibis.dog.client;

import ibis.dog.gui.application.ClientPanel;

public class Main {

    public static void main(String[] args) {
        try {

            // Create GUI and Application
            Client c = new Client();
            ClientPanel.createGUI(c);
            
            // Activate the application
            c.start();
            
        } catch (Exception e) {
            System.out.println("FATAL MyApp ERROR");
            e.printStackTrace();
        }
    } 
}
