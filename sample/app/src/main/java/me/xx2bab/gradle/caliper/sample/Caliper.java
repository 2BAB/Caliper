package me.xx2bab.gradle.caliper.sample;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.Settings;

public class Caliper {

    public static final String SERIAL = Build.SERIAL;

    public static String getString(ContentResolver resolver, String name) {
        return "";
    }

    public static String getSerial() {
        return "";
    }

}
