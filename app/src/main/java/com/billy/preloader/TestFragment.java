package com.billy.preloader;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.liwg.android.preloader.PreLoader;
import com.liwg.android.preloader.anno.Subscribe;

/**
 * Created by Administrator on 2019/9/22.
 */

public class TestFragment extends Fragment {
    @Subscribe
    public void test(String s){
        Log.e("---------", "test: " );

    }
}
