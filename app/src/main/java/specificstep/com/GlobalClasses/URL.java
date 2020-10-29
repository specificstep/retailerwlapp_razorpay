package specificstep.com.GlobalClasses;

/**
 * Created by ubuntu on 12/1/17.
 */

public class URL {

    //public static String base_url = "http://192.168.30.117:8056/webservices/"; //swami agencies url
    public static String base_url = "http://www.naaradpay.in/webservices/"; //naaradpay recharge group url

    public static String register = base_url + "register";
    public static String company = base_url + "company";
    public static String product = base_url + "product";
    public static String state = base_url + "state";
    public static String login = base_url + "login";
    public static String forgot_password = base_url + "forgotpassword";
    public static String recharge = base_url + "recharge";
    public static String electricity_recharge = base_url + "bbps";
    public static String electricity_recharge_bill = base_url + "bbps/checkbill";
    public static String electricity_customer_info = base_url + "bbps/customerinfo";
    public static String number_tracer = base_url + "numbertracer";
    public static String search_recharge = base_url + "searchrecharge";
    public static String latest_recharge = base_url + "latestrecharge";
    public static String balance = base_url + "balance";
    public static String setting = base_url + "setting";
    public static String offerplan = base_url + "numbertracer/offerplan";
    public static String skipotp = base_url + "skipotp";
    public static String complainReason = base_url + "complainlist/reason";

    public static String changePassword = base_url + "changepass";
    public static String cashBook = base_url + "cashbook";
    public static String accountLedger = base_url + "accounts";


    public static String complain = base_url + "complain";
    public static String complainList = base_url + "complainlist";

    // 2017_05_02 - get parent user details URL
    public static String GET_PARENT_USER_DETAILS = base_url + "getparent";

    // 2017_05_29 - Get browse plan type and it's type URL
    public static String GET_PLANS_TYPE = base_url + "Getplantype";
    public static String GET_PLANS = base_url + "Getplans";

    //2018_10_30 - forgot password URL
    public static String GET_FORGOT_OTP = base_url + "forgototp";
    public static String GET_FORGOT_CHANGE_PASSWORD = base_url + "newpass";

    // - Get name using mobile number or dth number


    //2018_12_3 - DMT URL
    public static String searchSender = base_url + "dmt/searchsender";
    public static String addSender = base_url + "dmt/addsender";
    public static String addBenefitiary = base_url + "dmt/addbeneficiary";
    public static String verifySender = base_url + "dmt/verifysender";
    public static String getIfscCode = base_url + "dmt/ifsccode";
    public static String getBankName = base_url + "dmt/bank";
    public static String getBranchName = base_url + "dmt/branch";
    public static String transfer = base_url + "dmt/transfer";
    public static String deleteBeneficiary = base_url + "dmt/deletebeneficiary";
    public static String verifyBeneficiary = base_url + "dmt/verifybeneficiary";
    public static String transactionList = base_url + "dmt/transactionlists";
    public static String resendDmtOtp = base_url + "dmt/resendsenderotp";
    public static String senderLimit = base_url + "dmt/senderlimit";

    public static String paymentRequestList = base_url + "paymentreqlist";

    //2018_12_19 - Alert
    public static String getAlert = base_url + "alert";

    //2018_12_31 - Service
    public static String getService = base_url + "service";

    //2019_2_18 - Payment Request
    public static String walletType = base_url + "wallets";
    public static String addcompanybank = base_url + "companybank";
    public static String addbank = base_url + "bank";
    public static String addpaymentrequest = base_url + "paymentrequest";
    public static String razorpay_paymentrequest = base_url + "paymentrequest/status";

    public static String notification_click = base_url + "notification";

    //2019_12_3 - Banner
    public static String banner = base_url + "banner";

    public URL(String strURL) {
    }
}
