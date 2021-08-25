package com.charlie.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * created by charlie on 2021/7/15
 */
class DeleteLogVisitor(
    var className: String?,
    methodVisitor: MethodVisitor,
    access: Int,
    name: String?,
    descriptor: String?
) : AdviceAdapter(ASM7, methodVisitor, access, name, descriptor) {


    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
//        println(" $opcode, $owner, $name, $descriptor, $isInterface")
        if (Opcodes.ACC_STATIC.and(opcode) != 0 && owner == "android/util/Log" && (name == "d" || name == "i" || name == "e" || name == "w" || name == "v")
            && descriptor == "(Ljava/lang/String;Ljava/lang/String;)I"
        ) {
            /**
             * 直接 return 只是删除了 Log 指令的调用，但是对应的当前的操作数栈没有进行处理
             */
            return
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }
}