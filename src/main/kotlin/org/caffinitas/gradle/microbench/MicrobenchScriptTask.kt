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

import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.*
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import javax.inject.Inject

open class MicrobenchScriptTask @Inject constructor(private val jar: TaskProvider<Jar>) : DefaultTask() {

    @OutputFile
    val scriptFile = project.objects.fileProperty().convention(project.layout.buildDirectory.file("microbench"))

    init {
        group = "build"
        description = "Generate the shell wrapper to run microbenchmarks"
    }

    @TaskAction
    fun generateScript() {
        val srcSet = project.extensions.getByType<SourceSetContainer>().getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        val ext = project.extensions.getByType<MicrobenchExtension>()

        val classpathFiles = (listOf(project.projectDir.resolve("test/conf")) +
                project.configurations.named(srcSet.runtimeClasspathConfigurationName).get().files).map { f ->
            if (f.startsWith(project.projectDir))
                "${'$'}{BASE}/${f.relativeTo(project.projectDir)}"
            else
                f.absolutePath
        }
        val script = """#!/bin/bash
#
# GENERATED FILE
#
# JMH wrapper shell script for ${project.path} for Java ${JavaVersion.current().majorVersion}
#
# DO NOT EDIT, ALL CHANGES IN THE ORIGINAL LOCATION WILL BE OVERWRITTEN (or copy it to a safe place)
#

BASE="${project.projectDir}"

CLASSPATH="${jar.get().archiveFile.get().asFile}"
${classpathFiles.joinToString("\n") { f -> """CLASSPATH="${'$'}{CLASSPATH}:$f"""" }}

java -cp ${"$"}{CLASSPATH} \
    ${if (JavaVersion.current().isJava11Compatible) ext.jvmOptions.get().joinToString(" \\\n    ") else ""} \
    ${'$'}{JVM_ARGS:"-Xms2g -Xmx2g"} \
    org.openjdk.jmh.Main \
    "$@"
"""

        val outFile = scriptFile.get().asFile

        outFile.writeText(script)
        outFile.setExecutable(true)
        logger.lifecycle("Wrote executable {}", outFile)
    }
}
