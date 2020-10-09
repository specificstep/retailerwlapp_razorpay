package specificstep.com.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import specificstep.com.Models.Color;
import specificstep.com.Models.Company;
import specificstep.com.Models.DMTAddBenefitiaryBankName;
import specificstep.com.Models.Default;
import specificstep.com.Models.Product;
import specificstep.com.Models.State;
import specificstep.com.Models.User;
import specificstep.com.utility.Dlog;

/**
 * Created by ubuntu on 13/1/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 9;

    Context context;

    private static final String DATABASE_NAME = "RechargeEngine";

    private static final String TABLE_DEFAULT_SETTINGS = "default_settings";

    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_STATE_ID = "state_id";
    private static final String KEY_STATE_NAME = "state_name";

    private static final String TABLE_COMPANY = "company";

    private static final String KEY_COMPANY_ITEM_ID = "item_id";
    private static final String KEY_COMPANY_NAME = "company_name";
    private static final String KEY_LOGO = "logo";
    private static final String KEY_SERVICE_TYPE = "service_type";
    private static final String KEY_FIRST_TAG = "first_tag";
    private static final String KEY_FIRST_LENGTH = "first_length";
    private static final String KEY_FIRST_TYPE = "first_type";
    private static final String KEY_FIRST_START_WITH = "first_start_with";
    private static final String KEY_FIRST_DEFINED = "first_defined";
    private static final String KEY_SECOND_TAG = "second_tag";
    private static final String KEY_SECOND_LENGTH = "second_length";
    private static final String KEY_SECOND_TYPE = "second_type";
    private static final String KEY_SECOND_START_WITH = "second_start_with";
    private static final String KEY_SECOND_DEFINED = "second_defined";
    private static final String KEY_THIRD_TAG = "third_tag";
    private static final String KEY_THIRD_LENGTH = "third_length";
    private static final String KEY_THIRD_TYPE = "third_type";
    private static final String KEY_THIRD_START_WITH = "third_start_with";
    private static final String KEY_THIRD_DEFINED = "third_defined";
    private static final String KEY_FOURTH_TAG = "fourth_tag";
    private static final String KEY_FOURTH_LENGTH = "fourth_length";
    private static final String KEY_FOURTH_TYPE = "fourth_type";
    private static final String KEY_FOURTH_START_WITH = "fourth_start_with";
    private static final String KEY_FOURTH_DEFINED = "fourth_defined";

    private static final String TABLE_PRODUCT = "product";

    private static final String KEY_PRODUCT_ITEM_ID = "item_id";
    private static final String KEY_PRODUCT_NAME = "product_name";
    private static final String KEY_COMPANY_ID = "company_id";
    private static final String KEY_PRODUCT_LOGO = "product_logo";
    private static final String KEY_IS_PARTIAL = "is_partial";

    private static final String TABLE_STATE = "state";

    private static final String KEY_CIRCLE_ID = "circle_id";
    private static final String KEY_CIRCLE_NAME = "circle_name";

    private static final String TABLE_USER = "user";

    private static final String KEY_OTP_CODE = "otp_code";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PWD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REG_DATE = "reg_date";

    private static final String TABLE_STATUS_COLOR = "status_color";

    private static final String KEY_COLOR_NAME = "name";
    private static final String KEY_COLOR_VALUE = "value";

    private static final String TABLE_DMT_BANK = "dmt_bank";
    private static final String KEY_DMT_BANK_ID = "bank_id";
    private static final String KEY_DMT_BANK_NAME = "bank_name";
    private static final String KEY_DMT_BANK_IFSC_CODE = "ifsc_code";

    /*private static final String TABLE_WALLET_LIST = "wallet_list";
    private static final String KEY_WALLET_NAME = "wallet_name";
    private static final String KEY_WALLET_TYPE = "wallet_type";
    private static final String KEY_WALLET_BALANCE = "wallet_balance";

    private static final String TABLE_DEPOSIT_BANKS = "deposit_bank";
    private static final String KEY_DEPOSIT_BANKS_ID = "id";
    private static final String KEY_DEPOSIT_BANKS_BANK_NAME = "bank_name";
    private static final String KEY_DEPOSIT_BANKS_BALANCE = "balance";
    private static final String KEY_DEPOSIT_BANKS_ACCOUNT_NUMBER = "account_number";
    private static final String KEY_DEPOSIT_BANKS_USER_ID = "user_id";
    private static final String KEY_DEPOSIT_BANKS_PAYEE_NAME = "payee_name";
    private static final String KEY_DEPOSIT_BANKS_ACCOUNT_TYPE = "account_type";
    private static final String KEY_DEPOSIT_BANKS_IFSC_CODE = "ifsc_code";
    private static final String KEY_DEPOSIT_BANKS_BRANCH_NAME = "branch_name";

    private static final String TABLE_ALL_BANKS = "all_banks";
    private static final String KEY_ALL_BANKS_ID = "id";
    private static final String KEY_ALL_BANKS_BANK_NAME = "bank_name";
    private static final String KEY_ALL_BANKS_ADD_DATE = "add_date";
    private static final String KEY_ALL_BANKS_EDIT_DATE = "edit_date";
    private static final String KEY_ALL_BANKS_IP_ADDRESS = "ip_address";
    private static final String KEY_ALL_BANKS_CREATED_BY = "created_by";
    private static final String KEY_ALL_BANKS_UPDATED_BY = "updated_by";
    private static final String KEY_ALL_BANKS_STATUS = "status";*/

    /* [START] - Notification table name and field */
//    private static final String TABLE_NOTIFY = "tbl_notify";
//    private static final String TABLE_NOTIFY_1 = "tbl_notify_1";

