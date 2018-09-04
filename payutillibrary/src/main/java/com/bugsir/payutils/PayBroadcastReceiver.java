package com.bugsir.payutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.mm.opensdk.modelbase.BaseResp;

/**
 *@author: BUG SIR
 *@date: 2018/9/3 22:12
 *@description: 
 */
public    class PayBroadcastReceiver extends BroadcastReceiver   {
    public static final String RESULT_CODE="code";
    public static final String RESULT_ERRSTR="errStr";
    private PayResultListener mPayListener;
    public PayBroadcastReceiver() {

    }

    public void setPayResultListener(PayResultListener listener)
    {
        this.mPayListener=listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int code = intent.getIntExtra(RESULT_CODE, -1);
            String errStr = intent.getStringExtra(RESULT_ERRSTR);

            if (code == BaseResp.ErrCode.ERR_OK) {
                if (mPayListener!=null)
                {
                    mPayListener.paySuccess();
                }

            } else {
                if (mPayListener!=null)
                {
                    mPayListener.payFailure(code+"", errStr);
                }
            }
        }
    }
}
