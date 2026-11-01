@file:Suppress("SpellCheckingInspection", "GrazieInspection")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.org.apache.commons.lang3.SystemUtils
import java.nio.file.LinkOption
import java.util.Calendar
import java.util.Properties
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

project.group = "de.openindex.zugferd"
project.version = libs.versions.application.version.get()

val applicationPackageName = "Quba"

val isLinux = SystemUtils.IS_OS_LINUX
val isMac = SystemUtils.IS_OS_MAC
val isWindows = SystemUtils.IS_OS_WINDOWS

val isAmd64 = listOf("amd64", "x86_64")
    .contains(SystemUtils.OS_ARCH.lowercase())
val isArm64 = listOf("aarch64", "arm64")
    .contains(SystemUtils.OS_ARCH.lowercase())

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        rootProject.file("local.properties").reader().use(::load)
    }
}

val appleBundleId: String = localProperties["APPLE_BUNDLE_ID"]?.toString()?.trim() ?: "${project.group}.manager"
val appleKeyChain: String = (localProperties["APPLE_KEYCHAIN"]?.toString()?.trim() ?: "").let {
    // Resolve relative keychain path to project root directory.
    if (it.startsWith("./")) {
        rootProject.layout.projectDirectory.asFile.resolve(it.substring(2)).absolutePath
    } else {
        it
    }
}

val appleSign: Boolean = localProperties["APPLE_SIGN"]?.toString()?.trim() == "1"
val appleSignKey: String = localProperties["APPLE_SIGN_KEY"]?.toString()?.trim() ?: ""

val appleNotarization: Boolean = localProperties["APPLE_NOTARIZATION"]?.toString()?.trim() == "1"
val appleNotarizationAppleId: String = localProperties["APPLE_NOTARIZATION_APPLE_ID"]?.toString()?.trim() ?: ""
val appleNotarizationTeamId: String = localProperties["APPLE_NOTARIZATION_TEAM_ID"]?.toString()?.trim() ?: ""
val appleNotarizationPassword: String = localProperties["APPLE_NOTARIZATION_PASSWORD"]?.toString()?.trim() ?: ""

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)

    // Versions Plugin
    // https://github.com/ben-manes/gradle-versions-plugin
    alias(libs.plugins.versions)

    // BuildInfo Plugin
    // https://codeberg.org/h34tnet/buildinfo
    alias(libs.plugins.buildinfo)
}

//
// Build Info
// https://codeberg.org/h34tnet/buildinfo#configuration
//
buildInfo {
    className = "AppInfo"
    packageName = "de.openindex.zugferd.manager"
    path = project.layout.buildDirectory.file("generated/buildInfo").get().asFile
    addSourceSet = false

    hostNetwork = false
    hostOs = false
    java = false
    kotlin = false
    gradle = false
    guessCiProvider = false
    gitlabCi = false

    customFields = mapOf(
        "VENDOR" to "Mustangproject",
    )
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain {
            kotlin.srcDirs(
                project.layout.buildDirectory.file("generated/buildInfo").get().asFile
            )
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            //implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // FileKit
            // https://github.com/vinceglb/FileKit
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)

            // Kotlinx-DateTime
            // https://github.com/Kotlin/kotlinx-datetime
            implementation(libs.kotlinx.datetime)

            // Kotlinx-Serialization
            // https://github.com/Kotlin/kotlinx.serialization
            implementation(libs.kotlinx.serialization.json)

            implementation("org.jsoup:jsoup:1.17.2")
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            // AppDirs
            // https://github.com/harawata/appdirs
            implementation(libs.appdirs)

            // Commons-Lang
            // https://commons.apache.org/proper/commons-lang/
            implementation(libs.commons.lang)

            // Jaxen XPath Engine for Java
            // https://github.com/jaxen-xpath/jaxen
            runtimeOnly(libs.jaxen)

            // Logback
            // https://commons.apache.org/proper/commons-lang/
            implementation(libs.logback.classic)

            // Kotlinx-DateTime
            // https://logback.qos.ch/
            implementation(libs.kotlinx.datetime)

            // Mustang
            // https://www.mustangproject.org/
            // https://github.com/ZUGFeRD/mustangproject/
            implementation(libs.mustang.library)
            implementation(libs.mustang.validator)
            //implementation(variantOf(libs.mustang.validator) { classifier("shaded") })
            //implementation(libs.mustang.validator) {
            //    artifact {
            //        classifier = "shaded"
            //    }
            //}
            //implementation("${libs.mustang.validator.get().module}:${libs.versions.mustang.get()}:shaded")

            // SLF4J
            // https://www.slf4j.org/
            // https://www.slf4j.org/legacy.html#jul-to-slf4j
            implementation(libs.slf4j.jul)


            implementation("org.jsoup:jsoup:1.17.2")
        }
    }
}

