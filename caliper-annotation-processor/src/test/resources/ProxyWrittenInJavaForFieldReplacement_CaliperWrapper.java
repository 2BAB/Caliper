package me.xx2bab.caliper.runtime.wrapper;

import java.lang.String;
import me.xx2bab.caliper.runtime.Caliper;

public final class ProxyWrittenInJavaForFieldReplacement_CaliperWrapper {
    public static String getString() {
        Caliper.log("android/provider/Settings$Secure", "getString", new String[]{}, new Object[]{});
        return me.xx2bab.caliper.test.ProxyWrittenInJavaForFieldReplacement.getString();
    }
}