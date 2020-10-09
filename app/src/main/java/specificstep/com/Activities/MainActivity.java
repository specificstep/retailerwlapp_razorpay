package specificstep.com.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import specificstep.com.Adapters.ServicesAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Database.NotificationTable;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.MyLocation;
import specificstep.com.GlobalClasses.ServicesModel;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.AlertModels;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.MyPrefs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    MenuItem menuItem;
    ArrayList<String> menuWallet;
    private final int ERROR = 2, SUCCESSALERT = 3,
            SUCCESS_SERVICE = 4, SUCCESS_WALLET_LIST = 5;
    private Context context;
    private SharedPreferences sharedPreferences;
    private MyPrefs prefs;
    private Constants constants;
    private DatabaseHelper databaseHelper;
    private LinearLayout /*llRecharge,*/ llRecentTransaction, llLogout,
            llMain, llUpdate, llChangePassword, llNotification, llCashBook,
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
    String TAG = "MainActivity :: ";
    TextView txtAlert;
    List<String> alertModelsList;
    AlertModels alertModels;
    int MY_PERMISSION_LOCATION = 1;
    MyLocation myLocation = new MyLocation();

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;

    private Context getContextInstance() {
        if (context == null) {
            context = MainActivity.this;
            return context;
        } else {
            return context;
        }
    }

    ArrayList<ServicesModel> serviceModelArrayList;
    ServicesModel servicesModel;

    private RecyclerView recyclerView;
    private ServicesAdapter servicesAdapter;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_testing);
        myLocation.getLocation(MainActivity.this, locationResult);
        marshmallowGPSPremissionCheck();

        Constants.chaneBackground(MainActivity.this, (LinearLayout) findViewById(R.id.lnrMainActivity));
        context = MainActivity.this;
        /* [START] - Set actionbar title */
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(Constants.chaneIcon(MainActivity.this));
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + getResources().getColor(R.color.colorWhite) + "\">" + "\t" + Constants.changeAppName(MainActivity.this) + "</font>"));
        // [END]

        initControls();
        getBundleData();
        setListener();

        // Display number of unread message
        setNotificationCounter();

        /*try {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ServicesModel>>() {
            }.getType();
//            ArrayList<ServicesModel> list = gson.fromJson(Constants.KEY_SERVICE_DATA, type);
            String jsonData = sharedPreferences.getString(Constants.KEY_SERVICE_DATA, "");
            ArrayList<ServicesModel> list = (ArrayList<ServicesModel>) gson.fromJson(jsonData,
                    new TypeToken<ArrayList<ServicesModel>>() {
                    }.getType());
//            Toast.makeText(context, list.size() + "", Toast.LENGTH_SHORT).show();
            if(list.size()>0) {
                servicesAdapter = new ServicesAdapter(MainActivity.this, list);
                recyclerView.setAdapter(servicesAdapter);
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }*/

        checkServices();

        if (Constants.checkInternet(MainActivity.this)) {
            updateDataAfterLogin();
        }
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                // your code here...
                makeAlertCall();
            }
        };
        timer.schedule(hourlyTask, 0l, 1000 * 10 * 60);

        if (Constants.checkInternet(MainActivity.this)) {
            makeWalletCall();
        }
        sharedPreferences.edit().putString(Constants.DMT_MOBILE, "").commit();

        //}
    }

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
                String decrypted_response = Constants.decryptAPI(MainActivity.this, encrypted_response);
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

    public void checkServices() {
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
            ActivityCompat.requestPermissions(MainActivity.this,
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

    public void parseAlertResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {

                String encrypted_response = jsonObject.getString("data");
                String decrypted_data = constants.decryptAPI(MainActivity.this, encrypted_response);
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
        } else
            Dlog.d("Data not empty");
        // Update data if date change
        String updateDate = prefs.retriveString(constants.PREF_UPDATE_DATE, "0");
        String currentDate = DateTime.getDate();
        if (TextUtils.equals(updateDate, "0")) {
            checkDataUpdateRequire = true;
        } else
            Dlog.d("Update date available");
        if (!TextUtils.equals(updateDate, currentDate)) {
            checkDataUpdateRequire = true;
        } else
            Dlog.d("Update date and current date are same");
        if (checkDataUpdateRequire) {
            position = 6;
            Intent intent = new Intent(getContextInstance(), HomeActivity.class);
            intent.putExtra("position", position);
            intent.putExtra(constants.KEY_REQUIRE_UPDATE, "1");
            startActivity(intent);
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
                notificationReceiver = new CheckNotification();
                // Register receiver
                MainActivity.this.registerReceiver(notificationReceiver, intentFilter);
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
                MainActivity.this.unregisterReceiver(notificationReceiver);
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
                if (Constants.checkInternet(MainActivity.this)) {
                    Constants.showWalletPopup(MainActivity.this);
                } else {
                    //Constants.showNoInternetDialog(MainActivity.this);
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
            position = 6;
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

    public void onBackPressed() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(MainActivity.this);
        }
        databaseHelper.closeDatabase();
        moveTaskToBack(true);
    }

    // display error in dialog
    private void displayErrorDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!((Activity) context).isFinishing()) {
                    new AlertDialog.Builder(MainActivity.this)
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
                parseServiceResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };
    // [END]

    public void parseServiceResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("msg").equals("List generated")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_data = Constants.decryptAPI(MainActivity.this, encrypted_response);
                Dlog.d("Decrypted Service: " + decrypted_data);
                JSONObject array = new JSONObject(decrypted_data);
                JSONArray array1 = array.getJSONArray("services");
                if (array1.length() > 0) {
                    serviceModelArrayList = new ArrayList<ServicesModel>();
                    for (int i = 0; i < array1.length(); i++) {
                        JSONObject object = array1.getJSONObject(i);
                        servicesModel = new ServicesModel();
                        servicesModel.setId(object.getString("id"));
                        serviceModelArrayList.add(servicesModel);
                    }

                    if (serviceModelArrayList.size() > 0) {
                        for (int i = 0; i < serviceModelArrayList.size(); i++) {
                            if (serviceModelArrayList.get(i).getId().equals(Constants.mobile_prepaid_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_MOB_PREPAID_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.ic_mobile);
                            } else if (serviceModelArrayList.get(i).getId().equals(Constants.dth_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_DTH_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.dth);
                            } else if (serviceModelArrayList.get(i).getId().equals(Constants.electricity_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_ELECTRICITY_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.ic_electricity2);
                            } else if (serviceModelArrayList.get(i).getId().equals(Constants.gas_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_GAS_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.gas);
                            } else if (serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_DMT_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.ic_dmt);
                            } else if (serviceModelArrayList.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_MOB_POSTPAID_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.tap);
                            } else if (serviceModelArrayList.get(i).getId().equals(Constants.water_id)) {
                                serviceModelArrayList.get(i).setName(Constants.KEY_WATER_TEXT);
                                serviceModelArrayList.get(i).setIcon(R.drawable.tap);
                            }
                        }
                        servicesModel = new ServicesModel();
                        servicesModel.setId(Constants.more_id);
                        servicesModel.setName(Constants.KEY_MORE_TEXT);
                        servicesModel.setIcon(R.drawable.ic_more);
                        serviceModelArrayList.add(servicesModel);
                    }

                    Constants.dmt_flag = new String[serviceModelArrayList.size()];
                    for (int i = 0; i < serviceModelArrayList.size(); i++) {
                        if (serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                            Constants.dmt_flag[i] = "true";
                            Constants.dmt_title = serviceModelArrayList.get(i).getName();
                        } else {
                            Constants.dmt_flag[i] = "false";
                        }
                    }

                    Constants.elctricity_flag = new String[serviceModelArrayList.size()];
                    for (int i = 0; i < serviceModelArrayList.size(); i++) {
                        if (serviceModelArrayList.get(i).getId().equals(Constants.electricity_id)) {
                            Constants.elctricity_flag[i] = "true";
                            Constants.electricity_title = serviceModelArrayList.get(i).getName();
                        } else {
                            Constants.elctricity_flag[i] = "false";
                        }
                    }

                    Constants.gas_flag = new String[serviceModelArrayList.size()];
                    for (int i = 0; i < serviceModelArrayList.size(); i++) {
                        if (serviceModelArrayList.get(i).getId().equals(Constants.gas_id)) {
                            Constants.gas_flag[i] = "true";
                            Constants.gas_title = serviceModelArrayList.get(i).getName();
                        } else {
                            Constants.gas_flag[i] = "false";
                        }
                    }

                    Constants.mobile_postpaid_flag = new String[serviceModelArrayList.size()];
                    for (int i = 0; i < serviceModelArrayList.size(); i++) {
                        if (serviceModelArrayList.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                            Constants.mobile_postpaid_flag[i] = "true";
                            Constants.mobile_postpaid_title = serviceModelArrayList.get(i).getName();
                        } else {
                            Constants.mobile_postpaid_flag[i] = "false";
                        }
                    }

                    SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(serviceModelArrayList);
                    prefsEditor.putString(Constants.KEY_SERVICE_DATA, json);
                    prefsEditor.commit();

                    Constants.serviceModelArrayList = serviceModelArrayList;
                    servicesAdapter = new ServicesAdapter(MainActivity.this, serviceModelArrayList);
                    recyclerView.setAdapter(servicesAdapter);

                } else {
                }

            } else {
                Dlog.d("Balance response not found. Status = " + jsonObject.getString("status"));
            }
        } catch (JSONException e) {
            Dlog.d("Error : " + e.getMessage());
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
                Dlog.d("Receiver call ACTION_REFRESH_MAINACTIVITY");
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
}