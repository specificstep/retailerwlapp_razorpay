package specificstep.com.Models;

public class PaymentRequestDepositBankModel {

    public String id;
    public String bank_name;
    public String balance;
    public String account_number;
    public String user_id;
    public String payee_name;
    public String account_type;
    public String ifsc_code;
    public String branch_name;

    public PaymentRequestDepositBankModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPayee_name() {
        return payee_name;
    }

    public void setPayee_name(String payee_name) {
        this.payee_name = payee_name;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getIfsc_code() {
        return ifsc_code;
    }

    public void setIfsc_code(String ifsc_code) {
        this.ifsc_code = ifsc_code;
    }

    public String getBranch_name() {
        return branch_name;
    }

    public void setBranch_name(String branch_name) {
        this.branch_name = branch_name;
    }

    @Override
    public String toString() {
        return bank_name + " - " + account_number;
    }
}
