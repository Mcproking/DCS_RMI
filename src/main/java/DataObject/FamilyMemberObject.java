package DataObject;

import Classes.FamilyMember;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class FamilyMemberObject implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID token;
    private FamilyMember fm;

    public FamilyMemberObject(UUID token, FamilyMember fm){
        this.token = token;
        this.fm = fm;
    }

    public UUID getToken() {
        return token;
    }

    public FamilyMember getFm() {
        return fm;
    }
}
