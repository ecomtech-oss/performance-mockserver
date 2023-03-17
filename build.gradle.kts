import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
}

extensions.getByType(JavaPluginExtension::class.java).apply {
    sourceCompatibility = VERSION_11
    targetCompatibility = VERSION_11
}

tasks.withType(Javadoc::class) {
    enabled = false
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    systemProperty("CLIENT_PORT", project.property("CLIENT_PORT")!!)
    systemProperty("JVM_METRICS_PORT", project.property("JVM_METRICS_PORT")!!)
    systemProperty("JVM_METRICS_PATH", project.property("JVM_METRICS_PATH")!!)
    systemProperty("SMTP_DELAY_SECONDS", project.property("SMTP_DELAY_SECONDS")!!)
    systemProperty("SMTP_PORT", project.property("SMTP_PORT")!!)
    systemProperty("team", project.property("team")!!)
    systemProperty("loglevel", project.property("loglevel")!!)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mock-server:mockserver-netty:5.15.0")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("org.slf4j:slf4j-reload4j:2.0.6")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.3")
    implementation("com.graphql-java:graphql-java:20.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.kirviq:dumbster:1.7.1")
}

val fatJar = task("fatJar", type = Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File"
        attributes["Main-Class"] = "org.samokat.performance.mockserver.core.MockServer"
    }
    exclude("META-INF", "META-INF/*.DSA")
    exclude("META-INF", "META-INF/*.SF")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}