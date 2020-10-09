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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Adapters.DMTBenefitiaryListAdapter;
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
public class DMTBenefitiaryListFragment extends Fragment {

    View view;
    Context context;
    FloatingActionButton btnAdd;
    public static ImageView imgNoData;
    public static RecyclerView recyclerView;
    public static RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    Dialog dialogError;
    LinearLayout lnrUserLimit;
    TextView txtUserLimit;


    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS_LOAD = 0, ERROR = 1, SUCCESS_WALLET_LIST = 2, SUCCESS_LOAD_LIMIT = 3;

    public static ArrayList<DMTSenderModel> mDmtSenderModelsArrayList;
    DMTSenderModel mDmtSenderModel;
    public static ArrayList<DMTSenderBeneficiaryModel> mDmtBeneficiaryModelsArrayList;
    DMTSenderBeneficiaryModel mDmtSenderBeneficiaryModel;

    LinearLayout lnrName, lnrMobile, lnrDob, lnrEmail;
    TextView txtName, txtMobile, txtDob, txtEmail;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public DMTBenefitiaryListFragment() {
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
                    if(Constants.walletsModelList.size()==0) {
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
                    try {
                        menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
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
        view = inflater.inflate(R.layout.fragment_dmtbenefitiary_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity().getSupportActionBar().setTitle("DMT Beneficiary List");
        initialize();
        makeSearchSenderCall();
        makeSenderLimitCall();
        // Display bottom bar
        mainActivity().displayDMTBottomBarDynamic();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DMTAddBenefitiaryFragment rechargeMainFragment = new DMTAddBenefitiaryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from", "list");
                rechargeMainFragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString()+"").commit();
            }
        });

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                    intent.putExtra("position",9);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    public void initialize() {

        btnAdd = (FloatingActionButton) view.findViewById(R.id.btnDMTAdd);
        recyclerView = (RecyclerView) view.findViewById(R.id.ll_recycler_benefitiary_list);
        txtDob = (TextView) view.findViewById(R.id.txtDMTdob);
        txtName = (TextView) view.findViewById(R.id.txtDMTName);
        txtMobile = (TextView) view.findViewById(R.id.txtDMTMobile);
        txtEmail = (TextView) view.findViewById(R.id.txtDMTEmail);
        lnrDob = (LinearLayout) view.findViewById(R.id.lnrDMTdob);
        lnrName = (LinearLayout) view.findViewById(R.id.lnrDMTName);
        lnrMobile = (LinearLayout) view.findViewById(R.id.lnrDMTMobile);
        lnrEmail = (LinearLayout) view.findViewById(R.id.lnrDMTEmail);
        lnrUserLimit = (LinearLayout) view.findViewById(R.id.lnrBeneficiaryListUserLimit);
        txtUserLimit = (TextView) view.findViewById(R.id.txtBeneficiaryListUserLimit);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataDmtBeneficiaryList);
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        dialogError = new Dialog(getActivity());
        dialogError.requestWindowFeature(Window.FEATURE_NO_TITLE);
        databaseHelper = new DatabaseHelper(getActivity());
        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();

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
                            DMTFragment.mob
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_LOAD, response).sendToTarget();
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

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if(msg.what == SUCCESS_LOAD) {
                parseSuccessAddResponse(msg.obj.toString());
                dismissProgressDialog();
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
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
                        object.getString("limit"));
            }
            } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    // parse success response
    private void parseSuccessAddResponse(String response) {
        LogMessage.i("DMT Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                //ll_recycler_view.setVisibility(View.VISIBLE);
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("AccountLedger : " + "Message : " + message);
                LogMessage.e("AccountLedger : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.e("AccountLedger : " + "decrypted_response : " + decrypted_response);

                mDmtSenderModelsArrayList = new ArrayList<DMTSenderModel>();
                if(decrypted_response != null) {
                    JSONObject object = new JSONObject(decrypted_response);
                    if(object.length() > 0) {
                        JSONObject obj = object.getJSONObject("remitter");
                        mDmtSenderModel = new DMTSenderModel();
                        mDmtSenderModel.setId(obj.getString("id"));
                        mDmtSenderModel.setFirstname(obj.getString("firstname"));
                        mDmtSenderModel.setLastname(obj.getString("lastname"));
                        mDmtSenderModel.setMobilenumber(obj.getString("mobilenumber"));
                        mDmtSenderModel.setEmail_address(obj.getString("email_address"));
                        mDmtSenderModel.setDob(obj.getString("dob"));
                        mDmtSenderModel.setPincode(obj.getString("pincode"));

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

                        if(mDmtBeneficiaryModelsArrayList.size()>0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            imgNoData.setVisibility(View.GONE);
                            Collections.reverse(mDmtBeneficiaryModelsArrayList);
                            adapter = new DMTBenefitiaryListAdapter(getActivity(), DMTBenefitiaryListFragment.this, mDmtBeneficiaryModelsArrayList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            imgNoData.setVisibility(View.VISIBLE);
                        }

                        lnrName.setVisibility(View.VISIBLE);
                        lnrMobile.setVisibility(View.VISIBLE);
                        if(!mDmtSenderModelsArrayList.get(0).getLastname().equals("null")) {
                            txtName.setText(mDmtSenderModelsArrayList.get(0).getFirstname() + " " + mDmtSenderModelsArrayList.get(0).getLastname());
                        } else {
                            txtName.setText(mDmtSenderModelsArrayList.get(0).getFirstname());
                        }
                        if(!mDmtSenderModelsArrayList.get(0).getMobilenumber().equals("null")) {
                            txtMobile.setText(mDmtSenderModelsArrayList.get(0).getMobilenumber());
                            lnrMobile.setVisibility(View.VISIBLE);
                        } else {
                            lnrMobile.setVisibility(View.GONE);
                        }
                        if(!mDmtSenderModelsArrayList.get(0).getDob().equals("null")) {
                            lnrDob.setVisibility(View.VISIBLE);
                            txtDob.setText(Constants.commonDateFormate(mDmtSenderModelsArrayList.get(0).getDob(),"yyyy-MM-dd","dd-MMM-yyyy"));
                        } else {
                            lnrDob.setVisibility(View.GONE);
                        }

                        if(!mDmtSenderModelsArrayList.get(0).getEmail_address().equals("null")) {
                            lnrEmail.setVisibility(View.VISIBLE);
                            txtEmail.setText(mDmtSenderModelsArrayList.get(0).getEmail_address());
                        } else {
                            lnrEmail.setVisibility(View.GONE);
                        }

                    } else {
                    }
                } else {
                }

            } else {
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

}