//    private static final String KEY_NOTIFY_ID = "notifyId";
//    private static final String KEY_MESSAGE = "message";
//    private static final String KEY_TITLE = "title";
//    private static final String KEY_RECEIVE_DATETIME = "receive_dateTime";
//    private static final String KEY_SAVE_TIME = "save_dateTime";
//    private static final String KEY_READ_DATETIME = "read_dateTime";
//    private static final String KEY_READ_FLAG = "readFlag";
//    private static final String KEY_DELETE_FLAG = "deleteFlag";
    // [END]

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_DEFAULT_SETTINGS = "CREATE TABLE " + TABLE_DEFAULT_SETTINGS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_ID + " TEXT," + KEY_STATE_ID + " TEXT," +
                KEY_STATE_NAME + " TEXT" + ")";

        String CREATE_TABLE_COMPANY = "CREATE TABLE " + TABLE_COMPANY + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_COMPANY_ITEM_ID + " TEXT," + KEY_COMPANY_NAME + " TEXT," +
                KEY_LOGO + " TEXT, " + KEY_SERVICE_TYPE + " TEXT" + ")";

        String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_PRODUCT + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_PRODUCT_ITEM_ID + " TEXT," + KEY_PRODUCT_NAME + " TEXT," +
                KEY_COMPANY_ID + " TEXT," + KEY_PRODUCT_LOGO + " TEXT," + KEY_SERVICE_TYPE + " TEXT," +
                KEY_IS_PARTIAL + " TEXT,"  + KEY_FIRST_TAG + " TEXT, " + KEY_FIRST_LENGTH + " TEXT, " +
                KEY_FIRST_TYPE + " TEXT, " + KEY_FIRST_START_WITH + " TEXT, " +
                KEY_FIRST_DEFINED + " TEXT, " + KEY_SECOND_TAG + " TEXT, " +
                KEY_SECOND_LENGTH + " TEXT, " + KEY_SECOND_TYPE + " TEXT, " +
                KEY_SECOND_START_WITH + " TEXT, " + KEY_SECOND_DEFINED + " TEXT, " +
                KEY_THIRD_TAG + " TEXT, " + KEY_THIRD_LENGTH + " TEXT, " +
                KEY_THIRD_TYPE + " TEXT, " + KEY_THIRD_START_WITH + " TEXT, " +
                KEY_THIRD_DEFINED + " TEXT, " + KEY_FOURTH_TAG + " TEXT, " +
                KEY_FOURTH_LENGTH + " TEXT, " + KEY_FOURTH_TYPE + " TEXT, " +
                KEY_FOURTH_START_WITH + " TEXT, " + KEY_FOURTH_DEFINED + " TEXT" + ")";

        String CREATE_TABLE_STATE = "CREATE TABLE " + TABLE_STATE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_CIRCLE_ID + " TEXT," + KEY_CIRCLE_NAME + " TEXT" + ")";

        String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_ID + " TEXT," + KEY_OTP_CODE + " TEXT," +
                KEY_USER_NAME + " TEXT," + KEY_DEVICE_ID + " TEXT," + KEY_NAME + " TEXT," + KEY_PWD + " TEXT," + KEY_REMEMBER_ME + " TEXT," +
                KEY_REG_DATE + " TEXT" + ")";

        String CREATE_TABLE_STATUS_COLOR = "CREATE TABLE " + TABLE_STATUS_COLOR + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_COLOR_NAME + " TEXT," + KEY_COLOR_VALUE + " TEXT" + ")";

        String CREATE_TABLE_DMT_BANK = "CREATE TABLE " + TABLE_DMT_BANK + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_DMT_BANK_ID + " TEXT," + KEY_DMT_BANK_NAME + " TEXT," + KEY_DMT_BANK_IFSC_CODE + " TEXT" + ")";

        /*String CREATE_TABLE_WALLET = "CREATE TABLE " + TABLE_WALLET_LIST + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_WALLET_NAME + " TEXT," + KEY_WALLET_TYPE + " TEXT," + KEY_WALLET_BALANCE + " TEXT" + ")";

        String CREATE_TABLE_DEPOSIT_BANK = "CREATE TABLE " + TABLE_DEPOSIT_BANKS + "(" +
                KEY_DEPOSIT_BANKS_ID + " TEXT," + KEY_DEPOSIT_BANKS_BANK_NAME + " TEXT," + KEY_DEPOSIT_BANKS_BALANCE + " TEXT," +
                KEY_DEPOSIT_BANKS_ACCOUNT_NUMBER + " TEXT," + KEY_DEPOSIT_BANKS_USER_ID + " TEXT," + KEY_DEPOSIT_BANKS_PAYEE_NAME + " TEXT," + KEY_DEPOSIT_BANKS_ACCOUNT_TYPE + " TEXT," +
                KEY_DEPOSIT_BANKS_IFSC_CODE + " TEXT," + KEY_DEPOSIT_BANKS_BRANCH_NAME + " TEXT" + ")";

        String CREATE_TABLE_ALL_BANK = "CREATE TABLE " + TABLE_ALL_BANKS + "(" +
                KEY_ALL_BANKS_ID + " TEXT," + KEY_ALL_BANKS_BANK_NAME + " TEXT," + KEY_ALL_BANKS_ADD_DATE + " TEXT," +
                KEY_ALL_BANKS_EDIT_DATE + " TEXT," + KEY_ALL_BANKS_IP_ADDRESS + " TEXT," + KEY_ALL_BANKS_CREATED_BY + " TEXT," + KEY_ALL_BANKS_UPDATED_BY + " TEXT," +
                KEY_ALL_BANKS_STATUS + " TEXT" + ")";
*/
        /* [START] - create Notification table */
        // Create notification table
        // + KEY_NOTIFY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//        String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + TABLE_NOTIFY + "("
//                + KEY_NOTIFY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + KEY_MESSAGE + " TEXT,"
//                + KEY_TITLE + " TEXT,"
//                + KEY_RECEIVE_DATETIME + " TEXT,"
//                + KEY_SAVE_TIME + " TEXT,"
//                + KEY_READ_DATETIME + " TEXT,"
//                // + KEY_DELETE_FLAG + " TEXT,"
//                + KEY_READ_FLAG + " TEXT" + ")";
        // [END]

