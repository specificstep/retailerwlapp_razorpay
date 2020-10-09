package specificstep.com.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import specificstep.com.Adapters.CombineTabAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Fragments.DMTPaymentsList;
import specificstep.com.Fragments.DTHTransactionList;
import specificstep.com.Fragments.ElectricityTransactionList;
import specificstep.com.Fragments.GasTransactionList;
import specificstep.com.Fragments.MobilePostPaidTransactionList;
import specificstep.com.Fragments.MobilePrepaidTransactionList;
import specificstep.com.Fragments.WaterTransactionList;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

public class CombineTransactionListActivity extends AppCompatActivity {

    private CombineTabAdapter adapter;
    private TabLayout tabLayout;
    public static ViewPager viewPager;
    ImageButton back;
    String curr_pos = "";
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int ERROR = 1, SUCCESS_WALLET_LIST = 2;
    private AlertDialog alertDialog;

    //multi wallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    ArrayList<String> menuWallet;
    Dialog dialog;
    ImageButton imgWallet;
    private TransparentProgressDialog transparentProgressDialog;
    private boolean isDMT = false;
    private boolean isWATER = false;
    private boolean isGAS = false;
    private boolean isELECTRCITY = false;
    private boolean isMOBILEPOSTPAID = false;
    private boolean isMOBILEPREPAID = false;
    private boolean isDTH = false;
    ArrayList<String> adapterPos;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine_transaction_list);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        back = (ImageButton) findViewById(R.id.btnTransListBack);
        imgWallet = (ImageButton) findViewById(R.id.imgTransListWallet);
        databaseHelper = new DatabaseHelper(CombineTransactionListActivity.this);
        userArrayList = databaseHelper.getUserDetail();
        adapterPos = new ArrayList<>();
        dialog = new Dialog(CombineTransactionListActivity.this);
        try {
            adapter = new CombineTabAdapter(getSupportFragmentManager());
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.mobile_prepaid_id)) {
                    isMOBILEPREPAID = true;
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.dth_id)) {
                    isDTH = true;
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.dmt_id)) {
                    isDMT = true;
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.water_id)) {
                    isWATER = true;
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.electricity_id)) {
                    isELECTRCITY = true;
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.gas_id)) {
                    isGAS = true;
                } else if (Constants.serviceModelArrayList.get(i).getId().equals(Constants.mobile_postpaid_id)) {
                    isMOBILEPOSTPAID = true;
                }
            }

            Dlog.e("Combine: " + isDMT + "<>" + isWATER + "<>" + isELECTRCITY + "<>" + isGAS);
            if(isMOBILEPREPAID) {
                adapter.addFragment(new MobilePrepaidTransactionList(), Constants.KEY_MOB_PREPAID_TEXT);
                adapterPos.add(Constants.mobile_prepaid_id);
            }
            if(isMOBILEPOSTPAID) {
                adapter.addFragment(new MobilePostPaidTransactionList(), Constants.KEY_MOB_POSTPAID_TEXT);
                adapterPos.add(Constants.mobile_postpaid_id);
            }
            if(isDTH) {
                adapter.addFragment(new DTHTransactionList(), Constants.KEY_DTH_TEXT);
                adapterPos.add(Constants.dth_id);
            }
            if(isDMT) {
                adapter.addFragment(new DMTPaymentsList(), Constants.KEY_DMT_TEXT);
                adapterPos.add(Constants.dmt_id);
            }
            if(isELECTRCITY) {
                adapter.addFragment(new ElectricityTransactionList(), Constants.KEY_ELECTRICITY_TEXT);
                adapterPos.add(Constants.electricity_id);
            }
            if(isGAS) {
                adapter.addFragment(new GasTransactionList(), Constants.KEY_GAS_TEXT);
                adapterPos.add(Constants.gas_id);
            }
            if(isWATER) {
                adapter.addFragment(new WaterTransactionList(), Constants.KEY_WATER_TEXT);
                adapterPos.add(Constants.water_id);
            }

            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
            curr_pos = getIntent().getStringExtra("current_pos");
            for(int i=0;i<adapterPos.size();i++) {
                if(curr_pos.equals(adapterPos.get(i))) {
                    pos = i;
                }
            }
            if (curr_pos != null) {
                viewPager.setCurrentItem(pos);
            }
        } catch (Exception e) {
            Dlog.d("Combine Tran Error: " + e.toString());
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(CombineTransactionListActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });

        imgWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.checkInternet(CombineTransactionListActivity.this)) {
                    makeWalletCall();
                } else {
                    //Constants.showNoInternetDialog(CombineTransactionListActivity.this);
                }
            }
        });

    }

    //multi wallet 14-3-2019
    public void makeWalletCall() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.walletType;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
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
        Dlog.d("Wallet Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(CombineTransactionListActivity.this, encrypted_response);
                Dlog.d("Wallet : " + "decrypted_response : " + decrypted_response);
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
                    menuWallet.add(object.getString("wallet_name") + " : " + getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                }
                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                if (walletsModelList.size() > 0) {
                    Constants.showWalletPopup(CombineTransactionListActivity.this);
                }
            } else {
            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    private void displayErrorDialog(String message) {
        try {
            if (!alertDialog.isShowing()) {
                alertDialog.setTitle("Info!");
                alertDialog.setCancelable(false);
                alertDialog.setMessage(message);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(CombineTransactionListActivity.this, message);
            } catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(CombineTransactionListActivity.this, R.drawable.fotterloading);
            }
            if (transparentProgressDialog != null) {
                if (!transparentProgressDialog.isShowing()) {
                    transparentProgressDialog.show();
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
            if (transparentProgressDialog != null) {
                if (transparentProgressDialog.isShowing())
                    transparentProgressDialog.dismiss();
            }
        } catch (Exception ex) {
            LogMessage.e("Error in dismiss progress");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
