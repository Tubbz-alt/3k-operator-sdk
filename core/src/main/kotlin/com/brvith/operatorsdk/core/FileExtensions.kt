/*
 * Copyright © 2020 Brvith Solutions.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

fun mainResourceFile(path: String, vararg more: String?): File {
    return Paths.get("./src/main/resources/$path", *more).toFile().normalize()
}

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
