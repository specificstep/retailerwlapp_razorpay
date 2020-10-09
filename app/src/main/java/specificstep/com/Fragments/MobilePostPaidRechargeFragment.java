package specificstep.com.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import specificstep.com.Activities.BrowsePlansActivity;
import specificstep.com.Activities.HomeActivity;
import specificstep.com.Adapters.ViewOffersRechargeAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Database.PlanTable;
import specificstep.com.Database.PlanTypesTable;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Company;
import specificstep.com.Models.Default;
import specificstep.com.Models.OfferRechargeModel;
import specificstep.com.Models.PlanTypesModel;
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
 * A simple {@link Fragment} subclass.
 */
public class MobilePostPaidRechargeFragment extends Fragment implements View.OnClickListener{

    private View view;
    /* Other class objects */
    private Context context;
    private TransparentProgressDialog transparentProgressDialog;
    private DatabaseHelper databaseHelper;
    private Dialog dialog_success;
    BottomSheetDialog dialog;
    public static BottomSheetDialog dialogAsk;
    private CheckConnection connection;
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private AlertDialog alertDialog, alertDialog_Permission;

    /* All local int and string variables */
    private final String ACTIONBAR_TITLE = "Mobile Postpaid Bill Pay";
    private int PICK_CONTACT = 500, GET_PLAN = 1;
    private final int SUCCESS_RECHARGE = 1, ERROR_RECHARGE = 2, ERROR = 3, SUCCESS_STATE = 4,
            SUCCESS_GET_PLAN_TYPE = 5, SUCCESS_NAME = 6, OFFER_CALL = 7,
            SUCCESS_VIEW_BILL = 8, SUCCESS_WALLET_LIST = 9, AUTHENTICATION_FAIL = 10;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private String strMobileNumber, strAmount, strCompanyName, strProductName, strCircle,
            strProductId, strCompanyId, strUserName, strMacAddress,
            strOtpCode, strCircleId, strCompanyLogo, is_partial, strCustomerName = "";
    private String isCreditStatus = "0", name = "";

    /* All ArrayList */
    private ArrayList<State> stateArrayList;
    private ArrayList<User> userArrayList;
    private ArrayList<Company> companyArrayList;
    private ArrayList<Product> productArrayList;
    private ArrayList<Default> defaultSettingArrayList;

    /* Adapter object */
    private ArrayAdapter<String> adapterCircleName;

    /* All views */
    private ImageView imgAllContacts;
    private EditText edtMobileNumber, edtName;
    public static EditText edtAmount;
    private TextView txtCompanyName, txtProductName, txtChangeProduct/*, txtBrowsePlans*/;
    ImageButton txtChangeCompany;
    private Spinner spiCircle;
    private Button btnProcess, /*btnProcess1,*/ btnBill;
    private CheckBox chkIsCredit;
    private LinearLayout llNameView, llIsCreditView, llCircleContainer, llAmount;
    ImageView company_image;
    // [END]

    String company_name1 = "";
    String product_name1 = "";
    String company_id1 = "";
    String product_id1 = "";

    //OfferPopup
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<OfferRechargeModel> data;
    ImageView imgNumberIcon;
    private MenuItem menuItem;
    ArrayList<String> menuWallet;

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;

    //multiple tags 24-8-2020
    LinearLayout lnrSecondTag, lnrThirdTag, lnrFourTag;
    EditText edtSecondTag, edtThirdTag, edtFourTag;

    public static String companyId = "", productId = "", companyName = "",
            productName = "", companyImage = "";

