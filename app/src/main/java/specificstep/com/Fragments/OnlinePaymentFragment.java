package specificstep.com.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.razorpay.Checkout;
import com.razorpay.Payment;
import com.razorpay.PaymentResultListener;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.DMTSenderBeneficiaryModel;
import specificstep.com.Models.DMTSenderModel;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class OnlinePaymentFragment extends Fragment implements PaymentResultListener {

    public static EditText edtMobile, edtamount;
    Button btnSubmit;
    View view;
    Context context;
    BottomSheetDialog dialogError, dialogVerify;
    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_WALLET_LIST = 4;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;

    public static ArrayList<DMTSenderModel> mDmtSenderModelsArrayList;
    DMTSenderModel mDmtSenderModel;
    public static ArrayList<DMTSenderBeneficiaryModel> mDmtBeneficiaryModelsArrayList;
    DMTSenderBeneficiaryModel mDmtSenderBeneficiaryModel;
    public static String mob;
    String balance;
    public static int sender_id;
    public static boolean verified = true;
    public static String senderId = "";
    private SharedPreferences sharedPreferences;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }
    private static final String TAG = "OnlinePaymentFragment";

    public OnlinePaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private MenuItem menuItem;
    ArrayList<String> menuWallet;

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // [START] - set option menu
        // Clear menu
        menu.clear();
        // Set menu
        inflater.inflate(R.menu.menu_main_activity, menu);
        Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        menuItem = menu.findItem(R.id.action_balance_menu_main);
        // [END]
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_balance_menu_main:
                if (Constants.checkInternet(getActivity())) {
                    if (Constants.walletsModelList.size() == 0) {
                        makeWalletCall();
                    } else {
                        Constants.showWalletPopup(getActivity());
                    }
                } else {
                    //Constants.showNoInternetDialog(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_online_payment, container, false);
        mainActivity().getSupportActionBar().setTitle("Wallet Topup");
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Checkout.preload(getActivity());

        initialize();
        mainActivity().displayRechargeBottomBar();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valid()) {
                    startPayment();
//                    makeSearchSenderCall();
                }
            }
        });

        return view;
    }
    public void startPayment() {
        /*
          You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = getActivity();

        final Checkout co = new Checkout();
        String amount = String.valueOf(Integer.valueOf(edtamount.getText().toString()) * 100);
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Razorpay Corp");
            options.put("description", "Demoing Charges");
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            options.put("currency", "INR");
            options.put("amount", amount);

            JSONObject preFill = new JSONObject();
            preFill.put("email", "test@razorpay.com");
            preFill.put("contact", edtMobile.getText().toString());

            options.put("prefill", preFill);

            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    //multi wallet 14-3-2019
    public void makeWalletCall() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.walletType;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_WALLET_LIST, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    public void parseSuccessWalletResponse(String response) {

        LogMessage.e("Wallet Response : " + response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("Message : " + message);
                LogMessage.e("Message : " + message);

                LogMessage.e("Wallet : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context, encrypted_response);

                LogMessage.e("Wallet : " + "decrypted_response : " + decrypted_response);

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
                    menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                }

                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                if (walletsModelList.size() > 0) {
                    Constants.showWalletPopup(getActivity());
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity().getSupportActionBar().setTitle("Wallet Topup");
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    Intent intent = new Intent(getActivity(), Main2Activity.class);
                    startActivity(intent);
                    sharedPreferences.edit().putString(Constants.DMT_MOBILE, "").commit();
                    return true;
                }
                return false;
            }
        });
    }

    public void initialize() {

        edtMobile = (EditText) view.findViewById(R.id.edtMobile);
        edtamount = (EditText) view.findViewById(R.id.edtamount);
        btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
        dialogError = new BottomSheetDialog(getActivity());
        dialogError.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogVerify = new BottomSheetDialog(getActivity());
        dialogVerify.requestWindowFeature(Window.FEATURE_NO_TITLE);
        databaseHelper = new DatabaseHelper(getActivity());
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        sharedPreferences = getActivity().getSharedPreferences(Constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);

        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();

        String mob = sharedPreferences.getString(Constants.DMT_MOBILE, "");
        edtMobile.setText(mob);

    }

    public boolean valid() {

        if (TextUtils.isEmpty(edtMobile.getText().toString())) {
            Toast.makeText(getActivity(), "Please enter Mobile No.", Toast.LENGTH_LONG).show();
            return false;
        } else if (edtMobile.getText().toString().length() < 10) {
            Toast.makeText(getActivity(), "Please enter valid Mobile No.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }

    }

    public void showErrorDialog() {
        dialogError.setContentView(R.layout.popup_dmt_search_error);
        FrameLayout bottomSheet = (FrameLayout) dialogError.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        dialogError.getWindow().getAttributes().windowAnimations = R.style.Animation;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogError.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogError.setCanceledOnTouchOutside(false);
        Button add_sender = (Button) dialogError.findViewById(R.id.btnDMTAddSender);
        Button try_again = (Button) dialogError.findViewById(R.id.btnDMTTryAgain);
        try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogError.dismiss();
            }
        });
        add_sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DMTAddSender rechargeMainFragment = new DMTAddSender();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString() + "").commit();
                dialogError.dismiss();
            }
        });
        dialogError.show();
    }

    private void makeSearchSenderCall() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.searchSender;
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
                            edtMobile.getText().toString()
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                } catch (Exception ex) {
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
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("DMT Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                //ll_recycler_view.setVisibility(View.VISIBLE);
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("AccountLedger : " + "Message : " + message);
                LogMessage.e("AccountLedger : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context, encrypted_response);
                LogMessage.e("AccountLedger : " + "decrypted_response : " + decrypted_response);

                mDmtSenderModelsArrayList = new ArrayList<DMTSenderModel>();
                if (decrypted_response != null) {
                    JSONObject object = new JSONObject(decrypted_response);
                    if (object.length() > 0) {
                        JSONObject obj = object.getJSONObject("remitter");
                        mDmtSenderModel = new DMTSenderModel();
                        mDmtSenderModel.setId(obj.getString("id"));
                        mDmtSenderModel.setFirstname(obj.getString("firstname"));
                        mDmtSenderModel.setLastname(obj.getString("lastname"));
                        mDmtSenderModel.setMobilenumber(obj.getString("mobilenumber"));
                        mDmtSenderModel.setEmail_address(obj.getString("email_address"));
                        mDmtSenderModel.setDob(obj.getString("dob"));
                        mDmtSenderModel.setPincode(obj.getString("pincode"));
                        mDmtSenderModel.setStatus(obj.getString("status"));

                        mDmtBeneficiaryModelsArrayList = new ArrayList<DMTSenderBeneficiaryModel>();
                        JSONArray array = obj.getJSONArray("beneficiary");
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object1 = array.getJSONObject(i);
                                mDmtSenderBeneficiaryModel = new DMTSenderBeneficiaryModel();
                                mDmtSenderBeneficiaryModel.setId(object1.getString("id"));
                                mDmtSenderBeneficiaryModel.setFirstname(object1.getString("firstname"));
                                mDmtSenderBeneficiaryModel.setLastname(object1.getString("lastname"));
                                mDmtSenderBeneficiaryModel.setMobile_number(object1.getString("mobile_number"));
                                mDmtSenderBeneficiaryModel.setAccount_number(object1.getString("account_number"));
                                mDmtSenderBeneficiaryModel.setBank_id(object1.getString("bank_id"));
                                mDmtSenderBeneficiaryModel.setIfsc_code(object1.getString("ifsc_code"));
                                mDmtSenderBeneficiaryModel.setAccount_type(object1.getString("account_type"));
                                mDmtSenderBeneficiaryModel.setBank_name(object1.getString("bank_name"));
                                mDmtSenderBeneficiaryModel.setAccount_verified(object1.getString("account_verified"));
                                mDmtBeneficiaryModelsArrayList.add(mDmtSenderBeneficiaryModel);
                            }
                            mDmtSenderModel.setBeneficiaryModels(mDmtBeneficiaryModelsArrayList);
                        } else {
                            mDmtBeneficiaryModelsArrayList.clear();
                        }
                        mDmtSenderModelsArrayList.add(mDmtSenderModel);
                        mob = edtMobile.getText().toString();
                        sharedPreferences.edit().putString(Constants.DMT_MOBILE, mob).commit();
                        if (obj.getString("status").equals("1")) {
                            try {
                                verified = true;
                                senderId = mDmtSenderModelsArrayList.get(0).getId();
                                DMTBenefitiaryListFragment rechargeMainFragment = new DMTBenefitiaryListFragment();
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString() + "").commit();
                            } catch (Exception e) {
                                Dlog.d(e.toString());
                            }
                        } else {
                            showVerifyDialog();
                        }
                    } else {
                        showErrorDialog();
                    }
                } else {
                    showErrorDialog();
                }

            } else {
                showErrorDialog();
            }
        } catch (JSONException e) {
            LogMessage.e("Cashbook : " + "Error 4 : " + e.getMessage());
            //Utility.toast(getActivity(), "No result found");
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    public void showVerifyDialog() {
        try {
            dialogVerify.setContentView(R.layout.popup_dmt_verify);
            FrameLayout bottomSheet = (FrameLayout) dialogVerify.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            dialogVerify.getWindow().getAttributes().windowAnimations = R.style.Animation;

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialogVerify.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            dialogVerify.setCanceledOnTouchOutside(false);

            TextView verifyName = (TextView) dialogVerify.findViewById(R.id.txtDmtVerifyName);
            TextView verifyMobile = (TextView) dialogVerify.findViewById(R.id.txtDmtVerifyMobile);
            TextView verifyDob = (TextView) dialogVerify.findViewById(R.id.txtDmtVerifyDob);
            TextView verifyEmail = (TextView) dialogVerify.findViewById(R.id.txtDmtVerifyEmail);
            LinearLayout lnrVerifyEmail = (LinearLayout) dialogVerify.findViewById(R.id.lnrDmtVerifyEmail);
            LinearLayout lnrVerifyDob = (LinearLayout) dialogVerify.findViewById(R.id.lnrDmtVerifyDob);
            Button cancel = (Button) dialogVerify.findViewById(R.id.btnDMTVerifyCancel);
            Button verify = (Button) dialogVerify.findViewById(R.id.btnDMTVerifyUser);

            if (!mDmtSenderModelsArrayList.get(0).getLastname().equals("null")) {
                verifyName.setText(mDmtSenderModelsArrayList.get(0).getFirstname() + " " + mDmtSenderModelsArrayList.get(0).getLastname());
            } else {
                verifyName.setText(mDmtSenderModelsArrayList.get(0).getFirstname());
            }
            verifyMobile.setText(mDmtSenderModelsArrayList.get(0).getMobilenumber());
            if (!mDmtSenderModelsArrayList.get(0).getDob().equals("null")) {
                verifyDob.setText(Constants.commonDateFormate(mDmtSenderModelsArrayList.get(0).getDob(), "yyyy-MM-dd", "dd-MMM-yyyy"));
                lnrVerifyDob.setVisibility(View.VISIBLE);
            } else {
                lnrVerifyDob.setVisibility(View.GONE);
            }
            if (!mDmtSenderModelsArrayList.get(0).getEmail_address().equals("null")) {
                verifyEmail.setText(mDmtSenderModelsArrayList.get(0).getEmail_address());
                lnrVerifyEmail.setVisibility(View.VISIBLE);
            } else {
                lnrVerifyEmail.setVisibility(View.GONE);
            }

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogVerify.dismiss();
                }
            });

            verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogVerify.dismiss();
                    verified = false;
                    sender_id = Integer.parseInt(mDmtSenderModelsArrayList.get(0).getId());
                    DMTOtpFragment rechargeMainFragment = new DMTOtpFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString() + "").commit();
                }
            });

            dialogVerify.show();
        } catch (Exception e) {
            Dlog.d("DMT Fragment Verify Error: " + e.toString());
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getActivity(), message);
            } catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
            Toast.makeText(getActivity(), "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

            getData(razorpayPaymentID);
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentSuccess", e);
        }
    }

    private void getData(String payment_id) {

        RazorpayClient razorpayClient = null;
        try {

            razorpayClient = new RazorpayClient("rzp_test_tzuoolyalWiPY5", "bCjf019Ltnr4b8MULX2HGdCo");
            Payment payment = razorpayClient.Payments.fetch(payment_id);

            int amount = payment.get("amount");
            Log.d("amount", String.valueOf(amount / 100));
            Log.d("payment", String.valueOf(payment));
        } catch (RazorpayException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPaymentError(int code, String response ) {
        try {
            Toast.makeText(getActivity(), "Payment failed: " + code + " " + response, Toast.LENGTH_SHORT).show();
            Log.d("payment_getresult", response);
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError", e);
        }
    }
}
