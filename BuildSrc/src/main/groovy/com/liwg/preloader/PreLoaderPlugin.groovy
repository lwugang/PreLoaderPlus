package com.liwg.preloader

import org.gradle.api.Plugin
import org.gradle.api.Project

class PreLoaderPlugin implements Plugin<Project> {
    static String DEF_EXTRA_KEY = "PRELOADER"

    @Override
    void apply(Project project) {
        //注册一个Transform
        project.extensions.create("PreLoader", PreLoaderExtension)
        project.configurations.all { configuration ->
            if (name != "implementation" && name != "compile") {
                return
            }
//            configuration.dependencies.add(project.dependencies.create("com.android.support.test.uiautomator:uiautomator-v18:2.1.2"))
        }
        def transform = new PreLoaderTransform(project)
        project.android.registerTransform(transform)
    }
}

class PreLoaderExtension {
    /**
     * 需要处理的包名前缀，默认是com
     */
    String pkgSuffix = 'com'
    /**
     * 预加载结果拦截处理器，主要针对网络返回结果处理
     */
    String resultInterceptClass = ""
}