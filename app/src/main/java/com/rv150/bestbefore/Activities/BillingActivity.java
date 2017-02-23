package com.rv150.bestbefore.Activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.rv150.bestbefore.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ivan on 23.02.17.
 */

public class BillingActivity extends AppCompatActivity {

    private IInAppBillingService mService;
    private static final String mDeveloperPayload;

    static {
            Random generator = new Random();
            StringBuilder randomStringBuilder = new StringBuilder();
            int randomLength = generator.nextInt(20);
            char tempChar;
            for (int i = 0; i < randomLength; i++){
                tempChar = (char) (generator.nextInt(96) + 32);
                randomStringBuilder.append(tempChar);
            }
            mDeveloperPayload = randomStringBuilder.toString();
        }

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            suggestPurchases();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.billing);
        setTitle(R.string.will_eat);
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }



    public void suggestPurchases() {
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("chocolate");
        skuList.add("pizza");
        skuList.add("dinner");
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        new RequestPurchases().execute(querySkus);
    }

    private void handleGoogleResult(Bundle bundle) {
        try {
            int response = bundle.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList
                        = bundle.getStringArrayList("DETAILS_LIST");

                if (responseList == null) {
                    return;
                }

                final int size = responseList.size();
                final Purchase[] items = new Purchase[size];

                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");
                    String desc = object.getString("description");

                    Purchase purchase = new Purchase();
                    purchase.setSku(sku);
                    purchase.setPrice(price);
                    purchase.setDesc(desc);
                    switch (sku) {
                        case "chocolate": {
                            purchase.setTitle(getString(R.string.chocolate_title));
                            items[0] = purchase;
                            break;
                        }
                        case "pizza": {
                            purchase.setTitle(getString(R.string.pizza_title));
                            items[1] = purchase;
                            break;
                        }
                        case "dinner": {
                            purchase.setTitle(getString(R.string.dinner_title));
                            items[2] = purchase;
                            break;
                        }
                    }
                }


                ListView listView = new ListView(this);

                final CustomAdapter adapter = new CustomAdapter(this, R.layout.purchase_item, items);

                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        purchaseItem((Purchase)parent.getItemAtPosition(position));
                    }
                });

                new AlertDialog.Builder(this)
                        .setTitle(R.string.what_do_you_want_to_give_to_developer)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BillingActivity.this.finish();
                            }
                        })
                        .setCancelable(false)
                        .setView(listView)
                        .show();
            }

        } catch (Exception ex) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_SHORT).show();
        }
    }


    private void purchaseItem(Purchase purchase) {
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    purchase.getSku(), "inapp", mDeveloperPayload);
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            if (pendingIntent == null) {
                throw new RuntimeException("Penging intent can not be null");
            }
            startIntentSenderForResult(pendingIntent.getIntentSender(),
                    1001, new Intent(), 0, 0, 0);
        }
        catch (Exception ex) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        }
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.thanks_for_purchase, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }





    private class RequestPurchases extends AsyncTask<Bundle, Void, Void> {
        private final ProgressDialog dialog;
        private Bundle result;
        private boolean stopOperation = false;

        RequestPurchases() {
            dialog = new ProgressDialog(BillingActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(getString(R.string.please_wait));
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    stopOperation = true;
                    finish();
                }
            });
            dialog.show();
        }



        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle querySkus = params[0];
            try {
                result = mService.getSkuDetails(3,
                        getPackageName(), "inapp", querySkus);
            }
            catch (Exception ex) {
                result = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.hide();
            if (!stopOperation) {
                handleGoogleResult(result);
            }
        }
    }



    private class CustomAdapter extends ArrayAdapter<Purchase> {

        private Purchase[] mItems;
        CustomAdapter (Context context, int resource, Purchase[] items) {
            super(context, resource, items);
            this.mItems = items;
        }



        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.purchase_item, parent, false);
            }
            Purchase item = mItems[position];

            TextView itemTitle = (TextView) convertView.findViewById(R.id.purchase_title);
            itemTitle.setText(item.getTitle() + " (" + item.getPrice() + ')');

            TextView itemDesc = (TextView) convertView.findViewById(R.id.purchase_desc);
            itemDesc.setText(item.getDesc());

            return convertView;
        }
    }


    private static final class Purchase {
        String price;
        String title;
        String desc;
        String sku;

        Purchase() {
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

}