package Classes;

import java.io.Serial;
import java.io.Serializable;

public class FamilyMember implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private String relationship;

    public FamilyMember(String name, String relationship){
        this.name = name;
        this.relationship = relationship;
    }

    public FamilyMember(int id, String name, String relationship) {
        this.id = id;
        this.name = name;
        this.relationship = relationship;
    }

    public int getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRelationship() {
        return relationship;
    }

    @Override
    public String toString() {
        return name + " (" + relationship + ")";
    }
}
