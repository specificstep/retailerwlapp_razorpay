package specificstep.com.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ubuntu on 19/1/17.
 */

public class Recharge implements Parcelable {

    String client_trans_id;
    String mo_no;
    String amount;
    String compnay_name;
    String product_name;
    String trans_date_time;
    String status;
    String recharge_status;
    String operator_trans_id;

    public Recharge(Parcel in ) {
        readFromParcel( in );
    }

    public Recharge() {
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Object createFromParcel(Parcel in ) {
            return new Recharge( in );
        }

        public Recharge[] newArray(int size) {
            return new Recharge[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(client_trans_id);
        dest.writeString(mo_no);
        dest.writeString(amount);
        dest.writeString(compnay_name);
        dest.writeString(product_name);
        dest.writeString(trans_date_time);
        dest.writeString(status);
        dest.writeString(recharge_status);
        dest.writeString(operator_trans_id);
    }

    private void readFromParcel(Parcel in) {

        client_trans_id = in .readString();
        mo_no = in .readString();
        amount = in .readString();
        compnay_name = in .readString();
        product_name = in .readString();
        trans_date_time = in .readString();
        status = in .readString();
        recharge_status = in .readString();
        operator_trans_id = in .readString();

    }


    public String getClient_trans_id() {
        return client_trans_id;
    }

    public void setClient_trans_id(String client_trans_id) {
        this.client_trans_id = client_trans_id;
    }

    public String getMo_no() {
        return mo_no;
    }

    public void setMo_no(String mo_no) {
        this.mo_no = mo_no;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCompnay_name() {
        return compnay_name;
    }

    public void setCompnay_name(String compnay_name) {
        this.compnay_name = compnay_name;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getTrans_date_time() {
        return trans_date_time;
    }

    public void setTrans_date_time(String trans_date_time) {
        this.trans_date_time = trans_date_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecharge_status() {
        return recharge_status;
    }

    public void setRecharge_status(String recharge_status) {
        this.recharge_status = recharge_status;
    }

    public String getOperator_trans_id() {
        return operator_trans_id;
    }

    public void setOperator_trans_id(String operator_trans_id) {
        this.operator_trans_id = operator_trans_id;
    }
}
