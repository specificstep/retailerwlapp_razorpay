package specificstep.com.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ubuntu on 16/3/17.
 */

public class AccountLedgerModel implements Parcelable {
    public String created_date;
    public String type;
    public String payment_id;
    public String particular;
    public String cr_dr;
    public String amount;
    public String balance;

    public AccountLedgerModel(Parcel in ) {
        readFromParcel( in );
    }

    public AccountLedgerModel() {
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Object createFromParcel(Parcel in ) {
            return new AccountLedgerModel( in );
        }

        public AccountLedgerModel[] newArray(int size) {
            return new AccountLedgerModel[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(created_date);
        dest.writeString(type);
        dest.writeString(payment_id);
        dest.writeString(particular);
        dest.writeString(cr_dr);
        dest.writeString(amount);
        dest.writeString(balance);
    }

    private void readFromParcel(Parcel in) {

        created_date = in .readString();
        type = in .readString();
        payment_id = in .readString();
        particular = in .readString();
        cr_dr = in .readString();
        amount = in .readString();
        balance = in .readString();

    }

}
