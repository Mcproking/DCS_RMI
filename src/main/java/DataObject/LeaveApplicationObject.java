package DataObject;

import Classes.LeaveApplication;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class LeaveApplicationObject implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID token;
    private LeaveApplication leaveApplication;

    public LeaveApplicationObject(UUID token, LeaveApplication leaveApplication){
        this.token = token;
        this.leaveApplication = leaveApplication;
    }

    public UUID getToken() {
        return token;
    }

    public LeaveApplication getLeaveApplication() {
        return leaveApplication;
    }
}
