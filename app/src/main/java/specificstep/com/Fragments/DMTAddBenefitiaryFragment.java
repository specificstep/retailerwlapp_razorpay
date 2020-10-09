package specificstep.com.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.DMTAddBenefitiaryBankName;
import specificstep.com.Models.DMTSenderBeneficiaryModel;
import specificstep.com.Models.DMTSenderModel;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class DMTAddBenefitiaryFragment extends Fragment {

    View view;
    EditText edtFirstName, edtLastName, edtAccountNo, edtIfscCode, edtMobile;
    Spinner spnType;
    Button btnSubmit, btnCancel;
    Switch swtVerify;
    AutoCompleteTextView edtBankName;
    Context context;

    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_LOAD = 6,
            SUCCESS_WALLET_LIST = 7;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    Dialog dialogError;

    List<String> bankNamesList;
    List<DMTAddBenefitiaryBankName> mainBankNamesList;
    ArrayAdapter<String> adapter, adapterType;

    String[] accountType = {"Saving","Current"};
    int benefitiary_id;
    String from = "";

    public static ArrayList<DMTSenderModel> mDmtSenderModelsArrayList;
    DMTSenderModel mDmtSenderModel;
    public static ArrayList<DMTSenderBeneficiaryModel> mDmtBeneficiaryModelsArrayList;
    DMTSenderBeneficiaryModel mDmtSenderBeneficiaryModel;
    String finalBank = "";

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public DMTAddBenefitiaryFragment() {
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
        view = inflater.inflate(R.layout.fragment_dmtadd_benefitiary, container, false);
        mainActivity().getSupportActionBar().setTitle("Add Beneficiary");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initialize();

        // Display bottom bar
        mainActivity().displayDMTBottomBarDynamic();

        try {
            Bundle bundle = getArguments();
            from = bundle.getString("from");
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()) {
                    makeAddBenefitiaryCall();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(from.equals("otp")) {
                    DMTBenefitiaryListFragment rechargeMainFragment = new DMTBenefitiaryListFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container, rechargeMainFragment).commit();
                } else {
                    try {
                        getFragmentManager().popBackStackImmediate();
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }
            }
        });

        mainBankNamesList = new ArrayList<DMTAddBenefitiaryBankName>();
        bankNamesList = new ArrayList<String>();

        try {
            //comment from database
            mainBankNamesList = databaseHelper.getDmtBank();
            for(int i=0;i<mainBankNamesList.size();i++) {
                bankNamesList.add(mainBankNamesList.get(i).toString());
            }
        } catch (Exception e) {
            Dlog.d("Fetch Deposit Bank Error: " + e.toString());
        }

        if (bankNamesList.size() > 0) {
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, bankNamesList.toArray(new String[0]));
            edtBankName.setAdapter(adapter);
        }

        edtBankName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i<mainBankNamesList.size();i++) {
                    if(mainBankNamesList.get(i).getBank_name().equals(edtBankName.getText().toString())) {
                        edtIfscCode.setText(mainBankNamesList.get(i).getIfsc_code());
                        finalBank = mainBankNamesList.get(i).getBank_id();
                    }
                }
            }
        });

        return view;
    }

    public void initialize() {

        edtFirstName = (EditText) view.findViewById(R.id.edtDMTAddBenefitiaryFirstName);
        edtLastName = (EditText) view.findViewById(R.id.edtDMTAddBenefitiaryLastName);
        edtAccountNo = (EditText) view.findViewById(R.id.edtDMTAddBenefitiaryAccountNo);
        edtIfscCode = (EditText) view.findViewById(R.id.edtDMTAddBenefitiaryIfscCode);
        edtMobile = (EditText) view.findViewById(R.id.edtDMTAddBenefitiaryMobile);
        btnSubmit = (Button) view.findViewById(R.id.btnAddBenefitiarySubmit);
        btnCancel = (Button) view.findViewById(R.id.btnAddBenefitiaryCancel);
        edtBankName = (AutoCompleteTextView) view.findViewById(R.id.edtDMTAddBenefitiaryBankName);
        edtBankName.setThreshold(3);
        spnType = (Spinner) view.findViewById(R.id.spnDMTAddBenefitiaryAccType);
        swtVerify = (Switch) view.findViewById(R.id.swtAddBeneficiary);
        adapterType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, accountType);
        spnType.setAdapter(adapterType);

        databaseHelper = new DatabaseHelper(getActivity());
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        dialogError = new Dialog(getActivity());
        dialogError.requestWindowFeature(Window.FEATURE_NO_TITLE);

        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();

    }

    public boolean valid() {

        if(TextUtils.isEmpty(edtFirstName.getText().toString())) {
            Toast.makeText(getActivity(),"Enter First Name.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtLastName.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Last Name.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtAccountNo.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Account Number.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtIfscCode.getText().toString())) {
            Toast.makeText(getActivity(),"Enter IFSC Code.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtMobile.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Mobile Number.",Toast.LENGTH_LONG).show();
            return false;
        } else if(edtMobile.getText().toString().length() < 10) {
            Toast.makeText(getActivity(),"Enter valid Mobile Number.",Toast.LENGTH_LONG).show();
            return false;
        } else if(!isIfscCodeValid(edtIfscCode.getText().toString())) {
            Toast.makeText(getActivity(),"Enter valid IFSC CODE.",Toast.LENGTH_LONG).show();
            return false;
        } else if(finalBank.equals("")) {
            Toast.makeText(getActivity(),"Enter valid Bank Name.",Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }

    }

    private void makeAddBenefitiaryCall() {
        showProgressDialog();
        String sender_id = "";
        if(from.equals("otp")) {
            sender_id = String.valueOf(DMTAddSender.sender_id);
        } else {
            sender_id = DMTFragment.mDmtSenderModelsArrayList.get(0).getId();
        }
        String str = "";
        if (swtVerify.isChecked()) {
            str = "1";
        } else {
            str = "0";
        }
        // create new threadc
        final String finalSender_id = sender_id;
        final String finalStr = str;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.addBenefitiary;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "mobile",
                            "firstname",
                            "lastname",
                            "account_no",
                            "account_type",
                            "sender_id",
                            "bank_id",
                            "ifsc_code",
                            "is_verify"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            edtMobile.getText().toString(),
                            edtFirstName.getText().toString(),
                            edtLastName.getText().toString(),
                            edtAccountNo.getText().toString(),
                            accountType[spnType.getSelectedItemPosition()],
                            finalSender_id,
                            finalBank,
                            edtIfscCode.getText().toString(),
                            finalStr
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

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
         try {
             Bundle bundle = getArguments();
             from = bundle.getString("from");
             if(from!=null && !from.equals("otp")) {
                 getView().setFocusableInTouchMode(true);
                 getView().requestFocus();
                 getView().setOnKeyListener(new View.OnKeyListener() {
                     @Override
                     public boolean onKey(View v, int keyCode, KeyEvent event) {
                         if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                             // handle back button's click listener
                             getFragmentManager().popBackStackImmediate();
                             return true;
                         }
                         return false;
                     }
                 });
             }
         } catch (Exception e) {
             System.out.print(e.toString());
         }
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
            } else if(msg.what == SUCCESS_LOAD) {
                parseSuccessAddResponse(msg.obj.toString());
                dismissProgressDialog();
            } else if(msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("Add Benefitiary Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                Toast.makeText(getActivity(),jsonObject.getString("msg")+"",Toast.LENGTH_LONG).show();
                if(jsonObject.getString("msg").equals("Beneficiary Added Successfully")) {
                    benefitiary_id = jsonObject.getInt("data");
                    makeSearchSenderCall();
                    if(from.equals("otp")) {
                        DMTBenefitiaryListFragment rechargeMainFragment = new DMTBenefitiaryListFragment();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, rechargeMainFragment).commit();
                    } else {
                        try {
                            getFragmentManager().popBackStackImmediate();
                        } catch (Exception e) {
                            Dlog.d(e.toString());
                        }
                    }
                } else {
                    showErrorDialog(jsonObject.getString("msg"));
                }
            } else {
                showErrorDialog(jsonObject.getString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    // parse success response
    private void parseSuccessAddResponse(String response) {
        LogMessage.i("DMT Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
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
                    }
                }

            }
        }
        catch (JSONException e) {
            LogMessage.e("Cashbook : " + "Error 4 : " + e.getMessage());
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
        }
    }

    public static boolean isIfscCodeValid(String email)
    {
        String regExp = "^[A-Z]{4}[0][A-Z0-9]{6}$";
        boolean isvalid = false;

        if (email.length() > 0) {
            isvalid = email.matches(regExp);
        }
        return isvalid;
    }

}
