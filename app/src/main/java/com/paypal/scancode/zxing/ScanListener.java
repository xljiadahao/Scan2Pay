package com.paypal.scancode.zxing;

import android.os.Bundle;

import com.google.zxing.Result;

public interface ScanListener {

	public void scanResult(Result rawResult, Bundle bundle);
	public void scanError(Exception e);
	
}
