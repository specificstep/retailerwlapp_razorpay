package specificstep.com.Models;

public class DMTAddBenefitiaryBankName {

    public String bank_id;
    public String bank_name;
    public String ifsc_code;

    public DMTAddBenefitiaryBankName() {
    }

    public String getBank_id() {
        return bank_id;
    }

    public void setBank_id(String bank_id) {
        this.bank_id = bank_id;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getIfsc_code() {
        return ifsc_code;
    }

    public void setIfsc_code(String ifsc_code) {
        this.ifsc_code = ifsc_code;
    }

    @Override
    public String toString() {
        return getBank_name().toString();
    }
}
