package com.charlie.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ddmlib.Log
import com.android.utils.FileUtils
import groovy.io.FileType
import org.gradle.configurationcache.serialization.codecs.transform.transformDependencies
import org.gradle.internal.impldep.bsh.commands.dir
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * created by charlie on 2021/6/30
 */
class DemoTransform : Transform() {

    override fun getName(): String {
        return "DemoTransform"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        if (transformInvocation != null) {
            if (!isIncremental) {
                //全量删掉所有
                transformInvocation.outputProvider.deleteAll()
            }
            //处理输入的文件夹和jars
            transformInvocation.inputs.forEach { transformInput ->
                System.out.println("handle directory ${transformInput.directoryInputs.size}")
                transformInput.directoryInputs.forEach {
                    TransformHelper.transformDirectory(it, transformInvocation.outputProvider, transformInvocation.isIncremental)
                }
                System.out.println("handle jars ${transformInput.jarInputs.size}")
                //jar暂时不处理，只是复制文件
                transformInput.jarInputs.forEach {
                    TransformHelper.transformJar(it, transformInvocation.outputProvider, transformInvocation.isIncremental)
                }

            }
        }

    }


}

