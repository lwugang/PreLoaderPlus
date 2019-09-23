package com.liwg.preloader

import com.liwg.preloader.bean.InjectLoaderBean
import com.liwg.preloader.util.U
import javassist.*

/**
 * Created by Administrator on 2019/9/21.
 */
class Generate {

    /**
     * 生成DataListener实现类
     */
    static InjectLoaderBean generatePreLoaderListenerClass(CtClass ctClass, CtMethod injectMethod, String key,String interceptClass, String path) {
        def pool = ctClass.getClassPool()
        def dataListenerClass = pool.get("com.liwg.android.preloader.interfaces.DataListener")
        def listenerImplClass
        def defClassName = ctClass.getPackageName() + "." + ctClass.getSimpleName() + "_PreLoader_" + key
        def onDataArrivedMethod
        try {
            listenerImplClass = pool.get(defClassName)
            if (listenerImplClass.isFrozen()) {
                listenerImplClass.defrost()
            }
            //如果存在对应的类，替换代码
            onDataArrivedMethod = listenerImplClass.getDeclaredMethod("onDataArrived")
            onDataArrivedMethod.setBody("if(\$1!=null){this.proxy." + injectMethod.name + "((" + injectMethod.getParameterTypes()[0].name + ")\$1);}else{this.proxy." +
                    injectMethod.name + "(null);}")
        } catch (NotFoundException e) {
            listenerImplClass = pool.makeClass(defClassName)
            def interfaces = [dataListenerClass] as CtClass[]
            listenerImplClass.setInterfaces(interfaces)
            //添加字段
            listenerImplClass.addField(new CtField(ctClass, "proxy", listenerImplClass))
            //添加构造方法
            def constructor = new CtConstructor([ctClass] as CtClass[], listenerImplClass)
            constructor.setBody("{this.proxy=\$1;}")
            listenerImplClass.addConstructor(constructor)
            StringBuilder code = new StringBuilder("public void onDataArrived(Object obj){")
            if (interceptClass != null && interceptClass.length() > 0) {
                code.append(String.format("if(new %s(this.proxy).intercept(obj)){return;}", interceptClass))
            }
            code.append("if(obj!=null){")
                code.append("this.proxy." + injectMethod.name + "((" + injectMethod.getParameterTypes()[0].name + ")obj);")
            code.append("}else{")
                code.append("this.proxy." + injectMethod.name + "(null);")
            code.append("}")
            code.append("}")
            def onDataArrived = CtMethod.make(code.toString(), listenerImplClass)
            listenerImplClass.addMethod(onDataArrived)
        }
        listenerImplClass.writeFile(path)
        listenerImplClass.freeze()
        return new InjectLoaderBean(key, listenerImplClass.name)
    }

    /**
     * 创建绑定生命周期的调用代码
     * @param ctClass
     * @param injectLoaderList
     * @param path
     */
    static void createBindActivityOrFragmentCode(CtClass ctClass, List<InjectLoaderBean> injectLoaderList, String path) {
        if (injectLoaderList.isEmpty())
            return
        boolean isActivity = U.isActivity(ctClass)
        CtMethod createMethod = createCreateMethod(ctClass, isActivity)
        CtMethod destroyMethod = createDestroyMethod(ctClass)

        StringBuilder createCodeBody = new StringBuilder()
        StringBuilder destroyCodeBody = new StringBuilder()
        for (int i = 0; i < injectLoaderList.size(); i++) {
            def loaderBean = injectLoaderList.get(i)
            if (isActivity) {
                createCodeBody.append(String.format("PreLoader.listenData(getIntent().getIntExtra(\"%s\",0),new %s(this));", loaderBean.key, loaderBean.className))
                destroyCodeBody.append(String.format(" PreLoader.destroy(getIntent().getIntExtra(\"%s\",0));", loaderBean.key))
            } else {
                createCodeBody.append(String.format("PreLoader.listenData(getArguments().getInt(\"%s\",0),new %s(this));", loaderBean.key, loaderBean.className))
                destroyCodeBody.append(String.format(" PreLoader.destroy(getArguments().getInt(\"%s\",0));", loaderBean.key))
            }
        }
        if (ctClass.isFrozen())
            ctClass.defrost()
        createMethod.insertAfter(createCodeBody.toString())
        destroyMethod.insertBefore(destroyCodeBody.toString())
        ctClass.writeFile(path)
    }

    private static CtMethod findMethod(CtClass ctClass, String methodName) {
        def methods = ctClass.getDeclaredMethods()
        for (int i = 0; i < methods.length; i++) {
            if (methodName == methods[i].name) {
                return methods[i]
            }
        }
        return null
    }

    /**
     * 创建Activity 的onCreate方法或者Fragment的onViewCreated方法
     * @param ctClass
     * @param isActivity
     * @return
     */
    private static CtMethod createCreateMethod(CtClass ctClass, boolean isActivity) {
        def method
        if (isActivity) {
            method = findMethod(ctClass, "onCreate")
        } else {
            method = findMethod(ctClass, "onViewCreated")
        }
        if (method == null) {
            def params
            if (isActivity) {
                params = [ctClass.getClassPool().get("android.os.Bundle")] as CtClass[]
            } else {
                params = [ctClass.getClassPool().get("android.view.View"), ctClass.getClassPool().get("android.os.Bundle")] as CtClass[]
            }
            CtMethod ctMethod = new CtMethod(CtClass.voidType, isActivity ? "onCreate" : "onViewCreated", params, ctClass)
            ctMethod.setModifiers(javassist.Modifier.PUBLIC)
            if (isActivity) {
                ctMethod.setBody("{super.onCreate(\$1);}")
            } else {
                ctMethod.setBody("{super.onViewCreated(\$1,\$2);}")
            }
            ctClass.addMethod(ctMethod)
            return ctMethod
        }
        return method
    }

    /**
     * 创建activity、fragment的onDestroy方法
     * @param ctClass
     * @return
     */
    private static CtMethod createDestroyMethod(CtClass ctClass) {
        def method = findMethod(ctClass, "onDestroy")
        if (method == null) {
            CtMethod ctMethod = new CtMethod(CtClass.voidType, "onDestroy", null, ctClass)
            ctMethod.setModifiers(javassist.Modifier.PUBLIC)
            ctMethod.setBody("{super.onDestroy();}")
            ctClass.addMethod(ctMethod)
            return ctMethod
        }
        return method
    }
}
