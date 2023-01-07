package me.xx2bab.caliper.runtime.wrapper;

import java.lang.String;
import me.xx2bab.caliper.anno.CaliperMeta;

@CaliperMeta(
        metadataInJSON = "{\"proxiedMethods\":[],\"proxiedFields\":[{\"className\":\"android/provider/Settings$Secure\",\"fieldName\":\"SERIAL\",\"opcode\":178,\"replacedClassName\":\"me/xx2bab/caliper/runtime/wrapper/ProxyWrittenInKtForFieldProxy_CaliperWrapper\",\"replacedMethodName\":\"getString\"}]}"
)
public final class ProxyWrittenInKtForFieldProxy_CaliperWrapper {
    public static String getString() {
        // Caliper.visitMethod("android/provider/Settings$Secure","getString");
        return me.xx2bab.caliper.test.ProxyWrittenInKtForFieldProxy.getString();
    }
}