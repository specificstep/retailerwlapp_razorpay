package specificstep.com.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Adapters.AccountLedgerAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.AccountLedgerModel;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

public class AccountLedgerFragment extends Fragment {

    private final int SUCCESS = 1, ERROR = 2, SUCCESSBALANCE = 3, SUCCESS_WALLET_LIST = 4;
    /* [START] - All View objects */
    // View class object for display fragment view
    private View view;
    // Load more data view
    private View footerView;
    private View footerViewNoMoreData;
    // [END]

    /* [START] - Other class objects */
    private Context context;
    // Database class
    private DatabaseHelper databaseHelper;
    private AccountLedgerAdapter acLedgerAdapter;
    private TransparentProgressDialog transparentProgressDialog;
    private Calendar fromCalendar, toCalendar;
    private SimpleDateFormat simpleDateFormat;
    // [END]

    ImageView imgNoData;
    private ListView lstCashbookSearch;
    // [END]

    /* [START] - Variables */
    private static boolean loadMoreFlage = false;
    boolean FLAG_INVALID_DETAIL = false;
    private int count = 0;
    // use for get cashbook data from and to limit
    private int start = 0, end = 10;
    private int year, month, day;
    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    private ArrayList<AccountLedgerModel> acLedgerModels;
    private ArrayList<AccountLedgerModel> beforeAcLedgerModels;
    private AlertDialog alertDialog;
    private boolean isAlertOkClicked = false ;
    // [END]
    private TextView txtOpeningBalance, txtClosingBalance;
    String balance;

