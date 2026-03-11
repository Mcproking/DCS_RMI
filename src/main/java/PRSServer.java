import Service.*;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class PRSServer {
    public PRSServer(){}

    public static void main(String[] args){
        try{
            LocateRegistry.createRegistry(1100);

            // Database Service
            DatabaseService dbService = new DatabaseServiceImpl();

            // Rebind Service
            AuthService authService = (AuthService) Naming.lookup("rmi://localhost:1099/login");
            PayrollService payrollService = new PayrollServiceImpl(dbService, authService);

            // Add service here
            Naming.rebind("rmi://localhost:1100/payroll", payrollService);

            System.out.println("PRS Server is Running");

        } catch (Exception e){
            System.err.println("Server exception: "+e.toString());
        }
    }
}
