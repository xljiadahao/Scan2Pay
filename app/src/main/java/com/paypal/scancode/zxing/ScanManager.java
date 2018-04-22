package com.paypal.scancode.zxing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.paypal.scancode.R;
import com.paypal.scancode.zxing.camera.CameraManager;
import com.paypal.scancode.zxing.decode.DecodeThread;
import com.paypal.scancode.zxing.decode.PhotoScanHandler;
import com.paypal.scancode.zxing.decode.RGBLuminanceSource;
import com.paypal.scancode.zxing.utils.BeepManager;
import com.paypal.scancode.zxing.utils.BitmapUtil;
import com.paypal.scancode.zxing.utils.CaptureActivityHandler;
import com.paypal.scancode.zxing.utils.InactivityTimer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class ScanManager implements SurfaceHolder.Callback{
	boolean isHasSurface = false;
	CameraManager cameraManager;
	CaptureActivityHandler handler;
	PhotoScanHandler photoScanHandler;
	Rect mCropRect = null;
	InactivityTimer inactivityTimer;
	public BeepManager beepManager;
	SurfaceView scanPreview = null;
	View scanContainer;
	View scanCropView;
	ImageView scanLine;
	final String TAG= ScanManager.class.getSimpleName();
	Activity activity;
	ScanListener listener;
	boolean isOpenLight=false;

	private int scanMode;

	public ScanManager(Activity activity, SurfaceView scanPreview, View scanContainer,
                       View scanCropView, ImageView scanLine, int scanMode, ScanListener listener) {
		this.activity=activity;
		this.scanPreview=scanPreview;
		this.scanContainer=scanContainer;
		this.scanCropView=scanCropView;
		this.scanLine=scanLine;
		this.listener=listener;
		this.scanMode=scanMode;

		TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				0.9f);
		animation.setDuration(4500);
		animation.setRepeatCount(-1);
		animation.setRepeatMode(Animation.RESTART);
		scanLine.startAnimation(animation);
		
	}

	public ScanManager(ScanListener listener){
		this.listener=listener;
	}
	
	public void onResume(){
		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		inactivityTimer = new InactivityTimer(activity);
		beepManager = new BeepManager(activity);
		cameraManager = new CameraManager(activity.getApplicationContext());
		
		handler = null;
		if (isHasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(scanPreview.getHolder());
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			scanPreview.getHolder().addCallback(this);
		}
		inactivityTimer.onResume();
	}
	public void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		beepManager.close();
		cameraManager.closeDriver();
		if (!isHasSurface) {
			scanPreview.getHolder().removeCallback(this);
		}
	}
	public void onDestroy() {
		inactivityTimer.shutdown();
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!isHasSurface) {
			isHasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isHasSurface = false;
	}
	void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, cameraManager, scanMode);
				Log.e("initCamera", "handler successful！:"+handler);
			}
			initCrop();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			listener.scanError(new Exception("check Permission of Camera for this App"));
		} catch (RuntimeException e) {
			e.printStackTrace();
			listener.scanError(new Exception("check Permission of Camera for this App"));
		}
	}

	public void switchLight(){
		if(isOpenLight){
			cameraManager.offLight();
		}else{
			cameraManager.openLight();
		}
		isOpenLight=!isOpenLight;
	}
	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}
	public Rect getCropRect() {
		return mCropRect;
	}

	public void handleDecode(Result rawResult, Bundle bundle) {
		inactivityTimer.onActivity();
	    beepManager.playBeepSoundAndVibrate();
		bundle.putInt("width", mCropRect.width());
		bundle.putInt("height", mCropRect.height());
		bundle.putString("result", rawResult.getText());
		listener.scanResult(rawResult, bundle);
	}
	public void handleDecodeError(Exception e){
		listener.scanError(e);
	}

	void initCrop() {
		int cameraWidth = cameraManager.getCameraResolution().y;
		int cameraHeight = cameraManager.getCameraResolution().x;

		int[] location = new int[2];
		scanCropView.getLocationInWindow(location);

		int cropLeft = location[0];
		int cropTop = location[1] - getStatusBarHeight();

		int cropWidth = scanCropView.getWidth();
		int cropHeight = scanCropView.getHeight();

		int containerWidth = scanContainer.getWidth();
		int containerHeight = scanContainer.getHeight();

		int x = cropLeft * cameraWidth / containerWidth;
		int y = cropTop * cameraHeight / containerHeight;

		int width = cropWidth * cameraWidth / containerWidth;
		int height = cropHeight * cameraHeight / containerHeight;

		mCropRect = new Rect(x, y, width + x, height + y);
	}
	int getStatusBarHeight() {
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			return activity.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public  void scanningImage(final String photo_path2) {
		if(TextUtils.isEmpty(photo_path2)){
			listener.scanError(new Exception("photo url is null!"));
		}
		photoScanHandler=new PhotoScanHandler(this);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Map<DecodeHintType, Object> hints = DecodeThread.getHints();
				hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
				// Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
				Bitmap bitmap= BitmapUtil.decodeBitmapFromPath(photo_path2,600,600);
				RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
				BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
				QRCodeReader reader = new QRCodeReader();
				MultiFormatReader multiFormatReader=new MultiFormatReader();
				try {
					Message msg= Message.obtain();
					msg.what= PhotoScanHandler.PHOTODECODEOK;
					msg.obj = multiFormatReader.decode(bitmap1, hints);
					photoScanHandler.sendMessage(msg);
				} catch (Exception e) {
					Message msg= Message.obtain();
					msg.what= PhotoScanHandler.PHOTODECODEERROR;
					msg.obj=new Exception("invalid picture");
					photoScanHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	public void reScan(){
		if(handler!=null){
			handler.sendEmptyMessage(R.id.restart_preview);
		}
	}
	public boolean isScanning(){
		if(handler!=null){
			return handler.isScanning();
		}
		return false;
	}

}
