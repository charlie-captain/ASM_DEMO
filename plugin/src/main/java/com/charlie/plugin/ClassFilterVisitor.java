//package com.charlie.plugin;
//
//
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
//import static com.charlie.plugin.ConfigKt.INJECT_CLASS;
//
//class ClassFilterVisitor extends ClassVisitor {
//
//    private String[] interfaces;
//    boolean visitedStaticBlock = false;
//    private String owner;
//
//    ClassFilterVisitor(ClassVisitor classVisitor) {
//        super(Opcodes.ASM5, classVisitor);
//    }
//
//    @Override
//    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//        super.visit(version, access, name, signature, superName, interfaces);
//        this.interfaces = interfaces;
//        if (interfaces != null && interfaces.length > 0) {
//            for (String i : interfaces) {
//                if (i.equals("onClick") || i.equals("android/view/View$OnClickListener")) {
//                    System.out.println(i.toString());
//                    visitedStaticBlock = true;
//                    this.owner = name;
////                    cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "doubleTap",
////                            String.format("L%s;", INJECT_CLASS),
////                            signature, null);
//
////                    cv.visitField(access,name,);
//                }
//            }
//        }
//    }
//
//    @Override
//    public MethodVisitor visitMethod(int access, String name,
//                                     String desc, String signature, String[] exceptions) {
//        if (interfaces != null && interfaces.length > 0) {
//            try {
////                if (visitedStaticBlock && name.equals("<init>")) {
////                    MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
////                    return new InitBlockVisitor(methodVisitor, owner);
////                }
////                MethodCell cell = MethodHelper.sInterfaceMethods.get(name + desc);
//
////                if (cell != null) {
//                for (String anInterface : interfaces) {
//                    System.out.println(anInterface.toString());
//                    if (anInterface.equals("android/view/View$OnClickListener")) {
//                        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
//                        return new CheckVisitor(methodVisitor, owner);
//                    }
//                }
////                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return super.visitMethod(access, name, desc, signature, exceptions);
//    }
//
//
//}
