支付宝、微信支付简单封装<br/>
引用方法:<br/>
支付宝：<br/>
<pre><code>
      AliPayUtils aliPayUtils=new AliPayUtils(VipActivity.this);
      aliPayUtils.pay(strOrderInfo);
      aliPayUtils.setPayResult(new PayResultListener() {
           @Override
           public void paySuccess() {
           //成功
           }
            @Override
            public void payFailure(Sring errorCode, String errStr) {
            //失败
            }
           });
           </code></pre>
微信：<br/>
<pre><code>
      WXPayUtil wxPayUtil=new WXPayUtil(VipActivity.this);
      wxPayUtil.payJSON(strOrderInfo);
      wxPayUtil.setPayListener(new PayResultListener() {
              @Override
               public void paySuccess() {
               //成功
               }
               @Override
               public void payFailure(Sring errorCode, String errStr) {
               //失败
               }
               });
  PS:记得在自己包名下创建.wxapi包名。再创建 WXPayEntryActivity 继承IWXAPIEventHandler并在回调方法
  onResp中加上如下代码：
  Intent intent = new Intent();
	intent.setAction(WXPayUtil.WXACTION);
	intent.putExtra(PayBroadcastReceiver.RESULT_CODE, resp.errCode);
	intent.putExtra(PayBroadcastReceiver.RESULT_ERRSTR, resp.errStr);
	LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	finish();
</code></pre>
混淆
<pre><code>
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.sdk.app.H5PayCallback {
    <fields>;
    <methods>;
}
-keep class com.alipay.android.phone.mrpc.core.** { *; }
-keep class com.alipay.apmobilesecuritysdk.** { *; }
-keep class com.alipay.mobile.framework.service.annotation.** { *; }
-keep class com.alipay.mobilesecuritysdk.face.** { *; }
-keep class com.alipay.tscenter.biz.rpc.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.alipay.tscenter.** { *; }
-keep class com.ta.utdid2.** { *;}
-keep class com.ut.device.** { *;}
</code></pre>
