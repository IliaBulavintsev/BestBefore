package com.rv150.bestbefore.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.Network.HttpPostBackup;
import com.rv150.bestbefore.Network.HttpPostRestore;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Receivers.AlarmReceiver;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.DBHelper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Rudnev on 01.07.2016.
 */
public class Preferences extends PreferenceActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private GoogleApiClient mGoogleApiClient;
    private String idToken;
    Preference auth;

   // private GoogleApiClient mGoogleDriveClient;

    SharedPreferences sPrefs;
    ProductDAO productDAO;
    GroupDAO groupDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar_settings, root, false);
        root.addView(bar, 0); // insert at top

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        productDAO = new ProductDAO(getApplicationContext());
        groupDAO = new GroupDAO(getApplicationContext());


        // Листенеры на тайм пикеры
        Preference time1 = getPreferenceManager().findPreference("time_in_first");
        time1.setSummary(getSummary(1));
        time1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getSummary(1));
                return true;
            }
        });

        Preference time2 = getPreferenceManager().findPreference("time_in_second");
        time2.setSummary(getSummary(2));
        time2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getSummary(2));
                return true;
            }
        });

        Preference time3 = getPreferenceManager().findPreference("time_in_third");
        time3.setSummary(getSummary(3));
        time3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getSummary(3));
                return true;
            }
        });

        Preference time4 = getPreferenceManager().findPreference("time_in_fourth");
        time4.setSummary(getSummary(4));
        time4.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getSummary(4));
                return true;
            }
        });

        Preference time5 = getPreferenceManager().findPreference("time_in_fifth");
        time5.setSummary(getSummary(5));
        time5.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getSummary(5));
                return true;
            }
        });



        Preference clearDictionary = findPreference("clear_dictionary");
        clearDictionary.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(preference.getContext())
                        .setTitle(R.string.warning)
                        .setMessage(R.string.sure_you_want_clear_user_dictionary)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ClearUserDictionaryTask(getApplicationContext()).execute();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                       .show();
                return true;
            }
        });





        // Синхронизация
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestIdToken(getString(R.string.CLIENT_ID))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();




        auth = findPreference("auth");
        if (isAuthenticated()) {
            auth.setTitle(R.string.log_out);
            auth.setSummary("");
        }


        auth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
               if (isAuthenticated()) { // Выйти и предложить авторизоваться
                   auth.setTitle(R.string.signin);
                   auth.setSummary(R.string.auth_via_google);
                   signOut();
                } else {
                    // Авторизация и кнопка "Выйти"
                    signIn();
                }
                return true;
            }
        });


