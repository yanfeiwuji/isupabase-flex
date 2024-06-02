plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "io.github.yanfeiwuji.isupabase"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
}

var mybatisFlexVersion = "1.8.8"
var hutoolVersion = "5.8.26"
var guavaVersion = "33.2.0-jre"
var jsonPathVersion = "2.9.0"// https://mvnrepository.com/artifact/com.jayway.jsonpath/json-path


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") 
    implementation("org.springframework.boot:spring-boot-starter-aop")

    implementation("com.zaxxer:HikariCP") 
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("cn.hutool:hutool-all:$hutoolVersion")
    implementation("com.mybatis-flex:mybatis-flex-spring-boot3-starter:$mybatisFlexVersion")
    // implementation("com.google.guava:guava:$guavaVersion")

    implementation("org.projectlombok:lombok")

    runtimeOnly("org.postgresql:postgresql")

    implementation("com.jayway.jsonpath:json-path:$jsonPathVersion")
    annotationProcessor("com.mybatis-flex:mybatis-flex-processor:$mybatisFlexVersion")
    annotationProcessor("org.projectlombok:lombok")

    // developmentOnly("org.springframework.boot:spring-boot-devtools")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
