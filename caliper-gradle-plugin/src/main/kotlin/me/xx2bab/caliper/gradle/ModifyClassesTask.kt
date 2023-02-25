package me.xx2bab.caliper.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

@CacheableTask
abstract class ModifyClassesTask : DefaultTask() {

    @get:Internal
    lateinit var objectFactory: ObjectFactory

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val allJars: ListProperty<RegularFile>

//    @InputFiles
//    @PathSensitive(PathSensitivity.ABSOLUTE)
//    @Incremental
//    fun getAllJarsInFileCollection(): FileCollection {
//        val fc = objectFactory.fileCollection()
//        fc.from(allJars)
//        return fc
//    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val allDirectories: ListProperty<Directory>

//    @InputFiles
//    @PathSensitive(PathSensitivity.ABSOLUTE)
//    @Incremental
//    fun getAllDirectoriesInFileCollection(): FileCollection {
//        val fc = objectFactory.fileCollection()
//        fc.from(allDirectories)
//        return fc
//    }

    @get:OutputFile
    abstract val output: RegularFileProperty


    @TaskAction
    fun taskAction() {
//        inputChanges: InputChanges
//        println("ModifyClassesTask -> isIncremental ${inputChanges.isIncremental}")
        println("ModifyClassesTask -> ${output.get().asFile.absolutePath}")
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )
        allJars.get().forEach { file ->
            logger.lifecycle("handling jar: " + file.asFile.absolutePath)
            val jarFile = JarFile(file.asFile)
            jarFile.entries()
                .asSequence()
                .filter { !it.isDirectory && !it.name.startsWith("META-INF") }
                .forEach { jarEntry ->
                    logger.lifecycle("Adding from jar ${jarEntry.name}")
                    jarOutput.putNextEntry(JarEntry(jarEntry.name))
                    jarFile.getInputStream(jarEntry).use {
                        it.copyTo(jarOutput)
                    }
                    jarOutput.closeEntry()
                }
            jarFile.close()
        }
        allDirectories.get().forEach { directory ->
            logger.lifecycle("handling dir: " + directory.asFile.absolutePath)
            directory.asFile.walk()
                .filter { it.isFile }
                .forEach { file ->
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                    logger.lifecycle(
                        "Adding from directory ${
                            relativePath.replace(
                                File.separatorChar,
                                '/'
                            )
                        }"
                    )
                    jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(jarOutput)
                    }
                    jarOutput.closeEntry()
                }
        }
        jarOutput.close()
    }
}