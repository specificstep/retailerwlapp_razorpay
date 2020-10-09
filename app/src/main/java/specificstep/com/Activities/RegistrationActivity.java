package specificstep.com.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.MyLocation;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Color;
import specificstep.com.Models.Company;
import specificstep.com.R;
import specificstep.com.Sms.SmsReceiver;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 12/1/17.
 */

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private final int SUCCESS_REGISTER_USER = 1, ERROR_REGISTER_USER = 2,
            ERROR = 3, SUCCESS_OTP_VERIFICATION = 4,
            ERROR_OTP_VERIFICATION = 5, ERROR_TOAST = 6,
            SUCCESS_MOBILE_COMPANY = 7, SUCCESS_DTH_COMPANY = 8,
            SUCCESS_SETTING = 9, SUCCESS_ELECTRICITY_COMPANY = 10,
            SUCCESS_MOBILE_POSTPAID_COMPANY = 11,
            SUCCESS_GAS_COMPANY = 12, SUCCESS_WATER_COMPANY = 13,
            AUTHENTICATION_FAIL = 14;
    private Context context;
    private EditText edtUsernameOrEmail;
    private Button btnRegisterApp, btn_reg_app_act_signup;
    private String strUsernameOrEmail;
    final private int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 100;
    private String strDeviceId = "";
    private TransparentProgressDialog transparentProgressDialog;
    private String token;
    private Constants constants;
    private SharedPreferences sharedPreferences;
    public String default_otp;
    private String strUserId, strStateName, strStateId, strName;
    private DatabaseHelper databaseHelper;
    private ArrayList<Company> companyArrayList;
    private ArrayList<Company> companyArrayList1;
    private ArrayList<Color> colorArrayList;

    public static boolean from;

    int MY_PERMISSION_LOCATION = 1;
    MyLocation myLocation = new MyLocation();

    private Context getContextInstance() {
        if (context == null) {
            context = RegistrationActivity.this;
            return context;
        } else {
            return context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        myLocation.getLocation(RegistrationActivity.this, locationResult);
        marshmallowGPSPremissionCheck();
        Constants.chaneBackground(RegistrationActivity.this, (LinearLayout) findViewById(R.id.lnrRegister));
        context = RegistrationActivity.this;
        /* [START] - Set actionbar title */
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(Constants.chaneIcon(RegistrationActivity.this));
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + getResources().getColor(R.color.colorServiceText) + "\">" + "\t Register App" + "</font>"));
        // [END]
        initControls();
        setListener();
        try {
            if (getIntent().hasExtra("from")) {
                from = true;
            } else {
                from = false;
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    private void initControls() {
        /* [START] - Initialise class objects */
        constants = new Constants();
        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        transparentProgressDialog = new TransparentProgressDialog(this, R.drawable.fotterloading);
        // [END]
        databaseHelper = new DatabaseHelper(getContextInstance());
        /* [START] - Initialise control objects */
        edtUsernameOrEmail = (EditText) findViewById(R.id.edt_uname_or_email_act_reg);
        btnRegisterApp = (Button) findViewById(R.id.btn_reg_app_act_reg);
        btn_reg_app_act_signup = (Button) findViewById(R.id.btn_reg_app_act_signup);
        // [END]

        /* [START] - set input filter in username edit text */
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }

        };
        edtUsernameOrEmail.setFilters(new InputFilter[]{filter});
        // [END]

        // Get token from shared preference
        token = sharedPreferences.getString(constants.TOKEN, "");
        companyArrayList = new ArrayList<Company>();
        colorArrayList = new ArrayList<Color>();
        readPhoneState();
    }

    private void marshmallowGPSPremissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegistrationActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_LOCATION);
        } else {
            //   gps functions.
        }
    }

    public MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {

        @Override
        public void gotLocation(Location location) {
            // TODO Auto-generated method stub
            try {
                double Longitude = location.getLongitude();
                double Latitude = location.getLatitude();
                Constants.Lati = Latitude + "";
                Constants.Long = Longitude + "";
                Dlog.d("Got Location : Longitude: " + Longitude
                        + " Latitude: " + Latitude);
            } catch (Exception e) {
                Dlog.d("Location permission denied. " + e.toString());
            }
        }
    };

    private void setListener() {
        // on click listener
        btnRegisterApp.setOnClickListener(this);
        btn_reg_app_act_signup.setOnClickListener(this);

    }

    /**
     * Check phone state read permission is enable or not
     */
    public void readPhoneState() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        } else {

        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnRegisterApp) {
            strUsernameOrEmail = edtUsernameOrEmail.getText().toString();
            if (strUsernameOrEmail == null || strUsernameOrEmail.equals(null) || strUsernameOrEmail.equals("")) {
                Utility.toast(getContextInstance(), "Please enter Email id or Username or Mobile No");
            } else {
                try {
                    // Generate token
                    if (token == null || token.equals(null) || token.equals("")) {
                        token = FirebaseInstanceId.getInstance().getToken();
                        System.out.println("Firebase Token: " + token);
                    }
                    if (token == null || token.equals(null) || token.equals("")) {
                        new AlertDialog.Builder(RegistrationActivity.this)
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
                        makeRegisterUser();
                    }
                } catch (Exception ex) {
                    Dlog.d("ERROR : " + ex.toString());
                    Utility.toast(getContextInstance(), "Check your internet connection");
                }
            }
        }
        if (v == btn_reg_app_act_signup) {
            try {
                String url = "https://www.naaradpay.com/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {

            }

        }
    }

    /* [START] - 2017_04_27 - Add native code for register user, and Remove volley code */
    private void makeRegisterUser() {
        if (Constants.checkInternet(RegistrationActivity.this)) {
            showProgressDialog();
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
                                strUsernameOrEmail,
                                token,
                                Constants.APP_VERSION
                        };
                        String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                        parseRegisterUserResponse(response);
                    } catch (Exception ex) {
                        Dlog.d("Error : " + ex.getMessage());
                        ex.printStackTrace();
                        myHandler.obtainMessage(ERROR_REGISTER_USER, "Please check your internet access").sendToTarget();
                    }
                }
            }).start();
        }
    }

    // method for parse register user response
    private void parseRegisterUserResponse(String response) {
        Dlog.d("Register User Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if (jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    myHandler.obtainMessage(SUCCESS_REGISTER_USER).sendToTarget();
                }
            } else if (jsonObject.getString("status").equals("2")) {
                myHandler.obtainMessage(ERROR_REGISTER_USER, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Application Registration fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_REGISTER_USER, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "Application Registration fail").sendToTarget();
        }
    }

    // Display register user error dialog
    private void displayRegisterUserErrorDialog(String message) {
        new AlertDialog.Builder(RegistrationActivity.this)
                .setTitle("Registration Error")
                .setCancelable(false)
                .setMessage(message)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    // handle register user messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS_REGISTER_USER) {
                makeAutoOtpCall();
            } else if (msg.what == ERROR_REGISTER_USER) {
                dismissProgressDialog();
                displayRegisterUserErrorDialog(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                Utility.toast(getContextInstance(), msg.obj.toString());
            } else if (msg.what == ERROR_OTP_VERIFICATION) {
                dismissProgressDialog();
                displayOTPVerificationErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_OTP_VERIFICATION) {
                parseSuccessOTPVerificationResponse(msg.obj.toString());
            } else if (msg.what == ERROR_TOAST) {
                dismissProgressDialog();
                Utility.toast(getContextInstance(), msg.obj.toString());
            } else if (msg.what == SUCCESS_MOBILE_COMPANY) {
                parseSuccessMobileCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_DTH_COMPANY) {
                parseSuccessDTHCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_SETTING) {
                parseSuccessSettingsResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_ELECTRICITY_COMPANY) {
                parseSuccessElectricityCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_MOBILE_POSTPAID_COMPANY) {
                parseSuccessMobilePostPaidCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_GAS_COMPANY) {
                parseSuccessGasCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_WATER_COMPANY) {
                parseSuccessWaterCompanyResponse(msg.obj.toString());
            } else if (msg.what == AUTHENTICATION_FAIL) {
                dismissProgressDialog();
                Utility.logout(RegistrationActivity.this, msg.obj.toString());
            }
        }
    };
    // [END]

    //2018_12_31 Auto Otp feature
    private void makeAutoOtpCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.skipotp;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "app"
                    };
                    String[] parametersValues = {
                            strUsernameOrEmail,
                            token,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseSkipOtpUserResponse(response);
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_REGISTER_USER, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    private void parseSkipOtpUserResponse(String response) {
        Dlog.d("Register User Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1 && jsonObject.getString("msg").equals("List generated")) {
                JSONObject object = jsonObject.getJSONObject("data");
                int skip_otp = object.getInt("skip_otp");
                default_otp = object.getString("default_otp");
                if (skip_otp == 1) {
                    makeOTPVerification();
                } else {
                    dismissProgressDialog();
                    Intent intent = new Intent(getContextInstance(), VerifyRegistrationActivity.class);
                    intent.putExtra("uname", strUsernameOrEmail);
                    intent.putExtra("device_id", strDeviceId);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(getApplicationContext(), jsonObject.getString("msg") + "", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "Application Registration fail").sendToTarget();
        }
    }

    private void makeOTPVerification() {
        if (Constants.checkInternet(RegistrationActivity.this)) {
            showProgressDialog();
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
                                default_otp,
                                strUsernameOrEmail,
                                token,
                                Constants.APP_VERSION
                        };
                        String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                        parseOTPVerificationResponse(response);
                    } catch (Exception ex) {
                        Dlog.d("Error : " + ex.getMessage());
                        ex.printStackTrace();
                        myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                    }
                }
            }).start();
        }
    }

    // method for parse OTP Verification user response
    private void parseOTPVerificationResponse(String response) {
        Dlog.d("OTP Verification Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if (jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
                }
            } else if (jsonObject.getString("status").equals("1")) {
                myHandler.obtainMessage(SUCCESS_OTP_VERIFICATION, response).sendToTarget();
            } else {
                Dlog.d("OTP Verification fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
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
            databaseHelper.addUserDetails(strUserId, default_otp, strUsernameOrEmail, token, strName, "0");

            SmsReceiver.bindListener(null);
            // get mobile company data
            makeJsonMobileCompany();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR_TOAST, "OTP verification fail").sendToTarget();
        }
    }

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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.mobile_prepaid_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseMobileCompanyResponse(response);
                } catch (Exception ex) {
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
                String decryptedString = Constants.decryptAPI(RegistrationActivity.this, encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_MOBILE_COMPANY, decryptedString).sendToTarget();
            } else {
                Dlog.d("Mobile Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
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

                companyArrayList1 = databaseHelper.getCompanyDetails("Mobile");
                databaseHelper.deleteCompany();
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get dth company data
            makeJsonDTHCompany();
        } catch (Exception ex) {
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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.dth_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseDTHCompanyResponse(response);
                } catch (Exception ex) {
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
                String decryptedString = Constants.decryptAPI(RegistrationActivity.this, encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_DTH_COMPANY, decryptedString).sendToTarget();
            } else {
                Dlog.d("DTH Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
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
                databaseHelper.deleteCompany();
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get setting data (color data and recharge control order data)
            makeJsonElectricityCompany();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // get mobile company data after getting mobile company data
    private void makeJsonElectricityCompany() {
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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.electricity_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseElectricityCompanyResponse(response);
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseElectricityCompanyResponse(String response) {
        Dlog.d("Electricity Company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(RegistrationActivity.this, encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_ELECTRICITY_COMPANY, decryptedString).sendToTarget();
            } else {
                Dlog.d("Electricity Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // parse success dth company
    private void parseSuccessElectricityCompanyResponse(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("company");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Company company = new Company();
                company.setId(jsonObject2.getString("id"));
                company.setCompany_name(jsonObject2.getString("company_name"));
                company.setLogo(jsonObject2.getString("logo"));
                company.setService_type("ELECTRICITY");
                companyArrayList.add(company);
            }
            if (companyArrayList.size() > 0) {
                databaseHelper.deleteCompany();
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get setting data (color data and recharge control order data)
            makeJsonMobilePostPaidCompany();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // get mobile company data after getting mobile company data
    private void makeJsonMobilePostPaidCompany() {
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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.mobile_postpaid_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseMobilePostPaidCompanyResponse(response);
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseMobilePostPaidCompanyResponse(String response) {
        Dlog.d("MobilePostPaid Company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(RegistrationActivity.this, encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_MOBILE_POSTPAID_COMPANY, decryptedString).sendToTarget();
            } else {
                Dlog.d("Electricity Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // parse success dth company
    private void parseSuccessMobilePostPaidCompanyResponse(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("company");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Company company = new Company();
                company.setId(jsonObject2.getString("id"));
                company.setCompany_name(jsonObject2.getString("company_name"));
                company.setLogo(jsonObject2.getString("logo"));
                company.setService_type("MOBILE_POSTPAID");
                company.setFirst_tag("first_tag");
                company.setFirst_length("first_length");
                company.setFirst_type("first_type");
                company.setFirst_defined("first_defined");
                company.setSecond_tag("second_tag");
                company.setSecond_length("second_length");
                company.setSecond_type("second_type");
                company.setSecond_start_with("second_start_with");
                company.setSecond_defined("second_defined");
                company.setThird_tag("third_tag");
                company.setThird_length("third_length");
                company.setThird_type("third_type");
                company.setThird_start_with("third_start_with");
                company.setThird_defined("third_defined");
                company.setFourth_tag("fourth_tag");
                company.setFourth_length("fourth_length");
                company.setFourth_type("fourth_type");
                company.setFourth_start_with("fourth_start_with");
                company.setFourth_defined("fourth_defined");
                companyArrayList.add(company);
            }
            if (companyArrayList.size() > 0) {
                databaseHelper.deleteCompany();
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get setting data (color data and recharge control order data)
            makeJsonGasCompany();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // get mobile company data after getting mobile company data
    private void makeJsonGasCompany() {
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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.gas_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseGasCompanyResponse(response);
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseGasCompanyResponse(String response) {
        Dlog.d("Gas Company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(RegistrationActivity.this, encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_GAS_COMPANY, decryptedString).sendToTarget();
            } else {
                Dlog.d("Electricity Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // parse success dth company
    private void parseSuccessGasCompanyResponse(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("company");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Company company = new Company();
                company.setId(jsonObject2.getString("id"));
                company.setCompany_name(jsonObject2.getString("company_name"));
                company.setLogo(jsonObject2.getString("logo"));
                company.setService_type("GAS");
                companyArrayList.add(company);
            }
            if (companyArrayList.size() > 0) {
                databaseHelper.deleteCompany();
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get setting data (color data and recharge control order data)
            makeJsonWaterCompany();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // get mobile company data after getting mobile company data
    private void makeJsonWaterCompany() {
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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.water_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseWaterCompanyResponse(response);
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR_OTP_VERIFICATION, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseWaterCompanyResponse(String response) {
        Dlog.d("Gas Company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(RegistrationActivity.this, encrypted_string);
                Dlog.d("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_WATER_COMPANY, decryptedString).sendToTarget();
            } else {
                Dlog.d("Electricity Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // parse success dth company
    private void parseSuccessWaterCompanyResponse(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("company");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Company company = new Company();
                company.setId(jsonObject2.getString("id"));
                company.setCompany_name(jsonObject2.getString("company_name"));
                company.setLogo(jsonObject2.getString("logo"));
                company.setService_type("WATER");
                companyArrayList.add(company);
            }
            if (companyArrayList.size() > 0) {
                databaseHelper.deleteCompany();
                databaseHelper.addCompanysDetails(companyArrayList);
            }
            // get setting data (color data and recharge control order data)
            makeJsonSetting();
        } catch (Exception ex) {
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
                            default_otp,
                            strUsernameOrEmail,
                            token,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseSettingsResponse(response);
                } catch (Exception ex) {
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
                LogMessage.d("DTH Company fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR_OTP_VERIFICATION, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
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
                String decrypted_response = Constants.decryptAPI(RegistrationActivity.this, encrypted_response);
                Dlog.d("Decoded settings : " + decrypted_response);

                String encrypted_response1 = jsonObject.getString("data");
                String decrypted_response1 = Constants.decryptAPI(RegistrationActivity.this, encrypted_response1);
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
            // after getting setting data start login activity
            Intent intent = new Intent(getContextInstance(), LoginActivity.class);
            intent.putExtra("device_id", token);
            startActivity(intent);
        } catch (JSONException ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "OTP verification fail").sendToTarget();
        }
    }

    // Display OTP verification user error dialog
    private void displayOTPVerificationErrorDialog(String message) {
        new AlertDialog.Builder(RegistrationActivity.this)
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (!from) {
            if (databaseHelper == null) {
                databaseHelper = new DatabaseHelper(RegistrationActivity.this);
            }
            databaseHelper.closeDatabase();
            moveTaskToBack(true);
        } else {
            finish();
        }
    }

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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Dlog.d("Latitude: " + location.getLatitude());
        Dlog.d("Longitude: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
