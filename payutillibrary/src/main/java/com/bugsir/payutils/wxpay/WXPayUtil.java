package com.bugsir.payutils.wxpay;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Xml;
import android.widget.Toast;

import com.bugsir.payutils.PayBroadcastReceiver;
import com.bugsir.payutils.PayResultListener;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;

public class WXPayUtil {

    public static String WXACTION = "com.bugsir.payutils.wxpayutil";

    public static String KEY = "";//应用签名
    public static String APPID = "";
    public static final String APP_ID = "appid";
    public static final String MCH_ID = "mch_id";
    public static final String PREPAY_ID = "prepay_id";
    public static final String PACKAGE_VALUE = "Sign=WXPay";
    //
    public static final String ORDER_NONCE_STR = "noncestr";
    public static final String ORDER_PACKAGE = "package";
    public static final String ORDER_PARTNERID = "partnerid";
    public static final String ORDER_PREPAY_ID = "prepayid";
    public static final String ORDER_TIMESTAMP = "timestamp";
    private PayReq mPayReq;
    private IWXAPI mIwxapi;
    private Context context;
    private PayBroadcastReceiver receiver;

    public WXPayUtil(Context context) {
        this.context = context;
        mPayReq = new PayReq();
        mIwxapi = WXAPIFactory.createWXAPI(context, null);
        receiver = new PayBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WXACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    public IWXAPI getIWAPI() {
        return mIwxapi;
    }

    public void unRegisterReceiver()
    {
        if (receiver!=null&&context!=null)
        {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        }

    }

    /**
     * @param info json格式数据（一般是服务端处理好所有数据了）
     */
    public void payJSON(String info) {
        if (!mIwxapi.isWXAppInstalled())
        {
            Toast.makeText(context,"您未安装微信，请先下载微信",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject payObject = new JSONObject(info);
            if (payObject.has(APP_ID)) {

                mPayReq.appId = payObject.getString(APP_ID);
                APPID = mPayReq.appId;
                mPayReq.partnerId = payObject.getString(ORDER_PARTNERID);
                mPayReq.prepayId = payObject.getString(ORDER_PREPAY_ID);
                mPayReq.packageValue = payObject.getString(ORDER_PACKAGE);
                mPayReq.nonceStr = payObject.getString(ORDER_NONCE_STR);
                mPayReq.timeStamp = payObject.getString(ORDER_TIMESTAMP);
                mPayReq.sign = payObject.getString("sign");
//                HashMap<String, String> values = new HashMap<>();
//                values.put(APP_ID, mPayReq.appId);
//                values.put(ORDER_NONCE_STR, mPayReq.nonceStr);
//                values.put(ORDER_PACKAGE, mPayReq.packageValue);
//                values.put(ORDER_PARTNERID, mPayReq.partnerId);
//                values.put(ORDER_PREPAY_ID, mPayReq.prepayId);
//                values.put(ORDER_TIMESTAMP, mPayReq.timeStamp);
//                mPayReq.sign = genAppSign(values);
                mIwxapi.registerApp(mPayReq.appId);
                mIwxapi.sendReq(mPayReq);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * @param info xml模式的订单格式（客户端处理签名）
     */
    public void payXML(String info) {
        Map<String, String> hashMap = decodeXml(info);
        if (hashMap == null) {
            throw new IllegalArgumentException("XML pay data is null");
        }
        if (mPayReq == null) {
            throw new IllegalArgumentException("wxpay PayReq is null");
        }
        if (mIwxapi == null) {
            throw new IllegalArgumentException("wxpay IWXAPI is null");
        }
        mPayReq.appId = hashMap.get(APP_ID);
        mPayReq.partnerId = hashMap.get(MCH_ID);
        mPayReq.prepayId = hashMap.get(PREPAY_ID);
        mPayReq.packageValue = "Sign=WXPay";
        mPayReq.nonceStr = genNonceStr();
        mPayReq.timeStamp = String.valueOf(genTimeStamp());
        HashMap<String, String> values = new HashMap<>();
        values.put(APP_ID, mPayReq.appId);
        values.put(ORDER_NONCE_STR, mPayReq.nonceStr);
        values.put(ORDER_PACKAGE, mPayReq.packageValue);
        values.put(ORDER_PARTNERID, mPayReq.partnerId);
        values.put(ORDER_PREPAY_ID, mPayReq.prepayId);
        values.put(ORDER_TIMESTAMP, mPayReq.timeStamp);
        mPayReq.sign = hashMap.get("sign");

        mIwxapi.registerApp(hashMap.get(APP_ID));
        mIwxapi.sendReq(mPayReq);
    }

    private String genAppSign(HashMap<String, String> values) {
        StringBuilder sb = new StringBuilder();
        Iterator iter = values.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');

        }
        sb.append("key=");
        sb.append(KEY);

        // this.sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = WXMD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        return appSign;
    }


    private String genNonceStr() {
        Random random = new Random();
        return WXMD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 解析xml
     *
     * @param content
     * @return
     */
    private Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            // 实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public void setPayListener(PayResultListener listener) {
        receiver.setPayResultListener(listener);
    }


}

