package com.charlie.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


class PluginDemo : Plugin<Project> {

    override fun apply(project: Project) {
        System.out.println("=============== plugin ===============")
        val android = project.extensions.getByType(AppExtension::class.java)

        System.out.println("asdqwe $android")
        System.out.println("asdqwe ${android.transforms}")
//        System.out.println("asdqwe ${android.}")
//        System.out.println("asdqwe $android")
//        System.out.println("asdqwe $android")

//
//        android.registerTransform(DoubleTapTransform())

        android.registerTransform(DemoTransform())
    }
}