    private Context getContextInstance() {
        if (context == null) {
            context = MobilePostPaidRechargeFragment.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public MobilePostPaidRechargeFragment() {
        // Required empty public constructor
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mobile_post_paid_recharge2, container, false);

        /* [START] - 2017_04_18 set title bar as Mobile recharge */
        mainActivity().getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        // [END]

        // initialise controls and variables
        initControls();
        edtMobileNumber.setText("");
        edtAmount.setText("");
        // set listener
        setListener();

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
    @Override
    public void onResume() {
        super.onResume();

        /* [START] - 2017_04_18 set title bar as Mobile recharge */
        mainActivity().getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        // [END]

        if (sharedPreferences.getBoolean(constants.isClicked, false) == true &&
                sharedPreferences.getString(constants.RECHARGEFROM,"").equals(Constants.KEY_MOB_POSTPAID_TEXT)) {
            edtMobileNumber.setText(sharedPreferences.getString(constants.MOBILENUMBER, ""));
            edtAmount.setText(sharedPreferences.getString(constants.AMOUNT, ""));
        }

        if(Constants.singleConstact.equals("")) {
        } else {
            edtMobileNumber.setText(Constants.singleConstact);
            Constants.singleConstact = "";
        }

        // Display bottom bar in add balance
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initControls() {
        /* [START] - bind view */
        llCircleContainer = (LinearLayout) view.findViewById(R.id.circle_container);
        llNameView = (LinearLayout) view.findViewById(R.id.ll_Recharge_NameView);
        llIsCreditView = (LinearLayout) view.findViewById(R.id.ll_Recharge_IsCreditView);
        edtMobileNumber = (EditText) view.findViewById(R.id.edt_mo_no_fragment_recharge);
        edtAmount = (EditText) view.findViewById(R.id.edt_amt_fragment_recharge);
        edtName = (EditText) view.findViewById(R.id.edt_Recharge_Name);
        btnProcess = (Button) view.findViewById(R.id.btn_proceed_fragment_recharge);
        spiCircle = (Spinner) view.findViewById(R.id.sp_circle_fragment_recharge);
        txtCompanyName = (TextView) view.findViewById(R.id.tv_company_name_fragment_mobile_recharge);
        txtProductName = (TextView) view.findViewById(R.id.tv_product_name_fragment_mobile_recharge);
        txtChangeCompany = (ImageButton) view.findViewById(R.id.tv_change_company);
        txtChangeProduct = (TextView) view.findViewById(R.id.tv_change_product);
        company_image = (ImageView) view.findViewById(R.id.tv_company_image);
        llAmount = (LinearLayout) view.findViewById(R.id.ll_Recharge_AmountView);
        btnBill = (Button) view.findViewById(R.id.btn_view_bill_fragment_recharge);
        /* [START] - 2017_05_23 - Add contact image in mobile recharge screen. */
        imgAllContacts = (ImageView) view.findViewById(R.id.img_Recharge_AllContacts);
        // [END]
        imgNumberIcon = (ImageView) view.findViewById(R.id.imgNumberIcon);
        imgNumberIcon.setBackground(getResources().getDrawable(R.drawable.ic_electricity));

        chkIsCredit = (CheckBox) view.findViewById(R.id.chk_Recharge_IsCredit);

        lnrSecondTag = (LinearLayout) view.findViewById(R.id.lnrPostpaidSecondTag);
        lnrThirdTag = (LinearLayout) view.findViewById(R.id.lnrPostpaidThirdTag);
        lnrFourTag = (LinearLayout) view.findViewById(R.id.lnrPostpaidFourTag);
        edtSecondTag = (EditText) view.findViewById(R.id.edtPostpaidSecondTag);
        edtThirdTag = (EditText) view.findViewById(R.id.edtPostpaidThirdTag);
        edtFourTag = (EditText) view.findViewById(R.id.edtPostpaidFourTag);

        /* [START] - initialise objects */
        // init database
        databaseHelper = new DatabaseHelper(getContextInstance());
        // init Constants string class object
        constants = new Constants();
        // init shared preferences
        sharedPreferences = getContextInstance().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        // init CheckConnection class object
        connection = new CheckConnection();
        // init progress bar
        transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
        // init all array list of State, User, Company, Product
        stateArrayList = new ArrayList<State>();
        userArrayList = new ArrayList<User>();
        companyArrayList = new ArrayList<Company>();
        defaultSettingArrayList = new ArrayList<Default>();
        productArrayList = new ArrayList<Product>();
        // init dialog
        dialog = new BottomSheetDialog(getContextInstance());
        dialog_success = new Dialog(getContextInstance());
        dialogAsk = new BottomSheetDialog(getContextInstance());
        // [END]

        /* [START] - select data from database and store in array list */
        // get state data
        stateArrayList = databaseHelper.getStateDetails();
        // get user data
        userArrayList = databaseHelper.getUserDetail();
        // get settings data
        defaultSettingArrayList = databaseHelper.getDefaultSettings();
        // get company name by mobile
        companyArrayList = databaseHelper.getCompanyDetails("Mobile");
        // [END]

        /* Get bundle data */
        getBundleData();

        /* [START] - get required value from user array list */
        strOtpCode = userArrayList.get(0).getOtp_code();
        strUserName = userArrayList.get(0).getUser_name();
        strMacAddress = userArrayList.get(0).getDevice_id();
        LogMessage.d("strMacAddress : " + strMacAddress);
        // [END]

        if(strCompanyName!=null && strProductName!=null) {

            /* [START] - set company name and product name in TextView */
            txtCompanyName.setText(strCompanyName + "-" + strProductName);
            if(strCompanyLogo == null || strCompanyLogo.equals("")) {
                company_image.setBackground(getResources().getDrawable(R.drawable.placeholder_icon));
            } else {
                Picasso.with(context).load(strCompanyLogo).placeholder(R.drawable.placeholder_icon).into(company_image);
            }
            if(is_partial != null) {
                if(is_partial.equals("1")) {
                    btnBill.setVisibility(View.VISIBLE);
                    btnProcess.setVisibility(View.GONE);
                    llAmount.setVisibility(View.GONE);
                } else {
                    btnBill.setVisibility(View.GONE);
                    btnProcess.setVisibility(View.VISIBLE);
                    llAmount.setVisibility(View.VISIBLE);
                }
            } else {
                btnBill.setVisibility(View.GONE);
                btnProcess.setVisibility(View.VISIBLE);
                llAmount.setVisibility(View.VISIBLE);
            }
        }
        // [END]

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

        /* [START] - set previous entries of mobile no and mount
        if user had clicked on change company or change product */
        if (sharedPreferences.getBoolean(constants.isClicked, false) == true &&
                sharedPreferences.getString(constants.RECHARGEFROM,"").equals(Constants.KEY_MOB_POSTPAID_TEXT)) {
            edtMobileNumber.setText(sharedPreferences.getString(constants.MOBILENUMBER, ""));
            edtAmount.setText(sharedPreferences.getString(constants.AMOUNT, ""));
        }
        // [END]

        /* [START] - get states data from SQLite */
        ArrayList<String> circle_array = new ArrayList<String>();
        for (int i = 0; i < stateArrayList.size(); i++) {
            circle_array.add(stateArrayList.get(i).getCircle_name());
        }
        // set array list in adapter
        adapterCircleName = new ArrayAdapter<String>(getContextInstance(), R.layout.adapter_spinner, circle_array);
        // set adapter in circle spinner
        spiCircle.setAdapter(adapterCircleName);
        // set default state in circle spinner
        for (int i = 0; i < adapterCircleName.getCount(); i++) {
            if (defaultSettingArrayList.get(0).getState_name().trim().equals(adapterCircleName.getItem(i).toString())) {
                spiCircle.setSelection(i);
                break;
            }
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

                /*try {
                    if(menuItem != null) {
                        if (walletsModelList.size() > 0) {
                            menuItem.setVisible(true);
                        } else {
                            menuItem.setVisible(false);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }*/

            } else {
            }
        }
        catch(JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void getBundleData() {
        // [START] - Get bundle data
        Bundle bundle = MobilePostPaidRechargeFragment.this.getArguments();
        if (bundle != null) {
            strCompanyId = bundle.getString("company_id");
            strProductId = bundle.getString("product_id");
            strCompanyName = bundle.getString("company_name");
            strProductName = bundle.getString("product_name");
            strCompanyLogo = bundle.getString("company_image");
            is_partial = bundle.getString("is_partial");
            String productLogo = bundle.getString("product_image");
        }
        // [END]
    }

    private void setListener() {
        txtChangeCompany.setOnClickListener(this);
        txtChangeProduct.setOnClickListener(this);
        btnProcess.setOnClickListener(this);
        imgAllContacts.setOnClickListener(this);
        btnBill.setOnClickListener(this);

        /* Set Text Change listener, get default value of state spinner, when user insert mobile no */
        edtMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                strMobileNumber = edtMobileNumber.getText().toString();
                if (strMobileNumber.length() == 10) {
                    CheckConnection checkConnection = new CheckConnection();
                    if (checkConnection.isConnectingToInternet(getActivity()) == true) {
                        //makeNativeDefaultState();
                        // makeGetName();
                    } else {
                        LogMessage.d("Internet connection not found");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        /* Set on item selected listener in circle spinner */
        spiCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorWhite));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        @SuppressLint("RestrictedApi") List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        if (requestCode == GET_PLAN) {
            if (resultCode == Activity.RESULT_OK) {
                String planRs = data.getStringExtra(Constants.KEY_PLAN_RS);
                String productId = data.getStringExtra(Constants.KEY_PRODUCT_ID);
                String productName = data.getStringExtra(Constants.KEY_PRODUCT_NAME);
                LogMessage.d("planRs : " + planRs + " productId : " + productId + " productName : " + productName);
                edtAmount.setText(planRs);
                strProductId = productId;
                strProductName = productName;
                txtProductName.setText(strProductName);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                LogMessage.d("Activity Result Cancel");
            }
        }
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
                                String contactNumber = new Utility().formattedContactNumber(phonesList.get(0));
                                if (contactNumber.trim().length() == 0) {
                                    Utility.amountToast(getContextInstance(), "Failed to get contact number.");
                                } else {
                                    edtMobileNumber.setText(contactNumber);
                                    Constants.singleConstact = contactNumber;
                                }
                            }
                            catch (Exception ex) {
                                LogMessage.e("Error in parse contact number.");
                                LogMessage.e("Error : " + ex.getMessage());
                                ex.printStackTrace();
                                Utility.amountToast(getContextInstance(), "Failed to get contact number.");
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
                                            String contactNumber = new Utility().formattedContactNumber(allContactDetails[which]);
                                            if (contactNumber.trim().length() == 0) {
                                                Utility.amountToast(getContextInstance(), "Failed to get contact number.");
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
                        Utility.amountToast(getContextInstance(), "Failed to get contact number.");
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {
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
                        Utility.amountToast(getContextInstance(), "Please enter amount");
                    }
                    break;
                case R.id.btn_cancel_confirm_dialog:
                    dialog.dismiss();
                    break;
                case R.id.btn_confirm_confirm_dialog:
                    CheckConnection checkConnection = new CheckConnection();
                    if (checkConnection.isConnectingToInternet(getContextInstance()) == true) {
                        // Show progress dialog
                        showProgressDialog();
                        // make mobile recharge using native code
                        makeNativeRecharge();
                    } else {
                        Utility.amountToast(getContextInstance(), "Check your internet connection");
                    }
                    dialog.dismiss();
                    break;
                case R.id.tv_change_company:
                    sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                    sharedPreferences.edit().putString(constants.RECHARGEFROM,Constants.KEY_MOB_POSTPAID_TEXT).commit();
                    MobilePostPaidRecharge rechargeMainFragment = new MobilePostPaidRecharge();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString()+"").commit();
                    break;
                case R.id.tv_change_product:
                    /* [START] - 2017_05_02 - If company have only one products than by pressing on change product move to company. */
                    ArrayList<Product> selectedCompanyProductArrayList = new ArrayList<Product>();
                    selectedCompanyProductArrayList = databaseHelper.getProductDetails(strCompanyId);
                    if (selectedCompanyProductArrayList.size() == 1) {
                        sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                        MobilePostPaidRecharge rechargeMainFragment_1 = new MobilePostPaidRecharge();
                        FragmentManager fragmentManager_1 = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction_1 = fragmentManager_1.beginTransaction();
                        fragmentTransaction_1.replace(R.id.container, rechargeMainFragment_1).addToBackStack(null).commit();
                    } else {
                        sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                        Bundle bundle = new Bundle();
                        bundle.putString("fragment_name", Constants.KEY_MOB_POSTPAID_TEXT);
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
                case R.id.img_Recharge_AllContacts:
                    /* [START] - 2017_05_23 - Display contact application and select contact from them and display selected number. */
                    if (Build.VERSION.SDK_INT >= 23) {
                        readContactPermission();
                    } else {
                        showContacts();
                    }
                    // [END]
                    break;
                case R.id.btn_view_bill_fragment_recharge:
                    if (!TextUtils.isEmpty(edtMobileNumber.getText().toString())) {
                        showProgressDialog();
                        makeNativeViewBill();
                    }
                    break;
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }

    private void makeNativeViewBill() {
        // create new thread for get name by mobile number
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.electricity_recharge_bill;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "product",
                            "mobile"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            strProductId,
                            strMobileNumber
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    Dlog.d("Electricity Bill Response: " + response);
                    myHandler.obtainMessage(SUCCESS_VIEW_BILL, response).sendToTarget();
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

    public void parseBillResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                Dlog.d("Electricity Bill response is: " + Constants.decryptAPI(context,encrypted_string));
                JSONObject obj = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                edtAmount.setText(obj.getString("amount")+"");
                strCustomerName = obj.getString("customer_name");
                btnProcess.performClick();
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
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error while parsing name response");
            LogMessage.e("Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showOfferPopup() {

        dialogAsk.setContentView(R.layout.popup_view_offer_recharge);
        dialogAsk.getWindow().getAttributes().windowAnimations = R.style.Animation;
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
                            "company"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            strMobileNumber,
                            strCompanyName
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    Dlog.d("Offer Response: " + response);
                    myHandler.obtainMessage(OFFER_CALL, response).sendToTarget();
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

    public void parseOfferResponse(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                Dlog.d("Offer response is: " + Constants.decryptAPI(context,encrypted_string));
                JSONArray array = new JSONArray(Constants.decryptAPI(context,encrypted_string));
                data = new ArrayList<OfferRechargeModel>();
                for(int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    OfferRechargeModel model = new OfferRechargeModel();
                    model.setRs(obj.getString("rs"));
                    model.setDesc(obj.getString("desc"));
                    data.add(model);
                }
                showOfferPopup();
                adapter = new ViewOffersRechargeAdapter("mobile",getActivity(),data);
                recyclerView.setAdapter(adapter);
            } else {
                final AlertDialog alertDialog = new AlertDialog.Builder(getContextInstance()).create();
                alertDialog.setCancelable(false);
                alertDialog.setMessage("No Offers Found");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        }
        catch (JSONException e) {
            LogMessage.e("Error while parsing name response");
            LogMessage.e("Error : " + e.getMessage());
            e.printStackTrace();
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(MobilePostPaidRechargeFragment.this.getActivity(),
                Manifest.permission.READ_CONTACTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            LogMessage.i("Displaying READ_CONTACTS permission rationale to provide additional context.");
            // Force fully user to grand permission
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // READ_CONTACTS permission has not been granted yet. Request it directly.
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
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseStateResponse(String response) {
        LogMessage.i("Default Response" + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("0")) {
                if(jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    Utility.amountToast(getContextInstance(), jsonObject.getString("msg"));
                }
            } else if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                JSONObject object = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                String strDefaultCircleId = object.getString("circle_id");
                String strDefaultCircleName = "";

                company_id1 = object.getString("company_id");

                String company_name =  databaseHelper.getCompanyName(object.getString("company_id"));

                String product_name =  databaseHelper.getProductName(object.getString("company_id"));
                product_id1 =  databaseHelper.getProductID(object.getString("company_id"));

                String logo = databaseHelper.getCompanyLogo(company_name);
                if(logo!=null){
                    Picasso.with(context).load(logo).placeholder(R.drawable.placeholder_icon).into(company_image);

                }
                // String company_name =  databaseHelper.getCompanyName("13"); //for vodaphone
                Log.e("", "company_name >> : "+company_name );
                Log.e("", "product_name >> : "+product_name );
                if(company_name!=null && product_name!=null){
                    txtCompanyName.setText(company_name +"-" +product_name);

                    company_name1 = company_name;
                    product_name1 = product_name;
                }

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
                    String url = URL.electricity_recharge;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "service_id",
                            "amount",
                            "product",
                            "circle_id",
                            "mobile",
                            "operator",
                            "isCredit",
                            "lati",
                            "long"
                    };

                    // set parameters values in string array
                    if(strCompanyId == null ){
                        strCompanyId = company_id1;
                    }
                    if(strProductId == null){
                        strProductId = product_id1;
                    }
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            Constants.mobile_postpaid_id,
                            strAmount,
                            strProductId,
                            strCircleId,
                            strMobileNumber,
                            strCompanyId,
                            isCreditStatus,
                            Constants.Lati,
                            Constants.Long
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseRechargeResponse(response);
                }
                catch (Exception ex) {
                    LogMessage.e("Error in recharge native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
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
                Utility.amountToast(getContextInstance(), msg.obj.toString());
            } else if (msg.what == SUCCESS_GET_PLAN_TYPE) {
                dismissProgressDialog();
                parseGetPlanTypeResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_NAME) {
                dismissProgressDialog();
                parseNameResponse(msg.obj.toString());
            } else if (msg.what == OFFER_CALL) {
                dismissProgressDialog();
                parseOfferResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_VIEW_BILL) {
                dismissProgressDialog();
                parseBillResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
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
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            edtMobileNumber.setText("");
            edtAmount.setText("");
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
                Utility.amountToast(getContextInstance(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
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
                Utility.amountToast(getContextInstance(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
    }

    // Parse success response and display dialog and send notification
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void parseSuccessRechargeResponse(String response) {
        try {

            //remove number from preference and earese data in to edittext
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            txtCompanyName.setText("");
            edtMobileNumber.setText("");
            edtAmount.setText("");
            String company_name = null;

            JSONObject object = new JSONObject(response);
            String recharge_id = object.getString("rechargeid");
            String recharge_status = object.getString("recharge_status");
            String date_time = object.getString("datetime");
            String mo_no = object.getString("mobile");
            String company_id = object.getString("company");
            String amount = object.getString("amount");

            for (int i = 0; i < companyArrayList.size(); i++) {
                if (company_id.equals(companyArrayList.get(i).getId())) {
                    company_name = companyArrayList.get(i).getCompany_name();
                }
            }
            productArrayList = databaseHelper.getProductDetails(company_id);
            String product_name = productArrayList.get(0).getProduct_name();

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
                amount = getContextInstance().getResources().getString(R.string.Rs) + "  " + object.getString("amount");
            }
            // [END]

            /* [START] - 2017_04_18 - change notification message */
            String notificationMessage = "";
            notificationMessage += recharge_id + "\n";
            notificationMessage += "Recharge Status : " + recharge_status + "\n";
            // notificationMessage += "Date Time : " + date_time + "\n";
            notificationMessage += "Number : " + mo_no + "\n";
            notificationMessage += "Company : " + strCompanyName + "\n";
            notificationMessage += "Product : " + strProductName + "\n";
            notificationMessage += "Amount : " + amount;
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
            bundle.putString("from", Constants.KEY_MOB_POSTPAID_TEXT);
            rechargeMainFragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString()+"").commit();

        }
        catch (Exception ex) {
            LogMessage.e("Error in parse success message");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            Utility.amountToast(getContextInstance(), "Recharge Error...");
        }
    }

    private void getNativeGetPlanType() {
        PlanTypesTable planTypesTable = new PlanTypesTable(getContextInstance());
        PlanTable planTable = new PlanTable(getContextInstance());
        if (planTypesTable.select_Data().size() > 0) {
            planTypesTable.delete_All();
        }
        if (planTable.select_Data().size() > 0) {
            planTable.delete_All();
        }
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.GET_PLANS_TYPE;
                    String response = InternetUtil.getServer_Data(url);
                    if (response == null || TextUtils.isEmpty(response)) {
                        myHandler.obtainMessage(ERROR, "Not any plan found.").sendToTarget();
                    } else {
                        myHandler.obtainMessage(SUCCESS_GET_PLAN_TYPE, response).sendToTarget();
                    }
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

    private void parseGetPlanTypeResponse(String response) {
        LogMessage.i("Get plan type response : " + response);
        try {
            PlanTypesTable planTypesTable = new PlanTypesTable(getContextInstance());
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                LogMessage.d("Plan Type ID : " + jsonObject.getString("id"));
                LogMessage.d("Plan Type name : " + jsonObject.getString("name"));

                // insert plan type data into database.
                PlanTypesModel planTypesModel = new PlanTypesModel();
                planTypesModel.name = jsonObject.getString("name");
                planTypesModel.id = jsonObject.getString("id");
                planTypesTable.insert(planTypesModel);
            }
            /* [START] - 2017_05_25 - Display Browse Plans screen. select particular plan and display selected amount in amount edit text */
            Intent intent = new Intent(getContextInstance(), BrowsePlansActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_OPERATOR, strCompanyName);
            bundle.putString(Constants.KEY_COMPANY_ID, strCompanyId);
            bundle.putString(Constants.KEY_STATE, strCircle);
            bundle.putString(Constants.KEY_CIRCLE_ID, strCircleId);
            intent.putExtras(bundle);
            startActivityForResult(intent, GET_PLAN);
            // [END]
        }
        catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "Not any plan found.").sendToTarget();
        }
    }
    // [END]

    /* Method : showConfirmationDialog show dialog for confirmation of recharge details */
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
        TextView txtCustomerName = (TextView) dialog.findViewById(R.id.tv_customer_name_confirm_dialog);
        //TextView txtProductName = (TextView) dialog.findViewById(R.id.tv_product_name_confirm_dialog);
        TextView tv_amount = (TextView) dialog.findViewById(R.id.tv_amount_confirm_dialog);
        //TextView tv_state = (TextView) dialog.findViewById(R.id.tv_state_confirm_dialog);
        TextView tv_mo_no = (TextView) dialog.findViewById(R.id.tv_mo_no_confirm_dialog);
        ImageView img = (ImageView) dialog.findViewById(R.id.img_company_comfirm_dialog);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel_confirm_dialog);
        Button btn_confirm = (Button) dialog.findViewById(R.id.btn_confirm_confirm_dialog);
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        if(!TextUtils.isEmpty(strCustomerName)) {
            txtCustomerName.setText("Customer Name: " + strCustomerName);
            txtCustomerName.setVisibility(View.VISIBLE);
        } else {
            txtCustomerName.setVisibility(View.GONE);
        }

        if(strCompanyName!=null && strCompanyName.length()>0){
            txtCompanyName.setText(strCompanyName + " - " + strProductName);
            String company_logo1 = databaseHelper.getCompanyLogo(strCompanyName);
            if (!TextUtils.isEmpty(company_logo1)) {
                Picasso.with(getActivity()).load(company_logo1).placeholder(R.drawable.placeholder_icon).into(img);
            } else {
                img.setImageResource(R.drawable.placeholder_icon);
            }
        }else{
            txtCompanyName.setText(company_name1 + " - " + product_name1);
            String company_logo1 = databaseHelper.getCompanyLogo(company_name1);
            if (!TextUtils.isEmpty(company_logo1)) {
                Picasso.with(getActivity()).load(company_logo1).placeholder(R.drawable.placeholder_icon).into(img);
            } else {
                img.setImageResource(R.drawable.placeholder_icon);
            }
        }

        tv_amount.setText(getActivity().getResources().getString(R.string.Rs) + " " + strAmount);
        tv_mo_no.setText(strMobileNumber);

        LinearLayout circle_confirm_container = (LinearLayout) dialog.findViewById(R.id.circle_confirm_container);
        String circle_visibility = sharedPreferences.getString("circle_visibility", "0");
        if (circle_visibility.compareTo("0") == 0) {
            circle_confirm_container.setVisibility(View.GONE);
        }

        if (strMobileNumber.isEmpty()) {
            Utility.amountToast(getContextInstance(), "Enter service no.");
        } else if(TextUtils.isEmpty(txtCompanyName.getText().toString())) {
            Utility.amountToast(getContextInstance(), "Choose Company.");
        } else if (strAmount.isEmpty()) {
            Utility.amountToast(getContextInstance(), "Enter Amount.");
        } else if (strMobileNumber.length() < 10) {
            edtMobileNumber.setError("Enter valid mobile number");
        } else if (connection.isConnectingToInternet(getContextInstance()) == true) {
            dialog.show();
        } else {
            new AlertDialog.Builder(getContextInstance())
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

    /* Method : showSuccessDialog show dialog for successfully recharge */
    public void showSuccessDialog(final String recharge_id, String recharge_status, String company_id, String product_id, String amount, String mo_no, String date_time) {
        dialog_success.setContentView(R.layout.dialog_success);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        // dialog_success.getWindow().setLayout(width, height);

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
        if(strCompanyName == null ){
            strCompanyName =   company_name1;
        }
        if(strProductName == null){
            strProductName = product_name1;
        }
        tv_company.setText(strCompanyName);
        tv_product.setText(strProductName);
        tv_amount.setText(amount);
        tv_mo_no.setText(mo_no);
        tv_date_time.setText(date_time);

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

    /* Method : sendNotification send notification when recharge completed */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification(String recharge_id, String date_time) {
        new NotificationUtil(getContextInstance())
                .sendNotification("Recharge Submitted",
                        "Recharge Id: " + recharge_id, date_time);

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
