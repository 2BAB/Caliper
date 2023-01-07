package me.xx2bab.caliper.runtime.wrapper;

import java.lang.String;
import me.xx2bab.caliper.anno.CaliperMeta;

@CaliperMeta(
        metadataInJSON = "{\"proxiedMethods\":[{\"className\":\"android/provider/Settings$Secure\",\"methodName\":\"getString\",\"opcode\":184,\"replacedClassName\":\"me/xx2bab/caliper/runtime/wrapper/ProxyWrittenInKtForMethodProxy_CaliperWrapper\",\"replacedMethodName\":\"getString\"}],\"proxiedFields\":[]}"
)
public final class ProxyWrittenInKtForMethodProxy_CaliperWrapper {
    public static String getString(String name) {
        // Caliper.visitMethod("android/provider/Settings$Secure","getString",name);
        return me.xx2bab.caliper.test.ProxyWrittenInKtForMethodProxy.getString(name);
    }
}