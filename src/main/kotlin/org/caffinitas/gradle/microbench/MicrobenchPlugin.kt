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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("unused")
class MicrobenchPlugin : Plugin<Project> {
    companion object {
        const val SOURCE_SET_NAME = "microbench"
    }

    override fun apply(project: Project): Unit = project.run {
        project.plugins.apply(JavaPlugin::class.java)

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)

        val ext = extensions.create("microbench", MicrobenchExtension::class, project)

        val sourceSets = javaConvention.sourceSets

        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)

        val sourceSet = sourceSets.register(SOURCE_SET_NAME) {
            java.srcDir("test/microbench")
            resources.srcDir("test/resources")
        }
        val namingScheme = ClassDirectoryBinaryNamingScheme(sourceSet.name)

        configurations.named(namingScheme.getTaskName(null, "implementation")) {
            extendsFrom(configurations.getByName(testSourceSet.implementationConfigurationName))
            dependencies.add(project.dependencies.create(mainSourceSet.output))
            dependencies.add(project.dependencies.create(testSourceSet.output))
            dependencies.add(project.dependencies.create("org.openjdk.jmh:jmh-core:${ext.jmhVersion.get()}"))
        }
        configurations.named(namingScheme.getTaskName(null, "compileOnly")) {
            dependencies.add(project.dependencies.create("org.openjdk.jmh:jmh-generator-annprocess:${ext.jmhVersion.get()}"))
        }
        configurations.named(namingScheme.getTaskName(null, "annotationProcessor")) {
            dependencies.add(project.dependencies.create("org.openjdk.jmh:jmh-generator-annprocess:${ext.jmhVersion.get()}"))
        }

        val jar = tasks.register<Jar>(namingScheme.getTaskName(null, "jar")) {
            group = "Build"
            description = "Assembles a jar archive containing the microbench classes"
            destinationDirectory.fileValue(buildDir.resolve("tools/lib"))
            archiveFileName.set("microbench.jar")
            from(sourceSet.get().output)
            finalizedBy(tasks.named("microbench"))
        }
        tasks.register("microbench", MicrobenchScriptTask::class.java, jar).configure {
            dependsOn(jar)
            inputs.property("jmh-version", ext.jmhVersion)
            inputs.property("jvm-options", ext.jvmOptions)
            inputs.file(jar.get().archiveFile)
            inputs.files(configurations.named(mainSourceSet.runtimeClasspathConfigurationName))
        }
    }
}
