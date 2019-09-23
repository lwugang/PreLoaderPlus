package com.liwg.preloader.util

import javassist.CtClass;

/**
 * Created by Administrator on 2019/9/21.
 */
class U {

    static boolean verifySubscribe(CtClass ctClass) {
        return isActivity(ctClass) || isFragment(ctClass)
    }

    static boolean isFragment(CtClass ctClass) {
        CtClass superClass = ctClass.getSuperclass()
        if (superClass == null) {
            return false
        }
        CtClass fragmentClass = superClass.getClassPool().get("android.app.Fragment")
        CtClass supportFragmentClass = superClass.getClassPool().get("android.support.v4.app.Fragment")
        while (superClass != fragmentClass && superClass != supportFragmentClass) {
            if (superClass.getPackageName().startsWith("android.")) {
                return false
            }
            if (superClass.getPackageName().startsWith("java.")) {
                return false
            }
            superClass = superClass.getSuperclass()
        }
        return true
    }

    static boolean isActivity(CtClass ctClass) {
        CtClass superClass = ctClass.getSuperclass()
        if (superClass == null) {
            return false
        }
        CtClass activityClass = superClass.getClassPool().get("android.app.Activity")
        CtClass appCompatActivityClass = superClass.getClassPool().get("android.support.v7.app.AppCompatActivity")
        CtClass fragmentActivityClass = superClass.getClassPool().get("android.support.v4.app.FragmentActivity")
        while (superClass != activityClass && superClass != fragmentActivityClass && superClass != appCompatActivityClass) {
            if (superClass.getPackageName().startsWith("android.")) {
                return false
            }
            if (superClass.getPackageName().startsWith("java.")) {
                return false
            }
            superClass = superClass.getSuperclass()
        }
        return true
    }

}
