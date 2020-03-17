package com.brvith.operatorsdk.core

import io.kubernetes.client.openapi.models.V1ServiceAccount
import io.kubernetes.client.util.Yaml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

fun <T : Any> logger(clazz: T) = LoggerFactory.getLogger(clazz.javaClass)!!

fun <T : KClass<*>> logger(clazz: T) = LoggerFactory.getLogger(clazz.java)!!

fun testResourceFile(path: String, vararg more: String?): File {
    return Paths.get("./src/test/resources/$path", *more).toFile().normalize()
}

fun buildResourceFile(path: String, vararg more: String?): File {
    return Paths.get("./build/$path", *more).toFile().normalize()
}

fun nFile(path: String, vararg more: String?): File {
    return Paths.get(path, *more).toFile().normalize()
}

fun nPath(path: String, vararg more: String?): Path {
    return Paths.get(path, *more).normalize().toAbsolutePath()
}

fun nPathName(path: String, vararg more: String?): String {
    return nPath(path, *more).toString()
}

suspend fun File.reCreateDirs(): File {
    if (this.exists()) {
        this.deleteRecursively()
    }
    this.mkdirs()
    check(this.exists()) {
        throw Exception("failed to re create dir(${this.absolutePath})")
    }
    return this
}

suspend fun writeBytes2File(file: File, bytes: ByteArray) = withContext(Dispatchers.IO) {
    Files.write(file.toPath(), bytes)
}

suspend fun deleteDir(path: String, vararg more: String?): Boolean = withContext(Dispatchers.IO) {
    nFile(path, *more).deleteRecursively()
}

suspend fun <T> File.asKubernetesResource(): T {
    return Yaml.load(this) as T
}
