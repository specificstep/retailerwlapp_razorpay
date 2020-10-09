package specificstep.com.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import specificstep.com.Activities.HomeActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.CheckConnection;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Color;
import specificstep.com.Models.Company;
import specificstep.com.Models.ComplainReasonModel;
import specificstep.com.Models.Product;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.LogMessage;
import specificstep.com.utility.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class RechargeStatusFragment extends Fragment {

    Context context;
    ImageView imgLogo, imgStatus;
    LinearLayout lnrRechargeStatus, lnrRechargeReceipt;
    TextView txtStatus, txtAmount, txtDate, txtName, txtWallet, txtTransactionId,
            txtOperatorId, txtCompany;
    Button btnComplain,btnClose;
    ImageView imgLogoReceipt, imgStatusReceipt;
    TextView txtStatusReceipt, txtAmountReceipt, txtDateReceipt, txtNameReceipt,
            txtWalletReceipt, txtTransactionIdReceipt, txtOperatorIdReceipt,
            txtCompanyReceipt, txtRechargeAgain;
    LinearLayout lnrRechargeAgain, lnrDownloadReceipt;
    View view;
    String recharge_response, from, receipt;
    DatabaseHelper databaseHelper;
    private ArrayList<User> userArrayList;
    String strMacAddress, strUserName, strOtpCode;
    private ArrayList<Company> companyArrayList;
    private ArrayList<Product> productArrayList;
    private SharedPreferences sharedPreferences;
    private Constants constants;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    String mo_no = "", recharge_id = "", recharge_status = "", date_time = "",
            company_id = "", amount = "", balance = "", product_id = "",
            operator_id = "", service = "", company_name = "", product_name = "";
    ArrayList<Color> colorArrayList;
    String _color_name, color_value;
    private TransparentProgressDialog transparentProgressDialog;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_REASON = 3;
    private AlertDialog alertDialog_1;
    BottomSheetDialog alertDialogBuilder;
    List<ComplainReasonModel> reasonModelList;
    ComplainReasonModel reasonModel;
    List<String> reasonList;
    private ArrayAdapter<String> adapterCircleName;

    public RechargeStatusFragment() {
        // Required empty public constructor
    }

    private Context getContextInstance() {
        if (context == null) {
            context = RechargeStatusFragment.this.getActivity();
            return context;
        } else {
            return context;
        }
    }

    private HomeActivity mainActivity() {
        return ((HomeActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recharge_status, container, false);
        initialize();
        try {

            recharge_response = getArguments().getString("key");
            from = getArguments().getString("from");

            JSONObject object = new JSONObject(recharge_response);

            Dlog.d("recharge response: " + recharge_response);
            recharge_id = object.getString("rechargeid");
            recharge_status = object.getString("recharge_status");
            date_time = object.getString("datetime");
            mo_no = object.getString("mobile");
            company_id = object.getString("company");
            amount = object.getString("amount");
            balance = object.getString("balance");
            product_id = object.getString("product");
            operator_id = object.getString("operatorid");
            service = object.getString("service");
            // Decimal format
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat format = new DecimalFormat("0.#");
            format.setDecimalFormatSymbols(symbols);

            if(service.equals(Constants.mobile_prepaid_id)) {
                companyArrayList = databaseHelper.getCompanyDetails("Mobile");
            } else if(service.equals(Constants.dth_id)){
                companyArrayList = databaseHelper.getCompanyDetails("DTH");
            } else if(service.equals(Constants.electricity_id)){
                companyArrayList = databaseHelper.getCompanyDetails("ELECTRICITY");
            } else if(service.equals(Constants.gas_id)){
                companyArrayList = databaseHelper.getCompanyDetails("GAS");
            } else if(service.equals(Constants.mobile_postpaid_id)){
                companyArrayList = databaseHelper.getCompanyDetails("MOBILE_POSTPAID");
            } else if(service.equals(Constants.water_id)){
                companyArrayList = databaseHelper.getCompanyDetails("WATER");
            }

            for (int i = 0; i < companyArrayList.size(); i++) {
                if (company_id.equals(companyArrayList.get(i).getId())) {
                    company_name = companyArrayList.get(i).getCompany_name();
                }
            }

            productArrayList = databaseHelper.getProductDetails(company_id);
            for (int i = 0; i < productArrayList.size(); i++) {
                if (product_id.equals(productArrayList.get(i).getId())) {
                    product_name = productArrayList.get(i).getProduct_name();
                }
            }

            if(service.equals(Constants.mobile_prepaid_id)) {
                mainActivity().getSupportActionBar().setTitle("Mobile Prepaid Recharge Bill");
                txtRechargeAgain.setText("Recharge Again");
            } else if(service.equals(Constants.dth_id)) {
                mainActivity().getSupportActionBar().setTitle("DTH Recharge Bill");
                txtRechargeAgain.setText("Recharge Again");
            } else if(service.equals(Constants.electricity_id)) {
                mainActivity().getSupportActionBar().setTitle("Electricity Bill");
                txtRechargeAgain.setText("Bill Pay Again");
            } else if(service.equals(Constants.gas_id)) {
                mainActivity().getSupportActionBar().setTitle("Gas Bill");
                txtRechargeAgain.setText("Bill Pay Again");
            } else if(service.equals(Constants.mobile_postpaid_id)) {
                mainActivity().getSupportActionBar().setTitle("Mobile Postpaid Bill");
                txtRechargeAgain.setText("Bill Pay Again");
            } else if(service.equals(Constants.water_id)) {
                mainActivity().getSupportActionBar().setTitle("Water Bill");
                txtRechargeAgain.setText("Bill Pay Again");
            }
            txtTransactionId.setText("Transaction Id: " + recharge_id);
            txtTransactionIdReceipt.setText(txtTransactionId.getText().toString());
            if(operator_id.equals("")) {
                txtOperatorId.setVisibility(View.GONE);
                txtOperatorIdReceipt.setVisibility(View.GONE);
            } else {
                txtOperatorId.setVisibility(View.VISIBLE);
                txtOperatorId.setText("Operator Id: " + operator_id + "");
                txtOperatorIdReceipt.setVisibility(View.VISIBLE);
                txtOperatorIdReceipt.setText(txtOperatorId.getText().toString());
            }
            String statusString = /*"Recharge Payment " + */"<b>" + recharge_status + "</b>";
            txtStatus.setText(Html.fromHtml(statusString));
            txtStatusReceipt.setText(txtStatus.getText().toString());
            txtDate.setText(Constants.commonDateFormate(date_time,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy hh:mm aa"));
            txtDateReceipt.setText(txtDate.getText().toString());
            txtName.setText(mo_no);
            txtNameReceipt.setText(txtName.getText().toString());
            txtAmount.setText(getContextInstance().getResources().getString(R.string.Rs) + " " + amount);
            txtAmountReceipt.setText(txtAmount.getText().toString());
            txtWallet.setText(getContextInstance().getResources().getString(R.string.Rs) + " " + balance);
            txtWalletReceipt.setText(txtWallet.getText().toString());
            txtCompany.setText(company_name + " - " + product_name);
            txtCompanyReceipt.setText(txtCompany.getText().toString());

            String logo = databaseHelper.getCompanyLogo(company_name);
            if(!TextUtils.isEmpty(logo)){
                Picasso.with(context).load(logo).placeholder(R.drawable.placeholder_icon).into(imgLogo);
                Picasso.with(context).load(logo).placeholder(R.drawable.placeholder_icon).into(imgLogoReceipt);
            }

            if(recharge_status.equalsIgnoreCase("Success")) {
                imgStatus.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_check));
                imgStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorGreen), android.graphics.PorterDuff.Mode.SRC_IN);
                imgStatusReceipt.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_check));
                imgStatusReceipt.setColorFilter(ContextCompat.getColor(context, R.color.colorGreen), android.graphics.PorterDuff.Mode.SRC_IN);
            } else if(recharge_status.equalsIgnoreCase("Failed")) {
                imgStatus.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_uncheck));
                imgStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
                imgStatusReceipt.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_uncheck));
                imgStatusReceipt.setColorFilter(ContextCompat.getColor(context, R.color.colorRed), android.graphics.PorterDuff.Mode.SRC_IN);
            } else if(recharge_status.equalsIgnoreCase("Pending")) {
                imgStatus.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_pending));
                imgStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorDefault), android.graphics.PorterDuff.Mode.SRC_IN);
                imgStatusReceipt.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_pending));
                imgStatusReceipt.setColorFilter(ContextCompat.getColor(context, R.color.colorDefault), android.graphics.PorterDuff.Mode.SRC_IN);
            } else if(recharge_status.equalsIgnoreCase("credit")) {
                imgStatus.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_pending));
                imgStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorDefault), android.graphics.PorterDuff.Mode.SRC_IN);
                imgStatusReceipt.setImageDrawable(getContextInstance().getResources().getDrawable(R.drawable.ic_pending));
                imgStatusReceipt.setColorFilter(ContextCompat.getColor(context, R.color.colorDefault), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            lnrRechargeAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if(service.equals("1")) {
                        sharedPreferences.edit().putString(constants.MOBILENUMBER, mo_no).commit();
                        sharedPreferences.edit().putString(constants.AMOUNT, amount).commit();
                        sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                    } else if(service.equals("2")) {*/
                        sharedPreferences.edit().putString(constants.MOBILENUMBER, mo_no).commit();
                        sharedPreferences.edit().putString(constants.AMOUNT, amount).commit();
                        sharedPreferences.edit().putBoolean(constants.isClicked, true).commit();
                        sharedPreferences.edit().putString(Constants.RECHARGEFROM, from).commit();
                        //}
                    try {
                        getFragmentManager().popBackStack();
                    } catch (Exception e) {
                        Dlog.d(e.toString());
                    }
                }
            });

            lnrDownloadReceipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        readContactPermission();
                    } else {
                        takeScreenshot(mo_no,recharge_id,from,from);
                    }
                }
            });

            if(recharge_status.equalsIgnoreCase("Success") || recharge_status.equalsIgnoreCase("Pending")) {
                btnComplain.setVisibility(View.VISIBLE);
            } else {
                btnComplain.setVisibility(View.GONE);
            }

            btnComplain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recharge_status.equalsIgnoreCase("Success") || recharge_status.equalsIgnoreCase("Pending")) {
                        //showComplainDialog();
                        makeComplainListCall();
                    }
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

        } catch (Exception e) {
            Dlog.d("Bundle error: " + e.toString());
        }

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
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                        try {
                            if(getFragmentManager() != null) {
                                getFragmentManager().popBackStackImmediate();
                            }
                        } catch (Exception e) {
                            Dlog.d(e.toString());
                        }
                    return true;
                }
                return false;
            }
        });
    }

    private void readContactPermission() {
        LogMessage.i("Checking permission.");
        // BEGIN_INCLUDE(READ_CONTACTS)
        // Check if the READ_CONTACTS permission is already available.
        if (ActivityCompat.checkSelfPermission(getContextInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Phone state permission has not been granted.
            requestReadContactPermission();
        } else {
            // Read SMS permissions is already available, show the camera preview.
            LogMessage.i("Read contact permission has already been granted.");
            takeScreenshot(mo_no,recharge_id,from,from);
        }
        // END_INCLUDE(READ_PHONE_STATE)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        @SuppressLint("RestrictedApi") List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            LogMessage.i("Received response for Read SMS permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Read SMS permission has been granted
                LogMessage.i("Write external permission has now been granted.");
                // Ask user for grand READ_PHONE_STATE permission.
                readContactPermission();
            } else {
                LogMessage.i("Write external permission was NOT granted.");
                Utility.amountToast(context, "Until you grant the permission, we cannot download the receipt");
                // again force fully prompt to user for grand permission.
                //readContactPermission();
            }
            // END_INCLUDE(permission_result)
        }
    }

    private void requestReadContactPermission() {
        LogMessage.i("Read phone state permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(READ_PHONE_STATE)
        if (ActivityCompat.shouldShowRequestPermissionRationale(RechargeStatusFragment.this.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            LogMessage.i("Displaying READ_CONTACTS permission rationale to provide additional context.");
            // Force fully user to grand permission
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // READ_CONTACTS permission has not been granted yet. Request it directly.
            //requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        // END_INCLUDE(READ_PHONE_STATE)
    }


    public void initialize() {

        databaseHelper = new DatabaseHelper(getActivity());
        constants = new Constants();
        transparentProgressDialog = new TransparentProgressDialog(getContextInstance(), R.drawable.fotterloading);
        sharedPreferences = getContextInstance().getSharedPreferences(constants.SHAREEDPREFERENCE, Context.MODE_PRIVATE);
        imgLogo = (ImageView) view.findViewById(R.id.imgRechargeStatusCompany);
        imgStatus = (ImageView) view.findViewById(R.id.imgRechargeStatusIcon);
        txtStatus = (TextView) view.findViewById(R.id.txtRechargeStatus);
        txtAmount = (TextView) view.findViewById(R.id.txtRechargeStatusAmount);
        txtDate = (TextView) view.findViewById(R.id.txtRechargeStatusDate);
        txtName = (TextView) view.findViewById(R.id.txtRechargeStatusName);
        txtWallet = (TextView) view.findViewById(R.id.txtRechargeStatusWallet);
        txtTransactionId = (TextView) view.findViewById(R.id.txtRechargeStatusTransactionId);
        txtOperatorId = (TextView) view.findViewById(R.id.txtRechargeStatusOperatorId);
        btnComplain = (Button) view.findViewById(R.id.btnRechargeStatusComplain);
        btnClose = (Button) view.findViewById(R.id.btnRechargeStatusClose);
        lnrRechargeAgain = (LinearLayout) view.findViewById(R.id.lnrRechargeStatusRechargeAgain);
        lnrDownloadReceipt = (LinearLayout) view.findViewById(R.id.lnrRechargeStatusDownload);
        txtCompany = (TextView) view.findViewById(R.id.txtRechargeCompany);
        lnrRechargeStatus = (LinearLayout) view.findViewById(R.id.lnrRechargeStatus);
        txtRechargeAgain = (TextView) view.findViewById(R.id.txtRechargeStatusRechargeAgain);

        lnrRechargeReceipt = (LinearLayout) view.findViewById(R.id.lnrRechargeReceipt);
        imgLogoReceipt = (ImageView) view.findViewById(R.id.imgRechargeStatusCompanyReceipt);
        imgStatusReceipt = (ImageView) view.findViewById(R.id.imgRechargeStatusIconReceipt);
        txtStatusReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusReceipt);
        txtAmountReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusAmountReceipt);
        txtDateReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusDateReceipt);
        txtNameReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusNameReceipt);
        txtWalletReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusWalletReceipt);
        txtTransactionIdReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusTransactionIdReceipt);
        txtOperatorIdReceipt = (TextView) view.findViewById(R.id.txtRechargeStatusOperatorIdReceipt);
        txtCompanyReceipt = (TextView) view.findViewById(R.id.txtRechargeCompanyReceipt);

        alertDialogBuilder = new BottomSheetDialog(getActivity());

        userArrayList = databaseHelper.getUserDetail();
        strMacAddress = userArrayList.get(0).getDevice_id();
        strUserName = userArrayList.get(0).getUser_name();
        strOtpCode = userArrayList.get(0).getOtp_code();
        companyArrayList = new ArrayList<Company>();
        productArrayList = new ArrayList<Product>();

    }

    Bitmap bitmap;
    View v1 = null;
    private void takeScreenshot(String mobile, String payment_id, String type, String receipt) {
        final String folder_name = "/" + Constants.changeAppName(getActivity()) + "/" + receipt + "/";
        try {
            File mydir = new File(Environment.getExternalStorageDirectory() + folder_name);
            if (!mydir.exists())
                mydir.mkdirs();
            else
                Dlog.d("error: dir. already exists");
            v1 = view.findViewById(R.id.lnrRechargeReceipt);
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
                        .make(lnrRechargeStatus, "Payment Receipt download successfully.", Snackbar.LENGTH_LONG)
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
            Toast.makeText(getActivity(), "Payment Receipt download fail.", Toast.LENGTH_LONG).show();
        }
    }

    private void openGeneratedPDF(String path) {
        File file = new File(path);
        Intent target = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT <= 19) {
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
        } else {
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(getActivity(), Constants.PACKAGE_NAME + ".provider", file);
            target.setDataAndType(uri, "application/pdf");
        }
        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
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
            if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            } else if (msg.what == SUCCESS_REASON) {
                parseComplainReasonResponse(msg.obj.toString());
            }
        }
    };

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            new android.app.AlertDialog.Builder(getActivity())
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
                Utility.toast(getActivity(), message);
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
                String decrypted_response = Constants.decryptAPI(getActivity(), encrypted_response);
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

    /*private void showComplainDialog() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.recharge_complain_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        colorArrayList = databaseHelper.getAllColors();

        TextView tv_order_id = (TextView) promptsView.findViewById(R.id.tv_order_id_complain);
        TextView tv_mo_no = (TextView) promptsView.findViewById(R.id.tv_mo_no_complain);
        TextView tv_amount = (TextView) promptsView.findViewById(R.id.tv_amount_complain);
        TextView tv_date_time = (TextView) promptsView.findViewById(R.id.tv_date_time_complain);
        TextView tv_status = (TextView) promptsView.findViewById(R.id.tv_status_complain);
        TextView tv_company_name = (TextView) promptsView.findViewById(R.id.tv_company_name_complain);
        ImageView iv_company_logo = (ImageView) promptsView.findViewById(R.id.iv_company_complain);

        final EditText edt_report_complain = (EditText) promptsView.findViewById(R.id.edt_report_complain);
        LinearLayout llOperatorId;
        TextView txtOperator;
        txtOperator = (TextView) promptsView.findViewById(R.id.txt_operator_id_complain);
        llOperatorId = (LinearLayout) promptsView.findViewById(R.id.ll_operator_id_complain);

        Spinner spinner = (Spinner) promptsView.findViewById(R.id.spinner_complain);

        //Set the text color of the Spinner's selected view (not a drop down list view)
        spinner.setSelection(0, true);
        View view = spinner.getSelectedView();
        ((TextView) view).setTextColor(context.getResources().getColor(R.color.colorWhite));
        ((TextView) view).setTextSize(14);

        //Set the listener for when each option is clicked.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Change the selected item's text color
                ((TextView) view).setTextColor(context.getResources().getColor(R.color.colorWhite));
                ((TextView) view).setTextSize(14);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tv_order_id.setText(" " + recharge_id);
        tv_mo_no.setText(" " + mo_no);
        tv_amount.setText(getContextInstance().getResources().getString(R.string.Rs) + " " + amount);
        tv_date_time.setText(Constants.commonDateFormate(date_time,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy hh:mm aa"));

         //[START] - Data proper not display ("Postpaid") (MAX - 10 character)
        // tv_product_name.setText(recharge.getProduct_name());
        String productName = product_name;
        try {
            if (productName.trim().length() > 10) {
                String subProductName = productName.substring(0, 10);
                productName = subProductName + "\n" + productName.substring(10, productName.length());
            } else {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tv_company_name.setText(company_name + " - " + productName);
        // [END]

         //[START] - Display operator id (If data not found, hide this field)
        if (operator_id.length() == 0
                || operator_id == null
                || operator_id.trim().equalsIgnoreCase("null")) {
            llOperatorId.setVisibility(View.GONE);
            txtOperator.setText(" " + operator_id);
        } else {
            llOperatorId.setVisibility(View.VISIBLE);
            txtOperator.setText(" " + operator_id);
        }
        // [END]

        //Set color of recharge status
        if (recharge_status.equalsIgnoreCase("success")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("success")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge_status);
        } else if (recharge_status.equalsIgnoreCase("pending")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("pending")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge_status);
        } else if (recharge_status.equalsIgnoreCase("failure")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("failure")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge_status);
        }
        // [START] - recharge_status":"Credit"
        else if (recharge_status.equalsIgnoreCase("credit")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("credit")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge_status);
        }
        // [END]


        String company_logo = databaseHelper.getCompanyLogo(company_name);
        if (TextUtils.isEmpty(company_logo)) {
            iv_company_logo.setImageResource(R.drawable.placeholder_icon);
        } else {
            Picasso.with(context).load(company_logo).placeholder(R.drawable.placeholder_icon).into(iv_company_logo);
        }

        // set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //submitComplainClickListener.onComplainClick(position, edt_report_complain.getText().toString().trim());
                                Log.e("onComplainClick", " Complain Text " + edt_report_complain.getText().toString().trim());

                                showProgressDialog();

                                CheckConnection checkConnection = new CheckConnection();
                                if (checkConnection.isConnectingToInternet(getContextInstance()) == true) {
                                    addComplain(edt_report_complain.getText().toString().trim());
                                } else {
                                    dismissProgressDialog();
                                    Utility.toast(getContextInstance(), "Check your internet connection");
                                }

                                Constants.isDialogOpen = false ;
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Constants.isDialogOpen = false ;
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // show dialog
        if (!alertDialog.isShowing()) {
            Constants.isDialogOpen = true ;
            alertDialog.show();
        }

    }*/

    public void showComplainDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    // get prompts.xml view
                    alertDialogBuilder = new BottomSheetDialog(getActivity());
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

                    colorArrayList = databaseHelper.getAllColors();

                    if (reasonList.size() > 0) {
                        adapterCircleName = new ArrayAdapter<String>(getActivity(), R.layout.adapter_spinner, reasonList);
                        // set adapter in circle spinner
                        spinner.setAdapter(adapterCircleName);
                    }

                    String order_add = "<b> Order Id: </b>" + recharge_id;
                    tv_order_id.setText(Html.fromHtml(order_add));
                    tv_mo_no.setText(" " + mo_no);
                    tv_amount.setText(Constants.addRsSymbol(getActivity(), amount));
                    tv_date_time.setText(Constants.commonDateFormate(date_time,"yyyy-MM-dd HH:mm:ss","dd-MMM-yyyy hh:mm aa"));

                    /* [START] - Data proper not display ("Postpaid") (MAX - 10 character) */
                    // tv_product_name.setText(recharge.getProduct_name());
                    String productName = product_name;
                    try {
                        if (productName.trim().length() > 10) {
                            String subProductName = productName.substring(0, 10);
                            tv_company_name.setText(company_name + " - " + subProductName + "\n" + productName.substring(10, productName.length()));
                        } else {
                            tv_company_name.setText(company_name + " - " + productName);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        tv_company_name.setText(company_name + " - " + productName);
                    }
                    // [END]

                    /* [START] - Display operator id (If data not found, hide this field) */
                    if (operator_id.trim().length() == 0
                            || operator_id == null
                            || operator_id.trim().equalsIgnoreCase("null")) {
                        llOperatorId.setVisibility(View.GONE);
                    } else {
                        llOperatorId.setVisibility(View.VISIBLE);
                        String operator_add = "<b> Operator Id: </b>" + operator_id;
                        txtOperatorId.setText(Html.fromHtml(operator_add));
                    }
                    // [END]

                    /*Set color of recharge status*/
                    if (recharge_status.equalsIgnoreCase("success")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("success")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge_status);
                    } else if (recharge_status.equalsIgnoreCase("pending")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("pending")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge_status);
                    } else if (recharge_status.equalsIgnoreCase("failure")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("failure")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge_status);
                    }
                    /* [START] - recharge_status":"Credit" */
                    else if (recharge_status.equalsIgnoreCase("credit")) {
                        for (int i = 0; i < colorArrayList.size(); i++) {
                            _color_name = colorArrayList.get(i).getColor_name();
                            if (_color_name.contains("credit")) {
                                color_value = colorArrayList.get(i).getColo_value();
                                tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                            }
                        }
                        tv_status.setText(recharge_status);
                    }
                    // [END]

                    String company_logo = databaseHelper.getCompanyLogo(company_name);
                    if (TextUtils.isEmpty(company_logo)) {
                        iv_company_logo.setImageResource(R.drawable.placeholder_icon);
                    } else {
                        Picasso.with(getActivity()).load(company_logo).placeholder(R.drawable.placeholder_icon).into(iv_company_logo);
                    }

                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showProgressDialog();
                            /*call webservice only if user is connected with internet*/
                            CheckConnection checkConnection = new CheckConnection();
                            if (checkConnection.isConnectingToInternet(getActivity()) == true) {
                                addComplain(edt_report_complain.getText().toString().trim(), reasonModelList.get(spinner.getSelectedItemPosition()).getId());
                            } else {
                                dismissProgressDialog();
                                Utility.toast(getActivity(), "Check your internet connection");
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

    private void addComplain(final String complainMessage, final String reason_id) {
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
                            strUserName,
                            strMacAddress,
                            strOtpCode,
                            recharge_id,
                            "",
                            amount,
                            mo_no,
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
            }else{
                displayComplainDialog(jsonObject.getString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Utility.toast(getContextInstance(), "Please check your internet access");
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
