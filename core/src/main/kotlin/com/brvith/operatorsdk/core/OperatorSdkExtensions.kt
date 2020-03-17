package com.brvith.operatorsdk.core

import io.kubernetes.client.util.Yaml
import java.io.File

fun <T : Any> T.asYaml(): String {
    return Yaml.dump(this)
}

@SuppressWarnings
fun <T> File.asYamlObject(): T {
    return Yaml.load(this) as T
}