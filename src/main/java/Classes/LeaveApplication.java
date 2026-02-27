package Classes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


public class LeaveApplication implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String EmpID;
    private int id;
    private Date startDate;
    private int days;
    private String reason;
    private LeaveStatus status;

    public LeaveApplication(Date startDate, int days, String reason) {
        this.startDate = startDate;
        this.days = days;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
    }

    public LeaveApplication(
            int id, Date startDate, int days, String reason, LeaveStatus staus, String EmpID){
        this.id = id;
        this.startDate = startDate;
        this.days = days;
        this.reason = reason;
        this.status = staus;
        this.EmpID = EmpID;
    }

    public Date getStartDate() {
        return startDate;
    }

    public java.sql.Date getStartDateSql(){
        return new java.sql.Date(startDate.getTime());
    }

    public int getDays() {
        return days;
    }

    public String getReason() {
        return reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus s) {
        this.status = s;
    }

    public int getId(){return this.id;}

    public String getEmpID(){return this.EmpID; }

    @Override
    public String toString() {
        return String.format("Leave: %tD, Days: %d, Status: %s", startDate, days, status);
    }
}
