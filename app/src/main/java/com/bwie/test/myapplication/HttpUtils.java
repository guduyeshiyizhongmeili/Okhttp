package com.bwie.test.myapplication;


import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by peng on 2017/9/21.
 */

public class HttpUtils {
    private static ReentrantLock lock = new ReentrantLock();
    private static volatile HttpUtils httpUtils;
    private final OkHttpClient okHttpClient;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static HashMap<String,String> headers = new HashMap<>();
    static{
        headers.put("client_type","android");
    }
    private HttpUtils() {
        okHttpClient = new OkHttpClient();
    }

    public static HttpUtils getInstance() {
        if (httpUtils == null) {
            lock.lock();
            try {
                if (httpUtils == null) {
                    httpUtils = new HttpUtils();
                }
            } finally {
                lock.unlock();
            }
        }
        return httpUtils;
    }

    public void doPost(String url, HashMap<String, String> params, final Class clazz, final OnNetListener onNetListener) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onNetListener!=null){
                            onNetListener.onError(e);
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (onNetListener!=null){
//                            onNetListener.onSuccess(response);
                            try {
                             BaseBean baseBena  = (BaseBean) new Gson().fromJson(response.body().string(), clazz);
                                if (baseBena.getCode().equals("200")){
                                    onNetListener.onSuccess(baseBena);
                                }else if(baseBena.getCode().equals("300")){
                                    //做相应的操作
                                }
                            } catch (IOException e) {

                            }
                        }
                    }
                });

            }
        });
    }

    /**
     * GET 请求
     *
     * @param url
     * @param headerMap
     * @param onNetListener
     */
    public void doGet(String url, HashMap<String, String> headerMap, final OnNetListener onNetListener) {
        if (headerMap == null || headerMap.size() == 0) {
            throw new RuntimeException("headerMap is null !!!!");
        }
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        headers.putAll(headerMap);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        Request request = builder.build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onNetListener.onError(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                //默认返回结果在子线程当中
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onNetListener.onSuccess(response);
                    }
                });
            }
        });
    }
}