//        String CREATE_TABLE_NOTIFICATION_1 = "CREATE TABLE " + TABLE_NOTIFY_1 + "("
//                + KEY_NOTIFY_ID + " TEXT,"
//                + KEY_MESSAGE + " TEXT,"
//                + KEY_TITLE + " TEXT,"
//                + KEY_RECEIVE_DATETIME + " TEXT,"
//                + KEY_SAVE_TIME + " TEXT,"
//                // + KEY_DELETE_FLAG + " TEXT,"
//                + KEY_READ_FLAG + " TEXT" + ")";

        db.execSQL(CREATE_TABLE_DEFAULT_SETTINGS);
        db.execSQL(CREATE_TABLE_COMPANY);
        db.execSQL(CREATE_TABLE_PRODUCT);
        db.execSQL(CREATE_TABLE_STATE);
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_STATUS_COLOR);
        db.execSQL(CREATE_TABLE_DMT_BANK);
        /*db.execSQL(CREATE_TABLE_WALLET);
        db.execSQL(CREATE_TABLE_DEPOSIT_BANK);
        db.execSQL(CREATE_TABLE_ALL_BANK);*/
//        db.execSQL(CREATE_TABLE_NOTIFICATION);
//        db.execSQL(CREATE_TABLE_NOTIFICATION_1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*if(!isFieldExist(TABLE_PRODUCT,KEY_IS_PARTIAL)) {
            db.execSQL("ALTER TABLE " + TABLE_PRODUCT + " ADD COLUMN " + KEY_IS_PARTIAL + " INTEGER DEFAULT 0");
        }*/

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS_COLOR);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DMT_BANK);
        /*db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLET_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPOSIT_BANKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_BANKS);*/
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFY);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFY_1);
            onCreate(db);
    }

    public void createTable(SQLiteDatabase db) {

        String CREATE_TABLE_COMPANY = "CREATE TABLE " + TABLE_COMPANY + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_COMPANY_ITEM_ID + " TEXT," + KEY_COMPANY_NAME + " TEXT," +
                KEY_LOGO + " TEXT, " + KEY_SERVICE_TYPE + " TEXT" + ")";
        /*String CREATE_TABLE_COMPANY = "CREATE TABLE " + TABLE_COMPANY + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_COMPANY_ITEM_ID + " TEXT," + KEY_COMPANY_NAME + " TEXT," +
                KEY_LOGO + " TEXT, " + KEY_SERVICE_TYPE + " TEXT, " +
                KEY_FIRST_TAG + " TEXT, " + KEY_FIRST_LENGTH + " TEXT, " +
                KEY_FIRST_TYPE + " TEXT, " + KEY_FIRST_START_WITH + " TEXT, " +
                KEY_FIRST_DEFINED + " TEXT, " + KEY_SECOND_TAG + " TEXT, " +
                KEY_SECOND_LENGTH + " TEXT, " + KEY_SECOND_TYPE + " TEXT, " +
                KEY_SECOND_START_WITH + " TEXT, " + KEY_SECOND_DEFINED + " TEXT, " +
                KEY_THIRD_TAG + " TEXT, " + KEY_THIRD_LENGTH + " TEXT, " +
                KEY_THIRD_TYPE + " TEXT, " + KEY_THIRD_START_WITH + " TEXT, " +
                KEY_THIRD_DEFINED + " TEXT, " + KEY_FOURTH_TAG + " TEXT, " +
                KEY_FOURTH_LENGTH + " TEXT, " + KEY_FOURTH_TYPE + " TEXT, " +
                KEY_FOURTH_START_WITH + " TEXT, " + KEY_FOURTH_DEFINED + " TEXT" + ")";*/

        String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_PRODUCT + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_PRODUCT_ITEM_ID + " TEXT," + KEY_PRODUCT_NAME + " TEXT," +
                KEY_COMPANY_ID + " TEXT," + KEY_PRODUCT_LOGO + " TEXT," + KEY_SERVICE_TYPE + " TEXT," +
                KEY_IS_PARTIAL + " TEXT," +  KEY_FIRST_TAG + " TEXT, " + KEY_FIRST_LENGTH + " TEXT, " +
                KEY_FIRST_TYPE + " TEXT, " + KEY_FIRST_START_WITH + " TEXT, " +
                KEY_FIRST_DEFINED + " TEXT, " + KEY_SECOND_TAG + " TEXT, " +
                KEY_SECOND_LENGTH + " TEXT, " + KEY_SECOND_TYPE + " TEXT, " +
                KEY_SECOND_START_WITH + " TEXT, " + KEY_SECOND_DEFINED + " TEXT, " +
                KEY_THIRD_TAG + " TEXT, " + KEY_THIRD_LENGTH + " TEXT, " +
                KEY_THIRD_TYPE + " TEXT, " + KEY_THIRD_START_WITH + " TEXT, " +
                KEY_THIRD_DEFINED + " TEXT, " + KEY_FOURTH_TAG + " TEXT, " +
                KEY_FOURTH_LENGTH + " TEXT, " + KEY_FOURTH_TYPE + " TEXT, " +
                KEY_FOURTH_START_WITH + " TEXT, " + KEY_FOURTH_DEFINED + " TEXT" + ")";

        String CREATE_TABLE_STATE = "CREATE TABLE " + TABLE_STATE + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_CIRCLE_ID + " TEXT," + KEY_CIRCLE_NAME + " TEXT" + ")";

        String CREATE_TABLE_STATUS_COLOR = "CREATE TABLE " + TABLE_STATUS_COLOR + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_COLOR_NAME + " TEXT," + KEY_COLOR_VALUE + " TEXT" + ")";

        String CREATE_TABLE_DMT_BANK = "CREATE TABLE " + TABLE_DMT_BANK + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," + KEY_DMT_BANK_ID + " TEXT," + KEY_DMT_BANK_NAME + " TEXT," + KEY_DMT_BANK_IFSC_CODE + " TEXT" + ")";

        db.execSQL(CREATE_TABLE_COMPANY);
        db.execSQL(CREATE_TABLE_PRODUCT);
        db.execSQL(CREATE_TABLE_STATE);
        db.execSQL(CREATE_TABLE_STATUS_COLOR);
        db.execSQL(CREATE_TABLE_DMT_BANK);

    }


    // This method will check if column exists in your table
    public boolean isFieldExist(String tableName, String fieldName)
    {
        boolean isExist = false;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("PRAGMA table_info("+tableName+")",null);
        res.moveToFirst();
        do {
            String currentColumn = res.getString(1);
            if (currentColumn.equals(fieldName)) {
                isExist = true;
            }
        } while (res.moveToNext());
        return isExist;
    }



    public void addColors(ArrayList<Color> colorArrayList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < colorArrayList.size(); i++) {

                values.put(KEY_COLOR_NAME, colorArrayList.get(i).getColor_name());
                values.put(KEY_COLOR_VALUE, colorArrayList.get(i).getColo_value());

                db.insert(TABLE_STATUS_COLOR, null, values);
            }

            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void addDmtBanks(ArrayList<DMTAddBenefitiaryBankName> colorArrayList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < colorArrayList.size(); i++) {

                values.put(KEY_DMT_BANK_ID, colorArrayList.get(i).getBank_id());
                values.put(KEY_DMT_BANK_NAME, colorArrayList.get(i).getBank_name());
                values.put(KEY_DMT_BANK_IFSC_CODE, colorArrayList.get(i).getIfsc_code());
                db.insert(TABLE_DMT_BANK, null, values);
            }

            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }


    /*public void addWallets(ArrayList<WalletsModel> walletArrayList) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i = 0; i < walletArrayList.size(); i++) {

            values.put(KEY_WALLET_NAME, walletArrayList.get(i).getWallet_name());
            values.put(KEY_WALLET_TYPE, walletArrayList.get(i).getWallet_type());
            values.put(KEY_WALLET_BALANCE, walletArrayList.get(i).getBalance());

            db.insert(TABLE_WALLET_LIST, null, values);
        }

        db.close(); // Closing database connection
    }

    public void addAllBanks(ArrayList<PaymentRequestBankModel> walletArrayList) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i = 0; i < walletArrayList.size(); i++) {

            values.put(KEY_ALL_BANKS_ID, walletArrayList.get(i).getId());
            values.put(KEY_ALL_BANKS_BANK_NAME, walletArrayList.get(i).getBank_name());
            values.put(KEY_ALL_BANKS_ADD_DATE, walletArrayList.get(i).getAdd_date());
            values.put(KEY_ALL_BANKS_EDIT_DATE, walletArrayList.get(i).getEdit_date());
            values.put(KEY_ALL_BANKS_IP_ADDRESS, walletArrayList.get(i).getIp_address());
            values.put(KEY_ALL_BANKS_CREATED_BY, walletArrayList.get(i).getCreated_by());
            values.put(KEY_ALL_BANKS_UPDATED_BY, walletArrayList.get(i).getUpdated_by());
            values.put(KEY_ALL_BANKS_STATUS, walletArrayList.get(i).getStatus());

            db.insert(TABLE_ALL_BANKS, null, values);
        }

        db.close(); // Closing database connection
    }

    public void addDepositBanks(ArrayList<PaymentRequestDepositBankModel> walletArrayList) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i = 0; i < walletArrayList.size(); i++) {

            values.put(KEY_DEPOSIT_BANKS_ID, walletArrayList.get(i).getId());
            values.put(KEY_DEPOSIT_BANKS_BANK_NAME, walletArrayList.get(i).getBank_name());
            values.put(KEY_DEPOSIT_BANKS_BALANCE, walletArrayList.get(i).getBalance());
            values.put(KEY_DEPOSIT_BANKS_ACCOUNT_NUMBER, walletArrayList.get(i).getAccount_number());
            values.put(KEY_DEPOSIT_BANKS_USER_ID, walletArrayList.get(i).getUser_id());
            values.put(KEY_DEPOSIT_BANKS_PAYEE_NAME, walletArrayList.get(i).getPayee_name());
            values.put(KEY_DEPOSIT_BANKS_ACCOUNT_TYPE, walletArrayList.get(i).getAccount_type());
            values.put(KEY_DEPOSIT_BANKS_IFSC_CODE, walletArrayList.get(i).getIfsc_code());
            values.put(KEY_DEPOSIT_BANKS_BRANCH_NAME, walletArrayList.get(i).getBranch_name());

            db.insert(TABLE_DEPOSIT_BANKS, null, values);
        }

        db.close(); // Closing database connection
    }*/

    public void addDefaultSettings(String user_id, String state_id, String state_name) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, user_id);
            values.put(KEY_STATE_ID, state_id);
            values.put(KEY_STATE_NAME, state_name);
            db.insert(TABLE_DEFAULT_SETTINGS, null, values);

            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void addUserDetails(String user_id, String otp_code, String user_name, String device_id, String name, String remember_me) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(KEY_USER_ID, user_id);
            values.put(KEY_OTP_CODE, otp_code);
            values.put(KEY_USER_NAME, user_name);
            values.put(KEY_DEVICE_ID, device_id);
            values.put(KEY_NAME, name);
            values.put(KEY_REMEMBER_ME, remember_me);
            db.insert(TABLE_USER, null, values);

            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void addCompanysDetails(ArrayList<Company> companyArrayList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            for (int i = 0; i < companyArrayList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_COMPANY_ITEM_ID, companyArrayList.get(i).getId());
                values.put(KEY_COMPANY_NAME, companyArrayList.get(i).getCompany_name());
                values.put(KEY_LOGO, companyArrayList.get(i).getLogo());
                values.put(KEY_SERVICE_TYPE, companyArrayList.get(i).getService_type());
                /*values.put(KEY_FIRST_TAG, companyArrayList.get(i).getFirst_tag());
                values.put(KEY_FIRST_LENGTH, companyArrayList.get(i).getFirst_length());
                values.put(KEY_FIRST_TYPE, companyArrayList.get(i).getFirst_type());
                values.put(KEY_FIRST_START_WITH, companyArrayList.get(i).getFirst_start_with());
                values.put(KEY_FIRST_DEFINED, companyArrayList.get(i).getFirst_defined());
                values.put(KEY_SECOND_TAG, companyArrayList.get(i).getSecond_tag());
                values.put(KEY_SECOND_LENGTH, companyArrayList.get(i).getSecond_length());
                values.put(KEY_SECOND_TYPE, companyArrayList.get(i).getSecond_type());
                values.put(KEY_SECOND_START_WITH, companyArrayList.get(i).getSecond_start_with());
                values.put(KEY_SECOND_DEFINED, companyArrayList.get(i).getSecond_defined());
                values.put(KEY_THIRD_TAG, companyArrayList.get(i).getThird_tag());
                values.put(KEY_THIRD_LENGTH, companyArrayList.get(i).getThird_length());
                values.put(KEY_THIRD_TYPE, companyArrayList.get(i).getThird_type());
                values.put(KEY_THIRD_START_WITH, companyArrayList.get(i).getThird_start_with());
                values.put(KEY_THIRD_DEFINED, companyArrayList.get(i).getThird_defined());
                values.put(KEY_FOURTH_TAG, companyArrayList.get(i).getFourth_tag());
                values.put(KEY_FOURTH_LENGTH, companyArrayList.get(i).getFourth_length());
                values.put(KEY_FOURTH_TYPE, companyArrayList.get(i).getFourth_type());
                values.put(KEY_FOURTH_START_WITH, companyArrayList.get(i).getFourth_start_with());
                values.put(KEY_FOURTH_DEFINED, companyArrayList.get(i).getFourth_defined());*/
                db.insert(TABLE_COMPANY, null, values);

            }
            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void addProductsDetails(ArrayList<Product> productArrayList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            for (int i = 0; i < productArrayList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_PRODUCT_ITEM_ID, productArrayList.get(i).getId());
                values.put(KEY_PRODUCT_NAME, productArrayList.get(i).getProduct_name());
                values.put(KEY_COMPANY_ID, productArrayList.get(i).getCompany_id());
                values.put(KEY_PRODUCT_LOGO, productArrayList.get(i).getProduct_logo());
                values.put(KEY_IS_PARTIAL, productArrayList.get(i).getIs_partial());
                values.put(KEY_SERVICE_TYPE, productArrayList.get(i).getService_type());
                values.put(KEY_FIRST_TAG, productArrayList.get(i).getFirst_tag());
                values.put(KEY_FIRST_LENGTH, productArrayList.get(i).getFirst_length());
                values.put(KEY_FIRST_TYPE, productArrayList.get(i).getFirst_type());
                values.put(KEY_FIRST_START_WITH, productArrayList.get(i).getFirst_start_with());
                values.put(KEY_FIRST_DEFINED, productArrayList.get(i).getFirst_defined());
                values.put(KEY_SECOND_TAG, productArrayList.get(i).getSecond_tag());
                values.put(KEY_SECOND_LENGTH, productArrayList.get(i).getSecond_length());
                values.put(KEY_SECOND_TYPE, productArrayList.get(i).getSecond_type());
                values.put(KEY_SECOND_START_WITH, productArrayList.get(i).getSecond_start_with());
                values.put(KEY_SECOND_DEFINED, productArrayList.get(i).getSecond_defined());
                values.put(KEY_THIRD_TAG, productArrayList.get(i).getThird_tag());
                values.put(KEY_THIRD_LENGTH, productArrayList.get(i).getThird_length());
                values.put(KEY_THIRD_TYPE, productArrayList.get(i).getThird_type());
                values.put(KEY_THIRD_START_WITH, productArrayList.get(i).getThird_start_with());
                values.put(KEY_THIRD_DEFINED, productArrayList.get(i).getThird_defined());
                values.put(KEY_FOURTH_TAG, productArrayList.get(i).getFourth_tag());
                values.put(KEY_FOURTH_LENGTH, productArrayList.get(i).getFourth_length());
                values.put(KEY_FOURTH_TYPE, productArrayList.get(i).getFourth_type());
                values.put(KEY_FOURTH_START_WITH, productArrayList.get(i).getFourth_start_with());
                values.put(KEY_FOURTH_DEFINED, productArrayList.get(i).getFourth_defined());
                db.insert(TABLE_PRODUCT, null, values);
            }
            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void addStatesDetails(ArrayList<State> stateArrayList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            for (int i = 0; i < stateArrayList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_CIRCLE_ID, stateArrayList.get(i).getCircle_id());
                values.put(KEY_CIRCLE_NAME, stateArrayList.get(i).getCircle_name());
                db.insert(TABLE_STATE, null, values);

            }
            //db.close(); // Closing database connection
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void truncateUpdateData() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            /*db.execSQL("DELETE FROM " + TABLE_STATE);
            db.execSQL("DELETE FROM " + TABLE_PRODUCT);
            db.execSQL("DELETE FROM " + TABLE_COMPANY);
            db.execSQL("DELETE FROM " + TABLE_STATUS_COLOR);
            db.execSQL("DELETE FROM " + TABLE_DMT_BANK);*/

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS_COLOR);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DMT_BANK);

            createTable(db);
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public int getCount(String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try {
            Cursor mCount = db.rawQuery("select count(*) from " + table, null);
            mCount.moveToFirst();
            count = mCount.getInt(0);
            mCount.close();
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }


        return count;
    }

    public boolean checkEmpty() {
        if (getCount(TABLE_STATE) == 0 ||
                getCount(TABLE_PRODUCT) == 0 ||
                getCount(TABLE_COMPANY) == 0 ||
                getCount(TABLE_STATUS_COLOR) == 0 ||
                getCount(TABLE_DMT_BANK) == 0) {
            return false;
        }
        return true;
    }

    public void deleteDefaultSettings() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_DEFAULT_SETTINGS);
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

