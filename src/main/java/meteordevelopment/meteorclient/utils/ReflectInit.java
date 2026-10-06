/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import net.minecraftforge.fml.ModList;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ReflectInit {
    private static final List<Set<Method>> initializers = new ArrayList<>();

    public static void registerPackages() {
        add(MeteorClient.ADDON);

        for (MeteorAddon addon : AddonManager.ADDONS) {
            try {
                add(addon);
            } catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        }
    }

    private static void add(MeteorAddon addon) {
        String pkg = addon.getPackage();
        if (pkg == null || pkg.isBlank()) return;
        Set<Method> methods = new HashSet<>();
        Set<String> classes = new HashSet<>();
        // Forge scans mod archives and development source sets before construction.
        for (var data : ModList.get().getAllScanData()) {
            for (var annotation : data.getAnnotations()) {
                String type = annotation.annotationType().getClassName();
                String name = annotation.clazz().getClassName();
                if ((type.equals(PreInit.class.getName()) || type.equals(PostInit.class.getName()))
                    && name.startsWith(pkg + ".")) classes.add(name);
            }
        }
        for (String name : classes) {
            try {
                for (Method method : Class.forName(name, false, ReflectInit.class.getClassLoader()).getDeclaredMethods()) {
                    if (method.isAnnotationPresent(PreInit.class) || method.isAnnotationPresent(PostInit.class)) methods.add(method);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load initializer " + name, e);
            }
        }
        if (addon == MeteorClient.ADDON && methods.isEmpty()) throw new IllegalStateException("Forge found no Meteor initializers");
        initializers.add(methods);
        MeteorClient.LOG.info("Forge initializer scan: package={} methods={}", pkg, methods.size());
    }

    public static void init(Class<? extends Annotation> annotation) {
        for (Set<Method> methods : initializers) {
            Set<Method> initTasks = methods.stream().filter(method -> method.isAnnotationPresent(annotation)).collect(Collectors.toSet());

            Map<Class<?>, List<Method>> byClass = initTasks.stream().collect(Collectors.groupingBy(Method::getDeclaringClass));
            Set<Method> left = new HashSet<>(initTasks);

            for (Method m; (m = left.stream().findAny().orElse(null)) != null;) {
                reflectInit(m, annotation, left, byClass);
            }
        }
    }

    private static <T extends Annotation> void reflectInit(Method task, Class<T> annotation, Set<Method> left, Map<Class<?>, List<Method>> byClass) {
        left.remove(task);

        for (Class<?> clazz : getDependencies(task, annotation)) {
            for (Method m : byClass.getOrDefault(clazz, Collections.emptyList())) {
                if (left.contains(m)) {
                    reflectInit(m, annotation, left, byClass);
                }
            }
        }

        try {
            task.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Initializer failed: " + task, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Method \"%s\" using Init annotations from non-static context".formatted(task.getName()), e);
        }
    }

    private static <T extends Annotation> Class<?>[] getDependencies(Method task, Class<T> annotation) {
        T init = task.getAnnotation(annotation);

        if (init instanceof PreInit pre) {
            return pre.dependencies();
        }
        else if (init instanceof PostInit post) {
            return post.dependencies();
        }

        return new Class<?>[]{};
    }
}
