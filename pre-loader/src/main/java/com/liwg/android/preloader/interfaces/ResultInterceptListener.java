package com.liwg.android.preloader.interfaces;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

/**
 * Created by Administrator on 2019/9/22.
 * 结果拦截处理器，如果需要统一处理，需要实现这个类，并配置到gradle里面
 */
public abstract class ResultInterceptListener<T> {
    private Context context;
    public ResultInterceptListener(Object object) {
        if(object instanceof Activity){
            context = (Context) object;
        }else if(object instanceof Fragment){
            context = ((Fragment)object).getActivity();
        }else if(object instanceof android.support.v4.app.Fragment){
            context = ((android.support.v4.app.Fragment)object).getActivity();
        }
    }

    public Context getContext() {
        return context;
    }

    public abstract boolean intercept(T t);
}
