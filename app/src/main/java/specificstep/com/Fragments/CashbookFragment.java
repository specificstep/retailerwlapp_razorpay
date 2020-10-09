package specificstep.com.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AbsListView;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Adapters.CashbookAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.CashbookModel;
import specificstep.com.Models.DateTime;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 16/3/17.
 */
public class CashbookFragment extends Fragment {

    private final int SUCCESS = 1, ERROR = 2, SUCCESS_WALLET_LIST = 3;
    private View view;
    private View footerView;
    private View footerViewNoMoreData;
    private Context context;
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private DatabaseHelper databaseHelper;
    private CashbookAdapter cashbookAdapter;
    private TransparentProgressDialog transparentProgressDialog;
    private Calendar fromCalendar, toCalendar;
    private SimpleDateFormat simpleDateFormat;

    private ListView lstCashbookSearch;

    private static boolean loadMoreFlage = false;
    boolean FLAG_INVALID_DETAIL = false;
    private int count = 0;
    private int start = 0, end = 10;
    private int year, month, day;
    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    private ArrayList<CashbookModel> cashbookModels;
    private ArrayList<CashbookModel> beforeCashbookModels;

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
    ArrayAdapter<String> adapter;
    String balance;
    ImageView imgNoData;
    boolean searchState = false;
    String walletPos = "";
    public static boolean walletFrom = false;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    private Context getContextInstance() {
        if (context == null) {
            context = CashbookFragment.this.getActivity();
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
                    if(Constants.walletsModelList.size() == 0) {
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
        view = inflater.inflate(R.layout.fragment_cashbook, null);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = CashbookFragment.this.getActivity();

        initControls();
        //set current date
        strFrom = simpleDateFormat.format(fromCalendar.getTime());
        strTo = simpleDateFormat.format(toCalendar.getTime());

        if(Constants.walletsModelList.size() == 0) {
            walletFrom = true;
            makeWalletCall();
        } else {
            walletsModelList = new ArrayList<WalletsModel>();
            walletsList = new ArrayList<String>();

            walletsModelList = Constants.walletsModelList;
            walletsList = Constants.walletsList;
        }
        loadDefaultCashbook();

        fat_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilter();
            }
        });
    }

    private void initControls() {
        /* [START] - Initialise class objects */

        constants = new Constants();
        databaseHelper = new DatabaseHelper(getContextInstance());
        sharedPreferences = getActivity().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
        cashbookModels = new ArrayList<CashbookModel>();
        beforeCashbookModels = new ArrayList<CashbookModel>();
        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

        userArrayList = databaseHelper.getUserDetail();
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();

        lstCashbookSearch = (ListView) view.findViewById(R.id.lv_trans_search_CashBook);
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataCashbook);
        // Load more data view
        footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.load_more_items, null);
        footerViewNoMoreData = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_no_moredata, null);
        //filter
        fat_filter = (FloatingActionButton) view.findViewById(R.id.fabCashbook);
        dialog = new BottomSheetDialog(getContextInstance());
    }

    //filter popup
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
            if(walletPos.equals("")) {
                spnWallet.setSelection(0);
            } else {
                spnWallet.setSelection(Integer.valueOf(walletPos));
            }
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
                walletPos = spnWallet.getSelectedItemPosition() + "";
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date from_date = sdf.parse(txtFrom.getText().toString());
                    Date to_date = sdf.parse(txtTo.getText().toString());

                    if (from_date.getMonth() == to_date.getMonth()) {
                        cashbookModels.clear();
                        start = 0;
                        end = 10;
                        showProgressDialog();
                        makeNativeCashBook();
                        cashbookAdapter = new CashbookAdapter(getContextInstance(), cashbookModels);
                        lstCashbookSearch.setAdapter(cashbookAdapter);
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

    // Load current cashbook
    private void loadDefaultCashbook() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date from_date = sdf.parse(strFrom);
            Date to_date = sdf.parse(strTo);

            if (from_date.getMonth() == to_date.getMonth()) {
                cashbookModels.clear();
                start = 0;
                end = 10;
                showProgressDialog();
                makeNativeCashBook();
                cashbookAdapter = new CashbookAdapter(getContextInstance(), cashbookModels);
                lstCashbookSearch.setAdapter(cashbookAdapter);
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

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                try {
                    final long timeInMilliseconds = new Date().getTime();
                    final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
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
    private void makeNativeCashBook() {
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

                    String url = URL.cashBook;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "start_limit",
                            "end_limit",
                            "user_type",
                            "from",
                            "to",
                            "wallet_id",
                            "app"
                    };
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            start + "",
                            end + "",
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
                }
            }
        }).start();
    }

    // parse success response
    private void parseSuccessResponse(String response) {
        Dlog.d("CashBook Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                lstCashbookSearch.setVisibility(View.VISIBLE);
                imgNoData.setVisibility(View.GONE);
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                Dlog.d("CashBook : " + "decrypted_response : " + decrypted_response);
                loadMoreData(decrypted_response);
            } else if (jsonObject.getString("status").equals("2") &&
                    jsonObject.getString("message").equalsIgnoreCase("Invalid Details")) {
                lstCashbookSearch.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
                removeFooterView();
                lstCashbookSearch.addFooterView(footerViewNoMoreData);

                loadMoreFlage = true;
                FLAG_INVALID_DETAIL = true;
                count++;
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Info!");
                alertDialog.setCancelable(false);
                alertDialog.setMessage(jsonObject.getString("message"));
                alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            } else {
                lstCashbookSearch.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
                removeFooterView();
                lstCashbookSearch.addFooterView(footerViewNoMoreData);
                if (start == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("No Payment Report found")
                            .setCancelable(false)
                            .setMessage(jsonObject.getString("message"))
                            .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                } else {

                }
            }
        }
        catch (JSONException e) {
            Dlog.d("Cashbook : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
            lstCashbookSearch.setVisibility(View.GONE);
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
            } else if (msg.what == SUCCESS_WALLET_LIST) {
                dismissProgressDialog();
                parseSuccessWalletResponse(msg.obj.toString());
            }
        }
    };

    private void removeFooterView() {
        lstCashbookSearch.removeFooterView(footerView);
        lstCashbookSearch.removeFooterView(footerViewNoMoreData);
    }

    /*Method : loadMoreData
               load data on scroll*/
    public void loadMoreData(String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("data");

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
                    CashbookModel cashbookModel = new CashbookModel();
                    cashbookModel.paymentId = object.getString("Payment Id");
                    cashbookModel.amount = object.getString("Amount");
                    cashbookModel.balance = "";
                    cashbookModel.dateTime = object.getString("Datetime");
                    cashbookModel.paymentFrom = object.getString("Payment From");
                    cashbookModel.paymentTo = object.getString("Payment To");
                    cashbookModel.remarks = object.getString("Remarks");
                    cashbookModel.userType = object.getString("UserType");
                    cashbookModels.add(cashbookModel);
                    beforeCashbookModels.add(cashbookModel);
                }
                cashbookAdapter.notifyDataSetChanged();
            } else {
                lstCashbookSearch.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            Dlog.d("Cashbook : " + "Error 8 : " + e.toString());
            new AlertDialog.Builder(getActivity())
                    .setTitle("Alert!")
                    .setCancelable(false)
                    .setMessage(getResources().getString(R.string.alert_servicer_down))
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
        lstCashbookSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if ((firstVisibleItem + visibleItemCount - 1) == cashbookModels.size() && !(loadMoreFlage)) {
                    loadMoreFlage = true;
                    start = start + 10;
                    end = 10;
                    makeNativeCashBook();
                }
            }
        });
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
                    menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
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
