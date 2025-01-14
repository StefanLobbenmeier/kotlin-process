import de.fayard.refreshVersions.core.versionFor

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

val myGroup = "com.github.pgreze".also { group = it }
val myArtifactId = "kotlin-process"
val myVersion = "1.5.1"
val myDescription = "Kotlin friendly way to run an external process".also { description = it }
val githubUrl = "https://github.com/pgreze/$myArtifactId"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.7"
}
tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(System.getenv("CI") != "true")
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set(versionFor("version.com.pinterest.ktlint..ktlint-cli"))
}

dependencies {
    setOf(
        kotlin("stdlib-jdk8"),
        KotlinX.coroutines.core,
    ).forEach { dependency ->
        compileOnly(dependency)
        testImplementation(dependency)
    }

    // To trigger refreshVersions updates
    "ktlint"("com.pinterest.ktlint:ktlint-cli:_")

    testImplementation("org.amshove.kluent:kluent:_")
    testImplementation(platform(Testing.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

//
// Publishing
//

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = myGroup
            artifactId = myArtifactId
            version = myVersion

            from(components["java"])

            pom {
                name.set(myArtifactId)
                description.set(myDescription)
                url.set(githubUrl)
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pgreze")
                        name.set("Pierrick Greze")
                    }
                    developer {
                        id.set("StefanLobbenmeier")
                        name.set("Stefan Lobbenmeier")
                    }
                }
                scm {
                    connection.set("$githubUrl.git")
                    developerConnection.set("scm:git:ssh://github.com:StefanLobbenmeier/$myArtifactId.git")
                    url.set(githubUrl)
                }
            }
        }
    }

    repositories {
        maven {
            val repository = "StefanLobbenmeier/kotlin-process"

            name = "StefanLobbenmeier"
            url = uri("https://maven.pkg.github.com/$repository")

            credentials {
                username = "StefanLobbenmeier"
                password = System.getenv("GITHUB_TOKEN")
            }
        }

    }
}
