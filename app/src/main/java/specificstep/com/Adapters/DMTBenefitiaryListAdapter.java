package specificstep.com.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import specificstep.com.Database.DatabaseHelper;
import specificstep.com.Fragments.DMTBenefitiaryListFragment;
import specificstep.com.Fragments.DMTPaymentFragment;
import specificstep.com.GlobalClasses.Constants;
import specificstep.com.GlobalClasses.TransparentProgressDialog;
import specificstep.com.GlobalClasses.URL;
import specificstep.com.Models.DMTSenderBeneficiaryModel;
import specificstep.com.Models.User;
import specificstep.com.R;
import specificstep.com.utility.Dlog;
import specificstep.com.utility.InternetUtil;
import specificstep.com.utility.Utility;

public class DMTBenefitiaryListAdapter extends RecyclerView.Adapter<DMTBenefitiaryListAdapter.MyViewHolder> {

    List<DMTSenderBeneficiaryModel> dataSet;
    public Context context;
    ArrayList<User> userArrayList;
    DatabaseHelper databaseHelper;
    private final int SUCCESS = 1, ERROR = 2, SUCCESS_VERIFY = 3;
    private TransparentProgressDialog transparentProgressDialog;
    private AlertDialog alertDialog_3;
    public static int position, verifyPos;
    public Fragment fragment;
    MyViewHolder holder1;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtFirstName, txtBankName;
        Button btnImps, btnDelete, btnVerify;
        LinearLayout lnrVerified;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.txtFirstName = (TextView) itemView.findViewById(R.id.txtDMTBenefitiaryFirstName);
            this.txtBankName = (TextView) itemView.findViewById(R.id.txtDMTBenefitiaryBankName);
            this.btnImps = (Button) itemView.findViewById(R.id.btnImps);
            this.btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
            this.btnVerify = (Button) itemView.findViewById(R.id.btnVerify);
            this.lnrVerified = (LinearLayout) itemView.findViewById(R.id.lnrVerified);
        }
    }

    public DMTBenefitiaryListAdapter(Context con, Fragment frag, List<DMTSenderBeneficiaryModel> data) {
        this.context = con;
        this.dataSet = data;
        this.fragment = frag;
        databaseHelper = new DatabaseHelper(context);
        userArrayList = new ArrayList<User>();
        transparentProgressDialog = new TransparentProgressDialog(context, R.drawable.fotterloading);
        userArrayList = databaseHelper.getUserDetail();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_benefitiary_list, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        holder1 = holder;
        holder.txtFirstName.setText(dataSet.get(listPosition).getFirstname() + " " + dataSet.get(listPosition).getLastname() + " (" + dataSet.get(listPosition).getMobile_number() + ")");
        holder.txtBankName.setText(dataSet.get(listPosition).getBank_name() + " (" + dataSet.get(listPosition).getAccount_type() + ")\n" + "Account No: " + dataSet.get(listPosition).getAccount_number() + "\nIFSC Code: " + dataSet.get(listPosition).getIfsc_code());
        holder.btnImps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DMTPaymentFragment rechargeMainFragment = new DMTPaymentFragment();
                Bundle args = new Bundle();
                args.putString("benefitiary_id", dataSet.get(listPosition).getId());
                rechargeMainFragment.setArguments(args);
                FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, rechargeMainFragment).addToBackStack(rechargeMainFragment.toString() + "").commit();
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = listPosition;
                makeBeneficiary();
            }
        });

        if (dataSet.get(listPosition).getAccount_verified().equals("0")) {
            holder.btnVerify.setVisibility(View.VISIBLE);
            holder.lnrVerified.setVisibility(View.GONE);
        } else {
            holder.btnVerify.setVisibility(View.GONE);
            holder.lnrVerified.setVisibility(View.VISIBLE);
        }

        holder.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPos = listPosition;
                makeBeneficiaryVerifyCall();
            }
        });

    }

    private void makeBeneficiaryVerifyCall() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.verifyBeneficiary;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "benf_id"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION,
                            dataSet.get(verifyPos).getId()
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS_VERIFY, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void makeBeneficiary() {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = URL.deleteBeneficiary;
                    String[] parameters = {
                            "username",
                            "mac_address",
                            "otp_code",
                            "app",
                            "sender_mobile",
                            "sender_id",
                            "beneficiary_id"
                    };
                    String[] parametersValues = {
                            userArrayList.get(0).getUser_name(),
                            userArrayList.get(0).getDevice_id(),
                            userArrayList.get(0).getOtp_code(),
                            Constants.APP_VERSION,
                            DMTBenefitiaryListFragment.mDmtSenderModelsArrayList.get(0).getMobilenumber(),
                            DMTBenefitiaryListFragment.mDmtSenderModelsArrayList.get(0).getId(),
                            dataSet.get(position).getId()
                    };
                    String response = InternetUtil.getUrlData(url, parameters, parametersValues);
                    myHandler.obtainMessage(SUCCESS, response).sendToTarget();
                } catch (Exception ex) {
                    Dlog.d("Error : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    // parse success response
    private void parseVerifyResponse(String response) {
        Dlog.d("Verify Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.getInt("status") == 1) {
                String benf_firstname = jsonObject.getString("benf_firstname");
                String benf_lastname = jsonObject.getString("benf_lastname");
                dataSet.get(verifyPos).setFirstname(benf_firstname);
                dataSet.get(verifyPos).setLastname(benf_lastname);
                dataSet.get(verifyPos).setAccount_verified("1");
                notifyDataSetChanged();
                String detail = dataSet.get(verifyPos).getBank_name() + " (" +
                        dataSet.get(verifyPos).getAccount_type() + ")\n" +
                        "Account No: " + dataSet.get(verifyPos).getAccount_number() +
                        "\nIFSC Code: " + dataSet.get(verifyPos).getIfsc_code();
                showDialog(context, jsonObject.getString("msg") + "", benf_firstname + " " + benf_lastname, detail);
            } else {
                displayRechargeErrorDialog(jsonObject.getString("msg")+"");
//                showDialog(context, "Verified Successfully.", "John Doe", "asdasd");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showDialog(Context context, String msg, String name, String detail) {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_dmt_verify, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        dialogView.setBackgroundResource(android.R.color.transparent);
        final AlertDialog b = dialogBuilder.create();

        Button btnClose = (Button) dialogView.findViewById(R.id.btnDmtVerifyClose);
        TextView txtName = (TextView) dialogView.findViewById(R.id.txtDmtVerifyName);
        TextView txtBank = (TextView) dialogView.findViewById(R.id.txtDmtVerifyBank);
        TextView txtStatus = (TextView) dialogView.findViewById(R.id.txtDmtVerifyStatus);
        ImageView imageView = (ImageView) dialogView.findViewById(R.id.imgDmtVerifyStatus);
        LinearLayout lnrDetail = (LinearLayout) dialogView.findViewById(R.id.lnrDmtVerifyDetail);


        txtStatus.setText(msg);

        lnrDetail.setVisibility(View.VISIBLE);
        txtName.setText(name);
        txtBank.setText(detail);


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
            }
        });

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b.show();
            }
        });

    }

    // handle recent transaction messages
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS) {
                dismissProgressDialog();
                parseResponse(msg.obj.toString());
            } else if (msg.what == ERROR) {
                dismissProgressDialog();
                displayRechargeErrorDialog(msg.obj.toString());

            } else if (msg.what == SUCCESS_VERIFY) {
                dismissProgressDialog();
                parseVerifyResponse(msg.obj.toString());
            }

        }
    };

    // parse success response
    private void parseResponse(String response) {
        Dlog.d("balance Response : " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            Toast.makeText(context, jsonObject.getString("msg") + "", Toast.LENGTH_LONG).show();
            if (jsonObject.getString("msg").equals("Beneficiary Deleted Successfully")) {
                DMTBenefitiaryListFragment.mDmtBeneficiaryModelsArrayList.remove(position);

                if (DMTBenefitiaryListFragment.mDmtBeneficiaryModelsArrayList.size() > 0) {
                    DMTBenefitiaryListFragment.recyclerView.setVisibility(View.VISIBLE);
                    DMTBenefitiaryListFragment.imgNoData.setVisibility(View.GONE);
                    Collections.reverse(DMTBenefitiaryListFragment.mDmtBeneficiaryModelsArrayList);
                    DMTBenefitiaryListFragment.adapter.notifyDataSetChanged();
                } else {
                    DMTBenefitiaryListFragment.recyclerView.setVisibility(View.GONE);
                    DMTBenefitiaryListFragment.imgNoData.setVisibility(View.VISIBLE);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayRechargeErrorDialog(String message) {
        /* [START] - 2017_05_01 - Close all alert dialog logic */
        try {
            alertDialog_3 = new AlertDialog.Builder(context).create();
            alertDialog_3.setTitle("Info!");
            alertDialog_3.setCancelable(false);
            alertDialog_3.setMessage(message);
            alertDialog_3.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog_3.show();
        } catch (Exception ex) {
            Dlog.d("Error : " + ex.getMessage());
            ex.printStackTrace();
            try {
                Utility.toast(context, message);
            } catch (Exception e) {
                Dlog.d("ERROR : " + e.getMessage());
            }
        }
        // [END]
    }

    // show progress dialog
    private void showProgressDialog() {
        try {
            if (transparentProgressDialog == null) {
                transparentProgressDialog = new TransparentProgressDialog(context, R.drawable.fotterloading);
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

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


}
