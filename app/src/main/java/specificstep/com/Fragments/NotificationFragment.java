package specificstep.com.Fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Adapters.NotificationAdapter;
import specificstep.com.Database.NotificationTable;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.Models.NotificationModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.NotificationUtil;

/**
 * Created by ubuntu on 14/3/17.
 */

public class NotificationFragment extends Fragment {

    private View view;
    private ListView lstNotification;
    private ImageView imgNoData;
    // Notification Model List for store notification message data
    private ArrayList<NotificationModel> notificationModels;
    // Adapter of notification message list
    private NotificationAdapter notificationAdapter;
    private TransparentProgressDialog transparentProgressDialog;
    private static final int SUCCESS_NOTIFICATION = 1;

    // Notification receiver
    private Context context;
    private NotificationUtil notificationUtil;
    private BroadcastReceiver notificationReceiver = null;
    public static final String ACTION_REFRESH_NOTIFICATION = "specificstep.com.metroenterprise.REFRESH_NOTIFICATION";
    private MenuItem globalMenuItem;

    private Context getContextInstance() {
        if (context == null) {
            context = NotificationFragment.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification, null);
        /*getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mainActivity().getSupportActionBar().setTitle("Notification");
        mainActivity().setNotificationCounter();

        initControls();
        setListView();
        setListener();
        getBundleData();*/

        return view;
    }

    private void getBundleData() {
        /* [START] - Display notification dialog if user press on notification */
        Bundle bundle = NotificationFragment.this.getArguments();
        String notificationId = "-1";
        if (bundle != null) {
            if (bundle.getString(Constants.KEY_NOTIFICATION_ID) != null) {
                notificationId = bundle.getString(Constants.KEY_NOTIFICATION_ID, "");
                LogMessage.d("Notification Id : " + notificationId);
                ArrayList<NotificationModel> notificationModels = new NotificationTable(NotificationFragment.this.getActivity()).getNotificationData(notificationId);
                if (notificationModels.size() > 0) {
                    NotificationModel model = notificationModels.get(0);
                    showNotificationDialog(model.id, model.title, model.message, model.receiveDateTime);
                }
            }
        }
        // [END]
    }

