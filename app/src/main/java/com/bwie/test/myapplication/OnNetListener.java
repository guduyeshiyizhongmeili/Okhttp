package com.bwie.test.myapplication;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by peng on 2017/9/21.
 */

public interface OnNetListener {
    public void onSuccess(Response response);
    public void onSuccess(BaseBean baseBean);

    public void onError(IOException e);
}
