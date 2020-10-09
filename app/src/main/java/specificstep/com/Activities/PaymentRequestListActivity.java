package specificstep.com.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import specificstep.com.Adapters.PaymentRequestListAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.PaymentRequestListModel;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.Utility;

public class PaymentRequestListActivity extends AppCompatActivity {

    ImageView imgNoData;
    public static RecyclerView recyclerView;
    public static RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    Dialog dialogError;
    ImageButton btnBack, imgWallet;
    TextView txtTitle;
    private String strMacAddress, strUserName, strOtpCode, strRegistrationDateTime;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS_LOAD = 0, ERROR = 1, SUCCESS_WALLET_LIST = 2;

    ArrayList<PaymentRequestListModel> requestModelsList;
    PaymentRequestListModel requestModel;

    //multi wallet 3-5-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    ArrayList<String> menuWallet;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_request_list);
        initialize();
        try {
            if (Constants.checkInternet(PaymentRequestListActivity.this)) {
                makeWalletCall();
                makePaymentRequestListCall();
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }

    public void initialize() {

        recyclerView = (RecyclerView) findViewById(R.id.ll_recycler_payment_request_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(PaymentRequestListActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        imgNoData = (ImageView) findViewById(R.id.imgNoDataPaymentRequestList);
        alertDialog = new AlertDialog.Builder(PaymentRequestListActivity.this).create();
        dialogError = new Dialog(PaymentRequestListActivity.this);
        dialogError.requestWindowFeature(Window.FEATURE_NO_TITLE);
        databaseHelper = new DatabaseHelper(PaymentRequestListActivity.this);
        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        strRegistrationDateTime = userArrayList.get(0).getReg_date();
        btnBack = (ImageButton) findViewById(R.id.btnPaymentRequestDetailBack);
        txtTitle = (TextView) findViewById(R.id.txtPaymentRequestDetailTitle);
        imgWallet = (ImageButton) findViewById(R.id.imgPaymentRequestListWallet);

        txtTitle.setText("Payment Request List");
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //multi wallet 3-5-2019
    public void makeWalletCall() {
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
                    Dlog.d("  Error  : " + ex.getMessage());
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
                String decrypted_response = Constants.decryptAPI(PaymentRequestListActivity.this,encrypted_response);
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
                    imgWallet.setVisibility(View.VISIBLE);
                    imgWallet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Constants.checkInternet(PaymentRequestListActivity.this)) {
                                Constants.showWalletPopup(PaymentRequestListActivity.this);
                            } else {
                                //Constants.showNoInternetDialog(PaymentRequestListActivity.this);
                            }
                        }
                    });
                } else {
                    imgWallet.setVisibility(View.INVISIBLE);
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void makePaymentRequestListCall() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.paymentRequestList;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_LOAD, response).sendToTarget();
                }
                catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setVisibility(View.GONE);
                            imgNoData.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();

    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == SUCCESS_LOAD) {
                parseSuccessResponse(msg.obj.toString());
                dismissProgressDialog();
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if(msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    public void parseSuccessResponse(String response) {
        Dlog.d("payment request list Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(PaymentRequestListActivity.this,encrypted_response);
                Dlog.d("Payment Request List : " + "decrypted_response : " + decrypted_response);
                JSONArray array = new JSONArray(decrypted_response);
                if(array.length()>0) {
                    requestModelsList = new ArrayList<PaymentRequestListModel>();
                    for(int i=0;i<array.length();i++) {
                        JSONObject object = array.getJSONObject(i);
                        requestModel = new PaymentRequestListModel();
                        requestModel.setDatetime(object.getString("datetime"));
                        requestModel.setAmount(object.getString("amount"));
                        requestModel.setWallet_name(object.getString("wallet_name"));
                        requestModel.setDeposit_bank(object.getString("deposit_bank"));
                        requestModel.setStatus(object.getString("status"));
                        requestModel.setRemark(object.getString("remark"));
                        requestModel.setAdmin_remark(object.getString("admin_remark"));
                        requestModelsList.add(requestModel);
                    }
                    Collections.reverse(requestModelsList);
                    if(requestModelsList.size()>0) {
                        adapter = new PaymentRequestListAdapter(PaymentRequestListActivity.this,requestModelsList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE);
                        imgNoData.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                    }

                }

            }
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }
    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(PaymentRequestListActivity.this, R.drawable.fotterloading);
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

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            if (!alertDialog.isShowing()) {
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
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(PaymentRequestListActivity.this, message);
            }
            catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

}
