package com.charlie.plugin

/**
 * created by charlie on 2021/7/12
 */
object Utils {


    fun ignorePackageNames(className: String): Boolean {
        //命中白名单返回false
//        for (packageName in whitePackageNames) {
//            if (className.startsWith(packageName, true)) {
//                return false
//            }
//        }

        //命中黑名单返回true
        for (packageName in blackPackageNames) {
            if (className.startsWith(packageName, true)) {
                println("过滤 $className, $packageName")
                return true
            }
        }

        return false
    }


    /**
     * 白名单
     */
    private val whitePackageNames = arrayOf("")


    /**
     * 黑名单
     */
    private val blackPackageNames = arrayOf(
        "kotlin.",
        "java.",
        "android.",
        "androidx."
    )


}