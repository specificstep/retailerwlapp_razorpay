package specificstep.com.Models;

public class WalletsModel {

    public String wallet_type;
    public String wallet_name;
    public String balance;

    public WalletsModel() {
    }

    public String getWallet_type() {
        return wallet_type;
    }

    public void setWallet_type(String wallet_type) {
        this.wallet_type = wallet_type;
    }

    public String getWallet_name() {
        return wallet_name;
    }

    public void setWallet_name(String wallet_name) {
        this.wallet_name = wallet_name;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return getWallet_name();
    }
}
