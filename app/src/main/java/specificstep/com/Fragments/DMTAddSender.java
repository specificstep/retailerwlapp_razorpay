package specificstep.com.Fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Database.DatabaseHelper;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class DMTAddSender extends Fragment {

    EditText edtFirstName, edtLastName, edtMobile, edtEmail, edtAddress, edtPincode, edtDob;
    Button btnSubmit, btnCancel;
    View view;
    Context context;
    private String strMacAddress, strUserName, strOtpCode;
    private ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_WALLET_LIST = 3;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    public static int sender_id;

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    public DMTAddSender() {
        // Required empty public constructor
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
        // [END]
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dmtadd_sender, container, false);
        mainActivity().getSupportActionBar().setTitle("Add Sender");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE & WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        initialize();

        // Display bottom bar
        mainActivity().displayDMTBottomBarDynamic();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()) {
                    makeAddSenderCall();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getFragmentManager().popBackStackImmediate();
                } catch (Exception e) {
                    Dlog.d(e.toString());
                }
            }
        });

        edtDob.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000 * 60 * 60 * 1));
                dialog.show();
            }
        });

        return view;
    }

    public void initialize() {

        edtFirstName = (EditText) view.findViewById(R.id.edtDMTAddSenderFirstName);
        edtLastName = (EditText) view.findViewById(R.id.edtDMTAddSenderLastName);
        edtMobile = (EditText) view.findViewById(R.id.edtDMTAddSenderMobile);
        edtEmail = (EditText) view.findViewById(R.id.edtDMTAddSenderEmail);
        edtAddress = (EditText) view.findViewById(R.id.edtDMTAddSenderAddress);
        edtPincode = (EditText) view.findViewById(R.id.edtDMTAddSenderPincode);
        edtDob = (EditText) view.findViewById(R.id.edtDMTAddSenderDob);
        btnSubmit = (Button) view.findViewById(R.id.btnAddSenderSubmit);
        btnCancel = (Button) view.findViewById(R.id.btnAddSenderCancel);

        edtMobile.setText(DMTFragment.edtMobile.getText().toString());
        edtMobile.setEnabled(false);

        databaseHelper = new DatabaseHelper(getActivity());
        alertDialog = new AlertDialog.Builder(getActivity()).create();

        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        myCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edtDob.setText(sdf.format(myCalendar.getTime()));
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
                    getFragmentManager().popBackStackImmediate();
                    return true;
                }
                return false;
            }
        });

    }

    public boolean valid() {

        if(TextUtils.isEmpty(edtFirstName.getText().toString())) {
            Toast.makeText(getActivity(),"Enter First Name.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtLastName.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Last Name.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtMobile.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Mobile Number.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtPincode.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Pincode.",Toast.LENGTH_LONG).show();
            return false;
        } else if(TextUtils.isEmpty(edtDob.getText().toString())) {
            Toast.makeText(getActivity(),"Enter Date Of Birth.",Toast.LENGTH_LONG).show();
            return false;
        } else if(edtMobile.getText().toString().length() < 10) {
            Toast.makeText(getActivity(),"Enter valid Mobile Number.",Toast.LENGTH_LONG).show();
            return false;
        } else if(!TextUtils.isEmpty(edtEmail.getText().toString()) && !Constants.isValidEmail(edtEmail.getText().toString())) {
            Toast.makeText(getActivity(), "Enter valid Email.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void makeAddSenderCall() {
        showProgressDialog();
        // create new threadc
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.addSender;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "mobile",
                            "firstname",
                            "lastname",
                            "dob",
                            "pincode"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION,
                            edtMobile.getText().toString(),
                            edtFirstName.getText().toString(),
                            edtLastName.getText().toString(),
                            edtDob.getText().toString(),
                            edtPincode.getText().toString()
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in Cash book native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // handle thread messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
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

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("Add Sender Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("status") == 1) {
                displayErrorDialog(jsonObject.getString("msg")+"");
                if(jsonObject.getString("msg").equals("Otp Send Successfully")) {
                    JSONObject object = jsonObject.getJSONObject("data");
                    sender_id = object.getInt("sender_id");
                    DMTFragment.mob = edtMobile.getText().toString();
                    DMTOtpFragment rechargeMainFragment = new DMTOtpFragment();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString()+"").commit();
                }
            } else {
                displayErrorDialog(jsonObject.getString("msg")+"");
            }
        }
        catch (JSONException e) {
            LogMessage.e("Cashbook : " + "Error 4 : " + e.getMessage());
            Utility.toast(getActivity(), "No result found");
            e.printStackTrace();
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
        }
        catch (Exception ex) {
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
        }
        catch (Exception ex) {
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getActivity(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

}
