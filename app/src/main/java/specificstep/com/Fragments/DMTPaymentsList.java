package specificstep.com.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import specificstep.com.Adapters.DMTPaymentListAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.DMTPaymentListModel;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class DMTPaymentsList extends Fragment implements View.OnClickListener{

    View view;
    ImageView imgNoData;
    public static RecyclerView recyclerView;
    public static RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    Dialog dialogError;

    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS_LOAD = 0, ERROR = 1;

    public static ArrayList<DMTPaymentListModel> mDmtPaymentListModelArrayList;
    DMTPaymentListModel paymentListModel;
    public boolean isVisible = false;
    private Context context;

    //filter control
    FloatingActionButton fat_filter;
    BottomSheetDialog dialog;
    Button cancel, search, reset;
    LinearLayout duration, durationChild, mobile, mobileChild;
    ImageView imgDuration/*, imgMobile*/;
    Spinner spnYear, spnMonth;
    //EditText edtMobile;
    TextView txtYear, txtMonth/*, txtMobile*/;
    int strYear = 0, strMonth = 0;
    //String strMobile = "";
    boolean monthSelected = false;
    public boolean searchState = false;
    public static boolean listState = false;

    int month, yer;
    int selected_year;
    int current_month, current_year;
    ArrayList<String> year_array;
    List<String> month_list;
    String month_array[];
    Calendar calendar;
    String /*str_mo_no = "", */str_year, str_month, str_selected_month, str_mac_address, str_user_name, str_otp_code, str_month_year, str_reg_date_time;

    public DMTPaymentsList() {
        // Required empty public constructor
    }

    private Context getContextInstance() {
        if (context == null) {
            context = DMTPaymentsList.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // set current fragment has its own options menu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dmtpayments_list, null);
        calendar = Calendar.getInstance();
        initialize();
        /*if (isVisible) {
            makeSearchSenderCall();
        }*/
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void setUserVisibleHint(final boolean isUserVisible) {
        super.setUserVisibleHint(isUserVisible);
        isVisible = isUserVisible;
        // when fragment visible to user and view is not null then enter here.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUserVisible && isResumed()/* && isResumed() && isAdded() && getUserVisibleHint()*/) {
                    Dlog.d("DMT User call");
                    makeSearchSenderCall();
                }
            }
        },500);
    }


    public void initialize() {

        recyclerView = (RecyclerView) view.findViewById(R.id.ll_recycler_payments_list);
        recyclerView.setHasFixedSize(true);
        //filter
        fat_filter = (FloatingActionButton) view.findViewById(R.id.fabDmt);
        dialog = new BottomSheetDialog(getContextInstance());
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataDmtTransactionList);
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        dialogError = new Dialog(getActivity());
        dialogError.requestWindowFeature(Window.FEATURE_NO_TITLE);
        databaseHelper = new DatabaseHelper(getActivity());
        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        str_reg_date_time = userArrayList.get(0).getReg_date();
        mDmtPaymentListModelArrayList = new ArrayList<DMTPaymentListModel>();

        fat_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilter();
            }
        });

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (str_reg_date_time != null) {
                Date d = sdf.parse(str_reg_date_time);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                month = (cal.get(Calendar.MONTH) + 1);
                yer = (cal.get(Calendar.YEAR) - 1);
                LogMessage.d("Year : " + String.valueOf(yer));
            } else {
                //Utility.toast(getContextInstance(), "Error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makeSearchSenderCall() {
        if(TextUtils.isEmpty(str_month_year)) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            String mon = (month+1) + "";
            if(mon.length() != 2) {
                str_month_year = year + "-" + "0" + mon;
            } else {
                str_month_year = year + "-" + mon;
            }
        }
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.transactionList;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            /*"mobile",*/
                            "mon_year"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            /*str_mo_no,*/
                            str_month_year
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_LOAD, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    dismissProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
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
            // super.handleMessage(msg);
            if (msg.what == SUCCESS_LOAD) {
                parseSuccessAddResponse(msg.obj.toString());
                dismissProgressDialog();
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            }
        }
    };

    // parse success response
    private void parseSuccessAddResponse(String response) {
        LogMessage.i("DMT payment list Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.e("AccountLedger : " + "Message : " + message);
                LogMessage.e("AccountLedger : " + "encrypted_response : " + encrypted_response);
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.e("AccountLedger : " + "decrypted_response : " + decrypted_response);

                JSONArray array = new JSONArray(decrypted_response);
                if (array.length() > 0) {
                    mDmtPaymentListModelArrayList = new ArrayList<DMTPaymentListModel>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        paymentListModel = new DMTPaymentListModel();
                        paymentListModel.setBeneficiary_first_name(object.getString("beneficiary_first_name"));
                        paymentListModel.setBeneficiary_last_name(object.getString("beneficiary_last_name"));
                        paymentListModel.setBeneficiary_mobile_number(object.getString("beneficiary_mobile_number"));
                        paymentListModel.setTblsendid(object.getString("tblsendid"));
                        paymentListModel.setSender_firstname(object.getString("sender_firstname"));
                        paymentListModel.setSender_lastname(object.getString("sender_lastname"));
                        paymentListModel.setSender_mobilenumber(object.getString("sender_mobilenumber"));
                        paymentListModel.setSender_altmobilenumber(object.getString("sender_altmobilenumber"));
                        paymentListModel.setSender_email_address(object.getString("sender_email_address"));
                        paymentListModel.setTbltransid(object.getString("tbltransid"));
                        paymentListModel.setTrans_id(object.getString("trans_id"));
                        paymentListModel.setTblsender_id(object.getString("tblsender_id"));
                        paymentListModel.setVendor_id(object.getString("vendor_id"));
                        paymentListModel.setAmount(object.getString("amount"));
                        paymentListModel.setTransaction_id(object.getString("transaction_id"));
                        paymentListModel.setProvider_id(object.getString("provider_id"));
                        paymentListModel.setBank(object.getString("bank"));
                        paymentListModel.setTblbeneficiary_id(object.getString("tblbeneficiary_id"));
                        paymentListModel.setTblapi_id(object.getString("tblapi_id"));
                        paymentListModel.setTbluser_id(object.getString("tbluser_id"));
                        paymentListModel.setApi_brid(object.getString("api_brid"));
                        paymentListModel.setTransaction_status(object.getString("transaction_status"));
                        paymentListModel.setAdd_date(object.getString("add_date"));
                        paymentListModel.setFees(object.getString("fees"));
                        paymentListModel.setGst(object.getString("gst"));
                        paymentListModel.setGst_unclaim(object.getString("gst_unclaim"));
                        paymentListModel.setTds(object.getString("tds"));
                        paymentListModel.setCom(object.getString("com"));
                        paymentListModel.setFirm_name(object.getString("firm_name"));
                        paymentListModel.setAccount_number(object.getString("account_number"));
                        mDmtPaymentListModelArrayList.add(paymentListModel);
                    }

                    if (mDmtPaymentListModelArrayList.size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        imgNoData.setVisibility(View.GONE);
                        adapter = new DMTPaymentListAdapter(getActivity(), mDmtPaymentListModelArrayList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                    }

                } else {
                    recyclerView.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
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
                transparentProgressDialog = new TransparentProgressDialog(getActivity(), R.drawable.fotterloading);
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
        } catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getActivity(), message);
            } catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    //filter popup
    public void showFilter() {

        dialog.setContentView(R.layout.popup_transaction_search_filter);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animation;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        FrameLayout bottomSheet = (FrameLayout) dialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        cancel = (Button) dialog.findViewById(R.id.btnTransSearchCancel);
        reset = (Button) dialog.findViewById(R.id.btnTransSearchReset);
        search = (Button) dialog.findViewById(R.id.btn_search_fragment_trans_search);
        duration = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchDuration);
        durationChild = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchDurationChild);
        mobile = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchMobile);
        mobileChild = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchMobileChild);
        mobile.setVisibility(View.GONE);
        mobileChild.setVisibility(View.GONE);
        imgDuration = (ImageView) dialog.findViewById(R.id.imgTransSearchDuration);
        //imgMobile = (ImageView) dialog.findViewById(R.id.imgTransSearchMobile);
        txtYear = (TextView) dialog.findViewById(R.id.txt_TrasactionSearch_SelectedYear);
        txtMonth = (TextView) dialog.findViewById(R.id.txt_TrasactionSearch_SelectedMonth);
        //txtMobile = (TextView) dialog.findViewById(R.id.txt_TrasactionSearch_SelectedMobileNo);
        spnYear = (Spinner) dialog.findViewById(R.id.sp_year_fragment_trans_search);
        spnMonth = (Spinner) dialog.findViewById(R.id.sp_month_fragment_trans_search);
        //edtMobile = (EditText) dialog.findViewById(R.id.edt_mo_no_fragment_trans_search);

        duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (durationChild.getVisibility() == View.VISIBLE) {
                    durationChild.setVisibility(View.GONE);
                    imgDuration.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_black_24dp));
                } else {
                    durationChild.setVisibility(View.VISIBLE);
                    imgDuration.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_remove));
                }
            }
        });

        /*mobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mobileChild.getVisibility() == View.VISIBLE) {
                    mobileChild.setVisibility(View.GONE);
                    imgMobile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_black_24dp));
                } else {
                    mobileChild.setVisibility(View.VISIBLE);
                    imgMobile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_remove));
                }
            }
        });*/

        year_array = new ArrayList<String>();
        month_list = new ArrayList<String>();
        month_array = getActivity().getResources().getStringArray(R.array.month_array);

        current_month = calendar.get(Calendar.MONTH) + 1;

        current_year = calendar.get(Calendar.YEAR);
        /*add years from registered year to current year*/
        if (yer != 0) {
            for (int i = yer; i <= current_year; i++) {
                year_array.add(String.valueOf(i));

            }
        } else {
            year_array.add(String.valueOf(current_year));
        }
        int current_yr_position = year_array.indexOf(String.valueOf(current_year));

        ArrayAdapter yearAdapter = new ArrayAdapter(getContextInstance(), R.layout.item_spinner, year_array);
        spnYear.setAdapter(yearAdapter);
        if (strYear == 0) {
            spnYear.setSelection(current_yr_position);
            selected_year = current_year;
        } else {
            spnYear.setSelection(strYear - 1);
            selected_year = Integer.parseInt(spnYear.getSelectedItem().toString());
        }

        ArrayAdapter monthAdapter = new ArrayAdapter(getContextInstance(), R.layout.item_spinner, month_list);
        spnMonth.setAdapter(monthAdapter);
        if (strMonth == 0) {
            spnMonth.setSelection(current_month - 1);
        } else {
            str_year = spnYear.getSelectedItem().toString();
            selected_year = Integer.parseInt(str_year);

            if (selected_year == current_year) {
                month_list.clear();
                for (int i = 0; i < current_month; i++) {
                    month_list.add(month_array[i]);
                }
                ArrayAdapter monthAdapter2 = new ArrayAdapter(getContextInstance(), R.layout.item_spinner, month_list);
                spnMonth.setAdapter(monthAdapter2);
                spnMonth.setSelection(current_month - 1);
            } else {
                month_list.clear();
                for (int i = month - 1; i <= 11; i++) {
                    month_list.add(month_array[i]);
                }
                ArrayAdapter monthAdapter1 = new ArrayAdapter(getContextInstance(), R.layout.item_spinner, month_list);
                spnMonth.setAdapter(monthAdapter1);
                spnMonth.setSelection(strMonth - 1);
            }
        }

        /*if (!strMobile.equals("")) {
            edtMobile.setText(strMobile);
            edtMobile.setSelection(edtMobile.getText().length());
        }*/

        /*set months from registered date to
        current date according to selected year*/
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
                    str_year = spnYear.getSelectedItem().toString();
                    selected_year = Integer.parseInt(str_year);

                    if (selected_year == current_year) {
                        month_list.clear();
                        for (int i = 0; i < current_month; i++) {
                            month_list.add(month_array[i]);
                        }
                        ArrayAdapter monthAdapter = new ArrayAdapter(getContextInstance(), R.layout.item_spinner, month_list);
                        spnMonth.setAdapter(monthAdapter);
                        spnMonth.setSelection(current_month - 1);

                    } else {
                        month_list.clear();
                        for (int i = month - 1; i <= 11; i++) {
                            month_list.add(month_array[i]);
                        }
                        ArrayAdapter monthAdapter = new ArrayAdapter(getContextInstance(), R.layout.item_spinner, month_list);
                        spnMonth.setAdapter(monthAdapter);
                        spnYear.setSelection(0);
                    }
                } catch (Exception e) {
                    Dlog.d("Transaction: " + e.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ((TextView) spnMonth.getSelectedView()).setTextColor(getActivity().getResources().getColor(R.color.colorPrimaryDark));
                    int selected_month_position = Arrays.asList(month_array).indexOf(spnMonth.getSelectedItem().toString());
                    str_month = String.valueOf(selected_month_position + 1);
                } catch (Exception e) {
                    Dlog.d("Transaction: " + e.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        search.setOnClickListener(this);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edtMobile.setText("");
                spnYear.setSelection(1);
                current_month = calendar.get(Calendar.MONTH) + 1;
                spnMonth.setSelection(current_month - 1);
                strYear = 0;
                strMonth = 0;
                //str_mo_no = "";
            }
        });

        dialog.show();

    }

    public boolean valid() {

        //str_mo_no = edtMobile.getText().toString();
        strYear = spnYear.getSelectedItemPosition() + 1;
        strMonth = spnMonth.getSelectedItemPosition() + 1;
        //strMobile = str_mo_no;

        if (str_month.length() != 2) {
            str_selected_month = "0" + str_month;
            str_month_year = str_year + "-" + str_selected_month;
        } else {
            str_selected_month = str_month;
            str_month_year = str_year + "-" + str_selected_month;
        }

        if (str_selected_month.contains("Select Month")) {
            Utility.toast(getContextInstance(), "Select Month");
            return false;
        } else if (str_year.isEmpty()) {
            Utility.toast(getContextInstance(), "Select year");
            return false;
        } /*else if(TextUtils.isEmpty(edtMobile.getText().toString())) {
            Utility.toast(getContextInstance(), "Please enter mobile no");
            return false;
        } */else {
            return true;
        }

    }

    @Override
    public void onClick(View v) {
        if (valid()) {
            searchState = true;
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            //str_mo_no = edtMobile.getText().toString();
            dialog.dismiss();
            strYear = spnYear.getSelectedItemPosition() + 1;
            strMonth = spnMonth.getSelectedItemPosition() + 1;
            //strMobile = str_mo_no;
            monthSelected = true;

            if (str_month.length() != 2) {
                str_selected_month = "0" + str_month;
                str_month_year = str_year + "-" + str_selected_month;
            } else {
                str_selected_month = str_month;
                str_month_year = str_year + "-" + str_selected_month;
            }
            mDmtPaymentListModelArrayList.clear();
            showProgressDialog();
            imgNoData.setVisibility(View.GONE);

            makeSearchSenderCall();

            listState = false;
        }
    }

}
