package specificstep.com.GlobalClasses;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import specificstep.com.Activities.Main2Activity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.NotificationUtil;

/**
 * Created by ubuntu on 6/2/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    DatabaseHelper databaseHelper;
    private static int count = 0;
    String channelId = "channel-01";
    String channelName = "Channel Name";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on", ""+isScreenOn);
        if(isScreenOn==false)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");
            wl_cpu.acquire(10000);
        }

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        databaseHelper = new DatabaseHelper(MyFirebaseMessagingService.this);
        //Calling method to generate notification
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Intent intent = new Intent(this, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody()).setAutoCancel(true).setContentIntent(pendingIntent);;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
        sendNotification(remoteMessage.getNotification().getTitle().toString() ,remoteMessage.getNotification().getBody(), simpleDateFormat.format(cal.getTime()));
        /*System.out.println("Message received");
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.d("Notification", "title : " + "Recharge Notification" + "Message : " + remoteMessage.getNotification().getBody() + "Receive DateTime : " + simpleDateFormat.format(cal.getTime()));
            new NotificationUtil(MyFirebaseMessagingService.this)
                    .sendNotification("Recharge Notification",
                            remoteMessage.getNotification().getBody(), simpleDateFormat.format(cal.getTime()));
            System.out.println("Message added");
        } catch (Exception e) {
            Dlog.d("MyFirebaseMessagingService Notification Error: " + e.toString());
        }*/
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String title, String messageBody, String date_time) {
        Log.d("Notification", "title : " + "Recharge Notification" + "Message : " + messageBody + "Receive DateTime : " + date_time);
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            /*Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mChannel.setDescription(messageBody);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotifyManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
        mBuilder.setContentTitle(title)
                .setContentText(messageBody)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(Color.parseColor("#FFD600"))
                .setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        mNotifyManager.notify(count, mBuilder.build());
        count++;*/
            new NotificationUtil(this)
                    .sendNotification("Recharge Notification",
                            messageBody, simpleDateFormat.format(cal.getTime()));
        } catch (Exception e) {
            Dlog.d("MyFirebaseMessagingService Notification Error: " + e.toString());
        }
    }

//    SharedPreferences sharedPreferences;
//    Constants constants;

//    @Override
//    public void onNewToken(String token) {
//
//        constants = new Constants();
//        sharedPreferences = getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
//
//        sharedPreferences.edit().putString(constants.TOKEN, token).commit();
//    }

}
