package specificstep.com.Models;

import java.util.ArrayList;

public class DMTSenderModel {

    public String id = "";
    public String firstname = "";
    public String lastname = "";
    public String mobilenumber = "";
    public String email_address = "";
    public String dob = "";
    public String pincode = "";
    public String status = "";
    ArrayList<DMTSenderBeneficiaryModel> beneficiaryModels;

    public DMTSenderModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public ArrayList<DMTSenderBeneficiaryModel> getBeneficiaryModels() {
        return beneficiaryModels;
    }

    public void setBeneficiaryModels(ArrayList<DMTSenderBeneficiaryModel> beneficiaryModels) {
        this.beneficiaryModels = beneficiaryModels;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
