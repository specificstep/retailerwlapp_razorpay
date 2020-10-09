package specificstep.com.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentRequestListModel implements Parcelable {

    String datetime;
    String amount;
    String wallet_name;
    String deposit_bank;
    String status;
    String remark;
    String admin_remark;

    public PaymentRequestListModel() {
    }

    public static final Creator<PaymentRequestListModel> CREATOR = new Creator<PaymentRequestListModel>() {
        @Override
        public PaymentRequestListModel createFromParcel(Parcel in) {
            return new PaymentRequestListModel(in);
        }

        @Override
        public PaymentRequestListModel[] newArray(int size) {
            return new PaymentRequestListModel[size];
        }
    };

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getWallet_name() {
        return wallet_name;
    }

    public void setWallet_name(String wallet_name) {
        this.wallet_name = wallet_name;
    }

    public String getDeposit_bank() {
        return deposit_bank;
    }

    public void setDeposit_bank(String deposit_bank) {
        this.deposit_bank = deposit_bank;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAdmin_remark() {
        return admin_remark;
    }

    public void setAdmin_remark(String admin_remark) {
        this.admin_remark = admin_remark;
    }

    public PaymentRequestListModel(Parcel in ) {
        readFromParcel( in );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(datetime);
        dest.writeString(amount);
        dest.writeString(wallet_name);
        dest.writeString(deposit_bank);
        dest.writeString(status);
        dest.writeString(remark);
        dest.writeString(admin_remark);
    }

    private void readFromParcel(Parcel in) {
        datetime = in .readString();
        amount = in .readString();
        wallet_name = in .readString();
        deposit_bank = in .readString();
        status = in .readString();
        remark = in .readString();
        admin_remark = in .readString();
    }

}
