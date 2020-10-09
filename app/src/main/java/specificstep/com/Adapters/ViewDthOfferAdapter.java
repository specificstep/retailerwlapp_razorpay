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
import specificstep.com.Models.DthOfferModel;
import specificstep.com.R;

public class ViewDthOfferAdapter extends RecyclerView.Adapter<ViewDthOfferAdapter.MyViewHolder>  {

    private List<DthOfferModel> dataSet;
    Context context;

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

    public ViewDthOfferAdapter(Context con,List<DthOfferModel> data) {
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
        holder.txtDesc.setText("Plan Name: " + dataSet.get(listPosition).getPlan_name()
                + "\nDescription: " + dataSet.get(listPosition).getDesc() + "\nDuration: "
                + dataSet.get(listPosition).getDuration());
        holder.lnr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String balance = dataSet.get(listPosition).getRs();
                if (!balance.equals("0")) {
                    DTHRechargeFragment.edtAmount.setText(dataSet.get(listPosition).getRs());
                    DTHRechargeFragment.dialogAsk.dismiss();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


}