//    public void deleteNotificationData() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("DELETE FROM " + TABLE_NOTIFY);
//        db.close();
//    }

    public void deleteCompanyDetail(String service_type) {
        try {
            Dlog.d("Service Type: " + service_type + " Table name:  " + TABLE_COMPANY);

            SQLiteDatabase db = this.getWritableDatabase();
            int status = db.delete(TABLE_COMPANY,
                    KEY_SERVICE_TYPE + "=?",
                    new String[]{service_type});
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void deleteCompany() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_COMPANY, null, null);
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void deleteProductDetail(String service_type) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            int result = db.delete(TABLE_PRODUCT,
                    KEY_SERVICE_TYPE + "=?",
                    new String[]{service_type});
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void deleteStateDetail() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_STATE);
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void deleteStatusColor() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_STATUS_COLOR);
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public void deleteDmtBank() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_DMT_BANK);
            //db.close();
        } catch (Exception e) {
            Dlog.d(e.toString());
        }
    }

    /*public void deleteWalletList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_WALLET_LIST);
        db.close();
    }

    public void deleteAllBanks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ALL_BANKS);
        db.close();
    }


    public void deleteDepositBanks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DEPOSIT_BANKS);
        db.close();
    }*/

    public void deleteUsersDetail() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_USER);
            //db.close();
        }catch (Exception e) {
            Dlog.d(e.toString());
        }

    }

    public ArrayList<Company> getCompanyDetails(String Service_type) {
        ArrayList<Company> companyArrayList = new ArrayList<Company>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_COMPANY,
                    new String[]{/*KEY_FOURTH_DEFINED, KEY_FOURTH_START_WITH,
                            KEY_FOURTH_TYPE, KEY_FOURTH_LENGTH, KEY_FOURTH_TAG,
                            KEY_THIRD_DEFINED, KEY_THIRD_START_WITH, KEY_THIRD_TYPE,
                            KEY_THIRD_LENGTH, KEY_THIRD_TAG, KEY_SECOND_DEFINED,
                            KEY_SECOND_START_WITH, KEY_SECOND_TYPE,
                            KEY_SECOND_LENGTH, KEY_SECOND_TAG, KEY_FIRST_DEFINED,
                            KEY_FIRST_START_WITH, KEY_FIRST_TYPE, KEY_FIRST_LENGTH,
                           KEY_FIRST_TAG, KEY_SERVICE_TYPE,*/ KEY_COMPANY_NAME,
                            KEY_LOGO, KEY_COMPANY_ITEM_ID},
                    KEY_SERVICE_TYPE + "=?",
                    new String[]{Service_type}, null, null, null, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    Company company = new Company();
                    company.setId(cursor.getString(2));
                    company.setCompany_name(cursor.getString(0));
                    company.setLogo(cursor.getString(1));
                    /*company.setFirst_tag(cursor.getString(19));
                    company.setFirst_length(cursor.getString(18));
                    company.setFirst_type(cursor.getString(17));
                    company.setFirst_start_with(cursor.getString(16));
                    company.setFirst_defined(cursor.getString(15));
                    company.setSecond_tag(cursor.getString(14));
                    company.setSecond_length(cursor.getString(13));
                    company.setSecond_type(cursor.getString(12));
                    company.setSecond_start_with(cursor.getString(11));
                    company.setSecond_defined(cursor.getString(10));
                    company.setThird_tag(cursor.getString(9));
                    company.setThird_length(cursor.getString(8));
                    company.setThird_type(cursor.getString(7));
                    company.setThird_start_with(cursor.getString(6));
                    company.setThird_defined(cursor.getString(5));
                    company.setFourth_tag(cursor.getString(4));
                    company.setFourth_length(cursor.getString(3));
                    company.setFourth_type(cursor.getString(2));
                    company.setFourth_start_with(cursor.getString(1));
                    company.setFourth_defined(cursor.getString(0));*/
                    companyArrayList.add(company);
                }
                while (cursor.moveToNext());
            }
            //db.close();
        } catch (Exception ex) {

        }
        return companyArrayList;

    }


    //@kns.p get company name from number tracer
    public String getCompanyName(String company_id) {
        String company_name = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_COMPANY,
                    new String[]{KEY_COMPANY_NAME},
                    KEY_COMPANY_ITEM_ID + "=?",
                    new String[]{company_id}, null, null, null, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.getCount()>0) {
                cursor.moveToFirst();
                company_name = cursor.getString(0);

            }
            //db.close();
        } catch (Exception ex) {

            Dlog.d("getCompanyName: " + ex.toString());
        }
        return company_name;

    }


    //@kns.p get company name from number tracer
    public String getProductName(String company_id) {
        String product_name = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_PRODUCT,
                    new String[]{KEY_PRODUCT_NAME},
                    KEY_COMPANY_ITEM_ID + "=?",
                    new String[]{company_id}, null, null, null, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.getCount()>0) {
                cursor.moveToFirst();
                product_name = cursor.getString(0);

            }
            //db.close();
        } catch (Exception ex) {
            Dlog.d("getCompanyName: " + ex.toString());
        }
        return product_name;

    }

    public String getProductID(String company_id) {
        String product_name = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_PRODUCT,
                    new String[]{KEY_PRODUCT_ITEM_ID},
                    KEY_COMPANY_ITEM_ID + "=?",
                    new String[]{company_id}, null, null, null, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.getCount()>0) {
                cursor.moveToFirst();
                product_name = cursor.getString(0);

            }
            //db.close();
        } catch (Exception ex) {
            Dlog.d("getProductId: " + ex.toString());
        }
        return product_name;

    }
