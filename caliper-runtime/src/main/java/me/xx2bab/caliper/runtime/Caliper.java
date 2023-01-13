package me.xx2bab.caliper.runtime;

import java.util.LinkedList;
import java.util.List;

public final class Caliper {
    private static List<SignatureVisitor> visitors = new LinkedList<>();

    public static void log(String className,
                    String elementName, // Method or Field name
                    String[] parameterNames,
                    Object[] parameterValues) {
        for (SignatureVisitor visitor : visitors) {
            visitor.visit(className, elementName, parameterNames, parameterValues);
        }
    }

    public static void accept(SignatureVisitor visitor) {
        visitors.add(visitor);
    }

}