//        Preference backup = findPreference("backup");
//        backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            public boolean onPreferenceClick(Preference preference) {
//                backup();
//                return true;
//            }
//        });

        final Preference restore = findPreference("restore");
        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                restore();
                return true;
            }
        });

        Preference syncButton = findPreference("sync");
        syncButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                signIn();
                return true;
            }
        });



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        // Google Drive API
//        mGoogleDriveClient = new GoogleApiClient.Builder(this)
//                .addApi(Drive.API)
//                .addScope(Drive.SCOPE_FILE)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
    }





    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        idToken = null;
                    }
                });
        setAuthFlag(false);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, Resources.RC_SIGN_IN);
    }




    // Drive
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, Resources.RC_DRIVE_API);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }




    private void backup() {
        List<Product> products = productDAO.getAll();

        if (products.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                             R.string.nothing_to_backup, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // Отсылаемый jsonArray


        JSONObject result = new JSONObject();
        try {
            if (idToken == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.error_has_occured_try_to_relogin, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            result.put("idToken", idToken);

            // Массив свежих продуктов
            JSONArray productsJson = new JSONArray();
            for (Product item : products) {
                JSONObject json = item.getJSON();
                productsJson.put(json);
            }


            result.put("products", productsJson);

            List<Group> groups = groupDAO.getAll();
            JSONArray groupsJson = new JSONArray();
            for (Group group: groups) {
                JSONObject json = group.getJSON();
                groupsJson.put(json);
            }
            result.put("groups", groupsJson);
        }
        catch (JSONException e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.internal_error_has_occured, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        new HttpPostBackup(this).execute(result.toString());
    }


    void restore() {
        JSONObject request = new JSONObject();
        try {
            if (idToken == null) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.error_has_occured_try_to_relogin, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            request.put("idToken", idToken);
        }
        catch (JSONException e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.internal_error_has_occured, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        new HttpPostRestore(this).execute(request.toString());
    }


    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Resources.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();

                if (acct != null) {
                    idToken = acct.getIdToken();
                    auth.setTitle(R.string.log_out);
                    auth.setSummary("");
                    Log.i("SIGN IN", "SUCCESS");
                    setAuthFlag(true);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.auth_success, Toast.LENGTH_SHORT);
                    toast.show();
                }
                // Возможно излишне
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.auth_failed, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.i("SIGN IN", "FAILED");
                }

            } else {
                // Signed out, show unauthenticated UI.
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.auth_failed, Toast.LENGTH_SHORT);
                toast.show();
                Log.i("SIGN IN", "FAILED");
            }
        }
        if (requestCode == Resources.RC_DRIVE_API && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }








    private String getSummary (int ID) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int hour, minute;
        switch (ID) {
            case 1: {
                hour = prefs.getInt(Resources.PREF_FIRST_HOUR, 17);
                minute = prefs.getInt(Resources.PREF_FIRST_MINUTE, 0);
                break;
            }
            case 2: {
                hour = prefs.getInt(Resources.PREF_SECOND_HOUR, 17);
                minute = prefs.getInt(Resources.PREF_SECOND_MINUTE, 0);
                break;
            }
            case 3: {
                hour = prefs.getInt(Resources.PREF_THIRD_HOUR, 17);
                minute = prefs.getInt(Resources.PREF_THIRD_MINUTE, 0);
                break;
            }
            case 4: {
                hour = prefs.getInt(Resources.PREF_FOURTH_HOUR, 17);
                minute = prefs.getInt(Resources.PREF_FOURTH_MINUTE, 0);
                break;
            }
            case 5: {
                hour = prefs.getInt(Resources.PREF_FIFTH_HOUR, 17);
                minute = prefs.getInt(Resources.PREF_FIFTH_MINUTE, 0);
                break;
            }
            default: {
                hour = minute = 0;
            }
        }
        if (minute == 0)
            return hour + ":" + minute + '0';
        else if (minute < 10) {
            return hour + ":" + '0' + minute;
        }
        else {
            return hour + ":" + minute;
        }
    }


    protected void onPause() {
        super.onPause();
        boolean firstNotif = sPrefs.getBoolean(Resources.PREF_FIRST_NOTIF, true);
        boolean secondNotif = sPrefs.getBoolean(Resources.PREF_SECOND_NOTIF, false);
        boolean thirdNotif = sPrefs.getBoolean(Resources.PREF_THIRD_NOTIF, false);
        boolean fourthNotif = sPrefs.getBoolean(Resources.PREF_FOURH_NOTIF, false);
        boolean fifthNotif = sPrefs.getBoolean(Resources.PREF_FIFTH_NOTIF, false);
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        SharedPreferences.Editor editor = sPrefs.edit();


        if (!firstNotif && !secondNotif && !thirdNotif && !fourthNotif && !fifthNotif) {
            editor.putBoolean(Resources.PREF_ALARM_SET, false);
            alarmReceiver.cancelAlarm(this);
        } else {
            editor.putBoolean(Resources.PREF_ALARM_SET, true);
            alarmReceiver.setAlarm(this);
        }
        editor.apply();

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
       // mGoogleDriveClient.connect();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Preferences Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.rv150.bestbefore/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Preferences Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.rv150.bestbefore/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private boolean isAuthenticated() {
        return sPrefs.getBoolean("auth_flag", false);
    }

    private void setAuthFlag (boolean value) {
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putBoolean("auth_flag", value);
        editor.apply();
    }




    private class ClearUserDictionaryTask extends AsyncTask<String, String, String> {

        Context context;
        ClearUserDictionaryTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(final String... args) {
            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(DBHelper.AutoCompletedProducts.TABLE_NAME, null, null);
            return null;
        }
    }





    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Drive API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }


        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileInfo.add(String.format("%s (%s)\n",
                            file.getName(), file.getId()));
                }
            }
            return fileInfo;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the Drive API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

}