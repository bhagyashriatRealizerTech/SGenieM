package com.realizer.schoolgenie.managment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;


import com.google.android.gcm.GCMRegistrar;
import com.realizer.schoolgenie.managment.exceptionhandler.ExceptionHandler;
import com.realizer.schoolgenie.managment.forgotpassword.SetMagicWordAsyncTaskGet;
import com.realizer.schoolgenie.managment.forgotpassword.SetPasswordAsyncTaskGet;
import com.realizer.schoolgenie.managment.forgotpassword.SetPasswordByEmailAsyncTaskGet;
import com.realizer.schoolgenie.managment.forgotpassword.ValidateMagicWordAsyncTaskGet;
import com.realizer.schoolgenie.managment.utils.Config;
import com.realizer.schoolgenie.managment.utils.OnTaskCompleted;
import com.realizer.schoolgenie.managment.utils.QueueListModel;
import com.realizer.schoolgenie.managment.view.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * Created by Win on 11/27/2015.
 */
public class LoginActivity extends Activity implements OnTaskCompleted {

    EditText userName, password;
    Button loginButton;
    CheckBox checkBox;
   // DatabaseQueries dbqr;
    int num;
    ProgressWheel loading;
    AlertDialog.Builder adbdialog;
    SharedPreferences sharedpreferences;
    TextView forgotPassword;
    String defaultMagicWord;
    static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

       // overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        setContentView(R.layout.login_activity);


        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }

        userName = (EditText) findViewById(R.id.edtEmpId);
        password = (EditText) findViewById((R.id.edtPassword));
        loginButton = (Button) findViewById(R.id.btnLogin);
        Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");
        loginButton.setTypeface(face);
        checkBox = (CheckBox) findViewById(R.id.checkBox1);
        loading = (ProgressWheel) findViewById(R.id.loading);
        forgotPassword = (TextView) findViewById(R.id.txtForgetPswrd);
        //dbqr = new DatabaseQueries(LoginActivity.this);
        num =0;
        defaultMagicWord="";
        //About Remember me in login page

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);


        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    userName.setText(result);
                    userName.setSelection(result.length());
                    // alert the user
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    password.setText(result);
                    password.setSelection(result.length());
                    // alert the user
                }
            }
        });



        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = sharedpreferences.edit();
                if (checkBox.isChecked()) {

                    edit.putString("Username", userName.getText().toString().trim());
                    edit.putString("Password", password.getText().toString().trim());
                    edit.putString("CHKSTATE", "true");
                    edit.commit();
                }
                else
                {
                    edit.putString("Username", "");
                    edit.putString("Password", "");
                    edit.putString("CHKSTATE", "false");
                    edit.commit();
                }
            }
        });


        String chk = sharedpreferences.getString("CHKSTATE","");
        Log.d("CHECKED", chk);
        if(chk.equals("true"))
        {
            checkBox.setChecked(true);
            userName.setText(sharedpreferences.getString("Username",""));
            password.setText(sharedpreferences.getString("Password", ""));
        }
        else
        {
            checkBox.setChecked(false);
            userName.setText("");
            password.setText("");
        }



        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");

                LayoutInflater inflater = getLayoutInflater();
                View dialoglayout = inflater.inflate(R.layout.forgotpwd_recoveryoption, null);
                Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
                Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
                final RadioButton mail = (RadioButton)dialoglayout.findViewById(R.id.rb_option_mail);
                final RadioButton magicword = (RadioButton)dialoglayout.findViewById(R.id.rb_option_magic_word);
                submit.setTypeface(face);
                cancel.setTypeface(face);

                mail.setChecked(true);

                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setView(dialoglayout);

                final AlertDialog alertDialog = builder.create();

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mail.isChecked()) {
                            alertDialog.dismiss();
                            recoverPasswordByEmail();
                        }
                        if (magicword.isChecked()) {
                            alertDialog.dismiss();
                            recoverPasswordByMagicWord("ForgotPassword",false,"");
                        }

                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor edit = sharedpreferences.edit();
                edit.putString("SchoolCode", "ada");
                edit.putString("DisplayName", "Vivaan Salgare");
                edit.putString("ThumbnailID", "http://enerji.ronesans.com/assets/content/board-of-directors/_imgtrans_bppt/firat_bilen.png");
                edit.putString("IsLogin","true");
                edit.commit();

