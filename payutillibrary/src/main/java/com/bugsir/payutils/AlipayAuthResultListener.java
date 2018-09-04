package com.bugsir.payutils;

import com.bugsir.payutils.alipay.AuthResult;

/**
 * @author: BUG SIR
 * @date: 2018/9/3 21:59
 * @description: 阿里登录认证
 */
public interface AlipayAuthResultListener {
    void authSuccess(AuthResult result);
    void authFailure();
}
