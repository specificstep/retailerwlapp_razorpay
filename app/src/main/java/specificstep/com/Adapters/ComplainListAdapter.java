package specificstep.com.Adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.Complain;
import specificstep.com.R;

/**
 * Created by ubuntu on 19/1/17.
 */

public class ComplainListAdapter extends BaseAdapter {

    Context context;

    private ArrayList<Complain> complainArrayList;

    private DatabaseHelper databaseHelper;

    private TextView tv_complain_type,tv_company_name,tv_complain_id,tv_amount,tv_mo_no,tv_complain_status,tv_description,tv_date_time;
    private ImageView iv_company;

    LayoutInflater inflater;

    public ComplainListAdapter(Context activity, ArrayList<Complain> rechargeArrayList) {
        context = activity;
        this.complainArrayList = rechargeArrayList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return complainArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return complainArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.adapter_complain, null);

        tv_company_name = (TextView) convertView.findViewById(R.id.tv_company_name_adapter_complain);
        tv_complain_id = (TextView) convertView.findViewById(R.id.tv_complain_id_adapter_complain);
        tv_amount = (TextView) convertView.findViewById(R.id.tv_amount_adapter_complain);
        tv_mo_no = (TextView) convertView.findViewById(R.id.tv_mo_no_adapter_complain);
        tv_complain_status = (TextView) convertView.findViewById(R.id.tv_complain_status_adapter_complain);
        tv_description = (TextView) convertView.findViewById(R.id.tv_description_adapter_complain);
        tv_date_time = (TextView) convertView.findViewById(R.id.tv_date_time_adapter_complain);
        tv_complain_type = (TextView) convertView.findViewById(R.id.tv_complain_type_adapter_complain);
        iv_company = (ImageView) convertView.findViewById(R.id.iv_company_adapter_complain);

        Complain complain = complainArrayList.get(position);

        tv_company_name.setText(complain.getCompnay_name());
        tv_complain_type.setText("Type: " + complain.getComplain_type());
        tv_complain_id.setText("Complain Id: " + complain.getComplain_id());
        tv_amount.setText(Constants.addRsSymbol((Activity) context,complain.getAmount()));
        tv_mo_no.setText(complain.getMo_no());
        tv_complain_status.setText(complain.getComplain_status());
        if(complain.getComplain_status().equalsIgnoreCase("solved")){
            tv_complain_status.setTextColor(context.getResources().getColor(R.color.colorGreen));
        } else if (complain.getComplain_status().equalsIgnoreCase("pending")) {
            tv_complain_status.setTextColor(context.getResources().getColor(R.color.colorPending));
        } else{
            tv_complain_status.setTextColor(context.getResources().getColor(R.color.colorDefault));
        }

        if(complain.getDescription()!=null && complain.getDescription().length()>0){
            tv_description.setText("Desc: " + complain.getDescription());
            tv_description.setVisibility(View.VISIBLE);
        } else{
            tv_description.setText("");
            tv_description.setVisibility(View.GONE);
        }

        tv_date_time.setText("Transaction Date: " + Constants.commonDateFormate(complain.getTrans_date_time(),"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy hh:mm aa"));

        if(complain.getCompnay_name() != null && complain.getCompnay_name().length() > 0){
            int index = complain.getCompnay_name().indexOf("-");
            String str = complain.getCompnay_name().substring(0,index-1);
            String company_logo = databaseHelper.getCompanyLogo(str);
            if(!TextUtils.isEmpty(company_logo)){
                Picasso.with(context).load(company_logo).placeholder(R.drawable.placeholder_icon).into(iv_company);
            } else {
                iv_company.setImageResource(R.drawable.placeholder_icon);
            }
        } else {
            iv_company.setImageResource(R.drawable.placeholder_icon);
        }

        return convertView;
    }
}
