package specificstep.com.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ubuntu on 16/3/17.
 */

public class CashbookModel implements Parcelable {
    public String paymentId;
    public String paymentFrom;
    public String paymentTo;
    public String userType;
    public String amount;
    public String balance;
    public String remarks;
    public String dateTime;

    @Override
    public int describeContents() {
        return 0;
    }

    public CashbookModel(Parcel in ) {
        readFromParcel( in );
    }

    public CashbookModel() {
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Object createFromParcel(Parcel in ) {
            return new CashbookModel( in );
        }

        public CashbookModel[] newArray(int size) {
            return new CashbookModel[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(paymentId);
        dest.writeString(paymentFrom);
        dest.writeString(paymentTo);
        dest.writeString(userType);
        dest.writeString(amount);
        dest.writeString(balance);
        dest.writeString(remarks);
        dest.writeString(dateTime);
    }

    private void readFromParcel(Parcel in) {

        paymentId = in .readString();
        paymentFrom = in .readString();
        paymentTo = in .readString();
        userType = in .readString();
        amount = in .readString();
        balance = in .readString();
        remarks = in .readString();
        dateTime = in .readString();

    }

}
