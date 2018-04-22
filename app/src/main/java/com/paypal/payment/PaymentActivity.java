package com.paypal.payment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.pyplqrcode.bo.PaymentData;
import com.paypal.pyplqrcode.bo.PaymentStatus;
import com.paypal.scancode.R;
import com.paypal.scancode.utils.Constant;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by xulei on 24/3/18.
 */

public class PaymentActivity extends Activity implements View.OnClickListener {

    @Bind(R.id.merchant)
    TextView merchant;
    @Bind(R.id.order)
    TextView order;
    @Bind(R.id.amount)
    TextView amount;
    @Bind(R.id.pay)
    Button pay;
    @Bind(R.id.webviewmerchant)
    WebView webView;

    private long merchantId;
    private String merchantName;
    private String merchantUrl;
    private String orderId;
    private String totalAmount;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_pay);
        ButterKnife.bind(this);

        merchant.setVisibility(View.INVISIBLE);
        order.setVisibility(View.INVISIBLE);
        amount.setVisibility(View.INVISIBLE);
        pay.setVisibility(View.INVISIBLE);

        pay.setOnClickListener(this);

        merchantId = getIntent().getLongExtra("id", 0l);
        merchantName = getIntent().getStringExtra("merchantName");
        merchantUrl =getIntent().getStringExtra("url");
        Log.i("payment", "pay with PayPal-->" + merchantId + ", " + merchantName + ", " + merchantUrl + "; "
                + "PaymentActivity onCreate UI thread: " + Thread.currentThread().getId());
        webView.setVisibility(View.VISIBLE);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(merchantUrl);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new PpJsInterface(PaymentActivity.this), "android");
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    public void paymentSummary(String orderId) {
        Log.i("paymentSummary test", "UI thread: " + Thread.currentThread().getId());
        merchant.setText(merchantName);
        merchant.setVisibility(View.VISIBLE);
        this.orderId = orderId;
        order.setText(orderId);
        order.setVisibility(View.VISIBLE);
        amount.setVisibility(View.VISIBLE);
        pay.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
    }

    public void paymentSummary(String orderId, String amount) {
        Log.i("paymentSummary", "UI thread: " + Thread.currentThread().getId()
                + ", orderId: " + orderId + ", amount: " + amount);
        this.merchant.setText(merchantName);
        this.merchant.setVisibility(View.VISIBLE);
        this.orderId = orderId;
        this.order.setText(orderId);
        this.order.setVisibility(View.VISIBLE);
        this.totalAmount = amount;
        this.amount.setText(amount + " SGD");
        this.amount.setVisibility(View.VISIBLE);
        this.pay.setVisibility(View.VISIBLE);
        this.webView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        System.out.println("test view: " + v.getId());
        switch (v.getId()) {

            case R.id.pay:
                Log.i("Pay with PayPal", "UI thread: " + Thread.currentThread().getId());
                pay();
                break;
            default:
                break;
        }
    }

    private void pay() {
        new Thread(networkTask).start();
    }

    private void getReceipt() {
        webView.setVisibility(View.VISIBLE);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        String receiptUrlRoot = merchantUrl.substring(0, merchantUrl.lastIndexOf("/"));
        Log.i("getReceipt", merchantId + ", " + orderId + ", " + receiptUrlRoot);
        StringBuilder receiptUrl = new StringBuilder(receiptUrlRoot);
        receiptUrl.append("/").append("getReciept.action?orderId=")
                .append(orderId).append("&merchantId=").append(String.valueOf(merchantId));
        webView.loadUrl(receiptUrl.toString());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            Toast.makeText(PaymentActivity.this, data.getString("toast"), Toast.LENGTH_LONG).show();
            if (!data.getBoolean("paymentStatus")) {
                return;
            }
            getReceipt();
        }
    };

    Runnable networkTask = new Runnable(){
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            PaymentData paymentData = null;
            try{
                paymentData = processPayment();
            } catch (ResourceAccessException rex) {
                System.out.println("Internet Not Accessible, " + rex.getMessage());
                data.putBoolean("paymentStatus", false);
                data.putString("toast", "Internet Not Accessible");
            } catch (Exception ex) {
                System.out.println("Unexpected Error, " + ex.getMessage());
                data.putBoolean("paymentStatus", false);
                data.putString("toast", "Unexpected Error");
            }
            if (paymentData != null) {
                if (paymentData.getStatus() == PaymentStatus.PAID) {
                    data.putBoolean("paymentStatus", true);
                    data.putString("toast", "Payment Successful");
                } else {
                    data.putBoolean("paymentStatus", false);
                    data.putString("toast", "Payment Failed");
                }
            } else {
                data.putBoolean("paymentStatus", false);
                data.putString("toast", "Payment Failed");
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private PaymentData processPayment() {
        RestTemplate restClient = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> media = new ArrayList<>();
        media.add(MediaType.ALL);
        converter.setSupportedMediaTypes(media);
        messageConverters.add(converter);
        restClient.setMessageConverters(messageConverters);

        StringBuilder urlBuilder = new StringBuilder(Constant.PP_ENDPOINT_URL);
        urlBuilder.append("/tokenmgt").append("/paywithpaypal");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        PaymentData params = new PaymentData();
        params.setMerchantId(String.valueOf(merchantId));
        params.setMerchantName(merchantName);
        params.setOrderId(orderId);
        params.setAmount(Double.valueOf(totalAmount));
        HttpEntity<PaymentData> requestEntity = new HttpEntity<PaymentData>(params, headers);
        try {
            ResponseEntity<PaymentData> response = restClient.exchange(urlBuilder.toString(),
                    HttpMethod.POST, requestEntity, PaymentData.class);
            PaymentData paymentData = response.getBody();
            return paymentData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
