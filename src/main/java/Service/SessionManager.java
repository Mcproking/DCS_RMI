package Service;

import Classes.Employee;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static Map<UUID, Employee> sessions = new ConcurrentHashMap<>();

    public static UUID createSession(UUID token, Employee employee) {
        boolean exist = false;
        // Check if employee already has a session
        for (Map.Entry<UUID, Employee> entry : sessions.entrySet()) {

            Employee existingEmp = entry.getValue();

            if (existingEmp.getIdNumber().equals(employee.getIdNumber())) {
                System.out.println("Session already exists for employee.");
                // change this emp token with the pass-in employee
                entry.setValue(employee);
                return entry.getKey(); // return existing token
            }
        }


        sessions.put(token, employee);

        System.out.println("Session Created. " + token + " | " + employee.getFirstName());

        return token;
    }

    public static Employee validate(UUID token) {
        Employee emp = sessions.get(token);
        if (emp == null) {
            throw new SecurityException("Invalid session");
        }
        return emp;
    }

    public static void removeSession(UUID token){
        sessions.remove(token);
        System.out.println(token + " Session Removed.");
    }



}
