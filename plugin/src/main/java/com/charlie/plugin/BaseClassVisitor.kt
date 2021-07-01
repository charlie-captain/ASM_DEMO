package com.charlie.plugin

import org.objectweb.asm.ClassVisitor

open class BaseClassVisitor(api: Int, classVisitor: ClassVisitor?) : ClassVisitor(api, classVisitor) {
    protected var className: String? = ""
    private var signature: String? = ""
    private var superName: String? = ""

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.signature = signature
        this.superName = superName
    }

    override fun visitEnd() {
        super.visitEnd()
    }
}


