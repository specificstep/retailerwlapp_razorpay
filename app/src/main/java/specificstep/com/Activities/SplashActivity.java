package specificstep.com.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Database.NotificationTable;
import specificstep.com.GlobalClasses.Config;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.NotificationUtils;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.GlobalClasses.VersionChecker;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.NotificationModel;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.NotificationUtil;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 4/1/17.
 */

public class SplashActivity extends Activity {

    private Context context;
    DatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;
    Constants constants;
    String is_app_installed_form_play_store;
    String title = "";
    private final int ERROR = 2, SUCCESS_NOTIFICATION_CLICK = 1;
    private ArrayList<User> userArrayList;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.acticity_splash);
        try {

            Dlog.d("Package name: " + getApplicationContext().getPackageName());
            Constants.APP_PACKAGE_NAME = getApplicationContext().getPackageName();

            try {
                //set background as per package name
                Constants.chaneBackground(SplashActivity.this, (LinearLayout) findViewById(R.id.lnrSplash));
                //set icon as per package name
                Constants.chaneIcon(SplashActivity.this, (ImageView) findViewById(R.id.imageView));

            } catch (Exception e) {
                Dlog.d("Splash crash");
            }

            context = SplashActivity.this;
            databaseHelper = new DatabaseHelper(SplashActivity.this);
            userArrayList = databaseHelper.getUserDetail();
            constants = new Constants();
            sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);

            /* [START] - Manage create short cut function */
            is_app_installed_form_play_store = sharedPreferences.getString
                    (constants.isAppInstallFromPlayStore, constants.isAppInstallFromPlayStore_No);

            if (is_app_installed_form_play_store.equals("No")) {
                Dlog.d("Splash : " + "App install from play store");
            } else {
                sharedPreferences.edit().putString(constants.isAppInstallFromPlayStore, "Yes").commit();
                Dlog.d("Splash : " + "App install from mobile");
                addShortcut(SplashActivity.this);
            }

            FirebaseInstanceId.getInstance().getToken();
            String token = FirebaseInstanceId.getInstance().getToken();
            System.out.println("firebase_token: " + token);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //timeOutSuccess();
                    /*if(Constants.checkInternet(SplashActivity.this)) {
                        getCurrentVersion();
                    } else {
                        timeOutSuccess();
                    }*/


                    if(getIntent().hasExtra("message")) {
                        String dataMsg = getIntent().getStringExtra("message");
                        String dataTitle = getIntent().getStringExtra("title");
                        String dataNotificationId = getIntent().getStringExtra("notificationId");
                        System.out.println("Message FCM: " + dataMsg);

                        NotificationModel model = new NotificationModel();
                        model.title = dataTitle;
                        model.message = dataMsg;
                        model.receiveDateTime = DateTime.getCurrentDateTime();
                        model.saveDateTime = DateTime.getCurrentDateTime();
                        model.readFlag = "0";
                        model.readDateTime = "";
                        Log.d("Notification", "title : " + model.title + "Message : " + model.message);
                        new NotificationTable(SplashActivity.this).addNotificationData(model);

                        makeNotificationClickCall(dataNotificationId);

                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("position", 8);
                        startActivity(intent);
                        finish();

                    } else {
                        getCurrentVersion();
                    }
                }
            }, 1000);

            // [END]



        } catch (Exception unused) {
            System.out.println("Splash Receive: " + unused.toString());
            //unused.printStackTrace();
        }

    }

    public void makeNotificationClickCall(String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.notification_click;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "is_read",
                            "notification_id"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION,
                            "1",
                            id
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    Dlog.d("Alert response: " + response);
                    myHandler.obtainMessage(SUCCESS_NOTIFICATION_CLICK, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_NOTIFICATION_CLICK) {
                //parseSuccessBannerResponse(msg.obj.toString());
                System.out.println("Notification Send success");
            }
        }
    };

    // display error in dialog
    private void displayErrorDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!((Activity) context).isFinishing()) {
                    new android.app.AlertDialog.Builder(SplashActivity.this)
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*
    Method : addShortcut
    (for adding shortcut on home page while app is installing first time)
    */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void addShortcut(Context context) {
        SharedPreferences prefs = null;
        prefs = getSharedPreferences("specificstep.com.rechargeengine", MODE_PRIVATE);

        if (prefs.getBoolean("firstrun", true)) {

            // Create explicit intent which will be used to call Our application
            // when some one clicked on short cut
            Intent shortcutIntent = new Intent(getApplicationContext(),
                    SplashActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent intent = new Intent();

            // Create Implicit intent and assign Shortcut Application Name, Icon
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, Constants.changeAppName(SplashActivity.this));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                            Constants.chaneIcon(SplashActivity.this)));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            // don't add duplicate shortcut
            intent.putExtra("duplicate", false);
            getApplicationContext().sendBroadcast(intent);

            sendNotification();
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    /*
        Method : sendNotification
        (sends notification while app is successfully installed)
        */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification() {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        new NotificationUtil(SplashActivity.this).sendNotification(Constants.changeAppName(SplashActivity.this), "App installed successfully", simpleDateFormat.format(cal.getTime()));
    }

    String currentVersion, storeVersion;

    //Dialog dialog;
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
                timeOutSuccess();
            } else {
                sharedPreferences.edit().putString(Constants.STOREAPPVERSION, currentVersion).commit();
                databaseHelper.truncateUpdateData();
                timeOutSuccess();
            }

        } else {
            sharedPreferences.edit().putString(constants.STOREAPPVERSION, currentVersion).commit();
            databaseHelper.truncateUpdateData();
            timeOutSuccess();
        }

        /*try {
            VersionChecker versionChecker = new VersionChecker();
            //new VersionChecker().execute();
            latestVersion = versionChecker.execute().get();
            if(!latestVersion.equals("null")) {
                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    if (!isFinishing()) { //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error
                        showUpdateDialog();
                        //}
                    } else {
                        timeOutSuccess();
                    }
                } else
                    timeOutSuccess();
            } else {
                timeOutSuccess();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            timeOutSuccess();
        } catch (ExecutionException e) {
            e.printStackTrace();
            timeOutSuccess();
        }*/

    }

    public void getCheckVersion() {

        PackageManager pm = getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo = pm.getPackageInfo(getPackageName(), 0);
            currentVersion = String.valueOf(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (sharedPreferences.contains(Constants.STOREAPPVERSION)) {

            storeVersion = sharedPreferences.getString(Constants.STOREAPPVERSION, "");
            if (storeVersion.equals(currentVersion)) {
                timeOutSuccess();
            } else {
                sharedPreferences.edit().putString(Constants.STOREAPPVERSION, currentVersion).commit();
                databaseHelper.truncateUpdateData();
                timeOutSuccess();
            }

        } else {
            sharedPreferences.edit().putString(constants.STOREAPPVERSION, currentVersion).commit();
            timeOutSuccess();
        }

        String latestVersion = "";
        try {
            VersionChecker versionChecker = new VersionChecker();
            //new VersionChecker().execute();
            latestVersion = versionChecker.execute().get();
            if (!latestVersion.equals("null")) {
                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    if (!isFinishing()) { //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error
                        showUpdateDialog();
                        //}
                    } else {
                        timeOutSuccess();
                    }
                } else
                    timeOutSuccess();
            } else {
                timeOutSuccess();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            timeOutSuccess();
        } catch (ExecutionException e) {
            e.printStackTrace();
            timeOutSuccess();
        }

    }

    public void timeOutSuccess() {
        if (sharedPreferences.getString(constants.VERIFICATION_STATUS, "").equals("1")
                && sharedPreferences.getString(constants.LOGIN_STATUS, "").equals("1")
                || sharedPreferences.getString(constants.LOGIN_STATUS, "") == "1") {
            Intent intent = new Intent(SplashActivity.this, Main2Activity.class);
            startActivity(intent);
        } else if (sharedPreferences.getString(constants.LOGIN_STATUS, "").equals("0")
                || sharedPreferences.getString(constants.LOGIN_STATUS, "") == "0") {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(SplashActivity.this, RegistrationActivity.class);
            startActivity(intent);
        }
    }

    private void showUpdateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setTitle("A New Update is Available");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=" + Constants.APP_PACKAGE_NAME)));
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false).show();
    }

}

