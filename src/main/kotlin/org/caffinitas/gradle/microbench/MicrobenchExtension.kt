/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.caffinitas.gradle.microbench

import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class MicrobenchExtension @Inject constructor(project: Project) {
    @Input
    val jmhVersion = project.objects.property(String::class).convention("1.21")

    @Input
    val jvmOptions = project.objects.listProperty(String::class).convention(listOf())
}
