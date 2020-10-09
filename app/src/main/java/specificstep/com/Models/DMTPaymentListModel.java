package specificstep.com.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class DMTPaymentListModel implements Parcelable {

    public String beneficiary_first_name;
    public String beneficiary_last_name;
    public String beneficiary_mobile_number;
    public String tblsendid;
    public String sender_firstname;
    public String sender_lastname;
    public String sender_mobilenumber;
    public String sender_altmobilenumber;
    public String sender_email_address;
    public String tbltransid;
    public String trans_id;
    public String tblsender_id;
    public String vendor_id;
    public String amount;
    public String transaction_id;
    public String provider_id;
    public String bank;
    public String tblbeneficiary_id;
    public String tblapi_id;
    public String tbluser_id;
    public String api_brid;
    public String transaction_status;
    public String add_date;
    public String fees;
    public String gst;
    public String gst_unclaim;
    public String tds;
    public String com;
    public String firm_name;
    public String account_number;

    public DMTPaymentListModel() {
    }

    public DMTPaymentListModel(Parcel in ) {
        readFromParcel( in );
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Object createFromParcel(Parcel in ) {
            return new DMTPaymentListModel( in );
        }

        public DMTPaymentListModel[] newArray(int size) {
            return new DMTPaymentListModel[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(beneficiary_first_name);
        dest.writeString(beneficiary_last_name);
        dest.writeString(beneficiary_mobile_number);
        dest.writeString(tblsendid);
        dest.writeString(sender_firstname);
        dest.writeString(sender_lastname);
        dest.writeString(sender_mobilenumber);
        dest.writeString(sender_altmobilenumber);
        dest.writeString(sender_email_address);
        dest.writeString(tbltransid);
        dest.writeString(trans_id);
        dest.writeString(tblsender_id);
        dest.writeString(vendor_id);
        dest.writeString(amount);
        dest.writeString(transaction_id);
        dest.writeString(provider_id);
        dest.writeString(bank);
        dest.writeString(tblbeneficiary_id);
        dest.writeString(tblapi_id);
        dest.writeString(tbluser_id);
        dest.writeString(api_brid);
        dest.writeString(transaction_status);
        dest.writeString(add_date);
        dest.writeString(fees);
        dest.writeString(gst);
        dest.writeString(gst_unclaim);
        dest.writeString(tds);
        dest.writeString(com);
        dest.writeString(firm_name);
        dest.writeString(account_number);
    }

    private void readFromParcel(Parcel in) {

        beneficiary_first_name = in .readString();
        beneficiary_last_name = in .readString();
        beneficiary_mobile_number = in .readString();
        tblsendid = in .readString();
        sender_firstname = in .readString();
        sender_lastname = in .readString();
        sender_mobilenumber = in .readString();
        sender_altmobilenumber = in .readString();
        sender_email_address = in .readString();
        tbltransid = in .readString();
        trans_id = in .readString();
        tblsender_id = in .readString();
        vendor_id = in .readString();
        amount = in .readString();
        transaction_id = in .readString();
        provider_id = in .readString();
        bank = in .readString();
        tblbeneficiary_id = in .readString();
        tblapi_id = in .readString();
        tbluser_id = in .readString();
        api_brid = in .readString();
        transaction_status = in .readString();
        add_date = in .readString();
        fees = in.readString();
        gst = in.readString();
        gst_unclaim = in.readString();
        tds = in.readString();
        com = in.readString();
        firm_name = in.readString();
        account_number = in.readString();
    }


    public String getBeneficiary_first_name() {
        return beneficiary_first_name;
    }

    public void setBeneficiary_first_name(String beneficiary_first_name) {
        this.beneficiary_first_name = beneficiary_first_name;
    }

    public String getBeneficiary_last_name() {
        return beneficiary_last_name;
    }

    public void setBeneficiary_last_name(String beneficiary_last_name) {
        this.beneficiary_last_name = beneficiary_last_name;
    }

    public String getBeneficiary_mobile_number() {
        return beneficiary_mobile_number;
    }

    public void setBeneficiary_mobile_number(String beneficiary_mobile_number) {
        this.beneficiary_mobile_number = beneficiary_mobile_number;
    }

    public String getTblsendid() {
        return tblsendid;
    }

    public void setTblsendid(String tblsendid) {
        this.tblsendid = tblsendid;
    }

    public String getSender_firstname() {
        return sender_firstname;
    }

    public void setSender_firstname(String sender_firstname) {
        this.sender_firstname = sender_firstname;
    }

    public String getSender_lastname() {
        return sender_lastname;
    }

    public void setSender_lastname(String sender_lastname) {
        this.sender_lastname = sender_lastname;
    }

    public String getSender_mobilenumber() {
        return sender_mobilenumber;
    }

    public void setSender_mobilenumber(String sender_mobilenumber) {
        this.sender_mobilenumber = sender_mobilenumber;
    }

    public String getSender_altmobilenumber() {
        return sender_altmobilenumber;
    }

    public void setSender_altmobilenumber(String sender_altmobilenumber) {
        this.sender_altmobilenumber = sender_altmobilenumber;
    }

    public String getSender_email_address() {
        return sender_email_address;
    }

    public void setSender_email_address(String sender_email_address) {
        this.sender_email_address = sender_email_address;
    }

    public String getTbltransid() {
        return tbltransid;
    }

    public void setTbltransid(String tbltransid) {
        this.tbltransid = tbltransid;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }

    public String getTblsender_id() {
        return tblsender_id;
    }

    public void setTblsender_id(String tblsender_id) {
        this.tblsender_id = tblsender_id;
    }

    public String getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(String vendor_id) {
        this.vendor_id = vendor_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(String provider_id) {
        this.provider_id = provider_id;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getTblbeneficiary_id() {
        return tblbeneficiary_id;
    }

    public void setTblbeneficiary_id(String tblbeneficiary_id) {
        this.tblbeneficiary_id = tblbeneficiary_id;
    }

    public String getTblapi_id() {
        return tblapi_id;
    }

    public void setTblapi_id(String tblapi_id) {
        this.tblapi_id = tblapi_id;
    }

    public String getTbluser_id() {
        return tbluser_id;
    }

    public void setTbluser_id(String tbluser_id) {
        this.tbluser_id = tbluser_id;
    }

    public String getApi_brid() {
        return api_brid;
    }

    public void setApi_brid(String api_brid) {
        this.api_brid = api_brid;
    }

    public String getTransaction_status() {
        return transaction_status;
    }

    public void setTransaction_status(String transaction_status) {
        this.transaction_status = transaction_status;
    }

    public String getAdd_date() {
        return add_date;
    }

    public void setAdd_date(String add_date) {
        this.add_date = add_date;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getGst() {
        return gst;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

    public String getGst_unclaim() {
        return gst_unclaim;
    }

    public void setGst_unclaim(String gst_unclaim) {
        this.gst_unclaim = gst_unclaim;
    }

    public String getTds() {
        return tds;
    }

    public void setTds(String tds) {
        this.tds = tds;
    }

    public String getCom() {
        return com;
    }

    public void setCom(String com) {
        this.com = com;
    }

    public String getFirm_name() {
        return firm_name;
    }

    public void setFirm_name(String firm_name) {
        this.firm_name = firm_name;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }
}