compose.desktop {
    application {
        mainClass = "de.openindex.zugferd.manager.MainKt"

        jvmArgs("-Xmx1G")
        jvmArgs("-Dfile.encoding=UTF-8")

        //
        // Wanted by PDFBox.
        // https://github.com/msgpack/msgpack-java/issues/600#issuecomment-1168754147
        // https://github.com/uncomplicate/neanderthal/issues/55#issuecomment-469914674
        //

        jvmArgs("--add-opens", "java.base/java.nio=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.base/jdk.internal.ref=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")


        //
        // Required by JCEF.
        // https://github.com/jcefmaven/jcefmaven#limitations
        //

        jvmArgs("--add-exports", "java.base/java.lang=ALL-UNNAMED")
        jvmArgs("--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED")
        if (isMac) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        buildTypes {
            release {
                //
                // TODO: Check how to make use of ProGuard on release builds.
                // Currently failing with an error.
                // https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#minification--obfuscation
                //

                proguard {
                    isEnabled = false

                    //
                    // ProGuard-Gradle version.
                    // https://mvnrepository.com/artifact/com.guardsquare/proguard-gradle
                    //
                    version = libs.versions.proguard.get()
                }
            }
        }

        nativeDistributions {
            val copyrightYear = "2025-${Calendar.getInstance().get(Calendar.YEAR)}"

            packageName = applicationPackageName
            //packageVersion = project.version.toString()
            vendor = "Mustangproject"
            copyright = "© $copyrightYear OpenIndex. All rights reserved."
            description = "A desktop application for creating and validating e-invoices."
            licenseFile = rootProject.file("LICENSE.txt")

            if (isLinux) {
                targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            }
            if (isMac) {
                targetFormats(TargetFormat.Dmg)
            }
            if (isWindows) {
                targetFormats(TargetFormat.Exe)
            }

            //includeAllModules = true

            modules(
                "java.base", // Defines the foundational APIs of the Java SE Platform.
                //"java.compiler", // Defines the Language Model, Annotation Processing, and Java Compiler APIs.
                "java.datatransfer", //Defines the API for transferring data between and within applications.
                "java.desktop", //Defines the AWT and Swing user interface toolkits, plus APIs for accessibility, audio, imaging, printing, and JavaBeans.
                //"java.instrument", //Defines services that allow agents to instrument programs running on the JVM.
                "java.logging",  // Defines the Java Logging API.
                "java.management", //Defines the Java Management Extensions (JMX) API.
                //"java.management.rmi", // Defines the RMI connector for the Java Management Extensions (JMX) Remote API.
                "java.naming", // Defines the Java Naming and Directory Interface (JNDI) API.
                //"java.net.http", // Defines the HTTP Client and WebSocket APIs.
                "java.prefs", // Defines the Preferences API.
                //"java.rmi", // Defines the Remote Method Invocation (RMI) API.
                //"java.scripting", // Defines the Scripting API.
                //"java.se", // Defines the API of the Java SE Platform.
                //"java.security.jgss", // Defines the Java binding of the IETF Generic Security Services API (GSS-API).
                //"java.security.sasl", // Defines Java support for the IETF Simple Authentication and Security Layer (SASL).
                //"java.smartcardio", // Defines the Java Smart Card I/O API.
                //"java.sql", // Defines the JDBC API.
                //"java.sql.rowset", // Defines the JDBC RowSet API.
                //"java.transaction.xa", // Defines an API for supporting distributed transactions in JDBC.
                "java.xml", // Defines the Java APIs for XML Processing (JAXP).
                //"java.xml.crypto", // Defines the API for XML cryptography.
                "jdk.accessibility", // Defines JDK utility classes used by implementors of Assistive Technologies.
                //"jdk.attach", // Defines the attach API.
                //"jdk.charsets", // Provides charsets that are not in java.base (mostly double byte and IBM charsets).
                //"jdk.compiler", // Defines the implementation of the system Java compiler and its command line equivalent, javac.
                //"jdk.crypto.cryptoki", // Provides the implementation of the SunPKCS11 security provider.
                //"jdk.crypto.ec", // Provides the implementation of the SunEC security provider.
                //"jdk.dynalink", // Defines the API for dynamic linking of high-level operations on objects.
                //"jdk.editpad", // Provides the implementation of the edit pad service used by jdk.jshell.
                //"jdk.hotspot.agent", // Defines the implementation of the HotSpot Serviceability Agent.
                //"jdk.httpserver", // Defines the JDK-specific HTTP server API, and provides the jwebserver tool for running a minimal HTTP server.
                //"jdk.incubator.vector", // Defines an API for expressing computations that can be reliably compiled at runtime into SIMD instructions, such as AVX instructions on x64, and NEON instructions on AArch64.
                //"jdk.jartool", // Defines tools for manipulating Java Archive (JAR) files, including the jar and jarsigner tools.
                //"jdk.javadoc", // Defines the implementation of the system documentation tool and its command-line equivalent, javadoc.
                //"jdk.jcmd", // Defines tools for diagnostics and troubleshooting a JVM such as the jcmd, jps, jstat tools.
                //"jdk.jconsole", // Defines the JMX graphical tool, jconsole, for monitoring and managing a running application.
                //"jdk.jdeps", // Defines tools for analysing dependencies in Java libraries and programs, including the jdeps, javap, and jdeprscan tools.
                //"jdk.jdi", // Defines the Java Debug Interface.
                //"jdk.jdwp.agent", // Provides the implementation of the Java Debug Wire Protocol (JDWP) agent.
                //"jdk.jfr", // Defines the API for JDK Flight Recorder.
                //"jdk.jlink", // Defines the jlink tool for creating run-time images, the jmod tool for creating and manipulating JMOD files, and the jimage tool for inspecting the JDK implementation-specific container file for classes and resources.
                //"jdk.jpackage", // Defines the Java Packaging tool, jpackage.
                //"jdk.jshell", // Provides the jshell tool for evaluating snippets of Java code, and defines a JDK-specific API for modeling and executing snippets.
                //"jdk.jsobject", // Defines the API for the JavaScript Object.
                //"jdk.jstatd", // Defines the jstatd tool for starting a daemon for the jstat tool to monitor JVM statistics remotely.
                "jdk.localedata", // Provides the locale data for locales other than US locale.
                //"jdk.management", // Defines JDK-specific management interfaces for the JVM.
                //"jdk.management.agent", // Defines the JMX management agent.
                //"jdk.management.jfr", // Defines the Management Interface for JDK Flight Recorder.
                //"jdk.naming.dns", // Provides the implementation of the DNS Java Naming provider.
                //"jdk.naming.rmi", // Provides the implementation of the RMI Java Naming provider.
                //"jdk.net", // Defines the JDK-specific Networking API.
                //"jdk.nio.mapmode", // Defines JDK-specific file mapping modes.
                //"jdk.sctp", // Defines the JDK-specific API for SCTP.
                //"jdk.security.auth", // Provides implementations of the javax.security.auth.* interfaces and various authentication modules.
                //"jdk.security.jgss", // Defines JDK extensions to the GSS-API and an implementation of the SASL GSSAPI mechanism.
                //"jdk.xml.dom", // Defines the subset of the W3C Document Object Model (DOM) API that is not part of the Java SE API.
                //"jdk.zipfs", // Provides the implementation of the Zip file system provider.

                //
                // Jetbrains JDK specific modules
                //
                "gluegen.rt",
                "jcef",
                "jogl.all",
            )

            if (isLinux) {
                // Required by FileKit
                // https://github.com/vinceglb/FileKit#-installation
                modules.add("jdk.security.auth")
            }

            linux {
                appCategory = "misc"
                appRelease = libs.versions.application.revision.get()
                menuGroup = "OpenIndex-ZUGFeRD"
                installationPath = "/opt/OpenIndex-${applicationPackageName}"
                debMaintainer = "andy@openindex.de"
                rpmLicenseType = "Apache-2.0"
                packageVersion = project.version.toString()
                iconFile.set(rootProject.layout.projectDirectory.dir("share").dir("icons").file("application.png"))
            }

            macOS {
                bundleID = appleBundleId
                dockName = applicationPackageName
                packageVersion = project.version.toString()
                packageBuildVersion = project.version.toString()
                appCategory = "public.app-category.business"
                iconFile.set(rootProject.layout.projectDirectory.dir("share").dir("icons").file("application.icns"))

                // Currently not published to the App Store.
                appStore = false

                // Default entitlements are used for application and runtime.
                // https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/resources/default-entitlements.plist
                entitlementsFile.set(
                    rootProject.layout.projectDirectory
                        .dir("share").dir("apple").file("default.entitlements.plist")
                )
                runtimeEntitlementsFile.set(
                    rootProject.layout.projectDirectory
                        .dir("share").dir("apple").file("default.entitlements.plist")
                )

                //
                // We're using the default signing mechanism to provide a valid runtime.
                // As further native libraries are added to the application bundle,
                // the bundle needs to be signed again in a later step.
                //
                signing {
                    sign.set(appleSign)
                    identity.set(appleSignKey)
                    keychain.set(appleKeyChain)
                }

                //
                // Currently we're not using the default notarization mechanism.
                //
                //notarization {
                //    appleID.set(appleNotarizationAppleId)
                //    password.set(appleNotarizationPassword)
                //    teamID.set(appleNotarizationTeamId)
                //}

                //infoPlist {
                //    extraKeysRawXml = """
                //        <key>CFBundleDevelopmentRegion</key>
                //        <string>German</string>
                //    """
                //}
            }

            windows {
                packageVersion = project.version.toString()
                menuGroup = "OpenIndex-ZUGFeRD"
                upgradeUuid = "c8ce1e91-7f6f-45a6-a1c5-14020bc7c5e3"
                console = false
                dirChooser = true
                perUserInstall = true
                iconFile.set(rootProject.layout.projectDirectory.dir("share").dir("icons").file("application.ico"))
            }
        }
    }
}

