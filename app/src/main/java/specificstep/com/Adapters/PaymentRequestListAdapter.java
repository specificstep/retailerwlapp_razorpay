package specificstep.com.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import specificstep.com.Activities.ListDetailActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.Color;
import specificstep.com.Models.PaymentRequestListModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;

public class PaymentRequestListAdapter extends RecyclerView.Adapter<PaymentRequestListAdapter.MyViewHolder> {

    ArrayList<PaymentRequestListModel> requestList;
    Activity context;
    ArrayList<Color> colorArrayList;
    String _color_name, color_value;
    DatabaseHelper databaseHelper;

    public PaymentRequestListAdapter(Activity activity, ArrayList<PaymentRequestListModel> rechargeArrayList) {
        context = activity;
        this.requestList = rechargeArrayList;
        databaseHelper = new DatabaseHelper(context);
        colorArrayList = databaseHelper.getAllColors();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout lnrPaymentDetail;
        TextView txtDate, txtRemark, txtBankName, txtStatus, txtAmount;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtDate = (TextView) itemView.findViewById(R.id.txtPaymentRequestListDate);
            txtRemark = (TextView) itemView.findViewById(R.id.txtPaymentRequestListRemark);
            txtBankName = (TextView) itemView.findViewById(R.id.txtPaymentRequestListBankName);
            txtStatus = (TextView) itemView.findViewById(R.id.txtPaymentRequestListTransactionStatus);
            txtAmount = (TextView) itemView.findViewById(R.id.txtPaymentRequestListAmount);
            lnrPaymentDetail = (LinearLayout) itemView.findViewById(R.id.lnrPaymentRequestList);
        }
    }

    @Override
    public PaymentRequestListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_payment_request_list, parent, false);

        PaymentRequestListAdapter.MyViewHolder myViewHolder = new PaymentRequestListAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final PaymentRequestListAdapter.MyViewHolder holder, final int listPosition) {

        if(!requestList.get(listPosition).getAmount().equals("null")) {
            holder.txtAmount.setText(context.getResources().getString(R.string.Rs) + " " + requestList.get(listPosition).getAmount());
            holder.txtAmount.setVisibility(View.VISIBLE);
        } else {
            holder.txtAmount.setVisibility(View.GONE);
        }

        if(!requestList.get(listPosition).getDeposit_bank().equals("null")) {
            holder.txtBankName.setText(requestList.get(listPosition).getDeposit_bank());
            holder.txtBankName.setVisibility(View.VISIBLE);
        } else {
            holder.txtBankName.setVisibility(View.GONE);
        }

        if(!requestList.get(listPosition).getRemark().equals("null")) {
            holder.txtRemark.setText(requestList.get(listPosition).getRemark());
            holder.txtRemark.setVisibility(View.VISIBLE);
        } else {
            holder.txtRemark.setVisibility(View.GONE);
        }

        if(!requestList.get(listPosition).getStatus().equals("null")) {
            holder.txtStatus.setText(requestList.get(listPosition).getStatus());
            holder.txtStatus.setVisibility(View.VISIBLE);
        } else {
            holder.txtStatus.setVisibility(View.GONE);
        }

        /*Set color of recharge status*/
        if (requestList.get(listPosition).getStatus().equalsIgnoreCase("success")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("success")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    holder.txtStatus.setTextColor(android.graphics.Color.parseColor(color_value));
                    holder.txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
        } else if (requestList.get(listPosition).getStatus().equalsIgnoreCase("pending")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("pending")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    holder.txtStatus.setTextColor(android.graphics.Color.parseColor(color_value));
                    holder.txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
        } else if (requestList.get(listPosition).getStatus().equalsIgnoreCase("failure")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("failure")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    holder.txtStatus.setTextColor(android.graphics.Color.parseColor(color_value));
                    holder.txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
        }
        /* [START] - recharge_status":"Credit" */
        else if (requestList.get(listPosition).getStatus().equalsIgnoreCase("credit")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("credit")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    holder.txtStatus.setTextColor(android.graphics.Color.parseColor(color_value));
                    holder.txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
        }
        // [END]

        holder.lnrPaymentDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListDetailActivity.class);
                intent.putExtra("classdata", (Parcelable) requestList.get(listPosition));
                intent.putExtra("from","paymentrequest");
                intent.putExtra("receipt_type", "");
                context.startActivity(intent);
            }
        });

        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String yesterday = df.format(cal.getTime());

            Calendar tcal = Calendar.getInstance();
            String today = df.format(tcal.getTime());

            if (listPosition == 0) {
                holder.txtDate.setVisibility(View.VISIBLE);
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date newDate = null;
                newDate = df1.parse(requestList.get(0).getDatetime());
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate1 = destDf.format(newDate);

                if (!newDate1.equals(today)) {
                    if (yesterday.equals(newDate1)) {
                        holder.txtDate.setText("Yesterday, " + newDate1);
                    } else {
                        holder.txtDate.setText(newDate1 + "");
                    }
                } else {
                    holder.txtDate.setText("Today, " + newDate1);
                }
            } else {
                DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                // parse the date string into Date object
                Date date = srcDf.parse(requestList.get(listPosition).getDatetime());
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate = destDf.format(date);
                if (Constants.commonDateFormate(requestList.get(listPosition).getDatetime(), "yyyy-MM-dd hh:mm:ss", "dd-MMM-yyyy").equals(Constants.commonDateFormate(requestList.get(listPosition-1).getDatetime(), "yyyy-MM-dd hh:mm:ss", "dd-MMM-yyyy"))) {
                    holder.txtDate.setVisibility(View.GONE);
                } else {
                    holder.txtDate.setVisibility(View.VISIBLE);
                    if (!newDate.equals(today)) {
                        if (yesterday.equals(newDate)) {
                            holder.txtDate.setText("Yesterday, " + newDate);
                        } else {
                            holder.txtDate.setText(newDate + "");
                        }
                    } else {
                        holder.txtDate.setText("Today, " + newDate);
                    }
                }
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

}
