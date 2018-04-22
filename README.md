# Scan2Pay

### Fork and Base project: [ScanCode Repository](https://github.com/liang530/ScanCode.git)
### Wiki: [Tech Blog : Showcase - Seamless Super App Prototype](https://xljiadahao.github.io/2018/04/15/Xu-Lei-Showcase-Super-App-Seamless-Payment-Framework-Prototype/)

## Description

With `Super App` and `Seamless Integration` concepts, this prototype provides a demo app for the seamless payment experience. <br/>
`Super App`: Super App means open for Integration, Integration and Integration! (Future App Concept, refer to WeChat Little Program) The future apps allows users `Use and Leave` without downloading. <br/>
`Seamless Integration`: Blur the lines of payment between online and offline. Blur the lines between different systems. 

## Design

![upload certificate](https://github.com/xljiadahao/xljiadahao.github.io/blob/master/images/superapp/logic_view.png "Upload Certificate")

## Development Guideline

Check out the [Backend Service Repo](https://github.com/xljiadahao/SnippetInnovationWithSpringBoot.git), branch `pypl`
1. Generate QR code. Stand for step 1, under the backend service repo, refer to [Unit Test](https://github.com/xljiadahao/SnippetInnovationWithSpringBoot/blob/pypl/src/test/java/com/snippet/jwt/QRCodeTokenTest.java), run the Unit Test to generate the QR code under qrcode directory. (You might need to build the merchant portal web application and provide the URL of the merchant portal which is required to be embedded in the token.)
2. Run the Backend Service Repo as the Spring Boot application (change the target to jar instead of war in the pom.xml). Here is the [Token Verification End Point URL](https://github.com/xljiadahao/SnippetInnovationWithSpringBoot/blob/pypl/src/main/java/com/snippet/jwt/JsonWebTokenController.java#L83), sample: `http://url/ppverify?token=xxx`. Note: As shown step 8, if you do NOT want to get the notification from the payment gateway, you need to directly set the payment status as [PAID](https://github.com/xljiadahao/SnippetInnovationWithSpringBoot/blob/pypl/src/main/java/com/snippet/paypal/PaymentRestController.java#L45); otherwise, the merchant portal need to consume the request coming from the payment gateway, here is the [Merchant Portal URL Configured in Payment Gateway](https://github.com/xljiadahao/SnippetInnovationWithSpringBoot/blob/pypl/src/main/java/com/snippet/paypal/PaymentRestController.java#L28), sample: `http://url/merchantportal/pay.rest, POST param: {"merchantId": "33", "orderId": "33"}`.
3. Merchant Portal need to integrate with Scan2Pay app by calling the snippet as below for step 6, and here is the [Sample HTML5 Snippet](https://github.com/xljiadahao/SnippetInnovationWithSpringBoot/blob/pypl/src/main/resources/templates/pay.html). Merchant Portal will provide the [Receipt URL](https://github.com/xljiadahao/Scan2Pay/blob/master/app/src/main/java/com/paypal/payment/PaymentActivity.java#L149) as well, sample: `http://url/getReciept.action?orderId=33&merchantId=33`; or you might want to ignore `getReceipt()` method.
```
function pay() {
    var orderId = $("#order").val();
    var amount = $("#amount").val();
    window.android.pay(orderId, amount);
}
```
4. Build the Scan2Pay project as the Android app. Configure the [Token Verification End Point URL](https://github.com/xljiadahao/Scan2Pay/blob/master/app/src/main/java/com/paypal/scancode/utils/Constant.java#L22).

Here you go. The demo app is ready to use.
