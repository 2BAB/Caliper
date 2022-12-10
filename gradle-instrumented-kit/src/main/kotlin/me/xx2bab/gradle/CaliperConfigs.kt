package me.xx2bab.gradle

object CaliperConfigs {

    fun regular() = """
        caliper {
            enableByVariant { variant ->
                variant.name.contains("debug", true)
                    && variant.name.contains("full", true)
            }
        }
    """

    val partialConfiguratedOnly = """
        caliper {
            
        }
    """.trimIndent()
}