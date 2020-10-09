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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Color;
import specificstep.com.Models.Company;
import specificstep.com.R;
import specificstep.com.Sms.SmsReceiver;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 12/1/17.
 */

public class VerifyRegistrationActivity extends Activity implements View.OnClickListener {
    /* Other class objects */
    private Context context;
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private DatabaseHelper databaseHelper;
    private TransparentProgressDialog transparentProgressDialog;
    private BroadcastReceiver messageReadReceiver;

    /* All local int and string variables */
    private final int SUCCESS_OTP_VERIFICATION = 1, ERROR_OTP_VERIFICATION = 2, ERROR = 3,
            SUCCESS_MOBILE_COMPANY = 4, SUCCESS_DTH_COMPANY = 5, SUCCESS_SETTING = 6,
            SUCCESS_REGISTER_USER = 7, ERROR_REGISTER_USER = 8, ERROR_TOAST = 9,
            AUTHENTICATION_FAIL = 10;
    private final int PERMISSION_REQUEST_CODE = 123;
    private String strOtp, strUserName, strToken, strUserId, strStateName, strStateId, strName;

    private ArrayList<Company> companyArrayList;
    private ArrayList<Color> colorArrayList;

    private EditText edtOtp;
    private Button btnRegisterApp, btnResend;
    private TextView txtTimer;

    /* variables of Count down timer for resend otp */
    private int minute = 1;
    private long onFinishCallTime = 1000 * 60 * minute;
    private long onTickCallTime = 1000;
    private String firstMinute = "01";
    private boolean secondTimeCall = false;

