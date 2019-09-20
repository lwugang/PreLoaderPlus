package com.liwg.preloader.plugin

import com.android.build.gradle.BaseExtension
import com.liwg.preloader.PreLoaderTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class PreLoaderPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        //注册一个Transform
        def transform = new PreLoaderTransform(project)
        project.android.registerTransform(transform)
    }
}