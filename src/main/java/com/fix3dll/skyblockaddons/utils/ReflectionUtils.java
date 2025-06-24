package com.fix3dll.skyblockaddons.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ReflectionUtils {

    public static List<Class<?>> scanPackage(String pkg, ClassLoader loader, Class<?> superType)
            throws IOException, URISyntaxException {

        String prefix = pkg.replace('.', '/') + "/";
        List<Class<?>> out = new ArrayList<>();

        Enumeration<URL> roots = loader.getResources(prefix);
        while (roots.hasMoreElements()) {
            URL url = roots.nextElement();
            switch (url.getProtocol()) {
                case "file" -> {
                    Path dir = Paths.get(url.toURI());
                    try (Stream<Path> stream = Files.walk(dir)) {
                        stream.filter(p -> p.toString().endsWith(".class"))
                                .forEach(p -> add(out, pkg, dir.relativize(p).toString(), loader, superType));
                    }
                }
                case "jar" -> {
                    String u = url.toURI().toString();
                    URI jarUri = URI.create(u.substring(0, u.indexOf('!')));

                    FileSystem fs;
                    boolean created = false;
                    try {
                        fs = FileSystems.getFileSystem(jarUri);
                    } catch (FileSystemNotFoundException ex) {
                        fs = FileSystems.newFileSystem(jarUri, Map.of());
                        created = true;
                    }

                    try {
                        Path dir = fs.getPath("/" + prefix);
                        if (Files.exists(dir)) {
                            try (Stream<Path> stream = Files.walk(dir)) {
                                stream.filter(p -> p.toString().endsWith(".class"))
                                        .forEach(p -> add(out, pkg, dir.relativize(p).toString(), loader, superType));
                            }
                        }
                    } finally {
                        if (created) fs.close();
                    }
                }
            }
        }
        return out;
    }

    private static void add(List<Class<?>> sink,
                            String rootPkg,
                            String relPath,
                            ClassLoader cl,
                            Class<?> superType) {
        String name = (rootPkg + '.' + relPath)
                .replace(File.separatorChar, '.')
                .replace('/', '.')
                .replaceAll("\\.class$", "");

        try {
            Class<?> cls = Class.forName(name, false, cl);
            if (superType == null || superType.isAssignableFrom(cls)) {
                sink.add(cls);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError | IncompatibleClassChangeError ignored) {
        }
    }

}