package Classes;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String message;
    private String employeeId;
    private boolean isRead;
    private Date createdAt;

    public Notification() {
    }

    public Notification(int id, String message, String employeeId, boolean isRead, Date createdAt) {
        this.id = id;
        this.message = message;
        this.employeeId = employeeId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
