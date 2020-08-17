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

import java.time.Duration
import org.jetbrains.gradle.ext.*

plugins {
    `kotlin-dsl`
    `maven-publish`
    signing
    id("de.marcphilipp.nexus-publish") version "0.4.0"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.7"
}

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = "org.caffinitas.gradle.microbench"
version = "0.1.7"
val readableName = "Creates a shell-script to run JMH"
description = "Just creates a shell-script to run JMH"

gradlePlugin {
    plugins {
        create("microbench") {
            id = "org.caffinitas.gradle.microbench"
            displayName = readableName
            implementationClass = "org.caffinitas.gradle.microbench.MicrobenchPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/snazy/gradle-microbench/"
    vcsUrl = "https://github.com/snazy/gradle-microbench/"
    description = project.description
    tags = listOf("java", "microbench", "jmh")

    plugins {
        named("microbench") {
            displayName = readableName
        }
    }
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                pom {
                    name.set(readableName)
                    description.set(project.description)
                    inceptionYear.set("2018")
                    url.set("https://github.com/snazy/gradle-microbench/")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("snazy")
                            name.set("Robert Stupp")
                            email.set("snazy@snazy.de")
                        }
                    }
                    scm {
                        connection.set("https://github.com/snazy/gradle-microbench.git")
                        developerConnection.set("https://github.com/snazy/gradle-microbench.git")
                        url.set("https://github.com/snazy/gradle-microbench/")
                    }
                }
            }
            named<MavenPublication>("microbenchPluginMarkerMaven") {
                pom {
                    name.set(readableName)
                    description.set(project.description)
                    inceptionYear.set("2018")
                    url.set("https://github.com/snazy/gradle-microbench/")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("snazy")
                            name.set("Robert Stupp")
                            email.set("snazy@snazy.de")
                        }
                    }
                    scm {
                        connection.set("https://github.com/snazy/gradle-microbench.git")
                        developerConnection.set("https://github.com/snazy/gradle-microbench.git")
                        url.set("https://github.com/snazy/gradle-microbench/")
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    afterEvaluate {
        sign(publishing.publications["pluginMaven"])
        sign(publishing.publications["microbenchPluginMarkerMaven"])
    }
}

nexusPublishing {
    packageGroup.set("org.caffinitas")
    clientTimeout.set(Duration.ofMinutes(10))
    repositories {
        sonatype()
    }
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.ALL
}

idea {
    module {
        isDownloadSources = true // this is the default BTW
        inheritOutputDirs = true
    }

    project {
        withGroovyBuilder {
            "settings" {
                val copyright: CopyrightConfiguration = getProperty("copyright") as CopyrightConfiguration
                val encodings: EncodingConfiguration = getProperty("encodings") as EncodingConfiguration
                val delegateActions: ActionDelegationConfig = getProperty("delegateActions") as ActionDelegationConfig

                delegateActions.testRunner = ActionDelegationConfig.TestRunner.CHOOSE_PER_TEST

                encodings.encoding = "UTF-8"
                encodings.properties.encoding = "UTF-8"

                copyright.useDefault = "Apache"
                copyright.profiles.create("Apache") {
                     notice = file("gradle/license.txt").readText()
                }
            }
        }
    }
}
