package com.liwg.preloader

import com.android.build.api.transform.*
import com.google.common.collect.Sets
import com.liwg.preloader.bean.InjectLoaderBean
import com.liwg.preloader.util.U
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.lang.annotation.Annotation
import java.util.regex.Pattern

class PreLoaderTransform extends Transform {
    Project project
    ClassPool mPool
    Pattern pattern = Pattern.compile("\"(.*?)\"")

    PreLoaderTransform(Project project) {    // 构造函数，我们将Project保存下来备用
        this.project = project
    }

    @Override
    String getName() {// 设置我们自定义的Transform对应的Task名称
        return "PreLoader"
    }


    @Override
    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型这样确保其他类型的文件不会传入
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }


    @Override
// 指定Transform的作用范围
    Set<QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT, QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS, QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        mPool = new ClassPool()
        mPool.appendSystemPath()
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //往类中注入代码
                inject(directoryInput.file.getAbsolutePath(), project.PreLoader.pkgSuffix, project)
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                //将 input 的目录复制到 output 指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                //往类中注入代码
                inject(jarInput.file.getAbsolutePath(), project.PreLoader.pkgSuffix, project)

                //重命名输出文件（同目录 copyFile 会冲突）
                def jarName = jarInput.name
                def md5Name = jarInput.file.hashCode()
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }

    private void inject(String path, String packageName, Project project) {
        mPool.appendClassPath(path)
        mPool.appendClassPath(project.android.bootClasspath[0].toString())
        mPool.importPackage("com.liwg.android.preloader")
        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse {
                File file ->
                    String filePath = file.absolutePath
                    if (filePath.endsWith(".class") && !filePath.contains('R$')
                            && !filePath.contains('R.class') && !filePath.contains("BuildConfig.class")) {
                        int index = filePath.indexOf(packageName)
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end).replace('\\', '.').replace('/', '.')
                        CtClass ctClass = mPool.getCtClass(className)
                        mPool.importPackage(ctClass.getPackageName())
                        if (!U.verifySubscribe(ctClass))
                            return
                        if (ctClass.isFrozen())
                            ctClass.defrost()
                        //遍历类中的所有方法
                        List<InjectLoaderBean> injectLoaderBeanList = new ArrayList<>()
                        for (CtMethod method : ctClass.getDeclaredMethods()) {
                            Annotation annotation = findSubscribeAnnotaion((Annotation[]) method.getAnnotations())
                            if (annotation != null) {
                                def matcher = pattern.matcher(annotation.toString())
                                String key = matcher.find() ? matcher.group() : PreLoaderPlugin.DEF_EXTRA_KEY
                                injectLoaderBeanList.add(Generate.generatePreLoaderListenerClass(ctClass, method, key.replaceAll("\"", ""),
                                        project.PreLoader.resultInterceptClass, path))
                            }
                        }
                        Generate.createBindActivityOrFragmentCode(ctClass, injectLoaderBeanList, path)
                        ctClass.freeze()
                    }
            }
        }
    }

    Annotation findSubscribeAnnotaion(Annotation[] annotations) {
        if (annotations == null || annotations.length == 0)
            return null
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType().getCanonicalName() == "com.liwg.android.preloader.anno.Subscribe") {
                return annotations[i]
            }
        }
        return null
    }


}