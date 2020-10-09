package specificstep.com.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.List;

import specificstep.com.Activities.ListDetailActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.CashbookModel;
import specificstep.com.Models.Default;
import specificstep.com.R;

/**
 * Created by ubuntu on 16/3/17.
 */

public class CashbookAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<CashbookModel> models = null;
    private Context context;
    List<Default> userList;
    DatabaseHelper databaseHelper;
    Dialog dialog;
    String userId;

    public CashbookAdapter(Context activity, ArrayList<CashbookModel> _models) {
        context = activity;
        inflater = LayoutInflater.from(activity.getApplicationContext());
        models = _models;
        databaseHelper = new DatabaseHelper(context);
        userList = databaseHelper.getDefaultSettings();
        userId = userList.get(0).getUser_id();
    }

    private class RowHolder {
        private TextView txtPaymentId, txtAmount, txtRemarks, txtDate;
        private ImageView img;
        private LinearLayout lnrCashbook;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RowHolder rowHolder;
        if (convertView == null) {
            rowHolder = new RowHolder();
            convertView = inflater.inflate(R.layout.adapter_cashbook, null);
            rowHolder.txtPaymentId = (TextView) convertView.findViewById(R.id.txt_Adapter_CashBook_PaymentId);
            rowHolder.txtAmount = (TextView) convertView.findViewById(R.id.txt_Adapter_CashBook_Amount);
            rowHolder.txtRemarks = (TextView) convertView.findViewById(R.id.txt_Adapter_CashBook_Remarks);
            rowHolder.img = (ImageView) convertView.findViewById(R.id.img_adapter_cashbook);
            rowHolder.txtDate = (TextView) convertView.findViewById(R.id.txtCashbookDate);
            rowHolder.lnrCashbook = (LinearLayout) convertView.findViewById(R.id.lnrCashbook);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (RowHolder) convertView.getTag();
        }
        rowHolder.txtPaymentId.setText(models.get(position).paymentId.trim());

        String amount = "";
        if(Float.parseFloat(models.get(position).amount)<0) {
            amount = models.get(position).amount.substring(1,models.get(position).amount.length());
        } else {
            amount = models.get(position).amount;
        }

        int frompos = models.get(position).paymentFrom.indexOf("-");
        String from = models.get(position).paymentFrom.substring(0,frompos-1);

        int topos = models.get(position).paymentTo.indexOf("-");
        String to = models.get(position).paymentTo.substring(0,topos-1);

        if(userId.equals(from)) {
            rowHolder.txtAmount.setText("- " + context.getResources().getString(R.string.Rs) + " " + amount);
            rowHolder.txtAmount.setTextColor(context.getResources().getColor(R.color.colorBlack));
            rowHolder.txtRemarks.setText(models.get(position).remarks.trim() + " To " + models.get(position).paymentTo.trim());
            rowHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.send));
        } else if(userId.equals(to)){
            rowHolder.txtAmount.setText("+ " + context.getResources().getString(R.string.Rs) + " " + amount);
            rowHolder.txtAmount.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            rowHolder.txtRemarks.setText(models.get(position).remarks.trim() + " From " + models.get(position).paymentFrom.trim());
            rowHolder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.receive));
        }

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String yesterday = df.format(cal.getTime());

            Calendar tcal = Calendar.getInstance();
            String today = df.format(tcal.getTime());

            if (position == 0) {
                rowHolder.txtDate.setVisibility(View.VISIBLE);
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newDate = null;
                newDate = df1.parse(models.get(0).dateTime);
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate1 = destDf.format(newDate);

                if (!newDate1.equals(today)) {
                    if (yesterday.equals(newDate1)) {
                        rowHolder.txtDate.setText("Yesterday, " + newDate1);
                    } else {
                        rowHolder.txtDate.setText(newDate1 + "");
                    }
                } else {
                    rowHolder.txtDate.setText("Today, " + newDate1);
                }
            } else {
                DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // parse the date string into Date object
                Date date = srcDf.parse(models.get(position).dateTime);
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate = destDf.format(date);
                if(Constants.commonDateFormate(models.get(position).dateTime,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy").equals(Constants.commonDateFormate(models.get(position-1).dateTime,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy"))) {
                    rowHolder.txtDate.setVisibility(View.GONE);
                } else {
                    rowHolder.txtDate.setVisibility(View.VISIBLE);
                    if (!newDate.equals(today)) {
                        if (yesterday.equals(newDate)) {
                            rowHolder.txtDate.setText("Yesterday, " + newDate);
                        } else {
                            rowHolder.txtDate.setText(newDate + "");
                        }
                    } else {
                        rowHolder.txtDate.setText("Today, " + newDate);
                    }
                }
            }

            rowHolder.lnrCashbook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ListDetailActivity.class);
                    intent.putExtra("classdata", (Parcelable) models.get(position));
                    intent.putExtra("from","cashbook");
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

    public CashbookModel getData(int position) {
        return models.get(position);
    }

}
