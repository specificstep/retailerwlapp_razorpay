package specificstep.com.utility;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import java.util.ArrayList;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.SplashActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Database.NotificationTable;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.NotificationModel;
import specificstep.com.Models.User;

/**
 * Created by ubuntu on 27/3/17.
 */

public class NotificationUtil {
    private Context context;
    private DatabaseHelper databaseHelper;
    private NotificationManager mNotificationManager = null;
    public static final String ACTION_REFRESH_NOTIFICATION = "specificstep.com.metroenterprise.REFRESH_NOTIFICATION";
    public static final String ACTION_REFRESH_MAINACTIVITY = "specificstep.com.metroenterprise.REFRESH_MAINACTIVITY";
    public static final String ACTION_REFRESH_HOMEACTIVITY = "specificstep.com.metroenterprise.REFRESH_HOMEACTIVITY";

    public NotificationUtil(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);

        initNotification();
    }

    private void initNotification() {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void sendNotification(String title, String message, String dateTime) {
        // --- START --- save recharge message.
        NotificationModel model = new NotificationModel();
        model.title = title;
        model.message = message;
        model.receiveDateTime = dateTime;
        model.saveDateTime = DateTime.getCurrentDateTime();
        model.readFlag = "0";
        model.readDateTime = "";
        Log.d("Notification", "title : " + model.title + "Message : " + model.message);
        new NotificationTable(context).addNotificationData(model);
        // --- END ---

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        Activity activity = (Activity) context;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(Constants.chaneIcon(activity))
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(Notification.PRIORITY_MAX);
        mBuilder.setAutoCancel(true);

        // [START] - Get last record id and set in notification
        String lastNotificationId = new NotificationTable(context).getLastNotificationId();
        int lastNotification = 0;
        try {
            lastNotification = Integer.parseInt(lastNotificationId);
        }
        catch (Exception ex) {
            lastNotification = 0;
        }
        // [END]

        ArrayList<NotificationModel> notificationModels = new NotificationTable(context).getLastNotificationData();
        int lastId = -1;
        if (notificationModels.size() > 0) {
            NotificationModel notificationModel = notificationModels.get(0);
            try {
                lastId = Integer.parseInt(notificationModel.id);
                Log.d("Notification", "Last id : " + lastId);
            }
            catch (Exception ex) {
                Log.d("Notification", "Error while parse id");
                ex.printStackTrace();
                lastId = -1;
            }
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        ArrayList<User> userArrayList = new ArrayList<User>();
        userArrayList = databaseHelper.getUserDetail();
        Intent resultIntent = null;
        if (userArrayList.size() > 0) {
            // Creates an explicit intent for an Activity in your app
            resultIntent = new Intent(context, HomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_SCREEN_NO, "6");
            bundle.putString(Constants.KEY_NOTIFICATION_ID, lastId + "");
            resultIntent.putExtras(bundle);
        } else {
            // Creates an explicit intent for an Activity in your app
            resultIntent = new Intent(context, SplashActivity.class);
        }
        stackBuilder.addParentStack(SplashActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());

        Intent intent1 = new Intent(ACTION_REFRESH_NOTIFICATION);
        context.sendBroadcast(intent1);

        Intent intent2 = new Intent(ACTION_REFRESH_MAINACTIVITY);
        context.sendBroadcast(intent2);

        Intent intent3 = new Intent(ACTION_REFRESH_HOMEACTIVITY);
        context.sendBroadcast(intent3);

    }

    public void cancelNotification(int notificationId) {
        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        try {
            // mNotificationManager.cancel(notificationId);
            mNotificationManager.cancel(0);
        }
        catch (Exception ex) {
            Log.e("Notification", "Error in cancel notification");
            Log.e("Notification", "Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
