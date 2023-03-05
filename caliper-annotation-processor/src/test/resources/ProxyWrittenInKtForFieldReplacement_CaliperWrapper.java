package me.xx2bab.caliper.runtime.wrapper;

import java.lang.String;

public final class ProxyWrittenInKtForFieldReplacement_CaliperWrapper {
    public static String getString() {
        Caliper.visitMethod("android/provider/Settings$Secure","getString");
        return me.xx2bab.caliper.test.ProxyWrittenInKtForFieldReplacement.getString();
    }
}
