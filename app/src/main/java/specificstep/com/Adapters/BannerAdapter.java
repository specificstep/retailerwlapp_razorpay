package specificstep.com.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import specificstep.com.Models.BannerModel;
import specificstep.com.R;

public class BannerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    //private Integer [] images = {R.drawable.banner,R.drawable.banner1};
    private ArrayList<BannerModel> bannerModelList;

    public BannerAdapter(Context context, ArrayList<BannerModel> bannerModelList) {
        this.context = context;
        this.bannerModelList = bannerModelList;
    }

    @Override
    public int getCount() {
        return bannerModelList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.row_slider_banner, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imgBanner);
        Picasso.with(context).load(bannerModelList.get(position).getImage()).placeholder(R.drawable.placeholder_icon).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri;
                if(bannerModelList.get(position).getName().contains("https:")) {
                    uri = Uri.parse(bannerModelList.get(position).getName().replace("https:","http:"));
                } else if(bannerModelList.get(position).getName().contains("http:")) {
                    uri = Uri.parse(bannerModelList.get(position).getName());
                } else {
                    uri = Uri.parse("http://" + bannerModelList.get(position).getName());
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
        });

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }

}
