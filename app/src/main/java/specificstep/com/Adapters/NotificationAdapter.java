package specificstep.com.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.NotificationModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;

/**
 * Created by ubuntu on 14/3/17.
 */

public class NotificationAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<NotificationModel> models = null;

    public NotificationAdapter(Context activity, ArrayList<NotificationModel> _models) {
        inflater = LayoutInflater.from(activity.getApplicationContext());
        models = _models;
    }

    private class RowHolder {
        private TextView txtId, txtMessage, txtTitle, txtDateTime, txtReadDateTime;
        private ImageView imgTick;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RowHolder rowHolder;
        if (convertView == null) {
            rowHolder = new RowHolder();
            convertView = inflater.inflate(R.layout.adapter_notification, null);
            rowHolder.txtId = (TextView) convertView.findViewById(R.id.txt_Item_Notification_Id);
            rowHolder.txtMessage = (TextView) convertView.findViewById(R.id.txt_Item_Notification_Message);
            rowHolder.txtTitle = (TextView) convertView.findViewById(R.id.txt_Item_Notification_Title);
            rowHolder.txtDateTime = (TextView) convertView.findViewById(R.id.txt_Item_Notification_DateTime);
            rowHolder.txtReadDateTime = (TextView) convertView.findViewById(R.id.txt_Item_Notification_ReadDateTime);
            rowHolder.imgTick = (ImageView) convertView.findViewById(R.id.img_Notification_MessageRead);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (RowHolder) convertView.getTag();
        }

        rowHolder.txtId.setText(models.get(position).id);
        rowHolder.txtTitle.setText(models.get(position).title);
        if (TextUtils.equals(models.get(position).readFlag, "1")) {
            rowHolder.imgTick.setVisibility(View.VISIBLE);
            rowHolder.txtReadDateTime.setVisibility(View.VISIBLE);
            rowHolder.txtReadDateTime.setText("Read : " + Constants.commonDateFormate(models.get(position).readDateTime,"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy hh:mm aa"));
        } else {
            rowHolder.imgTick.setVisibility(View.GONE);
            rowHolder.txtReadDateTime.setVisibility(View.GONE);
        }

        /* [START] - Display message in proper format
            Simple message = App installed successfully and shortcut created.
         */
        String formattedMessage = "";
        String originalMessage = models.get(position).message;
        String notificationDate = "";
        try {
            // Check message contains mobile number or not
            if (originalMessage.contains("-")) {
                formattedMessage = "Number : " + originalMessage.substring(0, originalMessage.indexOf("-")) + "\n";
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
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            formattedMessage = "Message : " + originalMessage;
            notificationDate = "";
        }
        // [END]

        rowHolder.txtMessage.setText(formattedMessage);
        if (TextUtils.isEmpty(notificationDate))
            rowHolder.txtDateTime.setText("Delivered : " + Constants.commonDateFormate(models.get(position).receiveDateTime,"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy hh:mm aa"));
        else
            rowHolder.txtDateTime.setText("Delivered : " + Constants.commonDateFormate(notificationDate,"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy hh:mm aa"));

        return convertView;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return models.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return models.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public NotificationModel getData(int position) {
        return models.get(position);
    }
}
