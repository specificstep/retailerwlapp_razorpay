package specificstep.com.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.razorpay.Payment;
import com.razorpay.PaymentResultListener;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import specificstep.com.Adapters.PaymentRequestListAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.PaymentRequestListModel;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.MyPrefs;
import specificstep.com.utility.Utility;

public class OnlinePaymentActivity extends AppCompatActivity implements PaymentResultListener {

    public static EditText edtMobile, edtamount;
    Button btnSubmit;
    ImageButton btnBack, imgWallet;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS_RAZORPAY = 3, SUCCESS_LOAD = 0, ERROR = 1, SUCCESS_WALLET_LIST = 2;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    //multi wallet 3-5-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    ArrayList<String> menuWallet;
    private SharedPreferences sharedPreferences;
    private MyPrefs prefs;
    private Constants constants;
    public static Context context;
    private String strMacAddress, strUserName, strOtpCode, strRegistrationDateTime;
    String TAG = "OnlinePaymentActivity :: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_payment);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        init();
        ClickListner();
        try {
            if (Constants.checkInternet(OnlinePaymentActivity.this)) {
                makeWalletCall();
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }

    private void ClickListner() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (valid()) {

                    makesendamountcall();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    public void makeWalletCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.walletType;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_WALLET_LIST, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    // dismiss progress dialog
    private void dismissProgressDialog() {
        try {
            if (transparentProgressDialog != null) {
                if (transparentProgressDialog.isShowing())
                    transparentProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            if (!alertDialog.isShowing()) {
                alertDialog.setTitle("Info!");
                alertDialog.setCancelable(false);
                alertDialog.setMessage(message);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(OnlinePaymentActivity.this, message);
            } catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_LOAD) {
                parseSuccessResponse(msg.obj.toString());
                dismissProgressDialog();
            } else if (msg.what == SUCCESS_RAZORPAY) {
                parseSuccess_razorpayResponse(msg.obj.toString());
                dismissProgressDialog();
            }
        }
    };

    public void parseSuccessResponse(String response) {
        Dlog.d("payment request Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                Log.e(TAG, "Message : " + message);
                Log.e(TAG, "Message : " + message);

                Log.e(TAG, "Payment Request : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context, encrypted_response);

                Log.e(TAG, "Payment Request : " + "decrypted_response : " + decrypted_response);

                JSONObject object = new JSONObject(decrypted_response);
                String payment_id = object.getString("data");
                prefs.saveString(constants.PREF_PAYMENT_UNIQUE_ID, payment_id);

                startPayment(payment_id);

//                String request_id = object.getString("request_id");

//                displayErrorDialog(msg);


            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Payment Request : " + "Error 4 : " + e.getMessage());
            Utility.toast(this, "No result found");
            e.printStackTrace();
        }

    }

    public void parseSuccess_razorpayResponse(String response) {
        Dlog.d("payment request Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
//                String encrypted_response = jsonObject.getString("msg");
//                String message = jsonObject.getString("msg");
//                Log.e(TAG, "Message : " + message);
//                Log.e(TAG, "Message : " + message);
//
//                Log.e(TAG, "Payment Request : " + "encrypted_response : " + encrypted_response);
//                String decrypted_response = Constants.decryptAPI(context, encrypted_response);
//
//                Log.e(TAG, "Payment Request : " + "decrypted_response : " + decrypted_response);
//
//                JSONObject object = new JSONObject(decrypted_response);
//                String msg = object.getString("msg");
                edtamount.setText("");
                edtMobile.setText("");
                Toast.makeText(this, "Payment Done Successfully", Toast.LENGTH_SHORT)
                        .show();
//                displayErrorDialog(msg);


            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Payment Request : " + "Error 4 : " + e.getMessage());
            Utility.toast(this, "No result found");
            e.printStackTrace();
        }

    }

    public void startPayment(String payment_id) {

        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = this;

        final Checkout co = new Checkout();
        int image = R.mipmap.ic_launcher; // Can be any drawablecheckout.setImage(image);
//        co.setImage(image);
        String amount = String.valueOf(Integer.valueOf(edtamount.getText().toString()) * 100);
        try {
            JSONObject options = new JSONObject();
            options.put("name", getApplicationContext().getString(R.string.app_name));
            options.put("description", payment_id);
            options.put("theme.color", "#FF9800");
            options.put("currency", "INR");
            options.put("amount", amount);
            JSONObject preFill = new JSONObject();
            preFill.put("email", "test@razorpay.com");
            preFill.put("contact", edtMobile.getText().toString());

            options.put("prefill", preFill);

            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    public void parseSuccessWalletResponse(String response) {
        Dlog.d("Wallet Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(OnlinePaymentActivity.this, encrypted_response);
                Dlog.d("Wallet : " + "decrypted_response : " + decrypted_response);
                JSONArray array = new JSONArray(decrypted_response);
                walletsModelList = new ArrayList<WalletsModel>();
                walletsList = new ArrayList<String>();
                menuWallet = new ArrayList<String>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    walletsModel = new WalletsModel();
                    walletsModel.setWallet_type(object.getString("wallet_type"));
                    walletsModel.setWallet_name(object.getString("wallet_name"));
                    walletsModel.setBalance(object.getString("balance"));
                    walletsModelList.add(walletsModel);
                    walletsList.add(object.getString("wallet_name"));
                    menuWallet.add(object.getString("wallet_name") + " : " + getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                }

                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                if (walletsModelList.size() > 0) {
                    imgWallet.setVisibility(View.VISIBLE);
                    imgWallet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Constants.checkInternet(OnlinePaymentActivity.this)) {
                                Constants.showWalletPopup(OnlinePaymentActivity.this);
                            } else {
                                //Constants.showNoInternetDialog(PaymentRequestListActivity.this);
                            }
                        }
                    });
                } else {
                    imgWallet.setVisibility(View.INVISIBLE);
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(OnlinePaymentActivity.this, R.drawable.fotterloading);
            }
            if (transparentProgressDialog != null) {
                if (!transparentProgressDialog.isShowing()) {
                    transparentProgressDialog.show();
                }
            }
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // dismiss progress dialog

    public void makesendamountcall() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.addpaymentrequest;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "amount",
                            "payment_mode"

                    };
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            edtamount.getText().toString(),
                            "4"
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_LOAD, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        }).start();

    }

    public void makerazorpaycall(String razorpayid, String paymentid, String amount) {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.razorpay_paymentrequest;
                    String[] parameters = {
                            "razorpayid",
                            "paymentid",
                            "amount"


                    };
                    String[] parametersValues = {
                            razorpayid,
                            paymentid,
                            amount

                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_RAZORPAY, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }
        }).start();

    }

    public boolean valid() {

        if (TextUtils.isEmpty(edtMobile.getText().toString())) {
            Toast.makeText(this, "Please enter Mobile No.", Toast.LENGTH_LONG).show();
            return false;
        } else if (edtMobile.getText().toString().length() < 10) {
            Toast.makeText(this, "Please enter valid Mobile No.", Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(edtamount.getText().toString())) {
            Toast.makeText(this, "Please enter Amount.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }

    }

    private void init() {


        context = OnlinePaymentActivity.this;

        Checkout.preload(getApplicationContext());
        constants = new Constants();

        sharedPreferences = getSharedPreferences(Constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        prefs = new MyPrefs(getContextInstance(), constants.PREF_NAME);

        databaseHelper = new DatabaseHelper(OnlinePaymentActivity.this);
        userArrayList = databaseHelper.getUserDetail();
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        edtMobile = (EditText) findViewById(R.id.edtMobile);
        edtamount = (EditText) findViewById(R.id.edtamount);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        alertDialog = new AlertDialog.Builder(OnlinePaymentActivity.this).create();

        btnBack = (ImageButton) findViewById(R.id.btnPaymentRequestDetailBack);
        imgWallet = (ImageButton) findViewById(R.id.imgPaymentRequestListWallet);

    }

    private Context getContextInstance() {
        if (context == null) {
            context = OnlinePaymentActivity.this;
            return context;
        } else {
            return context;
        }
    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
//            Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();
            getData(razorpayPaymentID);
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e(TAG, "Exception in onPaymentSuccess", e);
        }
    }

    private void getData(String razopay_id) {

        RazorpayClient razorpayClient = null;
        try {
            //test client data
//            razorpayClient = new RazorpayClient("rzp_test_HxRTUQuFDXfrx0", "B9WCrLpE9JeGQjh3O7oVVIpe");
            //live data
            razorpayClient = new RazorpayClient("rzp_live_ZN3un6gOPmSzRG", "OofwiNBzBxEdjttLbhRoe9wf");

//            razorpayClient = new RazorpayClient("rzp_test_tzuoolyalWiPY5", "bCjf019Ltnr4b8MULX2HGdCo");
            Payment payment = razorpayClient.Payments.fetch(razopay_id);

            int amount_int = payment.get("amount");
            String email = payment.get("email");
            String contact = payment.get("contact");
            String method = payment.get("method");
            String status = payment.get("status");
            Log.d("amount", String.valueOf(amount_int / 100));
            Log.d("payment", String.valueOf(payment));
            String amount = String.valueOf(amount_int / 100);
            Date date = payment.get("created_at");
            SimpleDateFormat simpledateformate = new SimpleDateFormat("yyyy-MM-dd");

            String DATE = simpledateformate.format(date);
            String payment_id = prefs.retriveString(constants.PREF_PAYMENT_UNIQUE_ID, "0");

            makerazorpaycall(razopay_id, payment_id, amount);

            Log.d("dateString", DATE);
            edtMobile.setText("");
            edtamount.setText("");
        } catch (RazorpayException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.d("notdone", "fail");
        try {
            edtMobile.setText("");
            edtamount.setText("");
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT)
                    .show();
//            Toast.makeText(this, "Payment failed: " + i + "" + s, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Exception PaymentError", String.valueOf(e));
        }
    }
}