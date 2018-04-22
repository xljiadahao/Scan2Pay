package com.paypal.pyplqrcode.bo;

/**
 * Created by xulei on 20/3/18.
 */

public class Merchant {

    private long id;
    private String name;
    private String url;
    private String paypalAccount;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getPaypalAccount() {
        return paypalAccount;
    }
    public void setPaypalAccount(String paypalAccount) {
        this.paypalAccount = paypalAccount;
    }

}
