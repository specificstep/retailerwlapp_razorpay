package specificstep.com.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Activities.HomeActivity;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class DMTOtpFragment extends Fragment {

    View view;
    EditText edtOtp;
    Button btnVerify, btnResend;
    TextView txtTimer;

    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    private Constants constants;
    private DatabaseHelper databaseHelper;
    private TransparentProgressDialog transparentProgressDialog;
    private final int  SUCCESS_REGISTER_USER = 7, ERROR_REGISTER_USER = 8,
                        AUTHENTICATION_FAIL = 9;
    private final int SUCCESS = 1, ERROR = 2;
    private AlertDialog alertDialog;

    /* variables of Count down timer for resend otp */
    private int minute = 1;
    private long onFinishCallTime = 1000 * 60 * minute;
    private long onTickCallTime = 1000;
    private String firstMinute = "01";
    private boolean secondTimeCall = false;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public DMTOtpFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dmtotp, container, false);
        mainActivity().getSupportActionBar().setTitle("DMT OTP Verification");
        initialize();

        // Display bottom bar
        mainActivity().displayDMTBottomBarDynamic();

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOtp();
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendOtp();
            }
        });

        if(!DMTFragment.verified) {
            btnResend.performClick();
        }

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void initialize() {

        alertDialog = new AlertDialog.Builder(getActivity()).create();
        Constants.chaneBackground(getActivity(),(LinearLayout) view.findViewById(R.id.lnrDMTOtp));
        constants = new Constants();
        databaseHelper = new DatabaseHelper(getActivity());
        transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);

        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();

        edtOtp = (EditText) view.findViewById(R.id.edtDMTOtp);
        btnVerify = (Button) view.findViewById(R.id.btnDMTOtpVerify);
        btnResend = (Button) view.findViewById(R.id.btnDMTOtpResend);
        btnResend.setEnabled(false);
        txtTimer = (TextView) view.findViewById(R.id.txtDMTOtpVerifyTimer);

    }

    @Override
    public void onResume() {
        super.onResume();
        countDownTimer.start();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    try {
                        getFragmentManager().popBackStackImmediate();
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void checkOtp() {
        String strOtp = edtOtp.getText().toString();
        CheckConnection checkConnection = new CheckConnection();
        if (strOtp.isEmpty()) {
            Utility.toast(getActivity(), "Enter OTP");
        } else if (checkConnection.isConnectingToInternet(getActivity()) == true) {
            showProgressDialog();
            makeOTPVerification();
        } else {
            Utility.toast(getActivity(), "Check your internet connection");
        }
    }

    private void makeOTPVerification() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int sender_id;
                    if(!DMTFragment.verified) {
                        sender_id = DMTFragment.sender_id;
                    } else {
                        sender_id = DMTAddSender.sender_id;
                    }

                    // set cashBook url
                    String url = URL.verifySender;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "sender_id",
                            "sender_otp_code"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            String.valueOf(sender_id),
                            edtOtp.getText().toString()
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                dismissProgressDialog();
                parseSuccessResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_REGISTER_USER) {
                dismissProgressDialog();
                parseRegisterUserResponse(msg.obj.toString());
            } else if (msg.what == AUTHENTICATION_FAIL) {
                dismissProgressDialog();
                Utility.logout(getActivity(), msg.obj.toString());
            }
        }
    };

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("OTP Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                Toast.makeText(getActivity(),jsonObject.getString("msg")+"",Toast.LENGTH_LONG).show();
                if(jsonObject.getString("msg").equals("Otp Verified Successfully")) {
                    DMTAddBenefitiaryFragment rechargeMainFragment = new DMTAddBenefitiaryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("from", "otp");
                    rechargeMainFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container, rechargeMainFragment).commit();
                }
            } else {
                Toast.makeText(getActivity(),jsonObject.getString("msg")+"",Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e) {
            LogMessage.e("Cashbook : " + "Error 4 : " + e.getMessage());
            //Utility.toast(getActivity(), "No result found");
            e.printStackTrace();
        }
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);
            }
            if (transparentProgressDialog != null) {
                if (!transparentProgressDialog.isShowing()) {
                    transparentProgressDialog.show();
                }
            }
        }
        catch (Exception ex) {
            LogMessage.e("Error in show progress");
            LogMessage.e("Error : " + ex.getMessage());
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
            LogMessage.e("Error in dismiss progress");
            LogMessage.e("Error : " + ex.getMessage());
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
        }
        catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getActivity(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    private void resendOtp() {
        try {
            String strUsernameOrEmail = strUserName;
            // Generate token
            if (strUsernameOrEmail.equals(null) || strUsernameOrEmail.equals("")) {
                Utility.toast(getActivity(), "Please enter email id or Username");
            } else {
                /*Checks whether user's phone is connected to internet or not*/
                CheckConnection checkConnection = new CheckConnection();
                if (checkConnection.isConnected(getActivity())) {
                    showProgressDialog();
                    makeRegisterUser();
                } else {
                    Utility.toast(getActivity(), "Check your internet connection");
                }
            }
        }
        catch (Exception ex) {
            LogMessage.e("Error while click on register button");
            LogMessage.d("ERROR : " + ex.toString());
        }
    }

    private void makeRegisterUser() {
        // create new thread for register user
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set register user url
                    String url = URL.resendDmtOtp;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "mobile"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            DMTFragment.mob
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
            Dlog.d("Resend Otp Response: " + response);
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
                    displayErrorDialog(jsonObject.getString("msg")+"");
                    // [END]
                }
            } else if (jsonObject.getString("status").equals("2")) {
                Utility.toast(getActivity(), jsonObject.getString("msg"));
            } else {
                LogMessage.d("Application Registration fail. Status = " + jsonObject.getString("status"));
                Utility.toast(getActivity(), jsonObject.getString("msg"));
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error occur while register application");
            LogMessage.e("Registration : " + "Error :" + e.toString());
            e.printStackTrace();
            Utility.toast(getActivity(), "User verification fail");
        }
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
                LogMessage.e("Error in on tick");
                LogMessage.e("Error : " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        @Override
        public void onFinish() {
            LogMessage.d("Finish call");
            try {
                // txtTimer.setText("00:00");
                if (secondTimeCall) {
                    txtTimer.setVisibility(View.INVISIBLE);
                    btnResend.setEnabled(true);
                    //edtOtp.setEnabled(false);
                } else {
                    secondTimeCall = true;
                    firstMinute = "00";
                    countDownTimer.start();
                }
            }
            catch (Exception ex) {
                LogMessage.d("Error in onFinish()");
                LogMessage.d("Error : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    };

}
