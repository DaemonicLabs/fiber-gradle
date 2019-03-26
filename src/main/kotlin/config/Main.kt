package config

import mu.KLogging

object Main : KLogging() {
    @JvmStatic
    fun main(vararg args: String) {
        println("running")


//        ExtractInfo.main(
//            arrayOf(
//                "config.ArrayConfig",
//                "config.NestedConfig",
//                "config.KottonKonfig.NestedInner",
//                "config.KottonKonfig",
//                "io.github.cottonmc.cotton.config.CottonConfig"
//            )
//        )
    }
}