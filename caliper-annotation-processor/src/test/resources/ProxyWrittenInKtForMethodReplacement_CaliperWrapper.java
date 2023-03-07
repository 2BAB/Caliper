package me.xx2bab.caliper.runtime.wrapper;

import java.lang.String;
import me.xx2bab.caliper.runtime.Caliper;

public final class ProxyWrittenInKtForMethodReplacement_CaliperWrapper {
    public static String getString(String name) {
        Caliper.log("android/provider/Settings$Secure", "getString", new String[]{"name"}, new Object[]{name});
        return me.xx2bab.caliper.test.ProxyWrittenInKtForMethodReplacement.getString(name);
    }
}