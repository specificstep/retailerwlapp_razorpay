package specificstep.com.Fragments;


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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import specificstep.com.Activities.Main2Activity;
import specificstep.com.Adapters.SearchListAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Recharge;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterTransactionList extends Fragment implements View.OnClickListener, SearchListAdapter.SubmitComplainClickListener {
    private Context context;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_RECENT = 3;
    View view;
    ListView lstTransactionSearch;
    LinearLayout ll_recycler_view;
    String str_mo_no = "", str_year, str_month, str_selected_month, str_mac_address, str_user_name, str_otp_code, str_month_year, str_reg_date_time;
    Calendar calendar;
    DatabaseHelper databaseHelper;
    ArrayList<User> userArrayList;
    ArrayList<Recharge> rechargeArrayList;
    ArrayList<Recharge> beforeRefreshArrayList;
    private View footerView;
    private View footerViewNoMoreData;
    public static boolean loadmoreFlage = true;
    public static boolean loadmoreFlageRecent = true;
    private int start = 0, end = 10;
    boolean FLAG_INVALID_DETAIL = false;
    int count = 0;
    SearchListAdapter searchListAdapter;
    SharedPreferences sharedPreferences;
    Constants constants;
    private TransparentProgressDialog transparentProgressDialog;
    int month, yer;
    int selected_year;
    int current_month, current_year;
    ArrayList<String> year_array;
    List<String> month_list;
    String month_array[];

    ImageView imgNoData;

    private AlertDialog alertDialog_1;

    //filter control
    FloatingActionButton fat_filter;
    BottomSheetDialog dialog;
    Button cancel, search, reset;
    LinearLayout duration, durationChild, mobile, mobileChild;
    ImageView imgDuration, imgMobile;
    Spinner spnYear, spnMonth;
    EditText edtMobile;
    TextView txtYear, txtMonth, txtMobile;
    int strYear = 0, strMonth = 0;
    String strMobile = "";
    boolean monthSelected = false;
    public boolean searchState = false;
    public boolean isVisible = false;
    public static boolean listState = false;

    private Context getContextInstance() {
        if (context == null) {
            context = WaterTransactionList.this.getActivity();
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

    private String service_id = "";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trans_search, null);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Constants.serviceModelArrayList != null && Constants.serviceModelArrayList.size() > 0) {
            for (int i = 0; i < Constants.serviceModelArrayList.size(); i++) {
                if (Constants.serviceModelArrayList.get(i).getName().equalsIgnoreCase(Constants.KEY_WATER_TEXT)) {
                    service_id = Constants.serviceModelArrayList.get(i).getId();
                }
            }
        } else {
            service_id = Constants.water_id;
        }
        calendar = Calendar.getInstance();
        constants = new Constants();
        sharedPreferences = getActivity().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
        databaseHelper = new DatabaseHelper(getContextInstance());
        userArrayList = new ArrayList<User>();
        rechargeArrayList = new ArrayList<Recharge>();
        beforeRefreshArrayList = new ArrayList<Recharge>();
        userArrayList = databaseHelper.getUserDetail();
        str_mac_address = userArrayList.get(0).getDevice_id();
        str_user_name = userArrayList.get(0).getUser_name();
        str_otp_code = userArrayList.get(0).getOtp_code();
        str_reg_date_time = userArrayList.get(0).getReg_date();

        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataTranSearch);
        //filter
        fat_filter = (FloatingActionButton) view.findViewById(R.id.fabTransSearch);
        dialog = new BottomSheetDialog(getContextInstance());

        fat_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilter();
            }
        });

        /*get year   and month from registered date*/
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
                Utility.toast(getContextInstance(), "Error");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        init();
        /*if (isVisible) {
            start = 0;
            //makeNativeRecentTransaction();
            makeNativeTransactionSearch();
        }*/

        searchListAdapter = new SearchListAdapter(getContextInstance(), rechargeArrayList, this,Constants.KEY_WATER_TEXT);
        lstTransactionSearch.setAdapter(searchListAdapter);
        listState = true;

        //lstTransactionSearch.addFooterView(footerViewNoMoreData);

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
                    start = 0;
                    str_mo_no = "";
                    strMobile = "";
                    strMonth = 0;
                    strYear = 0;
                    str_month_year = "";
                    Dlog.d("Water User call");
                    makeNativeTransactionSearch();
                }
            }
        },500);
    }

    private void init() {
        lstTransactionSearch = (ListView) view.findViewById(R.id.lv_trans_search_fragment_trans_search);
        ll_recycler_view = (LinearLayout) view.findViewById(R.id.ll_recycler_view);
        footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.load_more_items, null);
        footerViewNoMoreData = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_no_moredata, null);
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
        TextView txtLabel = (TextView)dialog.findViewById(R.id.txtLabel);
        txtLabel.setText("Service Number");
        ImageView imgEditText = (ImageView)dialog.findViewById(R.id.imgEditText);
        imgEditText.setImageResource(R.drawable.tap);
        cancel = (Button) dialog.findViewById(R.id.btnTransSearchCancel);
        reset = (Button) dialog.findViewById(R.id.btnTransSearchReset);
        search = (Button) dialog.findViewById(R.id.btn_search_fragment_trans_search);
        duration = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchDuration);
        durationChild = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchDurationChild);
        mobile = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchMobile);
        mobileChild = (LinearLayout) dialog.findViewById(R.id.lnrTransSearchMobileChild);
        imgDuration = (ImageView) dialog.findViewById(R.id.imgTransSearchDuration);
        imgMobile = (ImageView) dialog.findViewById(R.id.imgTransSearchMobile);
        txtYear = (TextView) dialog.findViewById(R.id.txt_TrasactionSearch_SelectedYear);
        txtMonth = (TextView) dialog.findViewById(R.id.txt_TrasactionSearch_SelectedMonth);
        txtMobile = (TextView) dialog.findViewById(R.id.txt_TrasactionSearch_SelectedMobileNo);
        spnYear = (Spinner) dialog.findViewById(R.id.sp_year_fragment_trans_search);
        spnMonth = (Spinner) dialog.findViewById(R.id.sp_month_fragment_trans_search);
        edtMobile = (EditText) dialog.findViewById(R.id.edt_mo_no_fragment_trans_search);

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

        mobile.setOnClickListener(new View.OnClickListener() {
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
        });

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

        if (!strMobile.equals("")) {
            edtMobile.setText(strMobile);
            edtMobile.setSelection(edtMobile.getText().length());
        }

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
                edtMobile.setText("");
                spnYear.setSelection(1);
                current_month = calendar.get(Calendar.MONTH) + 1;
                spnMonth.setSelection(current_month - 1);
                strYear = 0;
                strMonth = 0;
                str_mo_no = "";
            }
        });

        dialog.show();

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

    public boolean valid() {

        str_mo_no = edtMobile.getText().toString();
        strYear = spnYear.getSelectedItemPosition() + 1;
        strMonth = spnMonth.getSelectedItemPosition() + 1;
        strMobile = str_mo_no;

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
            Utility.toast(getContextInstance(), "Please enter service no");
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
            str_mo_no = edtMobile.getText().toString();
            dialog.dismiss();
            strYear = spnYear.getSelectedItemPosition() + 1;
            strMonth = spnMonth.getSelectedItemPosition() + 1;
            strMobile = str_mo_no;
            monthSelected = true;

            if (str_month.length() != 2) {
                str_selected_month = "0" + str_month;
                str_month_year = str_year + "-" + str_selected_month;
            } else {
                str_selected_month = str_month;
                str_month_year = str_year + "-" + str_selected_month;
            }
            rechargeArrayList.clear();
            start = 0;
            end = 10;
            showProgressDialog();
            imgNoData.setVisibility(View.GONE);

            makeNativeTransactionSearch();

            searchListAdapter = new SearchListAdapter(getContextInstance(), rechargeArrayList, this,Constants.KEY_WATER_TEXT);
            lstTransactionSearch.setAdapter(searchListAdapter);
            listState = false;
        }
    }

    /* [START] - 2017_04_28 - Add native code for transaction search, and Remove volley code */
    private void makeNativeTransactionSearch() {
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
        if(start==0) {
            rechargeArrayList = new ArrayList<>();
            rechargeArrayList.clear();
            searchListAdapter = new SearchListAdapter(getContextInstance(), rechargeArrayList, this,Constants.KEY_WATER_TEXT);
            lstTransactionSearch.setAdapter(searchListAdapter);
            showProgressDialog();
        }
        // create new thread for recent transaction
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set search_recharge url
                    String url = URL.search_recharge;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "mobile",
                            "start",
                            "end",
                            "mon_year",
                            "app",
                            "service_id"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            str_user_name,
                            str_mac_address,
                            str_otp_code,
                            str_mo_no,
                            start + "",
                            end + "",
                            str_month_year,
                            Constants.APP_VERSION,
                            service_id
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in transaction search native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("Trans Search Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                ll_recycler_view.setVisibility(View.VISIBLE);
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.i("Trans Search DECR : " + decrypted_response);
                loadMoreData(decrypted_response);

            } else if (jsonObject.getString("status").equals("2")) {
                if (start == 0) {
                    lstTransactionSearch.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
                } else {
                    if (rechargeArrayList.size() > 0) {
                        removeFooterView();
                        lstTransactionSearch.addFooterView(footerViewNoMoreData);
                    } else {
                        lstTransactionSearch.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                    }
                }

                loadmoreFlage = true;
                FLAG_INVALID_DETAIL = true;
                count++;
            } else {
                if (start == 0) {
                    lstTransactionSearch.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
                } else {
                    if (rechargeArrayList.size() > 0) {
                        removeFooterView();
                        lstTransactionSearch.addFooterView(footerViewNoMoreData);
                    } else {
                        lstTransactionSearch.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                    }
                }
                loadmoreFlage = true;
                FLAG_INVALID_DETAIL = true;
                count++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            lstTransactionSearch.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
        }
    }

    private AlertDialog alertDialog;

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

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            dismissProgressDialog();
            if (msg.what == SUCCESS) {
                dismissProgressDialog();
                parseSuccessResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_RECENT) {
                dismissProgressDialog();
                parseTransactionResponse(msg.obj.toString());
            }
        }
    };
    // [END]

    private void removeFooterView() {
        int footerCount = lstTransactionSearch.getFooterViewsCount();
        LogMessage.d("Footer Count All : " + footerCount);
        lstTransactionSearch.removeFooterView(footerView);
        lstTransactionSearch.removeFooterView(footerViewNoMoreData);
    }

    /*Method : loadMoreData
             load data on scroll*/
    public void loadMoreData(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("search");

            if (jsonArray.length() > 0) {
                lstTransactionSearch.setVisibility(View.VISIBLE);
                imgNoData.setVisibility(View.GONE);

                if (jsonArray.length() < 10) {
                    removeFooterView();
                    lstTransactionSearch.addFooterView(footerViewNoMoreData);
                    loadmoreFlage = true;
                } else {
                    if (start == 0) {
                        removeFooterView();
                        lstTransactionSearch.addFooterView(footerView);
                    }
                    loadmoreFlage = false;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Recharge recharge = new Recharge();
                    recharge.setClient_trans_id(object.getString("client_transaction_id"));
                    recharge.setMo_no(object.getString("mobile"));
                    recharge.setAmount(object.getString("amount"));
                    recharge.setCompnay_name(object.getString("company_name"));
                    recharge.setProduct_name(object.getString("product_name"));
                    recharge.setTrans_date_time(object.getString("trans_date_time"));
                    recharge.setStatus(object.getString("status"));
                    recharge.setRecharge_status(object.getString("recharge_status"));
                    recharge.setOperator_trans_id(object.getString("operator_trans_id"));
                    rechargeArrayList.add(recharge);
                    beforeRefreshArrayList.add(recharge);
                }
                searchListAdapter.notifyDataSetChanged();
                if(searchState) {
                    // [START] - set selected labels
                    txtMonth.setText("Month : " + spnMonth.getSelectedItem().toString());
                    txtYear.setText("Year : " + str_year);
                    txtMobile.setText("Number : " + str_mo_no);
                    // [END]
                }
                lstTransactionSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if ((firstVisibleItem + visibleItemCount - 1) == rechargeArrayList.size() && !(loadmoreFlage)) {
                            loadmoreFlage = true;
                            start = start + 10;
                            end = 10;
                            if (searchState) {
                                makeNativeTransactionSearch();
                                listState = false;
                            } else {
                                //makeNativeRecentTransaction();
                                makeNativeTransactionSearch();
                                listState = true;
                            }

                        }
                    }
                });

            } else {
                lstTransactionSearch.setVisibility(View.GONE);
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

    @Override
    public void onComplainClick(int position, String complainMessage, String reason_id) {
        Log.e("onComplainClick", "Position : " + position + " Complain Text " + complainMessage);

        showProgressDialog();
        /*call webservice only if user
        is connected with internet*/
        CheckConnection checkConnection = new CheckConnection();
        if (checkConnection.isConnectingToInternet(getContextInstance()) == true) {
            addComplain(position, complainMessage, reason_id);
        } else {
            dismissProgressDialog();
            Utility.toast(getContextInstance(), "Check your internet connection");
        }
    }

    private void addComplain(final int position, final String complainMessage, final String reason_id) {
        // create new thread for recent transaction
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set latest_recharge url6
                    String url = URL.complain;
                    // Set parameters list in string array
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
                    // set parameters values in string array
                    String[] parametersValues = {
                            str_user_name,
                            str_mac_address,
                            str_otp_code,
                            rechargeArrayList.get(position).getClient_trans_id(),
                            "",
                            rechargeArrayList.get(position).getAmount(),
                            rechargeArrayList.get(position).getMo_no(),
                            reason_id,
                            complainMessage,
                            Constants.APP_VERSION

                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    complainHandler.obtainMessage(SUCCESS, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in recent transaction native method");
                    LogMessage.e("Error : " + ex.getMessage());
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
        alertDialog_1 = new AlertDialog.Builder(getContextInstance()).create();
        // Setting Dialog Title
        alertDialog_1.setTitle("Info!");
        // set cancelable
        alertDialog_1.setCancelable(false);
        // Setting Dialog Message
        alertDialog_1.setMessage(message);
        // Setting OK Button
        alertDialog_1.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog_1.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog_1.show();
    }


    private void parseComplainResponse(String response) {
        LogMessage.i("Latest Complain Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            String responseStatus = jsonObject.getString("status");

            if (responseStatus.equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.d("Decrypted Complain Response  : " + decrypted_response);
                JSONObject object = new JSONObject(decrypted_response);
                String complain_id = object.getString("complain_id");
                displayComplainDialog(jsonObject.getString("msg") + " \nYour Complain Id is " + complain_id);
            } else {
                displayComplainDialog(jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toast(getContextInstance(), "Please check your internet access");
        }
    }

    private void makeNativeRecentTransaction() {
        if (start == 0) {
            showProgressDialog();
        }
        // create new thread for recent transaction
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set latest_recharge url
                    String url = URL.latest_recharge;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "start",
                            "end",
                            "app",
                            "service_id"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            str_user_name,
                            str_mac_address,
                            str_otp_code,
                            start + "",
                            end + "",
                            Constants.APP_VERSION,
                            service_id
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_RECENT, response).sendToTarget();
                } catch (Exception ex) {
                    LogMessage.e("Error in recent transaction native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    private void parseTransactionResponse(String response) {
        LogMessage.i("Latest Search Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            String responseStatus = jsonObject.getString("status");
            if (responseStatus.equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                LogMessage.d("Response Recent Transaction : " + decrypted_response);
                loadMoreRecentData(decrypted_response);
            } else if (responseStatus.equals("2") && jsonObject.getString("msg").equalsIgnoreCase("Invalid details")) {
                if (start == 0) {
                    lstTransactionSearch.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
                } else {
                    if (rechargeArrayList.size() > 0) {
                        removeFooterView();
                        lstTransactionSearch.addFooterView(footerViewNoMoreData);
                    } else {
                        lstTransactionSearch.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                    }
                }
                loadmoreFlageRecent = true;
                FLAG_INVALID_DETAIL = true;
                count++;
            } else {
                if (start == 0) {
                    lstTransactionSearch.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
                } else {
                    if (rechargeArrayList.size() > 0) {
                        removeFooterView();
                        lstTransactionSearch.addFooterView(footerViewNoMoreData);
                    } else {
                        lstTransactionSearch.setVisibility(View.GONE);
                        imgNoData.setVisibility(View.VISIBLE);
                    }
                }
                loadmoreFlageRecent = true;
                FLAG_INVALID_DETAIL = true;
                count++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            lstTransactionSearch.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
        }
    }

    public void loadMoreRecentData(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("latest");

            if (jsonArray.length() > 0) {
                lstTransactionSearch.setVisibility(View.VISIBLE);
                imgNoData.setVisibility(View.GONE);

                if (jsonArray.length() < 10) {
                    removeFooterView();
                    lstTransactionSearch.addFooterView(footerViewNoMoreData);
                    loadmoreFlageRecent = true;
                } else {
                    if (start == 0) {
                        removeFooterView();
                        lstTransactionSearch.addFooterView(footerView);
                    }
                    loadmoreFlageRecent = false;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Recharge recharge = new Recharge();
                    recharge.setClient_trans_id(object.getString("client_transaction_id"));
                    recharge.setMo_no(object.getString("mobile"));
                    recharge.setAmount(object.getString("amount"));
                    recharge.setCompnay_name(object.getString("company_name"));
                    recharge.setProduct_name(object.getString("product_name"));
                    recharge.setTrans_date_time(object.getString("trans_date_time"));
                    recharge.setStatus(object.getString("status"));
                    recharge.setRecharge_status(object.getString("recharge_status"));
                    recharge.setOperator_trans_id(object.getString("operator_trans_id"));
                    rechargeArrayList.add(recharge);
                }

                searchListAdapter.notifyDataSetChanged();

                lstTransactionSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if ((firstVisibleItem + visibleItemCount - 1) == rechargeArrayList.size() && !(loadmoreFlageRecent)) {
                            loadmoreFlageRecent = true;
                            start = start + 10;
                            end = 10;
                            if (searchState) {
                                makeNativeTransactionSearch();
                                listState = false;
                            } else {
                                //makeNativeRecentTransaction();
                                makeNativeTransactionSearch();
                                listState = true;
                            }

                        }
                    }
                });

            } else {
                lstTransactionSearch.setVisibility(View.GONE);
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

    }

}

