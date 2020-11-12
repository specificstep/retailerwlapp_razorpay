package specificstep.com.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.BuildConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import specificstep.com.Adapters.BannerAdapter;
import specificstep.com.Adapters.NavigationDrawerAdapter;
import specificstep.com.Adapters.ServicesAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Database.NotificationTable;
import specificstep.com.GlobalClasses.AppController;
import specificstep.com.GlobalClasses.Config;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.MyLocation;
import specificstep.com.GlobalClasses.NotificationUtils;
import specificstep.com.GlobalClasses.ServicesModel;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.AlertModels;
import specificstep.com.Models.BannerModel;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.NavigationModels;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.MyPrefs;
import specificstep.com.utility.UpdateData_ApiCall;
import specificstep.com.utility.Utility;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, LocationListener {

    // all menu names
    private final String MENU_HOME = "Home";
    private final String MENU_RECHARGE = "Mobile Prepaid Recharge";
    private final String MENU_DTH = "DTH Recharge";
    private final String MENU_ELECTRICITY = "Electricity Bill Pay";
    private final String MENU_RECENT_TRANSACTION = "Recent Transaction";
    private final String MENU_TRANSACTION_SEARCH = "Transaction Search";
    private final String MENU_COMPLAIN_REPORT = "Complain Report";
    //private final String MENU_CASH_BOOK = "Cash Book";
    private final String MENU_CASH_BOOK = "Payment Report";
    private final String MENU_ACCOUNT_LEDGER = "Account Ledger";
    private final String MENU_UPDATE_DATA = "Update Data";
    private final String MENU_CHANGE_PASSWORD = "Change Password";
    private final String MENU_DMT = "DMT";
    //private final String MENU_DMT_TRANSACTION_LIST = "DMT Transaction List";
    private final String MENU_DMT_TRANSACTION_LIST = "Transaction Reports";
    private final String MENU_PAYMENT_REQUEST = "Payment Request";
    private final String MENU_ONLINE_PAYMENT = "Wallet Topup";

    private final String MENU_GAS = "Gas Bill Pay";
    private final String MENU_MOBILE_POSTPAID = "Mobile Postpaid Bill Pay";
    private final String MENU_WATER = "Water Bill Pay";
    private final String MENU_NOTIFICATION = "Notification";
    private final String MENU_PARENT_USER = "Parent User";
    private final String MENU_LOGOUT = "Log Out";
    private final String MENU_ANOTHER_USER = "Login with Other number";
    private final String MENU_SHARE = "Share";
    String TAG = "Main2Activity :: ";
    private SharedPreferences sharedPreferences;

    public static Context context;

    MenuItem menuItem;
    ArrayList<String> menuWallet;
    private final int ERROR = 2, SUCCESSALERT = 3,
            SUCCESS_SERVICE = 4, SUCCESS_WALLET_LIST = 5, SUCCESS_BANNER = 6,
            AUTHENTICATION_FAIL = 10, SUCCESS_NOTIFICATION_CLICK = 11;
    private MyPrefs prefs;
    private Constants constants;
    private DatabaseHelper databaseHelper;
    private LinearLayout /*llRecharge,*/ llRecentTransaction, llLogout,
            llMain, llUpdate, ll_wallettopup_act_main, llChangePassword, llNotification, llCashBook,
            llAccountLedger, /*llDmt,*/
            llDmtTransactionList/*,
            llPaymentRequest, llParentUser, linParentUser, linLogOut*/;
    private TextView txtTotalNotification;
    private BottomNavigationView navigation;
    // [END]

    /* [START] - Variables */
    private String deviceId = "", title = "";
    // Position is use for which fragment display
    private int position = 0;
    private ArrayList<User> userArrayList;

    // Notification receiver
    private BroadcastReceiver notificationReceiver = null;
    public static final String ACTION_REFRESH_NOTIFICATION = "specificstep.com.metroenterprise.REFRESH_NOTIFICATION";
    TextView txtAlert;
    List<String> alertModelsList;
    AlertModels alertModels;
    int MY_PERMISSION_LOCATION = 1;
    MyLocation myLocation = new MyLocation();

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;

    //Banner 3-12-2019
    ArrayList<BannerModel> bannerModelList;
    BannerModel bannerModel;

    ArrayList<ServicesModel> serviceModelArrayList;
    ArrayList<ServicesModel> serviceModelArrayListFinal;
    ServicesModel servicesModel;
    private NestedScrollView nestedScrollView;
    private RecyclerView recyclerView;
    private ServicesAdapter servicesAdapter;
    public static DrawerLayout drawer = null;
    private TextView tv_email, tv_name;
    private Spinner tv_multi_wallet;
    private LinearLayout tv_lnr_multi_wallet;
    ArrayAdapter<String> adapter;
    public static String drawerPos = "";
    private TransparentProgressDialog transparentProgressDialog;
    ViewPager viewPager;
    BannerAdapter viewPagerAdapter;
    LinearLayout sliderDotspanel;
    private int dotscount;
    private ImageView[] dots;
    RelativeLayout relBanner;
    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 2000;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 2000; // time in milliseconds between successive task executions.
    boolean doubleBackToExitPressedOnce = false;

    private Context getContextInstance() {
        if (context == null) {
            context = Main2Activity.this;
            return context;
        } else {
            return context;
        }
    }

    // Custom navigation menu
    private ListView lstNavigation;
    TextView txtVersion;
    Thread thread;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    UpdateData_ApiCall updateData_apiCall;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences(Constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        context = Main2Activity.this;
        context = (AppController) getApplication();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        transparentProgressDialog = new TransparentProgressDialog(Main2Activity.this, R.drawable.fotterloading);
        databaseHelper = new DatabaseHelper(getContextInstance());
        userArrayList = databaseHelper.getUserDetail();
        viewPager = (ViewPager) findViewById(R.id.viewPagerBanner);

        sliderDotspanel = (LinearLayout) findViewById(R.id.SliderDotsBanner);
        relBanner = (RelativeLayout) findViewById(R.id.relBanner);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                makeWalletCall();
                //if (Constants.serviceModelArrayList == null && Constants.serviceModelArrayList.size() == 0) {
                checkServices();
                //}
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = Main2Activity.this.getCurrentFocus();
                if (view == null) {
                    view = new View(Main2Activity.this);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
        //drawer.addDrawerListener(toggle);
        //toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        setCustomNavigation();
        //checkServices();
        tv_name = (TextView) header.findViewById(R.id.tv_header_name);
        tv_email = (TextView) header.findViewById(R.id.tv_header_email);
        tv_multi_wallet = (Spinner) header.findViewById(R.id.tv_multi_wallet);
        tv_lnr_multi_wallet = (LinearLayout) header.findViewById(R.id.tv_lnr_multi_wallet);


        Constants.chaneBackground(Main2Activity.this, (LinearLayout) header.findViewById(R.id.lnrNavHeader));
        //set icon as per package
        Constants.chaneIcon(Main2Activity.this, (CircleImageView) header.findViewById(R.id.profile_image));

        /* Set header content of navigation drawer */
        if (userArrayList != null && userArrayList.size() > 0) {
            tv_email.setText(userArrayList.get(0).getUser_name());
            tv_name.setText(userArrayList.get(0).getName());
        }


        //new
        myLocation.getLocation(Main2Activity.this, locationResult);
        marshmallowGPSPremissionCheck();

        context = Main2Activity.this;
        /* [START] - Set actionbar title */
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(Constants.chaneIcon(Main2Activity.this));
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + getResources().getColor(R.color.colorWhite) + "\">" + "\t" + Constants.changeAppName(Main2Activity.this) + "</font>"));
        // [END]

        initControls();
        getBundleData();
        setListener();

        // Display number of unread message
        setNotificationCounter();

        /*try {
            Gson gson = new Gson();
            String jsonData = sharedPreferences.getString(Constants.KEY_SERVICE_DATA, "");
            ArrayList<ServicesModel> list = (ArrayList<ServicesModel>) gson.fromJson(jsonData,
                    new TypeToken<ArrayList<ServicesModel>>() {
                    }.getType());
            if (list.size() > 0) {
                servicesAdapter = new ServicesAdapter(Main2Activity.this, list);
                recyclerView.setAdapter(servicesAdapter);
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }*/

        checkServices();
        //getCurrentVersion();
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                // your code here...
                makeAlertCall();
            }
        };
        timer.schedule(hourlyTask, 0l, 1000 * 10 * 60);

        if (Constants.checkInternet(Main2Activity.this)) {
            makeWalletCall();
            makeBannerCall();
        }
        sharedPreferences.edit().putString(Constants.DMT_MOBILE, "").commit();

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

        if (Constants.checkInternet(Main2Activity.this)) {
            updateDataAfterLogin();
        }

        this.mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    try {
                        String dataMsg = getIntent().getStringExtra("message");
                        String dataTitle = getIntent().getStringExtra("title");
                        String dataNotificationId = getIntent().getStringExtra("notificationId");
                        System.out.println("Message FCM: " + dataMsg);
                        Intent intent1 = new Intent(Main2Activity.this, HomeActivity.class);
                        intent1.putExtra("position", 8);
                        startActivity(intent1);
                        System.out.println("Background FCM Message: " + dataMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

    }

    String currentVersion, storeVersion;

    public void getCurrentVersion() {

        PackageManager pm = getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo = pm.getPackageInfo(getPackageName(), 0);
            currentVersion = String.valueOf(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (sharedPreferences.contains(Constants.STOREAPPVERSION)) {

            storeVersion = sharedPreferences.getString(Constants.STOREAPPVERSION, "");
            if (storeVersion.equals(currentVersion)) {
            } else {
                sharedPreferences.edit().putString(Constants.STOREAPPVERSION, currentVersion).commit();
                databaseHelper.truncateUpdateData();
                updateDataAfterLogin();
            }

        } else {
            sharedPreferences.edit().putString(constants.STOREAPPVERSION, currentVersion).commit();
            databaseHelper.truncateUpdateData();
            updateDataAfterLogin();
        }

    }

    // set custom notification
    public void setCustomNavigation() {
        try {
            lstNavigation = (ListView) findViewById(R.id.lst_NavigationView);
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
                                            String updateTime1 = Constants.parseDateToddMMyyyy("hh:mm:ss", "hh:mm:ss", prefs.retriveString(constants.PREF_UPDATE_TIME, "0"));
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                                            String curTime = dateFormat.format(Calendar.getInstance().getTime());
                                            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss", Locale.US);
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

                                            if (hr > 0) {
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

            ArrayList<NavigationModels> stringArrayList = new ArrayList<NavigationModels>();
            // All menu items name
            stringArrayList.add(new NavigationModels(MENU_HOME, R.drawable.ic_home, 0));
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.mobile_prepaid_id)) {
                    stringArrayList.add(new NavigationModels(MENU_RECHARGE, R.drawable.ic_mobile, 0));
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.dth_id)) {
                    stringArrayList.add(new NavigationModels(MENU_DTH, R.drawable.dth, 0));
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                    stringArrayList.add(new NavigationModels(MENU_DMT, R.drawable.ic_dmt, 0));
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.water_id)) {
                    stringArrayList.add(new NavigationModels(MENU_WATER, R.drawable.tap, 0));
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.electricity_id)) {
                    stringArrayList.add(new NavigationModels(MENU_ELECTRICITY, R.drawable.ic_electricity2, 0));
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.gas_id)) {
                    stringArrayList.add(new NavigationModels(MENU_GAS, R.drawable.gas, 0));
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                    stringArrayList.add(new NavigationModels(MENU_MOBILE_POSTPAID, R.drawable.mobile, 0));
                }
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


            //Razopay feature
            ll_wallettopup_act_main.setVisibility(View.GONE);
            llChangePassword.setVisibility(View.VISIBLE);
            //stringArrayList.add(new NavigationModels(MENU_ONLINE_PAYMENT, R.drawable.ic_payment_on_black_24dp, 0));



            /*if(databaseHelper.getPaymentGateway().size()>0) {
                stringArrayList.add(new NavigationModels(MENU_ONLINE_PAYMENT, R.drawable.ic_payment_on_black_24dp, 0));
                ll_wallettopup_act_main.setVisibility(View.VISIBLE);
                llChangePassword.setVisibility(View.GONE);
            } else {*/
            //}
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
                    int pos;
                    if (TextUtils.equals(selectedMenuName, MENU_HOME)) {
                        Intent intent = new Intent(getContextInstance(), Main2Activity.class);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_RECHARGE)) {
                        pos = 0;
                        Intent intent = new Intent(Main2Activity.this, HomeActivity.class);
                        intent.putExtra("position", pos);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_DTH)) {
                        pos = 17;
                        Intent intent = new Intent(Main2Activity.this, HomeActivity.class);
                        intent.putExtra("position", pos);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_ELECTRICITY)) {
                        pos = 18;
                        Intent intent = new Intent(Main2Activity.this, HomeActivity.class);
                        intent.putExtra("position", pos);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_RECENT_TRANSACTION)) {
                        position = 1;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_COMPLAIN_REPORT)) {
                        position = 3;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_CASH_BOOK)) {
                        position = 4;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_ACCOUNT_LEDGER)) {
                        position = 5;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_UPDATE_DATA)) {
                        updateData_apiCall = new UpdateData_ApiCall(context);
//                        position = 6;
//                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
//                        intent.putExtra("position", position);
//                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_DMT)) {
                        position = 9;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_DMT_TRANSACTION_LIST)) {
                        Intent intent = new Intent(Main2Activity.this, CombineTransactionListActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (TextUtils.equals(selectedMenuName, MENU_PAYMENT_REQUEST)) {
                        position = 11;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_ONLINE_PAYMENT)) {
                        position = 19;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_GAS)) {
                        position = 14;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_MOBILE_POSTPAID)) {
                        position = 15;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_WATER)) {
                        position = 16;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_CHANGE_PASSWORD)) {
                        position = 7;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_NOTIFICATION)) {
                        position = 8;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_PARENT_USER)) {
                        position = 12;
                        Intent intent = new Intent(getContextInstance(), HomeActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    } else if (TextUtils.equals(selectedMenuName, MENU_SHARE)) {
                        showSharePopup();
                    } else if (TextUtils.equals(selectedMenuName, MENU_ANOTHER_USER)) {
                        new AlertDialog.Builder(Main2Activity.this)
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
                        new AlertDialog.Builder(Main2Activity.this)
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
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.changeAppName(Main2Activity.this));
            String shareMessage = "\nLet me recommend you " + Constants.changeAppName(Main2Activity.this) + " application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + Constants.APP_PACKAGE_NAME + "\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = "";
        int pos;
        int curr_pos;
        if (id == R.id.nav_Home) {
            Intent intent = new Intent(getContextInstance(), Main2Activity.class);
            startActivity(intent);
        } else if (id == R.id.nav_recharge) {
            pos = 0;
            curr_pos = 0;
            Intent intent = new Intent(Main2Activity.this, HomeActivity.class);
            intent.putExtra("position", pos);
            intent.putExtra("curr_pos", curr_pos);
            startActivity(intent);
        } else if (id == R.id.nav_rec_trans) {
            position = 1;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_trans_search) {
            position = 2;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_complain_report) {
            position = 3;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_Cashbook) {
            position = 4;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_account_ledger) {
            position = 5;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_update_data) {
            updateData_apiCall = new UpdateData_ApiCall(context);

//            position = 6;
//            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
//            intent.putExtra("position", position);
//            startActivity(intent);
        } else if (id == R.id.nav_ChangePassword) {
            position = 7;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_dmt) {
            position = 9;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_dmt_transaction_list) {
            position = 10;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_payment_req) {
            position = 11;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_gas) {
            position = 14;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_mobile_postpaid) {
            position = 15;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_water) {
            position = 16;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_Notification) {
            position = 8;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            showSharePopup();
        } else if (id == R.id.nav_another_number) {
            new AlertDialog.Builder(Main2Activity.this)
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
            new AlertDialog.Builder(Main2Activity.this)
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


    //new

    //multi wallet 14-3-2019
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
                String decrypted_response = Constants.decryptAPI(Main2Activity.this, encrypted_response);
                Dlog.d("Wallet : " + "decrypted_response : " + decrypted_response);
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
                    menuWallet.add(object.getString("wallet_name") + " : " + getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                }

                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                try {
                    if (menuItem != null) {
                        if (walletsModelList.size() > 0) {
                            menuItem.setVisible(true);
                        } else {
                            menuItem.setVisible(false);
                        }
                    }

                    if (walletsModelList.size() > 0) {
                        adapter = new ArrayAdapter<String>(Main2Activity.this, R.layout.adpter_multiwallet_spinner, menuWallet);
                        tv_multi_wallet.setAdapter(adapter);
                        tv_lnr_multi_wallet.setVisibility(View.VISIBLE);

                    } else {
                        tv_lnr_multi_wallet.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Dlog.d(e.toString());
                }

            } else {
                if (jsonObject.getString("msg").contains("1002")) {
                    myHandler.obtainMessage(AUTHENTICATION_FAIL, jsonObject.getString("msg")).sendToTarget();
                }
            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void checkServices() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.getService;
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
                    myHandler.obtainMessage(SUCCESS_SERVICE, response).sendToTarget();
                } catch (Exception ex) {
                    dismissProgressDialog();
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    private void marshmallowGPSPremissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main2Activity.this,
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
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mRegistrationBroadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    public void parseAlertResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {

                String encrypted_response = jsonObject.getString("data");
                String decrypted_data = constants.decryptAPI(Main2Activity.this, encrypted_response);
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
            }
        } catch (JSONException e) {
            Dlog.d("Error : " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void updateDataAfterLogin() {
        boolean checkDataUpdateRequire = false;
        // Update data if database is empty
        if (databaseHelper.checkEmpty() == false) {
            checkDataUpdateRequire = true;
        } else {
            Dlog.d("Data not empty");
            // Update data if date change
            String currentDate = DateTime.getDate();
            String updateDate = prefs.retriveString(constants.PREF_UPDATE_DATE, "0");

            try {
                if (!prefs.contain(constants.PREF_UPDATE_DATE)) {
                    checkDataUpdateRequire = true;
                } else {
                    try {
                        Dlog.d("Update date available");
                        //String updateTime = Constants.parseDateToddMMyyyy("hh:mm:ss", "hh:mm a", prefs.retriveString(constants.PREF_UPDATE_TIME, "0"));
                        String updateTime = prefs.retriveString(constants.PREF_UPDATE_TIME, "0");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                        /*SimpleDateFormat dateFormatDate = new SimpleDateFormat("dd-MM-yyyy");
                        String curDate = dateFormatDate.format(currentDate);
                        String upDate = dateFormatDate.format(updateDate);*/
                        String curTime = /*dateFormat.format(*/DateTime.getTime()/*)*/;
                        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.US);
                        Date dateUpdate = df.parse(updateDate + " " + updateTime);
                        Date dateCurrent = df.parse(currentDate + " " + curTime);

                        long diff = dateCurrent.getTime() - dateUpdate.getTime();
                        long seconds = diff / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;
                        long days = hours / 24;

                        if (hours < 0) {
                            hours = -hours;
                        }

                        if(hours < 0) {
                            checkDataUpdateRequire = true;
                        } else if (!TextUtils.equals(updateDate, currentDate)) {
                            checkDataUpdateRequire = true;
                        } else if (hours >= 4) {
                            checkDataUpdateRequire = true;
                        } else {

                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (checkDataUpdateRequire) {
            updateData_apiCall = new UpdateData_ApiCall(context);
//            position = 6;
//            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
//            intent.putExtra("position", position);
//            intent.putExtra(constants.KEY_REQUIRE_UPDATE, "1");
//            startActivity(intent);
        }
    }

    private void initControls() {
        /* [START] - Initialise class objects */
        constants = new Constants();
        databaseHelper = new DatabaseHelper(getContextInstance());
        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        prefs = new MyPrefs(getContextInstance(), constants.PREF_NAME);
        // [END]

        /* [START] - get user data from database and store into array list */
        userArrayList = databaseHelper.getUserDetail();
        // [END]

        /* [START] - Initialise control objects */
        // Bottom navigation control
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        // All linear layout
        //llRecharge = (LinearLayout) findViewById(R.id.lin_recharge_act_main);
        llRecentTransaction = (LinearLayout) findViewById(R.id.lin_recent_trans_act_main);
        llUpdate = (LinearLayout) findViewById(R.id.lin_update_button);
        llChangePassword = (LinearLayout) findViewById(R.id.ll_ChangePassword_act_main);
        ll_wallettopup_act_main = (LinearLayout) findViewById(R.id.ll_wallettopup_act_main);
        llNotification = (LinearLayout) findViewById(R.id.lin_Notification_act_main);
        llCashBook = (LinearLayout) findViewById(R.id.lin_CashBook_act_main);
        llLogout = (LinearLayout) findViewById(R.id.lin_logout_act_main);
        llMain = (LinearLayout) findViewById(R.id.ll_main);

        llAccountLedger = (LinearLayout) findViewById(R.id.ll_account_ledger_act_main);
        /*llDmt = (LinearLayout) findViewById(R.id.ll_dmt_act_main);
        llParentUser = (LinearLayout) findViewById(R.id.ll_parent_user_main);
        linParentUser = (LinearLayout) findViewById(R.id.lin_parent_user_act_main);
        linLogOut = (LinearLayout) findViewById(R.id.lin_logout_main);*/

        llDmtTransactionList = (LinearLayout) findViewById(R.id.ll_dmt_transaction_list_act_main);
        //llPaymentRequest = (LinearLayout) findViewById(R.id.ll_payment_request_act_main);

        // Text view
        txtTotalNotification = (TextView) findViewById(R.id.txt_TotalNotification);
        // [END]
        txtAlert = (TextView) findViewById(R.id.txtAlertMain);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        recyclerView = (RecyclerView) findViewById(R.id.recycle_service);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        registerNotificationReceiver();

    }

    private void registerNotificationReceiver() {
        /* [START] - Create custom notification for receiver notification data */
        try {
            if (notificationReceiver == null) {
                // Add notification filter
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_REFRESH_NOTIFICATION);
                // Create notification object
                notificationReceiver = new Main2Activity.CheckNotification();
                // Register receiver
                Main2Activity.this.registerReceiver(notificationReceiver, intentFilter);
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
                Main2Activity.this.unregisterReceiver(notificationReceiver);
                notificationReceiver = null;
            }
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void getBundleData() {
        if (getIntent().getStringExtra("device_id") != null
                && !TextUtils.isEmpty(getIntent().getStringExtra("device_id"))) {
            deviceId = getIntent().getStringExtra("device_id");
        }
    }

    private void setListener() {
        // Navigation selected listener
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        // On click listener
        //llRecharge.setOnClickListener(this);
        llRecentTransaction.setOnClickListener(this);
        llLogout.setOnClickListener(this);
        llUpdate.setOnClickListener(this);
        ll_wallettopup_act_main.setOnClickListener(this);
        llChangePassword.setOnClickListener(this);
        llNotification.setOnClickListener(this);
        llCashBook.setOnClickListener(this);

        llAccountLedger.setOnClickListener(this);
        //llDmt.setOnClickListener(this);
        //llParentUser.setOnClickListener(this);
        llDmtTransactionList.setOnClickListener(this);
        //llPaymentRequest.setOnClickListener(this);

        //linParentUser.setOnClickListener(this);
        //linLogOut.setOnClickListener(this);

    }

    /**
     * Display number of unread message
     */
    public void setNotificationCounter() {
        // Get total number of unread message and store into global static variable
        Constants.TOTAL_UNREAD_NOTIFICATION = new NotificationTable(getContextInstance()).getNumberOfNotificationRecord() + "";
        int totalNotification = 0;
        try {
            totalNotification = Integer.parseInt(Constants.TOTAL_UNREAD_NOTIFICATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            Dlog.d("Error : " + ex.getMessage());
            totalNotification = 0;
        }
        // Check if total number of unread message if grater then 0 then display total notification text view
        if (totalNotification > 0) {
            txtTotalNotification.setVisibility(View.VISIBLE);
            txtTotalNotification.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
        }
        // else number of unread message is 0 then set notification text view visible gone
        else {
            txtTotalNotification.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuItem = menu.findItem(R.id.action_balance_menu_main);
        makeWalletCall();
        //setNotificationCounter();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_balance_menu_main:
                if (Constants.checkInternet(Main2Activity.this)) {
                    Constants.showWalletPopup(Main2Activity.this);
                } else {
                    //Constants.showNoInternetDialog(Main2Activity.this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {

        Intent intent = null;
        /*if (v == llRecharge) {
            position = 0;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else */
        if (v == llRecentTransaction) {
            position = 1;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == llCashBook) {
            position = 4;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == llAccountLedger) {
            position = 5;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == llUpdate) {
            updateData_apiCall = new UpdateData_ApiCall(context);

//            position = 6;
//            intent = new Intent(getContextInstance(), HomeActivity.class);
//            intent.putExtra("position", position);
        } else if (v == ll_wallettopup_act_main) {
            position = 19;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == llChangePassword) {
            position = 7;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == llNotification) {
            position = 8;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } /*else if (v == llDmt) {
            position = 9;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } */ else if (v == llDmtTransactionList) {
            position = 10;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } /*else if (v == llPaymentRequest) {
            //position = 11;
            position = 3;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == llParentUser) {
            //position = 11;
            position = 12;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } */ else if (v == llLogout) {
            intent = new Intent(getContextInstance(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sharedPreferences.edit().clear().commit();
            sharedPreferences.edit().putBoolean(constants.LOGOUT, true).commit();
            sharedPreferences.edit().putString(constants.VERIFICATION_STATUS, "1").commit();
            sharedPreferences.edit().putString(constants.LOGIN_STATUS, "0").commit();
        } /*else if (v == linParentUser) {
            position = 12;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        } else if (v == linLogOut) {
            position = 13;
            intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
        }*/
        if (intent != null)
            startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(Main2Activity.this);
        }
        databaseHelper.closeDatabase();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                moveTaskToBack(true);
                finishAffinity();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    // display error in dialog
    private void displayErrorDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!((Activity) context).isFinishing()) {
                    new AlertDialog.Builder(Main2Activity.this)
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
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESSALERT) {
                parseAlertResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_SERVICE) {
                dismissProgressDialog();
                parseServiceResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                parseSuccessWalletResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_BANNER) {
                parseSuccessBannerResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_NOTIFICATION_CLICK) {
                //parseSuccessBannerResponse(msg.obj.toString());
                System.out.println("Notification Send success");
            } else if (msg.what == AUTHENTICATION_FAIL) {
                dismissProgressDialog();
                Utility.logout(Main2Activity.this, msg.obj.toString());
            }
        }
    };
    // [END]

    public void parseServiceResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                if (jsonObject.getString("msg").equals("List generated")) {
                    String encrypted_response = jsonObject.getString("data");
                    String decrypted_data = Constants.decryptAPI(Main2Activity.this, encrypted_response);
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

                        servicesAdapter = new ServicesAdapter(Main2Activity.this, serviceModelArrayListFinal);
                        recyclerView.setAdapter(servicesAdapter);
                        //nestedScrollView.fullScroll(View.FOCUS_UP);
                        //nestedScrollView.scrollTo(0, 0);
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

    //banner api 3-12-2019
    public void makeBannerCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.banner;
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
                    myHandler.obtainMessage(SUCCESS_BANNER, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    public void parseSuccessBannerResponse(String response) {
        Dlog.d("Banner Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(Main2Activity.this, encrypted_response);
                Dlog.d("Banner : " + "decrypted_response : " + decrypted_response);
                JSONObject array = new JSONObject(decrypted_response);
                JSONArray jsonArray = array.getJSONArray("banner");
                bannerModelList = new ArrayList<BannerModel>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    bannerModel = new BannerModel();
                    bannerModel.setId(object.getString("id"));
                    bannerModel.setName(object.getString("name"));
                    bannerModel.setImage(object.getString("image"));
                    bannerModel.setFrom(object.getString("from"));
                    bannerModel.setTo(object.getString("to"));
                    bannerModelList.add(bannerModel);
                }

                try {
                    if (bannerModelList.size() > 0) {
                        relBanner.setVisibility(View.VISIBLE);

                        viewPagerAdapter = new BannerAdapter(this, bannerModelList);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = viewPagerAdapter.getCount();
                        dots = new ImageView[dotscount];
                        for (int i = 0; i < dotscount; i++) {
                            dots[i] = new ImageView(this);
                            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(8, 0, 8, 0);
                            sliderDotspanel.addView(dots[i], params);
                        }
                        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
                        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            }

                            @Override
                            public void onPageSelected(int position) {
                                for (int i = 0; i < dotscount; i++) {
                                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
                                }
                                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {
                            }
                        });

                        /*After setting the adapter use the timer */
                        final Handler handler = new Handler();
                        final Runnable Update = new Runnable() {
                            public void run() {
                                if (currentPage == dotscount) {
                                    currentPage = 0;
                                }
                                viewPager.setCurrentItem(currentPage++, true);
                            }
                        };

                        timer = new Timer(); // This will create a new Thread
                        timer.schedule(new TimerTask() { // task to be scheduled
                            @Override
                            public void run() {
                                handler.post(Update);
                            }
                        }, DELAY_MS, PERIOD_MS);

                    } else {
                        relBanner.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Dlog.d(e.toString());
                }

            } else {

            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    position = 7;
                    title = "Change Password";
                    getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + getResources().getColor(R.color.colorServiceText) + "\">" + "\t" + title + "</font>"));
                    llMain.setVisibility(View.GONE);
                    Intent intent_ChangePassword = new Intent(getContextInstance(), HomeActivity.class);
                    intent_ChangePassword.putExtra("position", position);
                    startActivity(intent_ChangePassword);
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop notification update count down timer
        unregisterNotificationReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        // stop notification update count down timer
        unregisterNotificationReceiver();
    }

    /* [START] - Custom check notification data class */
    private class CheckNotification extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Dlog.d("Receiver action : " + action);
            if (action.equals(ACTION_REFRESH_NOTIFICATION)) {
                Dlog.d("Receiver call ACTION_REFRESH_Main2Activity");
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(Main2Activity.this, R.drawable.fotterloading);
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

}
