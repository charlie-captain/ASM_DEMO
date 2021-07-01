package com.charlie.plugin

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * created by charlie on 2021/7/1
 */
object TransformHelper {

    fun transformJar(jarInput: JarInput, outputProvider: TransformOutputProvider, isIncremental: Boolean) {
        System.out.println("${jarInput.file.name}")
        val destDir = outputProvider.getContentLocation(
            jarInput.name,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        FileUtils.copyFile(jarInput.file, destDir)
    }


    fun transformDirectory(directoryInput: DirectoryInput, outputProvider: TransformOutputProvider, isIncremental: Boolean) {
        val sourceFile = directoryInput.file
        val name = sourceFile.name
        val destDir = outputProvider.getContentLocation(name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
        System.out.println("TransformHelper[transformDirectory], name = $name, sourceFile Path = ${sourceFile.absolutePath}, destFile Path = ${destDir.absolutePath}, isIncremental = $isIncremental")
        if (isIncremental) {
            val changeFiles = directoryInput.changedFiles
            for (changeFile in changeFiles) {
                val status = changeFile.value
                val inputFile = changeFile.key
                val destPath = inputFile.absolutePath.replace(sourceFile.absolutePath, destDir.absolutePath)
                val destFile = File(destPath)
                System.out.println("目录：$destPath，状态：$status")
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
            FileUtils.copyDirectory(sourceFile, destDir)
            handleDirectory(sourceFile, destDir)
        }
    }


    private fun handleDirectory(sourceFile: File, destDir: File) {
        val files = sourceFile.listFiles { file, name ->
            val realFile = File(file, name)
            println("asdqwe filter $file,  $name ,$realFile isEnd =${name.endsWith(".class")}, ${realFile.isDirectory}  ${realFile.isFile}")
            if (realFile.isDirectory && !realFile.isFile) {
                true
            } else {
                name.endsWith(".class")
            }
        }

        System.out.println("handleDirectory files = ${files.map { it.name }}, destDir = ${destDir.name}")
        for (file in files) {
            try {
                val destFile = File(destDir, file.name)
                System.out.println("modifile class ${file.name}  dest ${destFile.name} ${file.isDirectory}")
                if (file.isDirectory) {
                    handleDirectory(file, destFile)
                } else if (file.name.endsWith(".class")) {
                    val fileInputStream = FileInputStream(file)
                    val sourceBytes = readBytes(fileInputStream)
                    var modifyBytes: ByteArray? = null
                    if (!file.name.contains("BuildConfig")) {
                        modifyBytes = sourceBytes?.let {
                            modifyClass(file, it)
                        }
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

    private fun handleJarFile(jarInput: JarInput, destFile: File) {
        // 空的 jar 包不进行处理
        if (jarInput.file == null || jarInput.file.length() == 0L) {
            System.out.println("handleJarFile, ${jarInput.file.absolutePath} is null")
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
                modifyClassBytes = destClassBytes?.let { modifyClass(jarInput.file, it) }
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

    private fun modifyClass(file: File, sourceBytes: ByteArray): ByteArray? {
        println("asdqwe ${file.absolutePath}  ${file.name}")
        try {
            val classReader = ClassReader(sourceBytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val classVisitor = PluginVisitor(Opcodes.ASM7, classWriter)
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG)
            return classWriter.toByteArray()
        } catch (exception: Exception) {
            System.out.println("modify class exception = ${exception.printStackTrace()}")
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

    fun getFileName(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1)
    }

}