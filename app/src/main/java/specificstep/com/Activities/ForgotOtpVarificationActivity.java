package specificstep.com.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

public class ForgotOtpVarificationActivity extends Activity implements View.OnClickListener {
    /* Other class objects */
    private Context context;
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private DatabaseHelper databaseHelper;
    private TransparentProgressDialog transparentProgressDialog;
    private BroadcastReceiver messageReadReceiver;

    /* All local int and string variables */
    private final int SUCCESS_OTP_VERIFICATION = 1, ERROR_OTP_VERIFICATION = 2, ERROR = 3,
            SUCCESS_REGISTER_USER = 7, ERROR_REGISTER_USER = 8, ERROR_TOAST = 9,
            AUTHENTICATION_FAIL = 10;
    private final int PERMISSION_REQUEST_CODE = 123;
    private String strOtp, strUserName, strToken, strAppOtp;

    /* All views */
    private EditText edtOtp;
    private Button btnRegisterApp, btnResend;
    private TextView txtTimer;

    /* variables of Count down timer for resend otp */
    private int minute = 1;
    private long onFinishCallTime = 1000 * 60 * minute;
    private long onTickCallTime = 1000;
    private String firstMinute = "01";
    private boolean secondTimeCall = false;
    private ArrayList<User> userArrayList;

    private Context getContextInstance() {
        if (context == null) {
            context = ForgotOtpVarificationActivity.this;
            return context;
        } else {
            return context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_otp_varification);
        Constants.chaneBackground(ForgotOtpVarificationActivity.this,(LinearLayout) findViewById(R.id.lnrForgotOtp));
        context = ForgotOtpVarificationActivity.this;
        constants = new Constants();
        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(getContextInstance());
        transparentProgressDialog = new TransparentProgressDialog(this, R.drawable.fotterloading);
        strUserName = getIntent().getStringExtra(LoginActivity.EXTRA_USERNAME);
        strToken = sharedPreferences.getString(constants.TOKEN, "-1");

        userArrayList = new ArrayList<User>();
        userArrayList = databaseHelper.getUserDetail();
        strAppOtp = userArrayList.get(0).getOtp_code();

        if (Build.VERSION.SDK_INT >= 23) {
            int hasContactPermission = ActivityCompat.checkSelfPermission(getContextInstance(), android.Manifest.permission.READ_SMS);
            if (hasContactPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ForgotOtpVarificationActivity.this, new String[]{android.Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
            }
        }
        init();
    }

    private void init() {
        edtOtp = (EditText) findViewById(R.id.edt_otp_act_reg_verify);
        btnRegisterApp = (Button) findViewById(R.id.btn_reg_app_act_reg_verify);
        txtTimer = (TextView) findViewById(R.id.txt_Verify_Timer);
        btnResend = (Button) findViewById(R.id.btn_Verify_ResendCode);
        btnResend.setEnabled(false);
        btnResend.setOnClickListener(this);
        // [END]
        btnRegisterApp.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register sms receiver
        //registerSmsReceiver();
        // start otp update timer
        countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        // un register sms receiver
        unRegisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        // un register sms receiver
        unRegisterReceiver();
        // cancel otp update timer
        countDownTimer.cancel();
    }

    private void unRegisterReceiver() {
        try {
            Constants.IS_RECEIVE_MESSAGE = false;
            if (messageReadReceiver != null) {
                unregisterReceiver(messageReadReceiver);
                messageReadReceiver = null;
            }
            messageReadReceiver = null;
        }
        catch (Exception ex) {
            Constants.IS_RECEIVE_MESSAGE = false;
            Dlog.d("Error : " + ex.toString());
            ex.printStackTrace();
            messageReadReceiver = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnRegisterApp) {
            checkOtp();
        } else if (v == btnResend) {
            resendOtp();
        }
    }

    private void resendOtp() {
        try {
            String strUsernameOrEmail = strUserName;
            // Generate token
            if (strToken == null || strToken.equals(null) || strToken.equals("") || strToken.equals("-1")) {
                strToken = FirebaseInstanceId.getInstance().getToken();
                sharedPreferences.edit().putString(constants.TOKEN, strToken).commit();
            }
            if (strToken.equals(null) || strToken.equals("")) {
                new AlertDialog.Builder(ForgotOtpVarificationActivity.this)
                        .setTitle("Error")
                        .setCancelable(false)
                        .setMessage("Please check your internet access")
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            } else if (strUsernameOrEmail.equals(null) || strUsernameOrEmail.equals("")) {
                Utility.toast(getContextInstance(), "Please enter email id or Username");
            } else {
                CheckConnection checkConnection = new CheckConnection();
                if (checkConnection.isConnected(this) && strToken != null) {
                    showProgressDialog();
                    makeRegisterUser();
                } else {
                    Utility.toast(getContextInstance(), "Check your internet connection");
                }
            }
        }
        catch (Exception ex) {
            Dlog.d("ERROR : " + ex.toString());
        }
    }

    private void checkOtp() {
        strOtp = edtOtp.getText().toString();
        CheckConnection checkConnection = new CheckConnection();
        if (strToken == null || strToken.equals(null) || strToken.equals("") || strToken.equals("-1")) {
            strToken = FirebaseInstanceId.getInstance().getToken();
            sharedPreferences.edit().putString(constants.TOKEN, strToken).commit();
        }
        if (strToken.equals(null) || strToken.equals("")) {
            new AlertDialog.Builder(ForgotOtpVarificationActivity.this)
                    .setTitle("Error")
                    .setCancelable(false)
                    .setMessage("Please check your internet access")
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else if (strOtp.isEmpty()) {
            Utility.toast(getContextInstance(), "Enter OTP");
        } else if (checkConnection.isConnectingToInternet(this) == true) {
            showProgressDialog();
            makeOTPVerification();
        } else {
            Utility.toast(getContextInstance(), "Check your internet connection");
        }
    }

    /* [START] - 2017_04_27 - Add native code for OTP verification, and Remove volley code */
    private void makeOTPVerification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.GET_FORGOT_OTP;
                    String[] parameters = {
                            "otp_code",
                            "username",
                            "mac_address",
                            "app",
                            "forgot_otp"
                    };
                    String[] parametersValues = {
                            strAppOtp,
                            strUserName,
                            strToken,
                            Constants.APP_VERSION,
                            edtOtp.getText().toString()
                    };
                    Dlog.d("Forgot Otp Parameter: " + parametersValues);
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseOTPVerificationResponse(response);
                }
                catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // method for parse OTP Verification user response
    private void parseOTPVerificationResponse(String response) {
        LogMessage.i("OTP Verification Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if(jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
                }
            } else if (jsonObject.getString("status").equals("1")) {
                myHandler.obtainMessage(SUCCESS_OTP_VERIFICATION, response).sendToTarget();
            } else {
                Dlog.d("OTP Verification fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("message")).sendToTarget();
            }
        }
        catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // Display OTP verification user error dialog
    private void displayOTPVerificationErrorDialog(String message) {
        new AlertDialog.Builder(ForgotOtpVarificationActivity.this)
                .setTitle("Error in verify OTP")
                .setCancelable(false)
                .setMessage(message)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    // Parse success response and display dialog
    private void parseSuccessOTPVerificationResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.getInt("status") == 1) {
                Toast.makeText(context,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotOtpVarificationActivity.this, ForgotPasswordActivity.class);
                intent.putExtra(ForgotPasswordActivity.EXTRA_OTP, edtOtp.getText().toString());
                intent.putExtra(ForgotPasswordActivity.EXTRA_PASSWORD,  String.valueOf(jsonObject.getInt("password")));
                startActivity(intent);
                ForgotOtpVarificationActivity.this.finish();
            } else {
                Toast.makeText(context,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR_TOAST, "OTP verification fail").sendToTarget();
        }
    }

