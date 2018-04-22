package com.paypal.scancode.zxing.utils;

public class AppliationUtil {

    public static int MAX_MEMORY = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
    public static long TOTAL_MEMORY = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
    public static long FREE_MEMORY = ((int) Runtime.getRuntime().freeMemory())/1024/1024;

}