package com.charlie.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class PluginVisitor(api: Int, classVisitor: ClassVisitor?) : BaseClassVisitor(api, classVisitor) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        var methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        methodVisitor = MeasureMethodCostTimeInterceptor(className, methodVisitor, access, name, descriptor)
        return methodVisitor
    }
}
