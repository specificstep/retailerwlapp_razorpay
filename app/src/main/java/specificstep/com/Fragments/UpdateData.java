package specificstep.com.Fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.LoginActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Color;
import specificstep.com.Models.Company;
import specificstep.com.Models.DMTAddBenefitiaryBankName;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.PaymentGatewayModel;
import specificstep.com.Models.Product;
import specificstep.com.Models.State;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.MyPrefs;
import specificstep.com.utility.NotificationUtil;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 9/1/17.
 */

public class UpdateData extends Fragment {

    private View view;

    /* Other class objects */
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private ProgressBar progressBar;
    private DatabaseHelper databaseHelper;
    private MyPrefs prefs;
    private Context context;
    private AlertDialog alertDialog, alertDialog_Logout;

    /* All local int and string variables */
    private final int SUCCESS_MOBILE_COMPANY = 1, ERROR = 2, SUCCESS_DTH_COMPANY = 3,
            SUCCESS_MOBILE_PRODUCT = 4, SUCCESS_DTH_PRODUCT = 5, SUCCESS_STATE = 6, SUCCESS_SETTING = 7,
            ERROR_INVALID_DETAILS = 8, SUCCESS_DMT_BANK_NAME = 12, SUCCESS_ELECTRICITY_SERVICE = 13,
            SUCCESS_ELECTRICITY_COMPANY = 14, SUCCESS_GAS_PRODUCT = 15,
            SUCCESS_GAS_COMPANY = 16, SUCCESS_MOBILE_POSTPAID_PRODUCT = 17,
            SUCCESS_MOBILE_POSTPAID_COMPANY = 18, SUCCESS_WATER_PRODUCT = 19,
            SUCCESS_WATER_COMPANY = 20;
    private String requireUpdate = "0";

    /* All ArrayList */
    private ArrayList<User> userArrayList;
    private ArrayList<Product> productArrayList;
    private ArrayList<State> stateArrayList;
    private ArrayList<Company> companyArrayList;

    /* All views */
    private TextView txtLastUpdate, textView;
    private Button updateButton, homeButton;

    //Mansi change - 26-2-2019
    ArrayList<WalletsModel> walletsModelList;
    WalletsModel walletsModel;

    ArrayList<DMTAddBenefitiaryBankName> mainBankNamesList;
    DMTAddBenefitiaryBankName bankNameModel;

