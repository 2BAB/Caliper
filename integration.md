# Integration

## 1. Add the plugin to classpath

``` kotlin
// Option 1.
// Add `mavenCentral` to `pluginManagement{}` on `settings.gradle.kts` (or the root `build.gradle.kts`),
// and then the caliper plugin id.
pluginManagement {
    repositories {
        ...
        mavenCentral()
    }
    plugins {
    	...    	
    	id("com.google.devtools.ksp") version "1.7.22-1.0.8" apply false
    	id("me.2bab.caliper") version "0.2.2" apply false
    }
}

// Option 2.
// Using classic `buildscript{}` block in root build.gradle.kts.
buildscript {
    repositories {
        ...
        mavenCentral()
    }
    dependencies {
        ...       
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.7.22-1.0.8")
        classpath("me.2bab:caliper-gradle-plugin:0.2.2")       
    }
}
```

## 2. Apply KSP & Caliper plugin in Android Application module

``` kotlin
plugins {
    id("com.android.application")    
    ...   
    id("me.2bab.caliper")
}

caliper {
    // Main feature flags (Mandatory).
    // Can not be lazily set, it's valid only if you call it before "afterEvaluate{}".    
    enableByVariant { variant ->
        // With below snippet, only "FullDebug" variant will be interacted with Caliper.
        // variant.buildType == "debug" && variant.flavorName == "full"
        true
    }
}
```

## 3. Add a proxy

Now imagine you have a class in your library module, and you want to intercept the
method `commonMethodReturnsString()`.

``` kotlin
// ./sample/library/.../LibrarySampleClass.kt
class LibrarySampleClass {
    fun commonMethodReturnsString(): String {
        return "commonMethodReturnsString"
    }
}
```

0x1. Create a new Android Library module to host our proxy rules.

``` kotlin
// ./sample/custom-proxy/build.gradle.kts
plugins {
    id("com.android.library")
    kotlin("android")

    id("com.google.devtools.ksp") // Apply the KSP plugin ahead of Caliper
    id("me.2bab.caliper")
}

android {...}

dependencies {
    ksp("me.2bab:caliper-annotation-processor:$latestVersion")
    implementation(project(":library"))
}                                  
```

0x2. Create a new class named `CustomProxy.kt` in the `src/main/kotlin` folder.

``` kotlin
package me.xx2bab.caliper.sample.customproxy

import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperMethodProxy
import me.xx2bab.caliper.sample.library.LibrarySampleClass

object CustomProxy {  // ①

    @CaliperMethodProxy(  // ②
        className = "me/xx2bab/caliper/sample/library/LibrarySampleClass",
        methodName = "commonMethodReturnsString",
        opcode = ASMOpcodes.INVOKEVIRTUAL
    )
    @JvmStatic // ③
    fun commonMethodReturnsString(lib: LibrarySampleClass) = "CustomProxy"  // ④

}
```

- ① The class must be an `object` class when writing in Kotlin.
- ② The annotation `@CaliperMethodProxy` is used to mark the method as a proxy.
    - `className` is the full name of the class to be intercepted whose package name is separated by
      slash.
    - `methodName` is the name of the method to be intercepted.
    - `opcode` is the operation-code of the method to be intercepted, for example:
        - `INVOKEVIRTUAL` for non-static method.
        - `INVOKESTATIC` for static method.
        - `GETSTATIC` for static field.
        - ... and so on. More details can be found
          in [ASM Opcodes](https://asm.ow2.io/asm4-guide.pdf).
    - Caplier supports 3 types of proxy methods:
        - `@CaliperMethodProxy` for method interception.
        - `@CaliperFieldProxy` for field interception.
        - `@CaliperClassProxy` for class interception. 
- ③ The method must be `@JvmStatic` class when writing in Kotlin.
- ④ The method must have
    - the same signature as the original method when it's a **static** method.
    - the same signature as the original method, except the first parameter will be the instance of
      the class, when it's a **non-static** method. For example, if the original method
      is `fun commonMethodReturnsString(): String`, then the proxy method should
      be `fun commonMethodReturnsString(lib: LibrarySampleClass): String`.

0x3. Now go back to the `app` module, and add the `custom-proxy` module as a dependency
with `caliper` configuration.

``` kotlin
caliper(project(":custom-proxy"))
```

After that, you can run the app and see the result. The toast message below should be `CustomProxy`.

```kotlin
Toast.makeText(
    this,
    LibrarySampleClass().commonMethodReturnsString(),
    Toast.LENGTH_SHORT
).show()
```

## 4. (Optional) Add pre-packaged proxies

The step 3 is a bit tedious(however empowering everyone to custom their own proxies), so we provide some pre-packaged proxies for you to use.

``` kotlin
caliper("me.2bab:caliper-runtime-privacy:$latestVersion")
caliper("me.2bab:caliper-runtime-battery-optim:$latestVersion")
```

More details can be found in [caliper-runtime-privacy](./caliper-runtime-privacy) and [caliper-runtime-battery-optim](./caliper-runtime-battery-optim/).


## 5. (Optional) Caliper runtime extension

You can also leverage the `Caliper` class to do some runtime operations. For example, print the signature of all proxied calling in the app.

``` kotlin
Caliper.register(object : SignatureVisitor {
    override fun visit(
        className: String,
        elementName: String,
        parameterNames: Array<String>,
        parameterValues: Array<Any>
    ) {
        println("$className->$elementName")
    }
})
```