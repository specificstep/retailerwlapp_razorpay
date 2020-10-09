package specificstep.com.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Adapters.ComplainListAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Complain;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

public class ComplainReportFragment extends Fragment {

    private Context context;

    private ListView lv_complain_trans;
    ImageView imgNoData;

    private ArrayList<User> userArrayList;
    private DatabaseHelper databaseHelper;
    private String str_mac_address, str_user_name, str_otp_code;

    private int start = 0, end = 10;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_WALLET_LIST = 3;
    private AlertDialog alertDialog;

    private TransparentProgressDialog progressDialog;

    private ArrayList<Complain> complainArrayList;
    private ComplainListAdapter complainListAdapter;

    private View footerView;
    private View footerViewNoMoreData;
    boolean loadmoreFlage = false;
    private final String ACTIONBAR_TITLE = "Complain Report";
    String balance;
    View view;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    private Context getContextInstance() {
        if (context == null) {
            context = ComplainReportFragment.this.getActivity();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_balance_menu_main:
                if (Constants.checkInternet(getActivity())) {
                    if(Constants.walletsModelList.size()==0) {
                        makeWalletCall();
                    } else {
                        Constants.showWalletPopup(getActivity());
                    }
                } else {
                    //Constants.showNoInternetDialog(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_complain_report, null);
        mainActivity().getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.load_more_items, null);
        footerViewNoMoreData = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_no_moredata, null);

        databaseHelper = new DatabaseHelper(getContextInstance());
        userArrayList = databaseHelper.getUserDetail();
        str_mac_address = userArrayList.get(0).getDevice_id();
        str_user_name = userArrayList.get(0).getUser_name();
        str_otp_code = userArrayList.get(0).getOtp_code();

        init();

        showProgressDialog();
        /*call webservice only if user
        is connected with internet*/
        CheckConnection checkConnection = new CheckConnection();
        if (checkConnection.isConnectingToInternet(getContextInstance()) == true) {
            getComplainList();

        } else {
            dismissProgressDialog();
            Utility.toast(getContextInstance(), "Check your internet connection");
            lv_complain_trans.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
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
                    menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                }

                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                if(walletsModelList.size()>0) {
                    Constants.showWalletPopup(getActivity());
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        }
        catch(JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    // display error in dialog
    private void displayErrorDialog(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!((Activity) context).isFinishing()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Info!")
                            .setCancelable(false)
                            .setMessage(message)
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity().getSupportActionBar().setTitle("Complain Report");
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    Intent intent = new Intent(getActivity(), Main2Activity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void init() {

        lv_complain_trans = (ListView) view.findViewById(R.id.lv_complain_report_fragment_complain_trans);
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataComplain);

        complainArrayList = new ArrayList<Complain>();
        complainListAdapter = new ComplainListAdapter(getContextInstance(), complainArrayList);
        lv_complain_trans.setAdapter(complainListAdapter);
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (progressDialog == null) {
                progressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
            }
            if (progressDialog != null) {
                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }
            }
        } catch (Exception ex) {
            LogMessage.e("Error in show progress");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // dismiss progress dialog
    private void dismissProgressDialog() {
        try {
            if (progressDialog != null) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        } catch (Exception ex) {
            LogMessage.e("Error in dismiss progress");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private void getComplainList() {
        // create new thread for recent transaction
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set latest_recharge url6
                    String url = URL.complainList;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "user_type",
                            "start",
                            "end",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            str_user_name,
                            str_mac_address,
                            str_otp_code,
                            "4",
                            String.valueOf(start),
                            String.valueOf(end),
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    complainHandler.obtainMessage(SUCCESS, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in recent transaction native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            lv_complain_trans.setVisibility(View.GONE);
                            imgNoData.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }


    private Handler complainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                dismissProgressDialog();
                parseComplainResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayComplainDialog(msg.obj.toString());
            }
        }
    };

    private void displayComplainDialog(String message) {
        alertDialog = new AlertDialog.Builder(getContextInstance()).create();
        // Setting Dialog Title
        alertDialog.setTitle("Info!");
        // set cancelable
        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    private void parseComplainResponse(String response) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            String responseStatus = jsonObject.getString("status");

            if (responseStatus.equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.d("Response Complain List : " + decrypted_response);
                loadMoreData(decrypted_response);
            } else {
                final JSONObject finalJsonObject = jsonObject;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv_complain_trans.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                        displayComplainDialog(finalJsonObject.optString("msg"));
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
       /*Method : loadMoreData
          load data on scroll*/

    public void loadMoreData(String response) {

        try {
            JSONArray jsonArray = new JSONArray(response);

            if(jsonArray.length()>0) {
                lv_complain_trans.setVisibility(View.VISIBLE);
                imgNoData.setVisibility(View.GONE);

                if (jsonArray.length() < 10) {
                    removeFooterView();
                    lv_complain_trans.addFooterView(footerViewNoMoreData);
                    loadmoreFlage = true;
                } else {
                    if (start == 0) {
                        removeFooterView();
                        lv_complain_trans.addFooterView(footerView);
                    }
                    loadmoreFlage = false;
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Complain complain = new Complain();
                    complain.setComplain_id(object.optString("complain_id"));
                    complain.setTitle(object.optString("title"));
                    complain.setComplain_type(object.optString("complain_type"));
                    complain.setDescription(object.optString("description"));
                    complain.setReason_code(object.optString("reason_code"));
                    complain.setTransaction_id(object.optString("transaction_id"));
                    complain.setStatus(object.optString("status"));
                    complain.setComplain_status(object.optString("complain_status"));
                    complain.setRemarks(object.optString("remarks"));
                    complain.setCompnay_name(object.optString("company_name"));
                    complain.setCircle_name(object.optString("circle_name"));
                    complain.setMo_no(object.optString("mobile"));
                    complain.setAmount(object.optString("amount"));
                    complain.setService_id(object.optString("service_id"));
                    complain.setProduct_id(object.optString("product_id"));
                    complain.setOperator_code(object.optString("operator_code"));
                    complain.setCircle_code(object.optString("circle_code"));
                    complain.setTrans_date_time(object.optString("trans_date_time"));
                    complain.setRecharge_status(object.optString("recharge_status"));
                    complain.setRecharge_id(object.optString("recharge_id"));
                    complain.setReason(object.optString("reason"));
                    complain.setReason_id(object.optString("reason_id"));
                    complain.setTran_service_id(object.optString("tran_service_id"));

                    complainArrayList.add(complain);
                }

                complainListAdapter.notifyDataSetChanged();

                removeFooterView();
                lv_complain_trans.addFooterView(footerViewNoMoreData);
                loadmoreFlage = true;

            } else {
                lv_complain_trans.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            new AlertDialog.Builder(getContextInstance())
                    .setTitle("Alert!")
                    .setCancelable(false)
                    .setMessage(getResources().getString(R.string.alert_servicer_down))
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }

        lv_complain_trans.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if ((firstVisibleItem + visibleItemCount - 1) == complainArrayList.size() && !(loadmoreFlage)) {
                    loadmoreFlage = true;
                    start = start + 10;
                    end = 10;
                    getComplainList();
                }
            }
        });
    }

    private void removeFooterView() {
        int footerCount = lv_complain_trans.getFooterViewsCount();
        LogMessage.d("Footer Count All : " + footerCount);
        lv_complain_trans.removeFooterView(footerView);
        lv_complain_trans.removeFooterView(footerViewNoMoreData);
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

}
