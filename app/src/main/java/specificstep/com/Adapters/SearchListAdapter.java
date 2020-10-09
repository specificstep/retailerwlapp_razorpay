package specificstep.com.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import specificstep.com.Activities.ListDetailActivity;
import specificstep.com.Database.DatabaseHelper;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.Color;
import specificstep.com.Models.ComplainReasonModel;
import specificstep.com.Models.Recharge;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.Utility;

/**
 * Created by ubuntu on 19/1/17.
 */

public class SearchListAdapter extends BaseAdapter {

    ArrayList<Recharge> rechargeArrayList;
    Context context;
    DatabaseHelper databaseHelper;
    LayoutInflater inflater;
    TextView tv_order_id,
            tv_mo_no, tv_amount, tv_status,
            tv_trans_search_date;
    LinearLayout tvComplain;
    LinearLayout llOperatorId;
    ImageView iv_company_logo;
    ArrayList<Color> colorArrayList;
    String _color_name, color_value;
    LinearLayout lnrTransactionSearch;

    //load complain reasons feature
    public int click_pos;
    ArrayList<User> userArrayList;
    private final int SUCCESS = 1, ERROR = 2;
    List<ComplainReasonModel> reasonModelList;
    ComplainReasonModel reasonModel;
    List<String> reasonList;
    BottomSheetDialog alertDialogBuilder;
    private ArrayAdapter<String> adapterCircleName;
    String receipt_type;

    private SubmitComplainClickListener submitComplainClickListener;

    public interface SubmitComplainClickListener {
        void onComplainClick(int position, String complainText, String reason_id);
    }

    public SearchListAdapter(Context activity, ArrayList<Recharge> rechargeArrayList, SubmitComplainClickListener submitComplainClickListener, String receipt_type) {

        context = activity;
        this.rechargeArrayList = rechargeArrayList;
        this.submitComplainClickListener = submitComplainClickListener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        databaseHelper = new DatabaseHelper(context);
        colorArrayList = databaseHelper.getAllColors();
        this.receipt_type = receipt_type;

    }

    @Override
    public int getCount() {
        return rechargeArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return rechargeArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.adapter_search_trans, null);

        tv_order_id = (TextView) convertView.findViewById(R.id.tv_order_id_adapter_rec_trans);
        tv_mo_no = (TextView) convertView.findViewById(R.id.tv_mo_no_adapter_rec_trans);
        tv_amount = (TextView) convertView.findViewById(R.id.tv_amount_adapter_rec_trans);
        tv_status = (TextView) convertView.findViewById(R.id.tv_status_adapter_rec_trans);
        iv_company_logo = (ImageView) convertView.findViewById(R.id.iv_company_adapter_rec_trans);
        tv_trans_search_date = (TextView) convertView.findViewById(R.id.txtTransSearchDate);
        lnrTransactionSearch = (LinearLayout) convertView.findViewById(R.id.lnrTransSearch);
        tvComplain = (LinearLayout) convertView.findViewById(R.id.tv_complain_adapter_rec_trans);
        tvComplain.setTag(position);

        final Recharge recharge = rechargeArrayList.get(position);

        tvComplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recharge.getRecharge_status().equalsIgnoreCase("success") || recharge.getRecharge_status().equalsIgnoreCase("pending")) {
                    if (v.getId() == R.id.tv_complain_adapter_rec_trans) {
                        click_pos = (Integer) v.getTag();
                        //stop timer
                        makeComplainListCall();
                    }
                }
            }
        });

        tv_order_id.setText(recharge.getClient_trans_id());
        tv_mo_no.setText(recharge.getMo_no());
        tv_amount.setText(Constants.addRsSymbol((Activity) context,recharge.getAmount()));

        String productName = recharge.getProduct_name();
        try {
            if (productName.trim().length() > 10) {
                String subProductName = productName.substring(0, 10);
                String productTemp = subProductName + "\n" + productName.substring(10, productName.length());
            } else {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // [END]

        /*Set color of recharge status*/
        if (recharge.getRecharge_status().equalsIgnoreCase("success")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("success")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setTextColor(android.graphics.Color.parseColor(color_value));
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
            tvComplain.setVisibility(View.VISIBLE);
        } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("pending")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setTextColor(android.graphics.Color.parseColor(color_value));
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
            tvComplain.setVisibility(View.VISIBLE);
        } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("failure")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setTextColor(android.graphics.Color.parseColor(color_value));
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
            tvComplain.setVisibility(View.GONE);
        }
        /* [START] - recharge_status":"Credit" */
        else if (recharge.getRecharge_status().equalsIgnoreCase("credit")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("credit")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setTextColor(android.graphics.Color.parseColor(color_value));
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
            tvComplain.setVisibility(View.GONE);
        }
        // [END]

        String company_logo = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
        if (!TextUtils.isEmpty(company_logo)) {
            Picasso.with(context).load(company_logo).placeholder(R.drawable.placeholder_icon).into(iv_company_logo);
        } else {
            iv_company_logo.setImageResource(R.drawable.placeholder_icon);
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String yesterday = df.format(cal.getTime());

            Calendar tcal = Calendar.getInstance();
            String today = df.format(tcal.getTime());

            if (position == 0) {
                tv_trans_search_date.setVisibility(View.VISIBLE);
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date newDate = null;
                newDate = df1.parse(rechargeArrayList.get(0).getTrans_date_time());
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate1 = destDf.format(newDate);

                if (!newDate1.equals(today)) {
                    if (yesterday.equals(newDate1)) {
                        tv_trans_search_date.setText("Yesterday, " + newDate1);
                    } else {
                        tv_trans_search_date.setText(newDate1 + "");
                    }
                } else {
                    tv_trans_search_date.setText("Today, " + newDate1);
                }
            } else {
                DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                // parse the date string into Date object
                Date date = srcDf.parse(recharge.getTrans_date_time());
                DateFormat destDf = new SimpleDateFormat("dd-MMM-yyyy");
                // format the date into another format
                String newDate = destDf.format(date);
                if (Constants.commonDateFormate(recharge.getTrans_date_time(),"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy").equals(Constants.commonDateFormate(rechargeArrayList.get(position - 1).getTrans_date_time(),"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy"))) {
                    tv_trans_search_date.setVisibility(View.GONE);
                } else {
                    tv_trans_search_date.setVisibility(View.VISIBLE);
                    if (!newDate.equals(today)) {
                        if (yesterday.equals(newDate)) {
                            tv_trans_search_date.setText("Yesterday, " + newDate);
                        } else {
                            tv_trans_search_date.setText(newDate + "");
                        }
                    } else {
                        tv_trans_search_date.setText("Today, " + newDate);
                    }
                }
            }

            lnrTransactionSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ListDetailActivity.class);
                    intent.putExtra("classdata", (Parcelable) recharge);
                    intent.putExtra("from", "transsearch");
                    intent.putExtra("receipt_type", receipt_type);
                    /*if(TransSearchFragment.listState) {
                        intent.putExtra("from", "rectransact");
                    } else {
                        intent.putExtra("from", "transsearch");
                    }*/
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            Dlog.d("Transaction Search: " + e.toString());
        }

        return convertView;
    }

    private void showComplainDialog(final int position) {

        // get prompts.xml view
        alertDialogBuilder = new BottomSheetDialog(context);
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
        llOperatorId = (LinearLayout) alertDialogBuilder.findViewById(R.id.ll_operator_id_complain);
        final Spinner spinner = (Spinner) alertDialogBuilder.findViewById(R.id.spinner_complain);

        if(reasonList.size()>0) {
            adapterCircleName = new ArrayAdapter<String>(context, R.layout.adapter_spinner, reasonList);
            // set adapter in circle spinner
            spinner.setAdapter(adapterCircleName);
        }

        Recharge recharge = rechargeArrayList.get(position);
        String order_add = "<b> Order Id: </b>" + recharge.getClient_trans_id();
        tv_order_id.setText(Html.fromHtml(order_add));
        tv_mo_no.setText(" " + recharge.getMo_no());
        tv_amount.setText(addRsSymbol(recharge.getAmount()));
        tv_date_time.setText(Constants.commonDateFormate(recharge.getTrans_date_time(),"yyyy-MM-dd hh:mm:ss","dd-MMM-yyyy hh:mm aa"));

        /* [START] - Data proper not display ("Postpaid") (MAX - 10 character) */
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
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
        } else if (recharge.getRecharge_status().equalsIgnoreCase("pending")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("pending")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
        } else if (recharge.getRecharge_status().equalsIgnoreCase("failure")) {
            for (int i = 0; i < colorArrayList.size(); i++) {
                _color_name = colorArrayList.get(i).getColor_name();
                if (_color_name.contains("failure")) {
                    color_value = colorArrayList.get(i).getColo_value();
                    tv_status.setBackgroundColor(android.graphics.Color.parseColor(color_value));
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
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
                    tv_amount.setTextColor(android.graphics.Color.parseColor(color_value));
                }
            }
            tv_status.setText(recharge.getRecharge_status());
        }
        // [END]

        String company_logo = databaseHelper.getCompanyLogo(recharge.getCompnay_name());
        if (TextUtils.isEmpty(company_logo)) {
            iv_company_logo.setImageResource(R.drawable.placeholder_icon);
        } else {
            Picasso.with(context).load(company_logo).placeholder(R.drawable.placeholder_icon).into(iv_company_logo);
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComplainClickListener.onComplainClick(position, edt_report_complain.getText().toString().trim(),reasonModelList.get(spinner.getSelectedItemPosition()).getId());
                Constants.isDialogOpen = false ;
                alertDialogBuilder.cancel();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.isDialogOpen = false ;
                alertDialogBuilder.cancel();
            }
        });

        alertDialogBuilder.show();

    }

    private String addRsSymbol(String amount) {
        String cAmount = amount;
        // Decimal format
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        // Add RS symbol in credit and debit amount
        try {
            if (!TextUtils.equals(cAmount, "0")) {
                cAmount = context.getResources().getString(R.string.currency_format, String.valueOf(format.parse(cAmount).floatValue()));
            }
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            cAmount = context.getResources().getString(R.string.currency_format, amount);
        }
        return cAmount;
    }

    /* [START] - 2017_04_28 - Add native code for transaction search, and Remove volley code */
    private void makeComplainListCall() {
        databaseHelper = new DatabaseHelper(context);
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

    // handle add complain messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                parseComplainResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                displayErrorDialog(msg.obj.toString());
            }
        }
    };


    // parse success response
    private void parseComplainResponse(String response) {
        Dlog.d("Trans Search Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equals("1")) {
                String encrypted_response = jsonObject.getString("data");
                String decrypted_response = Constants.decryptAPI(context,encrypted_response);
                Dlog.d("Complain List : " + decrypted_response);

                JSONArray array = new JSONArray(decrypted_response);
                if(array.length()>0) {
                    reasonModelList = new ArrayList<ComplainReasonModel>();
                    reasonList = new ArrayList<String>();
                    for(int i=0;i<array.length();i++) {
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
                showComplainDialog(click_pos);
            } else {
                reasonList.add("Recharge Status Success but not get benefit");
                showComplainDialog(click_pos);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            reasonList.add("Recharge Status Success but not get benefit");
            showComplainDialog(click_pos);
        }
    }

    // display error in dialog
    private void displayErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Info!")
                    .setCancelable(false)
                    .setMessage(message)
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(context, message);
            }
            catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

}
