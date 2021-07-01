//package com.charlie.plugin;
//
//
//
//import org.objectweb.asm.Label;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
//import static com.charlie.plugin.ConfigKt.INJECT_CLASS;
//import static com.charlie.plugin.ConfigKt.INJECT_METHOD;
//
//
//public class CheckVisitor extends MethodVisitor {
//    private String owner;
//
//    CheckVisitor(MethodVisitor mv, String owner) {
//        super(Opcodes.ASM5, mv);
//        this.owner = owner;
//    }
//
//    @Override
//    public void visitCode() {
//        mv.visitVarInsn(Opcodes.ALOAD, 0);
//        mv.visitFieldInsn(Opcodes.GETFIELD, owner, "doubleTap",
//                String.format("L%s;", INJECT_CLASS));
//        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, INJECT_CLASS,
//            INJECT_METHOD, "()Z", false);
//        Label label = new Label();
//        mv.visitJumpInsn(Opcodes.IFNE, label);
//        mv.visitInsn(Opcodes.RETURN);
//        mv.visitLabel(label);
//        super.visitCode();
//    }
//}
