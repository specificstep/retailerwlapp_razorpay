package specificstep.com.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import specificstep.com.Fragments.DTHRechargeFragment;
import specificstep.com.Fragments.RechargeFragment;
import specificstep.com.Models.OfferRechargeModel;
import specificstep.com.R;

public class ViewOffersRechargeAdapter extends RecyclerView.Adapter<ViewOffersRechargeAdapter.MyViewHolder> {

    private List<OfferRechargeModel> dataSet;
    Context context;
    String from;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtRs;
        TextView txtDesc;
        LinearLayout lnr;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.txtRs = (TextView) itemView.findViewById(R.id.txt_view_offer_rs);
            this.txtDesc = (TextView) itemView.findViewById(R.id.txt_view_offer_desc);
            this.lnr = (LinearLayout) itemView.findViewById(R.id.lnr_view_offer);
        }
    }

    public ViewOffersRechargeAdapter(String from,Context con,List<OfferRechargeModel> data) {
        this.from = from;
        this.context = con;
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_view_offers_recharge, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        holder.txtRs.setText(context.getResources().getString(R.string.Rs) + dataSet.get(listPosition).getRs());
        holder.txtDesc.setText(dataSet.get(listPosition).getDesc());
        holder.lnr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String balance = dataSet.get(listPosition).getRs();
                if(from.equals("mobile")) {
                    if(!balance.equals("0")) {
                        RechargeFragment.edtAmount.setText(dataSet.get(listPosition).getRs());
                        RechargeFragment.edtAmount.setSelection(RechargeFragment.edtAmount.getText().length());
                        RechargeFragment.dialogAsk.dismiss();
                    }
                } else if(from.equals("dth")) {
                    if(!balance.equals("0")) {
                        DTHRechargeFragment.edtAmount.setText(dataSet.get(listPosition).getRs());
                        RechargeFragment.edtAmount.setSelection(RechargeFragment.edtAmount.getText().length());
                        DTHRechargeFragment.dialogAsk.dismiss();
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
