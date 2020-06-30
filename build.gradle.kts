import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("application")
	id("org.springframework.boot") version "2.3.1.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("io.gitlab.arturbosch.detekt") version "1.4.0"
	id("jacoco")
	id("org.asciidoctor.convert") version "1.5.8"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
	kotlin("plugin.jpa") version "1.3.72"
}

group = "lechuck"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	jcenter()
	mavenCentral()
}

val snippetsDir by extra { file("build/generated-snippets") }

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.apache.commons:commons-lang3:3.10")
	implementation("org.apache.commons:commons-collections4:4.4")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.liquibase:liquibase-core")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("mysql:mysql-connector-java:8.0.18")
	runtimeOnly("org.lazyluke:log4jdbc-remix:0.2.7")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
	testImplementation("org.mockito:mockito-inline:3.3.3")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.test {
	outputs.dir(snippetsDir)
}

tasks.asciidoctor {
	inputs.dir(snippetsDir)
	dependsOn(tasks.test)
}

tasks.getByName<Jar>("jar").enabled = true
tasks.getByName<Tar>("bootDistTar").enabled = false
tasks.getByName<Zip>("bootDistZip").enabled = false
tasks.getByName<Zip>("distZip").enabled = false
tasks.getByName<Tar>("distTar").archiveName = "${project.name}.tar"

application {
	mainClassName = "lechuck.kakaopay.AppKt"
}

configure<DetektExtension> {
	input = files("$projectDir/src/main/kotlin")
	config = files("$projectDir/detekt.yml")
	autoCorrect = true
	failFast = false

	tasks.withType<Detekt> {
		setExcludes(listOf("**/resources/**", "**/bin/**", "**/build/**"))
	}
	reports {
		xml {
			enabled = true
			destination = file("$buildDir/reports/detekt.xml")
		}
		html {
			enabled = true
			destination = file("$buildDir/reports/detekt.html")
		}
	}
}

jacoco {
	toolVersion = "0.8.5"
}
tasks.jacocoTestReport {
	reports {
		xml.isEnabled = false
		csv.isEnabled = false
		html.destination = file("${buildDir}/reports/jacoco")
	}
}
