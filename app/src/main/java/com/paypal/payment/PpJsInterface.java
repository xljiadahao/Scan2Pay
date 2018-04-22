package com.paypal.payment;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.paypal.scancode.CommonScanActivity;

/**
 * Created by xulei on 22/3/18.
 */

public class PpJsInterface extends Activity {

    private Context context;

    public PpJsInterface(Context c) {
        context= c;
    }

    @JavascriptInterface
    public void pay(final String orderId) {
        Log.i("PpJsInterface pay test", "js thread: " + Thread.currentThread().getId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((PaymentActivity)context).paymentSummary(orderId);
            }
        });
    }

    public void pay (final String orderId, final String amount) {
        Log.i("PpJsInterface pay", "js thread: " + Thread.currentThread().getId()
                + ", orderId: " + orderId + ", amount: " + amount);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((PaymentActivity)context).paymentSummary(orderId, amount);
            }
        });
    }

}
