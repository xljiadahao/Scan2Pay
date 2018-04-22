package com.paypal.scancode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.paypal.scancode.utils.Constant;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends Activity {

    @Bind(R.id.env)
    RadioGroup env;
    @Bind(R.id.dev)
    RadioButton dev;
    @Bind(R.id.demo)
    RadioButton demo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        int mode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);


    }

    /**
     * button listener，Butterknife or Listener
     * @param view
     */
    @OnClick({R.id.create_code,R.id.scan_2code,R.id.scan_bar_code,R.id.scan_code})
    public void clickListener(View view){
        Intent intent;
        switch (view.getId()){
            case  R.id.create_code:
                intent=new Intent(this,CreateCodeActivity.class);
                startActivity(intent);
                break;
            case  R.id.scan_2code:
                intent=new Intent(this,CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_QRCODE_MODE);
                if (env.getCheckedRadioButtonId() == dev.getId()) {
                    intent.putExtra(Constant.ENV, Constant.ENV_DEV);
                } else {
                    intent.putExtra(Constant.ENV, Constant.ENV_DEMO);
                }
                startActivity(intent);
                break;
            case  R.id.scan_bar_code:
                intent=new Intent(this,CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_BARCODE_MODE);
                startActivity(intent);
                break;
            case  R.id.scan_code:
                intent=new Intent(this,CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_ALL_MODE);
                if (env.getCheckedRadioButtonId() == dev.getId()) {
                    intent.putExtra(Constant.ENV, Constant.ENV_DEV);
                } else {
                    intent.putExtra(Constant.ENV, Constant.ENV_DEMO);
                }
                startActivity(intent);
                break;
        }
    }

}