    private void initControls() {
        transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataNotification);
        lstNotification = (ListView) view.findViewById(R.id.lst_Adapter_Notification);
        notificationUtil = new NotificationUtil(getContextInstance());
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
                NotificationFragment.this.getActivity().registerReceiver(notificationReceiver, intentFilter);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            LogMessage.e("Error in register receiver");
            LogMessage.e("Error : " + ex.getMessage());
        }
        // [END]
    }

    private void unregisterNotificationReceiver() {
        try {
            if (notificationReceiver != null) {
                NotificationFragment.this.getActivity().unregisterReceiver(notificationReceiver);
                notificationReceiver = null;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            LogMessage.e("Error in register receiver");
            LogMessage.e("Error : " + ex.getMessage());
        }
    }

    private void setListView() {
        // Display progress dialog while loading notification data
        showProgressDialog();
        // Select notification data from database and store in array list
        notificationModels = new NotificationTable(getContextInstance()).getNotificationData_OrderBy();
        // check notification data is available or not
        if (notificationModels.size() > 0) {
            notificationAdapter = new NotificationAdapter(getContextInstance(), notificationModels);
            lstNotification.setAdapter(notificationAdapter);
            lstNotification.setVisibility(View.VISIBLE);
            imgNoData.setVisibility(View.GONE);
        } else {
            lstNotification.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
        }
        mainActivity().setNotificationCounter();
        myHandler.obtainMessage(SUCCESS_NOTIFICATION).sendToTarget();
    }

    private void setListener() {
        lstNotification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NotificationModel model = (NotificationModel) notificationAdapter.getItem(position);
                LogMessage.d("Selected Item Id : " + model.id);
                showNotificationDialog(model.id, model.title, model.message, model.receiveDateTime);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_notification, menu);
        Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        globalMenuItem = item;
        if (id == R.id.action_notification) {
            int cnt = new NotificationTable(getContextInstance()).getAllNotificationRecordCounter();
            if(cnt>0) {
                showClearConfirmDialog("Are you sure you want to clear all notifications?");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterNotificationReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerNotificationReceiver();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mainActivity().getSupportActionBar().setTitle("Notification");
        mainActivity().setNotificationCounter();

        initControls();
        setListView();
        setListener();
        getBundleData();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    /*Intent intent = new Intent(getActivity(), Main2Activity.class);
                    startActivity(intent);*/
                    if(getFragmentManager().getBackStackEntryCount() == 1) {
                        Intent intent = new Intent(getActivity(), Main2Activity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        getActivity().finish();
                    }
                    return true;
                }
                return false;
            }
        });

    }

    public void showNotificationDialog(final String notificationId, String title, String message, String receiveDateTime) {
        final Dialog dialogNotification = new Dialog(getActivity());
        dialogNotification.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNotification.setContentView(R.layout.dialog_notification);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogNotification.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialogNotification.getWindow().setAttributes(lp);
        dialogNotification.setCancelable(false);

        TextView txtId = (TextView) dialogNotification.findViewById(R.id.txt_Dialog_NotificationId);
        TextView txtTitle = (TextView) dialogNotification.findViewById(R.id.txt_Dialog_NotificationTitle);
        TextView txtMessage = (TextView) dialogNotification.findViewById(R.id.txt_Dialog_NotificationMessage);
        TextView txtDateTime = (TextView) dialogNotification.findViewById(R.id.txt_Dialog_Notification_DateTime);
        TextView txtReadDateTime = (TextView) dialogNotification.findViewById(R.id.txt_Dialog_Notification_ReadDateTime);
        Button btn_ok = (Button) dialogNotification.findViewById(R.id.btn_Dialog_NotificationOk);

        String formattedMessage = "";
        String originalMessage = message;
        String notificationDate = "";
        try {
            // Check message contains mobile number or not
            if (originalMessage.contains("-")) {
                formattedMessage = /*"Number : " + */originalMessage.substring(0, originalMessage.indexOf("-")) + "\n";
                originalMessage = originalMessage.substring(originalMessage.indexOf("-") + 1, originalMessage.length());
                // Check message contains comma separated value or not
                if (originalMessage.contains(",")) {
                    // Convert comma separated value in list
                    List<String> notificationMessageItems = Arrays.asList(originalMessage.split("\\s*,\\s*"));
                    // check list contains all value or not
                    if (notificationMessageItems.size() == 5) {
                        // get all value from list
                        formattedMessage += "Transaction Id : " + notificationMessageItems.get(3) + "\n";
                        formattedMessage += "Status : " + notificationMessageItems.get(0) + "\n";
                        formattedMessage += "Company : " + notificationMessageItems.get(1) + "\n";
                        formattedMessage += "Product : " + notificationMessageItems.get(2);
                        // formattedMessage += "Date Time : " + notificationMessageItems.get(4);
                        notificationDate = notificationMessageItems.get(4);
                    } else {
                        formattedMessage += originalMessage;
                    }
                } else {
                    formattedMessage += originalMessage;
                }
            } else {
                formattedMessage = originalMessage;
            }
        }
        catch (Exception ex) {
            LogMessage.d("Error while format notification message");
            LogMessage.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            formattedMessage = originalMessage;
            notificationDate = "";
        }
        // [END]

        txtId.setText(notificationId);
        txtTitle.setText(title);
        txtMessage.setText(formattedMessage);
        if (TextUtils.isEmpty(notificationDate))
            txtDateTime.setText("Date Time : " + Constants.commonDateFormate(receiveDateTime,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy hh:mm aa"));
        else
            txtDateTime.setText("Date Time : " + Constants.commonDateFormate(notificationDate,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy hh:mm aa"));
        txtId.setVisibility(View.GONE);

         //[START] - Display notification read date
        final ArrayList<NotificationModel> models = new NotificationTable(getContextInstance()).getNotificationData(notificationId);
        if(models != null && models.size() != 0) {
            if (TextUtils.equals(models.get(0).readFlag, "0")) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                txtReadDateTime.setText("Read : " + Constants.commonDateFormate(simpleDateFormat.format(cal.getTime()), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));
            } else {
                txtReadDateTime.setText("Read : " + Constants.commonDateFormate(models.get(0).readDateTime, "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));
            }
        }
        // [END]

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNotification.dismiss();
                if(models != null && models.size() != 0) {
                    if (TextUtils.equals(models.get(0).readFlag, "0")) {
                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.id = models.get(0).id;
                        notificationModel.receiveDateTime = models.get(0).receiveDateTime;
                        notificationModel.message = models.get(0).message;
                        notificationModel.readFlag = "1";
                        notificationModel.saveDateTime = models.get(0).saveDateTime;
                        notificationModel.title = models.get(0).title;
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        notificationModel.readDateTime = simpleDateFormat.format(cal.getTime());
                        new NotificationTable(getContextInstance()).updateNotification(notificationModel, notificationId);
                        setListView();

                        // [START] - Remove notification
                        int cancelNotificationId = 0;
                        try {
                            cancelNotificationId = Integer.parseInt(notificationModel.id);
                        } catch (Exception ex) {
                            cancelNotificationId = 0;
                        }
                        notificationUtil.cancelNotification(cancelNotificationId);
                        // [END]
                    }
                }
            }
        });

        dialogNotification.show();
    }

    public void showClearConfirmDialog(String msg) {
        final Dialog dialogNotification = new Dialog(getActivity());
        dialogNotification.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNotification.setContentView(R.layout.common_confirm_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogNotification.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialogNotification.getWindow().setAttributes(lp);
        dialogNotification.setCancelable(false);

        TextView txtMsg = (TextView) dialogNotification.findViewById(R.id.txtCommonMsg);
        Button btnOk = (Button) dialogNotification.findViewById(R.id.btnCommonOk);
        Button btnCancel = (Button) dialogNotification.findViewById(R.id.btnCommonCancel);
        txtMsg.setText(msg);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNotification.dismiss();
                try {
                    new NotificationTable(getContextInstance()).clearAllNotification();
                    setListView();
                } catch (Exception e) {
                    Dlog.d(e.toString());
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNotification.dismiss();
            }
        });

        dialogNotification.show();
    }

    // Handler for handle message
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SUCCESS_NOTIFICATION:
                    dismissProgressDialog();
                    break;
            }
        }
    };

    private void updateNotificationList() {
        try {
            int oldTotalNotificationCounter = Constants.TOTAL_NOTIFICATION;
            Constants.TOTAL_NOTIFICATION = new NotificationTable(getContextInstance()).getAllNotificationRecordCounter();
            int newTotalNotificationCounter = Constants.TOTAL_NOTIFICATION;
            if (oldTotalNotificationCounter != newTotalNotificationCounter) {
                setListView();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            LogMessage.d("Notification : " + "Error : " + ex.toString());
        }
    }

    /* [START] - Custom check notification data class */
    private class CheckNotification extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogMessage.d("Receiver action : " + action);
            if (action.equals(ACTION_REFRESH_NOTIFICATION)) {
                LogMessage.i("Receiver call ACTION_REFRESH_NOTIFICATION");
                try {
                    updateNotificationList();
                    setListView();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    LogMessage.e("Error in ACTION_REFRESH_NOTIFICATION");
                    LogMessage.e("Error : " + ex.getMessage());
                }
            }
        }
    }
    // [END]

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
