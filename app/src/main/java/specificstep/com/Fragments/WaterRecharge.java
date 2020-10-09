package specificstep.com.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Adapters.GridViewMobileRechargeAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Company;
import specificstep.com.Models.Product;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterRecharge extends Fragment {

    private View view;
    private GridView grid_mobile_recharge;
    private DatabaseHelper databaseHelper;
    private ArrayList<Company> companyArrayList;
    private ArrayList<Company> CompanyListDataSearch;
    private ArrayList<Company> finalCompanyArrayList;
    private ArrayList<Product> productArrayList;

    private MenuItem menuItem;
    ArrayList<String> menuWallet;

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    private TransparentProgressDialog transparentProgressDialog;
    ArrayList<User> userArrayList;
    private final int SUCCESS_WALLET_LIST = 1, ERROR = 2;
    private Context context;
    EditText edtSearch;
    TextView txtCompanyListClear;
    ImageView imgCompanyListNoData;

    private Context getContextInstance() {
        if (context == null) {
            context = WaterRecharge.this.getActivity();
            return context;
        } else {
            return context;
        }
    }


    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public WaterRecharge() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_water_recharge, container, false);

        mainActivity().getSupportActionBar().setTitle("Water Bill Pay");
        mainActivity().displayRechargeBottomBar(true);
        databaseHelper = new DatabaseHelper(getActivity());
        userArrayList = new ArrayList<User>();
        userArrayList = databaseHelper.getUserDetail();
        companyArrayList = new ArrayList<Company>();
        finalCompanyArrayList = new ArrayList<Company>();
        productArrayList = new ArrayList<Product>();
        init();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void init() {
        grid_mobile_recharge = (GridView) view.findViewById(R.id.grid_water_rechrge);
        edtSearch = view.findViewById(R.id.edtCompanyListSearch);
        txtCompanyListClear = view.findViewById(R.id.txtCompanyListClear);
        imgCompanyListNoData = view.findViewById(R.id.imgCompanyListNoData);
        String service_type = "WATER";
        companyArrayList = databaseHelper.getCompanyDetails(service_type);

        /* [START] - 2017_05_02 - if company have 0 product don't display this company */
        for (int i = 0; i < companyArrayList.size(); i++) {
            String companyId = companyArrayList.get(i).getId();
            productArrayList = databaseHelper.getProductDetails(companyId);
            if (productArrayList.size() > 0) {
                finalCompanyArrayList.add(companyArrayList.get(i));
            }
        }

        Collections.sort(finalCompanyArrayList, new Comparator<Company>() {
            @Override
            public int compare(Company o1, Company o2) {
                return o1.getCompany_name().compareToIgnoreCase(o2.getCompany_name());
            }
        });
        // [END]
        GridViewMobileRechargeAdapter adapter = new GridViewMobileRechargeAdapter(getActivity(), finalCompanyArrayList, getActivity().getSupportFragmentManager(), "WATER", "Water Bill Pay");
        grid_mobile_recharge.setAdapter(adapter);

        txtCompanyListClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");

                GridViewMobileRechargeAdapter adapter = new GridViewMobileRechargeAdapter(getActivity(), finalCompanyArrayList, getActivity().getSupportFragmentManager(), "WATER", "Water Bill Pay");
                grid_mobile_recharge.setAdapter(adapter);
                hideKeyboard(getActivity());
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void filter(String text) {
        ArrayList<Company> temp = new ArrayList();
        CompanyListDataSearch = new ArrayList<>();
        for (Company d : finalCompanyArrayList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getCompany_name().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
//        grid_mobile_recharge.setVisibility(View.VISIBLE);
//        imgCompanyListNoData.setVisibility(View.GONE);
        CompanyListDataSearch = temp;
        if (CompanyListDataSearch != null && CompanyListDataSearch.size() > 0) {
            GridViewMobileRechargeAdapter adapter = new GridViewMobileRechargeAdapter(getActivity(), CompanyListDataSearch, getActivity().getSupportFragmentManager(), "WATER", "Water Bill Pay");
            grid_mobile_recharge.setAdapter(adapter);
            grid_mobile_recharge.setVisibility(View.VISIBLE);
            imgCompanyListNoData.setVisibility(View.GONE);
        } else {
            grid_mobile_recharge.setVisibility(View.GONE);
            imgCompanyListNoData.setVisibility(View.VISIBLE);
        }
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

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };
    // [END]

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
        } catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(context, message);
            } catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
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
                String decrypted_response = Constants.decryptAPI(context, encrypted_response);

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

                if (walletsModelList.size() > 0) {
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
        } catch (JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
