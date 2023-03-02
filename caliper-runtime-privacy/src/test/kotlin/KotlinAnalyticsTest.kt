import com.bennyhuo.kotlin.analyzer.KotlinCodeAnalyzer
import com.bennyhuo.kotlin.analyzer.buildOptions
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinAnalyticsTest {

    @Test
    fun testAndroidOSBuildProxyAnalytics() {
        val filePath = "/Users/2bab/Desktop/Caliper/caliper-runtime-privacy" +
                "/src/main/kotlin/me/xx2bab/caliper/permission/"

        val result = KotlinCodeAnalyzer(buildOptions {
            inputPaths = listOf(filePath)
        }).analyze()
        println("[KotlinAnalyticsTest]>>>: " + result.files)
        result.files.forEach {
            it.declarations.forEach {
                println(it.name)
                println(it.annotations.size)
                println(it.firstChild.text)
                when (it) {
                    is KtNamedFunction -> {
                        println(it.text)
                    }
                    is KtClass -> {
                        println(it.text)
                    }
                }
            }
        }
    }

}