package me.xx2bab.caliper.runtime.wrapper;

import me.xx2bab.caliper.anno.CaliperMeta;

@CaliperMeta(
        metadataInJSON = "{\"proxiedMethods\":[],\"proxiedFields\":[],\"proxiedClasses\":[{\"className\":\"java.lang.Thread\",\"replacedClassName\":\"me/xx2bab/caliper/test/ThreadClassProxyWrittenInJava\"}]}"
)
private final class ThreadClassProxyWrittenInJava_CaliperWrapper {
}
