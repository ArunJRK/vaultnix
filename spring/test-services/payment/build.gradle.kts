plugins {
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
//	id("org.graalvm.buildtools.native") version "0.10.2"
}

group = "com.innowate.services"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-web")
	implementation("org.springframework.security:spring-security-core")
	implementation("org.springframework.security:spring-security-config")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.projectreactor.netty:reactor-netty")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation(platform("io.micrometer:micrometer-tracing-bom:1.3.4"))
	implementation("io.micrometer:micrometer-core")
	implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-registry-otlp")
	implementation("io.micrometer:micrometer-tracing-bridge-otel")

	implementation(platform("io.opentelemetry:opentelemetry-bom:1.42.1"))
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")


}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

