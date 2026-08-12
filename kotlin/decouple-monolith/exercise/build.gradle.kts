plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "io.temporal.nexus"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Pinned to match java/decouple-monolith/pom.xml so both trees teach the same SDK.
    implementation("io.temporal:temporal-sdk:1.31.0")

    // Without an SLF4J binding the Worker prints a warning banner on every start.
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        // Emits parameter names into the class files so Jackson can bind constructor
        // arguments by name when deserializing Nexus operation payloads.
        javaParameters = true
    }
}

application {
    mainClass.set("payments.temporal.PaymentsWorkerAppKt")
}

// Four entry points, mirroring the Maven exec:java@<id> targets on the Java side.
fun entryPoint(name: String, mainClassName: String, desc: String) =
    tasks.register<JavaExec>(name) {
        group = "application"
        description = desc
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
        standardInput = System.`in`
    }

entryPoint("paymentsWorker", "payments.temporal.PaymentsWorkerAppKt", "Runs the Payments Worker.")
entryPoint("complianceWorker", "compliance.temporal.ComplianceWorkerAppKt", "Runs the Compliance Worker.")
entryPoint("starter", "payments.temporal.PaymentStarterKt", "Starts the three sample payments.")
entryPoint("reviewStarter", "payments.temporal.ReviewStarterKt", "Submits the human review for TXN-B via Nexus.")
