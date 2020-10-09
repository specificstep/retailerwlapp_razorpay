package specificstep.com.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Activities.CombineTransactionListActivity;
import specificstep.com.Activities.HomeActivity;
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
public class DMTPaymentFragment extends Fragment {

    View view;
    Context context;
    Button btnSubmit, btnPaymentList;
    EditText edtAmount;
    TextView txtSender, txtName, txtAccountNo;
    BottomSheetDialog dialog, dialogConfirm;
    String benefitiary_id;
    ArrayList<DMTSenderBeneficiaryModel> mDmtBeneficiaryModelsArrayList;
    ArrayList<DMTSenderModel> mDmtSenderModelsArrayList;

    String strMacAddress, strUserName, strOtpCode, strRegistrationDateTime;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_WALLET_LIST = 3,
            SUCCESS_LOAD_LIMIT = 4;
    Dialog dialogError;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    LinearLayout lnrUserLimit;
    TextView txtUserLimit;
    int userBalance = 0;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public DMTPaymentFragment() {
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
                    makeWalletCall();
                } else {
                    //Constants.showNoInternetDialog(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    //multi wallet 14-3-2019
    public void makeWalletCall() {
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
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);

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

                if(walletsModelList.size()>0) {
                    Constants.showWalletPopup(getActivity());
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        }
        catch(JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dmtpayment, container, false);
        mainActivity().getSupportActionBar().setTitle("DMT Payment");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initialize();
        makeSenderLimitCall();
        // Display bottom bar
        mainActivity().displayDMTBottomBarDynamic();

        try {
            benefitiary_id = getArguments().getString("benefitiary_id");
            mDmtBeneficiaryModelsArrayList = DMTBenefitiaryListFragment.mDmtBeneficiaryModelsArrayList;
            mDmtSenderModelsArrayList = DMTBenefitiaryListFragment.mDmtSenderModelsArrayList;

            if(!mDmtSenderModelsArrayList.get(0).getLastname().toString().equals("null")) {
                txtSender.setText(mDmtSenderModelsArrayList.get(0).getFirstname().toString() + " " + mDmtSenderModelsArrayList.get(0).getLastname().toString() + " (" + mDmtSenderModelsArrayList.get(0).getMobilenumber().toString() + ")");
            } else {
                txtSender.setText(mDmtSenderModelsArrayList.get(0).getFirstname().toString() + " (" + mDmtSenderModelsArrayList.get(0).getMobilenumber().toString() + ")");
            }
            for(int i=0;i<mDmtBeneficiaryModelsArrayList.size();i++) {
                if(mDmtBeneficiaryModelsArrayList.get(i).getId().equals(benefitiary_id)) {
                    txtName.setText(mDmtBeneficiaryModelsArrayList.get(i).getFirstname() + " " + mDmtBeneficiaryModelsArrayList.get(i).getLastname() + " ("+ mDmtBeneficiaryModelsArrayList.get(i).getMobile_number() +")");
                    txtAccountNo.setText(mDmtBeneficiaryModelsArrayList.get(i).getBank_name() + " (" + mDmtBeneficiaryModelsArrayList.get(i).getAccount_type() + ")" + "\nAccount No: " + mDmtBeneficiaryModelsArrayList.get(i).getAccount_number() + "\nIFSC Code: " + mDmtBeneficiaryModelsArrayList.get(i).getIfsc_code());
                }
            }

        } catch (Exception e) {
            Dlog.d(e.toString());
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()) {
                    showDialog();
                }
            }
        });

        btnPaymentList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CombineTransactionListActivity.class);
                intent.putExtra("current_pos",Constants.dmt_id);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void makeSenderLimitCall() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.senderLimit;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "sender_id"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            DMTFragment.senderId
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_LOAD_LIMIT, response).sendToTarget();
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

    public void initialize() {

        btnSubmit = (Button) view.findViewById(R.id.btnDMTPaymentSubmit);
        btnPaymentList = (Button) view.findViewById(R.id.btnDMTPaymentList);
        edtAmount = (EditText) view.findViewById(R.id.edtDMTPaymentAmount);
        txtSender = (TextView) view.findViewById(R.id.txtDMTPaymentSender);
        txtName = (TextView) view.findViewById(R.id.txtDMTPaymentName);
        txtAccountNo = (TextView) view.findViewById(R.id.txtDMTPaymentAccountNo);
        lnrUserLimit = (LinearLayout) view.findViewById(R.id.lnrDMTPaymentUserLimit);
        txtUserLimit = (TextView) view.findViewById(R.id.txtDMTPaymentUserLimit);

        dialog = new BottomSheetDialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialogConfirm = new BottomSheetDialog(getActivity());
        dialogConfirm.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialogError = new Dialog(getActivity());
        dialogError.requestWindowFeature(Window.FEATURE_NO_TITLE);
        databaseHelper = new DatabaseHelper(getActivity());
        alertDialog = new AlertDialog.Builder(getActivity()).create();

        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        strRegistrationDateTime = userArrayList.get(0).getReg_date();

    }

    public boolean valid() {

        if(TextUtils.isEmpty(edtAmount.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Amount.",Toast.LENGTH_LONG).show();
            return false;
        } else if(userBalance > 0) {
            if(Integer.valueOf(edtAmount.getText().toString()) > userBalance) {
                Toast.makeText(getActivity(),"You can not send amount more than Sender Limit: " + userBalance,Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            return true;
        }
        return true;
    }

    public void showDialog() {
        dialog.setContentView(R.layout.popup_dmt_payment_detail);
        FrameLayout bottomSheet = (FrameLayout) dialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animation;

        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button transfer = (Button) dialog.findViewById(R.id.btnDMTPaymentPopupTransfer);
        Button cancel = (Button) dialog.findViewById(R.id.btnDMTPaymentPopupCancel);
        TextView txtSenderFirstName = (TextView) dialog.findViewById(R.id.txtDMTPaymentPopupFirstName);
        TextView txtBenefitiaryName = (TextView) dialog.findViewById(R.id.txtDMTPaymentPopupBeneficiaryName);
        TextView txtBenefitiaryAccountNo = (TextView) dialog.findViewById(R.id.txtDMTPaymentPopupBeneficiaryAccountNo);
        TextView txtAmount = (TextView) dialog.findViewById(R.id.txtDMTPaymentPopupAmount);

        if(!mDmtSenderModelsArrayList.get(0).getLastname().toString().equals("null")) {
            txtSenderFirstName.setText(mDmtSenderModelsArrayList.get(0).getFirstname() + " " + mDmtSenderModelsArrayList.get(0).getLastname() + " (" + mDmtSenderModelsArrayList.get(0).getMobilenumber() + ")");
        } else {
            txtSenderFirstName.setText(mDmtSenderModelsArrayList.get(0).getFirstname() + " (" + mDmtSenderModelsArrayList.get(0).getMobilenumber() + ")");
        }

        //txtSenderFirstName.setText(mDmtSenderModelsArrayList.get(0).getFirstname() + " " + mDmtSenderModelsArrayList.get(0).getLastname() + " (" + mDmtSenderModelsArrayList.get(0).getMobilenumber() + ")");
        for(int i=0;i<mDmtBeneficiaryModelsArrayList.size();i++) {
            if(mDmtBeneficiaryModelsArrayList.get(i).getId().equals(benefitiary_id)) {
                txtBenefitiaryName.setText(mDmtBeneficiaryModelsArrayList.get(i).getFirstname() + " " + mDmtBeneficiaryModelsArrayList.get(i).getLastname() + " ("+ mDmtBeneficiaryModelsArrayList.get(i).getMobile_number() +")");
                txtBenefitiaryAccountNo.setText(mDmtBeneficiaryModelsArrayList.get(i).getBank_name() + " (" + mDmtBeneficiaryModelsArrayList.get(i).getAccount_type() + ")" + "\nAccount No: " + mDmtBeneficiaryModelsArrayList.get(i).getAccount_number() + "\nIFSC Code: " + mDmtBeneficiaryModelsArrayList.get(i).getIfsc_code());
                txtAmount.setText("Amount:  " + getActivity().getResources().getString(R.string.Rs) + edtAmount.getText().toString());
            }
        }

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeTransferCall();
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void makeTransferCall() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.transfer;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "sender_mobile",
                            "sender_id",
                            "beneficiary_id",
                            "amount"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            mDmtSenderModelsArrayList.get(0).getMobilenumber(),
                            mDmtSenderModelsArrayList.get(0).getId(),
                            benefitiary_id,
                            edtAmount.getText().toString()
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
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                parseSuccessWalletResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_LOAD_LIMIT) {
                dismissProgressDialog();
                parseSuccessLimitLoadResponse(msg.obj.toString());
            }
        }
    };

    private void parseSuccessLimitLoadResponse(String response) {
        LogMessage.i("DMT Limit Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                //ll_recycler_view.setVisibility(View.VISIBLE);
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("Sender Limit : " + "Message : " + message);
                LogMessage.e("Sender Limit : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context, encrypted_response);
                LogMessage.e("Sender Limit : " + "decrypted_response : " + decrypted_response);
                JSONObject object = new JSONObject(decrypted_response);
                lnrUserLimit.setVisibility(View.VISIBLE);
                txtUserLimit.setText("Sender Balance Limit: " +
                        getResources().getString(R.string.Rs) + " " +
                        object.getInt("limit"));
                userBalance = object.getInt("limit");
            }
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("DMT Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                //ll_recycler_view.setVisibility(View.VISIBLE);
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("AccountLedger : " + "Message : " + message);
                LogMessage.e("AccountLedger : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.e("AccountLedger : " + "decrypted_response : " + decrypted_response);
                JSONObject object = new JSONObject(decrypted_response);
                String msg = message + " with transaction_id = " + object.getString("transaction_id");
                showDialogConfirm(msg);
            } else {
                showErrorDialog(jsonObject.getString("msg"));
                edtAmount.setText("");
            }
        }
        catch (JSONException e) {
            LogMessage.e("Cashbook : " + "Error 4 : " + e.getMessage());
            Utility.toast(getActivity(), "No result found");
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

    public void showErrorDialog(String message) {
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
    }

    public void showDialogConfirm(String msg) {
        dialogConfirm.setContentView(R.layout.popup_payment_message);
        FrameLayout bottomSheet = (FrameLayout) dialogConfirm.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        dialogConfirm.getWindow().getAttributes().windowAnimations = R.style.Animation;

        dialogConfirm.setCanceledOnTouchOutside(false);
        Button yes = (Button) dialogConfirm.findViewById(R.id.btnPopupYes);
        Button no = (Button) dialogConfirm.findViewById(R.id.btnPopupNo);
        TextView txtMessage = (TextView) dialogConfirm.findViewById(R.id.txtPopupMessage);
        txtMessage.setText(msg);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogConfirm.dismiss();
                edtAmount.setText("");
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogConfirm.dismiss();
                Intent intent = new Intent(getActivity(), CombineTransactionListActivity.class);
                intent.putExtra("current_pos","3");
                startActivity(intent);
                edtAmount.setText("");
                try {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(getFragmentManager() != null) {
                                //Do something after 100ms
                                getFragmentManager().popBackStackImmediate();
                            }
                        }
                    }, 1000);
                } catch (Exception e) {
                    Dlog.d(e.toString());
                }
            }
        });

        dialogConfirm.show();

    }

}
