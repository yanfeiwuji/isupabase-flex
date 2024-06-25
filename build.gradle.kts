plugins {
    java
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.graalvm.buildtools.native") version "0.10.2"
}

group = "io.github.yanfeiwuji.isupabase"
version = "0.0.1-SNAPSHOT"

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

var mybatisFlexVersion = "1.9.2"
var hutoolVersion = "5.8.26"
var guavaVersion = "33.2.0-jre"
var justAuthVersion = "1.16.6"
var uploadVersion = "2.0.0-M2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") {

        //   exclude("org.springframework.boot","spring-boot-starter-tomcat")
    }

    implementation("org.springframework.boot:spring-boot-starter-undertow")

    modules {
        // tomcat not read form-data  name empty str
        module("org.springframework.boot:spring-boot-starter-tomcat") {
            replacedBy("org.springframework.boot:spring-boot-starter-undertow")
        }
    }
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("com.zaxxer:HikariCP")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("cn.hutool:hutool-core:$hutoolVersion")
    implementation("cn.hutool:hutool-http:$hutoolVersion")
    implementation("com.mybatis-flex:mybatis-flex-spring-boot3-starter:$mybatisFlexVersion")
    // implementation("com.google.guava:guava:$guavaVersion")

    implementation("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-mail")
// https://mvnrepository.com/artifact/me.zhyd.oauth/JustAuth
    implementation("me.zhyd.oauth:JustAuth:$justAuthVersion")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-fileupload2-jakarta-servlet6
    implementation("org.apache.commons:commons-fileupload2-jakarta-servlet6:$uploadVersion")


//    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("com.mybatis-flex:mybatis-flex-processor:$mybatisFlexVersion")
    annotationProcessor("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")

    // developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