    private Context getContextInstance() {
        if (context == null) {
            context = VerifyRegistrationActivity.this;
            return context;
        } else {
            return context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_reg);
        Constants.chaneBackground(VerifyRegistrationActivity.this,(LinearLayout) findViewById(R.id.lnrVeriRegister));
        context = VerifyRegistrationActivity.this;
        constants = new Constants();
        companyArrayList = new ArrayList<Company>();
        colorArrayList = new ArrayList<Color>();
        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(getContextInstance());
        transparentProgressDialog = new TransparentProgressDialog(this, R.drawable.fotterloading);
        strUserName = getIntent().getStringExtra("uname");
        strToken = sharedPreferences.getString(constants.TOKEN, "-1");

        if (Build.VERSION.SDK_INT >= 23) {
            int hasContactPermission = ActivityCompat.checkSelfPermission(getContextInstance(), android.Manifest.permission.READ_SMS);
            if (hasContactPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(VerifyRegistrationActivity.this, new String[]{android.Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
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
            Dlog.d("Un - Register SMS register");
            if (messageReadReceiver != null) {
                unregisterReceiver(messageReadReceiver);
                messageReadReceiver = null;
                Dlog.d("Unregister sms receiver done");
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
                new AlertDialog.Builder(VerifyRegistrationActivity.this)
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
                /*Checks whether user's phone is connected to internet or not*/
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
            new AlertDialog.Builder(VerifyRegistrationActivity.this)
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
                    String url = URL.register;
                    String[] parameters = {
                            "otp_code",
                            "username",
                            "mac_address",
                            "app"
                    };
                    String[] parametersValues = {
                            strOtp,
                            strUserName,
                            strToken,
                            Constants.APP_VERSION
                    };
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
        Dlog.d("OTP Verification Response : " + response);
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
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
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
        new AlertDialog.Builder(VerifyRegistrationActivity.this)
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
            sharedPreferences.edit().putString(constants.VERIFICATION_STATUS, "1").commit();

            strUserId = jsonObject.getString("user_id");
            strStateId = jsonObject.getString("state_id");
            strStateName = jsonObject.getString("state_name");
            strName = jsonObject.getString("name");

            databaseHelper.deleteDefaultSettings();
            databaseHelper.addDefaultSettings(strUserId, strStateId, strStateName);
            databaseHelper.deleteUsersDetail();
            databaseHelper.addUserDetails(strUserId, strOtp, strUserName, strToken, strName, "0");

            SmsReceiver.bindListener(null);
            // get mobile company data
            makeJsonMobileCompany();
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
            } else if (msg.what == SUCCESS_MOBILE_COMPANY) {
                parseSuccessMobileCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_DTH_COMPANY) {
                parseSuccessDTHCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_SETTING) {
                parseSuccessSettingsResponse(msg.obj.toString());
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
                Utility.logout(VerifyRegistrationActivity.this, msg.obj.toString());
            }
        }
    };

    // get mobile company data after OTP verification
    private void makeJsonMobileCompany() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.company;
                    String[] parameters = {
                            "otp_code",
                            "username",
                            "mac_address",
                            "service",
                            "app"
                    };
                    String[] parametersValues = {
                            strOtp,
                            strUserName,
                            strToken,
                            "1",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseMobileCompanyResponse(response);
                }
                catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse mobile company response
    private void parseMobileCompanyResponse(String response) {
        Dlog.d("Mobile Company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(VerifyRegistrationActivity.this,encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_MOBILE_COMPANY, decryptedString).sendToTarget();
            } else {
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        }
        catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR_TOAST, "OTP verification fail").sendToTarget();
        }
    }

    // parse success mobile company
    private void parseSuccessMobileCompanyResponse(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("company");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Company company = new Company();
                company.setId(jsonObject2.getString("id"));
                company.setCompany_name(jsonObject2.getString("company_name"));
                company.setLogo(jsonObject2.getString("logo"));
                company.setService_type("Mobile");
                companyArrayList.add(company);
            }
            if (companyArrayList.size() > 0) {
                databaseHelper.deleteCompanyDetail("Mobile");
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get dth company data
            makeJsonDTHCompany();
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // get mobile company data after getting mobile company data
    private void makeJsonDTHCompany() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.company;
                    String[] parameters = {
                            "otp_code",
                            "username",
                            "mac_address",
                            "service",
                            "app"
                    };
                    String[] parametersValues = {
                            strOtp,
                            strUserName,
                            strToken,
                            "2",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseDTHCompanyResponse(response);
                }
                catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseDTHCompanyResponse(String response) {
        Dlog.d("DTH Company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(VerifyRegistrationActivity.this,encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_DTH_COMPANY, decryptedString).sendToTarget();
            } else {
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        }
        catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // parse success dth company
    private void parseSuccessDTHCompanyResponse(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("company");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Company company = new Company();
                company.setId(jsonObject2.getString("id"));
                company.setCompany_name(jsonObject2.getString("company_name"));
                company.setLogo(jsonObject2.getString("logo"));
                company.setService_type("DTH");
                companyArrayList.add(company);
            }
            if (companyArrayList.size() > 0) {
                databaseHelper.deleteCompanyDetail("DTH");
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get setting data (color data and recharge control order data)
            makeJsonSetting();
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // TODO : make to get plan types

    // get mobile company data after getting mobile company data
    private void makeJsonSetting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.setting;
                    String[] parameters = {
                            "otp_code",
                            "username",
                            "mac_address",
                            "app"
                    };
                    String[] parametersValues = {
                            strOtp,
                            strUserName,
                            strToken,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseSettingsResponse(response);
                }
                catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseSettingsResponse(String response) {
        Dlog.d("Setting Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                myHandler.obtainMessage(SUCCESS_SETTING, response).sendToTarget();
            } else {
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        }
        catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // parse success dth company
    private void parseSuccessSettingsResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data2");
                String decrypted_response = Constants.decryptAPI(VerifyRegistrationActivity.this,encrypted_response);
                Dlog.d("Decoded settings : " + decrypted_response);

                String encrypted_response1 = jsonObject.getString("data");
                String decrypted_response1 = Constants.decryptAPI(VerifyRegistrationActivity.this,encrypted_response1);
                Dlog.d("Setting Response : " + decrypted_response1);
                // parse color data
                JSONObject object = new JSONObject(decrypted_response);
                JSONArray jsonArray = object.getJSONArray("color");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object1 = jsonArray.getJSONObject(i);
                    Color color = new Color();
                    color.setColor_name(object1.getString("name"));
                    color.setColo_value(object1.getString("value"));
                    colorArrayList.add(color);
                }
                // parse recharge control order data
                object = new JSONObject(decrypted_response1);
                jsonArray = object.getJSONArray("order");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object1 = jsonArray.getJSONObject(i);
                    if (object1.getString("name").compareTo("circle") == 0) {
                        sharedPreferences.edit().putString(constants.PREF_IS_CIRCLE_VISIBILITY, object1.getString("status")).commit();
                    }
                    /* [START] - 2017_05_30 - Add is credit parameter in recharge
                     * According to the status of isCredit set is credit check box in recharge screen
                     * If is credit status is 1 then display is credit check box other wise hide check box */
                    String isCreditValue = object1.getString("name");
                    if (TextUtils.equals(isCreditValue.toLowerCase(), "iscredit")) {
                        sharedPreferences.edit().putString(constants.PREF_IS_CREDIT_STATUS, object1.getString("status")).commit();
                    }
                    // save name1 visibility status in prefrence
                    String nameValue = object1.getString("name");
                    if (TextUtils.equals(nameValue.toLowerCase(), "name1")) {
                        sharedPreferences.edit().putString(constants.PREF_NAME_STATUS, object1.getString("status")).commit();
                    }
                    // [END]
                }
                // [END]
                if (colorArrayList.size() > 0) {
                    databaseHelper.deleteStatusColor();
                    databaseHelper.addColors(colorArrayList);
                }
            }
            dismissProgressDialog();
            // stop otp update timer
            countDownTimer.cancel();
            // after getting setting data start login activity
            Intent intent = new Intent(getContextInstance(), LoginActivity.class);
            intent.putExtra("device_id", strToken);
            startActivity(intent);
        }
        catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    private void makeRegisterUser() {
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
                    Dlog.d("Error : " + ex.getMessage());
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
                    edtOtp.setEnabled(true);
                    txtTimer.setVisibility(View.VISIBLE);
                    btnResend.setEnabled(false);
                    firstMinute = "01";
                    secondTimeCall = false;
                    countDownTimer.start();
                }
            } else if (jsonObject.getString("status").equals("2")) {
                Utility.toast(getContextInstance(), jsonObject.getString("msg"));
            } else {
                Dlog.d("Application Registration fail. Status = " + jsonObject.getString("status"));
                Utility.toast(getContextInstance(), jsonObject.getString("msg"));
            }
        }
        catch (JSONException e) {
            Dlog.d("Registration : " + "Error :" + e.toString());
            e.printStackTrace();
            Utility.toast(getContextInstance(), "User verification fail");
        }
    }

    @Override
    public void onBackPressed() {
        if(databaseHelper == null) {
            databaseHelper = new DatabaseHelper(VerifyRegistrationActivity.this);
        }
        databaseHelper.closeDatabase();
        moveTaskToBack(true);
    }

    /* [START] - Count down timer for resend otp, interval = 2 minute */
    // private CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) { // (onFinish call time, onTick call time)
    private CountDownTimer countDownTimer = new CountDownTimer(onFinishCallTime, onTickCallTime) {
        @Override
        public void onTick(long millisUntilFinished) {
            try {
                long currentSecond = millisUntilFinished / 1000;
                String checkCurrentSecondLength = currentSecond + "";
                // LogMessage.d("Tick Second : " + currentSecond);
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
