package specificstep.com.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import specificstep.com.Models.ParentUserModel;
import specificstep.com.R;

/**
 * Created by ubuntu on 2/5/17.
 */

public class ParentUserAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<ParentUserModel> models = null;
    Context context;

    public ParentUserAdapter(Context activity, ArrayList<ParentUserModel> _models) {
        inflater = LayoutInflater.from(activity.getApplicationContext());
        models = _models;
        this.context = activity;
    }

    private class RowHolder {
        private TextView txtFirmName, txtMobileNumber, txtUserType, txtName;
        private ImageView imgCall;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RowHolder rowHolder;
        if (convertView == null) {
            rowHolder = new RowHolder();
            convertView = inflater.inflate(R.layout.item_parent_user, null);
            rowHolder.txtFirmName = (TextView) convertView.findViewById(R.id.txt_Item_ParentUser_FirmName);
            rowHolder.txtMobileNumber = (TextView) convertView.findViewById(R.id.txt_Item_ParentUser_MobileNumber);
            rowHolder.txtUserType = (TextView) convertView.findViewById(R.id.txt_Item_ParentUser_UserType);
            rowHolder.txtName = (TextView) convertView.findViewById(R.id.txt_Item_ParentUser_Name);
            rowHolder.imgCall = (ImageView) convertView.findViewById(R.id.imgParentUserCall);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (RowHolder) convertView.getTag();
        }

        if(!models.get(position).firmName.equals("null") || models.get(position).firmName != null) {
            String firm_html = "<b>Firm Name : </b>" + models.get(position).firmName;
            rowHolder.txtFirmName.setText(Html.fromHtml(firm_html));
            rowHolder.txtFirmName.setVisibility(View.VISIBLE);
        } else {
            rowHolder.txtFirmName.setVisibility(View.GONE);
        }
        String mobile_html = "<b>Mobile No : </b>" + models.get(position).mobileNumber;
        rowHolder.txtMobileNumber.setText(Html.fromHtml(mobile_html));
        String user_type_html = "<b>User Type : </b>" + models.get(position).userType;
        rowHolder.txtUserType.setText(Html.fromHtml(user_type_html));
        String name_html = "<b>Name : </b>" + models.get(position).name;
        rowHolder.txtName.setText(Html.fromHtml(name_html));

        rowHolder.imgCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = models.get(position).mobileNumber;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + number));
                context.startActivity(intent);
            }
        });

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

    public ParentUserModel getData(int position) {
        return models.get(position);
    }
}