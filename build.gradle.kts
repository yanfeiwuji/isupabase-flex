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
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.zaxxer:HikariCP")
    implementation("com.mybatis-flex:mybatis-flex-spring-boot3-starter:$mybatisFlexVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    annotationProcessor("com.mybatis-flex:mybatis-flex-processor:$mybatisFlexVersion")

    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
