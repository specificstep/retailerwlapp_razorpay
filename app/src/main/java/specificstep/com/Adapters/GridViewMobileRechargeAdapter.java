package specificstep.com.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Fragments.DTHRechargeFragment;
import specificstep.com.Fragments.ElectricityRechargeFragment;
import specificstep.com.Fragments.GasRechargeFragment;
import specificstep.com.Fragments.MobilePostPaidRechargeFragment;
import specificstep.com.Fragments.ProductFragment;
import specificstep.com.Fragments.RechargeFragment;
import specificstep.com.Fragments.WaterRechargeFragment;
import specificstep.com.Models.Company;
import specificstep.com.Models.Product;
import specificstep.com.R;

/**
 * Created by ubuntu on 16/1/17.
 */

public class GridViewMobileRechargeAdapter extends BaseAdapter {
    Context context;
    ArrayList<Company> companyArrayList;
    LayoutInflater inflater;
    FragmentManager fragmentManager;
    String str_fragment_name, final_fragment_name;
    private DatabaseHelper databaseHelper;
    private ArrayList<Product> productArrayList;

    public GridViewMobileRechargeAdapter(Context activity, ArrayList<Company> companyArrayList, FragmentManager supportFragmentManager, String fragment_name, String page_name) {
        context = activity;
        this.companyArrayList = companyArrayList;
        fragmentManager = supportFragmentManager;
        str_fragment_name = fragment_name;
        final_fragment_name = page_name;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        databaseHelper = new DatabaseHelper(context);
        productArrayList = new ArrayList<Product>();
    }

    @Override
    public int getCount() {
        return companyArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return companyArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.adapter_grid_mobile_recharge, null);
        ImageView iv_company_logo = (ImageView) convertView.findViewById(R.id.iv_company_logo_adapter_mo_recharge);
        TextView tv_company_name = (TextView) convertView.findViewById(R.id.tv_compnay_name_adapter_mo_recharge);

        tv_company_name.setText(companyArrayList.get(position).getCompany_name());
        String img_path = companyArrayList.get(position).getLogo();

        if (!TextUtils.isEmpty(img_path)) {
            Picasso.with(context)
                    .load(img_path)
                    .placeholder(R.drawable.placeholder_icon)
                    .error(R.drawable.placeholder_icon)
                    .into(iv_company_logo);
        } else {
            iv_company_logo.setImageResource(R.drawable.placeholder_icon);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    /* [START] - 2017_05_02 - If company have more than one products than  display products, if company have only one products than direct display to recharge form. */
                // if company have one product open recharge screen
                String companyId = companyArrayList.get(position).getId();
                productArrayList = databaseHelper.getProductDetails(companyId);
                if (productArrayList.size() == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("company_id", productArrayList.get(0).getCompany_id());
                    bundle.putString("product_id", productArrayList.get(0).getId());
                    bundle.putString("company_name", companyArrayList.get(position).getCompany_name());
                    bundle.putString("company_image", companyArrayList.get(position).getLogo());
                    bundle.putString("product_name", productArrayList.get(0).getProduct_name());
                    bundle.putString("product_image", productArrayList.get(0).getProduct_logo());
                    bundle.putString("is_partial", productArrayList.get(0).getIs_partial());

                    bundle.putString("first_tag", productArrayList.get(0).getFirst_tag());
                    bundle.putString("first_type", productArrayList.get(0).getFirst_type());
                    bundle.putString("first_start_with", productArrayList.get(0).getFirst_start_with());
                    bundle.putString("first_defined", productArrayList.get(0).getFirst_defined());
                    bundle.putString("first_length", productArrayList.get(0).getFirst_length());

                    bundle.putString("second_tag", productArrayList.get(0).getSecond_tag());
                    bundle.putString("second_length", productArrayList.get(0).getSecond_length());
                    bundle.putString("second_type", productArrayList.get(0).getSecond_type());
                    bundle.putString("second_start_with", productArrayList.get(0).getSecond_start_with());
                    bundle.putString("second_defined", productArrayList.get(0).getSecond_defined());

                    bundle.putString("third_tag", productArrayList.get(0).getThird_tag());
                    bundle.putString("third_length", productArrayList.get(0).getThird_length());
                    bundle.putString("third_type", productArrayList.get(0).getThird_type());
                    bundle.putString("third_start_with", productArrayList.get(0).getThird_start_with());
                    bundle.putString("third_defined", productArrayList.get(0).getThird_length());

                    bundle.putString("fourth_tag", productArrayList.get(0).getFourth_tag());
                    bundle.putString("fourth_length", productArrayList.get(0).getThird_length());
                    bundle.putString("fourth_type", productArrayList.get(0).getFourth_type());
                    bundle.putString("fourth_start_with", productArrayList.get(0).getFourth_start_with());
                    bundle.putString("fourth_defined", productArrayList.get(0).getFourth_defined());

                    //redirect it directly on Recharge fragment
                    if (str_fragment_name.equals("Mobile")) {
                        RechargeFragment rechargeFragment = new RechargeFragment();
                        rechargeFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString()+"").commit();
                    } else if (str_fragment_name.equals("DTH")) {
                        DTHRechargeFragment rechargeFragment = new DTHRechargeFragment();
                        rechargeFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString()+"").commit();
                    } else if (str_fragment_name.equals("ELECTRICITY")) {
                        ElectricityRechargeFragment rechargeFragment = new ElectricityRechargeFragment();
                        rechargeFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString()+"").commit();
                    } else if (str_fragment_name.equals("GAS")) {
                        GasRechargeFragment rechargeFragment = new GasRechargeFragment();
                        rechargeFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString()+"").commit();
                    } else if (str_fragment_name.equals("MOBILE_POSTPAID")) {
                        MobilePostPaidRechargeFragment rechargeFragment = new MobilePostPaidRechargeFragment();
                        rechargeFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString()+"").commit();
                    } else if (str_fragment_name.equals("WATER")) {
                        WaterRechargeFragment rechargeFragment = new WaterRechargeFragment();
                        rechargeFragment.setArguments(bundle);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.container, rechargeFragment).addToBackStack(rechargeFragment.toString()+"").commit();
                    }
                }
                // if company have more than one product then open product screen
                else if (productArrayList.size() > 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("company_id", companyArrayList.get(position).getId());
                    bundle.putString("company_name", companyArrayList.get(position).getCompany_name());
                    bundle.putString("company_image", companyArrayList.get(position).getLogo());
                    bundle.putString("fragment_name", str_fragment_name);
                    bundle.putString("fragment_page_name", final_fragment_name);
                    ProductFragment productFragment = new ProductFragment();
                    productFragment.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //fragmentTransaction.add(R.id.container, productFragment).addToBackStack(productFragment.toString()+"").commit();
                    fragmentTransaction.add(R.id.container, productFragment).addToBackStack("company").commit();
                }
                // [END]
            }
        });

        // [END]
        return convertView;
    }

}
