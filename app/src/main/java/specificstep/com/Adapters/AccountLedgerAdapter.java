package specificstep.com.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import specificstep.com.Activities.ListDetailActivity;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.AccountLedgerModel;
import specificstep.com.R;

/**
 * Created by ubuntu on 16/3/17.
 */

public class AccountLedgerAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<AccountLedgerModel> models = null;
    private Context context;

    public AccountLedgerAdapter(Context activity, ArrayList<AccountLedgerModel> _models) {
        context = activity;
        inflater = LayoutInflater.from(activity.getApplicationContext());
        models = _models;
    }

    private class RowHolder {
        private TextView txt_Adapter_acledger_paymentid, txt_Adapter_acledger_amount,
                txt_Adapter_acledger_remark, txt_Adapter_date;
        private ImageView img;
        private LinearLayout lnrAcledger;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RowHolder rowHolder;
        if (convertView == null) {
            rowHolder = new RowHolder();
            convertView = inflater.inflate(R.layout.adapter_account_ledger, null);
            rowHolder.txt_Adapter_acledger_paymentid = (TextView) convertView.findViewById(R.id.txt_Adapter_acledger_paymentid);
            rowHolder.txt_Adapter_acledger_amount = (TextView)convertView.findViewById(R.id.txt_Adapter_acledger_amount);
            rowHolder.txt_Adapter_acledger_remark = (TextView)convertView.findViewById(R.id.txt_Adapter_acledger_remark);
            rowHolder.txt_Adapter_date = (TextView) convertView.findViewById(R.id.txtAccountDate);
            rowHolder.img = (ImageView) convertView.findViewById(R.id.img_adapter_acledger);
            rowHolder.lnrAcledger = (LinearLayout) convertView.findViewById(R.id.lnrAcledger);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (RowHolder) convertView.getTag();
        }

        rowHolder.txt_Adapter_acledger_paymentid.setText(models.get(position).payment_id.trim());

        if(models.get(position).cr_dr.equals("Credit")) {
            rowHolder.txt_Adapter_acledger_amount.setText("+ " + context.getResources().getString(R.string.Rs) + " " + models.get(position).amount);
            rowHolder.txt_Adapter_acledger_amount.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            rowHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.receive));
        } else {
            rowHolder.txt_Adapter_acledger_amount.setText("- " + context.getResources().getString(R.string.Rs) + " " + models.get(position).amount);
            rowHolder.txt_Adapter_acledger_amount.setTextColor(context.getResources().getColor(R.color.colorBlack));
            rowHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.send));
        }

        if(models.get(position).type.equalsIgnoreCase("Recharge")) {
            rowHolder.txt_Adapter_acledger_remark.setText("Recharge To: " + models.get(position).particular.toUpperCase());
        } else if(models.get(position).type.equalsIgnoreCase("Payment")) {
            if(models.get(position).cr_dr.equals("Credit")) {
                rowHolder.txt_Adapter_acledger_remark.setText("Payment Received From: " + models.get(position).particular.toUpperCase());
            } else {
                rowHolder.txt_Adapter_acledger_remark.setText("Payment Sent To: " + models.get(position).particular.toUpperCase());
            }
        } else if(models.get(position).type.equalsIgnoreCase("DMT")) {
            rowHolder.txt_Adapter_acledger_remark.setText(models.get(position).particular.toUpperCase());
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String yesterday = df.format(cal.getTime());

            Calendar tcal = Calendar.getInstance();
            String today = df.format(tcal.getTime());

            if (position == 0) {
                rowHolder.txt_Adapter_date.setVisibility(View.VISIBLE);
                DateFormat df1 = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
                Date newDate = null;
                newDate = df1.parse(models.get(0).created_date);
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate1 = destDf.format(newDate);

                if (!newDate1.equals(today)) {
                    if (yesterday.equals(newDate1)) {
                        rowHolder.txt_Adapter_date.setText("Yesterday, " + newDate1);
                    } else {
                        rowHolder.txt_Adapter_date.setText(newDate1 + "");
                    }
                } else {
                    rowHolder.txt_Adapter_date.setText("Today, " + newDate1);
                }
            } else {
                DateFormat srcDf = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
                // parse the date string into Date object
                Date date = srcDf.parse(models.get(position).created_date);
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate = destDf.format(date);
                if(Constants.commonDateFormate(models.get(position).created_date,"dd-MMM-yy HH:mm:ss aa","dd-MMM-yyyy").equals(Constants.commonDateFormate(models.get(position-1).created_date,"dd-MMM-yy HH:mm:ss aa","dd-MMM-yyyy"))) {
                    rowHolder.txt_Adapter_date.setVisibility(View.GONE);
                } else {
                    rowHolder.txt_Adapter_date.setVisibility(View.VISIBLE);
                    if (!newDate.equals(today)) {
                        if (yesterday.equals(newDate)) {
                            rowHolder.txt_Adapter_date.setText("Yesterday, " + newDate);
                        } else {
                            rowHolder.txt_Adapter_date.setText(newDate + "");
                        }
                    } else {
                        rowHolder.txt_Adapter_date.setText("Today, " + newDate);
                    }
                }
            }

            rowHolder.lnrAcledger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ListDetailActivity.class);
                    intent.putExtra("classdata", (Parcelable) models.get(position));
                    intent.putExtra("from","acledger");
                    intent.putExtra("receipt_type", "");
                    context.startActivity(intent);
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
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

    public AccountLedgerModel getData(int position) {
        return models.get(position);
    }

}
