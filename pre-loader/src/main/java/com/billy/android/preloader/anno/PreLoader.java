package com.billy.android.preloader.anno;

public @interface PreLoader {
    /**
     * 预加载的key，不传使用默认key
     * 通过getIntent().getIntExtra()获取
     *
     * @return
     */
    String preloadKey() default "";
}