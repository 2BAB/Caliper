package me.xx2bab.caliper.runtime.wrapper;

import java.lang.String;

public final class ProxyWrittenInJavaForMethodReplacement_CaliperWrapper {
    public static String getString(String name) {
        Caliper.visitMethod("android/provider/Settings$Secure","getString",name);
        return me.xx2bab.caliper.test.ProxyWrittenInJavaForMethodReplacement.getString(name);
    }
}