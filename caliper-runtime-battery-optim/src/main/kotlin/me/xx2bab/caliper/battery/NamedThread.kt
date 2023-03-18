@file:JvmName("NamedThread")
package me.xx2bab.caliper.battery

import me.xx2bab.caliper.anno.CaliperClassProxy
import me.xx2bab.caliper.runtime.Caliper
import java.util.concurrent.atomic.AtomicInteger

@CaliperClassProxy(className = "java.lang.Thread")
class NamedThread : Thread {
    constructor() : super(
        currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement()
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(target: Runnable?) : super(
        target, currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement()
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(group: ThreadGroup?, target: Runnable?) : super(
        group, target, currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement()
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(name: String) : super(
        currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement() + "-" + name
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(group: ThreadGroup?, name: String) : super(
        group, currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement() + "-" + name
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(target: Runnable?, name: String) : super(
        target, currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement() + "-" + name
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(group: ThreadGroup?, target: Runnable?, name: String) : super(
        group, target, currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement() + "-" + name
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    constructor(group: ThreadGroup?, target: Runnable?, name: String, stackSize: Long) : super(
        group, target, currentThread().stackTrace[2].className
                + "-" + selfIncreasedId.getAndIncrement() + "-" + name, stackSize
    ) {
        Caliper.log(
            "NamedThread",
            "constructor",
            emptyArray(),
            emptyArray()
        )
    }

    companion object {
        var selfIncreasedId = AtomicInteger(0)
    }
}