package DataObject;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String UserID;
    private String Password; // TODO: refactor this to hashed password

    public LoginRequest(String UserID, String Password){
        this.UserID = UserID;
        this.Password = Password;
    }

    public String getUserID(){
        return  UserID;
    }

    public String getPassword() {
        return Password;
    }
}
