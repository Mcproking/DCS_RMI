package Classes;

import java.io.Serial;
import java.io.Serializable;

public class Payroll implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String EmpID;
    private int DeductedSalary;
    private int FinalSalary;
    private int Total_Leave;


    public Payroll(String EmpID){
        this.EmpID = EmpID;
    }

    public int getTotal_Leave() {
        return Total_Leave;
    }

    public void setTotal_Leave(int total_Leave) {
        Total_Leave = total_Leave;
    }


    public int getDeductedSalary() {
        return DeductedSalary;
    }

    public void setDeductedSalary(int deductedSalary) {
        DeductedSalary = deductedSalary;
    }



    public int getFinalSalary() {
        return FinalSalary;
    }

    public void setFinalSalary(int finalSalary) {
        FinalSalary = finalSalary;
    }

    public void setEmpID(String empID) {
        EmpID = empID;
    }

    public String getEmpID() {
        return EmpID;
    }
}
