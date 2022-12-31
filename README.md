# Caliper

🚧 **It's currently under incubating...**

A monitor for Android Sensitive Permission/API based on bytecode transformation.

Inspired by [allenymt/PrivacySentry](https://github.com/allenymt/PrivacySentry).

## Quick Start

**0x01. Add the plugin to classpath:**

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
    	id("me.2bab.caliper") version "0.1.0" apply false
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
        classpath("me.2bab:caliper:0.1.0")
    }
}
```

To be continued...

## Compatible Specification

Polyfill is only supported & tested on latest Major versions of Android Gradle Plugin.

**Changelog** can be found from the [Releases](https://github.com/2BAB/Caliper/releases).
    
|  AGP Version  | Latest Support Version |
|:-------------:|:----------------------:|
|  7.0.x~7.3.x  |           /            |


## Git Commit Check

Check this [link](https://medium.com/walmartlabs/check-out-these-5-git-tips-before-your-next-commit-c1c7a5ae34d1) to
make sure everyone will make a **meaningful** commit message.

So far we haven't added any hook tool, but follow the regex below:

```
(chore|feat|docs|fix|refactor|style|test|hack|release)(:)( )(.{0,80})
```

## License

>
> Copyright 2018-2023 2BAB
>
> Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