/*              boolean res = isConnectingToInternet();
                if(!res)
                {
                    Config.alertDialog(LoginActivity.this,"Network Error","No Internet Connection available");
                    //Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();

                }

                else if (userName.getText().toString().equals("") && password.getText().toString().equals("")) {
                    Config.alertDialog(LoginActivity.this,"Login","Please Enter Username/Password");
                   // Toast.makeText(getApplicationContext(), "Please Enter Username/Password", Toast.LENGTH_LONG).show();
                }
                else if (userName.getText().toString().equals("") ) {
                    Config.alertDialog(LoginActivity.this,"Login","Please Enter Username");
                    //Toast.makeText(getApplicationContext(), "Please Enter Username", Toast.LENGTH_LONG).show();
                }
                else if (password.getText().toString().equals("")) {
                    Config.alertDialog(LoginActivity.this, "Login", "Please Enter Password");
                             // Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_LONG).show();
                }
                else
                {
                    final SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    String logchk = sharedpreferences.getString("LogChk", "");
                    String uname = sharedpreferences.getString("Username","");
                    if(logchk.equals("true") && !uname.equals(userName.getText().toString().trim()))
                    {
                        adbdialog = new AlertDialog.Builder(LoginActivity.this);
                        adbdialog.setTitle("Login Alert");
                        adbdialog.setMessage("All the Data of Previous User will be Deleted,\nDo You want to Proceed?");
                        adbdialog.setIcon(android.R.drawable.ic_dialog_alert);
                        adbdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                loading.setVisibility(View.VISIBLE);

                                new NewLoginAsync().execute();

                            } });


                        adbdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                userName.setText("");
                                password.setText("");
                            } });
                        adbdialog.show();

                    }
                    else {
                        loading.setVisibility(View.VISIBLE);
                        LoginAsyncTaskGet obj = new LoginAsyncTaskGet(userName.getText().toString().replaceAll(" ", ""), password.getText().toString().replaceAll(" ", ""), LoginActivity.this, LoginActivity.this);
                        obj.execute();
                    }

                }*/
                Intent intent  = new Intent(LoginActivity.this,DashboardActivity.class);
                startActivity(intent);
                finish();
                finish();
            }
        });

    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.VIBRATE,
                        }, 101);
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onTaskCompleted(String s, QueueListModel queueListModel) {
        if(queueListModel != null) {
            if (queueListModel.getType().equalsIgnoreCase("SetMagicWord")) {
                if(s.equalsIgnoreCase("true"))
                {

                }
                boolean b = false;
                if(queueListModel.getTime().equalsIgnoreCase("true"))
                    b= true;


                if (b) {
                    loading.setVisibility(View.GONE);
                    GCMReg();
                    SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("Login", "true");
                    edit.commit();

                    Intent intent = new Intent(LoginActivity.this,DashboardActivity.class);
                    startActivity(intent);
                    finish();
                    //getResultsFromApi();


                } else {
                    loading.setVisibility(View.GONE);
                    if (num == 0)
                        Config.alertDialog(LoginActivity.this, "Login", "Invalid credentials, Please Try again!");
                        //Toast.makeText(getApplicationContext(), "Invalid credentials, Pls Try again!", Toast.LENGTH_LONG).show();
                    else if (num == 1)
                        Config.alertDialog(LoginActivity.this, "Network Error", "Server Not Responding Please Try After Some Time");
                       // Toast.makeText(getApplicationContext(), "Server Not Responding Please Try After Some Time", Toast.LENGTH_SHORT).show();
                }
            }
            else  if (queueListModel.getType().equalsIgnoreCase("ValidateMagicWord")) {
                loading.setVisibility(View.GONE);
                if(s.equalsIgnoreCase("true"))
                {
                    resetPassword();
                }
                else
                {
                    Config.alertDialog(LoginActivity.this, "Forgot Password", "Invalid User ID / Magic Word Entered");
                    //Toast.makeText(LoginActivity.this,"Wrong User ID / Wrong Magic Word Entered",Toast.LENGTH_SHORT).show();
                }

            }
            else  if (queueListModel.getType().equalsIgnoreCase("SetPassword")) {
                loading.setVisibility(View.GONE);
                if(s.equalsIgnoreCase("true"))
                {

                }
                else
                {
                    Config.alertDialog(LoginActivity.this, "Reset Password", "Fail to Reset Password");
                   // Toast.makeText(LoginActivity.this,"Fail to Reset Password",Toast.LENGTH_SHORT).show();
                }

            }
            else  if (queueListModel.getType().equalsIgnoreCase("SendEmail")) {
                loading.setVisibility(View.GONE);
                if(s.equalsIgnoreCase("true"))
                {
                    Config.alertDialog(LoginActivity.this, "Forgot Password", "Email Sent to your Email ID.");
                    //Toast.makeText(LoginActivity.this,"Email Sent Successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Config.alertDialog(LoginActivity.this, "Forgot Password", "Fail to Send Email.");
                    //Toast.makeText(LoginActivity.this,"Fail to Send Email",Toast.LENGTH_SHORT).show();
                }

            }
        }
        else
        {
        boolean b = false;
        final SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("UidName", userName.getText().toString().trim().toLowerCase());
        edit.putString("Username", userName.getText().toString().trim().toLowerCase());
        edit.putString("Password", password.getText().toString().trim());
        edit.commit();

        String logchk = sharedpreferences.getString("LogChk", "");
        String mWord = "";
            String validate = "";
            JSONObject rootObj = null;
            String accesstoken ="";
            try {
                rootObj = new JSONObject(s);
                validate = rootObj.getString("isLoginSuccessfull");
                JSONObject teacherInfo  = rootObj.getJSONObject("Teacher");
                mWord =teacherInfo.getString("MagicWord");
                accesstoken =  rootObj.getString("AccessToken");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            edit.putString("AccessToken", accesstoken);
            edit.commit();
        if (logchk.equals("true")) {

            try {

                if (validate.equals("valid")) {
                    b = true;
                } else {
                    String Schoolcode = rootObj.getString("SchoolCode");
                    if (Schoolcode.length() == 0) {
                        num = 1;
                    }
                    b = false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (b == true) {
                if (mWord.trim().length() > 0 && !mWord.equalsIgnoreCase("null")) {
                    // if (b == true) {

                    loading.setVisibility(View.GONE);
                    GCMReg();
                    edit.putString("Login", "true");
                    edit.commit();
                    Intent intent = new Intent(LoginActivity.this,DashboardActivity.class);
                    startActivity(intent);
                    finish();
                    //getResultsFromApi();

                } else {
                    loading.setVisibility(View.GONE);
                    recoverPasswordByMagicWord("FirstLogin", b, s);
                }
            }
            else {
                loading.setVisibility(View.GONE);
                if (num == 0)
                    Config.alertDialog(LoginActivity.this, "Login", "Invalid credentials, Please Try Again");
                    //Toast.makeText(getApplicationContext(), "Invalid credentials, Pls Try again!", Toast.LENGTH_LONG).show();
                else if (num == 1)
                    Config.alertDialog(LoginActivity.this, "Network Error", "Server Not Responding Please Try After Some Time");
                //Toast.makeText(getApplicationContext(), "Server Not Responding Please Try After Some Time", Toast.LENGTH_SHORT).show();
            }



        } else {
            loading.setVisibility(View.GONE);
            String Schoolcode = null;
            try {

                if (validate.equalsIgnoreCase("valid")) {
                    num = 0;
                    Schoolcode = rootObj.getString("SchoolCode");
                    if (Schoolcode.length() == 0 || Schoolcode.equalsIgnoreCase("null"))
                        num = 1;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(num==0) {
                b = parsData(s);
            }
            else
            b= false;

            if (b) {
            if (mWord.trim().length() > 0 && !mWord.equalsIgnoreCase("null")) {

                    GCMReg();
                    edit.putString("Login", "true");
                    edit.commit();
                Intent intent = new Intent(LoginActivity.this,DashboardActivity.class);
                startActivity(intent);
                finish();
                   // getResultsFromApi();


            } else {

                    recoverPasswordByMagicWord("FirstLogin", b, s);

            }
        }
            else {
                if (num == 0)
                    Config.alertDialog(LoginActivity.this, "Login", "Invalid credentials, Please Try Again");
                    //Toast.makeText(getApplicationContext(), "Invalid credentials, Pls Try again!", Toast.LENGTH_LONG).show();
                else if (num == 1)
                    Config.alertDialog(LoginActivity.this, "Network Error", "Server Not Responding Please Try After Some Time");
                //Toast.makeText(getApplicationContext(), "Server Not Responding Please Try After Some Time", Toast.LENGTH_SHORT).show();
            }
        }
    }
    }



    public boolean parsData(String json) {

        String validate = "";
        String Schoolcode = "";
        JSONObject rootObj = null;

        try {
            rootObj = new JSONObject(json);

            validate = rootObj.getString("isLoginSuccessfull");
            Schoolcode = rootObj.getString("SchoolCode");
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("SchoolCode", Schoolcode);
            edit.putString("DisplayName", "");
            edit.putString("ThumbnailID", "");
            edit.commit();
            //qr.deleteTable();


        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(validate.equals("valid"))
            return true;
        else
            return false;
    }

    public void GCMReg()
    {
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(Config.DISPLAY_MESSAGE_ACTION));
        GCMRegistrar.register(LoginActivity.this, Config.SENDER_ID);
    }



    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    /*String newMessage = intent.getExtras().getString(Config.EXTRA_MESSAGE);
                    Toast.makeText(context,newMessage,Toast.LENGTH_SHORT).show();*/
                }
            };



    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public class NewLoginAsync extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

           // dbqr.deleteAllData();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
            SharedPreferences.Editor edit = sharedpreferences.edit();
            edit.putString("LogChk", "false");
            edit.commit();
            LoginAsyncTaskGet obj = new LoginAsyncTaskGet(userName.getText().toString().replaceAll(" ",""),password.getText().toString().replaceAll(" ", ""),LoginActivity.this,LoginActivity.this);
            obj.execute();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(loading != null  && loading.isShown())
        {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // condition to lock the screen at the time of refreshing
        if ((loading != null && loading.isShown())) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void recoverPasswordByEmail()
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_rmailpassword, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText userID = (EditText)dialoglayout.findViewById(R.id.edtuserid);
        final EditText email = (EditText)dialoglayout.findViewById(R.id.edtmailid);
        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);
        final AlertDialog alertDialog = builder.create();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userID.getText().toString().trim();
                String userEmail =  email.getText().toString().trim();
                alertDialog.dismiss();
               if(userID.length()>0 && userEmail.length()>0)
                {
                    loading.setVisibility(View.VISIBLE);
                    new SetPasswordByEmailAsyncTaskGet(userId,userEmail,LoginActivity.this,LoginActivity.this).execute();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


    public void recoverPasswordByMagicWord(final String from, final boolean b1, final String s)
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_mwordpassword, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText userID = (EditText)dialoglayout.findViewById(R.id.edtuserid);
        userID.setText(userName.getText().toString());
        final EditText magicWord = (EditText)dialoglayout.findViewById(R.id.edtmagicword);

        final TextView titledialog = (TextView)dialoglayout.findViewById(R.id.dialogTitle);
        final TextView infodialog = (TextView)dialoglayout.findViewById(R.id.infodialog);

        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);
        if(from.equalsIgnoreCase("FirstLogin"))
        {
            titledialog.setText("Set Magic Word");
            infodialog.setText("Please Set Your Magic Word , you can use this for password recovery");
            builder.setCancelable(false);
        }

        final AlertDialog alertDialog = builder.create();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userID.getText().toString().trim();
                String wordMagic =  magicWord.getText().toString().trim();
                alertDialog.dismiss();
                if (from.equalsIgnoreCase("FirstLogin")) {
                    if(userId.length()>0 && wordMagic.length()>0)

                    new SetMagicWordAsyncTaskGet(userId,wordMagic, String.valueOf(b1),LoginActivity.this,LoginActivity.this).execute();
                }
                else
                {
                    loading.setVisibility(View.VISIBLE);
                    new ValidateMagicWordAsyncTaskGet(userId,wordMagic,LoginActivity.this,LoginActivity.this).execute();
                }
                //resetPassword();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
                if (from.equalsIgnoreCase("FirstLogin")) {
                    boolean b =false;
                    //boolean b = parsData(s);
                    if (b == true) {
                        loading.setVisibility(View.GONE);
                        GCMReg();
                        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        SharedPreferences.Editor edit = sharedpreferences.edit();
                        edit.putString("Login", "true");
                        edit.commit();

                        //getResultsFromApi();

                       /* Intent ser = new Intent(LoginActivity.this, AutoSyncService.class);
                        Singlton.setAutoserviceIntent(ser);
                        startService(ser);
                        Intent i = new Intent(LoginActivity.this, DrawerActivity.class);
                        startActivity(i);*/

                    } else {
                        if (num == 0)
                            Config.alertDialog(LoginActivity.this, "Login", "Invalid credentials, Please Try Again");
                            //Toast.makeText(getApplicationContext(), "Invalid credentials, Pls Try again!", Toast.LENGTH_LONG).show();

                        else if (num == 1)
                            Config.alertDialog(LoginActivity.this, "Network Error", "Server Not Responding Please Try After Some Time");
                            //Toast.makeText(getApplicationContext(), "Server Not Responding Please Try After Some Time", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        alertDialog.show();
    }


    public void resetPassword()
    {
        final Typeface face= Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/font.ttf");

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.forgotpwd_resetpassword, null);
        Button submit = (Button)dialoglayout.findViewById(R.id.btn_submit);
        Button cancel = (Button)dialoglayout.findViewById(R.id.btn_cancel);
        final EditText userID = (EditText)dialoglayout.findViewById(R.id.edtuserid);
        final EditText pwd = (EditText)dialoglayout.findViewById(R.id.edtpwd);
        final EditText cPwd = (EditText)dialoglayout.findViewById(R.id.edtconfirmpwd);
        submit.setTypeface(face);
        cancel.setTypeface(face);
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(dialoglayout);
        final AlertDialog alertDialog = builder.create();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userID.getText().toString().trim();
                String password =  pwd.getText().toString().trim();
                String cPassword =  cPwd.getText().toString().trim();

                alertDialog.dismiss();

                if(password.equals(cPassword))
                new SetPasswordAsyncTaskGet(userId,password,LoginActivity.this,LoginActivity.this).execute();
                else
                    Config.alertDialog(LoginActivity.this, "Login", "Password Mismatch");
                    //Toast.makeText(LoginActivity.this,"Password Mismatch",Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void getDeviceId(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("DeviceId", telephonyManager.getDeviceId());
                    edit.commit();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
