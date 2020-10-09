package specificstep.com.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import specificstep.com.Activities.HomeActivity;
import specificstep.com.Adapters.ViewDthOfferAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Default;
import specificstep.com.Models.DthOfferModel;
import specificstep.com.Models.Product;
import specificstep.com.Models.State;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.NotificationUtil;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 16/1/17.
 */

public class DTHRechargeFragment extends Fragment implements View.OnClickListener {
    private View view;
    /* Other class objects */
    private Context context;
    private DatabaseHelper databaseHelper;
    private Dialog dialog_success;
    BottomSheetDialog dialog;
    public static BottomSheetDialog dialogAsk, dialogInfo;
    private TransparentProgressDialog transparentProgressDialog;
    private CheckConnection connection;
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private AlertDialog alertDialog, alertDialog_Permission;

    /* All local int and string variables */
    private final String ACTIONBAR_TITLE = "DTH Recharge";
    private final int SUCCESS_RECHARGE = 1, ERROR_RECHARGE = 2,
            ERROR = 3, SUCCESS_STATE = 4, SUCCESS_NAME = 5,
            OFFER_CALL = 7, SUCCESS_CUSTOMER_INFO = 8, SUCCESS_WALLET_LIST = 9,
            AUTHENTICATION_FAIL = 10;
    private String strMobileNumber, strAmount, strCircle, strProductId, strCompanyId,
            strUserName, strMacAddress, strOtpCode, strCircleId, strCompanyName, strProductName, strCompanyLogo;
    private int PICK_CONTACT = 500;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private String isCreditStatus = "0", name = "";

    /* All ArrayList */
    private ArrayList<State> stateArrayList;
    private ArrayList<User> userArrayList;
    private ArrayList<Default> defaultArrayList;

    /* Adapter object */
    private ArrayAdapter<String> adapterCircleName;

    /* All Views */
    private Button btnProceed, btnProcess1, btnCustomerInfo;
    private TextView txtCompanyName, txtProductName;
    private ImageButton txtChangeCompany, txtChangeProduct;
    private EditText edtMobileNumber, edtName;
    public static EditText edtAmount;
    private Spinner spiCircle;
    private CheckBox chkIsCredit;
    private ImageView imgAllContacts, company_image;
    private LinearLayout llIsCreditView, llNameView, llCircleContainer;

    //OfferPopup
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<DthOfferModel> data;
    DthOfferModel offerModel;
    public static String companyId = "", productId = "", companyName = "",
            productName = "", companyImage = "";
    private MenuItem menuItem;
    ArrayList<String> menuWallet;

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;

    private Context getContextInstance() {
        if (context == null) {
            context = DTHRechargeFragment.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dth_recharge, null);

        /* [START] - 2017_04_18 set title bar as DTH recharge */
        mainActivity().getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        // [END]

        databaseHelper = new DatabaseHelper(getActivity());
        constants = new Constants();
        sharedPreferences = getActivity().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        dialog = new BottomSheetDialog(getActivity());
        connection = new CheckConnection();
        dialog_success = new Dialog(getActivity());
        dialogAsk = new BottomSheetDialog(getContextInstance());
        dialogInfo = new BottomSheetDialog(getContextInstance());

        stateArrayList = new ArrayList<State>();
        userArrayList = new ArrayList<User>();
        defaultArrayList = new ArrayList<Default>();

        stateArrayList = databaseHelper.getStateDetails();
        userArrayList = databaseHelper.getUserDetail();
        defaultArrayList = databaseHelper.getDefaultSettings();
        transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);

        Bundle bundle = getArguments();
        strCompanyId = bundle.getString("company_id");
        strProductId = bundle.getString("product_id");
        strCompanyName = bundle.getString("company_name");
        strProductName = bundle.getString("product_name");
        strCompanyLogo = bundle.getString("company_image");

        strOtpCode = userArrayList.get(0).getOtp_code();
        strUserName = userArrayList.get(0).getUser_name();
        strMacAddress = userArrayList.get(0).getDevice_id();

        init();