    //filter control
    FloatingActionButton fat_filter;
    BottomSheetDialog dialog;
    String strFrom = "", strTo = "";
    Button cancel, search, reset;
    LinearLayout duration, durationChild, wallet, walletChild, lnrFrom, lnrTo;
    ImageView imgDuration, imgWallet;
    TextView txtFrom, txtTo, txtFromTest, txtToTest;
    DatePicker dpResult;
    Spinner spnWallet;
    //multiwallet 25-3-2019
    ArrayList<WalletsModel> walletsModelList;
    ArrayList<String> walletsList;
    WalletsModel walletsModel;
    ArrayList<String> menuWallet;
    ArrayAdapter<String> adapter;
    boolean searchState = false;
    public static boolean walletFrom = false;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    private Context getContextInstance() {
        if (context == null) {
            context = AccountLedgerFragment.this.getActivity();
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account_ledger, null);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        alertDialog = new AlertDialog.Builder(getContextInstance()).create();

        initControls();
        //set current date
        strFrom = simpleDateFormat.format(fromCalendar.getTime());
        strTo = simpleDateFormat.format(toCalendar.getTime());

        if(Constants.walletsModelList.size() == 0) {
            walletFrom = false;
            makeWalletCall();
        } else {
            walletsModelList = new ArrayList<WalletsModel>();
            walletsList = new ArrayList<String>();

            walletsModelList = Constants.walletsModelList;
            walletsList = Constants.walletsList;
        }
        makeAccountLedgerBalance();

        fat_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilter();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    private void initControls() {
        /* [START] - Initialise class objects */

        databaseHelper = new DatabaseHelper(getContextInstance());
        transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
        acLedgerModels = new ArrayList<AccountLedgerModel>();
        beforeAcLedgerModels = new ArrayList<AccountLedgerModel>();
        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        userArrayList = databaseHelper.getUserDetail();
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataAcledger);
        lstCashbookSearch = (ListView) view.findViewById(R.id.lv_trans_search_CashBook);
        footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.load_more_items, null);
        footerViewNoMoreData = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_no_moredata, null);
        txtOpeningBalance = (TextView) view.findViewById(R.id.txtCashSummaryOpening);
        txtClosingBalance = (TextView) view.findViewById(R.id.txtCashSummaryClosing);
        //filter
        fat_filter = (FloatingActionButton) view.findViewById(R.id.fabAcledger);
        dialog = new BottomSheetDialog(getContextInstance());
    }

    public void showFilter() {

        dialog.setContentView(R.layout.popup_cashbook_filter);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animation;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.setCanceledOnTouchOutside(false);

        cancel = (Button) dialog.findViewById(R.id.btnCashbookCancel);
        reset = (Button) dialog.findViewById(R.id.btnCashbookReset);
        search = (Button) dialog.findViewById(R.id.btnCashbookSearch);
        duration = (LinearLayout) dialog.findViewById(R.id.lnrCashbookDuration);
        durationChild = (LinearLayout) dialog.findViewById(R.id.lnrCashbookDurationChild);
        wallet = (LinearLayout) dialog.findViewById(R.id.lnrCashbookWallet);
        walletChild = (LinearLayout) dialog.findViewById(R.id.lnrCashbookWalletChild);
        imgDuration = (ImageView) dialog.findViewById(R.id.imgCashbookDuration);
        imgWallet = (ImageView) dialog.findViewById(R.id.imgCashbookWallet);
        lnrFrom = (LinearLayout) dialog.findViewById(R.id.lnrCashbookDurationFrom);
        lnrTo = (LinearLayout) dialog.findViewById(R.id.lnrCashbookDurationTo);
        txtFrom = (TextView) dialog.findViewById(R.id.from_date_CashBook);
        txtTo = (TextView) dialog.findViewById(R.id.to_date_CashBook);
        txtFromTest = (TextView) dialog.findViewById(R.id.from_date_CashBook_Test);
        txtToTest = (TextView) dialog.findViewById(R.id.to_date_CashBook_Test);
        dpResult= (DatePicker) dialog.findViewById(R.id.dpResult);
        spnWallet = (Spinner) dialog.findViewById(R.id.spnCashbookWallet);

        setCurrentDateOnView();
        formDatePicker();
        toDatePicker();

        if (walletsModelList != null && walletsModelList.size() > 0) {
            adapter = new ArrayAdapter<String>(getActivity(), R.layout.adapter_multiwallet_spinner_color, walletsList);
            spnWallet.setAdapter(adapter);
            wallet.setVisibility(View.VISIBLE);
            walletChild.setVisibility(View.VISIBLE);
        } else {
            wallet.setVisibility(View.GONE);
            walletChild.setVisibility(View.GONE);
        }

        duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(durationChild.getVisibility() == View.VISIBLE) {
                    durationChild.setVisibility(View.GONE);
                    imgDuration.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_black_24dp));
                } else {
                    durationChild.setVisibility(View.VISIBLE);
                    imgDuration.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_remove));
                }
            }
        });

        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(walletChild.getVisibility() == View.VISIBLE) {
                    walletChild.setVisibility(View.GONE);
                    imgWallet.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_black_24dp));
                } else {
                    walletChild.setVisibility(View.VISIBLE);
                    imgWallet.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_remove));
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                searchState = true;
                strFrom = simpleDateFormat.format(fromCalendar.getTime());
                strTo = simpleDateFormat.format(toCalendar.getTime());
                isAlertOkClicked = false ;
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date from_date = sdf.parse(txtFrom.getText().toString());
                    Date to_date = sdf.parse(txtTo.getText().toString());

                    if (from_date.getMonth() == to_date.getMonth()) {
                        acLedgerModels.clear();
                        start = 0;
                        end = 10;
                        showProgressDialog();

                        makeNativeAccountLedger();//2
                        acLedgerAdapter = new AccountLedgerAdapter(getContextInstance(), acLedgerModels);
                        lstCashbookSearch.setAdapter(acLedgerAdapter);
                        setBalance();
                    } else {
                        Utility.toast(getContextInstance(), "Please select dates of same month.");
                    }
                }
                catch (Exception ex) {
                    Dlog.d("Cashbook : " + "Error 3 : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                    fromCalendar.setTime(sdf.parse(strFrom));
                    toCalendar.setTime(sdf.parse(strTo));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spnWallet.setSelection(0);
                fromCalendar.setTime(new Date());
                txtFrom.setText(simpleDateFormat.format(fromCalendar.getTime()));
                toCalendar.setTime(new Date());
                txtTo.setText(simpleDateFormat.format(toCalendar.getTime()));
            }
        });

        dialog.show();

    }

    private void makeAccountLedgerBalance() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.balance;
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
                    myHandler.obtainMessage(SUCCESSBALANCE, response).sendToTarget();
                }
                catch (Exception ex) {
                    Dlog.d("  Error  : "+ ex.getMessage() );
                    ex.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lstCashbookSearch.setVisibility(View.GONE);
                            imgNoData.setVisibility(View.VISIBLE);
                            dismissProgressDialog();
                            myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                        }
                    });
                }
            }
        }).start();
    }

    // parse success response
    private void parseSuccessResponseBalance(String response) {
        Dlog.d(" AccountLedger Balance Response : "+ response );

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                Dlog.d("AccountLedger : " + "decrypted_response : " + decrypted_response);
                JSONObject object = new JSONObject(decrypted_response);
                balance = object.getString("balance");
                loadDefaultAccountLedger();
            } else {

            }
        }
        catch (JSONException e) {
            Dlog.d("Cashbook : " + "Error 4 : " + e.getMessage());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lstCashbookSearch.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
                }
            });
            e.printStackTrace();
        }
    }

    // Load current cashbook
    private void loadDefaultAccountLedger() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date from_date = sdf.parse(strFrom);
            Date to_date = sdf.parse(strFrom);

            if (from_date.getMonth() == to_date.getMonth()) {
                acLedgerModels.clear();
                start = 0;
                end = 10;
                showProgressDialog();
                imgNoData.setVisibility(View.GONE);

                makeNativeAccountLedger(); //1
                //@kns.p
                acLedgerAdapter = new AccountLedgerAdapter(getContextInstance(), acLedgerModels);
                lstCashbookSearch.setAdapter(acLedgerAdapter);
                setBalance();

            } else {
                Utility.toast(getContextInstance(), "Please select dates of same month.");
            }
        }
        catch (Exception ex) {
            Dlog.d("Cashbook : " + "Error 3 : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Set from date and it's click listener
     */
    private void formDatePicker() {
        final DatePickerDialog.OnDateSetListener fromDatePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                try {
                    fromCalendar.set(Calendar.YEAR, year);
                    fromCalendar.set(Calendar.MONTH, monthOfYear);
                    fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateFromLabel(txtFrom);

                    /* [START] - 2017_04_18 - set to date selection validation and update to date label */
                    try {
                        // set default year in to_date_picker as per from_date_picker
                        toCalendar.set(Calendar.YEAR, year);
                        // set default month in to_date_picker as per from_date_picker
                        toCalendar.set(Calendar.MONTH, monthOfYear);
                        // get last date from from_date_picker selected month
                        int lastDayOfMonth = fromCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        // get current date for validate to_date_picker max date
                        String currentFullDate = DateTime.getDate();
                        // get current date from full date
                        String currentDate = DateTime.getDayFromFullDate(currentFullDate);
                        // get current month from full date
                        String currentMonth = DateTime.getMonthFromFullDate(currentFullDate);
                        // Check current month and from_date_picker month are same or not.
                        if (Integer.parseInt(currentMonth) == monthOfYear + 1) {
                            // if current month and from_date_picker month are same, check current date and last day of month
                            if (Integer.parseInt(currentDate) < lastDayOfMonth) {
                                // if current date is less then last day of month then set current date in to calender
                                toCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(currentDate));
                            } else {
                                // else set last day of month in to calender
                                toCalendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                            }
                        } else {
                            // else set last day of month in to calender
                            toCalendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                        }
                        updateToLabel(txtTo);
                    }
                    catch (Exception ex) {
                        Dlog.d("Error : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    // [END]
                }
                catch (Exception ex) {
                    Dlog.d("Cashbook : " + "Error 1 : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        };

        txtFrom.setText(strFrom);
        txtFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    long timeInMilliseconds = new Date().getTime();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                            fromDatePicker,
                            fromCalendar.get(Calendar.YEAR),
                            fromCalendar.get(Calendar.MONTH),
                            fromCalendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setMaxDate(timeInMilliseconds + (1000 * 60 * 60 * 1));
                    datePickerDialog.show();
                }
                catch (Exception e) {
                    Dlog.d("Cashbook : " + "Error 1 : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Set to date and it's click listener
     */
    private void toDatePicker() {
        final DatePickerDialog.OnDateSetListener toDatePicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                toCalendar.set(Calendar.YEAR, year);
                toCalendar.set(Calendar.MONTH, monthOfYear);
                toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateToLabel(txtTo);
            }
        };

        txtTo.setText(strTo);
        txtTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                try {
                    Calendar calendar = Calendar.getInstance();
                    Date mDate = sdf.parse(txtFrom.getText().toString());
                    long min_TimeInMilliseconds = mDate.getTime();
                    long max_Time = calendar.getTimeInMillis();
                    long max_TimeInMilliseconds = 0;

                    /* [START] - 2017_04_19 - set to date selection validation and update to date label */
                    try {
                        // set default year in to_date_picker as per from_date_picker
                        calendar.set(Calendar.YEAR, year);
                        // set default month in to_date_picker as per from_date_picker
                        calendar.set(Calendar.MONTH, mDate.getMonth());
                        int monthOfYear = mDate.getMonth();
                        // get last date from from_date_picker selected month
                        int lastDayOfMonth = fromCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        // get current date for validate to_date_picker max date
                        String currentFullDate = DateTime.getDate();
                        // get current date from full date
                        String currentDate = DateTime.getDayFromFullDate(currentFullDate);
                        // get current month from full date
                        String currentMonth = DateTime.getMonthFromFullDate(currentFullDate);
                        // Check current month and from_date_picker month are same or not.
                        if (Integer.parseInt(currentMonth) == monthOfYear + 1) {
                            // if current month and from_date_picker month are same, check current date and last day of month
                            if (Integer.parseInt(currentDate) < lastDayOfMonth) {
                                // if current date is less then last day of month then set current date in to calender
                                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(currentDate));
                            } else {
                                // else set last day of month in to calender
                                calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                            }
                        } else {
                            // else set last day of month in to calender
                            calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
                        }
                        max_TimeInMilliseconds = calendar.getTimeInMillis();
                    }
                    catch (Exception ex) {
                        Dlog.d("Error : " + ex.getMessage());
                        ex.printStackTrace();
                        max_TimeInMilliseconds = max_Time;
                    }
                    // [END]

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                            toDatePicker,
                            toCalendar.get(Calendar.YEAR),
                            toCalendar.get(Calendar.MONTH),
                            toCalendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setMinDate(min_TimeInMilliseconds);
                    datePickerDialog.getDatePicker().setMaxDate(max_TimeInMilliseconds + (1000 * 60 * 60 * 1));
                    datePickerDialog.show();
                }
                catch (Exception e) {
                    Dlog.d("Cashbook : " + "Error 2 : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateFromLabel(TextView editText) {
        String myFormat = "dd-MMM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(sdf.format(fromCalendar.getTime()));
    }

    private void updateToLabel(TextView editText) {
        String myFormat = "dd-MMM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(sdf.format(toCalendar.getTime()));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    // display current date
    public void setCurrentDateOnView() {

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set current date into text view
        txtFromTest.setText(new StringBuilder()
                // Month is 0 based, just add 1
                .append(month + 1).append("-").append(day).append("-")
                .append(year).append(" "));

        // set current date into date picker
        dpResult.init(year, month, day, null);
    }

    /* [START] - 2017_04_28 - Add native code for cash book, and Remove volley code */
    private void makeNativeAccountLedger() {
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                    DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date from_date = sdf.parse(strFrom);
                    Date to_date = sdf.parse(strTo);
                    String wallet_id = "";
                    if(searchState) {
                        wallet_id = walletsModelList.get(spnWallet.getSelectedItemPosition()).getWallet_type();
                    } else {
                        wallet_id = "";
                    }

                    // set cashBook url
                    String url = URL.accountLedger;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "user_type",
                            "fromdate",
                            "todate",
                            "wallet_id",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            "4",
                            outputFormat.format(from_date),
                            outputFormat.format(to_date),
                            wallet_id,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                }
                catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            lstCashbookSearch.setVisibility(View.GONE);
                            imgNoData.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }

    // parse success response
    private void parseSuccessResponse(String response) {
        Dlog.d("AccountLedger Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                Dlog.d("AccountLedger : " + "decrypted_response : " + decrypted_response);

                if(isAlertOkClicked == false){
                    loadMoreData(decrypted_response);
                }
            } else if (jsonObject.getString("status").equals("2")) {
                lstCashbookSearch.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
                int footerCount = lstCashbookSearch.getFooterViewsCount();
                removeFooterView();
                lstCashbookSearch.addFooterView(footerViewNoMoreData);
                loadMoreFlage = true;
                FLAG_INVALID_DETAIL = true;
                count++;
            } else {
                lstCashbookSearch.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
                int footerCount = lstCashbookSearch.getFooterViewsCount();
                removeFooterView();
                lstCashbookSearch.addFooterView(footerViewNoMoreData);
            }
        }
        catch (JSONException e) {
            Dlog.d("Cashbook : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
            lstCashbookSearch.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
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
                Utility.toast(getContextInstance(), message);
            }
            catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                dismissProgressDialog();
                parseSuccessResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESSBALANCE) {
                dismissProgressDialog();
                parseSuccessResponseBalance(msg.obj.toString());
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    private void removeFooterView() {
        try {
            int footerCount = lstCashbookSearch.getFooterViewsCount();
            LogMessage.e("Footer Count All : " + footerCount);
            lstCashbookSearch.removeFooterView(footerView);
            lstCashbookSearch.removeFooterView(footerViewNoMoreData);
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }

    /*Method : loadMoreData
               load data on scroll*/
    public void loadMoreData(String response) {
        try {
         SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm:ss");
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("accounts");

            if(jsonArray.length()>0) {
                lstCashbookSearch.setVisibility(View.VISIBLE);
                imgNoData.setVisibility(View.GONE);

                if (jsonArray.length() < 10) {
                    removeFooterView();
                    lstCashbookSearch.addFooterView(footerViewNoMoreData);
                    loadMoreFlage = true;
                } else {
                    if (start == 0) {
                        removeFooterView();
                        lstCashbookSearch.addFooterView(footerView);
                    }
                    loadMoreFlage = false;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    AccountLedgerModel acledgerModel = new AccountLedgerModel();
                    Dlog.d("Original date: " + object.getString("created_date"));

                    try {
                        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date newDate = spf.parse(object.getString("created_date"));
                        spf = new SimpleDateFormat("dd-MMM-yy hh:mm:ss aa");
                        acledgerModel.created_date = spf.format(newDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    acledgerModel.type = object.getString("type");
                    acledgerModel.payment_id = object.getString("recharge_uniqid");
                    acledgerModel.particular = object.getString("payment_name");
                    acledgerModel.cr_dr = object.getString("cr_dr");
                    acledgerModel.amount = object.getString("amount");
                    acledgerModel.balance = object.getString("balance");

                    acLedgerModels.add(acledgerModel);
                    beforeAcLedgerModels.add(acledgerModel);
                }
                Collections.reverse(acLedgerModels);
                setBalance();
                removeFooterView();

            } else {
                lstCashbookSearch.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
            }

        }
        catch (JSONException e) {
            lstCashbookSearch.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
            e.printStackTrace();
            Dlog.d("Cashbook : " + "Error 8 : " + e.toString());
                if (!alertDialog.isShowing()) {
                    alertDialog.setTitle("Alert!");
                    alertDialog.setCancelable(false);
                    alertDialog.setMessage(getActivity().getResources().getString(R.string.alert_servicer_down));
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                            isAlertOkClicked = true ;
                            loadMoreFlage = false;
                            removeFooterView();
                            dismissProgressDialog();
                        }
                    });
                    alertDialog.show();
                }

        }
    }

    public void setBalance() {

        try {
            if(acLedgerModels.size()>0) {
                Double openBal = 0.0;
                NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
                Number numberBalance = null, numberAmount = null, numberEndBal = null;
                numberBalance = format.parse(acLedgerModels.get(0).balance);
                numberAmount = format.parse(acLedgerModels.get(0).amount);
                if(acLedgerModels.get(0).cr_dr.equals("Credit")) {
                    openBal = numberBalance.doubleValue() + numberAmount.doubleValue();
                } else {
                    openBal = numberBalance.doubleValue() - numberAmount.doubleValue();
                }
                BigDecimal cached = new BigDecimal(openBal+"");
                txtOpeningBalance.setText(getResources().getString(R.string.currency_format, Constants.formatBigDecimalToString(cached)));
                numberEndBal = format.parse(acLedgerModels.get(acLedgerModels.size()-1).balance);
                BigDecimal bigDecimal = new BigDecimal((numberEndBal.doubleValue())+"");
                txtClosingBalance.setText(getResources().getString(R.string.currency_format, Constants.formatBigDecimalToString(bigDecimal)));
            } else {
                Dlog.d("CachedBalance: " + balance);
                BigDecimal cached = new BigDecimal(balance);
                txtOpeningBalance.setText(getResources().getString(R.string.currency_format, Constants.formatBigDecimalToString(cached)));
                txtClosingBalance.setText(getResources().getString(R.string.currency_format, Constants.formatBigDecimalToString(cached)));
            }
        } catch (Exception e) {
            Dlog.d(e.toString());
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
                    Intent intent = new Intent(getActivity(), Main2Activity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

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
                    Dlog.d("  Error  : " + ex.getMessage());
                    ex.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lstCashbookSearch.setVisibility(View.GONE);
                            imgNoData.setVisibility(View.VISIBLE);
                            dismissProgressDialog();
                        }
                    });
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
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
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
                    try {
                        menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }

                if(walletFrom && walletsModelList.size()>0) {
                    Constants.showWalletPopup(getActivity());
                }

            } else {
                displayErrorDialog(jsonObject.getString("msg") + "");
            }
        }
        catch(JSONException e) {
            Dlog.d("Wallet : " + "Error 4 : " + e.getMessage());
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
