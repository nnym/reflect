package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.util.stream.Stream;

/**
 Utilities for opening modules to all unnamed modules and getting the module layers defined to class loaders.
 */
public class Modules {
    private static final MethodHandle implAddOpensToAllUnnamed = Invoker.findVirtual(Module.class, "implAddOpensToAllUnnamed", void.class, String.class);
    private static final MethodHandle layers = Invoker.findStatic(ModuleLayer.class, "layers", Stream.class, ClassLoader.class);
    private static final Pointer name = new Pointer().instanceField(Module.class, "name");

    /** Open package {@code pkg} in module {@code module} to all unnamed modules. */
    public static void open(Module module, String pkg) {
        Invoker.invoke(implAddOpensToAllUnnamed, module, pkg);
    }

    /** Open the package of the class named by {@code type} and loaded by {@link Reflect#defaultClassLoader}. */
    public static void open(String type) {
        open(Classes.load(type));
    }

    /** Open the package of {@code type}. */
    public static void open(Class<?> type) {
        open(type.getModule(), type.getPackageName());
    }

    /**
     Make {@code module} unnamed, thereby opening it to all unnamed modules.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.
     */
    public static void anonymize(Module module) {
        name.putReference(module, null);
    }

    /**
     Make the modules in {@code layer} unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.
     */
    public static void anonymize(ModuleLayer layer) {
        layer.modules().forEach(Modules::anonymize);
    }

    /**
     Make the modules in {@code layer} and its parents unnamed recursively.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.
     */
    public static void anonymizeAll(ModuleLayer layer) {
        anonymize(layer);
        layer.parents().forEach(Modules::anonymizeAll);
    }

    /**
     Make all modules defined to {@code loader} unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.
     */
    public static void anonymize(ClassLoader loader) {
        layers(loader).forEach(Modules::anonymize);
    }

    /**
     Make all modules in module layers defined to {@code loader} and their parents unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.
     */
    public static void anonymizeAll(ClassLoader loader) {
        layers(loader).forEach(Modules::anonymizeAll);
    }

    /**
     Make all modules in module layers defined to this class' loader and their parents unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.
     */
    public static void anonymizeAll() {
        anonymizeAll(Modules.class.getClassLoader());
    }

    public static Stream<ModuleLayer> layers(ClassLoader loader) {
        return Invoker.invoke(layers, loader);
    }

    public static Stream<ModuleLayer> allLayers(ModuleLayer layer) {
        var parents = layer.parents();
        var layers = Stream.of(layer);
        return parents.isEmpty() ? layers : Stream.concat(layers, parents.stream());
    }

    public static Stream<ModuleLayer> allLayers(ClassLoader loader) {
        return layers(loader).flatMap(Modules::allLayers);
    }

    public static Stream<Module> modules(ClassLoader loader) {
        return layers(loader).flatMap(layer -> layer.modules().stream());
    }

    public static Stream<Module> allModules(ClassLoader loader) {
        return allLayers(loader).flatMap(layer -> layer.modules().stream());
    }
}
