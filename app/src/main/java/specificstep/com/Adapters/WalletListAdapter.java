package specificstep.com.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import specificstep.com.Models.WalletsModel;
import specificstep.com.R;

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.MyViewHolder> {

    Context context;
    List<WalletsModel> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtWalletName, txtWalletAmount;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.txtWalletName = (TextView) itemView.findViewById(R.id.txtWalletName);
            this.txtWalletAmount = (TextView) itemView.findViewById(R.id.txtWalletAmount);
        }
    }

    public WalletListAdapter(Context con, List<WalletsModel> data) {
        this.context = con;
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_wallet_list_popup, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(WalletListAdapter.MyViewHolder holder, int position) {

        holder.txtWalletName.setText(dataSet.get(position).getWallet_name());
        holder.txtWalletAmount.setText(context.getResources().getString(R.string.Rs) + "  " + dataSet.get(position).getBalance());

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
