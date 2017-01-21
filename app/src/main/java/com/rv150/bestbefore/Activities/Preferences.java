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
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.rv150.bestbefore.DAO.GroupDAO;
import com.rv150.bestbefore.DAO.ProductDAO;
import com.rv150.bestbefore.Exceptions.DuplicateEntryException;
import com.rv150.bestbefore.Models.Group;
import com.rv150.bestbefore.Models.Product;
import com.rv150.bestbefore.Network.HttpPostRestore;
import com.rv150.bestbefore.R;
import com.rv150.bestbefore.Receivers.AlarmReceiver;
import com.rv150.bestbefore.Resources;
import com.rv150.bestbefore.Services.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.drive.Drive.SCOPE_APPFOLDER;

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
    private Preference auth;
    private boolean fileOperation;

    private String mFileId;

    // private GoogleApiClient mGoogleDriveClient;

    private SharedPreferences sPrefs;
    private ProductDAO productDAO;
    private GroupDAO groupDAO;

    private static final String TAG = "Preferences activity";

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
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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
                .requestScopes(SCOPE_APPFOLDER)
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


        Preference backup = findPreference("backup");
        backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                backup();
                return true;
            }
        });

        final Preference restore = findPreference("restore");
        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                restore();
                return true;
            }
        });


        final Preference restoreDeprecated = findPreference("restore_deprecated");
        restoreDeprecated.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                restore_deprecated();
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
        fileOperation = true;
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }


    private void restore (){
        fileOperation = false;
        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {

            if (result.getStatus().isSuccess()) {

                if (fileOperation) {
                    CreateFileOnGoogleDrive(result);

                } else {
                    OpenFileFromGoogleDrive();

                }
            }
        }
    };


    private void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result) {

        final DriveContents driveContents = result.getDriveContents();

        List<Product> products = productDAO.getAll();
        if (products.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.nothing_to_backup, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        List<Group> groups = groupDAO.getAll();
        Map<String, List> map = new HashMap<>();
        map.put("products", products);
        if (!groups.isEmpty()) {
            map.put("groups", groups);
        }

        final OutputStream outputStream = driveContents.getOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(outputStream);
            oos.writeObject(map);
            oos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.backup_failed, Toast.LENGTH_SHORT);
            toast.show();
            if (oos != null) {
                try {
                    oos.close();
                }
                catch (IOException ee) {
                    Log.e(TAG, "Error closing OutputStream");
                }
            }
            return;
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("Backup")
                .setMimeType("text/plain")
                .setStarred(true).build();
        Drive.DriveApi.getAppFolder(mGoogleApiClient)
                .createFile(mGoogleApiClient, changeSet, driveContents)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFileResult result) {
                        if (result.getStatus().isSuccess()) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    R.string.backup_success, Toast.LENGTH_SHORT);
                            toast.show();

                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    R.string.backup_failed, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });

    }




    public void OpenFileFromGoogleDrive(){
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setActivityStartFolder(Drive.DriveApi.getAppFolder(mGoogleApiClient).getDriveId())
                .setMimeType(new String[] { "text/plain", "text/html" })
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, Resources.RC_INTENT_SENDER, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.w(TAG, "Unable to send intent", e);
        }

//                    final DriveId myFileId = metadataBuffer.get(0).getDriveId();
//                    final DriveFile file = myFileId.asDriveFile();
//                    file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
//                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
//                        @Override
//                        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
//                            if (!result.getStatus().isSuccess()) {
//                                Toast toast = Toast.makeText(getApplicationContext(),
//                                        R.string.restore_failed, Toast.LENGTH_SHORT);
//                                toast.show();
//                                return;
//                            }
//                            DriveContents contents = result.getDriveContents();
//                            try {
//                                ObjectInputStream objectInputStream = new ObjectInputStream(contents.getInputStream());
//                                final Map<String, List> map = (Map) objectInputStream.readObject();
//                                objectInputStream.close();
//
//                                List<Product> currentProducts = productDAO.getAll();
//                                if (!currentProducts.isEmpty()) {
//                                    new AlertDialog.Builder(Preferences.this)
//                                            .setTitle(R.string.warning)
//                                            .setMessage(R.string.do_you_want_to_overwite_existing)
//                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
//                                            {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    saveRestored(map, false);
//                                                }
//
//                                            })
//                                            .setNeutralButton(R.string.combine, new DialogInterface.OnClickListener()
//                                            {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    saveRestored(map, true);
//                                                }
//
//                                            })
//                                            .setNegativeButton(R.string.no, null)
//                                            .show();
//                                }
//                                else {
//                                    saveRestored(map, false);
//                                }
//                            }
//                            catch (Exception e) {
//                                Toast toast = Toast.makeText(getApplicationContext(),
//                                        R.string.restore_failed + e.getMessage(), Toast.LENGTH_SHORT);
//                                toast.show();
//                            }
//                        }
//                    });
//
//                }
//                else {
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            R.string.data_not_found, Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//                metadataBuffer.release();
//            }
//        });

    }






    private void saveRestored(Map<String, List> map, boolean union) {
        List<Product> products = (List) map.get("products");
        List<Group> groups = (List) map.get("groups");

        Map<Long, Long> oldIdToNew = new HashMap<>(); // Старые ID к новым для правильных внешних ключей

        if (!union) {
            productDAO.deleteAll();
            groupDAO.deleteAll();
        }

        if (groups != null) {
            for (Group group : groups) {
                long oldId = group.getId();
                long newId;
                try {
                    newId = groupDAO.insertGroup(group);
                } catch (DuplicateEntryException e) {
                    newId = groupDAO.get(group.getName()).getId();
                }
                oldIdToNew.put(oldId, newId);
            }
        }

        for (Product product: products) {
            long oldGroupId = product.getGroupId();
            if (oldGroupId != -1) {
                long newGroupId =  oldIdToNew.get(oldGroupId);
                product.setGroupId(newGroupId);
            }
            productDAO.insertProduct(product);
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                R.string.restore_success, Toast.LENGTH_SHORT);
        toast.show();
    }





    private void restore_deprecated() {
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
            if (result != null && result.isSuccess()) {
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
}