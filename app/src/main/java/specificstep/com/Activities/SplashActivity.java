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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Config;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.NotificationUtils;
import specificstep.com.GlobalClasses.VersionChecker;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.NotificationUtil;

/**
 * Created by ubuntu on 4/1/17.
 */

public class SplashActivity extends Activity {

    private Context context;
    DatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;
    Constants constants;
    String is_app_installed_form_play_store;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    String title = "";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.acticity_splash);

        try {
            FirebaseInstanceId.getInstance().getToken();
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d("firebase_token", token);

            if (getIntent().hasExtra("title")) {
                String title = getIntent().getStringExtra("title");
                Log.d("firebase_title", title);
            }
        } catch (Exception unused) {
            unused.printStackTrace();
        }
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
        constants = new Constants();
        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);

        /* [START] - Manage create short cut function */
        is_app_installed_form_play_store = sharedPreferences.getString
                (constants.isAppInstallFromPlayStore, constants.isAppInstallFromPlayStore_No);
        // [END]
//        try {
//            if (Constants.checkInternet(SplashActivity.this)) {
//                getCheckVersion();
//            } else {
//                timeOutSuccess();
//            }
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        }

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
                getCurrentVersion();
            }
        }, 1000);

        if (is_app_installed_form_play_store.equals("No")) {
            Dlog.d("Splash : " + "App install from play store");
        } else {
            sharedPreferences.edit().putString(constants.isAppInstallFromPlayStore, "Yes").commit();
            Dlog.d("Splash : " + "App install from mobile");
            addShortcut(SplashActivity.this);
        }
        // [END]


    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
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