    private Context getContextInstance() {
        if (context == null) {
            context = UpdateData.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update_data, null);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        databaseHelper = new DatabaseHelper(getContextInstance());
        constants = new Constants();
        sharedPreferences = getContextInstance().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        prefs = new MyPrefs(getContextInstance(), constants.PREF_NAME);

        userArrayList = databaseHelper.getUserDetail();

        productArrayList = new ArrayList<Product>();
        stateArrayList = new ArrayList<State>();
        companyArrayList = new ArrayList<Company>();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setMax(100);

        textView = (TextView) view.findViewById(R.id.textView);

        updateButton = (Button) view.findViewById(R.id.btn_update_data);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData();
            }
        });
        homeButton = (Button) view.findViewById(R.id.btn_return_home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                Intent intent = new Intent(getContextInstance(), Main2Activity.class);
                startActivity(intent);
            }
        });

        txtLastUpdate = (TextView) view.findViewById(R.id.txt_Update_LastUpdateDate);
        String updateDate = prefs.retriveString(constants.PREF_UPDATE_DATE, "0");
        String updateTime = prefs.retriveString(constants.PREF_UPDATE_TIME, "0");
        if (TextUtils.equals(updateDate, "0")) {
            txtLastUpdate.setVisibility(View.GONE);
        } else {
            txtLastUpdate.setVisibility(View.VISIBLE);
            String convert_date = Constants.commonDateFormate(updateDate + " " + updateTime,"dd.MM.yyyy HH:mm:ss","dd-MMM-yyyy hh:mm aa");
            txtLastUpdate.setText("Last update : " + convert_date);
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(constants.KEY_REQUIRE_UPDATE) != null
                    && !bundle.getString(constants.KEY_REQUIRE_UPDATE).equals("")) {
                requireUpdate = bundle.getString(constants.KEY_REQUIRE_UPDATE);
                if (TextUtils.equals(requireUpdate, "1")) {
                    updateData();
                }
            }
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    private void updateData() {
        updateButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        txtLastUpdate.setVisibility(View.GONE);
        progressBar.setProgress(0);
        try {
            databaseHelper.truncateUpdateData();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
        makeMobileProduct();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
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
                    // handle back button's click listener
                    Intent intent = new Intent(getContextInstance(), Main2Activity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

    }


    // Mobile Product
    private void makeMobileProduct() {
        textView.setText("Updating\n\n1/15) Mobile Services (Processing)");
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
                } catch (Exception ex) {
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
        LogMessage.i("Mobile Product Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Mobile product 1 : " + decryptedResponse);
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("product");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Product product = new Product();
                    product.setId(jsonObject2.getString("id"));
                    product.setProduct_name(jsonObject2.getString("product_name"));
                    product.setCompany_id(jsonObject2.getString("company_id"));
                    product.setProduct_logo(jsonObject2.getString("logo"));
                    product.setIs_partial(jsonObject2.getString("is_partial"));
                    product.setService_type("Mobile");
                    product.setFirst_tag(jsonObject2.getString("first_tag"));
                    product.setFirst_length(jsonObject2.getString("first_length"));
                    product.setFirst_type(jsonObject2.getString("first_type"));
                    product.setFirst_defined("");
                    product.setSecond_tag(jsonObject2.getString("second_tag"));
                    product.setSecond_length(jsonObject2.getString("second_length"));
                    product.setSecond_type(jsonObject2.getString("second_type"));
                    product.setSecond_start_with("");
                    product.setSecond_defined("");
                    product.setThird_tag(jsonObject2.getString("third_tag"));
                    product.setThird_length(jsonObject2.getString("third_length"));
                    product.setThird_type(jsonObject2.getString("third_type"));
                    product.setThird_start_with("");
                    product.setThird_defined("");
                    product.setFourth_tag(jsonObject2.getString("fourth_tag"));
                    product.setFourth_length(jsonObject2.getString("fourth_length"));
                    product.setFourth_type(jsonObject2.getString("fourth_type"));
                    product.setFourth_start_with("");
                    product.setFourth_defined("");
                    productArrayList.add(product);
                }
                databaseHelper.addProductsDetails(productArrayList);
                progressBar.setProgress(7);
                // get dth product data
                makeDTHProduct();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
        }
    }

    // DTH Product
    private void makeDTHProduct() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Processing)");
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
    private void parseDTHProductResponse(String response) {
        LogMessage.i("DTH Product Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Mobile product 2 : " + decryptedResponse);
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
                    product.setIs_partial(jsonObject2.getString("is_partial"));
                    product.setService_type("DTH");
                    product.setFirst_tag(jsonObject2.getString("first_tag"));
                    product.setFirst_length(jsonObject2.getString("first_length"));
                    product.setFirst_type(jsonObject2.getString("first_type"));
                    product.setFirst_defined("");
                    product.setSecond_tag(jsonObject2.getString("second_tag"));
                    product.setSecond_length(jsonObject2.getString("second_length"));
                    product.setSecond_type(jsonObject2.getString("second_type"));
                   product.setSecond_start_with("");
                    product.setSecond_defined("");
                    product.setThird_tag(jsonObject2.getString("third_tag"));
                    product.setThird_length(jsonObject2.getString("third_length"));
                    product.setThird_type(jsonObject2.getString("third_type"));
                    product.setThird_start_with("");
                    product.setThird_defined("");
                    product.setFourth_tag(jsonObject2.getString("fourth_tag"));
                    product.setFourth_length(jsonObject2.getString("fourth_length"));
                    product.setFourth_type(jsonObject2.getString("fourth_type"));
                    product.setFourth_start_with("");
                    product.setFourth_defined("");
                    productArrayList.add(product);
                }
                databaseHelper.addProductsDetails(productArrayList);
                progressBar.setProgress(14);
                // get state data
                makeElectricityProduct();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // DTH Product
    private void makeElectricityProduct() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Processing)");
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
                    product.setIs_partial(jsonObject2.getString("is_partial"));
                    product.setService_type("ELECTRICITY");
                    product.setFirst_tag(jsonObject2.getString("first_tag"));
                    product.setFirst_length(jsonObject2.getString("first_length"));
                    product.setFirst_type(jsonObject2.getString("first_type"));
                                       product.setFirst_defined("");

                    product.setSecond_tag(jsonObject2.getString("second_tag"));
                    product.setSecond_length(jsonObject2.getString("second_length"));
                    product.setSecond_type(jsonObject2.getString("second_type"));
                   product.setSecond_start_with("");
                    product.setSecond_defined("");
                    product.setThird_tag(jsonObject2.getString("third_tag"));
                    product.setThird_length(jsonObject2.getString("third_length"));
                    product.setThird_type(jsonObject2.getString("third_type"));
                    product.setThird_start_with("");
                    product.setThird_defined("");
                    product.setFourth_tag(jsonObject2.getString("fourth_tag"));
                    product.setFourth_length(jsonObject2.getString("fourth_length"));
                    product.setFourth_type(jsonObject2.getString("fourth_type"));
                    product.setFourth_start_with("");
                    product.setFourth_defined("");
                    productArrayList.add(product);
                }
                databaseHelper.addProductsDetails(productArrayList);
                progressBar.setProgress(21);
                // get state data
                makeGasProduct();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // GAS Product
    private void makeGasProduct() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Processing)");
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
                            "6",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_GAS_PRODUCT, response).sendToTarget();
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
    private void parseGasProductResponse(String response) {
        LogMessage.i("Gas Product Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Gas product : " + decryptedResponse);
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
                    product.setIs_partial(jsonObject2.getString("is_partial"));
                    product.setService_type("GAS");
                    product.setFirst_tag(jsonObject2.getString("first_tag"));
                    product.setFirst_length(jsonObject2.getString("first_length"));
                    product.setFirst_type(jsonObject2.getString("first_type"));
                                       product.setFirst_defined("");

                    product.setSecond_tag(jsonObject2.getString("second_tag"));
                    product.setSecond_length(jsonObject2.getString("second_length"));
                    product.setSecond_type(jsonObject2.getString("second_type"));
                   product.setSecond_start_with("");
                    product.setSecond_defined("");
                    product.setThird_tag(jsonObject2.getString("third_tag"));
                    product.setThird_length(jsonObject2.getString("third_length"));
                    product.setThird_type(jsonObject2.getString("third_type"));
                    product.setThird_start_with("");
                    product.setThird_defined("");
                    product.setFourth_tag(jsonObject2.getString("fourth_tag"));
                    product.setFourth_length(jsonObject2.getString("fourth_length"));
                    product.setFourth_type(jsonObject2.getString("fourth_type"));
                    product.setFourth_start_with("");
                    product.setFourth_defined("");
                    productArrayList.add(product);
                }
                databaseHelper.addProductsDetails(productArrayList);
                progressBar.setProgress(28);
                // get state data
                makeMobilePostpaidProduct();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // Mobile Postpaid Product
    private void makeMobilePostpaidProduct() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Processing)");
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
                            "22",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_MOBILE_POSTPAID_PRODUCT, response).sendToTarget();
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
    private void parseMobilePostpaidProductResponse(String response) {
        LogMessage.i("Mobile Postpaid Product Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Mobile Postpaid product : " + decryptedResponse);
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
                    product.setIs_partial(jsonObject2.getString("is_partial"));
                    product.setService_type("MOBILE_POSTPAID");
                    product.setFirst_tag(jsonObject2.getString("first_tag"));
                    product.setFirst_length(jsonObject2.getString("first_length"));
                    product.setFirst_type(jsonObject2.getString("first_type"));
                                       product.setFirst_defined("");

                    product.setSecond_tag(jsonObject2.getString("second_tag"));
                    product.setSecond_length(jsonObject2.getString("second_length"));
                    product.setSecond_type(jsonObject2.getString("second_type"));
                   product.setSecond_start_with("");
                    product.setSecond_defined("");
                    product.setThird_tag(jsonObject2.getString("third_tag"));
                    product.setThird_length(jsonObject2.getString("third_length"));
                    product.setThird_type(jsonObject2.getString("third_type"));
                    product.setThird_start_with("");
                    product.setThird_defined("");
                    product.setFourth_tag(jsonObject2.getString("fourth_tag"));
                    product.setFourth_length(jsonObject2.getString("fourth_length"));
                    product.setFourth_type(jsonObject2.getString("fourth_type"));
                    product.setFourth_start_with("");
                    product.setFourth_defined("");
                    productArrayList.add(product);
                }
                databaseHelper.addProductsDetails(productArrayList);
                progressBar.setProgress(35);
                // get state data
                makeWaterProduct();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // Water Product
    private void makeWaterProduct() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Processing)");
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
                            "11",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_WATER_PRODUCT, response).sendToTarget();
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
    private void parseWaterProductResponse(String response) {
        LogMessage.i("Water Product Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Water product : " + decryptedResponse);
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
                    product.setIs_partial(jsonObject2.getString("is_partial"));
                    product.setService_type("WATER");
                    product.setFirst_tag(jsonObject2.getString("first_tag"));
                    product.setFirst_length(jsonObject2.getString("first_length"));
                    product.setFirst_type(jsonObject2.getString("first_type"));
                                       product.setFirst_defined("");

                    product.setSecond_tag(jsonObject2.getString("second_tag"));
                    product.setSecond_length(jsonObject2.getString("second_length"));
                    product.setSecond_type(jsonObject2.getString("second_type"));
                   product.setSecond_start_with("");
                    product.setSecond_defined("");
                    product.setThird_tag(jsonObject2.getString("third_tag"));
                    product.setThird_length(jsonObject2.getString("third_length"));
                    product.setThird_type(jsonObject2.getString("third_type"));
                    product.setThird_start_with("");
                    product.setThird_defined("");
                    product.setFourth_tag(jsonObject2.getString("fourth_tag"));
                    product.setFourth_length(jsonObject2.getString("fourth_length"));
                    product.setFourth_type(jsonObject2.getString("fourth_type"));
                    product.setFourth_start_with("");
                    product.setFourth_defined("");
                    productArrayList.add(product);
                }

                databaseHelper.addProductsDetails(productArrayList);
                progressBar.setProgress(42);
                // get state data
                makeMobileCompany();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    /* [START] - 2017_04_28 - Add native code for update data, and Remove volley code */
    private void makeMobileCompany() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Done)\n" +
                "7/15) Mobile Companies (Processing)");
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.company;
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
                    myHandler.obtainMessage(SUCCESS_MOBILE_COMPANY, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseMobileCompanyResponse(String response) {
        LogMessage.i("Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Mobile company 1 : " + decryptedResponse);
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("company");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setId(jsonObject2.getString("id"));
                    company.setCompany_name(jsonObject2.getString("company_name"));
                    company.setLogo(jsonObject2.getString("logo"));
                    company.setService_type("Mobile");
                    companyArrayList.add(company);
                }
                databaseHelper.addCompanysDetails(companyArrayList);
                progressBar.setProgress(49);
                // get dth company data
                makeDTHCompany();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
        }
    }

    // DTH company
    private void makeDTHCompany() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Done)\n" +
                "7/15) Mobile Companies (Done)\n" +
                "8/15) DTH Companies (Processing)");
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.company;
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
                    myHandler.obtainMessage(SUCCESS_DTH_COMPANY, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseDTHCompanyResponse(String response) {
        LogMessage.i("DTH company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Mobile company 2 : " + decryptedResponse);
                companyArrayList.clear();
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("company");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setId(jsonObject2.getString("id"));
                    company.setCompany_name(jsonObject2.getString("company_name"));
                    company.setLogo(jsonObject2.getString("logo"));
                    company.setService_type("DTH");
                    companyArrayList.add(company);
                }
                databaseHelper.addCompanysDetails(companyArrayList);
                progressBar.setProgress(56);
                // get electricity product
                //makeState();
                makeElectricityCompany();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // Electricity company
    private void makeElectricityCompany() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Done)\n" +
                "7/15) Mobile Companies (Done)\n" +
                "8/15) DTH Companies (Done)\n" +
                "9/15) Electricity Companies (Processing)");
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.company;
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
                    myHandler.obtainMessage(SUCCESS_ELECTRICITY_COMPANY, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseElectricityCompanyResponse(String response) {
        LogMessage.i("Electricity company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Electricity company : " + decryptedResponse);
                companyArrayList.clear();
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("company");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setId(jsonObject2.getString("id"));
                    company.setCompany_name(jsonObject2.getString("company_name"));
                    company.setLogo(jsonObject2.getString("logo"));
                    company.setService_type("ELECTRICITY");
                    companyArrayList.add(company);
                }
                databaseHelper.addCompanysDetails(companyArrayList);
                progressBar.setProgress(63);
                // get mobile product
                makeGasCompany();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // GAS company
    private void makeGasCompany() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Done)\n" +
                "7/15) Mobile Companies (Done)\n" +
                "8/15) DTH Companies (Done)\n" +
                "9/15) Electricity Companies (Done)\n" +
                "10/15) Gas Companies (Processing)");
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.company;
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
                            "6",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_GAS_COMPANY, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseGasCompanyResponse(String response) {
        LogMessage.i("Gas company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Gas company : " + decryptedResponse);
                companyArrayList.clear();
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("company");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setId(jsonObject2.getString("id"));
                    company.setCompany_name(jsonObject2.getString("company_name"));
                    company.setLogo(jsonObject2.getString("logo"));
                    company.setService_type("GAS");
                    companyArrayList.add(company);
                }
                databaseHelper.addCompanysDetails(companyArrayList);
                progressBar.setProgress(70);
                // get mobile product
                makeMobilePostpaidCompany();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // Electricity company
    private void makeMobilePostpaidCompany() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Done)\n" +
                "7/15) Mobile Companies (Done)\n" +
                "8/15) DTH Companies (Done)\n" +
                "9/15) Electricity Companies (Done)\n" +
                "10/15) Gas Companies (Done)\n" +
                "11/15) Mobile Postpaid Companies (Processing)");
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.company;
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
                            "22",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_MOBILE_POSTPAID_COMPANY, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseMobilePostpaidCompanyResponse(String response) {
        LogMessage.i("Mobile postpaid company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Mobile postpaid company : " + decryptedResponse);
                companyArrayList.clear();
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("company");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setId(jsonObject2.getString("id"));
                    company.setCompany_name(jsonObject2.getString("company_name"));
                    company.setLogo(jsonObject2.getString("logo"));
                    company.setService_type("MOBILE_POSTPAID");
                    companyArrayList.add(company);
                }
                databaseHelper.addCompanysDetails(companyArrayList);
                progressBar.setProgress(77);
                // get mobile product
                makeWaterCompany();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // Electricity company
    private void makeWaterCompany() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                "2/15) DTH Services (Done)\n" +
                "3/15) Electricity Services (Done)\n" +
                "4/15) Gas Services (Done)\n" +
                "5/15) Mobile Postpaid Services (Done)\n" +
                "6/15) Water Services (Done)\n" +
                "7/15) Mobile Companies (Done)\n" +
                "8/15) DTH Companies (Done)\n" +
                "9/15) Electricity Companies (Done)\n" +
                "10/15) Gas Companies (Done)\n" +
                "11/15) Mobile Postpaid Companies (Done)\n" +
                "12/15) Water Companies (Processing)");
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.company;
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
                            "11",
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_WATER_COMPANY, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseWaterCompanyResponse(String response) {
        LogMessage.i("Water company Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("Water company : " + decryptedResponse);
                companyArrayList.clear();
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("company");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setId(jsonObject2.getString("id"));
                    company.setCompany_name(jsonObject2.getString("company_name"));
                    company.setLogo(jsonObject2.getString("logo"));
                    company.setService_type("WATER");
                    companyArrayList.add(company);
                }
                databaseHelper.addCompanysDetails(companyArrayList);
                progressBar.setProgress(84);
                // get mobile product
                makeState();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "DTH data not found").sendToTarget();
        }
    }

    // get State data
    private void makeState() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                        "2/15) DTH Services (Done)\n" +
                        "3/15) Electricity Services (Done)\n" +
                        "4/15) Gas Services (Done)\n" +
                        "5/15) Mobile Postpaid Services (Done)\n" +
                        "6/15) Water Services (Done)\n" +
                        "7/15) Mobile Companies (Done)\n" +
                        "8/15) DTH Companies (Done)\n" +
                        "9/15) Electricity Companies (Done)\n" +
                        "10/15) Gas Companies (Done)\n" +
                        "11/15) Mobile Postpaid Companies (Done)\n" +
                        "12/15) Water Companies (Done)\n" +
                        "13/15) States (Processing)");
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
    private void parseStateResponse(String response) {
        LogMessage.i("State Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_string = jsonObject.getString("data");
                String decryptedResponse = Constants.decryptAPI(context,encrypted_string);
                LogMessage.d("All state : " + decryptedResponse);
                JSONObject jsonObject1 = new JSONObject(decryptedResponse);
                JSONArray jsonArray = jsonObject1.getJSONArray("state");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    State state = new State();
                    state.setCircle_id(jsonObject2.getString("circle_id"));
                    state.setCircle_name(jsonObject2.getString("circle_name"));
                    stateArrayList.add(state);
                }
                if (stateArrayList.size() > 0) {
                    databaseHelper.addStatesDetails(stateArrayList);
                    progressBar.setProgress(91);
                }
                // get setting data (color and recharge order field data)
                makeJsonSetting();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            myHandler.obtainMessage(ERROR, "States data not found").sendToTarget();
        }
    }

    // get mobile company data after getting mobile company data
    private void makeJsonSetting() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                        "2/15) DTH Services (Done)\n" +
                        "3/15) Electricity Services (Done)\n" +
                        "4/15) Gas Services (Done)\n" +
                        "5/15) Mobile Postpaid Services (Done)\n" +
                        "6/15) Water Services (Done)\n" +
                        "7/15) Mobile Companies (Done)\n" +
                        "8/15) DTH Companies (Done)\n" +
                        "9/15) Electricity Companies (Done)\n" +
                        "10/15) Gas Companies (Done)\n" +
                        "11/15) Mobile Postpaid Companies (Done)\n" +
                        "12/15) Water Companies (Done)\n" +
                        "13/15) States (Done)\n" +
                        "14/15) Settings (Processing)");
        // create new thread for register user
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set OTP verification url
                    String url = URL.setting;
                    // Set parameters list in string array
                    String[] parameters = {
                            "otp_code",
                            "username",
                            "mac_address",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getOtp_code(),
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    parseSettingsResponse(response);
                } catch (Exception ex) {
                    LogMessage.e("Error in OTP verification user");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse dth company response
    private void parseSettingsResponse(String response) {
        LogMessage.i("Setting Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                myHandler.obtainMessage(SUCCESS_SETTING, response).sendToTarget();
            } else if (jsonObject.getString("status").equals("2") &&
                    TextUtils.equals(jsonObject.getString("msg").toLowerCase().trim(), "invalid details")) {
                myHandler.obtainMessage(ERROR_INVALID_DETAILS, jsonObject.getString("msg")).sendToTarget();
            } else {
                LogMessage.d("Setting fail. Status = " + jsonObject.getString("status"));
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        } catch (JSONException ex) {
            LogMessage.e("Error in parse setting response");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "Setting data not found").sendToTarget();
        }
    }

    // parse success dth company
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void parseSuccessSettingsResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {

                ArrayList<Color> colorArrayList = new ArrayList<Color>();
                ArrayList<PaymentGatewayModel> paymentGatewayArrayList = new ArrayList<PaymentGatewayModel>();

                if (jsonObject.has("data3")){
                    String encrypted_response2 = jsonObject.getString("data3");
                    String decrypted_response2 = Constants.decryptAPI(context,encrypted_response2);
                    LogMessage.i("Decoded Payment settings : " + decrypted_response2);

                    // parse payment data
                    JSONObject objectPayment = new JSONObject(decrypted_response2);
                    JSONArray jsonArrayPayment = objectPayment.getJSONArray("paymentgateway");
                    for (int i = 0; i < jsonArrayPayment.length(); i++) {
                        JSONObject object1 = jsonArrayPayment.getJSONObject(i);
                        PaymentGatewayModel color = new PaymentGatewayModel();
                        color.setId(object1.getString("id"));
                        color.setName(object1.getString("name"));
                        paymentGatewayArrayList.add(color);
                    }

                    if (paymentGatewayArrayList.size() > 0) {
                        databaseHelper.addPaymentGateway(paymentGatewayArrayList);
                        progressBar.setProgress(97);
                    }
                }

                String encrypted_response = jsonObject.getString("data2");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.i("Decoded settings : " + decrypted_response);

                String encrypted_response1 = jsonObject.getString("data");
                String decrypted_response1 = Constants.decryptAPI(context,encrypted_response1);
                LogMessage.d("Setting Response : " + decrypted_response1);



                // parse color data
                JSONObject object = new JSONObject(decrypted_response);
                JSONArray jsonArray = object.getJSONArray("color");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object1 = jsonArray.getJSONObject(i);
                    Color color = new Color();
                    color.setColor_name(object1.getString("name"));
                    color.setColo_value(object1.getString("value"));
                    colorArrayList.add(color);
                }

                // parse recharge control order data
                object = new JSONObject(decrypted_response1);
                jsonArray = object.getJSONArray("order");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object1 = jsonArray.getJSONObject(i);
                    if (object1.getString("name").compareTo("circle") == 0) {
                        sharedPreferences.edit().putString(constants.PREF_IS_CIRCLE_VISIBILITY, object1.getString("status")).commit();
                    }
                    /* [START] - 2017_05_30 - Add is credit parameter in recharge
                     * According to the status of isCredit set is credit check box in recharge screen
                     * If is credit status is 1 then display is credit check box other wise hide check box */
                    String isCreditValue = object1.getString("name");
                    if (TextUtils.equals(isCreditValue.toLowerCase(), "iscredit")) {
                        sharedPreferences.edit().putString(constants.PREF_IS_CREDIT_STATUS, object1.getString("status")).commit();
                    }
                    // save name1 visibility status in prefrence
                    String nameValue = object1.getString("name");
                    if (TextUtils.equals(nameValue.toLowerCase(), "name1")) {
                        sharedPreferences.edit().putString(constants.PREF_NAME_STATUS, object1.getString("status")).commit();
                    }
                    // [END]
                }
                // [END]
                if (colorArrayList.size() > 0) {
                    databaseHelper.addColors(colorArrayList);
                    progressBar.setProgress(97);
                }



            }

            makeBankNameCall();
        } catch (JSONException ex) {
            LogMessage.e("Error in parse settings response");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            myHandler.obtainMessage(ERROR, "Setting data not found").sendToTarget();
        }
    }

    private void makeBankNameCall() {
        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                        "2/15) DTH Services (Done)\n" +
                        "3/15) Electricity Services (Done)\n" +
                        "4/15) Gas Services (Done)\n" +
                        "5/15) Mobile Postpaid Services (Done)\n" +
                        "6/15) Water Services (Done)\n" +
                        "7/15) Mobile Companies (Done)\n" +
                        "8/15) DTH Companies (Done)\n" +
                        "9/15) Electricity Companies (Done)\n" +
                        "10/15) Gas Companies (Done)\n" +
                        "11/15) Mobile Postpaid Companies (Done)\n" +
                        "12/15) Water Companies (Done)\n" +
                        "13/15) States (Done)\n" +
                        "14/15) Settings (Done)\n" +
                        "15/15) Bank List (Processing)");
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.getBankName;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "bank_name"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION,
                            ""
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_DMT_BANK_NAME, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void parseSuccessBankNameResponse(String response) {

        LogMessage.i("Bank Name Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                if (jsonObject.getString("msg").equals("List generated")) {
                    String encrypted_response = jsonObject.getString("data");
                    LogMessage.e("AccountLedger : " + "encrypted_response : " + encrypted_response);
                    String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                    LogMessage.e("AccountLedger : " + "decrypted_response : " + decrypted_response);

                    JSONArray object = new JSONArray(decrypted_response);
                    if (object.length() > 0) {
                        mainBankNamesList = new ArrayList<DMTAddBenefitiaryBankName>();
                        for (int i = 0; i < object.length(); i++) {
                            JSONObject obj = object.getJSONObject(i);
                            bankNameModel = new DMTAddBenefitiaryBankName();
                            bankNameModel.setBank_id(obj.getString("bank_id"));
                            bankNameModel.setBank_name(obj.getString("bank_name"));
                            bankNameModel.setIfsc_code(obj.getString("ifsc_code"));
                            mainBankNamesList.add(bankNameModel);
                        }
                    }
                    if (mainBankNamesList.size() > 0) {
                        databaseHelper.addDmtBanks(mainBankNamesList);
                    }

                }
            }

            // Put code after update data
            progressBar.setProgress(100);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            new NotificationUtil(getContextInstance()).sendNotification("Update", "Data updated successfully.", simpleDateFormat.format(cal.getTime()));
            prefs.saveString(constants.PREF_UPDATE_DATE, DateTime.getDate());
            prefs.saveString(constants.PREF_UPDATE_TIME, DateTime.getTime());

            // [START] - Display data update date time
            String updateDate = prefs.retriveString(constants.PREF_UPDATE_DATE, "0");
            String updateTime = prefs.retriveString(constants.PREF_UPDATE_TIME, "0");
            if (TextUtils.equals(updateDate, "0")) {
                txtLastUpdate.setVisibility(View.GONE);
            } else {
                txtLastUpdate.setVisibility(View.VISIBLE);
                String convert_date = Constants.commonDateFormate(updateDate + " " + updateTime,"dd.MM.yyyy HH:mm:ss","dd-MMM-yyyy hh:mm aa");
                txtLastUpdate.setText("Last update : " + convert_date);
            }
            // [END]
            // [START] - Back to home after update
            if (TextUtils.equals(requireUpdate, "1")) {
                LogMessage.i("Back to home");
                if (getActivity() != null) {
                    getActivity().finish();
                }
                HomeActivity.callMainActivity();
            }
            // [END]

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Updating\n\n1/15) Mobile Services (Done)\n" +
                                        "2/15) DTH Services (Done)\n" +
                                        "3/15) Electricity Services (Done)\n" +
                                        "4/15) Gas Services (Done)\n" +
                                        "5/15) Mobile Postpaid Services (Done)\n" +
                                        "6/15) Water Services (Done)\n" +
                                        "7/15) Mobile Companies (Done)\n" +
                                        "8/15) DTH Companies (Done)\n" +
                                        "9/15) Electricity Companies (Done)\n" +
                                        "10/15) Gas Companies (Done)\n" +
                                        "11/15) Mobile Postpaid Companies (Done)\n" +
                                        "12/15) Water Companies (Done)\n" +
                                        "13/15) States (Done)\n" +
                                        "14/15) Settings (Done)\n" +
                                        "15/15) Bank List (Done)\n\nDone.");
                        homeButton.setVisibility(View.VISIBLE);
                    }
                });
            }

        } catch (JSONException e) {
            LogMessage.e("Cashbook : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            alertDialog = new AlertDialog.Builder(getContextInstance()).create();
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
                Utility.toast(getContextInstance(), message);
            } catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // display error in dialog and LogMessage out from application
    private void displayErrorDialog_Logout(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            alertDialog_Logout = new AlertDialog.Builder(getActivity())
                    .setTitle("Info!")
                    .setMessage(message + "\nPlease logout and login again.")
                    .setPositiveButton("Logout",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    sharedPreferences.edit().clear().commit();
                                    Intent intent1 = new Intent(getContextInstance(), LoginActivity.class);
                                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent1);
                                    UpdateData.this.getActivity().finish();
                                    sharedPreferences.edit().putBoolean(constants.LOGOUT, true).commit();
                                }
                            }
                    ).setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    alertDialog_Logout.dismiss();
                                }
                            }
                    ).create();
            alertDialog_Logout.show();
        } catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getContextInstance(), message + "\nPlease logout and login again.");
            } catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == SUCCESS_MOBILE_COMPANY) {
                parseMobileCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_DTH_COMPANY) {
                parseDTHCompanyResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_MOBILE_PRODUCT) {
                parseMobileProductResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_DTH_PRODUCT) {
                parseDTHProductResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_STATE) {
                parseStateResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_SETTING) {
                parseSuccessSettingsResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
                displayError(msg.obj.toString());
            } else if (msg.what == ERROR_INVALID_DETAILS) {
                displayErrorDialog_Logout(msg.obj.toString());
                displayError(msg.obj.toString());
            } else if (msg.what == SUCCESS_DMT_BANK_NAME) {
                parseSuccessBankNameResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_ELECTRICITY_SERVICE) {
                parseElectricityProductResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_ELECTRICITY_COMPANY) {
                parseElectricityCompanyResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_GAS_PRODUCT) {
                parseGasProductResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_GAS_COMPANY) {
                parseGasCompanyResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_MOBILE_POSTPAID_PRODUCT) {
                parseMobilePostpaidProductResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_MOBILE_POSTPAID_COMPANY) {
                parseMobilePostpaidCompanyResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_WATER_PRODUCT) {
                parseWaterProductResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_WATER_COMPANY) {
                parseWaterCompanyResponse(msg.obj.toString());
            }
        }
    };

    private void displayError(String error) {
        /* [START] - Display update date */
        String updateDate = prefs.retriveString(constants.PREF_UPDATE_DATE, "0");
        String updateTime = prefs.retriveString(constants.PREF_UPDATE_TIME, "0");
        if (TextUtils.equals(updateDate, "0")) {
            txtLastUpdate.setVisibility(View.GONE);
        } else {
            txtLastUpdate.setVisibility(View.VISIBLE);
            txtLastUpdate.setText("Last update : " + updateDate + " " + updateTime);
        }
        // [END]
        textView.setText("Error\n\n" + error);
        homeButton.setVisibility(View.VISIBLE);
    }
    // [END]
}