//==================================================


    public ArrayList<Product> getProductDetails(String company_id) {
        ArrayList<Product> productArrayList = new ArrayList<Product>();
        // Select All Query
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT, new String[]{KEY_FOURTH_DEFINED, KEY_FOURTH_START_WITH,
                        KEY_FOURTH_TYPE, KEY_FOURTH_LENGTH, KEY_FOURTH_TAG,
                        KEY_THIRD_DEFINED, KEY_THIRD_START_WITH, KEY_THIRD_TYPE,
                        KEY_THIRD_LENGTH, KEY_THIRD_TAG, KEY_SECOND_DEFINED,
                        KEY_SECOND_START_WITH, KEY_SECOND_TYPE,
                        KEY_SECOND_LENGTH, KEY_SECOND_TAG, KEY_FIRST_DEFINED,
                        KEY_FIRST_START_WITH, KEY_FIRST_TYPE, KEY_FIRST_LENGTH,
                        KEY_FIRST_TAG,KEY_PRODUCT_ITEM_ID, KEY_PRODUCT_NAME, KEY_COMPANY_ID, KEY_PRODUCT_LOGO, KEY_IS_PARTIAL}, KEY_COMPANY_ID + "=?",
                new String[]{company_id}, null, null, null, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
//                product.setId(cursor.getString(Integer.parseInt(KEY_PRODUCT_ITEM_ID)));
//                product.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_ITEM_ID))));
                product.setId(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_ITEM_ID)));

                product.setProduct_name(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                product.setCompany_id(cursor.getString(cursor.getColumnIndex(KEY_COMPANY_ID)));
                product.setProduct_logo(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_LOGO)));
                product.setIs_partial(cursor.getString(cursor.getColumnIndex(KEY_IS_PARTIAL)));
                product.setFirst_tag(cursor.getString(cursor.getColumnIndex(KEY_FIRST_TAG)));
                product.setFirst_length(cursor.getString(cursor.getColumnIndex(KEY_FIRST_LENGTH)));
                product.setFirst_type(cursor.getString(cursor.getColumnIndex(KEY_FIRST_TYPE)));
                product.setFirst_start_with(cursor.getString(cursor.getColumnIndex(KEY_FIRST_START_WITH)));
                product.setFirst_defined(cursor.getString(cursor.getColumnIndex(KEY_FIRST_DEFINED)));
                product.setSecond_tag(cursor.getString(cursor.getColumnIndex(KEY_SECOND_TAG)));
                product.setSecond_length(cursor.getString(cursor.getColumnIndex(KEY_SECOND_LENGTH)));
                product.setSecond_type(cursor.getString(cursor.getColumnIndex(KEY_SECOND_TYPE)));
                product.setSecond_start_with(cursor.getString(cursor.getColumnIndex(KEY_SECOND_START_WITH)));
                product.setSecond_defined(cursor.getString(cursor.getColumnIndex(KEY_SECOND_DEFINED)));
                product.setThird_tag(cursor.getString(cursor.getColumnIndex(KEY_THIRD_TAG)));
                product.setThird_length(cursor.getString(cursor.getColumnIndex(KEY_THIRD_LENGTH)));
                product.setThird_type(cursor.getString(cursor.getColumnIndex(KEY_THIRD_TYPE)));
                product.setThird_defined(cursor.getString(cursor.getColumnIndex(KEY_THIRD_DEFINED)));
                product.setFourth_tag(cursor.getString(cursor.getColumnIndex(KEY_FOURTH_TAG)));
                product.setFourth_length(cursor.getString(cursor.getColumnIndex(KEY_FOURTH_LENGTH)));
                product.setFourth_type(cursor.getString(cursor.getColumnIndex(KEY_FOURTH_TYPE)));
                product.setFourth_start_with(cursor.getString(cursor.getColumnIndex(KEY_FOURTH_START_WITH)));
                product.setFourth_defined(cursor.getString(cursor.getColumnIndex(KEY_FOURTH_DEFINED)));
                productArrayList.add(product);
            }
            while (cursor.moveToNext());
        }
        //db.close();
        return productArrayList;
    }

    public ArrayList<State> getStateDetails() {
        ArrayList<State> stateArrayList = new ArrayList<State>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STATE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                State state = new State();
                state.setCircle_id(cursor.getString(1));
                state.setCircle_name(cursor.getString(2));
                stateArrayList.add(state);
            }
            while (cursor.moveToNext());
        }
        db.close();
        return stateArrayList;
    }

    public ArrayList<Default> getDefaultSettings() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ArrayList<Default> defaultArrayList = new ArrayList<Default>();
        Cursor cursor = sqLiteDatabase.query(TABLE_DEFAULT_SETTINGS, new String[]{KEY_ID,
                        KEY_USER_ID, KEY_STATE_ID, KEY_STATE_NAME}, null,
                null, null, null, null, null);
        if (cursor != null && cursor.getCount()>0) {
            cursor.moveToFirst();
            Default aDefault = new Default();
            aDefault.setUser_id(cursor.getString(1));
            aDefault.setState_id(cursor.getString(2));
            aDefault.setState_name(cursor.getString(3));
            defaultArrayList.add(aDefault);
        }
        cursor.close();
        sqLiteDatabase.close();
        return defaultArrayList;
    }

    public String getCircleID(String circle_name) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_STATE, new String[]{KEY_CIRCLE_ID}, KEY_CIRCLE_NAME + "=?",
                new String[]{circle_name}, null, null, null, null);

        String circle_id = null;
        if (cursor != null && cursor.getCount()>0) {
            cursor.moveToFirst();
            circle_id = cursor.getString(0);
        }
        sqLiteDatabase.close();
        return circle_id;
    }

    public ArrayList<Color> getAllColors() {

        ArrayList<Color> textColorArrayList = new ArrayList<Color>();
        String selectQuery = "SELECT  * FROM " + TABLE_STATUS_COLOR;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Color textColor = new Color();
                textColor.setColor_name(cursor.getString(1));
                textColor.setColo_value(cursor.getString(2));
                textColorArrayList.add(textColor);
            }
            while (cursor.moveToNext());
        }

        //db.close();
        return textColorArrayList;
    }

    public ArrayList<DMTAddBenefitiaryBankName> getDmtBank() {

        ArrayList<DMTAddBenefitiaryBankName> textColorArrayList = new ArrayList<DMTAddBenefitiaryBankName>();
        String selectQuery = "SELECT * FROM " + TABLE_DMT_BANK;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DMTAddBenefitiaryBankName textColor = new DMTAddBenefitiaryBankName();
                textColor.setBank_id(cursor.getString(1));
                textColor.setBank_name(cursor.getString(2));
                textColor.setIfsc_code(cursor.getString(3));
                textColorArrayList.add(textColor);
            }
            while (cursor.moveToNext());
        }

        //db.close();
        return textColorArrayList;
    }

    public void closeDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.close();
    }

    /*public ArrayList<WalletsModel> getAllWalletList() {

        ArrayList<WalletsModel> textWalletArrayList = new ArrayList<WalletsModel>();
        String selectQuery = "SELECT  * FROM " + TABLE_WALLET_LIST;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                WalletsModel textColor = new WalletsModel();
                textColor.setWallet_name(cursor.getString(1));
                textColor.setWallet_type(cursor.getString(2));
                textColor.setBalance(cursor.getString(3));
                textWalletArrayList.add(textColor);
            }
            while (cursor.moveToNext());
        }

        db.close();
        return textWalletArrayList;
    }

    public ArrayList<PaymentRequestBankModel> getAllBanks() {

        ArrayList<PaymentRequestBankModel> textWalletArrayList = new ArrayList<PaymentRequestBankModel>();
        String selectQuery = "SELECT  * FROM " + TABLE_ALL_BANKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PaymentRequestBankModel textColor = new PaymentRequestBankModel();
                textColor.setId(cursor.getString(0));
                textColor.setBank_name(cursor.getString(1));
                textColor.setAdd_date(cursor.getString(2));
                textColor.setEdit_date(cursor.getString(3));
                textColor.setIp_address(cursor.getString(4));
                textColor.setCreated_by(cursor.getString(5));
                textColor.setUpdated_by(cursor.getString(6));
                textColor.setStatus(cursor.getString(7));
                textWalletArrayList.add(textColor);
            }
            while (cursor.moveToNext());
        }

        db.close();
        return textWalletArrayList;
    }

    public ArrayList<PaymentRequestDepositBankModel> getAllDepositBanks() {

        ArrayList<PaymentRequestDepositBankModel> textWalletArrayList = new ArrayList<PaymentRequestDepositBankModel>();
        String selectQuery = "SELECT  * FROM " + TABLE_DEPOSIT_BANKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PaymentRequestDepositBankModel textColor = new PaymentRequestDepositBankModel();
                textColor.setId(cursor.getString(0));
                textColor.setBank_name(cursor.getString(1));
                textColor.setBalance(cursor.getString(2));
                textColor.setAccount_number(cursor.getString(3));
                textColor.setUser_id(cursor.getString(4));
                textColor.setPayee_name(cursor.getString(5));
                textColor.setAccount_type(cursor.getString(6));
                textColor.setIfsc_code(cursor.getString(7));
                textColor.setBranch_name(cursor.getString(8));
                textWalletArrayList.add(textColor);
            }
            while (cursor.moveToNext());
        }

        db.close();
        return textWalletArrayList;
    }*/


    public String getCompanyLogo(String company_name) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_COMPANY, new String[]{KEY_LOGO}, KEY_COMPANY_NAME + "=?",
                new String[]{company_name}, null, null, null, null);

        String company_logo = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            company_logo = cursor.getString(0);

        }
        sqLiteDatabase.close();
        return company_logo;
    }

    public ArrayList<User> getUserDetail() {

        ArrayList<User> userArrayList = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String select_query = "SELECT * FROM " + TABLE_USER;
        Cursor cursor = sqLiteDatabase.rawQuery(select_query, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            user.setOtp_code(cursor.getString(2));
            user.setUser_name(cursor.getString(3));
            user.setDevice_id(cursor.getString(4));
            user.setName(cursor.getString(5));
            user.setPassword(cursor.getString(6));
            user.setRemember_me(cursor.getString(7));
            user.setReg_date(cursor.getString(8));
            userArrayList.add(user);
        }
        cursor.close();
        sqLiteDatabase.close();
        return userArrayList;
    }

    public int updateUserDetails(String uname, String pwd, String remember_me_status, String reg_date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PWD, pwd);
        values.put(KEY_REMEMBER_ME, remember_me_status);
        values.put(KEY_REG_DATE, reg_date);

        // updating row
        return db.update(TABLE_USER, values, KEY_USER_NAME + " = ?",
                new String[]{String.valueOf(uname)});

    }

    // Backup DB. Require to add permission
//    public void backupDatabase() {
//        File sd = Environment.getExternalStorageDirectory();
//        File data = Environment.getDataDirectory();
//        if (sd.canWrite()) {
//            // String currentDBPath = Constants.DB_Path;
//             String currentDBPath = "/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME;
//             Log.d("FILE", "DB current path : " + currentDBPath);
////            String currentDBPath = context.getDatabasePath(DATABASE_NAME) + "";
//            Log.d("FILE", "DB path : " + context.getDatabasePath(DATABASE_NAME).getPath());
//
//            String backupDBPath = "DB_" + DATABASE_NAME + "_" + DateTime.getDate() + ".db";
//            File currentDB = new File(data, currentDBPath);
//            File backupDB = new File(sd, backupDBPath);
//             if (currentDB.exists()) {
//                try {
//                    int status = FileUtility.copyFile(currentDB, backupDB);
//                    if (status == 1) {
//                        Log.d("FILE", "File write");
//                    } else {
//                        Log.d("FILE", "File not write");
//                    }
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Log.d("FILE", "File not found");
//            }
//        }
//    }
}
