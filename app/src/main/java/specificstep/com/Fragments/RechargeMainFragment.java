package specificstep.com.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Product;
import specificstep.com.Models.State;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 16/1/17.
 */

public class RechargeMainFragment extends Fragment {

    private final int ERROR = 2, SUCCESS_MOBILE_PRODUCT = 3, SUCCESS_DTH_PRODUCT = 4,
            SUCCESS_STATE = 5, SUCCESS_ELECTRICITY_SERVICE = 6, SUCCESS_WALLET_LIST = 7;
    private Context context;
    View view;
    TabLayout tabLayout;
    ViewPager viewPager;
    Adapter adapter;
    String PREF_KEY_CURRENT_TAB = "PREF_KEY_CURRENT_TAB";

    ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    SharedPreferences sharedPreferences;
    Constants constants;
    private int position = 0;
    ArrayList<Product> productArrayList;
    ArrayList<State> stateArrayList;
    boolean isThere;
    private TransparentProgressDialog transparentProgressDialog;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    private Context getContextInstance() {
        if (context == null) {
            context = RechargeMainFragment.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recharge_main, null);
        /* [START] - 2017_04_18 set title bar as Mobile recharge */
        mainActivity().getSupportActionBar().setTitle("Recharge");
        // [END]

        // initialise controls and variables
        initControls();
        // set listener
        setListener();

        // Display bottom bar in add balance
        mainActivity().displayRechargeBottomBar(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    private void initControls() {

        // log = new LogMessage(RechargeMainFragment.class.getSimpleName());
        // [START] - bind view
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        // [END]

        // [START] - initialise objects
        constants = new Constants();
        databaseHelper = new DatabaseHelper(context);
        userArrayList = new ArrayList<User>();
        adapter = new Adapter(getChildFragmentManager());
        productArrayList = new ArrayList<Product>();
        stateArrayList = new ArrayList<State>();
        sharedPreferences = context.getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        // [END]

        // [START] - get user data from database
        userArrayList = databaseHelper.getUserDetail();
        // [END]

        // [START] - Setup view pager
        setupViewPager(viewPager);
        // [END]

        // [START] - set current view pager
        tabLayout.setupWithViewPager(viewPager);
        position = sharedPreferences.getInt(constants.SELECTED_TAB, 0);
        viewPager.setCurrentItem(position);
        // [END]

        // [START] - Get mobile product, DTH and state data from server
        LogMessage.i("Fetch Data : " + sharedPreferences.getString("fetch_data", ""));
        // Check fetch_data value from shared preference, if value is "1" then get data from server
        if (TextUtils.equals(sharedPreferences.getString("fetch_data", ""), "1")) {
            // get product data
            makeMobileProduct();
            // get DTH data
            makeDTHProduct();
            // get State data
            makeState();

            try {
                isThere = Arrays.asList(Constants.elctricity_flag).contains("true");
                if (isThere) {
                    makeElectricityProduct();
                }
            } catch (Exception e) {
                Dlog.d(e.toString());
            }

        }
        // after getting mobile, DTH and state data change fetch_data value as "0"
        sharedPreferences.edit().putString("fetch_data", "0").commit();
        // [END]
    }

    private void setListener() {
        // [START] - Add tab selected listener to change tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // store current tab position in integer variable
                position = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        // [END]
    }

    private MenuItem menuItem;
    ArrayList<String> menuWallet;

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // [START] - set option menu
        // Clear menu
        menu.clear();
        // Set menu
        inflater.inflate(R.menu.menu_main_activity, menu);
        Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        menuItem = menu.findItem(R.id.action_balance_menu_main);
        // [END]
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_balance_menu_main:
                if (Constants.checkInternet(getActivity())) {
                    makeWalletCall();
                } else {
                    //Constants.showNoInternetDialog(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        // [START] - Set view pager
        // Add mobile recharge fragment
        adapter.addFragment(new MobileRecharge(), "Mobile Recharge");
        // Add DTH recharge fragment
        adapter.addFragment(new DTHRecharge(), "DTH Recharge");

        try {
            isThere = Arrays.asList(Constants.elctricity_flag).contains("true");
            if (isThere) {
                adapter.addFragment(new ElectricityRecharge(), Constants.electricity_title);
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

        // set adapter in view pager
        viewPager.setAdapter(adapter);
        // [END]
    }

    //multi wallet 14-3-2019
    public void makeWalletCall() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.walletType;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_WALLET_LIST, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    public void parseSuccessWalletResponse(String response) {

        LogMessage.e("Wallet Response : " + response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("Message : " + message);
                LogMessage.e("Message : " + message);

                LogMessage.e("Wallet : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);

                LogMessage.e("Wallet : " + "decrypted_response : " + decrypted_response);

                JSONArray array = new JSONArray(decrypted_response);
                walletsModelList = new ArrayList<WalletsModel>();
                walletsList = new ArrayList<String>();
                menuWallet = new ArrayList<String>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    walletsModel = new WalletsModel();
                    walletsModel.setWallet_type(object.getString("wallet_type"));
                    walletsModel.setWallet_name(object.getString("wallet_name"));
                    walletsModel.setBalance(object.getString("balance"));
                    walletsModelList.add(walletsModel);
                    walletsList.add(object.getString("wallet_name"));
                    try {
                        menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }

                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                if(walletsModelList.size()>0) {
                    Constants.showWalletPopup(getActivity());
                }

                /*try {
                    if(menuItem != null) {
                        if (walletsModelList.size() > 0) {
                            menuItem.setVisible(true);
                        } else {
                            menuItem.setVisible(false);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }*/

            } else {
            }
        }
        catch(JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    /***
     * Adapter for view pager
     */
    static class Adapter extends FragmentStatePagerAdapter {
        // Create variables for fragment list and fragment title list.
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * This method is use to add fragments in FragmentStatePagerAdapter
         *
         * @param fragment Object of fragment
         * @param title    Title of fragment
         */
        public void addFragment(Fragment fragment, String title) {
            // Add fragment object in Fragment type list
            mFragments.add(fragment);
            // Add fragment title in String type list
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Intent intent = new Intent(getActivity(), Main2Activity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }

    private void makeMobileProduct() {
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.product;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "service",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            "1",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_MOBILE_PRODUCT, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseMobileProductResponse(String response) {
        LogMessage.i("Product List Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                JSONObject jsonObject1 = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                LogMessage.i("Product List Decrypted Response : " + jsonObject1);
                JSONArray jsonArray = jsonObject1.getJSONArray("product");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Product product = new Product();
                    product.setId(jsonObject2.getString("id"));
                    product.setProduct_name(jsonObject2.getString("product_name"));
                    product.setCompany_id(jsonObject2.getString("company_id"));
                    product.setProduct_logo(jsonObject2.getString("logo"));
                    product.setService_type("Mobile");
                    productArrayList.add(product);
                }
                databaseHelper.deleteProductDetail("Mobile");
                databaseHelper.addProductsDetails(productArrayList);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // DTH Product
    private void makeDTHProduct() {
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.product;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "service",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            "2",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_DTH_PRODUCT, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in DTH Product native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseDTHProductResponse(String response) {
        LogMessage.i("Product List Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                JSONObject jsonObject1 = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                JSONArray jsonArray = jsonObject1.getJSONArray("product");
                productArrayList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Product product = new Product();
                    product.setId(jsonObject2.getString("id"));
                    product.setProduct_name(jsonObject2.getString("product_name"));
                    product.setCompany_id(jsonObject2.getString("company_id"));
                    product.setProduct_logo(jsonObject2.getString("logo"));
                    product.setService_type("DTH");
                    productArrayList.add(product);
                }
                databaseHelper.deleteProductDetail("DTH");
                databaseHelper.addProductsDetails(productArrayList);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // DTH Product
    private void makeElectricityProduct() {
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.product;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "service",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            "3",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_ELECTRICITY_SERVICE, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in DTH Product native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseElectricityProductResponse(String response) {
        LogMessage.i("DTH Product Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Electricity product : " + decryptedResponse);
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("product");
                productArrayList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Product product = new Product();
                    product.setId(jsonObject2.getString("id"));
                    product.setProduct_name(jsonObject2.getString("product_name"));
                    product.setCompany_id(jsonObject2.getString("company_id"));
                    product.setProduct_logo(jsonObject2.getString("logo"));
                    product.setService_type("ELECTRICITY");
                    productArrayList.add(product);
                }
                databaseHelper.deleteProductDetail("ELECTRICITY");
                databaseHelper.addProductsDetails(productArrayList);
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    private AlertDialog alertDialog;

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Info!");
            alertDialog.setCancelable(false);
            alertDialog.setMessage(message);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
        catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(context, message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // get State data
    private void makeState() {
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.state;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "service",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            "1",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_STATE, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in DTH Product native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseStateResponse(String response) {
        LogMessage.i("State List Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                JSONObject jsonObject1 = new JSONObject(Constants.decryptAPI(context,encrypted_string));
                JSONArray jsonArray = jsonObject1.getJSONArray("state");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    State state = new State();
                    state.setCircle_id(jsonObject2.getString("circle_id"));
                    state.setCircle_name(jsonObject2.getString("circle_name"));
                    stateArrayList.add(state);
                }
                databaseHelper.deleteStateDetail();
                databaseHelper.addStatesDetails(stateArrayList);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == SUCCESS_MOBILE_PRODUCT) {
                parseMobileProductResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_DTH_PRODUCT) {
                parseDTHProductResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_STATE) {
                parseStateResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_ELECTRICITY_SERVICE) {
                parseElectricityProductResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };
    // [END]

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.edit().putInt(constants.SELECTED_TAB, position).commit();
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
            }
            if (transparentProgressDialog != null) {
                if (!transparentProgressDialog.isShowing()) {
                    transparentProgressDialog.show();
                }
            }
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // dismiss progress dialog
    private void dismissProgressDialog() {
        try {
            if (transparentProgressDialog != null) {
                if (transparentProgressDialog.isShowing())
                    transparentProgressDialog.dismiss();
            }
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}