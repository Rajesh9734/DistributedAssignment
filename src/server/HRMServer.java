package server;

import common.HRMInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HRMServer {
    public static void main(String[] args) {
        try {
            // Create the implementation
            HRMInterface stub = new HRMServerImpl();

            // Start the RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Bind the remote object to the name "HRMService"
            registry.rebind("HRMService", stub);

            System.out.println("HRM Server is running...");
            System.out.println("RMI Registry listening on port 1099.");
            System.out.println("Database Initialized.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}

