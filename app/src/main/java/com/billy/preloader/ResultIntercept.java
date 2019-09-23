package com.billy.preloader;

import com.liwg.android.preloader.interfaces.ResultInterceptListener;

import java.util.Random;

/**
 * Created by Administrator on 2019/9/22.
 */

public class ResultIntercept extends ResultInterceptListener {
    public ResultIntercept(Object object) {
        super(object);
    }

    @Override
    public boolean intercept(Object o) {
        return new Random().nextInt(10) < 5;
    }
}
