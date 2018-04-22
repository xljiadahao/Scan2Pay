package com.paypal.scancode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.paypal.scancode.zxing.encode.EncodingHandler;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateCodeActivity extends Activity {
    @Bind(R.id.et_code_key)
    EditText etCodeKey;
    @Bind(R.id.btn_create_code)
    Button btnCreateCode;
    @Bind(R.id.iv_2_code)
    ImageView iv2Code;
    @Bind(R.id.iv_bar_code)
    ImageView ivBarCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_code);
        ButterKnife.bind(this);
    }
    @OnClick({R.id.btn_create_code,R.id.btn_create_code_and_img})
    public void clickListener(View view){
        String key=etCodeKey.getText().toString();
        switch (view.getId()){
            case  R.id.btn_create_code:
                if(TextUtils.isEmpty(key)){
                    Toast.makeText(this,"input content", Toast.LENGTH_SHORT).show();
                }else{
                    create2Code(key);
                    createBarCode(key);
                }
                break;
            case  R.id.btn_create_code_and_img:
                Bitmap bitmap = create2Code(key);
                Bitmap headBitmap = getHeadBitmap(60);
                if(bitmap!=null&&headBitmap!=null){
                    createQRCodeBitmapWithPortrait(bitmap,headBitmap);
                }
                break;
        }
    }
    private Bitmap createBarCode(String key) {
        Bitmap qrCode = null;
        try {
            qrCode = EncodingHandler.createBarCode(key, 600, 300);
            ivBarCode.setImageBitmap(qrCode);
        } catch (Exception e) {
            Toast.makeText(this,"the content is not supported", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return qrCode;
    }

    private Bitmap create2Code(String key) {
        Bitmap qrCode=null;
        try {
            qrCode= EncodingHandler.create2Code(key, 400);
            iv2Code.setImageBitmap(qrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return qrCode;
    }

    private Bitmap getHeadBitmap(int size) {
        try {
            Bitmap portrait = BitmapFactory.decodeResource(getResources(),R.drawable.head);
            Matrix mMatrix = new Matrix();
            float width = portrait.getWidth();
            float height = portrait.getHeight();
            mMatrix.setScale(size / width, size / height);
            return Bitmap.createBitmap(portrait, 0, 0, (int) width,
                    (int) height, mMatrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createQRCodeBitmapWithPortrait(Bitmap qr, Bitmap portrait) {

        int portrait_W = portrait.getWidth();
        int portrait_H = portrait.getHeight();

        int left = (qr.getWidth() - portrait_W) / 2;
        int top = (qr.getHeight() - portrait_H) / 2;
        int right = left + portrait_W;
        int bottom = top + portrait_H;
        Rect rect1 = new Rect(left, top, right, bottom);

        Canvas canvas = new Canvas(qr);
        Rect rect2 = new Rect(0, 0, portrait_W, portrait_H);
        canvas.drawBitmap(portrait, rect2, rect1, null);
    }

}
