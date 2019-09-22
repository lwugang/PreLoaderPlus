package com.liwg.preloader.bean
/**
 * Created by Administrator on 2019/9/21.
 */
class InjectLoaderBean {
    String key
    String className

    InjectLoaderBean(String key, String className) {
        this.key = key
        this.className = className
    }
}
