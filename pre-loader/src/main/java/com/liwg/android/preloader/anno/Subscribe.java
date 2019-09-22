package com.liwg.android.preloader.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
    /**
     * 预加载的key，不传使用默认key
     * 通过getIntent().getIntExtra()获取
     *
     */
    String preloadKey() default "PRELOADER";
}