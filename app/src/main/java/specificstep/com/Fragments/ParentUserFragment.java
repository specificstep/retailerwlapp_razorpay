package specificstep.com.Fragments;

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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Activities.Main2Activity;
import specificstep.com.Adapters.ParentUserAdapter;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.ParentUserModel;
import specificstep.com.Models.User;
import specificstep.com.Models.WalletsModel;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

public class ParentUserFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ParentUserFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ParentUserFragment newInstance(String param1, String param2) {
        ParentUserFragment fragment = new ParentUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /* [START] - 2017_05_02 - All Variables and Objects */
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_WALLET_LIST = 4;
    private View view;
    private Context context;
    // All static variables class
    private Constants constants;
    // Custom progress bar
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog;
    // Database class
    private DatabaseHelper databaseHelper;
    // All array lists
    private ArrayList<User> userArrayList;
    // All string variables
    private String strMacAddress, strUserName, strOtpCode, strRegistrationDateTime;
    // Controls objects
    private ListView lstParentUserDetails;
    private LinearLayout llParentUserDetails;
    private TextView txtFirmName, txtMobileNo, txtUserType, txtName;
    private ImageView imgNoData;
    LinearLayout lnrCurrent;
    // [END]

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    private Context getContextInstance() {
        if (context == null) {
            context = ParentUserFragment.this.getActivity();
            return context;
        } else {
            return context;
        }
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

    //mansi change
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_parent_user, container, false);
        context = ParentUserFragment.this.getActivity();
        initControls();

        showProgressDialog();
        makeGetParentUserDetails();

        return view;
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
                    try {
                        menuWallet.add(object.getString("wallet_name") + " : " + getActivity().getResources().getString(R.string.Rs) + " " + object.getString("balance"));
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }

                Constants.walletsList = walletsList;
                Constants.walletsModelList = walletsModelList;

