package com.bugsir.payutils.alipay;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.bugsir.payutils.AlipayAuthResultListener;
import com.bugsir.payutils.PayResultListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝支付工具集合
 */
public class AliPayUtils {


    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    private static final int SDK_AUTH_FLAG=3;

    private Activity con;
    private PayResultListener payResultListener;
    private AlipayAuthResultListener authResultListener;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG:{
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        if (payResultListener != null) {
                            payResultListener.paySuccess();
                        }
                    } else {
                        if (payResultListener != null) {
                            payResultListener.payFailure(resultStatus,resultInfo);
                        }
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
//                        if (TextUtils.equals(resultStatus, "8000")) {
//                            Toast.makeText(con, "支付结果确认中",
//                                    Toast.LENGTH_SHORT).show();
//
//                        } else {
//                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                            Toast.makeText(con, "支付失败"+resultStatus,
//                                    Toast.LENGTH_SHORT).show();
//                            if (payResultListener != null) {
//                                payResultListener.payFailure(resultStatus,resultInfo);
//                            }
//                        }
                    }}
                    break;

                case SDK_CHECK_FLAG:
                    Toast.makeText(con, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                case SDK_AUTH_FLAG:{//登录
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        if (authResultListener!=null)
                        {
                            authResultListener.authSuccess(authResult);
                        }
                    } else {
                        // 其他状态值则为授权失败
                        if (authResultListener!=null)
                        {
                            authResultListener.authFailure();
                        }
                    }}
                    break;
                default:
                    break;
            }
        }

        ;
    };


    public void authV2(final String strAuthInfo)
    {
        Runnable authRunnable = new Runnable() {

            @Override
            public void run() {
                AuthTask alipay = new AuthTask(con);
                Map<String, String> result = alipay.authV2(strAuthInfo,true);

                Message msg = new Message();
                msg.what = SDK_AUTH_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(authRunnable);
        payThread.start();
    }


    /**
     * 后台已组织好数据
     */
    public void pay(final String info)
    {
    	 Runnable payRunnable = new Runnable() {

             @Override
             public void run() {
                 PayTask alipay = new PayTask(con);
                 Map<String, String> result = alipay.payV2(info,true);

                 Message msg = new Message();
                 msg.what = SDK_PAY_FLAG;
                 msg.obj = result;
                 mHandler.sendMessage(msg);
             }
         };
          // 必须异步调用
         Thread payThread = new Thread(payRunnable);
         payThread.start();
    }

    private  String getTimeStrByFormat(long time, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        String nowdate = df.format(new Date(time));
        return nowdate;
    }


    public AliPayUtils(Activity con)
    {
    	this.con = con;
    }


    /**
     * 构造支付订单参数列表
     * @param app_id
     * @param total_amount
     * @param subject
     * @param body
     * @param out_trade_no
     * @param rsaKey
     * @param rsa2
     */
    public  void pay(String app_id,String total_amount,String subject,String body,String out_trade_no,String rsaKey,boolean rsa2) {
        Map<String, String> keyValues = new HashMap<String, String>();

        keyValues.put("app_id", app_id);

        keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\""+total_amount+"\",\"subject\":\""+subject+"\",\"body\":\""+body+"\",\"out_trade_no\":\"" + out_trade_no +  "\"}");

        keyValues.put("charset", "utf-8");

        keyValues.put("method", "alipay.trade.app.pay");

        keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");

        keyValues.put("timestamp", getTimeStrByFormat(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"));

        keyValues.put("version", "1.0");
        String orderParam=buildOrderParam(keyValues);
        String sign=getSign(keyValues,rsaKey,true);
        String orderInfo=orderParam+ "&" + sign;
        pay(orderInfo);
    }

    /**
     * 构造支付订单参数信息
     *
     * @param map
     * 支付订单参数
     * @return
     */
    private  String buildOrderParam(Map<String, String> map) {
        List<String> keys = new ArrayList<String>(map.keySet());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            sb.append(buildKeyValue(key, value, true));
            sb.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        sb.append(buildKeyValue(tailKey, tailValue, true));

        return sb.toString();
    }

    /**
     * 拼接键值对
     *
     * @param key
     * @param value
     * @param isEncode
     * @return
     */
    private static String buildKeyValue(String key, String value, boolean isEncode) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("=");
        if (isEncode) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sb.append(value);
            }
        } else {
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * 对支付参数信息进行签名
     *
     * @param map
     *            待签名授权信息
     *
     * @return
     */
    private   String getSign(Map<String, String> map, String rsaKey, boolean rsa2) {
        List<String> keys = new ArrayList<String>(map.keySet());
        // key排序
        Collections.sort(keys);

        StringBuilder authInfo = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            authInfo.append(buildKeyValue(key, value, false));
            authInfo.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        authInfo.append(buildKeyValue(tailKey, tailValue, false));

        String oriSign = SignUtils.sign(authInfo.toString(), rsaKey, rsa2);
        String encodedSign = "";

        try {
            encodedSign = URLEncoder.encode(oriSign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "sign=" + encodedSign;
    }


    public void setPayResult(PayResultListener payResultListener) {
        this.payResultListener = payResultListener;
    }

    public void setAuthResult(AlipayAuthResultListener authResultListener) {
        this.authResultListener = authResultListener;
    }

}
