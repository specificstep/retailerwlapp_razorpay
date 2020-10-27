package specificstep.com.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import specificstep.com.Adapters.NavigationDrawerAdapter;
import specificstep.com.BuildConfig;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Database.NotificationTable;
import specificstep.com.Fragments.AccountLedgerFragment;
import specificstep.com.Fragments.CashbookFragment;
import specificstep.com.Fragments.ChangePasswordFragment;
import specificstep.com.Fragments.ComplainReportFragment;
import specificstep.com.Fragments.DMTFragment;
import specificstep.com.Fragments.DTHRecharge;
import specificstep.com.Fragments.ElectricityRecharge;
import specificstep.com.Fragments.GasRecharge;
import specificstep.com.Fragments.MobilePostPaidRecharge;
import specificstep.com.Fragments.MobileRecharge;
import specificstep.com.Fragments.NotificationFragment;
import specificstep.com.Fragments.OnlinePaymentFragment;
import specificstep.com.Fragments.ParentUserFragment;
import specificstep.com.Fragments.PaymentRequestFragment;
import specificstep.com.Fragments.RecentTransactionFragment;
import specificstep.com.Fragments.UpdateData;
import specificstep.com.Fragments.WaterRecharge;
import specificstep.com.GlobalClasses.AppController;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.ServicesModel;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.AlertModels;
import specificstep.com.Models.NavigationModels;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.MyPrefs;
import specificstep.com.utility.UpdateData_ApiCall;
import specificstep.com.utility.Utility;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private final int ERROR = 2, SUCCESSALERT = 3, SUCCESSSERVICE = 4,
            AUTHENTICATION_FAIL = 5;
    /* [START] - Other class objects */
    private Constants constants;
    public static Context context;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;
    // [END]

    /* [START] - Controls objects */
    private ActionBarDrawerToggle toggle;
    private TextView tv_email, tv_name;
    private Spinner tv_multi_wallet;
    private LinearLayout tv_lnr_multi_wallet;
    // Default bottom bar
    private LinearLayout llDefault, llUpdate, llChangePassword, llNotification, llLogout;
    private TextView txtDefaultTotalNotification;
    // Notification bottom bar
    private LinearLayout llNotificationBottom, llNotificationRecharge, llNotificationRecentTransaction, llNotificationTransactionSearch;
    // Cashbook bottom bar
    private LinearLayout llCashbookBottom, llCashbookRecharge, llCashbookRecentTrasaction, llCashbookNotification;
    private TextView txtCashbookTotalNotification;
    // Change password bottom bar
    private LinearLayout llChangePasswordBottom, llChangePasswordRecharge, llChangePasswordRecentTransaction, llChangePasswordNotification;
    private TextView txtChangePasswordTotalNotification;
    // Recharge bottom bar
    private LinearLayout llRechargeBottom, llRechargeRecentTransaction, llRechargeTransaction, llRechargeNotification;
    private TextView txtRechargeTotalNotification;
    // Transaction search
    private LinearLayout llTransactionSearchBottom, llTransactionSearchRecharge, llTransactionSearchRecentTransaction, llTransactionSearchNotification;
    private TextView txtTransactionSearchTotalNotification;
    // Recent transaction
    private LinearLayout llRecentTransactionBottom, llRecentTransactionRecharge, llRecentTransactionTransactionSearch, llRecentTransactionNotification;
    private TextView txtRecentTransactionTotalNotification;

    // DMT
    private LinearLayout llBottomDMT, llTransactionSearchDMT, llTransactionSearchRecentTransactionDMT, llSenderSearchDMT;

    /* [START] - Variables */
    private int position;
    private ArrayList<User> userArrayList;
    // Screen Number

    // [END]
    private final int WATER = 15, MOBILE_POSTPAID = 14, GAS = 13, SUCCESS_WALLET_LIST = 12, PAYMENT_REQUEST = 11,
            DMT_TRANSACTION_LIST = 10, DMT = 9, NOTIFICATION = 8,
            CHANGE_PASSWORD = 7, UPDATE_DATA = 6, ACCOUNT_LEGER = 5,
            CASH_BOOK = 4, COMPLAIN_REPORT = 3, TRANSACTION_SEARCH = 2,
            RECENT_TRANSACTION = 1, RECHARGE = 0, ONLINE_PAYMENT = 19;
    // Notification receiver
    private BroadcastReceiver notificationReceiver = null;
    public static final String ACTION_REFRESH_HOMEACTIVITY = "specificstep.com.metroenterprise.REFRESH_NOTIFICATION";

    // Navigation view
    private NavigationView navigationView = null;
    private DrawerLayout drawer = null;

    // Custom navigation menu
    private ListView lstNavigation;

    // all menu names
    private final String MENU_HOME = "Home";
    private final String MENU_RECHARGE = "Mobile Prepaid Recharge";
    private final String MENU_DTH = "DTH Recharge";
    private final String MENU_ELECTRICITY = "Electricity Bill Pay";
    private final String MENU_RECENT_TRANSACTION = "Recent Transaction";
    private final String MENU_TRANSACTION_SEARCH = "Transaction Search";
    private final String MENU_COMPLAIN_REPORT = "Complain Report";
    private final String MENU_CASH_BOOK = "Payment Report";
    private final String MENU_ACCOUNT_LEDGER = "Account Ledger";
    private final String MENU_UPDATE_DATA = "Update Data";
    private final String MENU_CHANGE_PASSWORD = "Change Password";
    private final String MENU_DMT = "DMT";
    private final String MENU_DMT_TRANSACTION_LIST = "Transaction Reports";
    private final String MENU_PAYMENT_REQUEST = "Payment Request";
    private final String MENU_GAS = "Gas Bill Pay";
    private final String MENU_MOBILE_POSTPAID = "Mobile Postpaid Bill Pay";
    private final String MENU_WATER = "Water Bill Pay";
    private final String MENU_NOTIFICATION = "Notification";
    private final String MENU_PARENT_USER = "Parent User";
    private final String MENU_LOGOUT = "Log Out";
    private final String MENU_ANOTHER_USER = "Login with Other number";
    private final String MENU_SHARE = "Share";
    private final String MENU_ONLINE_PAYMENT = "Wallet Topup";
    String TAG = "MainActivity :: ";
    TextView txtAlert;

    List<String> alertModelsList;
    AlertModels alertModels;
    AppController app;
    ArrayList<ServicesModel> serviceModelArrayList;
    ArrayList<ServicesModel> serviceModelArrayListFinal;
    ServicesModel servicesModel;

    //multiwallet 4-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    ArrayAdapter<String> adapter;
    ArrayList<NavigationModels> stringArrayList;
    ArrayList<NavigationModels> mEvents;
    public static String drawerPos = "";
    Thread thread;
    TextView txtVersion;
    private MyPrefs prefs;
    UpdateData_ApiCall updateData_apiCall;

    private Context getContextInstance() {
        if (context == null) {
            context = HomeActivity.this;
            return context;
        } else {
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = HomeActivity.this;
        context = (AppController) getApplication();
        initControls();
        getBundleData();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                makeWalletCall();
                checkServices();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = HomeActivity.this.getCurrentFocus();
                if (view == null) {
                    view = new View(HomeActivity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        setCustomNavigation();
        checkServices();
        tv_name = (TextView) header.findViewById(R.id.tv_header_name);
        tv_email = (TextView) header.findViewById(R.id.tv_header_email);
        tv_multi_wallet = (Spinner) header.findViewById(R.id.tv_multi_wallet);
        tv_lnr_multi_wallet = (LinearLayout) header.findViewById(R.id.tv_lnr_multi_wallet);

        Constants.chaneBackground(HomeActivity.this, (LinearLayout) header.findViewById(R.id.lnrNavHeader));
        //set icon as per package
        Constants.chaneIcon(HomeActivity.this, (CircleImageView) header.findViewById(R.id.profile_image));

        initBottomNavigation();
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                // your code here...
                makeAlertCall();
            }
        };
        timer.schedule(hourlyTask, 0l, 1000 * 10 * 60);
        /* Set header content of navigation drawer */
        if (userArrayList != null && userArrayList.size() > 0) {
            tv_email.setText(userArrayList.get(0).getUser_name());
            tv_name.setText(userArrayList.get(0).getName());
        }

        /* Set Current Fragment according to selected item from MainActivity */
        if (databaseHelper.checkEmpty() == false) {
            position = 6;
            displayBottomNavigationDynamic(UPDATE_DATA);
        }
        /*if (position == 0) {
            sharedPreferences.edit().putInt(constants.SELECTED_TAB, curr_pos).commit();
            openFragment(MENU_RECHARGE, new RechargeMainFragment()); //skip above and direct open this fragment
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            displayBottomNavigationDynamic(RECHARGE);
        } */
        if (position == 0) {
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            openFragment(MENU_RECHARGE, new MobileRecharge());
            displayBottomNavigationDynamic(RECHARGE);
        } else if (position == 17) {
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            openFragment(MENU_DTH, new DTHRecharge());
            displayBottomNavigationDynamic(RECHARGE);
        } else if (position == 18) {
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            openFragment(MENU_ELECTRICITY, new ElectricityRecharge());
            displayBottomNavigationDynamic(RECHARGE);
        } else if (position == 1) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (position == 2) {
            openTransactionSearchFragment();
            displayBottomNavigationDynamic(TRANSACTION_SEARCH);
        } else if (position == 3) {
            openFragment(MENU_COMPLAIN_REPORT, new ComplainReportFragment());
            displayBottomNavigationDynamic(COMPLAIN_REPORT);
        } else if (position == 4) {
            openFragment(MENU_CASH_BOOK, new CashbookFragment());
            displayBottomNavigationDynamic(CASH_BOOK);
        } else if (position == 5) {
            openFragment(MENU_ACCOUNT_LEDGER, new AccountLedgerFragment());
            displayBottomNavigationDynamic(ACCOUNT_LEGER);
        } else if (position == 6) {
            updateData_apiCall = new UpdateData_ApiCall(HomeActivity.this);

//            openFragment(MENU_UPDATE_DATA, new UpdateData());
            displayBottomNavigationDynamic(UPDATE_DATA);
        } else if (position == 7) {
            openFragment(MENU_CHANGE_PASSWORD, new ChangePasswordFragment());
            displayBottomNavigationDynamic(CHANGE_PASSWORD);
        } else if (position == 8) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        } else if (position == 9) {
            openFragment(MENU_DMT, new DMTFragment());
            displayBottomNavigationDynamic(DMT);
        } else if (position == 10) {
            Intent intent = new Intent(HomeActivity.this, CombineTransactionListActivity.class);
            startActivity(intent);
            finish();
        } else if (position == 12) {
            openFragment(MENU_PARENT_USER, new ParentUserFragment());
            displayBottomNavigationDynamic(RECHARGE);
        } else if (position == 13) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage(R.string.confirm_logout_message)
                    .setPositiveButton(R.string.confirm_signout_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().clear().commit();
                            Intent intent1 = new Intent(getContextInstance(), LoginActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent1);
                            finish();
                            sharedPreferences.edit().putBoolean(constants.LOGOUT, true).commit();
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .create()
                    .show();
        } else if (position == 11) {
            openFragment(MENU_PAYMENT_REQUEST, new PaymentRequestFragment());
            displayBottomNavigationDynamic(PAYMENT_REQUEST);
        } else if (position == 19) {
            Intent intent1 = new Intent(getContextInstance(), OnlinePaymentActivity.class);
            startActivity(intent1);
            finish();
//            openFragment(MENU_ONLINE_PAYMENT, new OnlinePaymentFragment());
//            displayBottomNavigationDynamic(ONLINE_PAYMENT);
        } else if (position == 14) {
            openFragment(MENU_GAS, new GasRecharge());
            displayBottomNavigationDynamic(GAS);
        } else if (position == 15) {
            openFragment(MENU_MOBILE_POSTPAID, new MobilePostPaidRecharge());
            displayBottomNavigationDynamic(MOBILE_POSTPAID);
        } else if (position == 16) {
            openFragment(MENU_WATER, new WaterRecharge());
            displayBottomNavigationDynamic(WATER);
        }
        // Display number of unread message
        setNotificationCounter();
        getNotificationIntentData();
        checkForUpdateData();

        lstNavigation.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                drawerPos = firstVisibleItem + "";
                System.out.println("Drawer POS: " + drawerPos);
            }
        });

    }

    public static void callMainActivity() {
        Intent intent = new Intent(context, Main2Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void checkServices() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.getService;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    Dlog.d("Service response: " + response);
                    myHandler.obtainMessage(SUCCESSSERVICE, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    private void checkForUpdateData() {
        Intent intent = getIntent();
        if (intent.getStringExtra(constants.KEY_REQUIRE_UPDATE) != null
                && !TextUtils.isEmpty(intent.getStringExtra(constants.KEY_REQUIRE_UPDATE))) {
            String requireUpdate = intent.getStringExtra(constants.KEY_REQUIRE_UPDATE);
            Dlog.d("Require update : " + requireUpdate);

            // Open update fragment
            if (TextUtils.equals(requireUpdate, "1")) {
                updateData_apiCall = new UpdateData_ApiCall(context);

//                FragmentTransaction fragment = getSupportFragmentManager().beginTransaction();
//                UpdateData updateData1 = new UpdateData();
//                Bundle bundle = new Bundle();
//                bundle.putString(constants.KEY_REQUIRE_UPDATE, requireUpdate);
//                updateData1.setArguments(bundle);
//                fragment.replace(R.id.container, updateData1);
//                fragment.commit();
                displayBottomNavigationDynamic(UPDATE_DATA);
            }
        }
    }

    private void initBottomNavigation() {
        llDefault = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Default);
        llLogout = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Default_Logout);
        llChangePassword = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Default_ChangePassword);
        llNotification = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Default_Notification);
        llUpdate = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Default_Update);
        // Unread notification counter
        txtDefaultTotalNotification = (TextView) findViewById(R.id.txt_Home_BottomNavigation_Default_TotalNotification);
        llLogout.setOnClickListener(this);
        llChangePassword.setOnClickListener(this);
        llNotification.setOnClickListener(this);
        llUpdate.setOnClickListener(this);
        // Notification Bottom navigation controls
        llNotificationBottom = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Notification);
        llNotificationRecharge = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Notification_Recharge);
        llNotificationRecentTransaction = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Notification_RecentTransaction);
        llNotificationTransactionSearch = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Notification_TransactionSearch);
        llNotificationRecharge.setOnClickListener(this);
        llNotificationRecentTransaction.setOnClickListener(this);
        llNotificationTransactionSearch.setOnClickListener(this);
        // Cashbook Bottom navigation controls
        llCashbookBottom = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Cashbook);
        llCashbookRecharge = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Cashbook_Recharge);
        llCashbookRecentTrasaction = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Cashbook_RecentTransaction);
        llCashbookNotification = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Cashbook_Notification);
        // Unread notification counter
        txtCashbookTotalNotification = (TextView) findViewById(R.id.txt_Home_BottomNavigation_Cashbook_TotalNotification);
        llCashbookRecharge.setOnClickListener(this);
        llCashbookRecentTrasaction.setOnClickListener(this);
        llCashbookNotification.setOnClickListener(this);
        // Change password Bottom navigation controls
        llChangePasswordBottom = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_ChangePassword);
        llChangePasswordRecharge = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_ChangePassword_Recharge);
        llChangePasswordRecentTransaction = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_ChangePassword_RecentTrasaction);
        llChangePasswordNotification = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_ChangePassword_Notification);
        // Unread notification counter
        txtChangePasswordTotalNotification = (TextView) findViewById(R.id.txt_Home_BottomNavigation_ChangePassword_TotalNotification);
        llChangePasswordRecharge.setOnClickListener(this);
        llChangePasswordRecentTransaction.setOnClickListener(this);
        llChangePasswordNotification.setOnClickListener(this);
        // Recharge Bottom navigation controls
        llRechargeBottom = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Recharge);
        llRechargeTransaction = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Recharge_TransactionSearch);
        llRechargeRecentTransaction = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Recharge_RecentTrasaction);
        llRechargeNotification = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_Recharge_Notification);
        // Unread notification counter
        txtRechargeTotalNotification = (TextView) findViewById(R.id.txt_Home_BottomNavigation_Recharge_TotalNotification);
        llRechargeTransaction.setOnClickListener(this);
        llRechargeRecentTransaction.setOnClickListener(this);
        llRechargeNotification.setOnClickListener(this);
        // Transaction search Bottom navigation controls
        llTransactionSearchBottom = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_TransactionSearch);
        llTransactionSearchRecharge = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_TransactionSearch_Recharge);
        llTransactionSearchRecentTransaction = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_TransactionSearch_RecentTransaction);
        llTransactionSearchNotification = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_TransactionSearch_Notification);
        // Unread notification counter
        txtTransactionSearchTotalNotification = (TextView) findViewById(R.id.txt_Home_BottomNavigation_TransactionSearch_TotalNotification);
        llTransactionSearchRecharge.setOnClickListener(this);
        llTransactionSearchRecentTransaction.setOnClickListener(this);
        llTransactionSearchNotification.setOnClickListener(this);
        // Recent transaction Bottom navigation controls
        llRecentTransactionBottom = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_RecentTransaction);
        llRecentTransactionRecharge = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_RecentTransaction_Recharge);
        llRecentTransactionTransactionSearch = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_RecentTransaction_TransactionSearch);
        llRecentTransactionNotification = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_RecentTransaction_Notification);
        // Unread notification counter
        txtRecentTransactionTotalNotification = (TextView) findViewById(R.id.txt_Home_BottomNavigation_RecentTransaction_TotalNotification);
        llRecentTransactionRecharge.setOnClickListener(this);
        llRecentTransactionTransactionSearch.setOnClickListener(this);
        llRecentTransactionNotification.setOnClickListener(this);
        // DMT
        llBottomDMT = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_DMT);
        llTransactionSearchDMT = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_DMT_TransactionSearch);
        llTransactionSearchRecentTransactionDMT = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_DMT_RecentTrasaction);
        llSenderSearchDMT = (LinearLayout) findViewById(R.id.ll_Home_BottomNavigation_DMT_SenderSearch);
        llTransactionSearchDMT.setOnClickListener(this);
        llTransactionSearchRecentTransactionDMT.setOnClickListener(this);
        llSenderSearchDMT.setOnClickListener(this);
        txtAlert = (TextView) findViewById(R.id.txtAlertMain);
    }

    public void makeAlertCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.getAlert;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    Dlog.d("Alert response: " + response);
                    myHandler.obtainMessage(SUCCESSALERT, response).sendToTarget();
                } catch (Exception ex) {
                    Log.e(TAG, "  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void parseServiceResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                if (jsonObject.getString("msg").equals("List generated")) {
                    String encrypted_response = jsonObject.getString("data");
                    String decrypted_data = Constants.decryptAPI(HomeActivity.this, encrypted_response);
                    Dlog.d("Decrypted Service: " + decrypted_data);
                    JSONObject array = new JSONObject(decrypted_data);
                    JSONArray array1 = array.getJSONArray("services");
                    if (array1.length() > 0) {
                        serviceModelArrayList = new ArrayList<ServicesModel>();
                        serviceModelArrayListFinal = new ArrayList<ServicesModel>();
                        for (int i = 0; i < array1.length(); i++) {
                            JSONObject object = array1.getJSONObject(i);
                            servicesModel = new ServicesModel();
                            servicesModel.setId(object.getString("id"));
                            serviceModelArrayList.add(servicesModel);
                        }

                        ServicesModel model;
                        if (serviceModelArrayList.size() > 0) {
                            for (int i = 0; i < serviceModelArrayList.size(); i++) {
                                if (serviceModelArrayList.get(i).getId().equals(Constants.mobile_prepaid_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.mobile_prepaid_id);
                                    model.setName(Constants.KEY_MOB_PREPAID_TEXT);
                                    model.setIcon(R.drawable.ic_mobile);
                                    serviceModelArrayListFinal.add(model);
                                } else if (serviceModelArrayList.get(i).getId().equals(Constants.dth_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.dth_id);
                                    model.setName(Constants.KEY_DTH_TEXT);
                                    model.setIcon(R.drawable.dth);
                                    serviceModelArrayListFinal.add(model);
                                } else if (serviceModelArrayList.get(i).getId().equals(Constants.electricity_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.electricity_id);
                                    model.setName(Constants.KEY_ELECTRICITY_TEXT);
                                    model.setIcon(R.drawable.ic_electricity2);
                                    serviceModelArrayListFinal.add(model);
                                } else if (serviceModelArrayList.get(i).getId().equals(Constants.gas_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.gas_id);
                                    model.setName(Constants.KEY_GAS_TEXT);
                                    model.setIcon(R.drawable.gas);
                                    serviceModelArrayListFinal.add(model);
                                } else if (serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.dmt_id);
                                    model.setName(Constants.KEY_DMT_TEXT);
                                    model.setIcon(R.drawable.ic_dmt);
                                    serviceModelArrayListFinal.add(model);
                                } else if (serviceModelArrayList.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.mobile_postpaid_id);
                                    model.setName(Constants.KEY_MOB_POSTPAID_TEXT);
                                    model.setIcon(R.drawable.mobile);
                                    serviceModelArrayListFinal.add(model);
                                } else if (serviceModelArrayList.get(i).getId().equals(Constants.water_id)) {
                                    model = new ServicesModel();
                                    model.setId(Constants.water_id);
                                    model.setName(Constants.KEY_WATER_TEXT);
                                    model.setIcon(R.drawable.tap);
                                    serviceModelArrayListFinal.add(model);
                                }
                            }
                            model = new ServicesModel();
                            model.setId(Constants.more_id);
                            model.setName(Constants.KEY_MORE_TEXT);
                            model.setIcon(R.drawable.ic_more);
                            serviceModelArrayListFinal.add(model);

                            servicesModel = new ServicesModel();
                            servicesModel.setId(Constants.more_id);
                            servicesModel.setName(Constants.KEY_MORE_TEXT);
                            servicesModel.setIcon(R.drawable.ic_more);
                            serviceModelArrayList.add(servicesModel);

                            for (int i = 0; i < serviceModelArrayListFinal.size(); i++) {
                                if (serviceModelArrayListFinal.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                                    moveService(1, i);
                                }
                                if (serviceModelArrayListFinal.get(i).getId().equals(Constants.dmt_id)) {
                                    moveService(2, i);
                                }
                            }
                        }

                        Constants.dmt_flag = new String[serviceModelArrayListFinal.size()];
                        for (int i = 0; i < serviceModelArrayListFinal.size(); i++) {
                            if (serviceModelArrayListFinal.get(i).getId().equals(Constants.dmt_id)) {
                                Constants.dmt_flag[i] = "true";
                                Constants.dmt_title = serviceModelArrayListFinal.get(i).getName();
                            } else {
                                Constants.dmt_flag[i] = "false";
                            }
                        }

                        Constants.elctricity_flag = new String[serviceModelArrayListFinal.size()];
                        for (int i = 0; i < serviceModelArrayListFinal.size(); i++) {
                            if (serviceModelArrayListFinal.get(i).getId().equals(Constants.electricity_id)) {
                                Constants.elctricity_flag[i] = "true";
                                Constants.electricity_title = serviceModelArrayListFinal.get(i).getName();
                            } else {
                                Constants.elctricity_flag[i] = "false";
                            }
                        }

                        Constants.gas_flag = new String[serviceModelArrayListFinal.size()];
                        for (int i = 0; i < serviceModelArrayListFinal.size(); i++) {
                            if (serviceModelArrayListFinal.get(i).getId().equals(Constants.gas_id)) {
                                Constants.gas_flag[i] = "true";
                                Constants.gas_title = serviceModelArrayListFinal.get(i).getName();
                            } else {
                                Constants.gas_flag[i] = "false";
                            }
                        }

                        Constants.mobile_postpaid_flag = new String[serviceModelArrayListFinal.size()];
                        for (int i = 0; i < serviceModelArrayListFinal.size(); i++) {
                            if (serviceModelArrayListFinal.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                                Constants.mobile_postpaid_flag[i] = "true";
                                Constants.mobile_postpaid_title = serviceModelArrayListFinal.get(i).getName();
                            } else {
                                Constants.mobile_postpaid_flag[i] = "false";
                            }
                        }

                        Constants.water_flag = new String[serviceModelArrayListFinal.size()];
                        for (int i = 0; i < serviceModelArrayListFinal.size(); i++) {
                            if (serviceModelArrayListFinal.get(i).getId().equals(Constants.water_id)) {
                                Constants.water_flag[i] = "true";
                                Constants.water_title = serviceModelArrayListFinal.get(i).getName();
                            } else {
                                Constants.water_flag[i] = "false";
                            }
                        }

                        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(serviceModelArrayListFinal);
                        prefsEditor.putString(Constants.KEY_SERVICE_DATA, json);
                        prefsEditor.commit();

                        Constants.serviceModelArrayList = serviceModelArrayListFinal;
                    } else {
                    }
                    setCustomNavigation();
                } else {
                    Dlog.d("Balance response not found. Status = " + jsonObject.getString("status"));
                }
            } else {
                if (jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                }
            }
        } catch (JSONException e) {
            Dlog.d("Error : " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void moveService(int newPos, int oldPos) {
        serviceModelArrayListFinal.add(newPos, serviceModelArrayListFinal.remove(oldPos));
    }

    public void parseAlertResponse(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {

                String encrypted_response = jsonObject.getString("data");
                String decrypted_data = Constants.decryptAPI(HomeActivity.this, encrypted_response);
                Dlog.d("Decrypted Alert: " + decrypted_data);
                JSONArray array = new JSONArray(decrypted_data);
                if (array.length() > 0) {
                    alertModelsList = new ArrayList<String>();
                    for (int i = 0; i < array.length(); i++) {
                        if (i == array.length() - 1) {
                            JSONObject object = array.getJSONObject(i);
                            alertModels = new AlertModels();
                            alertModelsList.add(object.getString("alert_message"));
                        }
                    }

                    if (alertModelsList.size() > 0) {
                        StringBuilder builder = new StringBuilder();
                        for (String details : alertModelsList) {
                            builder.append(details);
                        }
                        txtAlert.setText(builder);
                        txtAlert.setSelected(true);
                        txtAlert.setVisibility(View.VISIBLE);
                    } else {
                        txtAlert.setVisibility(View.GONE);
                    }
                } else {
                    txtAlert.setVisibility(View.GONE);
                }
            } else {
                Dlog.d("Balance response not found. Status = " + jsonObject.getString("status"));
            }
        } catch (JSONException e) {
            Dlog.d("Error : " + e.getMessage());
            e.printStackTrace();
        }

    }

    /* [START] -  Change bottom navigation dynamic */
    private void displayNotificationBottomBar() {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.VISIBLE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llRechargeBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
    }

    public void displayDMTBottomBarDynamic() {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llRechargeBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.VISIBLE);

    }

    private void displayDefaultBottomBar() {
        llDefault.setVisibility(View.VISIBLE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llRechargeBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
    }

    private void displayCashbookBottomBar() {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.VISIBLE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llRechargeBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
    }

    private void displayChangePasswordBottomBar() {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.VISIBLE);
        llRechargeBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
    }

    private void displayUpdateDataBottomBar() {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llRechargeBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
    }

    public void displayRechargeBottomBar() {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llRechargeBottom.setVisibility(View.VISIBLE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
    }

    public void displayRechargeBottomBar(boolean displayBottom) {
        llDefault.setVisibility(View.GONE);
        llNotificationBottom.setVisibility(View.GONE);
        llCashbookBottom.setVisibility(View.GONE);
        llChangePasswordBottom.setVisibility(View.GONE);
        llTransactionSearchBottom.setVisibility(View.GONE);
        llRecentTransactionBottom.setVisibility(View.GONE);
        llBottomDMT.setVisibility(View.GONE);
        if (displayBottom) {
            llRechargeBottom.setVisibility(View.VISIBLE);
        } else {
            llRechargeBottom.setVisibility(View.GONE);
        }
    }

    // [START] - Remove bottom navigation and drawer in update screen. */
    private void
    displayBottomNavigationDynamic(int fragmentNumber) {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (fragmentNumber == NOTIFICATION) {
            displayNotificationBottomBar();
        } else if (fragmentNumber == CASH_BOOK) {
            displayCashbookBottomBar();
        } else if (fragmentNumber == CHANGE_PASSWORD) {
            displayChangePasswordBottomBar();
        } else if (fragmentNumber == UPDATE_DATA) {
            displayUpdateDataBottomBar();
            //drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else if (fragmentNumber == RECHARGE) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == DMT) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == DMT_TRANSACTION_LIST) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == PAYMENT_REQUEST) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == GAS) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == MOBILE_POSTPAID) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == WATER) {
            displayRechargeBottomBar();
        } else if (fragmentNumber == ONLINE_PAYMENT) {
            displayRechargeBottomBar();
        } else {
            displayDefaultBottomBar();
        }
    }
    // [END]

    // set custom notification
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setCustomNavigation() {
        try {

            txtVersion = (TextView) findViewById(R.id.txtNavVersion);

            thread = new Thread() {

                @Override
                public void run() {
                    try {
                        while (!thread.isInterrupted()) {
                            Thread.sleep(1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // update TextView here!
                                        String updateDate = prefs.retriveString(constants.PREF_UPDATE_DATE, "0");
                                        String updateTime = prefs.retriveString(constants.PREF_UPDATE_TIME, "0");
                                        if (TextUtils.equals(updateDate, "0")) {
                                            txtVersion.setText("v" + BuildConfig.VERSION_NAME);
                                        } else {
                                            String updateTime1 = Constants.parseDateToddMMyyyy("hh:mm:ss", "hh:mm a", prefs.retriveString(constants.PREF_UPDATE_TIME, "0"));
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                            String curTime = dateFormat.format(Calendar.getInstance().getTime());
                                            SimpleDateFormat df = new SimpleDateFormat("hh:mm a", Locale.US);
                                            Date dateUpdate = df.parse(updateTime1);
                                            Date dateCurrent = df.parse(curTime);

                                            /*long difference = dateCurrent.getTime() - dateUpdate.getTime();
                                            int days = (int) (difference / (1000 * 60 * 60 * 24));
                                            int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                                            int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                                            hours = (hours < 0 ? -hours : hours);*/
                                            long diff = dateCurrent.getTime() - dateUpdate.getTime();
                                            long seconds = diff / 1000;
                                            long minutes = seconds / 60;
                                            long hours = minutes / 60;
                                            long days = hours / 24;

                                            int hr = (int) (minutes / 60); //since both are ints, you get an int
                                            int min = (int) (minutes % 60);
                                            System.out.printf("%d:%02d", hr, min);

                                            if(hr > 0) {
                                                txtVersion.setText("v" + BuildConfig.VERSION_NAME + "       Last Update:  " + hr + "hr:" + min + "min ago");
                                            } else {
                                                txtVersion.setText("v" + BuildConfig.VERSION_NAME + "       Last Update:  " + minutes + "min ago");
                                            }
                                        }

                                    } catch (Exception e) {
                                        System.out.println(e.toString());
                                    }

                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };

            thread.start();

            final ArrayList<NavigationModels> stringArrayList = new ArrayList<NavigationModels>();
            // All menu items name
            stringArrayList.add(new NavigationModels(MENU_HOME, R.drawable.ic_home, 0));
            stringArrayList.add(new NavigationModels(MENU_RECHARGE, R.drawable.ic_mobile, 0));
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                    Constants.mobile_postpaid_flag[i] = "true";
                } else {
                    Constants.mobile_postpaid_flag[i] = "false";
                }
            }
            boolean isThere2 = Arrays.asList(Constants.mobile_postpaid_flag).contains("true");
            if (isThere2) {
                stringArrayList.add(new NavigationModels(MENU_MOBILE_POSTPAID, R.drawable.mobile, 0));
            } else {
            }
            stringArrayList.add(new NavigationModels(MENU_DTH, R.drawable.dth, 0));
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                    Constants.dmt_flag[i] = "true";
                } else {
                    Constants.dmt_flag[i] = "false";
                }
            }
            boolean isThere = Arrays.asList(Constants.dmt_flag).contains("true");
            if (isThere) {
                stringArrayList.add(new NavigationModels(MENU_DMT, R.drawable.ic_dmt, 0));
            } else {
            }
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.electricity_id)) {
                    Constants.elctricity_flag[i] = "true";
                } else {
                    Constants.elctricity_flag[i] = "false";
                }
            }
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.gas_id)) {
                    Constants.gas_flag[i] = "true";
                } else {
                    Constants.gas_flag[i] = "false";
                }
            }

            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.water_id)) {
                    Constants.water_flag[i] = "true";
                } else {
                    Constants.water_flag[i] = "false";
                }
            }
            boolean isThere4 = Arrays.asList(Constants.elctricity_flag).contains("true");
            if (isThere4) {
                stringArrayList.add(new NavigationModels(MENU_ELECTRICITY, R.drawable.ic_electricity2, 0));
            } else {
            }

            boolean isThere1 = Arrays.asList(Constants.gas_flag).contains("true");
            if (isThere1) {
                stringArrayList.add(new NavigationModels(MENU_GAS, R.drawable.gas, 0));
            } else {
            }

            boolean isThere3 = Arrays.asList(Constants.water_flag).contains("true");
            if (isThere3) {
                stringArrayList.add(new NavigationModels(MENU_WATER, R.drawable.tap, 0));
            } else {
            }
            stringArrayList.add(new NavigationModels(MENU_RECENT_TRANSACTION, R.drawable.ic_recent, 0));
            stringArrayList.add(new NavigationModels(MENU_COMPLAIN_REPORT, R.drawable.ic_complain, 0));
            stringArrayList.add(new NavigationModels(MENU_CASH_BOOK, R.drawable.ic_payment_report, 0));
            stringArrayList.add(new NavigationModels(MENU_ACCOUNT_LEDGER, R.drawable.ic_chrome_acledger, 0));
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                    Constants.dmt_flag[i] = "true";
                } else {
                    Constants.dmt_flag[i] = "false";
                }
            }
            stringArrayList.add(new NavigationModels(MENU_DMT_TRANSACTION_LIST, R.drawable.ic_transaction_report, 0));
            stringArrayList.add(new NavigationModels(MENU_PAYMENT_REQUEST, R.drawable.ic_payment_on_black_24dp, 0));
            /*if(databaseHelper.getPaymentGateway().size()>0) {
                stringArrayList.add(new NavigationModels(MENU_ONLINE_PAYMENT, R.drawable.ic_payment_on_black_24dp, 0));
            }*/
            stringArrayList.add(new NavigationModels(MENU_ONLINE_PAYMENT, R.drawable.ic_payment_on_black_24dp, 0));
            stringArrayList.add(new NavigationModels(MENU_PARENT_USER, R.drawable.ic_parent_user, 0));
            stringArrayList.add(new NavigationModels(MENU_CHANGE_PASSWORD, R.drawable.ic_menu_change_password, 0));
            stringArrayList.add(new NavigationModels(MENU_NOTIFICATION, R.drawable.ic_notifications_black_24dp, 0));
            stringArrayList.add(new NavigationModels(MENU_SHARE, R.drawable.ic_share_black_24dp, 0));
            stringArrayList.add(new NavigationModels(MENU_ANOTHER_USER, R.drawable.ic_person, 0));
            stringArrayList.add(new NavigationModels(MENU_UPDATE_DATA, R.drawable.ic_update_button, 0));
            stringArrayList.add(new NavigationModels(MENU_LOGOUT, R.drawable.ic_cancel, 0));

            final NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(getContextInstance(), stringArrayList);
            lstNavigation.setAdapter(adapter);
            lstNavigation.setSelection(Integer.parseInt(drawerPos));

            lstNavigation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedMenuName = adapter.getData(position);
                    // Handle navigation view item clicks here.
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);

                    if (sharedPreferences.contains(Constants.DMT_MOBILE)) {
                        sharedPreferences.edit().putString(Constants.DMT_MOBILE, "").commit();
                    }
                    if (TextUtils.equals(selectedMenuName, MENU_ANOTHER_USER) || TextUtils.equals(selectedMenuName, MENU_LOGOUT)) {
                    } else {
                        if (!databaseHelper.checkEmpty()) {
                            selectedMenuName = MENU_UPDATE_DATA;
                        }
                    }
                    if (TextUtils.equals(selectedMenuName, MENU_HOME)) {
                        Intent intent = new Intent(getContextInstance(), Main2Activity.class);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_RECHARGE)) {
                        openFragment(MENU_RECHARGE, new MobileRecharge());
                        sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
                        sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
                        displayBottomNavigationDynamic(RECHARGE);
                    } else if (TextUtils.equals(selectedMenuName, MENU_DTH)) {
                        openFragment(MENU_DTH, new DTHRecharge());
                        sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
                        sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
                        displayBottomNavigationDynamic(RECHARGE);
                    } else if (TextUtils.equals(selectedMenuName, MENU_ELECTRICITY)) {
                        openFragment(MENU_ELECTRICITY, new ElectricityRecharge());
                        sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
                        sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
                        displayBottomNavigationDynamic(RECHARGE);
                    } else if (TextUtils.equals(selectedMenuName, MENU_RECENT_TRANSACTION)) {
                        openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
                        displayBottomNavigationDynamic(RECENT_TRANSACTION);
                    } else if (TextUtils.equals(selectedMenuName, MENU_COMPLAIN_REPORT)) {
                        openFragment(MENU_COMPLAIN_REPORT, new ComplainReportFragment());
                        displayBottomNavigationDynamic(COMPLAIN_REPORT);
                    } else if (TextUtils.equals(selectedMenuName, MENU_CASH_BOOK)) {
                        openFragment(MENU_CASH_BOOK, new CashbookFragment());
                        displayBottomNavigationDynamic(CASH_BOOK);
                    } else if (TextUtils.equals(selectedMenuName, MENU_ACCOUNT_LEDGER)) {
                        openFragment(MENU_ACCOUNT_LEDGER, new AccountLedgerFragment());
                        displayBottomNavigationDynamic(ACCOUNT_LEGER);
                    } else if (TextUtils.equals(selectedMenuName, MENU_UPDATE_DATA)) {
                        updateData_apiCall = new UpdateData_ApiCall(HomeActivity.this);

//                        openFragment(MENU_UPDATE_DATA, new UpdateData());
                        displayBottomNavigationDynamic(UPDATE_DATA);
                    } else if (TextUtils.equals(selectedMenuName, MENU_DMT)) {
                        openFragment(MENU_DMT, new DMTFragment());
                        displayBottomNavigationDynamic(DMT);
                    } else if (TextUtils.equals(selectedMenuName, MENU_DMT_TRANSACTION_LIST)) {
                        Intent intent = new Intent(HomeActivity.this, CombineTransactionListActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (TextUtils.equals(selectedMenuName, MENU_PAYMENT_REQUEST)) {
                        openFragment(MENU_PAYMENT_REQUEST, new PaymentRequestFragment());
                        displayBottomNavigationDynamic(PAYMENT_REQUEST);
                    } else if (TextUtils.equals(selectedMenuName, MENU_ONLINE_PAYMENT)) {
//                        openFragment(MENU_ONLINE_PAYMENT, new OnlinePaymentFragment());
//                        displayBottomNavigationDynamic(ONLINE_PAYMENT);
                        Intent intent1 = new Intent(getContextInstance(), OnlinePaymentActivity.class);
                        startActivity(intent1);
                        finish();
                    } else if (TextUtils.equals(selectedMenuName, MENU_GAS)) {
                        openFragment(MENU_GAS, new GasRecharge());
                        displayBottomNavigationDynamic(GAS);
                    } else if (TextUtils.equals(selectedMenuName, MENU_MOBILE_POSTPAID)) {
                        openFragment(MENU_MOBILE_POSTPAID, new MobilePostPaidRecharge());
                        displayBottomNavigationDynamic(MOBILE_POSTPAID);
                    } else if (TextUtils.equals(selectedMenuName, MENU_WATER)) {
                        openFragment(MENU_WATER, new WaterRecharge());
                        displayBottomNavigationDynamic(WATER);
                    } else if (TextUtils.equals(selectedMenuName, MENU_CHANGE_PASSWORD)) {
                        openFragment(MENU_CHANGE_PASSWORD, new ChangePasswordFragment());
                        displayBottomNavigationDynamic(CHANGE_PASSWORD);
                    } else if (TextUtils.equals(selectedMenuName, MENU_NOTIFICATION)) {
                        openFragment(MENU_NOTIFICATION, new NotificationFragment());
                        displayBottomNavigationDynamic(NOTIFICATION);
                    } else if (TextUtils.equals(selectedMenuName, MENU_PARENT_USER)) {
                        ParentUserFragment parentUserFragment = ParentUserFragment.newInstance("1", "2");
                        openFragment(MENU_PARENT_USER, parentUserFragment);
                        displayBottomNavigationDynamic(RECHARGE);
                    } else if (TextUtils.equals(selectedMenuName, MENU_SHARE)) {
                        showSharePopup();
                    } else if (TextUtils.equals(selectedMenuName, MENU_ANOTHER_USER)) {
                        new AlertDialog.Builder(HomeActivity.this)
                                .setMessage(R.string.confirm_signin_with_another_user_message)
                                .setPositiveButton(R.string.str_login_another_user, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent1 = new Intent(getContextInstance(), RegistrationActivity.class);
                                        intent1.putExtra("from", "dashboard");
                                        startActivity(intent1);
                                    }
                                })
                                .setNegativeButton("cancel", null)
                                .create()
                                .show();
                    } else if (TextUtils.equals(selectedMenuName, MENU_LOGOUT)) {
                        new AlertDialog.Builder(HomeActivity.this)
                                .setMessage(R.string.confirm_logout_message)
                                .setPositiveButton(R.string.confirm_signout_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sharedPreferences.edit().clear().commit();
                                        Intent intent1 = new Intent(getContextInstance(), LoginActivity.class);
                                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent1);
                                        finish();
                                        sharedPreferences.edit().putBoolean(constants.LOGOUT, true).commit();
                                    }
                                })
                                .setNegativeButton("cancel", null)
                                .create()
                                .show();
                    }
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
            });
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void showSharePopup() {

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.changeAppName(HomeActivity.this));
            String shareMessage = "\nLet me recommend you " + Constants.changeAppName(HomeActivity.this) + " application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + Constants.APP_PACKAGE_NAME + "\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }

    }

    private void initControls() {
        /* [START] - Initialise class objects */
        lstNavigation = (ListView) findViewById(R.id.lst_NavigationView);
        constants = new Constants();
        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(getContextInstance());
        prefs = new MyPrefs(getContextInstance(), constants.PREF_NAME);
        // [END]
        /* [START] - get user data from database and store into array list */
        userArrayList = databaseHelper.getUserDetail();
        // [END]
        registerNotificationReceiver();
    }

    private void registerNotificationReceiver() {
        /* [START] - Create custom notification for receiver notification data */
        try {
            if (notificationReceiver == null) {
                // Add notification filter
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_REFRESH_HOMEACTIVITY);
                // Create notification object
                notificationReceiver = new CheckNotification();
                // Register receiver
                HomeActivity.this.registerReceiver(notificationReceiver, intentFilter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Dlog.d("Error : " + ex.getMessage());
        }
        // [END]
    }

    private void unregisterNotificationReceiver() {
        try {
            if (notificationReceiver != null) {
                HomeActivity.this.unregisterReceiver(notificationReceiver);
                notificationReceiver = null;
            }
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void getBundleData() {
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        LogMessage.d("position : " + String.valueOf(position));
    }

    private void getNotificationIntentData() {
        /* [START] - Open notification fragment if user press on notification */
        Dlog.d("getNotificationIntentData() call");
        Bundle bundle = getIntent().getExtras();
        String screenNo = "-1";
        String notificationId = "-1";
        if (bundle != null) {
            if (bundle.getString(Constants.KEY_SCREEN_NO) != null) {
                screenNo = bundle.getString(Constants.KEY_SCREEN_NO, "-1");
            }
            if (bundle.getString(Constants.KEY_NOTIFICATION_ID) != null) {
                notificationId = bundle.getString(Constants.KEY_NOTIFICATION_ID, "");
            }
        }
        if (!TextUtils.equals(screenNo, "-1") && !TextUtils.equals(notificationId, "-1")) {
            openNotificationFragment(MENU_NOTIFICATION, notificationId);
        }
        // [END]
    }

    private void openFragment(String title, Fragment fragment) {
        getSupportActionBar().setTitle(title);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment).addToBackStack(fragment.toString() + "").commit();
    }

    public void openTransactionSearchFragment() {
        Intent intent = new Intent(HomeActivity.this, CombineTransactionListActivity.class);
        intent.putExtra("current_pos", "0");
        startActivity(intent);
    }

    private void openNotificationFragment(String title, String notificationId) {
        getSupportActionBar().setTitle(title);

        NotificationFragment notificationFragment = new NotificationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_NOTIFICATION_ID, notificationId);
        notificationFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, notificationFragment).commit();

        displayBottomNavigationDynamic(NOTIFICATION);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setNotificationCounter() {
        int totalNotification = 0;
        try {
            Constants.TOTAL_UNREAD_NOTIFICATION = new NotificationTable(getContextInstance()).getNumberOfNotificationRecord() + "";
            totalNotification = Integer.parseInt(Constants.TOTAL_UNREAD_NOTIFICATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            Dlog.d("Notification : " + "Error : " + ex.toString());
            totalNotification = 0;
        }
        if (totalNotification > 0) {
            txtDefaultTotalNotification.setVisibility(View.VISIBLE);
            txtCashbookTotalNotification.setVisibility(View.VISIBLE);
            txtChangePasswordTotalNotification.setVisibility(View.VISIBLE);
            txtRechargeTotalNotification.setVisibility(View.VISIBLE);
            txtTransactionSearchTotalNotification.setVisibility(View.VISIBLE);
            txtRecentTransactionTotalNotification.setVisibility(View.VISIBLE);
            txtDefaultTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
            txtCashbookTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
            txtChangePasswordTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
            txtRechargeTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
            txtTransactionSearchTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
            txtRecentTransactionTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
        } else {
            txtDefaultTotalNotification.setVisibility(View.GONE);
            txtCashbookTotalNotification.setVisibility(View.GONE);
            txtChangePasswordTotalNotification.setVisibility(View.GONE);
            txtRechargeTotalNotification.setVisibility(View.GONE);
            txtTransactionSearchTotalNotification.setVisibility(View.GONE);
            txtRecentTransactionTotalNotification.setVisibility(View.GONE);
        }
        // reset custom navigation menu
        try {
            setCustomNavigation();
        } catch (Exception ex) {
            Dlog.d("Custom navigation menu error : " + ex.toString());
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
//            super.onBackPressed();
            unregisterNotificationReceiver();
            HomeActivity.this.finish();
            Intent intent = new Intent(getContextInstance(), Main2Activity.class);
            startActivity(intent);
        }
        /*else {
            int fragments = getSupportFragmentManager().getBackStackEntryCount();
            if (fragments == 1) {
                finish();
            } else if (getFragmentManager().getBackStackEntryCount() > 1) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }*/

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return toggle.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = "";


        if (id == R.id.nav_Home) {
            Intent intent = new Intent(getContextInstance(), Main2Activity.class);
            startActivity(intent);
        } else if (id == R.id.nav_recharge) {
            title = MENU_RECHARGE;
            getSupportActionBar().setTitle(title);
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            MobileRecharge rechargeFragment = new MobileRecharge();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString() + "").commit();

            displayBottomNavigationDynamic(RECHARGE);
        } else if (id == R.id.nav_dth) {
            title = MENU_DTH;
            getSupportActionBar().setTitle(title);
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            DTHRecharge rechargeFragment = new DTHRecharge();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString() + "").commit();

            displayBottomNavigationDynamic(RECHARGE);
        } else if (id == R.id.nav_electricity) {
            title = MENU_ELECTRICITY;
            getSupportActionBar().setTitle(title);
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            ElectricityRecharge rechargeFragment = new ElectricityRecharge();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString() + "").commit();

            displayBottomNavigationDynamic(RECHARGE);
        } else if (id == R.id.nav_rec_trans) {
            title = MENU_RECENT_TRANSACTION;
            getSupportActionBar().setTitle(title);
            RecentTransactionFragment recentTransactionFragment = new RecentTransactionFragment();
            fragmentTransaction(recentTransactionFragment);
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (id == R.id.nav_trans_search) {
            title = MENU_TRANSACTION_SEARCH;
            getSupportActionBar().setTitle(title);
            openTransactionSearchFragment();
            displayBottomNavigationDynamic(TRANSACTION_SEARCH);
        } else if (id == R.id.nav_complain_report) {
            title = MENU_COMPLAIN_REPORT;
            getSupportActionBar().setTitle(title);
            ComplainReportFragment complainReportFragment = new ComplainReportFragment();
            fragmentTransaction(complainReportFragment);
            displayBottomNavigationDynamic(COMPLAIN_REPORT);
        } else if (id == R.id.nav_Cashbook) {
            title = MENU_CASH_BOOK;
            getSupportActionBar().setTitle(title);
            CashbookFragment cashbookFragment = new CashbookFragment();
            fragmentTransaction(cashbookFragment);
            displayBottomNavigationDynamic(CASH_BOOK);
        } else if (id == R.id.nav_account_ledger) {
            title = MENU_ACCOUNT_LEDGER;
            getSupportActionBar().setTitle(title);
            AccountLedgerFragment accountLedgerFragment = new AccountLedgerFragment();
            fragmentTransaction(accountLedgerFragment);
            displayBottomNavigationDynamic(ACCOUNT_LEGER);
        } else if (id == R.id.nav_update_data) {
            updateData_apiCall = new UpdateData_ApiCall(context);

//            title = MENU_UPDATE_DATA;
//            getSupportActionBar().setTitle(title);
//            UpdateData updateDataFragment = new UpdateData();
//            fragmentTransaction(updateDataFragment);
            displayBottomNavigationDynamic(UPDATE_DATA);
        } else if (id == R.id.nav_ChangePassword) {
            title = MENU_CHANGE_PASSWORD;
            getSupportActionBar().setTitle(title);
            ChangePasswordFragment changePasswordActivity = new ChangePasswordFragment();
            fragmentTransaction(changePasswordActivity);
            displayBottomNavigationDynamic(CHANGE_PASSWORD);
        } else if (id == R.id.nav_dmt) {
            title = MENU_DMT;
            getSupportActionBar().setTitle(title);
            DMTFragment changePasswordActivity = new DMTFragment();
            fragmentTransaction(changePasswordActivity);
            displayBottomNavigationDynamic(DMT);
        } else if (id == R.id.nav_dmt_transaction_list) {
            title = MENU_DMT_TRANSACTION_LIST;
            getSupportActionBar().setTitle(title);
            Intent intent = new Intent(HomeActivity.this, CombineTransactionListActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_payment_req) {
            title = MENU_PAYMENT_REQUEST;
            getSupportActionBar().setTitle(title);
            PaymentRequestFragment notificationFragment = new PaymentRequestFragment();
            fragmentTransaction(notificationFragment);
            displayBottomNavigationDynamic(PAYMENT_REQUEST);
        } else if (id == R.id.nav_online_payment) {
            title = MENU_ONLINE_PAYMENT;
//            getSupportActionBar().setTitle(title);
//            OnlinePaymentFragment notificationFragment = new OnlinePaymentFragment();
//            fragmentTransaction(notificationFragment);
//            displayBottomNavigationDynamic(ONLINE_PAYMENT);
            Intent intent1 = new Intent(getContextInstance(), OnlinePaymentActivity.class);
            startActivity(intent1);
            finish();
        } else if (id == R.id.nav_gas) {
            title = MENU_GAS;
            getSupportActionBar().setTitle(title);
            GasRecharge notificationFragment = new GasRecharge();
            fragmentTransaction(notificationFragment);
            displayBottomNavigationDynamic(GAS);
        } else if (id == R.id.nav_mobile_postpaid) {
            title = MENU_MOBILE_POSTPAID;
            getSupportActionBar().setTitle(title);
            MobilePostPaidRecharge notificationFragment = new MobilePostPaidRecharge();
            fragmentTransaction(notificationFragment);
            displayBottomNavigationDynamic(MOBILE_POSTPAID);
        } else if (id == R.id.nav_water) {
            title = MENU_WATER;
            getSupportActionBar().setTitle(title);
            WaterRecharge notificationFragment = new WaterRecharge();
            fragmentTransaction(notificationFragment);
            displayBottomNavigationDynamic(WATER);
        } else if (id == R.id.nav_Notification) {
            title = MENU_NOTIFICATION;
            getSupportActionBar().setTitle(title);
            NotificationFragment notificationFragment = new NotificationFragment();
            fragmentTransaction(notificationFragment);
            displayBottomNavigationDynamic(NOTIFICATION);
        } else if (id == R.id.nav_share) {
            showSharePopup();
        } else if (id == R.id.nav_another_number) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage(R.string.confirm_signin_with_another_user_message)
                    .setPositiveButton(R.string.str_login_another_user, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent1 = new Intent(getContextInstance(), RegistrationActivity.class);
                            intent1.putExtra("from", "dashboard");
                            startActivity(intent1);
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .create()
                    .show();
        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage(R.string.confirm_logout_message)
                    .setPositiveButton(R.string.confirm_signout_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().clear().commit();
                            Intent intent1 = new Intent(getContextInstance(), LoginActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent1);
                            finish();
                            sharedPreferences.edit().putBoolean(constants.LOGOUT, true).commit();
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .create()
                    .show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void fragmentTransaction(Fragment fragmentname) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragmentname).addToBackStack(fragmentname.toString() + "").commit();
    }

    //multi wallet 4-3-2019
    public void makeWalletCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.walletType;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_WALLET_LIST, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    public void parseSuccessWalletResponse(String response) {

        Dlog.d("Wallet Response : " + response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(HomeActivity.this, encrypted_response);
                Dlog.d("Wallet : " + "decrypted_response : " + decrypted_response);
                JSONArray array = new JSONArray(decrypted_response);
                walletsModelList = new ArrayList<WalletsModel>();
                walletsList = new ArrayList<String>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    walletsModel = new WalletsModel();
                    walletsModel.setWallet_type(object.getString("wallet_type"));
                    walletsModel.setWallet_name(object.getString("wallet_name"));
                    walletsModel.setBalance(object.getString("balance"));
                    walletsModelList.add(walletsModel);
                    walletsList.add(object.getString("wallet_name") + " : " + getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                }

                if (walletsModelList.size() > 0) {
                    adapter = new ArrayAdapter<String>(HomeActivity.this, R.layout.adpter_multiwallet_spinner, walletsList);
                    tv_multi_wallet.setAdapter(adapter);
                    tv_lnr_multi_wallet.setVisibility(View.VISIBLE);

                } else {
                    tv_lnr_multi_wallet.setVisibility(View.GONE);
                }

            } else {
                if (jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                } else {
                    displayErrorDialog(jsonObject.getString("msg") + "");
                }
            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    // display error in dialog
    private void displayErrorDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!HomeActivity.this.isFinishing()) {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setTitle("Info!")
                            .setCancelable(false)
                            .setMessage(message)
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        });
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESSALERT) {
                parseAlertResponse(msg.obj.toString());
            } else if (msg.what == SUCCESSSERVICE) {
                parseServiceResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                parseSuccessWalletResponse(msg.obj.toString());
            } else if (msg.what == AUTHENTICATION_FAIL) {
                Utility.logout(HomeActivity.this, msg.obj.toString());
            }
        }
    };
    // [END]

    @Override
    public void onClick(View v) {
        if (v == llChangePassword) {
            openFragment(MENU_CHANGE_PASSWORD, new ChangePasswordFragment());
            displayBottomNavigationDynamic(CHANGE_PASSWORD);
        } else if (v == llLogout) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage(R.string.confirm_logout_message)
                    .setPositiveButton(R.string.confirm_signout_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().clear().commit();
                            Intent intent1 = new Intent(getContextInstance(), LoginActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent1);
                            finish();
                            sharedPreferences.edit().putBoolean(constants.LOGOUT, true).commit();
                        }
                    })
                    .setNegativeButton("cancel", null)
                    .create()
                    .show();

        } else if (v == llNotification) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        } else if (v == llUpdate) {
            updateData_apiCall = new UpdateData_ApiCall(context);

//            openFragment(MENU_UPDATE_DATA, new UpdateData());
            displayBottomNavigationDynamic(UPDATE_DATA);
        }
        // Notification bottom navigation listener
        else if (v == llNotificationRecharge) {
            openFragment(MENU_RECHARGE, new MobileRecharge());
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            displayBottomNavigationDynamic(RECHARGE);
        } else if (v == llNotificationRecentTransaction) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (v == llNotificationTransactionSearch) {
            openTransactionSearchFragment();
            displayBottomNavigationDynamic(TRANSACTION_SEARCH);
        }
        // Cashbook bottom navigation listener
        else if (v == llCashbookRecharge) {
            openFragment(MENU_RECHARGE, new MobileRecharge());
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            displayBottomNavigationDynamic(RECHARGE);
        } else if (v == llCashbookRecentTrasaction) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (v == llCashbookNotification) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        }
        // Change password bottom navigation listener
        else if (v == llChangePasswordRecharge) {
            openFragment(MENU_RECHARGE, new MobileRecharge());
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            displayBottomNavigationDynamic(RECHARGE);
        } else if (v == llChangePasswordRecentTransaction) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (v == llChangePasswordNotification) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        }
        // Recharge bottom navigation listener
        else if (v == llRechargeTransaction) {
            openTransactionSearchFragment();
            displayBottomNavigationDynamic(TRANSACTION_SEARCH);
        } else if (v == llRechargeRecentTransaction) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (v == llRechargeNotification) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        }
        // Transaction search bottom navigation listener
        else if (v == llTransactionSearchRecharge) {
            openFragment(MENU_RECHARGE, new MobileRecharge());
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            displayBottomNavigationDynamic(RECHARGE);
        } else if (v == llTransactionSearchRecentTransaction) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (v == llTransactionSearchNotification) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        }
        // DMT
        else if (v == llTransactionSearchRecentTransactionDMT) {
            openFragment(MENU_RECENT_TRANSACTION, new RecentTransactionFragment());
            displayBottomNavigationDynamic(RECENT_TRANSACTION);
        } else if (v == llTransactionSearchDMT) {
            openTransactionSearchFragment();
            displayBottomNavigationDynamic(TRANSACTION_SEARCH);
        } else if (v == llSenderSearchDMT) {
            openFragment(MENU_DMT, new DMTFragment());
            displayBottomNavigationDynamic(DMT);
        }

        // Recent transaction bottom navigation listener
        else if (v == llRecentTransactionRecharge) {
            openFragment(MENU_RECHARGE, new MobileRecharge());
            sharedPreferences.edit().putString(constants.MOBILENUMBER, "").commit();
            sharedPreferences.edit().putString(constants.AMOUNT, "").commit();
            displayBottomNavigationDynamic(RECHARGE);
        } else if (v == llRecentTransactionTransactionSearch) {
            openTransactionSearchFragment();
            displayBottomNavigationDynamic(TRANSACTION_SEARCH);
        } else if (v == llRecentTransactionNotification) {
            openFragment(MENU_NOTIFICATION, new NotificationFragment());
            displayBottomNavigationDynamic(NOTIFICATION);
        }
        // [END]
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNotificationReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNotificationReceiver();
    }

    /* [START] - Custom check notification data class */
    private class CheckNotification extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Dlog.d("Receiver action : " + action);
            if (action.equals(ACTION_REFRESH_HOMEACTIVITY)) {
                try {
                    setNotificationCounter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Dlog.d("Error : " + ex.getMessage());
                }
            }
        }
    }
    // [END]

}