        // Display bottom bar in add balance
        mainActivity().displayRechargeBottomBar(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void init() {
        llIsCreditView = (LinearLayout) view.findViewById(R.id.ll_DTHRecharge_IsCreditView);
        llNameView = (LinearLayout) view.findViewById(R.id.ll_DTHRecharge_NameView);
        llCircleContainer = (LinearLayout) view.findViewById(R.id.circle_container);
        chkIsCredit = (CheckBox) view.findViewById(R.id.chk_DTHRecharge_IsCredit);
        edtName = (EditText) view.findViewById(R.id.edt_DTHRecharge_Name);
        edtMobileNumber = (EditText) view.findViewById(R.id.edt_mo_no_fragment_recharge);
        edtAmount = (EditText) view.findViewById(R.id.edt_amt_fragment_recharge);
        btnProceed = (Button) view.findViewById(R.id.btn_proceed_fragment_recharge);
        btnCustomerInfo = (Button) view.findViewById(R.id.btn_view_customer_info_recharge);
        spiCircle = (Spinner) view.findViewById(R.id.sp_circle_fragment_recharge);
        txtCompanyName = (TextView) view.findViewById(R.id.tv_company_name_fragment_DTH_recharge);
        txtProductName = (TextView) view.findViewById(R.id.tv_product_name_fragment_DTH_recharge);
        txtChangeCompany = (ImageButton) view.findViewById(R.id.tv_change_company);
        txtChangeProduct = (ImageButton) view.findViewById(R.id.tv_change_product);
        /* [START] - 2017_05_31 - Add contact image in mobile recharge screen. */
        imgAllContacts = (ImageView) view.findViewById(R.id.img_DTHRecharge_AllContacts);
        company_image = (ImageView) view.findViewById(R.id.tv_company_image);
        btnProcess1 = (Button) view.findViewById(R.id.btn_view_offers_recharge);
        // [END]
        edtMobileNumber.setText("");
        edtAmount.setText("");

        /* [START] - Image View onClickListener */
        imgAllContacts.setOnClickListener(this);
        btnProcess1.setOnClickListener(this);
        btnCustomerInfo.setOnClickListener(this);
        // [END]
        txtChangeCompany.setOnClickListener(this);
        txtChangeProduct.setOnClickListener(this);

        txtCompanyName.setText(strCompanyName);
        txtProductName.setText(strProductName);
        if(strCompanyLogo == null || strCompanyLogo.equals("")) {
            company_image.setBackground(getResources().getDrawable(R.drawable.placeholder_icon));
        } else {
            Picasso.with(context).load(strCompanyLogo).placeholder(R.drawable.placeholder_icon).into(company_image);
        }

        /* set previous entries of mobile no and mount if user had clicked on change company or change product */
        if (sharedPreferences.getBoolean(constants.isClicked, false) == true &&
                sharedPreferences.getString(constants.RECHARGEFROM,"").equals(Constants.KEY_DTH_TEXT)) {
            edtMobileNumber.setText(sharedPreferences.getString(constants.MOBILENUMBER, ""));
            edtAmount.setText(sharedPreferences.getString(constants.AMOUNT, ""));
        }
        /* get State name */
        ArrayList<String> circle_array = new ArrayList<String>();
        for (int i = 0; i < stateArrayList.size(); i++) {
            circle_array.add(stateArrayList.get(i).getCircle_name());
        }

        spiCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorWhite));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /* get default value of state spinner when user insert smart no */
        edtMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                strMobileNumber = edtMobileNumber.getText().toString();
                if (!strMobileNumber.isEmpty()) {
                    CheckConnection checkConnection = new CheckConnection();
                    if (checkConnection.isConnectingToInternet(getActivity()) == true) {
                        makeNativeDefaultState();
                    } else {
                        LogMessage.d("Internet connection not found");
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        /* Set on item selected listener in circle spinner */
        adapterCircleName = new ArrayAdapter<String>(getActivity(), R.layout.adapter_spinner, circle_array);
        spiCircle.setAdapter(adapterCircleName);
        // set default value of state spinner
        for (int i = 0; i < adapterCircleName.getCount(); i++) {
            if (defaultArrayList.get(0).getState_name().trim().equals(adapterCircleName.getItem(i).toString())) {
                spiCircle.setSelection(i);
                break;
            }
        }
        btnProceed.setOnClickListener(this);

        /* [START] - set Circle visibility */
        String circle_visibility = sharedPreferences.getString(constants.PREF_IS_CIRCLE_VISIBILITY, "0");
        if (circle_visibility.compareTo("0") == 0) {
            llCircleContainer.setVisibility(View.GONE);
        }
        // [END]
        /* [START] - 2017_05_30 - Set is credit visibility */
        String isCreditVisibility = sharedPreferences.getString(constants.PREF_IS_CREDIT_STATUS, "0");
        if (TextUtils.equals(isCreditVisibility, "0")) {
            llIsCreditView.setVisibility(View.GONE);
        }
        // Set name visibility
        String nameVisibility = sharedPreferences.getString(constants.PREF_NAME_STATUS, "0");
        if (TextUtils.equals(nameVisibility, "0")) {
            llNameView.setVisibility(View.GONE);
        }
        // [END]
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
            }
        }
        catch(JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void makeNativeCustomerInfo() {
        // create new thread for get name by mobile number
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.electricity_customer_info;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "mobile",
                            "company"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            edtMobileNumber.getText().toString(),
                            strCompanyName
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    Dlog.d("Electricity Customer Info Response: " + response);
                    myHandler.obtainMessage(SUCCESS_CUSTOMER_INFO, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in get name native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    public void parseCustomerInfoResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                Dlog.d("Electricity Customer Info response is: " + Constants.decryptAPI(context,encrypted_string));
                JSONObject obj = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                if(!obj.getString("customerName").equals("null") &&
                        !obj.getString("planName").equals("null") &&
                        !obj.getString("balance").equals("null") &&
                        !obj.getString("monthlyRecharge").equals("null") &&
                        !obj.getString("nextRechargeDate").equals("null")) {
                    showCustomerInfoPopup(obj);
                } else {
                    Toast.makeText(getActivity(),"Customer Information not found.",Toast.LENGTH_LONG).show();
                }
            } else {
                final AlertDialog alertDialog = new AlertDialog.Builder(getContextInstance()).create();
                alertDialog.setCancelable(false);
                alertDialog.setMessage(jsonObject.getString("msg"));
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                //dialogAsk.dismiss();
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error while parsing name response");
            LogMessage.e("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showCustomerInfoPopup(JSONObject obj) {
        dialogInfo.setContentView(R.layout.popup_customer_info);
        dialogInfo.getWindow().getAttributes().windowAnimations = R.style.Animation;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogInfo.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogInfo.setTitle(getActivity().getResources().getString(R.string.str_view_offer_title));

        TextView txtCustomerName = (TextView) dialogInfo.findViewById(R.id.txt_customer_info_popup_customer_name);
        TextView txtPlanName = (TextView) dialogInfo.findViewById(R.id.txt_customer_info_popup_plan_name);
        TextView txtBalance = (TextView) dialogInfo.findViewById(R.id.txt_customer_info_popup_balance);
        TextView txtMonthlyRecharge = (TextView) dialogInfo.findViewById(R.id.txt_customer_info_popup_monthly_recharge);
        TextView txtNextDate = (TextView) dialogInfo.findViewById(R.id.txt_customer_info_popup_next_date);
        Button btnCancel = (Button) dialogInfo.findViewById(R.id.btnCustomerInfoCancel);

        try {
            if(!obj.getString("customerName").equals("null")) {
                String strName = "<b>Customer Name:   </b>" + obj.getString("customerName");
                txtCustomerName.setText(Html.fromHtml(strName));
                txtCustomerName.setVisibility(View.VISIBLE);
            } else {
                txtCustomerName.setVisibility(View.GONE);
            }
            if(!obj.getString("planName").equals("null")) {
                String strPlan = "<b>Plan Name:   </b>" + obj.getString("planName");
                txtPlanName.setText(Html.fromHtml(strPlan));
                txtPlanName.setVisibility(View.VISIBLE);
            } else {
                txtPlanName.setVisibility(View.GONE);
            }
            if(!obj.getString("balance").equals("null")) {
                String strBalance = "<b>Balance:   </b>" + getActivity().getResources().getString(R.string.Rs) + " " + obj.getString("balance");
                txtBalance.setText(Html.fromHtml(strBalance));
                txtBalance.setVisibility(View.VISIBLE);
            } else {
                txtBalance.setVisibility(View.GONE);
            }
            if(!obj.getString("monthlyRecharge").equals("null")) {
                String strMonthly = "<b>Monthly Recharge:   </b>" + getActivity().getResources().getString(R.string.Rs) + " " + obj.getString("monthlyRecharge");
                txtMonthlyRecharge.setText(Html.fromHtml(strMonthly));
                txtMonthlyRecharge.setVisibility(View.VISIBLE);
            } else {
                txtMonthlyRecharge.setVisibility(View.GONE);
            }
            if(!obj.getString("nextRechargeDate").equals("null")) {
                String strData = "<b>Next Recharge Date:   </b>" + Constants.commonDateFormate(obj.getString("nextRechargeDate"), "yyyy-MM-dd", "dd-MMM-yyyy");
                txtNextDate.setText(Html.fromHtml(strData));
                txtNextDate.setVisibility(View.VISIBLE);
            } else {
                txtNextDate.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInfo.dismiss();
            }
        });

        dialogInfo.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onResume() {
        super.onResume();

        mainActivity().getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        // [END]

        databaseHelper = new DatabaseHelper(getActivity());
        constants = new Constants();
        sharedPreferences = getActivity().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        dialog = new BottomSheetDialog(getActivity());
        connection = new CheckConnection();
        dialog_success = new Dialog(getActivity());
        dialogAsk = new BottomSheetDialog(getContextInstance());

        stateArrayList = new ArrayList<State>();
        userArrayList = new ArrayList<User>();
        defaultArrayList = new ArrayList<Default>();

        stateArrayList = databaseHelper.getStateDetails();
        userArrayList = databaseHelper.getUserDetail();
        defaultArrayList = databaseHelper.getDefaultSettings();
        transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);

        try {
            Bundle bundle = getArguments();
            strCompanyId = bundle.getString("company_id");
            strProductId = bundle.getString("product_id");
            strCompanyName = bundle.getString("company_name");
            strProductName = bundle.getString("product_name");
            strCompanyLogo = bundle.getString("company_image");
        } catch (Exception e) {
            Dlog.d("bundle: " + e.toString());
        }

        strOtpCode = userArrayList.get(0).getOtp_code();
        strUserName = userArrayList.get(0).getUser_name();
        strMacAddress = userArrayList.get(0).getDevice_id();

        if (sharedPreferences.getBoolean(constants.isClicked, false) == true &&
                sharedPreferences.getString(constants.RECHARGEFROM,"").equals(Constants.KEY_DTH_TEXT)) {
            edtMobileNumber.setText(sharedPreferences.getString(constants.MOBILENUMBER, ""));
            edtAmount.setText(sharedPreferences.getString(constants.AMOUNT, ""));
        }

        // Display bottom bar in add balance
        mainActivity().displayRechargeBottomBar(false);

        mainActivity().displayRechargeBottomBar(false);

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

    @Override
    public void onClick(View v) {
        strMobileNumber = edtMobileNumber.getText().toString();
        strAmount = edtAmount.getText().toString();
        strCircle = spiCircle.getSelectedItem().toString();
        strCircleId = databaseHelper.getCircleID(strCircle);
        sharedPreferences.edit().putString(constants.MOBILENUMBER, strMobileNumber).commit();
        sharedPreferences.edit().putString(constants.AMOUNT, strAmount).commit();

        switch (v.getId()) {
            case R.id.btn_proceed_fragment_recharge:
                if (!TextUtils.isEmpty(edtAmount.getText().toString())) {
                    showConfirmationDialog();
                } else {
                    Utility.toast(getContextInstance(), "Please enter amount");
                }
                break;
            case R.id.btn_cancel_confirm_dialog:
                dialog.dismiss();
                break;
            case R.id.btn_confirm_confirm_dialog:
                CheckConnection checkConnection = new CheckConnection();
                if (checkConnection.isConnectingToInternet(getActivity()) == true) {
                    showProgressDialog();
                    // make dth recharge using native code
                    makeNativeRecharge();
                } else {
                    Utility.toast(getContextInstance(), "Check your internet connection");
                }
                dialog.dismiss();
                break;
            case R.id.tv_change_company:
                sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                sharedPreferences.edit().putString(constants.RECHARGEFROM,Constants.KEY_DTH_TEXT).commit();
                DTHRecharge rechargeMainFragment = new DTHRecharge();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString()+"").commit();
                break;
            case R.id.tv_change_product:
                /* [START] - 2017_05_02 - If company have only one products than by pressing on change product move to company */
                ArrayList<Product> selectedCompanyProductArrayList = new ArrayList<Product>();
                selectedCompanyProductArrayList = databaseHelper.getProductDetails(strCompanyId);
                if (selectedCompanyProductArrayList.size() == 1) {
                    sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                    DTHRecharge rechargeMainFragment_1 = new DTHRecharge();
                    FragmentManager fragmentManager_1 = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction_1 = fragmentManager_1.beginTransaction();
                    fragmentTransaction_1.replace(R.id.container, rechargeMainFragment_1).addToBackStack(null).commit();
                } else {
                    sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                    Bundle bundle = new Bundle();
                    bundle.putString("fragment_name", "DTH");
                    bundle.putString("company_id", strCompanyId);
                    bundle.putString("company_name", strCompanyName);
                    ProductFragment productFragment = new ProductFragment();
                    productFragment.setArguments(bundle);
                    FragmentManager fragmentManager1 = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
                    fragmentTransaction1.replace(R.id.container, productFragment).addToBackStack(null).commit();
                }
                // [END]
                break;
            case R.id.img_DTHRecharge_AllContacts:
                /* [START] - 2017_05_30 - Display contact application and select contact from them and display selected number. */
                if (Build.VERSION.SDK_INT >= 23) {
                    LogMessage.d("Device is marshmallow");
                    readContactPermission();
                } else {
                    LogMessage.d("Device is not marshmallow");
                    showContacts();
                }
                // [END]
                break;
            case R.id.btn_view_offers_recharge:
                if(validMobile()) {
                    showProgressDialog();
                    makeOfferCall();
                }
                break;
            case R.id.btn_view_customer_info_recharge:
                if(validMobile()) {
                    showProgressDialog();
                    makeNativeCustomerInfo();
                }
                break;
        }

    }

    public boolean validMobile() {
        if (TextUtils.isEmpty(edtMobileNumber.getText().toString().trim())) {
            Utility.amountToast(getContextInstance(), "Please enter smart number");
            return false;
        } else if (strMobileNumber.length() < 10) {
            Utility.amountToast(getContextInstance(), "Please enter valid smart number");
            return false;
        } else {
            return true;
        }
    }

    private void showOfferPopup() {

        dialogAsk.setContentView(R.layout.popup_view_offer_recharge);
        FrameLayout bottomSheet = (FrameLayout) dialogAsk.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogAsk.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogAsk.setTitle(getActivity().getResources().getString(R.string.str_view_offer_title));
        recyclerView = (RecyclerView) dialogAsk.findViewById(R.id.ll_recycler_view_offer_recharge);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        dialogAsk.show();

    }

    private void makeOfferCall() {
        // create new thread for get name by mobile number
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.offerplan;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "mobile_number",
                            "company",
                            "type"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            strMobileNumber,
                            strCompanyName,
                            "dth"
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(OFFER_CALL, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in get name native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    public void parseOfferResponse(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                Dlog.d("Offer response is: " + Constants.decryptAPI(context,encrypted_string));

                JSONObject array1 = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                JSONArray array = array1.getJSONArray("Plan");
                if(array.length()>0) {
                    data = new ArrayList<DthOfferModel>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        offerModel = new DthOfferModel();
                        JSONObject  menu = obj.getJSONObject("rs");
                        Iterator iter = menu.keys();
                        while(iter.hasNext()){
                            String key = (String)iter.next();
                            offerModel.setDuration(key);
                            offerModel.setRs(menu.getString(key).trim());
                        }
                        offerModel.setDesc(obj.getString("desc"));
                        offerModel.setPlan_name(obj.getString("plan_name"));
                        data.add(offerModel);
                    }
                }
                showOfferPopup();
                adapter = new ViewDthOfferAdapter(getActivity(),data);
                recyclerView.setAdapter(adapter);
            } else {
                try {
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContextInstance()).create();
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage("No Offers Found");
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();

                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error while parsing name response");
            LogMessage.e("Error : " + e.getMessage());
            e.printStackTrace();
            final AlertDialog alertDialog = new AlertDialog.Builder(getContextInstance()).create();
            alertDialog.setCancelable(false);
            alertDialog.setMessage("No Offers Found");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            dialogAsk.dismiss();
        }
    }

    private void readContactPermission() {
        LogMessage.i("Checking permission.");
        // BEGIN_INCLUDE(READ_CONTACTS)
        // Check if the READ_CONTACTS permission is already available.
        if (ActivityCompat.checkSelfPermission(getContextInstance(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Phone state permission has not been granted.
            requestReadContactPermission();
        } else {
            // Read SMS permissions is already available, show the camera preview.
            LogMessage.i("Read contact permission has already been granted.");
            showContacts();
        }
        // END_INCLUDE(READ_PHONE_STATE)
    }

    /**
     * Requests the Read phone state permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadContactPermission() {
        LogMessage.i("Read phone state permission has NOT been granted. Requesting permission.");
        // BEGIN_INCLUDE(READ_PHONE_STATE)
        if (ActivityCompat.shouldShowRequestPermissionRationale(DTHRechargeFragment.this.getActivity(),
                Manifest.permission.READ_CONTACTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            LogMessage.i("Displaying READ_CONTACTS permission rationale to provide additional context.");
            // Force fully user to grand permission
            /*ActivityCompat.requestPermissions(DTHRechargeFragment.this.getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);*/
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // READ_CONTACTS permission has not been granted yet. Request it directly.
            /*ActivityCompat.requestPermissions(DTHRechargeFragment.this.getActivity(), new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);*/
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        // END_INCLUDE(READ_PHONE_STATE)
    }

    private void showContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        @SuppressLint("RestrictedApi") List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            LogMessage.i("Received response for Read SMS permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Read SMS permission has been granted
                LogMessage.i("Read SMS permission has now been granted.");
                // Ask user for grand READ_PHONE_STATE permission.
                readContactPermission();
            } else {
                LogMessage.i("Read SMS permission was NOT granted.");
                Utility.amountToast(context, "Until you grant the permission, we canot display the names");
                // again force fully prompt to user for grand permission.
                readContactPermission();
            }
            // END_INCLUDE(permission_result)
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    edtMobileNumber.setText("");
                    edtName.setText("");
                    Uri contactData = data.getData();
                    try {
                        String id = contactData.getLastPathSegment();
                        Cursor phoneCursor = getContextInstance().getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{id},
                                        null);

                        final ArrayList<String> phonesList = new ArrayList<String>();
                        final ArrayList<String> phonesType = new ArrayList<String>();
                        while (phoneCursor.moveToNext()) {
                            // This would allow you get several phone numbers
                            // if the phone numbers were stored in an array
                            String phone = phoneCursor.getString(phoneCursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                            String type = phoneCursor.getString(phoneCursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            phonesList.add(phone);
                            phonesType.add(new Utility().getContactTypeName(type));
                        }
                        phoneCursor.close();

                        if (phonesList.size() == 0) {
                            displayPermissionError("Failed to get contact number.\nMake sure contact permission granted.");
                        } else if (phonesList.size() == 1) {
                            LogMessage.i("Contact No. : " + phonesList.get(0));
                            try {
                                String contactNumber = new Utility().formattedDTHNumber(phonesList.get(0));
                                if (contactNumber.trim().length() == 0) {
                                    Utility.toast(getContextInstance(), "Failed to get contact number.");
                                } else {
                                    edtMobileNumber.setText(contactNumber);
                                }
                            }
                            catch (Exception ex) {
                                LogMessage.e("Error in parse contact number.");
                                LogMessage.e("Error : " + ex.getMessage());
                                ex.printStackTrace();
                                Utility.toast(getContextInstance(), "Failed to get contact number.");
                                edtMobileNumber.setText("");
                                edtName.setText("");
                            }
                        } else {
                            final String[] allContactDetails = new String[phonesList.size()];
                            for (int i = 0; i < phonesList.size(); i++) {
                                allContactDetails[i] = phonesType.get(i) + " : " + phonesList.get(i);
                            }
                            AlertDialog.Builder dialog = new AlertDialog.Builder(getContextInstance());
                            dialog.setTitle("Choose phone");
                            ((AlertDialog.Builder) dialog).setItems(allContactDetails,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            LogMessage.i("Selected number : " + allContactDetails[which]);
                                            String contactNumber = new Utility().formattedDTHNumber(allContactDetails[which]);
                                            if (contactNumber.trim().length() == 0) {
                                                Utility.toast(getContextInstance(), "Failed to get contact number.");
                                            } else {
                                                edtMobileNumber.setText(contactNumber);
                                            }
                                        }
                                    }).create();
                            dialog.show();
                        }
                    }
                    catch (Exception e) {
                        LogMessage.i("Failed to get phone data : " + e.getMessage());
                        e.printStackTrace();
                        Utility.toast(getContextInstance(), "Failed to get contact number.");
                    }
                }
            }
        }
    }

    private void displayPermissionError(String message) {
        try {
            alertDialog_Permission = new AlertDialog.Builder(getContextInstance()).create();
            alertDialog_Permission.setTitle("Contact permission request");
            alertDialog_Permission.setCancelable(true);
            alertDialog_Permission.setMessage(message);
            alertDialog_Permission.setButton("SETTINGS", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog_Permission.dismiss();
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getContextInstance().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                    catch (Exception ex) {
                        LogMessage.e("Error in open setting screen.");
                        LogMessage.e("Error : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
            alertDialog_Permission.show();
        }
        catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getContextInstance(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
    }

    /* [START] - 2017_04_24 - Add native code for add balance, and Remove volley code */
    /* [START] - 2017_05_31 - Get name using dth or mobile number from server and fill in name edit text */
    private void makeGetName() {
        // create new thread for get name by mobile number
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.number_tracer;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "mobile",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            strMobileNumber,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_NAME, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in get name native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse name response
    private void parseNameResponse(String response) {
        LogMessage.i("Name Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if(jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                }
            } else if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                JSONObject object = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                String strName = object.getString("circle_id");
                edtName.setText(strName.trim());
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error while parsing name response");
            LogMessage.e("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }
    // [END]

    private void makeNativeDefaultState() {
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.number_tracer;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "mobile",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            strMobileNumber,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_STATE, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in get state native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseStateResponse(String response) {
        LogMessage.i("Default Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if(jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                }
            } else if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                JSONObject object = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                String strDefaultCircleId = object.getString("circle_id");
                String strDefaultCircleName = "";
                for (int i = 0; i < stateArrayList.size(); i++) {
                    if (strDefaultCircleId.equals(stateArrayList.get(i).getCircle_id())) {
                        strDefaultCircleName = stateArrayList.get(i).getCircle_name();
                    }
                }
                for (int i = 0; i < adapterCircleName.getCount(); i++) {
                    if (strDefaultCircleName.trim().equals(adapterCircleName.getItem(i).toString())) {
                        spiCircle.setSelection(i);
                        break;
                    }
                }
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error while parsing state response");
            LogMessage.e("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void makeNativeRecharge() {
        /* [START] - 2017_05_30 - Add isCredit parameter recharge URL
         * Value = 1 or 0
         * If is credit check box is checked then pass 1
         * if is credit check box is un-checked then pass 0 */
        if (chkIsCredit.isChecked())
            isCreditStatus = "1";
        // get entered name from edit text
        if (!TextUtils.isEmpty(edtName.getText().toString()))
            name = edtName.getText().toString().trim();
        // [END]

        // create new thread for recharge
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set recharge url
                    String url = URL.recharge;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "amount",
                            "product",
                            "circle_id",
                            "mobile",
                            "operator",
                            "service_id",
                            "app",
                            "isCredit",
                            "lati",
                            "long"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            strAmount,
                            strProductId,
                            strCircleId,
                            strMobileNumber,
                            strCompanyId,
                            Constants.dth_id,
                            Constants.APP_VERSION,
                            isCreditStatus,
                            Constants.Lati,
                            Constants.Long
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseRechargeResponse(response);
                }
                catch (Exception ex) {
                    LogMessage.e("Error in get recharge native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    myHandler.obtainMessage(ERROR_RECHARGE, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // method for parse recharge response
    private void parseRechargeResponse(String response) {
        LogMessage.i("Recharge Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if(jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    myHandler.obtainMessage(ERROR_RECHARGE, jsonObject.getString("msg")).sendToTarget();
                }
            } else if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedString = Constants.decryptAPI(context,encrypted_string);
                LogMessage.i("Response : " + encrypted_string);
                LogMessage.i("Decrypt : " + decryptedString);
                myHandler.obtainMessage(SUCCESS_RECHARGE, decryptedString).sendToTarget();
            } else {
                myHandler.obtainMessage(ERROR_RECHARGE, jsonObject.getString("msg")).sendToTarget();
            }
        }
        catch (JSONException ex) {
            LogMessage.e("Error in parse recharge response");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "Recharge Error...").sendToTarget();
        }
    }

    // handle recharge messages
    private Handler myHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == SUCCESS_RECHARGE) {
                dismissProgressDialog();
                parseSuccessRechargeResponse(msg.obj.toString());
            } else if (msg.what == ERROR_RECHARGE) {
                dismissProgressDialog();
                displayRechargeErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_STATE) {
                dismissProgressDialog();
                parseStateResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                Utility.toast(getContextInstance(), msg.obj.toString());
            } else if (msg.what == SUCCESS_NAME) {
                dismissProgressDialog();
                parseNameResponse(msg.obj.toString());
            } else if (msg.what == OFFER_CALL) {
                dismissProgressDialog();
                parseOfferResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_CUSTOMER_INFO) {
                dismissProgressDialog();
                parseCustomerInfoResponse(msg.obj.toString());
            }  else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            } else if (msg.what == AUTHENTICATION_FAIL) {
                dismissProgressDialog();
                Utility.logout(getActivity(), msg.obj.toString());
            }
        }
    };

    private void displayRechargeErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            alertDialog = new AlertDialog.Builder(getContextInstance()).create();
            alertDialog.setTitle("Recharge Failed");
            alertDialog.setCancelable(false);
            alertDialog.setMessage(message);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
        catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getContextInstance(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // Parse success response and display dialog and send notification
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void parseSuccessRechargeResponse(String response) {
        try {
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            txtCompanyName.setText("");
            edtMobileNumber.setText("");
            edtAmount.setText("");
            JSONObject object = new JSONObject(response);
            String recharge_id = object.getString("rechargeid");
            String recharge_status = object.getString("recharge_status");
            String operator_id = object.getString("operatorid");
            String date_time = object.getString("datetime");
            String mo_no = object.getString("mobile");
            String company_id = object.getString("company");
            String product_id = object.getString("product");
            String service = object.getString("service");
            String amount = object.getString("amount");
            String margin = object.getString("margin");
            String balance = object.getString("balance");

            /* [START] - 2017_04_24 - Add RS symbol with amount & Add .00 after amount*/
            // Decimal format
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat format = new DecimalFormat("0.#");
            format.setDecimalFormatSymbols(symbols);
            // Add RS symbol in credit and debit amount
            try {
                if (!TextUtils.equals(amount, "0")) {
                    amount = " " + getContextInstance().getResources().getString(R.string.Rs) + "  " + format.parse(amount).floatValue();
                }
            }
            catch (Exception ex) {
                LogMessage.e("Error in decimal number");
                LogMessage.e("Error : " + ex.getMessage());
                ex.printStackTrace();
                amount = " " + getContextInstance().getResources().getString(R.string.Rs) + "  " + object.getString("amount");
            }
            // [END]

            // Show recharge success dialog
            //showSuccessDialog(recharge_id, recharge_status, company_id, product_id, amount, mo_no, date_time);
            /* [START] - 2017_04_18 - change notification message */
            String notificationMessage = "";
            notificationMessage += recharge_id + "\n";
            notificationMessage += "Recharge Status : " + recharge_status + "\n";
            // notificationMessage += "Date Time : " + date_time + "\n";
            notificationMessage += "Smart Number : " + mo_no + "\n";
            notificationMessage += "Company : " + strCompanyName + "\n";
            notificationMessage += "Product : " + strProductName + "\n";
            notificationMessage += "Amount : " + amount;
            // sendNotification(recharge_id, date_time);
            sendNotification(notificationMessage, date_time);
            // [END]

            //Recharge status screen - 7-3-2019
            companyId = strCompanyId;
            productId = strProductId;
            companyName = strCompanyName;
            productName = strProductName;
            companyImage = strCompanyLogo;
            RechargeStatusFragment rechargeMainFragment = new RechargeStatusFragment();
            Bundle bundle = new Bundle();
            bundle.putString("key", response);
            bundle.putString("from", Constants.KEY_DTH_TEXT);
            rechargeMainFragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString()+"").commit();

        }
        catch (Exception ex) {
            LogMessage.e("Error in parse success message");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            Utility.toast(getContextInstance(), "Recharge Error...");
        }
    }
    // [END]

    /*Method : showConfirmationDialog show dialog for confirmation of recharge details*/
    public void showConfirmationDialog() {
        dialog.setContentView(R.layout.dialog_confirmation);
        FrameLayout bottomSheet = (FrameLayout) dialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        dialog.setTitle("Recharge Confirmation");
        TextView txtCompanyName = (TextView) dialog.findViewById(R.id.tv_company_name_confirm_dialog);
        //TextView txtProductName = (TextView) dialog.findViewById(R.id.tv_product_name_confirm_dialog);
        TextView tv_amount = (TextView) dialog.findViewById(R.id.tv_amount_confirm_dialog);
        TextView tv_state = (TextView) dialog.findViewById(R.id.tv_state_confirm_dialog);
        TextView tv_mo_no = (TextView) dialog.findViewById(R.id.tv_mo_no_confirm_dialog);
        ImageView img = (ImageView) dialog.findViewById(R.id.img_company_comfirm_dialog);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_confirm_dialog);
        Button btn_confirm = (Button) dialog.findViewById(R.id.btn_confirm_confirm_dialog);
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        TextView mobile_label = (TextView) dialog.findViewById(R.id.tv_mo_no_confirm_dialog);
        mobile_label.setText("Smart Number : ");

        LinearLayout circle_confirm_container = (LinearLayout) dialog.findViewById(R.id.circle_confirm_container);
        String circle_visibility = sharedPreferences.getString("circle_visibility", "0");
        if (circle_visibility.compareTo("0") == 0) {
            circle_confirm_container.setVisibility(View.GONE);
        }

        txtCompanyName.setText(strCompanyName + " - " + strProductName);
        String company_logo1 = databaseHelper.getCompanyLogo(strCompanyName);
        if (!TextUtils.isEmpty(company_logo1)) {
            Picasso.with(getActivity()).load(company_logo1).placeholder(R.drawable.placeholder_icon).into(img);
        } else {
            img.setImageResource(R.drawable.placeholder_icon);
        }
        //tv_product_name.setText(strProductName);
        tv_amount.setText(getActivity().getResources().getString(R.string.Rs) + " " + strAmount);
        tv_state.setText(strCircle);
        tv_mo_no.setText(strMobileNumber);
        if (strMobileNumber.isEmpty()) {
            Utility.toast(getContextInstance(), "Enter Smart no.");
        } else if (strAmount.isEmpty()) {
            Utility.toast(getContextInstance(), "Enter Amount.");
        } else if (connection.isConnectingToInternet(getActivity()) == true) {
            dialog.show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Connection Error")
                    .setCancelable(false)
                    .setMessage("Please make sure your device is connected to internet")
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    /*Method : showSuccessDialog show dialog for successfully recharge */
    public void showSuccessDialog(String recharge_id, String recharge_status, String company_id, String product_id, String amount, String mo_no, String date_time) {
        dialog_success.setContentView(R.layout.dialog_success);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        TextView tv_recharge_id = (TextView) dialog_success.findViewById(R.id.tv_recharge_id);
        TextView tv_date_time = (TextView) dialog_success.findViewById(R.id.tv_date_time);
        TextView tv_recharge_status = (TextView) dialog_success.findViewById(R.id.tv_recharge_status);
        TextView tv_mo_no = (TextView) dialog_success.findViewById(R.id.tv_mo_no);
        TextView tv_company = (TextView) dialog_success.findViewById(R.id.tv_company);
        TextView tv_amount = (TextView) dialog_success.findViewById(R.id.tv_amount);
        TextView tv_product = (TextView) dialog_success.findViewById(R.id.tv_product);
        Button btn_ok = (Button) dialog_success.findViewById(R.id.btn_ok);
        tv_recharge_id.setText(recharge_id);
        tv_recharge_status.setText(recharge_status);
        tv_company.setText(strCompanyName);
        tv_product.setText(strProductName);
        tv_amount.setText(amount);
        tv_mo_no.setText(mo_no);
        tv_date_time.setText(date_time);

        TextView success_number = (TextView) dialog_success.findViewById(R.id.success_number);
        success_number.setText("Smart Number :");

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_success.dismiss();
                edtMobileNumber.setText("");
                edtAmount.setText("");
                edtName.setText("");
                chkIsCredit.setChecked(false);
                sharedPreferences.edit().putBoolean(constants.isClicked, false).commit();
            }
        });
        dialog_success.show();
    }

    /*Method : sendNotification send notification when recharge completed*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification(String recharge_id, String date_time) {
        new NotificationUtil(getContextInstance())
                .sendNotification("Recharge completed", "Recharge Id: " + recharge_id, date_time);
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
}
