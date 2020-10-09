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

import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.NavigationModels;
import specificstep.com.R;
import specificstep.com.utility.Dlog;

/**
 * Created by ubuntu on 5/4/17.
 */

public class NavigationDrawerAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<NavigationModels> models = null;

    public NavigationDrawerAdapter(Context activity, ArrayList<NavigationModels> _models) {
        inflater = LayoutInflater.from(activity.getApplicationContext());
        models = _models;
    }

    private class RowHolder {
        private TextView txtTitle, txtMessage;
        private ImageView imgMenuImage;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RowHolder rowHolder;
        if (convertView == null) {
            rowHolder = new RowHolder();
            convertView = inflater.inflate(R.layout.item_menu, null);
            rowHolder.txtTitle = (TextView) convertView.findViewById(R.id.txt_Item_NavigationMenu_Title);
            rowHolder.txtMessage = (TextView) convertView.findViewById(R.id.txt_Item_NavigationMenu_Message);
            rowHolder.imgMenuImage = (ImageView) convertView.findViewById(R.id.img_Item_NavigationViewMenu_MenuImage);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (RowHolder) convertView.getTag();
        }
        rowHolder.txtTitle.setText(models.get(position).getTitle());
        rowHolder.imgMenuImage.setImageResource(models.get(position).getIcon());

        /*if (TextUtils.equals(models.get(position), "Home")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_home);
        } else if (TextUtils.equals(models.get(position), "Recharge")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_menu_recharge);
        } else if (TextUtils.equals(models.get(position), "Transaction Search")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_menu_trans_search);
        } else if (TextUtils.equals(models.get(position), "Recent Transaction")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_recent);
        } else if (TextUtils.equals(models.get(position), "Payment Report")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_payment_report);
        } else if (TextUtils.equals(models.get(position), "Update Data")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_update_button);
        } else if (TextUtils.equals(models.get(position), "Change Password")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_menu_change_password);
        } else if (TextUtils.equals(models.get(position), "Notification")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_notifications_black_24dp);
        } else if (TextUtils.equals(models.get(position), "Parent User")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_parent_user);
        } else if (TextUtils.equals(models.get(position), "Log Out")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_cancel);
        } else if (TextUtils.equals(models.get(position), "Complain Report")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_complain);
        } else if (TextUtils.equals(models.get(position), "Account Ledger")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_chrome_acledger);
        } else if (TextUtils.equals(models.get(position), "DMT")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_dmt);
        } else if (TextUtils.equals(models.get(position), "DMT Transaction List")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_menu_trans_search);
        } else if (TextUtils.equals(models.get(position), "Payment Request")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_payment_on_black_24dp);
        } else if (TextUtils.equals(models.get(position), "Login with Other number")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_person);
        } else if (TextUtils.equals(models.get(position), "Transaction Reports")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_transaction_report);
        } else if (TextUtils.equals(models.get(position), "Share")) {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_share_black_24dp);
        } else {
            rowHolder.imgMenuImage.setImageResource(R.drawable.ic_cancel);
        }*/
        // display notification counter in drawer
        if (TextUtils.equals(models.get(position).getTitle(), "Notification")) {
            int totalUnreadNotification = 0;
            try {
                totalUnreadNotification = Integer.parseInt(Constants.TOTAL_UNREAD_NOTIFICATION);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                Dlog.d("Notification : " + "Error : " + ex.toString());
                totalUnreadNotification = 0;
            }
            if (totalUnreadNotification > 0) {
                rowHolder.txtMessage.setVisibility(View.VISIBLE);
                rowHolder.txtMessage.setText(Constants.TOTAL_UNREAD_NOTIFICATION);
                // reset custom navigation menu
            } else {
                rowHolder.txtMessage.setVisibility(View.GONE);
            }
        } else {
            rowHolder.txtMessage.setVisibility(View.GONE);
        }
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

    public String getData(int position) {
        return models.get(position).getTitle();
    }
}

