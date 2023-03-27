# Caliper

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.2bab/caliper-gradle-plugin/badge.svg)](https://search.maven.org/artifact/me.2bab/caliper-gradle-plugin)
[![Actions Status](https://github.com/2bab/Caliper/workflows/CI/badge.svg)](https://github.com/2bab/Caliper/actions)
[![Apache 2](https://img.shields.io/badge/License-Apache%202-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)


🚧 **It's currently under incubating...**

A monitor & controller for Android sensitive permissions/api calls based on bytecode transformation.

It supports adding custom proxy rules to intercept sensitive api calls with a single annotation like below:

```kotlin
object ActivityProxy {
    @CaliperMethodProxy(
        className = "android/app/Activity",
        methodName = "requestPermissions",
        opcode = ASMOpcodes.INVOKEVIRTUAL
    )
    @JvmStatic
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        activity.requestPermissions(permissions, requestCode)
    }
}
```

Now all `Activity#requestPermissions` calls will be forwarded to the proxy method.

## Integration

Please navigate to [Integration Guide](./integration.md) for full details.

## Compatible Specification

Caliper is only supported & tested on latest 2 Minor versions of Android Gradle Plugin.

**Changelog** can be found from the [Releases](https://github.com/2BAB/Caliper/releases).
    
| AGP Version | Recommended Gradle Version | Compiled Kotlin Version |   Compiled Kotlin Version    | Latest Support Version |
|:-----------:|:--------------------------:|:-----------------------:|:----------------------------:|:----------------------:|
|    8.0.x    |           8.0.2+           |         1.7.22          |         1.7.22-1.0.8         |         0.2.2          |


## Git Commit Check

Check this [link](https://medium.com/walmartlabs/check-out-these-5-git-tips-before-your-next-commit-c1c7a5ae34d1) to
make sure everyone will make a **meaningful** commit message.

So far we haven't added any hook tool, but follow the regex below:

```
(chore|feat|docs|fix|refactor|style|test|hack|release)(:)( )(.{0,80})
```

## Credits

- The design of interceptor rules (`@CaliperProxyXyz`) was inspired by [allenymt/PrivacySentry](https://github.com/allenymt/PrivacySentry).
- The Kotlin code-analyzer forked from [bennyhuo/kotlin-code-analyzer](https://github.com/bennyhuo/kotlin-code-analyzer). 

## License

>
> Copyright 2022-2023 2BAB
>
> Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
