package com.bugsir.payutils.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.bugsir.payutils.PayBroadcastReceiver;
import com.bugsir.payutils.wxpay.WXPayUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
	public static int payResultCode = -999;
	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, WXPayUtil.APPID, false);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
			Intent intent = new Intent();
			intent.setAction(WXPayUtil.WXACTION);
			intent.putExtra(PayBroadcastReceiver.RESULT_CODE, resp.errCode);
			intent.putExtra(PayBroadcastReceiver.RESULT_ERRSTR, resp.errStr);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			finish();
	}
}