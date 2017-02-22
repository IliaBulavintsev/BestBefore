package com.rv150.bestbefore.Services;

import android.app.Application;

import org.solovyev.android.checkout.Billing;

/**
 * Created by ivan on 20.02.17.
 */

public class MyApplication extends Application {

    private static MyApplication sInstance;

    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Override
        public String getPublicKey() {
            final String s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkzrZVd1QI" +
                    "0wcYV0lOHFr3M9Xg8ntBYQTA9MYqjz+2y7qcSIUeuEG2ORGyZwt/dPTd0RVIYHyZ/" +
                    "3WYA6oBNRAqj36OIAtAxpKY3+yyMfVXGtX97Crms1oEkHBU0Ma9n0cW0HmYT8kXN814R" +
                    "RsCuz46GkEt62Ce73aVd0LG43FA1m1sHvc8Z02kOCcNMLfNysn93nydpr8yb4gsuwfE" +
                    "HZ10n4yy7/0FJC17ybdKUiANRC+n/nIbFCWkMUeI0WjzrupxS+eypyZbT38K3br1Glao" +
                    "RPUD8INr2OGFWJuA87+52OQcNopKbTDUeZFLAbwQvwuJI0hN1LLohyimJSmkrtgaQIDAQAB";
            return s;
           // return Encryption.decrypt(s, "se.solovyev@gmail.com");
        }
    });

    public MyApplication() {
        sInstance = this;
    }

    public static MyApplication get() {
        return sInstance;
    }

    public Billing getBilling() {
        return mBilling;
    }
}