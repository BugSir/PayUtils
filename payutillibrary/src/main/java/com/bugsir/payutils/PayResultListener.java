package com.bugsir.payutils;

/**
 * @author: BUG SIR
 * @date: 2018/9/3 21:53
 * @description: 支付成功或失败回调接口
 */
public interface PayResultListener {
    void paySuccess();

    void payFailure(String errorCode, String errStr);
}
