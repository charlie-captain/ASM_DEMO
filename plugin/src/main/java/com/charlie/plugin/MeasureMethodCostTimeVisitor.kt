package com.charlie.plugin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 方法耗时
 */
class MeasureMethodCostTimeVisitor(
    var className: String?, methodVisitor:MethodVisitor,
    access: Int,
    name: String?,
    descriptor: String?
) : AdviceAdapter(org.objectweb.asm.Opcodes.ASM7, methodVisitor, access, name, descriptor) {

    var startId = 0

    override fun onMethodEnter() {
        super.onMethodEnter()
        if (name == "<init>") {
            //跳过构造
            return
        }
        println("onMethod Enter: $className, name = $name")
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        startId = newLocal(Type.LONG_TYPE)
        mv.visitVarInsn(LSTORE, startId)

    }


    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (name == "<init>") {
            return
        }
        println("onMethod Exit: $className, name = $name")
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        mv.visitVarInsn(LLOAD, startId)
        mv.visitInsn(LSUB)
        val endId = newLocal(Type.LONG_TYPE)
        mv.visitVarInsn(LSTORE, endId)
        mv.visitLdcInsn(className)
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        mv.visitLdcInsn(name + ": cost = ")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        mv.visitVarInsn(LLOAD, endId)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
        mv.visitIntInsn(BIPUSH, 32)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        mv.visitInsn(POP)

    }
}
