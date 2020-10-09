package specificstep.com.Adapters;

import android.app.Activity;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.GlobalClasses.ServicesModel;
import specificstep.com.R;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.MyViewHolder> {

    private List<ServicesModel> moviesList;
    Activity activity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView img;
        public LinearLayout lnrService;
        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.txtTitle);
            img = (ImageView) view.findViewById(R.id.imgIcon);
            lnrService = (LinearLayout) view.findViewById(R.id.lnrService);
        }
    }


    public ServicesAdapter(Activity activity, List<ServicesModel> moviesList) {
        this.moviesList = moviesList;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_services, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.title.setText(moviesList.get(position).getName());
        holder.img.setImageDrawable(activity.getResources().getDrawable(moviesList.get(position).getIcon()));
        holder.lnrService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos;
                if(moviesList.get(position).getId().equals("1")) {
                    pos = 0;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("2")) {
                    pos = 17;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("3")) {
                    pos = 18;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("6")) {
                    pos = 14;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("21")) {
                    pos = 9;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("22")) {
                    pos = 15;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("11")) {
                    pos = 16;
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra("position", pos);
                    activity.startActivity(intent);
                } else if(moviesList.get(position).getId().equals("0")) {
                    Main2Activity.drawer.openDrawer(Gravity.START);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

}
