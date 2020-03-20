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

import io.kubernetes.client.util.Yaml
import java.io.File

fun <T : Any> T.asYaml(): String {
    return Yaml.dump(this)
}

@SuppressWarnings
fun <T> File.asYamlObject(): T {
    return Yaml.load(this) as T
}