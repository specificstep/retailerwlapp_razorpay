package specificstep.com.GlobalClasses;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import specificstep.com.Adapters.WalletListAdapter;
import specificstep.com.BuildConfig;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Models.Default;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;

/**
 * Created by ubuntu on 13/1/17.
 */

public class Constants {

    public static String APP_VERSION = "1.4";
    //global variable
    public static boolean isDialogOpen = false;

    public static String SHAREEDPREFERENCE = "SHAREEDPREFERENCE";
    public static String VERIFICATION_STATUS = "VERIFICATION_STATUS";
    public static String LOGOUT = "false";
    public static String MOBILENUMBER = "MOBILENUMBER";
    public static String AMOUNT = "AMOUNT";
    public static String isClicked = "false";
    public static String RECHARGEFROM = "RECHARGEFROM";

    public static String LOGIN_STATUS = "LOGIN_STATUS";

    public static String STOREAPPVERSION = "STOREAPPVERSION";

    /* [START] - Create short cut check variables */
    public static String isAppInstallFromPlayStore_No = "No";
    public static String isAppInstallFromPlayStore = "isAppInstallFromPlayStore";
    // [END]

    public static String SELECTED_TAB = "SELECTED_TAB";
    public static String TOKEN = "TOKEN";

    // General message
    public static String SENDER_ID = "SPECIF";

    public static String TOTAL_UNREAD_NOTIFICATION = "0";
    public static int TOTAL_NOTIFICATION = 0;

    public static String DMT_MOBILE = "DMT_MOBILE";

    //services names
    public static String KEY_MOB_POSTPAID_TEXT = "Mobile Postpaid";
    public static String KEY_GAS_TEXT = "Gas";
    public static String KEY_WATER_TEXT = "water";
    public static String KEY_DMT_TEXT = "DMT";
    public static String KEY_ELECTRICITY_TEXT = "Electricity";
    public static String KEY_DTH_TEXT = "DTH";
    public static String KEY_MOB_PREPAID_TEXT = "Mobile Prepaid";
    public static String KEY_MORE_TEXT = "More";

    //services ids
    public static String water_id = "11";
    public static String dmt_id = "21";
    public static String gas_id = "6";
    public static String online_payment_id="30";
    public static String electricity_id = "3";
    public static String mobile_prepaid_id = "1";
    public static String dth_id = "2";
    public static String mobile_postpaid_id = "22";
    public static String more_id = "0";

    //wallet arrays
    public static ArrayList<WalletsModel> walletsModelList = new ArrayList<WalletsModel>();
    public static ArrayList<String> walletsList = new ArrayList<String>();

    //service data array
    public static ArrayList<ServicesModel> serviceModelArrayList = new ArrayList<ServicesModel>();
    public static final String KEY_SERVICE_DATA = "service_data";

    //electricity recharge
    public static String[] elctricity_flag;
    public static String electricity_title;

    //gas recharge
    public static String[] gas_flag;
    public static String gas_title;

    //mobile prepaid recharge
    public static String[] mobile_prepaid_flag;
    public static String mobile_prepaid_title;

    //dth recharge
    public static String[] dth_flag;
    public static String dth_title;

    //mobile postpaid recharge
    public static String[] mobile_postpaid_flag;
    public static String mobile_postpaid_title;

    //water recharge
    public static String[] water_flag;
    public static String water_title;

    //dmt
    public static String[] dmt_flag;
    public static String dmt_title;

    public static String Lati = "";
    public static String Long = "";

    //electricity contact
    public static String singleConstact = "";

    public static AlertDialog dialog;

    // public final static String DB_Path = "/data/specificstep.com.metroenterprise/databases/RechargeEngine";
    // public final static String DB_Path = "/data/specificstep.com.metroenterprise/databases/RechargeEngine";

    // Notification key
    public static final String KEY_SCREEN_NO = "screen_no";
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    public final String KEY_RECHARGE_CURR_POS = "recharge_curr_pos";

    public final String KEY_REQUIRE_UPDATE = "require_update";

    // Preference key
    public final String PREF_NAME = "setting_data";
    public final String PREF_UPDATE_DATE = "update_date";
    public final String PREF_UPDATE_TIME = "update_time";
    public final String PREF_IS_CREDIT_STATUS = "is_credit_status";
    public final String PREF_NAME_STATUS = "name_status";
    public final String PREF_IS_CIRCLE_VISIBILITY = "circle_visibility";

    public static boolean IS_RECEIVE_MESSAGE = false;

    //for payment unique id
    public final String PREF_PAYMENT_UNIQUE_ID = "payment_unique_id";

    // Plan parameter key
    public static String KEY_OPERATOR = "operator";
    public static String KEY_COMPANY_ID = "company_id";
    public static String KEY_STATE = "state";
    public static String KEY_CIRCLE_ID = "circle_id";

    // On activity for result argument
    public static String KEY_PLAN_RS = "plan_rs";
    public static String KEY_PRODUCT_ID = "product_id";
    public static String KEY_PRODUCT_NAME = "product_name";

