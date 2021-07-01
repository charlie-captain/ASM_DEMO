package com.charlie.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ddmlib.Log
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


//        if (transformInvocation != null) {
//            transformInvocation.outputProvider.deleteAll()
//            for (transformInput in transformInvocation.inputs) {
//                for (jarInput in transformInput.jarInputs) {
////                    TransformHelper.transformJars(jarInput, transformInvocation.outputProvider, transformInvocation.isIncremental)
//
//                    val outputProvider = transformInvocation.outputProvider
//                    val jarName = jarInput.name
//                    val status = jarInput.status
//                    val destFile = outputProvider.getContentLocation(jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                    Log.d("asdqwe", "TransformHelper[transformJars], jar = $jarName, status = $status, isIncremental = $isIncremental")
//                    if (isIncremental) {
//                        when (status) {
//                            Status.ADDED -> {
//                                handleJarFile(jarInput, destFile)
//                            }
//                            Status.CHANGED -> {
//                                handleJarFile(jarInput, destFile)
//                            }
//                            Status.REMOVED -> {
//                                if (destFile.exists()) {
//                                    destFile.delete()
//                                }
//                            }
//                            Status.NOTCHANGED -> {
//
//                            }
//                            else -> {
//                            }
//                        }
//                    } else {
//                        handleJarFile(jarInput, destFile)
//                    }
//                }
//                for (directoryInput in transformInput.directoryInputs) {
//                    transformDirectory(directoryInput, transformInvocation.outputProvider, transformInvocation.isIncremental)
//                }
//            }
//        }
    }

    private fun handleJarFile(jarInput: JarInput, destFile: File) {
        // 空的 jar 包不进行处理
        if (jarInput.file == null || jarInput.file.length() == 0L) {
            Log.d("asdqwe", "handleJarFile, ${jarInput.file.absolutePath} is null")
            return
        }
        // 构建 JarFile 文件
        val modifyJar = JarFile(jarInput.file, false)
        // 创建目标文件流
        val jarOutputStream = JarOutputStream(FileOutputStream(destFile))
        val enumerations = modifyJar.entries()
        // 遍历 Jar 文件进行处理
        for (jarEntry in enumerations) {
            val inputStream = modifyJar.getInputStream(jarEntry)
            val entryName = jarEntry.name
            if (entryName.startsWith(".DSA") || entryName.endsWith(".SF")) {
                return
            }
            val tempEntry = JarEntry(entryName)
            jarOutputStream.putNextEntry(tempEntry)
            var modifyClassBytes: ByteArray? = null
            val destClassBytes = readBytes(inputStream)
            if (!jarEntry.isDirectory && entryName.endsWith(".class") && !entryName.startsWith("android")) {
                modifyClassBytes = destClassBytes?.let { modifyClass(it) }
            }

            if (modifyClassBytes != null) {
                jarOutputStream.write(modifyClassBytes)
            } else {
                jarOutputStream.write(destClassBytes)
            }
            jarOutputStream.flush()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        modifyJar.close()
    }


    fun transformDirectory(directoryInput: DirectoryInput, outputProvider: TransformOutputProvider, isIncremental: Boolean) {
        val sourceFile = directoryInput.file
        val name = sourceFile.name
        val destDir = outputProvider.getContentLocation(name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
//    ADLog.info("TransformHelper[transformDirectory], name = $name, sourceFile Path = ${sourceFile.absolutePath}, destFile Path = ${destDir.absolutePath}, isIncremental = $isIncremental")
        if (isIncremental) {
            val changeFiles = directoryInput.changedFiles
            for (changeFile in changeFiles) {
                val status = changeFile.value
                val inputFile = changeFile.key
                val destPath = inputFile.absolutePath.replace(sourceFile.absolutePath, destDir.absolutePath)
                val destFile = File(destPath)
//            ADLog.info("目录：$destPath，状态：$status")
                when (status) {
                    Status.NOTCHANGED -> {

                    }
                    Status.REMOVED -> {
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                    }
                    Status.CHANGED, Status.ADDED -> {
                        handleDirectory(inputFile, destFile)
                    }
                    else -> {
                    }
                }
            }
        } else {
            // 首先全部拷贝，防止有后续处理异常导致文件的丢失
            com.android.utils.FileUtils.copyDirectory(sourceFile, destDir)
            handleDirectory(sourceFile, destDir)
        }
    }


    private fun handleDirectory(sourceFile: File, destDir: File) {
        val files = sourceFile.listFiles { file, name ->
            if (file != null && file.isDirectory) {
                true
            } else {
                name.endsWith(".class")
            }
        }

        for (file in files) {
            try {
                val destFile = File(destDir, file.name)
                if (file.isDirectory) {
                    handleDirectory(file, destFile)
                } else {
                    val fileInputStream = FileInputStream(file)
                    val sourceBytes = readBytes(fileInputStream)
                    var modifyBytes: ByteArray? = null
                    if (!file.name.contains("BuildConfig")) {
                        modifyBytes = sourceBytes?.let { modifyClass(it) }
                    }
                    if (modifyBytes != null) {
                        val destPath = destFile.absolutePath
                        destFile.delete()
                        byte2File(destPath, modifyBytes)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    private fun modifyClass(sourceBytes: ByteArray): ByteArray? {
        try {
            val classReader = ClassReader(sourceBytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val classVisitor = AndExtensionInterceptor(Opcodes.ASM7, classWriter)
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG)
            return classWriter.toByteArray()
        } catch (exception: Exception) {
            Log.d("asdqwe", "modify class exception = ${exception.printStackTrace()}")
        }
        return null
    }


    fun readBytes(inputStream: InputStream): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).apply { len = this } != -1) {
                byteArrayOutputStream.write(buffer, 0, len)
            }
            byteArrayOutputStream.flush()
            return byteArrayOutputStream.toByteArray()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            byteArrayOutputStream.close()
            inputStream.close()
        }
        return null
    }

    fun byte2File(outputPath: String, sourceByte: ByteArray) {
        val file = File(outputPath)
        if (file.exists()) {
            file.delete()
        }

        val inputStream = ByteArrayInputStream(sourceByte)
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).apply { len = this } != -1) {
            outputStream.write(buffer, 0, len)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
}

open class BaseClassInterceptor(api: Int, classVisitor: ClassVisitor?) : ClassVisitor(api, classVisitor) {
    protected var className: String? = ""
    private var signature: String? = ""
    private var superName: String? = ""

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        Log.d("asdqwe", "开始访问【类】，name = $name, superName = $superName, version = $version")
        this.className = name
        this.signature = signature
        this.superName = superName
    }

    override fun visitEnd() {
        super.visitEnd()
        Log.d("asdqwe", "结束访问类")
    }
}


class AndExtensionInterceptor(api: Int, classVisitor: ClassVisitor?) : BaseClassInterceptor(api, classVisitor) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
//        ADLog.info("开始访问方法： name = $name, access = ${AccessCodeUtils.accessCode2String(access)}, descriptor = $descriptor")
        var methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
//        if (andExt!!.printLog) {
//            ADLog.error("PrintLogInterceptor")
        methodVisitor = PrintLogInterceptor(className, methodVisitor, access, name, descriptor)
//        }
//        if (andExt!!.deleteLog) {
//            ADLog.error("DeleteLogInterceptor")
//            methodVisitor = DeleteLogInterceptor(methodVisitor, access, name, descriptor)
//        }
//
//        if (andExt!!.tryCatch) {
//            ADLog.error("TryCatchInterceptor")
//            methodVisitor = TryCatchInterceptor(methodVisitor, access, name, descriptor)
//        }
        return methodVisitor
    }
}


/**
 * 增加所有调用方法的名称日志输出的地方
 */
class PrintLogInterceptor(
    var className: String?, methodVisitor: MethodVisitor,
    access: Int,
    name: String?,
    descriptor: String?
) : AdviceAdapter(org.objectweb.asm.Opcodes.ASM7, methodVisitor, access, name, descriptor) {

    override fun onMethodEnter() {
        super.onMethodEnter()
        // 将当前类名添加到操作栈，作为 TAG
        mv.visitLdcInsn(getFileName(className!!))
        // 将当前方法名添加到操作栈，进行输出
        mv.visitLdcInsn(name)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false)
    }
}

fun getFileName(path: String): String {
    return path.substring(path.lastIndexOf("/") + 1)
}

