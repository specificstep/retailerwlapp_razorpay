package specificstep.com.Adapters;

import android.app.Activity;
import android.content.Context;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import specificstep.com.Activities.ListDetailActivity;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.Models.DMTPaymentListModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;

public class DMTPaymentListAdapter extends RecyclerView.Adapter<DMTPaymentListAdapter.MyViewHolder>{

    List<DMTPaymentListModel> dataSet;
    public Context context;
    public static int position;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtSenderName, txtTransId, txtAmount, txtTransactionStatus,
                txtDate;
        LinearLayout lnrPaymentDetail;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.txtSenderName = (TextView) itemView.findViewById(R.id.txtDMTPaymentListSenderName);
            this.txtTransId = (TextView) itemView.findViewById(R.id.txtDMTPaymentListTransId);
            this.txtAmount = (TextView) itemView.findViewById(R.id.txtDMTPaymentListAmount);
            this.txtTransactionStatus = (TextView) itemView.findViewById(R.id.txtDMTPaymentListTransactionStatus);
            this.txtDate = (TextView) itemView.findViewById(R.id.txtDMTPaymentListDate);
            this.lnrPaymentDetail = (LinearLayout) itemView.findViewById(R.id.lnrDMTPaymentList);

        }
    }

    public DMTPaymentListAdapter(Context con,List<DMTPaymentListModel> data) {
        this.context = con;
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_payment_list, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        if(!dataSet.get(listPosition).getSender_lastname().equals("null")) {
            holder.txtSenderName.setText(dataSet.get(listPosition).getSender_mobilenumber() + " (" + dataSet.get(listPosition).getSender_firstname() + " " + dataSet.get(listPosition).getSender_lastname() + ")");
        } else {
            holder.txtSenderName.setText(dataSet.get(listPosition).getSender_mobilenumber() + " (" + dataSet.get(listPosition).getSender_firstname() + ")");
        }
        if(!dataSet.get(listPosition).getTrans_id().equals("null")) {
            holder.txtTransId.setText(dataSet.get(listPosition).getTrans_id());
            holder.txtTransId.setVisibility(View.VISIBLE);
        } else {
            holder.txtTransId.setVisibility(View.GONE);
        }
        try {
            if (!dataSet.get(listPosition).getAmount().equals("null")) {
                holder.txtAmount.setText(Constants.addRsSymbol((Activity) context, dataSet.get(listPosition).getAmount()));
                holder.txtAmount.setVisibility(View.VISIBLE);
            } else {
                holder.txtAmount.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
        if(!dataSet.get(listPosition).getTransaction_status().equals("null")) {
            if(dataSet.get(listPosition).getTransaction_status().equals("1")) {
                holder.txtTransactionStatus.setText("Successful");
                holder.txtTransactionStatus.setTextColor(context.getResources().getColor(R.color.colorGreen));
                holder.txtAmount.setTextColor(context.getResources().getColor(R.color.colorGreen));
            } else if(dataSet.get(listPosition).getTransaction_status().equals("0")) {
                holder.txtTransactionStatus.setText("Pending");
                holder.txtTransactionStatus.setTextColor(context.getResources().getColor(R.color.textColor));
                holder.txtAmount.setTextColor(context.getResources().getColor(R.color.textColor));
            } else if(dataSet.get(listPosition).getTransaction_status().equals("2")) {
                holder.txtTransactionStatus.setText("Fail");
                holder.txtTransactionStatus.setTextColor(context.getResources().getColor(R.color.colorRed));
                holder.txtAmount.setTextColor(context.getResources().getColor(R.color.colorRed));
            } else {
                holder.txtTransactionStatus.setText("Fail");
                holder.txtTransactionStatus.setTextColor(context.getResources().getColor(R.color.colorRed));
                holder.txtAmount.setTextColor(context.getResources().getColor(R.color.colorRed));
            }
            holder.txtTransactionStatus.setVisibility(View.VISIBLE);
        } else {
            holder.txtTransactionStatus.setVisibility(View.GONE);
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String yesterday = df.format(cal.getTime());

            Calendar tcal = Calendar.getInstance();
            String today = df.format(tcal.getTime());

            if (listPosition == 0) {
                holder.txtDate.setVisibility(View.VISIBLE);
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newDate = null;
                newDate = df1.parse(dataSet.get(0).getAdd_date());
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
                DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // parse the date string into Date object
                Date date = srcDf.parse(dataSet.get(listPosition).getAdd_date());
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate = destDf.format(date);
                if (Constants.commonDateFormate(dataSet.get(listPosition).getAdd_date(),"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy").equals(Constants.commonDateFormate(dataSet.get(listPosition - 1).getAdd_date(),"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy"))) {
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

            holder.lnrPaymentDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ListDetailActivity.class);
                    intent.putExtra("classdata", (Parcelable) dataSet.get(listPosition));
                    intent.putExtra("from","dmttransaction");
                    intent.putExtra("receipt_type", "");
                    context.startActivity(intent);
                }
            });


        } catch (Exception e) {
            Dlog.d("Transaction Search: " + e.toString());
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
