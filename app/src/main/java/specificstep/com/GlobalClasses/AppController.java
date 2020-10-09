package specificstep.com.GlobalClasses;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;

import io.fabric.sdk.android.Fabric;

/**
 * Created by admin1 on 21/3/16.
 */

public class AppController extends Application {

    public static AppController instance;
    public static boolean DEBUG = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        this.DEBUG = isDebuggable(this);
        instance = this;

        /* report crash if any issues with app */
        // Fabric.with(this, new Crashlytics());
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static AppController getInstance() {
        return instance;
    }

    /*get Debug Mode*/
    private boolean isDebuggable(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appinfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            /* debuggable variable will remain false */
        }

        return debuggable;
    }

}
