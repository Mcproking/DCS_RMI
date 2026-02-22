import Service.*;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class HRMServer {
    public HRMServer(){
        // have to load the db, i think of just using sqlite3
    }



    public static void main(String[] args){
        try{
            LocateRegistry.createRegistry(1099);

            // Database Service
            DatabaseService dbService = new DatabaseServiceImpl();

            // Rebind Service
            AuthService authService = new AuthServiceImpl(dbService);
            EmployeeService employeeService = new EmployeeServiceImpl(dbService, authService);

            // Add service here
            Naming.rebind("rmi://localhost/login", authService);
            Naming.rebind("rmi://localhost/employee", employeeService);

            System.out.println("HRM Server is Running");

        } catch (Exception e){
            System.err.println("Server exception: "+e.toString());
        }
    }
}