tasks {
    getByName("compileKotlinDesktop") {
        dependsOn("buildInfo")
    }

    register("bundle") {
        group = "OpenIndex"
        description = "Create application bundle for current operating system."

        if (isLinux) {
            dependsOn("bundleLinuxArchive")
            dependsOn("bundleLinuxDeb")
            dependsOn("bundleLinuxRpm")
        }
        if (isMac) {
            dependsOn("bundleMacDmg")
        }
        if (isWindows) {
            dependsOn("bundleWindowsArchive")
            dependsOn("bundleWindowsExe")
        }
    }

    if (isLinux) {
        register<Tar>("bundleLinuxArchive") {
            group = "OpenIndex"
            description = "Create application archive for Linux."
            dependsOn("createReleaseDistributable")

            archiveExtension = "tar.gz"
            archiveVersion = libs.versions.application.pkg.get()
            archiveClassifier = if (isArm64) "linux-arm64" else "linux-x64"
            compression = Compression.GZIP
            destinationDirectory = rootProject.layout.buildDirectory
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/app"))
            from(rootProject.layout.projectDirectory.file("LICENSE.txt")) {
                into(project.name)
            }
        }

        register<Copy>("bundleLinuxDeb") {
            group = "OpenIndex"
            description = "Create deb installer for Linux."
            dependsOn("packageReleaseDistributionForCurrentOS")

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/deb"))
            rename { name ->
                if (!name.endsWith(".deb")) {
                    return@rename name
                }

                val arch = if (isArm64) "arm64" else "x64"
                "${project.name}-${libs.versions.application.pkg.get()}-linux-${arch}.deb"
            }
        }

        register<Copy>("bundleLinuxRpm") {
            group = "OpenIndex"
            description = "Create rpm installer for Linux."
            dependsOn("packageReleaseDistributionForCurrentOS")

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/rpm"))
            rename { name ->
                if (!name.endsWith(".rpm")) {
                    return@rename name
                }

                val arch = if (isArm64) "arm64" else "x64"
                "${project.name}-${libs.versions.application.pkg.get()}-linux-${arch}.rpm"
            }
        }

        // Modify runtime image for Linux builds.
        whenTaskAdded {
            if (name != "createRuntimeImage") {
                return@whenTaskAdded
            }

            val runtimeImageDir = project.layout.buildDirectory
                .dir("compose/tmp/main/runtime")
                .get()

            val runtimeLibDir = runtimeImageDir
                .dir("lib")

            val cefLocalesDir = runtimeLibDir
                .dir("locales")

            val cefTranslations = listOf(
                "de.pak",
                "en.pak",
                "en-gb.pak",
                "en-us.pak",
            )

            doLast {
                // Make JCEF binaries executable.
                listOf("chrome-sandbox", "jcef_helper")
                    .map { runtimeLibDir.file(it).asFile.absolutePath }
                    .forEach {
                        exec {
                            commandLine("chmod", "ugo+x", it)
                        }
                    }

                // Delete unnecessary "cef_server" binary.
                runtimeLibDir.file("cef_server")
                    .asFile.deleteRecursively()

                // Delete unnecessary translations.
                cefLocalesDir.asFile.listFiles()
                    ?.filter { it.isFile }
                    ?.filter { !cefTranslations.contains(it.name.lowercase()) }
                    ?.forEach { it.delete() }
            }
        }

        // Copy alternative launcher script into distribution directory.
        whenTaskAdded {
            val releaseType = when (name) {
                "createReleaseDistributable" -> "main-release"
                "createDistributable" -> "main"
                else -> return@whenTaskAdded
            }

            val appBundleDir = project.layout.buildDirectory
                .dir("compose/binaries/${releaseType}/app/${project.name}")
                .get()

            val appBinDir = appBundleDir
                .dir("bin")

            val launcherScript = rootProject.layout.projectDirectory
                .dir("share").dir("linux").file("launcher.sh")

            doLast {
                copy {
                    from(launcherScript)
                    into(appBinDir)
                    rename {
                        "${applicationPackageName}.sh"
                    }
                }
            }
        }
    }

    if (isMac) {
        /**
         * Test, if a file should be signed.
         */
        fun File.isSignableFile(): Boolean {
            val path = toPath()
            if (!path.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                return false
            }
            if (path.isExecutable()) {
                return true
            }
            if (path.name.endsWith(".dylib")) {
                return true
            }
            if (path.name.endsWith(".jnilib")) {
                return true
            }
            return false
        }

        /**
         * Unsign a file or directory.
         */
        fun Project.macUnsign(fileToUnsign: File) {
            exec {
                workingDir = fileToUnsign.parentFile
                executable = "codesign"
                args = listOf(
                    "--remove-signature",
                    fileToUnsign.name,
                )
            }
        }

        /**
         * Sign a file or directory.
         */
        fun Project.macSign(
            fileToSign: File,
            entitlements: File? = null,
            unsignBefore: Boolean = true,
            runtime: Boolean = false,
        ) {
            //println("signing ${fileToSign.name}")
            logger.info("signing ${fileToSign.name}")

            if (unsignBefore) {
                macUnsign(fileToSign)
            }

            exec {
                workingDir = fileToSign.parentFile
                executable = "codesign"
                args = buildList {
                    add("--force")
                    add("--timestamp")
                    add("--sign")
                    add(appleSignKey)
                    add("--keychain")
                    add(appleKeyChain)
                    add("--identifier")
                    if (runtime) {
                        add("com.oracle.java.${appleBundleId}")
                    } else {
                        add(appleBundleId)
                    }
                    add("--options")
                    add("runtime")
                    if (entitlements != null) {
                        add("--entitlements")
                        add(entitlements.absolutePath)
                    }
                    add(fileToSign.name)
                }
            }
        }

        register<Copy>("bundleMacDmg") {
            group = "OpenIndex"
            description = "Create dmg installer for MacOS."

            // We currently don't use automatic notarization by the CMP Gradle plugin.
            //dependsOn("notarizeDmg")

            dependsOn("packageReleaseDistributionForCurrentOS")

            val arch = if (isArm64) "arm64" else "x64"
            val dmgTargetFileName = "${project.name}-${libs.versions.application.pkg.get()}-macos-${arch}.dmg"

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/dmg"))
            rename { name ->
                if (!name.endsWith(".dmg")) {
                    return@rename name
                }
                dmgTargetFileName
            }

            // Sign, notarize and staple the generated DMG file.
            doLast {
                // DMG is not automatically signed.
                // Therefore, we're doing this manually.
                if (appleSign) {
                    macSign(
                        fileToSign = rootProject.layout.buildDirectory
                            .file(dmgTargetFileName).get().asFile,
                        unsignBefore = false,
                    )
                }

                if (appleNotarization) {
                    // Notarize final DMG.
                    exec {
                        workingDir = rootProject.layout.buildDirectory.get().asFile
                        executable = "xcrun"
                        args = listOf(
                            "notarytool",
                            "submit",
                            //"--verbose",
                            "--apple-id", appleNotarizationAppleId,
                            "--team-id", appleNotarizationTeamId,
                            "--password", appleNotarizationPassword,
                            "--wait",
                            dmgTargetFileName,
                        )
                    }

                    // Staple notarized DMG.
                    exec {
                        workingDir = rootProject.layout.buildDirectory.get().asFile
                        executable = "xcrun"
                        args = listOf(
                            "stapler",
                            "staple",
                            dmgTargetFileName,
                        )
                    }
                }
            }
        }

        // Extract and sign native libraries before the Mac distributable is created.
        // And finally sign the modified application bundle again.
        whenTaskAdded {
            val releaseType = when (name) {
                "createReleaseDistributable" -> "main-release"
                "createDistributable" -> "main"
                else -> return@whenTaskAdded
            }

            val appBundleDir = project.layout.buildDirectory
                .dir("compose/binaries/${releaseType}/app/${project.name}.app")
                .get()

            val runtimeDir = appBundleDir.dir("Contents/runtime")
            val frameworksDir = runtimeDir.dir("Contents/Frameworks")

            val defaultEntitlements = rootProject.layout.projectDirectory
                .dir("share").dir("apple").file("default.entitlements.plist")

            val cefTranslations = listOf(
                "de.lproj",
                "en.lproj",
                "en-gb.lproj",
                "en-us.lproj",
            )

            // Copy native libraries into the application bundle and sign the bundle again.
            // https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/internal/MacSigner.kt
            // https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/internal/MacSigningHelper.kt
            doLast {
                val srcFrameworksDir = File(SystemUtils.JAVA_HOME).parentFile.resolve("Frameworks")
                if (!srcFrameworksDir.isDirectory) {
                    throw GradleException("Frameworks folder not found in JAVA_HOME.")
                }

                copy {
                    from(srcFrameworksDir) {
                        // Exclude unnecessary "cef_server" application.
                        exclude {
                            it.name == "cef_server.app"
                        }

                        // Exclude unnecessary translations.
                        exclude {
                            it.relativePath.parent.endsWith("Resources")
                                    && it.name.endsWith(".lproj")
                                    && !cefTranslations.contains(it.name.lowercase())
                        }
                    }
                    into(frameworksDir)
                }

                if (appleSign) {
                    //frameworksDir.asFile.walkBottomUp()
                    //    .filter { it.isSignableFile() }
                    //    .forEach {
                    //        macSign(
                    //            fileToSign = it,
                    //            entitlements = defaultEntitlements.asFile,
                    //        )
                    //    }

                    //frameworksDir.asFile.listFiles()
                    //    ?.filter { it.isDirectory }
                    //    ?.forEach {
                    //        macSign(
                    //            fileToSign = it,
                    //            entitlements = defaultEntitlements.asFile,
                    //        )
                    //    }

                    macSign(
                        fileToSign = runtimeDir.asFile,
                        entitlements = defaultEntitlements.asFile,
                        runtime = true,
                    )

                    macSign(
                        fileToSign = appBundleDir.asFile,
                        entitlements = defaultEntitlements.asFile,
                        runtime = false,
                    )
                }
            }
        }
    }

    if (isWindows) {
        register<Zip>("bundleWindowsArchive") {
            group = "OpenIndex"
            description = "Create application archive for Windows."
            dependsOn("createReleaseDistributable")

            archiveExtension = "zip"
            archiveVersion = libs.versions.application.pkg.get()
            archiveClassifier = if (isArm64) "windows-arm64" else "windows-x64"
            destinationDirectory = rootProject.layout.buildDirectory
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/app"))
            from(rootProject.layout.projectDirectory.file("LICENSE.txt")) {
                into(project.name)
            }
        }

        register("bundleWindowsExe") {
            group = "OpenIndex"
            description = "Create exe installer for Windows."
            dependsOn("packageReleaseDistributionForCurrentOS")

            doLast {
                copy {
                    into(rootProject.layout.buildDirectory)
                    from(project.layout.buildDirectory.dir("compose/binaries/main-release/exe"))
                    rename { name ->
                        if (!name.endsWith(".exe")) {
                            return@rename name
                        }

                        val arch = if (isArm64) "arm64" else "x64"
                        "${project.name}-${libs.versions.application.pkg.get()}-windows-${arch}.exe"
                    }
                }
            }
        }

        // Modify runtime image for Windows builds.
        whenTaskAdded {
            if (name != "createRuntimeImage") {
                return@whenTaskAdded
            }

            val runtimeImageDir = project.layout.buildDirectory
                .dir("compose/tmp/main/runtime")
                .get()

            val runtimeBinDir = runtimeImageDir
                .dir("bin")

            val runtimeLibDir = runtimeImageDir
                .dir("lib")

            val cefLocalesDir = runtimeLibDir
                .dir("locales")

            val cefTranslations = listOf(
                "de.pak",
                "en.pak",
                "en-gb.pak",
                "en-us.pak",
            )

            doLast {
                // Copy required files from JAVA_HOME.
                listOf(
                    "icudtl.dat",
                    "jcef_helper.exe",
                    "v8_context_snapshot.bin",
                ).forEach {
                    copy {
                        from(
                            File(SystemUtils.JAVA_HOME)
                                .resolve("bin")
                                .resolve(it)
                        )
                        into(runtimeBinDir)
                    }
                }

                // Delete unnecessary translations.
                cefLocalesDir.asFile.listFiles()
                    ?.filter { it.isFile }
                    ?.filter { !cefTranslations.contains(it.name.lowercase()) }
                    ?.forEach { it.delete() }
            }
        }
    }
}
