package client;

import common.HRMInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    private static HRMInterface stub;

    public static void connect() {
        try {
            // Find the registry on localhost (or change host if needed)
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            // Lookup the remote object
            stub = (HRMInterface) registry.lookup("HRMService");
            System.out.println("Client connected to RMI Server.");
        } catch (Exception e) {
            System.err.println("Client RMI Connection failed: " + e.getMessage());
            e.printStackTrace();
            // In a real app, perhaps show a dialog and exit
        }
    }

    public static HRMInterface getService() {
        if (stub == null) {
            connect();
        }
        return stub;
    }
}

