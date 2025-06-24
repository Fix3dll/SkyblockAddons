package com.fix3dll.skyblockaddons.utils;

public class ReflectionUtils {

    /**
     * Searches and returns the desired {@link Class} in the specified package, and it's subpackages.
     * @param className desired class name
     * @param basePackage base package to start searching
     * @return desired {@link Class}
     * @author Fix3dll
     */
    public static Class<?> getClassFromSubpackages(String className, String basePackage) {
        return getClassFromSubpackages(className, "", basePackage);
    }

    /**
     * Searches and returns the desired {@link Class} in the specified package, and it's subpackages.
     * @param className desired class name
     * @param subclassName if the desired Class is a subclass, its name
     * @param basePackage base package to start searching
     * @return desired {@link Class}
     * @author Fix3dll
     */
    public static Class<?> getClassFromSubpackages(String className, String subclassName, String basePackage) {
        Package[] packages = Package.getPackages(); // "net.minecraft.entity"

        for (Package pkg : packages) {
            if (pkg.getName().startsWith(basePackage)) {
                try {
                    Class<?> foundClass;
                    if (subclassName.isBlank()) {
                        foundClass = Class.forName(pkg.getName() + "." + className);
                    } else {
                        foundClass = Class.forName(pkg.getName() + "." + className + "$" + subclassName);
                    }
                    return foundClass;
                } catch (ClassNotFoundException ignored) {}
            }
        }

        return null;
    }

}
