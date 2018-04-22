/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.scancode;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.paypal.payment.PaymentActivity;
import com.paypal.pyplqrcode.bo.Merchant;
import com.paypal.scancode.utils.Constant;
import com.paypal.scancode.zxing.ScanListener;
import com.paypal.scancode.zxing.ScanManager;
import com.paypal.scancode.zxing.decode.DecodeThread;
import com.paypal.scancode.zxing.decode.Utils;

import org.springframework.http.HttpStatus;
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

//@TargetApi(Build.VERSION_CODES.GINGERBREAD)
//@SuppressLint("NewApi")
public final class CommonScanActivity extends Activity implements ScanListener, View.OnClickListener {

    private String token;
    private String env;

    static final String TAG = CommonScanActivity.class.getSimpleName();
    SurfaceView scanPreview = null;
    View scanContainer;
    View scanCropView;
    ImageView scanLine;
    ScanManager scanManager;
    TextView iv_light;
    TextView qrcode_g_gallery;
    TextView qrcode_ic_back;
    final int PHOTOREQUESTCODE = 1111;

    @Bind(R.id.service_register_rescan)
    Button rescan;
    @Bind(R.id.service_process)
    Button process;
    @Bind(R.id.scan_image)
    ImageView scan_image;
    @Bind(R.id.authorize_return)
    ImageView authorize_return;
    private int scanMode;

    @Bind(R.id.common_title_TV_center)
    TextView title;
//    @Bind(R.id.scan_hint)
//    TextView scan_hint;
    @Bind(R.id.tv_scan_result)
    TextView tv_scan_result;
    @Bind(R.id.webView)
    WebView webView;


