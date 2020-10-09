package specificstep.com.Activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import specificstep.com.Adapters.ComplainListAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.AccountLedgerModel;
import specificstep.com.Models.CashbookModel;
import specificstep.com.Models.Color;
import specificstep.com.Models.Complain;
import specificstep.com.Models.ComplainReasonModel;
import specificstep.com.Models.DMTPaymentListModel;
import specificstep.com.Models.Default;
import specificstep.com.Models.PaymentRequestListModel;
import specificstep.com.Models.Recharge;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

public class ListDetailActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_COMPLAIN = 3,
            SUCCESS_REASON = 4, SUCCESS_WALLET_LIST = 5;
    ImageButton back, wallet;
    ImageView img;
    ImageView imgStatus, img1;
    TextView txtRemark, txtDate, txtAmount, txtTitle, txtCompany,
            txtSender, txtTransactionId, txtOperatorId, txtTitleText;

    //receipt
    ImageView imgReceipt;
    ImageView imgStatusReceipt, imgReceipt1;
    TextView txtRemarkReceipt, txtDateReceipt, txtAmountReceipt,
            txtCompanyReceipt, txtSenderReceipt, txtTransactionIdReceipt,
            txtOperatorIdReceipt, txtTitleTextReceipt;

    LinearLayout lnrDownload;
    CashbookModel cashbookModels;
    AccountLedgerModel accountLedgerModel;
    Recharge recharge;
    PaymentRequestListModel paymentRequestModel;
    ArrayList<Color> colorArrayList;
    String _color_name, color_value;
    String from, receipt_type;
    List<Default> userList;
    DatabaseHelper databaseHelper;
    LinearLayout lnrComplain;
    private TransparentProgressDialog transparentProgressDialog;
    ArrayList<User> userArrayList;
    private android.app.AlertDialog alertDialog_1;

    //DMT detail
    LinearLayout lnrRecSearch, lnrDmt, lnrDmtAgain, lnrDmtDownload, lnrDmtFees, lnrDmtFirmName;
    ImageView imgDmtDetail, imgDmtStatus;
    TextView txtDmtStatus, txtDmtAmount, txtDmtDate, txtDmtSender,
            txtDmtBeneficiary, txtDmtBank, txtDmtOperatorId, txtDmtTransactionId,
            txtDmtFees, txtDmtGst, txtDmtTds, txtDmtcom, txtDmtFirmName;

    //dmt receipt detail
    LinearLayout lnrRecSearchReceipt, lnrDmtReceipt, lnrDmtFeesReceipt, lnrDmtFirmNameReceipt;
    ImageView imgDmtDetailReceipt, imgDmtStatusReceipt;
    TextView txtDmtStatusReceipt, txtDmtAmountReceipt, txtDmtDateReceipt, txtDmtSenderReceipt,
            txtDmtBeneficiaryReceipt, txtDmtBankReceipt, txtDmtOperatorIdReceipt, txtDmtTransactionIdReceipt,
            txtDmtFeesReceipt, txtDmtGstReceipt, txtDmtTdsReceipt, txtDmtcomReceipt, txtDmtFirmNameReceipt;

    DMTPaymentListModel dmtModel;

    LinearLayout lnrListDetail;
    private int start = 0, end = 10;

    BottomSheetDialog alertDialogBuilder;
    List<ComplainReasonModel> reasonModelList;
    ComplainReasonModel reasonModel;
    List<String> reasonList;
    private ArrayAdapter<String> adapterCircleName;

    //complain thread
    ListView recyclerViewComplain;
    LinearLayout lnrComplainThread;
    private ArrayList<Complain> complainArrayList;
    private ComplainListAdapter complainListAdapter;
    String paymentId = "", paymentTo = "", paymentType = "", receipt = "",
            payment_time = "";
    //multi wallet 27-5-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    ArrayList<String> menuWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_list_detail);

        initialize();
        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.checkInternet(ListDetailActivity.this)) {
                    if (Constants.walletsModelList.size() == 0) {
                        makeWalletCall();
                    } else {
                        Constants.showWalletPopup(ListDetailActivity.this);
                    }
                } else {
                    //Constants.showNoInternetDialog(ListDetailActivity.this);
                }
            }
        });
        try {

            from = getIntent().getStringExtra("from");
            receipt_type = getIntent().getStringExtra("receipt_type");
            if (from.equals("cashbook")) {
                //defer other details with dmt detail
                lnrDmt.setVisibility(View.GONE);
                lnrRecSearch.setVisibility(View.VISIBLE);

                txtTitle.setText("Payment Report Detail");
                txtTitleText.setText("Payment Detail");
                cashbookModels = getIntent().getExtras().getParcelable("classdata");
                txtOperatorId.setVisibility(View.GONE);
                lnrComplain.setVisibility(View.GONE);
                txtTransactionId.setText("Payment Id: " + cashbookModels.paymentId);
                txtCompany.setVisibility(View.GONE);

                String amount = "";
                if (Float.parseFloat(cashbookModels.amount) < 0) {
                    amount = cashbookModels.amount.substring(1, cashbookModels.amount.length());
                } else {
                    amount = cashbookModels.amount;
                }

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                userList = databaseHelper.getDefaultSettings();
                String userId = userList.get(0).getUser_id();

                int frompos = cashbookModels.paymentFrom.indexOf("-");
                String from = cashbookModels.paymentFrom.substring(0, frompos - 1);

                int topos = cashbookModels.paymentTo.indexOf("-");
                String to = cashbookModels.paymentTo.substring(0, topos - 1);

                img.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);

                if (userId.equals(from)) {
                    txtAmount.setText("- " + getResources().getString(R.string.Rs) + " " + amount);
                    txtAmount.setTextColor(getResources().getColor(R.color.colorBlack));
                    img1.setImageDrawable(getResources().getDrawable(R.drawable.send));
                    txtSender.setText(cashbookModels.paymentFrom);
                } else if (userId.equals(to)) {
                    txtAmount.setText("+ " + getResources().getString(R.string.Rs) + " " + amount);
                    txtAmount.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    img1.setImageDrawable(getResources().getDrawable(R.drawable.receive));
                    txtSender.setText(cashbookModels.paymentTo);
                }
                txtRemark.setText(cashbookModels.remarks.trim() + " Successfull");

                txtDate.setText(Constants.commonDateFormate(cashbookModels.dateTime.trim(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

                lnrDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentId = cashbookModels.paymentId;
                        paymentTo = cashbookModels.paymentTo;
                        paymentType = "normal";
                        receipt = "Payment Report Receipt";
                        payment_time = Constants.commonDateFormate(cashbookModels.dateTime, "yyyy-MM-dd HH:mm:ss", "HH_mm_ss");
                        if (Build.VERSION.SDK_INT >= 23) {
                            readContactPermission(cashbookModels.paymentTo, cashbookModels.paymentId + payment_time, "normal", receipt);
                        } else {
                            takeScreenshot(cashbookModels.paymentTo, cashbookModels.paymentId + payment_time, "normal", receipt);
                        }
                    }
                });

                //Receipt data

                txtTitleTextReceipt.setText("Payment Detail");
                cashbookModels = getIntent().getExtras().getParcelable("classdata");
                txtOperatorIdReceipt.setVisibility(View.GONE);
                txtTransactionIdReceipt.setText("Payment Id: " + cashbookModels.paymentId);
                txtCompanyReceipt.setVisibility(View.GONE);

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                userList = databaseHelper.getDefaultSettings();
                String userId1 = userList.get(0).getUser_id();

                int frompos1 = cashbookModels.paymentFrom.indexOf("-");
                String from1 = cashbookModels.paymentFrom.substring(0, frompos1 - 1);

                int topos1 = cashbookModels.paymentTo.indexOf("-");
                String to1 = cashbookModels.paymentTo.substring(0, topos1 - 1);

                imgReceipt.setVisibility(View.GONE);
                imgReceipt1.setVisibility(View.VISIBLE);

                if (userId1.equals(from1)) {
                    txtAmountReceipt.setText("- " + getResources().getString(R.string.Rs) + " " + cashbookModels.amount);
                    txtAmountReceipt.setTextColor(getResources().getColor(R.color.colorBlack));
                    imgReceipt1.setImageDrawable(getResources().getDrawable(R.drawable.send));
                    txtSenderReceipt.setText(cashbookModels.paymentFrom);
                } else if (userId1.equals(to1)) {
                    txtAmountReceipt.setText("+ " + getResources().getString(R.string.Rs) + " " + cashbookModels.amount);
                    txtAmountReceipt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    imgReceipt1.setImageDrawable(getResources().getDrawable(R.drawable.receive));
                    txtSenderReceipt.setText(cashbookModels.paymentTo);
                }
                txtRemarkReceipt.setText(cashbookModels.remarks.trim() + " Successfull");

                txtDateReceipt.setText(Constants.commonDateFormate(cashbookModels.dateTime.trim(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

            } else if (from.equals("acledger")) {
                //defer other details with dmt detail
                lnrDmt.setVisibility(View.GONE);
                lnrRecSearch.setVisibility(View.VISIBLE);

                txtTitle.setText("Account Ledger Detail");
                accountLedgerModel = getIntent().getExtras().getParcelable("classdata");

                txtTransactionId.setText("Payment Id : " + accountLedgerModel.payment_id);
                txtOperatorId.setText("Balance: " + getResources().getString(R.string.Rs) + " " + accountLedgerModel.balance);
                txtOperatorId.setVisibility(View.VISIBLE);
                txtCompany.setVisibility(View.GONE);
                lnrComplain.setVisibility(View.GONE);

                img.setVisibility(View.GONE);
                img1.setVisibility(View.VISIBLE);

                if (accountLedgerModel.cr_dr.equals("Credit")) {
                    txtAmount.setText("+ " + getResources().getString(R.string.Rs) + " " + accountLedgerModel.amount);
                    //rowHolder.txt_Adapter_acledger_balance.setText("Balance: " + balance);
                    txtAmount.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    img1.setImageDrawable(getResources().getDrawable(R.drawable.receive));
                } else {
                    txtAmount.setText("- " + getResources().getString(R.string.Rs) + " " + accountLedgerModel.amount);
                    //rowHolder.txt_Adapter_acledger_balance.setText("Balance: " + balance);
                    txtAmount.setTextColor(getResources().getColor(R.color.colorBlack));
                    img1.setImageDrawable(getResources().getDrawable(R.drawable.send));
                }

                if (accountLedgerModel.type.equalsIgnoreCase("Recharge")) {
                    txtRemark.setText("Recharge Successfull");
                    txtTitleText.setText("Recharge Detail");
                    txtSender.setText("To: " + accountLedgerModel.particular);
                } else if (accountLedgerModel.type.equalsIgnoreCase("Payment")) {
                    txtTitleText.setText("Payment Detail");
                    if (accountLedgerModel.cr_dr.equals("Credit")) {
                        txtRemark.setText("Payment Received Successfully");
                        txtSender.setText("To: " + accountLedgerModel.particular);
                    } else {
                        txtRemark.setText("Payment Sent Successfully");
                        txtSender.setText("From: " + accountLedgerModel.particular);
                    }
                } else if (accountLedgerModel.type.equalsIgnoreCase("DMT")) {
                    txtTitleText.setText(accountLedgerModel.particular + " Detail");
                    if (accountLedgerModel.cr_dr.equals("Credit")) {
                        txtRemark.setText(accountLedgerModel.particular + " Received Successfully");
                        txtSender.setText("From: " + accountLedgerModel.particular);
                    } else {
                        txtRemark.setText(accountLedgerModel.particular + " Sent Successfully");
                        txtSender.setText("To: " + accountLedgerModel.particular);
                    }
                }

                txtDate.setText(Constants.commonDateFormate(accountLedgerModel.created_date.trim(), "dd-MMM-yy hh:mm:ss aa", "dd-MMM-yyyy hh:mm aa"));

                lnrDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentId = accountLedgerModel.payment_id;
                        paymentTo = accountLedgerModel.particular;
                        paymentType = "normal";
                        receipt = "Account Ledger Receipt";
                        payment_time = Constants.commonDateFormate(accountLedgerModel.created_date, "dd-MMM-yy hh:mm:ss aa", "HH_mm_ss");
                        if (Build.VERSION.SDK_INT >= 23) {
                            readContactPermission(accountLedgerModel.particular, accountLedgerModel.payment_id + payment_time, "normal", receipt);
                        } else {
                            takeScreenshot(accountLedgerModel.particular, accountLedgerModel.payment_id + payment_time, "normal", receipt);
                        }
                    }
                });


                //receipt data

                accountLedgerModel = getIntent().getExtras().getParcelable("classdata");

                txtTransactionIdReceipt.setText("Payment Id : " + accountLedgerModel.payment_id);
                txtOperatorIdReceipt.setText("Balance: " + getResources().getString(R.string.Rs) + " " + accountLedgerModel.balance);
                txtOperatorIdReceipt.setVisibility(View.VISIBLE);
                txtCompanyReceipt.setVisibility(View.GONE);

                imgReceipt.setVisibility(View.GONE);
                imgReceipt1.setVisibility(View.VISIBLE);

                if (accountLedgerModel.cr_dr.equals("Credit")) {
                    txtAmountReceipt.setText("+ " + getResources().getString(R.string.Rs) + " " + accountLedgerModel.amount);
                    txtAmountReceipt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    imgReceipt1.setImageDrawable(getResources().getDrawable(R.drawable.receive));
                } else {
                    txtAmountReceipt.setText("- " + getResources().getString(R.string.Rs) + " " + accountLedgerModel.amount);
                    txtAmountReceipt.setTextColor(getResources().getColor(R.color.colorBlack));
                    imgReceipt1.setImageDrawable(getResources().getDrawable(R.drawable.send));
                }

                if (accountLedgerModel.type.equalsIgnoreCase("Recharge")) {
                    txtRemarkReceipt.setText("Recharge Successfull");
                    txtTitleTextReceipt.setText("Recharge Detail");
                    txtSenderReceipt.setText("To: " + accountLedgerModel.particular);
                } else if (accountLedgerModel.type.equalsIgnoreCase("Payment")) {
                    txtTitleTextReceipt.setText("Payment Detail");
                    if (accountLedgerModel.cr_dr.equals("Credit")) {
                        txtRemarkReceipt.setText("Payment Received Successfully");
                        txtSenderReceipt.setText("To: " + accountLedgerModel.particular);
                    } else {
                        txtRemarkReceipt.setText("Payment Sent Successfully");
                        txtSenderReceipt.setText("From: " + accountLedgerModel.particular);
                    }
                } else if (accountLedgerModel.type.equalsIgnoreCase("DMT")) {
                    txtTitleTextReceipt.setText(accountLedgerModel.particular + " Detail");
                    if (accountLedgerModel.cr_dr.equals("Credit")) {
                        txtRemarkReceipt.setText(accountLedgerModel.particular + " Received Successfully");
                        txtSenderReceipt.setText("From: " + accountLedgerModel.particular);
                    } else {
                        txtRemarkReceipt.setText(accountLedgerModel.particular + " Sent Successfully");
                        txtSenderReceipt.setText("To: " + accountLedgerModel.particular);
                    }
                }

                txtDateReceipt.setText(Constants.commonDateFormate(accountLedgerModel.created_date.trim(), "dd-MMM-yy hh:mm:ss aa", "dd-MMM-yyyy hh:mm aa"));

            } else if (from.equals("transsearch")) {
                //defer other details with dmt detail
                lnrDmt.setVisibility(View.GONE);
                lnrRecSearch.setVisibility(View.VISIBLE);

                getComplainList();

                txtTitle.setText("Transaction Search Detail");
                txtTitleText.setText("Transaction Detail");
                recharge = getIntent().getExtras().getParcelable("classdata");

                txtTransactionId.setText("Transaction Id: " + recharge.getClient_trans_id());
                txtSender.setText(recharge.getMo_no());
                txtCompany.setText(recharge.getCompnay_name() + " - " + recharge.getProduct_name());

                txtAmount.setText(getResources().getString(R.string.Rs) + " " + recharge.getAmount());

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                colorArrayList = databaseHelper.getAllColors();
                /*Set color of recharge status*/
                if (recharge.getRecharge_status().equalsIgnoreCase("success")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("success")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    if (recharge.getOperator_trans_id() != null) {
                        txtOperatorId.setVisibility(View.VISIBLE);
                        txtOperatorId.setText("Operator Id: " + recharge.getOperator_trans_id());
                    } else {
                        txtOperatorId.setVisibility(View.GONE);
                    }
                    lnrComplain.setVisibility(View.VISIBLE);
                    lnrDownload.setVisibility(View.VISIBLE);
                } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("pending")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorId.setVisibility(View.GONE);
                    lnrComplain.setVisibility(View.VISIBLE);
                    lnrDownload.setVisibility(View.GONE);
                } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("failure")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorId.setVisibility(View.GONE);
                    lnrComplain.setVisibility(View.GONE);
                    lnrDownload.setVisibility(View.GONE);
                }
                /* [START] - recharge_status":"Credit" */
                else if (recharge.getRecharge_status().equalsIgnoreCase("credit")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("credit")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorId.setVisibility(View.GONE);
                    lnrComplain.setVisibility(View.GONE);
                    lnrDownload.setVisibility(View.GONE);
                }
                // [END]

                img.setVisibility(View.VISIBLE);
                img1.setVisibility(View.GONE);

                String company_logo = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
                if (!TextUtils.isEmpty(company_logo)) {
                    Picasso.with(ListDetailActivity.this).load(company_logo).placeholder(R.drawable.placeholder_icon).into(img);
                } else {
                    img.setImageResource(R.drawable.placeholder_icon);
                }

                txtRemark.setText("Recharge " + recharge.getRecharge_status());
                txtDate.setText(Constants.commonDateFormate(recharge.getTrans_date_time().trim(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

                lnrComplain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeComplainListCall();
                    }
                });

                lnrDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentId = recharge.getClient_trans_id();
                        paymentTo = recharge.getMo_no();
                        paymentType = "normal";
                        receipt = receipt_type;
                        payment_time = Constants.commonDateFormate(recharge.getTrans_date_time(), "yyyy-MM-dd HH:mm:ss", "HH_mm_ss");
                        if (Build.VERSION.SDK_INT >= 23) {
                            readContactPermission(recharge.getMo_no(), recharge.getClient_trans_id() + payment_time, "normal", receipt);
                        } else {
                            takeScreenshot(recharge.getMo_no(), recharge.getClient_trans_id() + payment_time, "normal", receipt);
                        }
                    }
                });


                //Receipt detail
                txtTitleTextReceipt.setText("Transaction Detail");
                recharge = getIntent().getExtras().getParcelable("classdata");

                txtTransactionIdReceipt.setText("Transaction Id: " + recharge.getClient_trans_id());
                txtSenderReceipt.setText(recharge.getMo_no());
                txtCompanyReceipt.setText(recharge.getCompnay_name() + " - " + recharge.getProduct_name());

                txtAmountReceipt.setText(getResources().getString(R.string.Rs) + " " + recharge.getAmount());

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                colorArrayList = databaseHelper.getAllColors();
                /*Set color of recharge status*/
                if (recharge.getRecharge_status().equalsIgnoreCase("success")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("success")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    if (recharge.getOperator_trans_id() != null) {
                        txtOperatorIdReceipt.setVisibility(View.VISIBLE);
                        txtOperatorIdReceipt.setText("Operator Id: " + recharge.getOperator_trans_id());
                    } else {
                        txtOperatorIdReceipt.setVisibility(View.GONE);
                    }
                } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("pending")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("failure")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                }
                /* [START] - recharge_status":"Credit" */
                else if (recharge.getRecharge_status().equalsIgnoreCase("credit")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("credit")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                }
                // [END]

                imgReceipt.setVisibility(View.VISIBLE);
                imgReceipt1.setVisibility(View.GONE);

                String company_logo1 = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
                if (!TextUtils.isEmpty(company_logo1)) {
                    Picasso.with(ListDetailActivity.this).load(company_logo1).placeholder(R.drawable.placeholder_icon).into(imgReceipt);
                } else {
                    imgReceipt.setImageResource(R.drawable.placeholder_icon);
                }

                txtRemarkReceipt.setText("Recharge " + recharge.getRecharge_status());
                txtDateReceipt.setText(Constants.commonDateFormate(recharge.getTrans_date_time().trim(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

            } else if (from.equals("rectransact")) {
                //defer other details with dmt detail
                lnrDmt.setVisibility(View.GONE);
                lnrRecSearch.setVisibility(View.VISIBLE);

                getComplainList();

                txtTitle.setText("Recent Transaction Detail");
                txtTitleText.setText("Transaction Detail");
                recharge = getIntent().getExtras().getParcelable("classdata");

                txtTransactionId.setText("Transaction Id: " + recharge.getClient_trans_id());
                txtCompany.setText(recharge.getCompnay_name() + " - " + recharge.getProduct_name());
                txtSender.setText(recharge.getMo_no());

                txtAmount.setText(getResources().getString(R.string.Rs) + " " + recharge.getAmount());

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                colorArrayList = databaseHelper.getAllColors();
                /*Set color of recharge status*/
                if (recharge.getRecharge_status().equalsIgnoreCase("success")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("success")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    if (recharge.getOperator_trans_id() != null) {
                        txtOperatorId.setVisibility(View.VISIBLE);
                        txtOperatorId.setText("Operator Id: " + recharge.getOperator_trans_id());
                    } else {
                        txtOperatorId.setVisibility(View.GONE);
                    }
                    lnrComplain.setVisibility(View.VISIBLE);
                    lnrDownload.setVisibility(View.VISIBLE);
                } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("pending")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorId.setVisibility(View.GONE);
                    lnrComplain.setVisibility(View.VISIBLE);
                    lnrDownload.setVisibility(View.GONE);
                } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("failure")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorId.setVisibility(View.GONE);
                    lnrComplain.setVisibility(View.GONE);
                    lnrDownload.setVisibility(View.GONE);
                }
                /* [START] - recharge_status":"Credit" */
                else if (recharge.getRecharge_status().equalsIgnoreCase("credit")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("credit")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorId.setVisibility(View.GONE);
                    lnrComplain.setVisibility(View.GONE);
                    lnrDownload.setVisibility(View.GONE);
                }
                // [END]

                img.setVisibility(View.VISIBLE);
                img1.setVisibility(View.GONE);

                String company_logo = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
                if (!TextUtils.isEmpty(company_logo)) {
                    Picasso.with(ListDetailActivity.this).load(company_logo).placeholder(R.drawable.placeholder_icon).into(img);
                } else {
                    img.setImageResource(R.drawable.placeholder_icon);
                }

                txtRemark.setText("Recharge " + recharge.getRecharge_status());
                txtDate.setText(Constants.commonDateFormate(recharge.getTrans_date_time().trim(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

                lnrComplain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeComplainListCall();
                    }
                });

                lnrDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentId = recharge.getClient_trans_id();
                        paymentTo = recharge.getMo_no();
                        paymentType = "normal";
                        receipt = "Recent Transaction Receipt";
                        payment_time = Constants.commonDateFormate(recharge.getTrans_date_time(), "yyyy-MM-dd HH:mm:ss", "HH_mm_ss");
                        if (Build.VERSION.SDK_INT >= 23) {
                            readContactPermission(recharge.getMo_no(), recharge.getClient_trans_id() + payment_time, "normal", receipt);
                        } else {
                            takeScreenshot(recharge.getMo_no(), recharge.getClient_trans_id() + payment_time, "normal", receipt);
                        }
                    }
                });


                //Receipt detail
                txtTitleTextReceipt.setText("Transaction Detail");
                recharge = getIntent().getExtras().getParcelable("classdata");

                txtTransactionIdReceipt.setText("Transaction Id: " + recharge.getClient_trans_id());
                txtCompanyReceipt.setText(recharge.getCompnay_name() + " - " + recharge.getProduct_name());
                txtSenderReceipt.setText(recharge.getMo_no());

                txtAmountReceipt.setText(getResources().getString(R.string.Rs) + " " + recharge.getAmount());

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                colorArrayList = databaseHelper.getAllColors();
                /*Set color of recharge status*/
                if (recharge.getRecharge_status().equalsIgnoreCase("success")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("success")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    if (recharge.getOperator_trans_id() != null) {
                        txtOperatorIdReceipt.setVisibility(View.VISIBLE);
                        txtOperatorIdReceipt.setText("Operator Id: " + recharge.getOperator_trans_id());
                    } else {
                        txtOperatorIdReceipt.setVisibility(View.GONE);
                    }
                } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("pending")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("failure")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                }
                /* [START] - recharge_status":"Credit" */
                else if (recharge.getRecharge_status().equalsIgnoreCase("credit")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("credit")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                }
                // [END]

                imgReceipt.setVisibility(View.VISIBLE);
                imgReceipt1.setVisibility(View.GONE);

                String company_logo1 = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
                if (!TextUtils.isEmpty(company_logo1)) {
                    Picasso.with(ListDetailActivity.this).load(company_logo1).placeholder(R.drawable.placeholder_icon).into(imgReceipt);
                } else {
                    imgReceipt.setImageResource(R.drawable.placeholder_icon);
                }

                txtRemarkReceipt.setText("Recharge " + recharge.getRecharge_status());
                txtDateReceipt.setText(Constants.commonDateFormate(recharge.getTrans_date_time().trim(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

            } else if (from.equals("dmttransaction")) {
                //defer other details with dmt detail
                lnrDmt.setVisibility(View.VISIBLE);
                lnrRecSearch.setVisibility(View.GONE);

                txtTitle.setText("DMT Transaction Detail");
                dmtModel = getIntent().getExtras().getParcelable("classdata");

                if (dmtModel.getTransaction_id().equals("") ||
                        dmtModel.getTransaction_id() == null ||
                        dmtModel.getTransaction_id().equals("null")) {
                    txtDmtOperatorId.setVisibility(View.GONE);
                } else {
                    txtDmtOperatorId.setVisibility(View.VISIBLE);
                    txtDmtOperatorId.setText("Operator Id: " + dmtModel.getTransaction_id() + "");
                }

                if (dmtModel.getTrans_id().equals("") ||
                        dmtModel.getTrans_id() == null ||
                        dmtModel.getTrans_id().equals("null")) {
                    txtDmtTransactionId.setVisibility(View.GONE);
                } else {
                    txtDmtTransactionId.setVisibility(View.VISIBLE);
                    txtDmtTransactionId.setText("Transaction Id: " + dmtModel.getTrans_id() + "");
                }

                txtDmtDate.setText(Constants.commonDateFormate(dmtModel.getAdd_date(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));
                if (!TextUtils.isEmpty(dmtModel.getSender_lastname()) && !TextUtils.isEmpty(dmtModel.getSender_firstname())) {
                    txtDmtSender.setText(dmtModel.getSender_mobilenumber() + " (" + dmtModel.getSender_firstname() + " " + dmtModel.getSender_lastname() + ")");
                } else if (!TextUtils.isEmpty(dmtModel.getSender_lastname()) && TextUtils.isEmpty(dmtModel.getSender_firstname())) {
                    txtDmtSender.setText(dmtModel.getSender_mobilenumber() + " (" + dmtModel.getSender_firstname() + ")");
                } else {
                    txtDmtSender.setText(dmtModel.getSender_mobilenumber());
                }
                txtDmtBeneficiary.setText(dmtModel.getBeneficiary_mobile_number() + " (" + dmtModel.getBeneficiary_first_name() + " " + dmtModel.getBeneficiary_last_name() + ")");
                txtDmtBank.setText(dmtModel.getBank() + " (" + dmtModel.getAccount_number() + ")");
                txtDmtAmount.setText(getResources().getString(R.string.Rs) + " " + dmtModel.getAmount());
                if(dmtModel.getFirm_name() != null || !TextUtils.isEmpty(dmtModel.getFirm_name())) {
                    txtDmtFirmName.setText(dmtModel.getFirm_name());
                    lnrDmtFirmName.setVisibility(View.VISIBLE);
                } else {
                    lnrDmtFirmName.setVisibility(View.GONE);
                }

                if (!dmtModel.getTransaction_status().equals("null")) {
                    if (dmtModel.getTransaction_status().equals("1")) {
                        String statusString = "DMT Transaction " + "<b>Successfull</b>";
                        txtDmtStatus.setText(Html.fromHtml(statusString));
                        imgDmtStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                        imgDmtStatus.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorGreen), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmount.setTextColor(getResources().getColor(R.color.colorGreen));
                        lnrDmtDownload.setVisibility(View.VISIBLE);
                        lnrDmtFees.setVisibility(View.VISIBLE);
                        txtDmtFees.setText(dmtModel.getFees());
                        txtDmtGst.setText(dmtModel.getGst());
                        txtDmtTds.setText(dmtModel.getTds());
                        txtDmtcom.setText(dmtModel.getCom());
                    } else if (dmtModel.getTransaction_status().equals("0")) {
                        String statusString = "DMT Transaction " + "<b>Pending</b>";
                        txtDmtStatus.setText(Html.fromHtml(statusString));
                        imgDmtStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                        imgDmtStatus.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorDefault), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmount.setTextColor(getResources().getColor(R.color.colorDefault));
                        lnrDmtDownload.setVisibility(View.GONE);
                        lnrDmtFees.setVisibility(View.VISIBLE);
                        txtDmtFees.setText(dmtModel.getFees());
                        txtDmtGst.setText(dmtModel.getGst());
                        txtDmtTds.setText(dmtModel.getTds());
                        txtDmtcom.setText(dmtModel.getCom());
                    } else if (dmtModel.getTransaction_status().equals("2")) {
                        String statusString = "DMT Transaction " + "<b>Fail</b>";
                        txtDmtStatus.setText(Html.fromHtml(statusString));
                        imgDmtStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                        imgDmtStatus.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmount.setTextColor(getResources().getColor(R.color.colorRed));
                        lnrDmtDownload.setVisibility(View.GONE);
                        lnrDmtFees.setVisibility(View.GONE);
                    } else {
                        String statusString = "DMT Transaction " + "<b>Fail</b>";
                        txtDmtStatus.setText(Html.fromHtml(statusString));
                        imgDmtStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                        imgDmtStatus.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmount.setTextColor(getResources().getColor(R.color.colorRed));
                        lnrDmtDownload.setVisibility(View.GONE);
                        lnrDmtFees.setVisibility(View.GONE);
                    }
                    txtDmtStatus.setVisibility(View.VISIBLE);
                } else {
                    lnrDmtDownload.setVisibility(View.GONE);
                    txtDmtStatus.setVisibility(View.GONE);
                }

                lnrDmtDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        receipt = "DMT Transaction Receipt";
                        payment_time = Constants.commonDateFormate(dmtModel.getAdd_date(), "yyyy-MM-dd HH:mm:ss", "HH_mm_ss");
                        if (Build.VERSION.SDK_INT >= 23) {
                            String mobile = dmtModel.getSender_mobilenumber() + " (" + dmtModel.getSender_firstname() + " " + dmtModel.getSender_lastname();
                            paymentId = dmtModel.getTrans_id();
                            paymentTo = mobile;
                            paymentType = "dmt";
                            readContactPermission(mobile, dmtModel.getTrans_id() + payment_time, "dmt", receipt);
                        } else {
                            String mobile = dmtModel.getSender_mobilenumber() + " (" + dmtModel.getSender_firstname() + " " + dmtModel.getSender_lastname();
                            paymentId = dmtModel.getTrans_id();
                            paymentTo = mobile;
                            paymentType = "dmt";
                            takeScreenshot(mobile, dmtModel.getTrans_id() + payment_time, "dmt", receipt);
                        }
                    }
                });


                //receipt data

                dmtModel = getIntent().getExtras().getParcelable("classdata");

                if (dmtModel.getTransaction_id().equals("") ||
                        dmtModel.getTransaction_id() == null ||
                        dmtModel.getTransaction_id().equals("null")) {
                    txtDmtOperatorIdReceipt.setVisibility(View.GONE);
                } else {
                    txtDmtOperatorIdReceipt.setVisibility(View.VISIBLE);
                    txtDmtOperatorIdReceipt.setText("Operator Id: " + dmtModel.getTransaction_id() + "");
                }

                if (dmtModel.getTrans_id().equals("") ||
                         dmtModel.getTrans_id() == null ||
                        dmtModel.getTrans_id().equals("null")) {
                    txtDmtTransactionIdReceipt.setVisibility(View.GONE);
                } else {
                    txtDmtTransactionId.setVisibility(View.VISIBLE);
                    txtDmtTransactionIdReceipt.setText("Transaction Id: " + dmtModel.getTrans_id() + "");
                }

                txtDmtDateReceipt.setText(Constants.commonDateFormate(dmtModel.getAdd_date(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

                if (!dmtModel.getSender_lastname().equals("null")) {
                    txtDmtSenderReceipt.setText(dmtModel.getSender_mobilenumber() + " (" + dmtModel.getSender_firstname() + " " + dmtModel.getSender_lastname() + ")");
                } else {
                    txtDmtSenderReceipt.setText(dmtModel.getSender_mobilenumber() + " (" + dmtModel.getSender_firstname() + ")");
                }

                txtDmtBeneficiaryReceipt.setText(dmtModel.getBeneficiary_mobile_number() + " (" + dmtModel.getBeneficiary_first_name() + " " + dmtModel.getBeneficiary_last_name() + ")");
                txtDmtBankReceipt.setText(dmtModel.getBank() + " (" + dmtModel.getAccount_number() + ")");
                txtDmtAmountReceipt.setText(getResources().getString(R.string.Rs) + " " + dmtModel.getAmount());
                if(dmtModel.getFirm_name() != null || !TextUtils.isEmpty(dmtModel.getFirm_name())) {
                    txtDmtFirmNameReceipt.setText(dmtModel.getFirm_name());
                    lnrDmtFirmNameReceipt.setVisibility(View.VISIBLE);
                } else {
                    lnrDmtFirmNameReceipt.setVisibility(View.GONE);
                }

                if (!dmtModel.getTransaction_status().equals("null")) {
                    if (dmtModel.getTransaction_status().equals("1")) {
                        String statusString = "DMT Transaction " + "<b>Successfull</b>";
                        txtDmtStatusReceipt.setText(Html.fromHtml(statusString));
                        imgDmtStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                        imgDmtStatusReceipt.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorGreen), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmountReceipt.setTextColor(getResources().getColor(R.color.colorGreen));
                        lnrDmtFeesReceipt.setVisibility(View.VISIBLE);
                        txtDmtFeesReceipt.setText(dmtModel.getFees());
                        txtDmtGstReceipt.setText(dmtModel.getGst());
                        txtDmtTdsReceipt.setText(dmtModel.getTds());
                        txtDmtcomReceipt.setText(dmtModel.getCom());
                    } else if (dmtModel.getTransaction_status().equals("0")) {
                        String statusString = "DMT Transaction " + "<b>Pending</b>";
                        txtDmtStatusReceipt.setText(Html.fromHtml(statusString));
                        imgDmtStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                        imgDmtStatusReceipt.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorDefault), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmountReceipt.setTextColor(getResources().getColor(R.color.colorDefault));
                        lnrDmtFeesReceipt.setVisibility(View.VISIBLE);
                        txtDmtFeesReceipt.setText(dmtModel.getFees());
                        txtDmtGstReceipt.setText(dmtModel.getGst());
                        txtDmtTdsReceipt.setText(dmtModel.getTds());
                        txtDmtcomReceipt.setText(dmtModel.getCom());
                    } else if (dmtModel.getTransaction_status().equals("2")) {
                        String statusString = "DMT Transaction " + "<b>Fail</b>";
                        txtDmtStatusReceipt.setText(Html.fromHtml(statusString));
                        imgDmtStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                        imgDmtStatusReceipt.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmountReceipt.setTextColor(getResources().getColor(R.color.colorRed));
                        lnrDmtFeesReceipt.setVisibility(View.GONE);
                    } else {
                        String statusString = "DMT Transaction " + "<b>Fail</b>";
                        txtDmtStatusReceipt.setText(Html.fromHtml(statusString));
                        imgDmtStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                        imgDmtStatusReceipt.setColorFilter(ContextCompat.getColor(ListDetailActivity.this, R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
                        txtDmtAmountReceipt.setTextColor(getResources().getColor(R.color.colorRed));
                        lnrDmtFeesReceipt.setVisibility(View.GONE);
                    }
                    txtDmtStatusReceipt.setVisibility(View.VISIBLE);
                } else {
                    txtDmtStatusReceipt.setVisibility(View.GONE);
                }

            } else if (from.equals("paymentrequest")) {
                //defer other details with dmt detail
                lnrDmt.setVisibility(View.GONE);
                lnrRecSearch.setVisibility(View.VISIBLE);

                txtTitle.setText("Payment Request Detail");
                txtTitleText.setText("Payment Request Detail");
                paymentRequestModel = getIntent().getExtras().getParcelable("classdata");

                if (!paymentRequestModel.getRemark().equals("null")) {
                    txtTransactionId.setText("Transaction Id: " + paymentRequestModel.getRemark());
                    txtTransactionId.setVisibility(View.VISIBLE);
                } else {
                    txtTransactionId.setVisibility(View.GONE);
                }
                if (!paymentRequestModel.getDeposit_bank().equals("null")) {
                    txtCompany.setText(paymentRequestModel.getDeposit_bank());
                    txtCompany.setVisibility(View.VISIBLE);
                } else {
                    txtCompany.setVisibility(View.GONE);
                }

                if (!paymentRequestModel.getAdmin_remark().equals("null")) {
                    txtSender.setText(paymentRequestModel.getAdmin_remark());
                    txtSender.setVisibility(View.VISIBLE);
                } else {
                    txtSender.setVisibility(View.GONE);
                }

                if (!paymentRequestModel.getWallet_name().equals("null")) {
                    txtOperatorId.setText(paymentRequestModel.getWallet_name());
                    txtOperatorId.setVisibility(View.VISIBLE);
                } else {
                    txtOperatorId.setVisibility(View.GONE);
                }

                lnrComplain.setVisibility(View.GONE);
                txtRemark.setVisibility(View.GONE);

                if (!paymentRequestModel.getAmount().equals("null")) {
                    txtAmount.setText(getResources().getString(R.string.Rs) + " " + paymentRequestModel.getAmount());
                    txtAmount.setVisibility(View.VISIBLE);
                } else {
                    txtAmount.setVisibility(View.GONE);
                }

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                colorArrayList = databaseHelper.getAllColors();
                /*Set color of recharge status*/
                if (paymentRequestModel.getStatus().equalsIgnoreCase("success")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("success")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    lnrDownload.setVisibility(View.VISIBLE);
                } else if (paymentRequestModel.getStatus().equalsIgnoreCase("pending")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("pending")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    lnrDownload.setVisibility(View.GONE);
                } else if (paymentRequestModel.getStatus().equalsIgnoreCase("failure")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("failure")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    lnrDownload.setVisibility(View.GONE);
                }
                /* [START] - recharge_status":"Credit" */
                else if (paymentRequestModel.getStatus().equalsIgnoreCase("credit")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("credit")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmount.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatus.setColorFilter(android.graphics.Color.parseColor(color_value));
                    lnrDownload.setVisibility(View.GONE);
                }
                // [END]

                img1.setVisibility(View.VISIBLE);
                img.setVisibility(View.GONE);

                txtDate.setText(Constants.commonDateFormate(paymentRequestModel.getDatetime(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

                lnrDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paymentId = paymentRequestModel.getRemark();
                        paymentTo = paymentRequestModel.getDeposit_bank();
                        paymentType = "normal";
                        receipt = "Payment Request Receipt";
                        payment_time = Constants.commonDateFormate(paymentRequestModel.getDatetime(), "yyyy-MM-dd HH:mm:ss", "HH_mm_ss");
                        if (Build.VERSION.SDK_INT >= 23) {
                            readContactPermission(paymentRequestModel.getDeposit_bank(), paymentRequestModel.getRemark() + payment_time, "normal", receipt);
                        } else {
                            takeScreenshot(paymentRequestModel.getDeposit_bank(), paymentRequestModel.getRemark() + payment_time, "normal", receipt);
                        }
                    }
                });


                //Receipt detail
                txtTitleTextReceipt.setText("Payment Request Detail");
                paymentRequestModel = getIntent().getExtras().getParcelable("classdata");

                if (!paymentRequestModel.getRemark().equals("null")) {
                    txtTransactionIdReceipt.setText("Transaction Id: " + paymentRequestModel.getRemark());
                    txtTransactionIdReceipt.setVisibility(View.VISIBLE);
                } else {
                    txtTransactionIdReceipt.setVisibility(View.GONE);
                }
                if (!paymentRequestModel.getDeposit_bank().equals("null")) {
                    txtCompanyReceipt.setText(paymentRequestModel.getDeposit_bank());
                    txtCompanyReceipt.setVisibility(View.VISIBLE);
                } else {
                    txtCompanyReceipt.setVisibility(View.GONE);
                }

                if (!paymentRequestModel.getAdmin_remark().equals("null")) {
                    txtSenderReceipt.setText(paymentRequestModel.getAdmin_remark());
                    txtSenderReceipt.setVisibility(View.VISIBLE);
                } else {
                    txtSenderReceipt.setVisibility(View.GONE);
                }

                if (!paymentRequestModel.getWallet_name().equals("null")) {
                    txtOperatorIdReceipt.setText(paymentRequestModel.getWallet_name());
                    txtOperatorIdReceipt.setVisibility(View.VISIBLE);
                } else {
                    txtOperatorIdReceipt.setVisibility(View.GONE);
                }

                txtRemarkReceipt.setVisibility(View.GONE);

                if (!paymentRequestModel.getAmount().equals("null")) {
                    txtAmountReceipt.setText(getResources().getString(R.string.Rs) + " " + paymentRequestModel.getAmount());
                    txtAmountReceipt.setVisibility(View.VISIBLE);
                } else {
                    txtAmountReceipt.setVisibility(View.GONE);
                }

                databaseHelper = new DatabaseHelper(ListDetailActivity.this);
                colorArrayList = databaseHelper.getAllColors();
                /*Set color of recharge status*/
                if (paymentRequestModel.getStatus().equalsIgnoreCase("success")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("success")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                } else if (paymentRequestModel.getStatus().equalsIgnoreCase("pending")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("pending")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                } else if (paymentRequestModel.getStatus().equalsIgnoreCase("failure")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("failure")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                }
                /* [START] - recharge_status":"Credit" */
                else if (paymentRequestModel.getStatus().equalsIgnoreCase("credit")) {
                    for (int i = 0; i < colorArrayList.size(); i++) {
                        _color_name = colorArrayList.get(i).getColor_name();
                        if (_color_name.contains("credit")) {
                            color_value = colorArrayList.get(i).getColo_value();
                            txtAmountReceipt.setTextColor(android.graphics.Color.parseColor(color_value));
                        }
                    }
                    imgStatusReceipt.setImageDrawable(getResources().getDrawable(R.drawable.ic_pending));
                    imgStatusReceipt.setColorFilter(android.graphics.Color.parseColor(color_value));
                }
                // [END]

                txtDateReceipt.setText(Constants.commonDateFormate(paymentRequestModel.getDatetime(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

            }

        } catch (Exception e) {
            Dlog.d("List Detail Error: " + e.toString());
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    public void initialize() {

        lnrListDetail = (LinearLayout) findViewById(R.id.lnrListDetail);
        back = (ImageButton) findViewById(R.id.btnDetailBack);
        wallet = (ImageButton) findViewById(R.id.imgDetailWallet);
        img = (ImageView) findViewById(R.id.imgDetail);
        img1 = (ImageView) findViewById(R.id.imgDetail1);
        imgStatus = (ImageView) findViewById(R.id.imgRechargeStatusIcon);
        txtRemark = (TextView) findViewById(R.id.txtDetailRemark);
        txtDate = (TextView) findViewById(R.id.txtDetailDate);
        txtAmount = (TextView) findViewById(R.id.txtDetailAmount);
        txtTitle = (TextView) findViewById(R.id.txtDetailTitle);
        txtCompany = (TextView) findViewById(R.id.txtDetailCompany);
        txtSender = (TextView) findViewById(R.id.txtDetailSenderName);
        txtTransactionId = (TextView) findViewById(R.id.txtDetailTransactionId);
        txtOperatorId = (TextView) findViewById(R.id.txtDetailOperatorId);
        txtTitleText = (TextView) findViewById(R.id.txtDetailTitleText);
        lnrComplain = (LinearLayout) findViewById(R.id.lnrComplain);
        lnrDownload = (LinearLayout) findViewById(R.id.lnrDetailDownload);

        //receipt
        imgReceipt = (ImageView) findViewById(R.id.imgDetailRecept);
        imgReceipt1 = (ImageView) findViewById(R.id.imgDetailRecept1);
        imgStatusReceipt = (ImageView) findViewById(R.id.imgRechargeStatusIconRecept);
        txtRemarkReceipt = (TextView) findViewById(R.id.txtDetailRemarkRecept);
        txtDateReceipt = (TextView) findViewById(R.id.txtDetailDateRecept);
        txtAmountReceipt = (TextView) findViewById(R.id.txtDetailAmountRecept);
        txtCompanyReceipt = (TextView) findViewById(R.id.txtDetailCompanyRecept);
        txtSenderReceipt = (TextView) findViewById(R.id.txtDetailSenderNameRecept);
        txtTransactionIdReceipt = (TextView) findViewById(R.id.txtDetailTransactionIdRecept);
        txtOperatorIdReceipt = (TextView) findViewById(R.id.txtDetailOperatorIdRecept);
        txtTitleTextReceipt = (TextView) findViewById(R.id.txtDetailTitleTextRecept);

        databaseHelper = new DatabaseHelper(ListDetailActivity.this);
        userArrayList = new ArrayList<User>();
        userArrayList = databaseHelper.getUserDetail();

        //dmt detail
        lnrRecSearch = (LinearLayout) findViewById(R.id.lnrRecSearchDetail);
        lnrDmt = (LinearLayout) findViewById(R.id.lnrDmtDetail);
        lnrDmtAgain = (LinearLayout) findViewById(R.id.lnrDmtDetailRechargeAgain);
        lnrDmtDownload = (LinearLayout) findViewById(R.id.lnrDmtDetailDownload);
        imgDmtDetail = (ImageView) findViewById(R.id.imgDmtDetailCompany);
        imgDmtStatus = (ImageView) findViewById(R.id.imgDmtDetailIcon);
        txtDmtStatus = (TextView) findViewById(R.id.txtDmtDetailStatus);
        txtDmtAmount = (TextView) findViewById(R.id.txtDmtDetailAmount);
        txtDmtDate = (TextView) findViewById(R.id.txtDmtDetailDate);
        txtDmtSender = (TextView) findViewById(R.id.txtDmtDetailSenderName);
        txtDmtBeneficiary = (TextView) findViewById(R.id.txtDmtDetailBeneficiaryName);
        txtDmtBank = (TextView) findViewById(R.id.txtDmtDetailBankName);
        txtDmtOperatorId = (TextView) findViewById(R.id.txtDmtDetailOperatorId);
        txtDmtTransactionId = (TextView) findViewById(R.id.txtDmtDetailTransactionId);
        txtDmtFees = (TextView) findViewById(R.id.txtDmtDetailFees);
        txtDmtGst = (TextView) findViewById(R.id.txtDmtDetailGst);
        txtDmtTds = (TextView) findViewById(R.id.txtDmtDetailTds);
        txtDmtcom = (TextView) findViewById(R.id.txtDmtDetailCommission);
        txtDmtFirmName = (TextView) findViewById(R.id.txtDmtDetailBeneficiaryFirmName);
        lnrDmtFirmName = (LinearLayout) findViewById(R.id.lnrDmtDetailFirmName);
        lnrDmtFees = (LinearLayout) findViewById(R.id.lnrDmtDetailFees);

        //dmt receipt detail
        lnrRecSearchReceipt = (LinearLayout) findViewById(R.id.lnrRecSearchDetailRecept);
        lnrDmtReceipt = (LinearLayout) findViewById(R.id.lnrDmtDetailReceipt);
        imgDmtDetailReceipt = (ImageView) findViewById(R.id.imgDmtDetailCompanyReceipt);
        imgDmtStatusReceipt = (ImageView) findViewById(R.id.imgDmtDetailIconReceipt);
        txtDmtStatusReceipt = (TextView) findViewById(R.id.txtDmtDetailStatusReceipt);
        txtDmtAmountReceipt = (TextView) findViewById(R.id.txtDmtDetailAmountReceipt);
        txtDmtDateReceipt = (TextView) findViewById(R.id.txtDmtDetailDateReceipt);
        txtDmtSenderReceipt = (TextView) findViewById(R.id.txtDmtDetailSenderNameReceipt);
        txtDmtBeneficiaryReceipt = (TextView) findViewById(R.id.txtDmtDetailBeneficiaryNameReceipt);
        txtDmtBankReceipt = (TextView) findViewById(R.id.txtDmtDetailBankNameReceipt);
        txtDmtOperatorIdReceipt = (TextView) findViewById(R.id.txtDmtDetailOperatorIdReceipt);
        txtDmtTransactionIdReceipt = (TextView) findViewById(R.id.txtDmtDetailTransactionIdReceipt);
        txtDmtFeesReceipt = (TextView) findViewById(R.id.txtDmtDetailFeesReceipt);
        txtDmtGstReceipt = (TextView) findViewById(R.id.txtDmtDetailGstReceipt);
        txtDmtTdsReceipt = (TextView) findViewById(R.id.txtDmtDetailTdsReceipt);
        txtDmtcomReceipt = (TextView) findViewById(R.id.txtDmtDetailCommissionReceipt);
        lnrDmtFeesReceipt = (LinearLayout) findViewById(R.id.lnrDmtDetailFeesReceipt);
        txtDmtFirmNameReceipt = (TextView) findViewById(R.id.txtDmtDetailBeneficiaryFirmNameReceipt);
        lnrDmtFirmNameReceipt = (LinearLayout) findViewById(R.id.lnrDmtDetailFirmNameReceipt);

        alertDialogBuilder = new BottomSheetDialog(ListDetailActivity.this);

        //complain thread list
        recyclerViewComplain = (ListView) findViewById(R.id.lstListDetailComplain);
        lnrComplainThread = (LinearLayout) findViewById(R.id.lnrDetailComplain);

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
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
                String decrypted_response = Constants.decryptAPI(ListDetailActivity.this, encrypted_response);
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
                    wallet.setVisibility(View.VISIBLE);
                    wallet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Constants.checkInternet(ListDetailActivity.this)) {
                                Constants.showWalletPopup(ListDetailActivity.this);
                            } else {
                                //Constants.showNoInternetDialog(ListDetailActivity.this);
                            }
                        }
                    });
                } else {
                    wallet.setVisibility(View.INVISIBLE);
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getComplainList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.complainList;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "user_type",
                            "start",
                            "end",
                            "app",
                            "trans_id"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            "4",
                            String.valueOf(start),
                            String.valueOf(end),
                            Constants.APP_VERSION,
                            recharge.getClient_trans_id()
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    complainHandler.obtainMessage(SUCCESS_COMPLAIN, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                }
            }
        }).start();
    }

    private void parseComplainListResponse(String response) {
        Dlog.d("Complain List Response: " + response);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            String responseStatus = jsonObject.getString("status");

            if (responseStatus.equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(ListDetailActivity.this, encrypted_response);
                Dlog.d("Response Complain List : " + decrypted_response);
                loadMoreData(decrypted_response);
            } else {
                recyclerViewComplain.setVisibility(View.GONE);
                lnrComplainThread.setVisibility(View.GONE);
                //displayComplainDialog(jsonObject.optString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadMoreData(String response) {

        try {
            JSONArray jsonArray = new JSONArray(response);

            if (jsonArray.length() > 0) {
                recyclerViewComplain.setVisibility(View.VISIBLE);
                lnrComplainThread.setVisibility(View.VISIBLE);
                complainArrayList = new ArrayList<Complain>();
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

                if (complainArrayList.size() > 0) {
                    complainListAdapter = new ComplainListAdapter(ListDetailActivity.this, complainArrayList);
                    recyclerViewComplain.setAdapter(complainListAdapter);
                    setListViewHeightBasedOnChildren(recyclerViewComplain);
                    recyclerViewComplain.setVisibility(View.VISIBLE);
                    lnrComplainThread.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewComplain.setVisibility(View.GONE);
                    lnrComplainThread.setVisibility(View.GONE);
                }

            } else {
                recyclerViewComplain.setVisibility(View.GONE);
                lnrComplainThread.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            recyclerViewComplain.setVisibility(View.GONE);
            lnrComplainThread.setVisibility(View.GONE);
        }

    }

    private void addComplain(final String complainMessage, final String reason_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.complain;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "trans_id",
                            "provider_id",
                            "amount",
                            "mobile",
                            "reason_id",
                            "description",
                            "app"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            recharge.getClient_trans_id(),
                            "",
                            recharge.getAmount(),
                            recharge.getMo_no(),
                            reason_id,
                            complainMessage,
                            Constants.APP_VERSION

                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    complainHandler.obtainMessage(SUCCESS, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    complainHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // handle add complain messages
    private Handler complainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                dismissProgressDialog();
                parseComplainResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayComplainDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_COMPLAIN) {
                parseComplainListResponse(msg.obj.toString());
            }
        }
    };

    private void displayComplainDialog(String message) {
        alertDialog_1 = new android.app.AlertDialog.Builder(ListDetailActivity.this).create();
        alertDialog_1.setTitle("Info!");
        alertDialog_1.setCancelable(false);
        alertDialog_1.setMessage(message);
        alertDialog_1.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog_1.dismiss();
            }
        });
        alertDialog_1.show();
    }

    private void parseComplainResponse(String response) {
        Dlog.d("Latest Complain Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            String responseStatus = jsonObject.getString("status");

            if (responseStatus.equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(ListDetailActivity.this, encrypted_response);
                Dlog.d("Decrypted Complain Response  : " + decrypted_response);
                JSONObject object = new JSONObject(decrypted_response);
                String complain_id = object.getString("complain_id");
                displayComplainDialog(jsonObject.getString("msg") + " \nYour Complain Id is " + complain_id);
                getComplainList();
            } else {
                displayComplainDialog(jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toast(ListDetailActivity.this, "Please check your internet access");
        }
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(ListDetailActivity.this, R.drawable.fotterloading);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void readContactPermission(String mobile, String payment_id, String type, String receipt) {
        Dlog.d("Checking permission.");
        // BEGIN_INCLUDE(READ_CONTACTS)
        // Check if the READ_CONTACTS permission is already available.
        if (ActivityCompat.checkSelfPermission(ListDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Phone state permission has not been granted.
            requestReadContactPermission();
        } else {
            // Read SMS permissions is already available, show the camera preview.
            Dlog.d("Read contact permission has already been granted.");
            takeScreenshot(mobile, payment_id, type, receipt);
        }
        // END_INCLUDE(READ_PHONE_STATE)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Dlog.d("Received response for Read SMS permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Read SMS permission has been granted
                Dlog.d("Write external permission has now been granted.");
                // Ask user for grand READ_PHONE_STATE permission.
                readContactPermission(paymentTo, paymentId + payment_time, paymentType, receipt);
            } else {
                Dlog.d("Write external permission was NOT granted.");
                Utility.amountToast(ListDetailActivity.this, "Please grant the permission to download your receipt.");
                // again force fully prompt to user for grand permission.
                //readContactPermission();
            }
            // END_INCLUDE(permission_result)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestReadContactPermission() {
        LogMessage.i("Read phone state permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(READ_PHONE_STATE)
        if (ActivityCompat.shouldShowRequestPermissionRationale(ListDetailActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Dlog.d("Displaying READ_CONTACTS permission rationale to provide additional context.");
            // Force fully user to grand permission
            ActivityCompat.requestPermissions(ListDetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // READ_CONTACTS permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(ListDetailActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        // END_INCLUDE(READ_PHONE_STATE)
    }

    Bitmap bitmap;

    View v1 = null;

    private void takeScreenshot(String mobile, String payment_id, String type, String receipt) {
        final String folder_name = "/" + Constants.changeAppName(ListDetailActivity.this) + "/" + receipt + "/";
        try {
            File mydir = new File(Environment.getExternalStorageDirectory() + folder_name);
            if (!mydir.exists())
                mydir.mkdirs();
            else
                Dlog.d("error: dir. already exists");
            if (type.equals("normal")) {
                v1 = findViewById(R.id.lnrRecSearchDetailRecept);
            } else {
                v1 = findViewById(R.id.lnrDmtDetailReceipt);// get ur root view id
            }
            lnrDmtFeesReceipt.setVisibility(View.GONE);
            v1.setDrawingCacheEnabled(true);
            v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v1.layout(0, 0, v1.getMeasuredWidth(), v1.getMeasuredHeight());
            v1.buildDrawingCache(true);
            v1.setDrawingCacheBackgroundColor(getResources().getColor(R.color.colorWhite));
            bitmap = loadBitmapFromView(v1, v1.getWidth(), v1.getHeight());
            v1.setDrawingCacheEnabled(false);


            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(v1.getWidth(), v1.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            canvas.drawPaint(paint);

            bitmap = Bitmap.createScaledBitmap(bitmap, v1.getWidth(), v1.getHeight(), true);

            paint.setColor(android.graphics.Color.BLUE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);


            //save image in gallery
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 40, bytes);
            final File f = new File(Environment.getExternalStorageDirectory()
                    + File.separator + folder_name + File.separator + payment_id + ".pdf");
            if (f.exists()) {
//                openImage(f);
                openGeneratedPDF(f.getPath());
            } else {
                document.writeTo(new FileOutputStream(f));
                document.close();
                Snackbar snackbar = Snackbar
                        .make(lnrListDetail, "Payment Receipt download successfully.", Snackbar.LENGTH_LONG)
                        .setAction("View", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openGeneratedPDF(f.getPath());
                            }
                        });
                View view = snackbar.getView();
                TextView tv = (TextView) view.findViewById(R.id.snackbar_text);
                tv.setTextColor(android.graphics.Color.WHITE);
                snackbar.show();
            }
        } catch (Exception e) {
            Dlog.d("Download Receipt Error: " + e.toString());
            Toast.makeText(ListDetailActivity.this, "Payment Receipt download fail.", Toast.LENGTH_LONG).show();
        }
    }

    private void openGeneratedPDF(String path) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT <= 19) {
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
        } else {
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(ListDetailActivity.this, Constants.PACKAGE_NAME + ".provider", file);
            target.setDataAndType(uri, "application/pdf");
        }
        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    public void openImage(File f) {
        String file_name = f.getPath();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT <= 19) {
            intent.setDataAndType(Uri.parse("file://" + file_name), "image/*");
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(ListDetailActivity.this, Constants.PACKAGE_NAME + ".provider", f);
            intent.setDataAndType(uri, "image/*");
        }
        startActivity(intent);
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.draw(c);
        return b;
    }

    /* [START] - 2017_04_28 - Add native code for transaction search, and Remove volley code */
    private void makeComplainListCall() {
        databaseHelper = new DatabaseHelper(ListDetailActivity.this);
        userArrayList = databaseHelper.getUserDetail();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.complainReason;
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
                    myHandler.obtainMessage(SUCCESS_REASON, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // handle add complain messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                parseComplainResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_REASON) {
                parseComplainReasonResponse(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            new android.app.AlertDialog.Builder(ListDetailActivity.this)
                    .setTitle("Info!")
                    .setCancelable(false)
                    .setMessage(message)
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(ListDetailActivity.this, message);
            } catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // parse success response
    private void parseComplainReasonResponse(String response) {
        Dlog.d("Trans Search Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(ListDetailActivity.this, encrypted_response);
                Dlog.d("Complain List : " + decrypted_response);

                JSONArray array = new JSONArray(decrypted_response);
                if (array.length() > 0) {
                    reasonModelList = new ArrayList<ComplainReasonModel>();
                    reasonList = new ArrayList<String>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        reasonModel = new ComplainReasonModel();
                        reasonModel.setId(object.getString("id"));
                        reasonModel.setReason_detail(object.getString("reason_detail"));
                        reasonModelList.add(reasonModel);
                        reasonList.add(object.getString("reason_detail"));
                    }
                } else {
                    reasonList.add("Recharge Status Success but not get benefit");
                }
                showComplainDialog();
            } else {
                reasonList.add("Recharge Status Success but not get benefit");
                showComplainDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            reasonList.add("Recharge Status Success but not get benefit");
            showComplainDialog();
        }
    }

    public void showComplainDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    // get prompts.xml view
                    alertDialogBuilder = new BottomSheetDialog(ListDetailActivity.this);
                    alertDialogBuilder.setContentView(R.layout.complain_dialog);
                    FrameLayout bottomSheet = (FrameLayout) alertDialogBuilder.findViewById(R.id.design_bottom_sheet);
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    alertDialogBuilder.getWindow().getAttributes().windowAnimations = R.style.Animation;
                    alertDialogBuilder.setCanceledOnTouchOutside(false);
                    alertDialogBuilder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    TextView tv_order_id = (TextView) alertDialogBuilder.findViewById(R.id.tv_order_id_complain);
                    TextView tv_mo_no = (TextView) alertDialogBuilder.findViewById(R.id.tv_mo_no_complain);
                    TextView tv_amount = (TextView) alertDialogBuilder.findViewById(R.id.tv_amount_complain);
                    TextView tv_date_time = (TextView) alertDialogBuilder.findViewById(R.id.tv_date_time_complain);
                    TextView tv_status = (TextView) alertDialogBuilder.findViewById(R.id.tv_status_complain);
                    TextView tv_company_name = (TextView) alertDialogBuilder.findViewById(R.id.tv_company_name_complain);
                    ImageView iv_company_logo = (ImageView) alertDialogBuilder.findViewById(R.id.iv_company_complain);
                    TextView txtOperatorId = (TextView) alertDialogBuilder.findViewById(R.id.txt_operator_id_complain);

                    Button submit = (Button) alertDialogBuilder.findViewById(R.id.btn_report_complain_submit);
                    Button cancel = (Button) alertDialogBuilder.findViewById(R.id.btn_report_complain_cancel);
                    final EditText edt_report_complain = (EditText) alertDialogBuilder.findViewById(R.id.edt_report_complain);
                    LinearLayout llOperatorId = (LinearLayout) alertDialogBuilder.findViewById(R.id.ll_operator_id_complain);

                    final Spinner spinner = (Spinner) alertDialogBuilder.findViewById(R.id.spinner_complain);

                    if (reasonList.size() > 0) {
                        adapterCircleName = new ArrayAdapter<String>(ListDetailActivity.this, R.layout.adapter_spinner, reasonList);
                        // set adapter in circle spinner
                        spinner.setAdapter(adapterCircleName);
                    }

                    String order_add = "<b> Order Id: </b>" + recharge.getClient_trans_id();
                    tv_order_id.setText(Html.fromHtml(order_add));
                    tv_mo_no.setText(" " + recharge.getMo_no());
                    tv_amount.setText(Constants.addRsSymbol(ListDetailActivity.this, recharge.getAmount()));
                    tv_date_time.setText(Constants.commonDateFormate(recharge.getTrans_date_time(), "yyyy-MM-dd HH:mm:ss", "dd-MMM-yyyy hh:mm aa"));

                    /* [START] - Data proper not display ("Postpaid") (MAX - 10 character) */
                    // tv_product_name.setText(recharge.getProduct_name());
                    String productName = recharge.getProduct_name();
                    try {
                        if (productName.trim().length() > 10) {
                            String subProductName = productName.substring(0, 10);
                            tv_company_name.setText(recharge.getCompnay_name() + " - " + subProductName + "\n" + productName.substring(10, productName.length()));
                        } else {
                            tv_company_name.setText(recharge.getCompnay_name() + " - " + productName);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        tv_company_name.setText(recharge.getCompnay_name() + " - " + productName);
                    }
                    // [END]

                    /* [START] - Display operator id (If data not found, hide this field) */
                    if (recharge.getOperator_trans_id().trim().length() == 0
                            || recharge.getOperator_trans_id() == null
                            || recharge.getOperator_trans_id().trim().equalsIgnoreCase("null")) {
                        llOperatorId.setVisibility(View.GONE);
                    } else {
                        llOperatorId.setVisibility(View.VISIBLE);
                        String operator_add = "<b> Operator Id: </b>" + recharge.getOperator_trans_id();
                        txtOperatorId.setText(Html.fromHtml(operator_add));
                    }
                    // [END]

                    /*Set color of recharge status*/
                    if (recharge.getRecharge_status().equalsIgnoreCase("success")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("success")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge.getRecharge_status());
                    } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("pending")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge.getRecharge_status());
                    } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("failure")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge.getRecharge_status());
                    }
                    /* [START] - recharge_status":"Credit" */
                    else if (recharge.getRecharge_status().equalsIgnoreCase("credit")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("credit")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge.getRecharge_status());
                    }
                    // [END]

                    String company_logo = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
                    if (TextUtils.isEmpty(company_logo)) {
                        iv_company_logo.setImageResource(R.drawable.placeholder_icon);
                    } else {
                        Picasso.with(ListDetailActivity.this).load(company_logo).placeholder(R.drawable.placeholder_icon).into(iv_company_logo);
                    }

                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showProgressDialog();
                            /*call webservice only if user is connected with internet*/
                            CheckConnection checkConnection = new CheckConnection();
                            if (checkConnection.isConnectingToInternet(ListDetailActivity.this) == true) {
                                addComplain(edt_report_complain.getText().toString().trim(), reasonModelList.get(spinner.getSelectedItemPosition()).getId());
                            } else {
                                dismissProgressDialog();
                                Utility.toast(ListDetailActivity.this, "Check your internet connection");
                            }
                            Constants.isDialogOpen = false;
                            alertDialogBuilder.dismiss();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Constants.isDialogOpen = false;
                            alertDialogBuilder.dismiss();
                        }
                    });
                    alertDialogBuilder.show();

                }
            }
        });
    }

}
