package com.paypal.scancode.utils;

public interface Constant {

    public static final String REQUEST_SCAN_TYPE="type";
    public static final int REQUEST_SCAN_TYPE_COMMON=0;
    public static final int REQUEST_SCAN_TYPE_REGIST=1;
    public static final String REQUEST_SCAN_MODE="ScanMode";
    public static final int REQUEST_SCAN_MODE_BARCODE_MODE = 0X100;
    public static final int REQUEST_SCAN_MODE_QRCODE_MODE = 0X200;
    public static final int REQUEST_SCAN_MODE_ALL_MODE = 0X300;

    public static final String ENV = "environment";
    public static final String ENV_DEV = "dev";
    public static final String ENV_DEMO = "demo";

    /**
     * Payment Gateway URL, for token validation and payment process
     * To be configured
     */
    // TODO
    public static final String PP_ENDPOINT_URL = "URL";

}