    // handle OTP verification user messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS_OTP_VERIFICATION) {
                parseSuccessOTPVerificationResponse(msg.obj.toString());
            } else if (msg.what == ERROR_OTP_VERIFICATION) {
                dismissProgressDialog();
                displayOTPVerificationErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_REGISTER_USER) {
                dismissProgressDialog();
                parseRegisterUserResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                Utility.toast(getContextInstance(), msg.obj.toString());
            } else if (msg.what == ERROR_TOAST) {
                dismissProgressDialog();
                Utility.toast(getContextInstance(), msg.obj.toString());
            } else if (msg.what == AUTHENTICATION_FAIL) {
                dismissProgressDialog();
                Utility.logout(ForgotOtpVarificationActivity.this, msg.obj.toString());
            }
        }
    };

    private void makeRegisterUser() {
        // create new thread for register user
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.register;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "app"
                    };
                    String[] parametersValues = {
                            strUserName,
                            strToken,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_REGISTER_USER, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in register user");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_REGISTER_USER, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // method for parse register user response
    private void parseRegisterUserResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if(jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    /* [START] - start otp update timer */
                    edtOtp.setEnabled(true);
                    txtTimer.setVisibility(View.VISIBLE);
                    btnResend.setEnabled(false);
                    firstMinute = "01";
                    secondTimeCall = false;
                    countDownTimer.start();
                    // [END]
                }
            } else if (jsonObject.getString("status").equals("2")) {
                Utility.toast(getContextInstance(), jsonObject.getString("msg"));
            } else {
                LogMessage.d("Application Registration fail. Status = " + jsonObject.getString("status"));
                Utility.toast(getContextInstance(), jsonObject.getString("msg"));
            }
        }
        catch (JSONException e) {
            Dlog.d("Registration : " + "Error :" + e.toString());
            e.printStackTrace();
            Utility.toast(getContextInstance(), "User verification fail");
        }
    }
    // [END]

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ForgotOtpVarificationActivity.this.finish();
    }

    /* [START] - Count down timer for resend otp, interval = 2 minute */
    // private CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) { // (onFinish call time, onTick call time)
    private CountDownTimer countDownTimer = new CountDownTimer(onFinishCallTime, onTickCallTime) {
        @Override
        public void onTick(long millisUntilFinished) {
            try {
                long currentSecond = millisUntilFinished / 1000;
                String checkCurrentSecondLength = currentSecond + "";
                if (currentSecond == 60) {
                    currentSecond = 59;
                }
                if (checkCurrentSecondLength.length() == 1) {
                    txtTimer.setText(firstMinute + ":0" + currentSecond);
                } else {
                    txtTimer.setText(firstMinute + ":" + currentSecond);
                }
            }
            catch (Exception ex) {
                Dlog.d("Error : " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        @Override
        public void onFinish() {
            LogMessage.d("Finish call");
            try {
                if (secondTimeCall) {
                    txtTimer.setVisibility(View.INVISIBLE);
                    btnResend.setEnabled(true);
                } else {
                    secondTimeCall = true;
                    firstMinute = "00";
                    countDownTimer.start();
                }
            }
            catch (Exception ex) {
                Dlog.d("Error : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    };

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
            }
            if (transparentProgressDialog != null) {
                if (!transparentProgressDialog.isShowing()) {
                    transparentProgressDialog.show();
                }
            }
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // dismiss progress dialog
    private void dismissProgressDialog() {
        try {
            if (transparentProgressDialog != null) {
                if (transparentProgressDialog.isShowing())
                    transparentProgressDialog.dismiss();
            }
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
