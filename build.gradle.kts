import groovy.namespace.QName

plugins {
    java
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    // id("org.graalvm.buildtools.native") version "0.10.2"
}

group = "io.github.yanfeiwuji"

version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/repositories/snapshots")

}

var mybatisFlexVersion = "1.9.3"
var hutoolVersion = "5.8.26"
var guavaVersion = "33.2.0-jre"
var justAuthVersion = "1.16.6"
var uploadVersion = "2.0.0-M2"

dependencies {

    api("org.springframework.boot:spring-boot-starter-web"){
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-undertow")
    modules {
        // tomcat not read form-data  name empty str
        module("org.springframework.boot:spring-boot-starter-tomcat") {
            replacedBy("org.springframework.boot:spring-boot-starter-undertow")
        }
    }
    api("org.springframework.boot:spring-boot-starter-aop")

    api("com.zaxxer:HikariCP")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("cn.hutool:hutool-core:$hutoolVersion")
    api("cn.hutool:hutool-http:$hutoolVersion")
    api("com.mybatis-flex:mybatis-flex-spring-boot3-starter:$mybatisFlexVersion")
    // implementation("com.google.guava:guava:$guavaVersion")

    api("org.projectlombok:lombok")
    api("org.springframework.boot:spring-boot-starter-validation")

    api("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    api("org.springframework.boot:spring-boot-starter-mail")
    api("me.zhyd.oauth:JustAuth:$justAuthVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("com.mybatis-flex:mybatis-flex-processor:$mybatisFlexVersion")
    annotationProcessor("org.projectlombok:lombok")
    // runtimeOnly("com.mysql:mysql-connector-j")

    // developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}

publishing {

    publications {
        create<MavenPublication>("mavenJava") {
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }

            }
            from(components["java"])
            groupId = "io.github.yanfeiwuji"
            artifactId = "isupabase-flex"
            version = "0.0.1"

        }

    }
    repositories {
        mavenLocal()
    }
}


