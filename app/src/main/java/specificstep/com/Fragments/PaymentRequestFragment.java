package specificstep.com.Fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.PaymentRequestListActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.EnglishNumberToWords;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Default;
import specificstep.com.Models.PaymentRequestBankModel;
import specificstep.com.Models.PaymentRequestDepositBankModel;
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
public class PaymentRequestFragment extends Fragment {

    View view;
    Context context;
    FloatingActionButton fab;
    EditText add_request_amount, add_payment_date, add_cheque_no, add_transaction_id,
            add_remarks, add_branch;
    Spinner add_wallet_type, add_payment_method, add_deposit_bank, add_bank;
    Button add_submit, add_reset, btnBankDetail;
    CardView crdBankDetail;
    LinearLayout lnrAddAmountWords, lnrAddBank, lnrAddChequeNo, lnrAddBranch,
            lnrAddTransactionId, lnrAddRemarks, lnrAddBankDetail, lnrDepositBank;
    TextView txtAddNumberWords, txtDetailPayeeName, txtDetailBankName;
    ScrollView scrollView;
    TransparentProgressDialog transparentProgressDialog;
    String TAG = "PaymentRequestFragment :: ";
    private String strMacAddress, strUserName, strOtpCode, strRegistrationDateTime;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    AlertDialog alertDialog;
    private final int ERROR = 1, SUCCESSPAYMENTREQUEST = 2,
            SUCCESS_WALLET_LIST = 3, SUCCESS_DEPOSIT_BANK = 4, SUCCESS_BANK = 5;
    List<WalletsModel> walletsModelList;
    List<String> wallet;
    WalletsModel walletsModel;
    ArrayAdapter<String> adapterWallet;
    EnglishNumberToWords words;
    List<PaymentRequestDepositBankModel> depositBankModelList;
    PaymentRequestDepositBankModel depositBankModel;
    List<String> depositBankList;
    ArrayAdapter<String> adapterDepositBank;
    Calendar myCalendar;
    int selectDate = 0, selectMonth = 0, selectYear = 0;
    String[] paymentMethod = {"Cash", "Cheque/DD", "E-Transfer", "NEFT/RTGS"};
    ArrayAdapter<String> adapterPaymentMethod;
    List<PaymentRequestBankModel> bankModelList;
    PaymentRequestBankModel bankModel;
    List<String> bankList;
    ArrayAdapter<String> adapterBank;
    ArrayList<Default> defaultArrayList;
    String user_id;
    Dialog dialog;
    public static boolean walletFrom = false;

