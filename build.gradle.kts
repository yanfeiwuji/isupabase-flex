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
   mavenLocal()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

var mybatisFlexVersion = "1.9.1"
var hutoolVersion = "5.8.26"
var guavaVersion = "33.2.0-jre"


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("com.zaxxer:HikariCP")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("cn.hutool:hutool-core:$hutoolVersion")
    implementation("com.mybatis-flex:mybatis-flex-spring-boot3-starter:$mybatisFlexVersion")
    // implementation("com.google.guava:guava:$guavaVersion")

    implementation("org.projectlombok:lombok")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("com.mybatis-flex:mybatis-flex-processor:$mybatisFlexVersion")
    annotationProcessor("org.projectlombok:lombok")

    // developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