                /*if(menuItem != null) {
                    if (walletsModelList.size() > 0) {
                        menuItem.setVisible(true);
                    } else {
                        menuItem.setVisible(false);
                    }
                }*/

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
        } catch (Exception e) {
            LogMessage.e("Wallet : " + "Error 4 : " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void initControls() {
        /* [START] - Initialise class objects */
        // log = new LogMessage(ParentUserFragment.class.getSimpleName());
        constants = new Constants();
        transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
        databaseHelper = new DatabaseHelper(getContextInstance());
        // [END]

        /* [START] - get user data from database and store into string variables */
        userArrayList = databaseHelper.getUserDetail();
        // Store user information in variables
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        strRegistrationDateTime = userArrayList.get(0).getReg_date();
        // [END]

        /* [START] - Initialise control objects */
        lstParentUserDetails = (ListView) view.findViewById(R.id.lst_ParentUser_ParentUserDetails);
        llParentUserDetails = (LinearLayout) view.findViewById(R.id.ll_ParentUser);
        imgNoData = (ImageView) view.findViewById(R.id.imgNoDataParentUser);
        txtFirmName = (TextView) view.findViewById(R.id.txt_ParentUser_FirmName);
        txtName = (TextView) view.findViewById(R.id.txt_ParentUser_Name);
        txtMobileNo = (TextView) view.findViewById(R.id.txt_ParentUser_MobileNo);
        txtUserType = (TextView) view.findViewById(R.id.txt_ParentUser_UserType);
        lnrCurrent = (LinearLayout) view.findViewById(R.id.lnrParentUserCurrent);
        // [END]
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity().getSupportActionBar().setTitle("Parent User");
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
    }

    /* [START] - 2017_05_02 - Get parent user details */
    private void makeGetParentUserDetails() {
        // create new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set cashBook url
                    String url = URL.GET_PARENT_USER_DETAILS;
                    // Set parameters list in string array
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app"
                    };
                    // set parameters values in string array
                    String[] parametersValues = {
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            Constants.APP_VERSION
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                }
                catch (Exception ex) {
                    LogMessage.e("Error in get parent user details native method");
                    LogMessage.e("Error : " + ex.getMessage());
                    ex.printStackTrace();
                    myHandler.obtainMessage(ERROR, "Please check your internet access").sendToTarget();
                }
            }
        }).start();
    }

    // parse success response
    private void parseSuccessResponse(String response) {
        LogMessage.i("Parent User Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                llParentUserDetails.setVisibility(View.VISIBLE);
                String encrypted_response = jsonObject.getString("data");
                String message = jsonObject.getString("msg");
                LogMessage.d("Parent User : " + "Message : " + message);
                try {
                    String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                    LogMessage.d("Parent User : " + "Response : " + decrypted_response);
                    loadData(decrypted_response);
                }
                catch (Exception ex) {
                    myHandler.obtainMessage(ERROR, "Parent user data not found.").sendToTarget();
                }
            } else {
                myHandler.obtainMessage(ERROR, jsonObject.getString("msg")).sendToTarget();
            }
        }
        catch (JSONException e) {
            LogMessage.d("Cashbook : " + "Error 4 : " + e.getMessage());
            //Utility.toast(getContextInstance(), "No result found");
            e.printStackTrace();
        }
    }

    public void loadData(String response) {
        try {
            ArrayList<ParentUserModel> parentUserModels = new ArrayList<ParentUserModel>();
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray = jsonObject1.getJSONArray("userdata");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                ParentUserModel parentUserModel = new ParentUserModel();
                parentUserModel.position = i + 1 + "";
                parentUserModel.firmName = object.getString("firm_name");
                parentUserModel.mobileNumber = object.getString("phone_no");
                parentUserModel.userType = object.getString("usertype");
                String fullName = " - ";
                try {
                    fullName = object.getString("first_name") + " " + object.getString("last_name");
                }
                catch (Exception ex) {
                    fullName = " - ";
                }
                parentUserModel.name = fullName;

                parentUserModels.add(parentUserModel);
            }
            if (parentUserModels != null) {
                if (parentUserModels.size() > 0) {
                    Collections.reverse(parentUserModels);
                    ParentUserAdapter parentUserAdapter = new ParentUserAdapter(getContextInstance(), parentUserModels);
                    lstParentUserDetails.setAdapter(parentUserAdapter);
                    lstParentUserDetails.setVisibility(View.VISIBLE);
                    imgNoData.setVisibility(View.GONE);
                } else {
                    lstParentUserDetails.setVisibility(View.GONE);
                    imgNoData.setVisibility(View.VISIBLE);
                }
            } else {
                lstParentUserDetails.setVisibility(View.GONE);
                imgNoData.setVisibility(View.VISIBLE);
            }

            // display user login details
            JSONObject jsonObject = new JSONObject(response);
            String details = jsonObject.getString("details");
            JSONObject detailsObject = new JSONObject(details);
            lnrCurrent.setVisibility(View.VISIBLE);

            String firm_html = "<b>Firm Name : </b>" + detailsObject.getString("firm_name");
            txtFirmName.setText(Html.fromHtml(firm_html));
            String fullName = " - ";
            try {
                fullName = detailsObject.getString("first_name") + " " + detailsObject.getString("last_name");
            }
            catch (Exception ex) {
                fullName = " - ";
            }

            String name_html = "<b>Name : </b>" + fullName;
            txtName.setText(Html.fromHtml(name_html));
            String mobile_html = "<b>Mobile No : </b>" + detailsObject.getString("phone_no");
            txtMobileNo.setText(Html.fromHtml(mobile_html));
            String user_type_html = "<b>User Type : </b>" + detailsObject.getString("usertype");
            txtUserType.setText(Html.fromHtml(user_type_html));
        }
        catch (JSONException e) {
            e.printStackTrace();
            LogMessage.d("Cashbook : " + "Error : " + e.toString());
            new AlertDialog.Builder(getContextInstance())
                    .setTitle("Alert!")
                    .setCancelable(false)
                    .setMessage(getResources().getString(R.string.alert_servicer_down))
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
            lstParentUserDetails.setVisibility(View.GONE);
            imgNoData.setVisibility(View.VISIBLE);
        }
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
            LogMessage.e("Error in error dialog");
            LogMessage.e("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(getContextInstance(), message);
            }
            catch (Exception e) {
                LogMessage.e("Error in toast message");
                LogMessage.e("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }
    // [END]

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

}
