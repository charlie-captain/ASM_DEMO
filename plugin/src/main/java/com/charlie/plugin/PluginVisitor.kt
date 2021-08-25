package com.charlie.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class PluginVisitor(api: Int, classVisitor: ClassVisitor?) : ClassVisitor(api, classVisitor) {

    var className: String? = null

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (className != null && !Utils.ignorePackageNames(className!!)) {
            methodVisitor = DeleteLogVisitor(className, methodVisitor, access, name, descriptor)

            methodVisitor = MeasureMethodCostTimeVisitor(className, methodVisitor, access, name, descriptor)
        }
        return methodVisitor
    }
}