    public static String APP_PACKAGE_NAME = "";
    public static String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    //set background as per package name
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void chaneBackground(Activity activity, LinearLayout lnr) {
        Constants.APP_PACKAGE_NAME = activity.getPackageName();
        lnr.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimaryDark));
        lnr.getBackground().setAlpha(200);
    }

    //set app image in all imageview as per package name
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void chaneIcon(Activity activity, ImageView img) {
        Constants.APP_PACKAGE_NAME = activity.getPackageName();
        if (Constants.APP_PACKAGE_NAME.equals(PACKAGE_NAME)) {
            img.setBackground(activity.getResources().getDrawable(R.drawable.ic_launcher_new));
        }
    }

    //Change HomeActivity slider app image as per package name
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void chaneIcon(Activity activity, CircleImageView img) {
        Constants.APP_PACKAGE_NAME = activity.getPackageName();
        if (Constants.APP_PACKAGE_NAME.equals(PACKAGE_NAME)) {
            img.setBackground(activity.getResources().getDrawable(R.mipmap.ic_launcher));
        }
    }

    //Change App Image in all java files as per package name
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static int chaneIcon(Activity activity) {
        Constants.APP_PACKAGE_NAME = activity.getPackageName();
        int image = 0;
        if (Constants.APP_PACKAGE_NAME.equals(PACKAGE_NAME)) {
            image = activity.getResources().getIdentifier("ic_launcher", "mipmap", APP_PACKAGE_NAME);
        }
        return image;
    }

    //Change app name as per package name
    public static String changeAppName(Activity activity) {
        Constants.APP_PACKAGE_NAME = activity.getPackageName();
        String app_name = "";
        if (Constants.APP_PACKAGE_NAME.equals(PACKAGE_NAME)) {
            app_name = activity.getResources().getString(R.string.app_name);
        }
        return app_name;
    }

    public static String commonDateFormate(String time, String inputPattern, String outputPattern) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String addRsSymbol(Activity context, String amount) {
        String cAmount = amount;
        // Decimal format
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        // Add RS symbol in credit and debit amount
        try {
            if (!TextUtils.equals(cAmount, "0")) {
                cAmount = context.getResources().getString(R.string.currency_format, String.valueOf(format.parse(cAmount).floatValue()));
            }
        } catch (Exception ex) {
            Log.e("Cash Adapter", "Error in decimal number");
            Log.e("Cash Adapter", "Error : " + ex.getMessage());
            ex.printStackTrace();
            cAmount = context.getResources().getString(R.string.currency_format, amount);
        }
        return cAmount;
    }

    public static String formatBigDecimalToString(BigDecimal bigDecimal) {
        if (bigDecimal == null) return "0";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.##");
        format.setDecimalFormatSymbols(symbols);
        return format.format(bigDecimal.floatValue());
    }

    public static boolean checkInternet(Activity activity) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            System.out.println("Network available.");
            if (getInetAddressByName("www.google.com")) {
                System.out.println("google find successful.");
                //if(getInetAddressByName("portal.specificstep.com")) {
                if (getInetAddressByName("www.maatarinimobile.co.in")) {
                    connected = true;
                    System.out.println("URL find successful.");
                } else {
                    connected = false;
                    System.out.println("Please check your mobile data or wifi connection.");
                    showErrorInternetDialog(activity, "Please check your mobile data or wifi connection and try again later.");
                }
            } else {
                connected = false;
                System.out.println("Please check your mobile data connection.");
                showErrorInternetDialog(activity, "Please check your mobile data connection and try again later.");
            }
        } else {
            System.out.println("Network not available.");
            connected = false;
            showNoInternetDialog(activity);
        }
        return connected;
    }

    public static void showErrorInternetDialog(final Activity context, String msg) {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle("Error!")
                    .setMessage(msg)
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                            ((Activity) context).startActivityForResult(settingsIntent, 9003);
                        }
                    })
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public static boolean getInetAddressByName(String name) {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    InetAddress address = InetAddress.getByName(params[0]);
                    return !address.equals("");
                } catch (UnknownHostException e) {
                    return false;
                }
            }
        };
        try {
            return task.execute(name).get();
        } catch (InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static void showWalletPopup(Activity activity) {

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_wallet_list);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AnimationLeftRight;
        //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP | Gravity.RIGHT;
        window.setAttributes(lp);
        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recycle_wallet);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        RecyclerView.Adapter walletListAdapter = new WalletListAdapter(activity, walletsModelList);
        recyclerView.setAdapter(walletListAdapter);

        dialog.show();

    }

    public static void showNoInternetDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("No Internet")
                .setMessage("Please check your internet connection.")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
                        ((Activity) context).startActivityForResult(settingsIntent, 9003);
                    }
                })
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /*Method : decryptAPI
           Decrypt response of webservice*/
    public static String decryptAPI(Context context, String response) {
        ArrayList<Default> defaultArrayList;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        ArrayList<User> userArrayList = databaseHelper.getUserDetail();
        defaultArrayList = databaseHelper.getDefaultSettings();
        String user_id = defaultArrayList.get(0).getUser_id();
        MCrypt mCrypt = new MCrypt(user_id, userArrayList.get(0).getDevice_id());
        String decrypted_response = null;
        byte[] decrypted_bytes = Base64.decode(response, Base64.DEFAULT);
        try {
            decrypted_response = new String(mCrypt.decrypt(mCrypt.bytesToHex(decrypted_bytes)), "UTF-8");
        } catch (Exception e) {
            Dlog.d("DecryptAPI : " + "Error 7 : " + e.getMessage());
            e.printStackTrace();
        }
        return decrypted_response;
    }

    public static void hideKeyboard(Context context, View view) {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

}