    public PaymentRequestFragment() {
        // Required empty public constructor
    }

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private MenuItem menuItem;
    ArrayList<String> menuWallet;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
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
                        walletFrom = true;
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_payment_request, container, false);
        hideKeyboardFrom(getActivity(),view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity().getSupportActionBar().setTitle("Payment Request");

        initialize();

        walletFrom = false;
        makeWalletCall();
        makeDepositBankCall();
        makeBankCall();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentRequestListActivity.class);
                startActivity(intent);
            }
        });

        defaultArrayList = databaseHelper.getDefaultSettings();
        user_id = defaultArrayList.get(0).getUser_id();

        add_deposit_bank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtDetailPayeeName.setText(depositBankModelList.get(position).getPayee_name());
                if(depositBankModelList.get(position).getAccount_type().equals("null")) {
                    txtDetailBankName.setText(depositBankModelList.get(position).getBank_name() + " - " +
                            depositBankModelList.get(position).getBranch_name() + "\nAccount No: " +
                            depositBankModelList.get(position).getAccount_number() + "\nIFSC Code: " +
                            depositBankModelList.get(position).getIfsc_code());
                } else {
                    txtDetailBankName.setText(depositBankModelList.get(position).getBank_name() + " - " +
                            depositBankModelList.get(position).getBranch_name() + " (" +
                            depositBankModelList.get(position).getAccount_type() + ")\nAccount No: " +
                            depositBankModelList.get(position).getAccount_number() + "\nIFSC Code: " +
                            depositBankModelList.get(position).getIfsc_code());
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnBankDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.scrollTo(0, (int)crdBankDetail.getY());
                hideKeyboardFrom(getActivity(),view);
            }
        });
        //[End]

        add_request_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0 && s.length()<10) {
                    txtAddNumberWords.setText(words.convert(Integer.parseInt(s.toString())));
                    lnrAddAmountWords.setVisibility(View.VISIBLE);
                } else {
                    lnrAddAmountWords.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        add_payment_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar mcurrentDate=Calendar.getInstance();
                final int year = mcurrentDate.get(Calendar.YEAR);
                final int month = mcurrentDate.get(Calendar.MONTH);
                final int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog mDatePicker =new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        String tempDate = selectedyear + "-" + (selectedmonth+1) + "-" + selectedday;
                        add_payment_date.setText(Constants.commonDateFormate(tempDate,"yyyy-MM-dd","dd-MMM-yyyy"));
                        selectDate = selectedday;
                        selectMonth = selectedmonth+1;
                        selectYear = selectedyear;
                    }
                },year, month, day);
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                if(!TextUtils.isEmpty(add_payment_date.getText().toString())) {
                    mDatePicker.updateDate(selectYear, selectMonth - 1, selectDate);
                }
                mDatePicker.show();

            }
        });

        add_payment_method.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    lnrAddBank.setVisibility(View.GONE);
                    lnrAddChequeNo.setVisibility(View.GONE);
                    lnrAddBranch.setVisibility(View.VISIBLE);
                    lnrAddTransactionId.setVisibility(View.GONE);
                    lnrAddRemarks.setVisibility(View.VISIBLE);
                } else if(position == 1) {
                    lnrAddBank.setVisibility(View.VISIBLE);
                    lnrAddChequeNo.setVisibility(View.VISIBLE);
                    lnrAddBranch.setVisibility(View.VISIBLE);
                    lnrAddTransactionId.setVisibility(View.GONE);
                    lnrAddRemarks.setVisibility(View.VISIBLE);
                } else if(position == 2){
                    lnrAddBank.setVisibility(View.GONE);
                    lnrAddChequeNo.setVisibility(View.GONE);
                    lnrAddBranch.setVisibility(View.GONE);
                    lnrAddTransactionId.setVisibility(View.VISIBLE);
                    lnrAddRemarks.setVisibility(View.VISIBLE);
                } else if(position == 3){
                    lnrAddBank.setVisibility(View.VISIBLE);
                    lnrAddChequeNo.setVisibility(View.GONE);
                    lnrAddBranch.setVisibility(View.GONE);
                    lnrAddTransactionId.setVisibility(View.VISIBLE);
                    lnrAddRemarks.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapterPaymentMethod = new ArrayAdapter<String>(getActivity(),
                R.layout.item_spinner, paymentMethod);
        add_payment_method.setAdapter(adapterPaymentMethod);

        add_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()) {
                    showDialog();
                }
            }
        });

        add_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_request_amount.setText("");
                txtAddNumberWords.setText("");
                add_payment_date.setText("");
                add_branch.setText("");
                add_remarks.setText("");
                add_cheque_no.setText("");
                add_transaction_id.setText("");

            }
        });

    }

    public void initialize() {

        add_request_amount = (EditText) view.findViewById(R.id.edtPaymentRequestAmount);
        add_payment_date = (EditText) view.findViewById(R.id.edtPaymentRequestPaymentDate);
        add_cheque_no = (EditText) view.findViewById(R.id.edtPaymentRequestChequeNo);
        add_transaction_id = (EditText) view.findViewById(R.id.edtPaymentRequestTransactionId);
        add_remarks = (EditText) view.findViewById(R.id.edtPaymentRequestRemarks);
        add_deposit_bank = (Spinner) view.findViewById(R.id.edtPaymentRequestDepositBank);
        add_bank = (Spinner) view.findViewById(R.id.spnPaymentRequestBank);
        add_branch = (EditText) view.findViewById(R.id.edtPaymentRequestBranch);
        add_wallet_type = (Spinner) view.findViewById(R.id.spnPaymentRequestWalletType);
        add_payment_method = (Spinner) view.findViewById(R.id.spnPaymentRequestPaymentMethod);
        add_submit = (Button) view.findViewById(R.id.btnPaymentRequestSubmit);
        add_reset = (Button) view.findViewById(R.id.btnPaymentRequestReset);
        txtAddNumberWords = (TextView) view.findViewById(R.id.txtPaymentRequestAmountWords);
        lnrAddAmountWords = (LinearLayout) view.findViewById(R.id.lnrAddAmountWords);
        lnrAddBank = (LinearLayout) view.findViewById(R.id.lnrPaymentRequestAddBank);
        lnrAddChequeNo = (LinearLayout) view.findViewById(R.id.lnrPaymentRequestAddChequeNo);
        lnrAddBranch = (LinearLayout) view.findViewById(R.id.lnrPaymentRequestAddBranch);
        lnrAddTransactionId = (LinearLayout) view.findViewById(R.id.lnrPaymentRequestAddTransactionId);
        lnrAddRemarks = (LinearLayout) view.findViewById(R.id.lnrPaymentRequestAddRemarks);
        lnrAddBankDetail = (LinearLayout) view.findViewById(R.id.lnrAddBankDetail);
        lnrDepositBank = (LinearLayout) view.findViewById(R.id.lnrPaymentRequestDepositBank);
        btnBankDetail = (Button) view.findViewById(R.id.btnPaymentRequestBankDetail);
        crdBankDetail = (CardView) view.findViewById(R.id.crdDepositBankDetail);
        scrollView = (ScrollView) view.findViewById(R.id.scrollViewPaymentRequest);
        txtDetailPayeeName = (TextView) view.findViewById(R.id.txtPaymentRequestDetailPayeeName);
        txtDetailBankName = (TextView) view.findViewById(R.id.txtPaymentRequestDetailBankName);
        fab = (FloatingActionButton) view.findViewById(R.id.fabPaymentRequest);

        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        words = new EnglishNumberToWords();
        myCalendar = Calendar.getInstance();
        transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);
        databaseHelper = new DatabaseHelper(getActivity());
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        strRegistrationDateTime = userArrayList.get(0).getReg_date();

    }

    public void makeDepositBankCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.addcompanybank;
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
                    myHandler.obtainMessage(SUCCESS_DEPOSIT_BANK, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("  Error  : "+ ex.getMessage() );

                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();

    }

    public void parseSuccessDepositBankResponse(String response) {

        LogMessage.e("Deposit Bank List Response : "+ response );

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("Message : "+ message );
                LogMessage.e("Message : "+ message );

                LogMessage.e("Deposit Bank List : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);

                LogMessage.e("Deposit Bank List : " + "decrypted_response : " + decrypted_response);

                JSONObject object = new JSONObject(decrypted_response);
                JSONArray array = object.getJSONArray("data");
                depositBankModelList = new ArrayList<PaymentRequestDepositBankModel>();
                if(array.length()>0) {
                    lnrDepositBank.setVisibility(View.VISIBLE);
                    depositBankList = new ArrayList<String>();
                    for(int i=0;i<array.length();i++) {
                        JSONObject object1 = array.getJSONObject(i);
                        depositBankModel = new PaymentRequestDepositBankModel();
                        depositBankModel.setId(object1.getString("id"));
                        depositBankModel.setBank_name(object1.getString("bank_name"));
                        depositBankModel.setBalance(object1.getString("balance"));
                        depositBankModel.setAccount_number(object1.getString("account_number"));
                        depositBankModel.setUser_id(object1.getString("user_id"));
                        depositBankModel.setPayee_name(object1.getString("payee_name"));
                        depositBankModel.setAccount_type(object1.getString("account_type"));
                        depositBankModel.setIfsc_code(object1.getString("ifsc_code"));
                        depositBankModel.setBranch_name(object1.getString("branch_name"));
                        depositBankModelList.add(depositBankModel);
                        depositBankList.add(depositBankModelList.get(i).toString());
                    }

                    try {
                        if (depositBankList.size() > 0) {
                            if(getActivity() != null) {
                                adapterDepositBank = new ArrayAdapter<String>(getActivity(),
                                        R.layout.item_spinner, depositBankList);
                                add_deposit_bank.setAdapter(adapterDepositBank);
                            }
                            lnrAddBankDetail.setVisibility(View.VISIBLE);
                            crdBankDetail.setVisibility(View.VISIBLE);
                            lnrDepositBank.setVisibility(View.VISIBLE);

                            txtDetailPayeeName.setText(depositBankModelList.get(0).getPayee_name());
                            if (depositBankModelList.get(0).getAccount_type().equals("null")) {
                                txtDetailBankName.setText(depositBankModelList.get(0).getBank_name() + " - " +
                                        depositBankModelList.get(0).getBranch_name() + "\nAccount No: " +
                                        depositBankModelList.get(0).getAccount_number() + "\nIFSC Code: " +
                                        depositBankModelList.get(0).getIfsc_code());
                            } else {
                                txtDetailBankName.setText(depositBankModelList.get(0).getBank_name() + " - " +
                                        depositBankModelList.get(0).getBranch_name() + " (" +
                                        depositBankModelList.get(0).getAccount_type() + ")\nAccount No: " +
                                        depositBankModelList.get(0).getAccount_number() + "\nIFSC Code: " +
                                        depositBankModelList.get(0).getIfsc_code());
                            }
                        } else {
                            lnrAddBankDetail.setVisibility(View.GONE);
                            crdBankDetail.setVisibility(View.GONE);
                            lnrDepositBank.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                } else {
                    lnrDepositBank.setVisibility(View.GONE);
                }

        } else {
            displayErrorDialog(jsonObject.getString("msg")+"");
        }
    }
        catch (JSONException e) {
        LogMessage.e("Deposit Bank List : " + "Error 4 : " + e.getMessage());
        e.printStackTrace();
    }

}

    public void makeBankCall() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.addbank;
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
                    myHandler.obtainMessage(SUCCESS_BANK, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("  Error  : "+ ex.getMessage() );

                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();

    }

    public void parseSuccessBankResponse(String response) {

        LogMessage.e("Bank List Response : " + response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("Message : " + message);
                LogMessage.e("Message : " + message);

                LogMessage.e("Bank List : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);

                LogMessage.e("Bank List : " + "decrypted_response : " + decrypted_response);

                JSONObject object = new JSONObject(decrypted_response);
                JSONArray array = object.getJSONArray("data");
                if (array.length() > 0) {
                    bankModelList = new ArrayList<PaymentRequestBankModel>();
                    bankList = new ArrayList<String>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object1 = array.getJSONObject(i);
                        bankModel = new PaymentRequestBankModel();
                        bankModel.setId(object1.getString("id"));
                        bankModel.setBank_name(object1.getString("bank_name"));
                        bankModel.setAdd_date(object1.getString("add_date"));
                        bankModel.setEdit_date(object1.getString("edit_date"));
                        bankModel.setIp_address(object1.getString("ip_address"));
                        bankModel.setCreated_by(object1.getString("created_by"));
                        bankModel.setUpdated_by(object1.getString("updated_by"));
                        bankModel.setStatus(object1.getString("status"));
                        bankModelList.add(bankModel);
                        bankList.add(bankModelList.get(i).toString());
                    }

                    try {
                        if (bankList.size() > 0) {
                            adapterBank = new ArrayAdapter<String>(getActivity(),
                                    R.layout.item_spinner, bankList);
                            add_bank.setAdapter(adapterBank);
                        }
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        } catch (JSONException e) {
            LogMessage.e("Bank List : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }


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
                wallet = new ArrayList<String>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    walletsModel = new WalletsModel();
                    walletsModel.setWallet_type(object.getString("wallet_type"));
                    walletsModel.setWallet_name(object.getString("wallet_name"));
                    walletsModel.setBalance(object.getString("balance"));
                    walletsModelList.add(walletsModel);
                    try {
                        wallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }

                //Add wallet list [Start]
                if(wallet.size()>0) {
                    adapterWallet = new ArrayAdapter<String>(getActivity(),
                            R.layout.item_spinner, wallet);
                    add_wallet_type.setAdapter(adapterWallet);
                }
                //[End]
                /*if(menuItem != null) {
                    if (walletsModelList.size() > 0) {
                        menuItem.setVisible(true);
                    } else {
                        menuItem.setVisible(false);
                    }
                }*/

                if(walletFrom && walletsModelList.size()>0) {
                    Constants.showWalletPopup(getActivity());
                }

            } else {
            }
        }
        catch(JSONException e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void makePaymentRequestCall() {

        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String to_bank_id = "", cheque_no = "", branch = "",
                            transaction_id = "", remarks = "", deposit_bank = "";
                    if(lnrAddBank.getVisibility() == View.VISIBLE) {
                        to_bank_id = bankModelList.get(add_bank.getSelectedItemPosition()).getId().toString();
                    } else {
                        to_bank_id = "";
                    }
                    if(lnrAddChequeNo.getVisibility() == View.VISIBLE) {
                        cheque_no = add_cheque_no.getText().toString();
                    } else {
                        cheque_no = "";
                    }
                    if(lnrAddBranch.getVisibility() == View.VISIBLE) {
                        branch = add_branch.getText().toString();
                    } else {
                        branch = "";
                    }
                    if(lnrAddTransactionId.getVisibility() == View.VISIBLE) {
                        transaction_id = add_transaction_id.getText().toString();
                    } else {
                        transaction_id = "";
                    }
                    if(lnrAddRemarks.getVisibility() == View.VISIBLE) {
                        remarks = add_remarks.getText().toString();
                    } else {
                        remarks = "";
                    }
                    if(depositBankModelList.size()>0) {
                        deposit_bank = depositBankModelList.get(add_deposit_bank.getSelectedItemPosition()).getId().toString();
                    } else {
                        deposit_bank = "";
                    }
                    // set cashBook url
                    String url = URL.addpaymentrequest;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "user_id",
                            "amount",
                            "bank_id",
                            "payment_date",
                            "branch_name",
                            "payment_mode",
                            "remarks",
                            "to_bank_id",
                            "cheque_no",
                            "tid"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            user_id,
                            add_request_amount.getText().toString(),
                            deposit_bank,
                            Constants.commonDateFormate(add_payment_date.getText().toString(),"dd-MMM-yyyy","yyyy-MM-dd"),
                            branch,
                            String.valueOf(add_payment_method.getSelectedItemPosition()),
                            remarks,
                            to_bank_id,
                            cheque_no,
                            transaction_id
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESSPAYMENTREQUEST, response).sendToTarget();
                }
                catch (Exception ex) {
                    Log.e(TAG, "  Error  : "+ ex.getMessage() );
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();

    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESSPAYMENTREQUEST) {
                dismissProgressDialog();
                parseSuccessPaymentRequestResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_DEPOSIT_BANK) {
                parseSuccessDepositBankResponse(msg.obj.toString());
            } else if(msg.what == SUCCESS_BANK) {
                parseSuccessBankResponse(msg.obj.toString());
            }
        }
    };
    // [END]

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            if (!alertDialog.isShowing()) {
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
            Log.e(TAG,"Error in error dialog");
            Log.e(TAG,"Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getActivity(), message);
            }
            catch (Exception e) {
                Log.e(TAG,"Error in toast message");
                Log.e(TAG,"ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);
            }
            if (transparentProgressDialog != null) {
                if (!transparentProgressDialog.isShowing()) {
                    transparentProgressDialog.show();
                }
            }
        }
        catch (Exception ex) {
            Log.e(TAG,"Error in show progress");
            Log.e(TAG,"Error : " + ex.getMessage());
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
            Log.e(TAG,"Error in dismiss progress");
            Log.e(TAG,"Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void parseSuccessPaymentRequestResponse(String response) {

        Log.e(TAG, "Payment Request Response : "+ response );

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                Log.e(TAG, "Message : "+ message );
                Log.e(TAG, "Message : "+ message );

                Log.e(TAG,"Payment Request : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);

                Log.e(TAG,"Payment Request : " + "decrypted_response : " + decrypted_response);

                JSONObject object = new JSONObject(decrypted_response);
                String msg = object.getString("data");
                displayErrorDialog(msg);
                add_request_amount.setText("");
                add_payment_date.setText("");
                add_branch.setText("");
                add_remarks.setText("");
                add_cheque_no.setText("");
                add_transaction_id.setText("");

            } else {
                displayErrorDialog(jsonObject.getString("msg")+"");
            }
        }
        catch (JSONException e) {
            Log.e(TAG,"Payment Request : " + "Error 4 : " + e.getMessage());
            Utility.toast(getActivity(), "No result found");
            e.printStackTrace();
        }

    }

    public void showDialog() {
        dialog.setContentView(R.layout.popup_payment_request);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.setTitle("Payment Details");
        Button cancel = (Button) dialog.findViewById(R.id.btnPopupPaymentCancel);
        Button submit = (Button) dialog.findViewById(R.id.btnPopupPaymentSubmit);
        TextView txtWallet = (TextView) dialog.findViewById(R.id.txtPopupPaymentWalletType);
        TextView txtAmount = (TextView) dialog.findViewById(R.id.txtPopupPaymentAmount);
        TextView txtBank = (TextView) dialog.findViewById(R.id.txtPopupPaymentBank);
        TextView txtNote = (TextView) dialog.findViewById(R.id.txtPopupPaymentNote);
        TextView txtMsg = (TextView) dialog.findViewById(R.id.txtPopupPaymentMsg);
        LinearLayout lnrNote = (LinearLayout) dialog.findViewById(R.id.lnrPopupPaymentNote);
        LinearLayout lnrBank = (LinearLayout) dialog.findViewById(R.id.lnrPopupPaymentBank);

        txtWallet.setText(add_wallet_type.getSelectedItem().toString());
        if(add_payment_method.getSelectedItemPosition() == 0) {
            txtAmount.setText("Branch: " + add_branch.getText().toString() + "\nDate: " +
                    add_payment_date.getText().toString() + "\nMethod: " +
                    add_payment_method.getSelectedItem().toString());
        } else if(add_payment_method.getSelectedItemPosition() == 1) {
            txtAmount.setText(add_bank.getSelectedItem().toString() + " - " + add_branch.getText().toString() +
                    "\nDate: " + add_payment_date.getText().toString() + "\nMethod: " +
                    add_payment_method.getSelectedItem().toString() + "\nCheque No: " +
                    add_cheque_no.getText().toString());
        } else if(add_payment_method.getSelectedItemPosition() == 2) {
            txtAmount.setText("Date: " +
                    add_payment_date.getText().toString() + "\nMethod: " +
                    add_payment_method.getSelectedItem().toString() + "\nTransaction Id: " +
                    add_transaction_id.getText().toString());
        } else if(add_payment_method.getSelectedItemPosition() == 3) {
            txtAmount.setText(add_bank.getSelectedItem().toString() + "\nDate: " +
                    add_payment_date.getText().toString() + "\nMethod: " +
                    add_payment_method.getSelectedItem().toString() + "\nTransaction Id: " +
                    add_transaction_id.getText().toString());
        }
        if(depositBankModelList.size()>0) {
            txtBank.setText(add_deposit_bank.getSelectedItem().toString());
            lnrBank.setVisibility(View.VISIBLE);
        } else {
            lnrBank.setVisibility(View.GONE);
        }
        txtMsg.setText("AMOUNT : " + add_request_amount.getText().toString());
        if(TextUtils.isEmpty(add_remarks.getText().toString())) {
            lnrNote.setVisibility(View.GONE);
        } else {
            txtNote.setText(add_remarks.getText().toString());
            lnrNote.setVisibility(View.VISIBLE);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePaymentRequestCall();
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public boolean valid() {

        try {
            if (TextUtils.isEmpty(add_request_amount.getText())) {
                Toast.makeText(getActivity(), "Please enter amount", Toast.LENGTH_LONG).show();
                return false;
            } else if (Integer.parseInt(add_request_amount.getText().toString()) < 500) {
                Toast.makeText(getActivity(), "Please enter minimum 500 Amount", Toast.LENGTH_LONG).show();
                return false;
            } else if (add_deposit_bank.getSelectedItem().equals("") || add_deposit_bank.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Please select deposit bank", Toast.LENGTH_LONG).show();
                return false;
            } else if (TextUtils.isEmpty(add_payment_date.getText())) {
                Toast.makeText(getActivity(), "Please select payment date", Toast.LENGTH_LONG).show();
                return false;
            } else if (add_payment_method.getSelectedItem().equals("") || add_payment_method.getSelectedItem() == null) {
                Toast.makeText(getActivity(), "Please select payment method", Toast.LENGTH_LONG).show();
                return false;
            } else if (add_payment_method.getSelectedItemPosition() == 0) {
                if (TextUtils.isEmpty(add_branch.getText())) {
                    Toast.makeText(getActivity(), "Please enter branch", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (add_payment_method.getSelectedItemPosition() == 1) {
                if (add_bank.getSelectedItem().equals("") || add_bank.getSelectedItem() == null) {
                    Toast.makeText(getActivity(), "Please select bank", Toast.LENGTH_LONG).show();
                    return false;
                } else if (TextUtils.isEmpty(add_cheque_no.getText())) {
                    Toast.makeText(getActivity(), "Please enter cheque no", Toast.LENGTH_LONG).show();
                    return false;
                } else if (TextUtils.isEmpty(add_branch.getText())) {
                    Toast.makeText(getActivity(), "Please enter branch", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (add_payment_method.getSelectedItemPosition() == 2) {
                if (TextUtils.isEmpty(add_transaction_id.getText())) {
                    Toast.makeText(getActivity(), "Please enter transaction id", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else if (add_payment_method.getSelectedItemPosition() == 3) {
                if (add_bank.getSelectedItem().equals("") || add_bank.getSelectedItem() == null) {
                    Toast.makeText(getActivity(), "Please select bank", Toast.LENGTH_LONG).show();
                    return false;
                } else if (TextUtils.isEmpty(add_transaction_id.getText())) {
                    Toast.makeText(getActivity(), "Please enter transaction id", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                return true;
            }
        }catch (Exception e) {
            System.out.println(e.toString());
        }

        return true;
    }

}