    /// @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scan_code);
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
        ButterKnife.bind(this);
        scanMode=getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_ALL_MODE);
        env = getIntent().getStringExtra(Constant.ENV);
        initView();
    }

    void initView() {
        switch (scanMode){
            case DecodeThread.BARCODE_MODE:
                title.setText(R.string.scan_barcode_title);
                // scan_hint.setText(R.string.scan_barcode_hint);
                break;
            case DecodeThread.QRCODE_MODE:
                title.setText(R.string.scan_qrcode_title);
                // scan_hint.setText(R.string.scan_qrcode_hint);
                break;
            case DecodeThread.ALL_MODE:
                title.setText(R.string.scan_allcode_title);
                // scan_hint.setText(R.string.scan_allcode_hint);
                break;
        }
        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = findViewById(R.id.capture_container);
        scanCropView = findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        qrcode_g_gallery = (TextView) findViewById(R.id.qrcode_g_gallery);
        qrcode_g_gallery.setOnClickListener(this);
        qrcode_ic_back = (TextView) findViewById(R.id.qrcode_ic_back);
        qrcode_ic_back.setOnClickListener(this);
        iv_light = (TextView) findViewById(R.id.iv_light);
        iv_light.setOnClickListener(this);
        rescan.setOnClickListener(this);
        process.setOnClickListener(this);
        authorize_return.setOnClickListener(this);
        scanManager = new ScanManager(this, scanPreview, scanContainer, scanCropView, scanLine, scanMode,this);
    }

    @Override
    public void onResume() {
        super.onResume();
        scanManager.onResume();
        rescan.setVisibility(View.INVISIBLE);
        process.setVisibility(View.INVISIBLE);
        scan_image.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        scanManager.onPause();
    }
    /**
     *
     */
    public void scanResult(Result rawResult, Bundle bundle) {
        if (!scanManager.isScanning()) {
            rescan.setVisibility(View.VISIBLE);
            scan_image.setVisibility(View.VISIBLE);
            process.setVisibility(View.VISIBLE);
            Bitmap barcode = null;
            byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
            if (compressedBitmap != null) {
                barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
            }
            scan_image.setImageBitmap(barcode);
        }
        rescan.setVisibility(View.VISIBLE);
        process.setVisibility(View.VISIBLE);
        scan_image.setVisibility(View.VISIBLE);
        tv_scan_result.setVisibility(View.VISIBLE);
        token = rawResult.getText();
        String result = null;
        if (rawResult.getText().length() > 140) {
            result = rawResult.getText().substring(0, 70) + "..." + rawResult.getText().substring(rawResult.getText().length() - 70);
        } else {
            result = rawResult.getText();
        }
        tv_scan_result.setText("Info："+ result);
        if (Constant.ENV_DEMO.equalsIgnoreCase(env)) {
            token();
        }
    }

    void startScan() {
        if (rescan.getVisibility() == View.VISIBLE || process.getVisibility() == View.VISIBLE) {
            rescan.setVisibility(View.INVISIBLE);
            process.setVisibility(View.INVISIBLE);
            scan_image.setVisibility(View.GONE);
            scanManager.reScan();
        }
    }

    @Override
    public void scanError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        if(e.getMessage()!=null&&e.getMessage().startsWith("check Permission of Camera")){
            scanPreview.setVisibility(View.INVISIBLE);
        }
    }

    public void showPictures(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photo_path;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTOREQUESTCODE:
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor.moveToFirst()) {
                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        photo_path = cursor.getString(colum_index);
                        if (photo_path == null) {
                            photo_path = Utils.getPath(getApplicationContext(), data.getData());
                        }
                        scanManager.scanningImage(photo_path);
                    }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qrcode_g_gallery:
                showPictures(PHOTOREQUESTCODE);
                break;
            case R.id.iv_light:
                scanManager.switchLight();
                break;
            case R.id.qrcode_ic_back:
                finish();
                break;
            case R.id.service_register_rescan:
                startScan();
                break;
            case R.id.authorize_return:
                finish();
                break;
            case R.id.service_process:
                System.out.println("process Order and Payment");
                token();
                break;
            default:
                break;
        }
    }

    private void token() {
        new Thread(networkTask).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            if (!data.getBoolean("verifyStatus")) {
                if (data.getString("toast") != null) {
                    Toast.makeText(CommonScanActivity.this, data.getString("toast"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CommonScanActivity.this, "Invalid PayPal Token", Toast.LENGTH_LONG).show();
                }
                return;
            }
            Intent intent = new Intent(CommonScanActivity.this, PaymentActivity.class);
            long id = data.getLong("id");
            String merchantName = data.getString("merchantName");
            String merchantUrl = data.getString("url");
            Log.i("prototype", "request result -->" + id + ", " + merchantName + ", " + merchantUrl);
            intent.putExtra("id", id);
            intent.putExtra("merchantName", merchantName);
            intent.putExtra("url", merchantUrl);
            startActivity(intent);
            finish();
        }
    };

    Runnable networkTask = new Runnable(){
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            Merchant merchant = null;
            try{
                merchant = verifyToken(token);
            } catch (ResourceAccessException rex) {
                System.out.println("Internet Not Accessible, " + rex.getMessage());
                data.putBoolean("verifyStatus", false);
                data.putString("toast", "Internet Not Accessible");
            } catch (Exception ex) {
                System.out.println("Unexpected Error, " + ex.getMessage());
                data.putBoolean("verifyStatus", false);
                data.putString("toast", "Internet Not Accessible");
            }
            if (merchant != null) {
                System.out.println("remote test: " + merchant.getName() + ", " + merchant.getUrl());
                data.putLong("id", merchant.getId());
                data.putString("merchantName", merchant.getName());
                data.putString("url", merchant.getUrl());
                data.putBoolean("verifyStatus", true);
            } else {
                data.putBoolean("verifyStatus", false);
            }
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private Merchant verifyToken(String token) {
        RestTemplate restClient = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        //Add the Jackson Message converter
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> media = new ArrayList<>();
        media.add(MediaType.ALL);
        converter.setSupportedMediaTypes(media);
        messageConverters.add(converter);
        restClient.setMessageConverters(messageConverters);
        StringBuilder urlBuilder = new StringBuilder(Constant.PP_ENDPOINT_URL);
        urlBuilder.append("/").append("tokenmgt/ppverify?token={token}");
        ResponseEntity<Merchant> response  = null;
        try {
            response = restClient.getForEntity(urlBuilder.toString(), Merchant.class, token);
            if(response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.out.println("token verification status code: " + response.getStatusCode().value());
                return null;
            }
        } catch (Exception ex) {
            System.out.println("token verification failed: " + ex.getMessage());
            return null;
        }
    }

}