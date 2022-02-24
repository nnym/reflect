package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.util.stream.Stream;

/**
 Utilities for opening modules and getting modules and layers defined to class loaders.
 */
public class Modules {
    /**
     A special module that represents all modules.
     */
    public static final Module everyone = Accessor.getReference(Module.class, "EVERYONE_MODULE");

    private static final Pointer name = Pointer.of(Module.class, "name");
    private static final MethodHandle implAddExportsOrOpens = Invoker.findSpecial(Module.class, "implAddExportsOrOpens", void.class, String.class, Module.class, boolean.class, boolean.class);
    private static final MethodHandle layers = Invoker.findStatic(ModuleLayer.class, "layers", Stream.class, ClassLoader.class);

    /**
     Open a module's package to all modules.

     @param module a module
     @param pkg    a package in the module to open
     */
    public static void open(Module module, String pkg) {
        implAddExportsOrOpens.invoke(module, pkg, everyone, true, true);
    }

    /**
     Open the package of a type to all modules.

     @param type a type whose package to open
     */
    public static void open(Class<?> type) {
        open(type.getModule(), type.getPackageName());
    }

    /**
     Open all packages in a module to all modules.

     @since 4.6.0
     */
    public static void open(Module module) {
        module.getPackages().forEach(pkg -> open(module, pkg));
    }

    /**
     {@linkplain #open(Module) Open} the modules in a {@linkplain ModuleLayer module layer} to all modules.

     @since 4.6.0
     */
    public static void open(ModuleLayer layer) {
        layer.modules().forEach(Modules::open);
    }

    /**
     {@linkplain #open(ModuleLayer) Open} the module layers defined to a class loader to all modules.

     @since 4.6.0
     */
    public static void open(ClassLoader loader) {
        layers(loader).forEach(Modules::open);
    }

    /**
     Make {@code module} unnamed; thereby opening it to all unnamed modules.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.

     @see #open(Module)
     */
    @Deprecated(since = "4.6.0")
    public static void anonymize(Module module) {
        name.putReference(module, null);
    }

    /**
     Open the package of a class referenced by name and loaded by {@link Reflect#defaultClassLoader} to all modules.

     @param type the name of a class in the package
     @deprecated because this method is unnecessary: let the client load the class.
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static void open(String type) {
        open(Classes.load(type));
    }

    /**
     Make the modules in {@code layer} unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.

     @deprecated because this method is too dangerous to be useful; consider {@linkplain #open(Module) the new bulk open methods}.
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static void anonymize(ModuleLayer layer) {
        layer.modules().forEach(Modules::anonymize);
    }

    /**
     Make the modules in {@code layer} and its parents unnamed recursively.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.

     @deprecated because this method is too dangerous to be useful.
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static void anonymizeAll(ModuleLayer layer) {
        anonymize(layer);
        layer.parents().forEach(Modules::anonymizeAll);
    }

    /**
     Make all modules defined to {@code loader} unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.

     @deprecated because this method is too dangerous to be useful.
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static void anonymize(ClassLoader loader) {
        layers(loader).forEach(Modules::anonymize);
    }

    /**
     Make all modules in module layers defined to {@code loader} and their parents unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.

     @deprecated because this method is too dangerous to be useful.
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static void anonymizeAll(ClassLoader loader) {
        layers(loader).forEach(Modules::anonymizeAll);
    }

    /**
     Make all modules in module layers defined to this class' loader and their parents unnamed.
     This method is unsafe (see {@link Module#getLayer()} and other methods that rely on {@link Module#isNamed()}); use it with <b>great</b> caution.

     @deprecated because this method is too dangerous to be useful.
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static void anonymizeAll() {
        anonymizeAll(Modules.class.getClassLoader());
    }

    /**
     Return a stream of the module layers that are defined to a class loader.

     @return the module layers defined to the class loader
     */
    public static Stream<ModuleLayer> layers(ClassLoader loader) {
        return (Stream<ModuleLayer>) layers.invokeExact(loader);
    }

    /**
     Return a stream of a module layer and its ancestors.

     @return a stream of the module layer and its ancestors
     */
    public static Stream<ModuleLayer> allLayers(ModuleLayer leaf) {
        var parents = leaf.parents();
        var layers = Stream.of(leaf);
        return parents.isEmpty() ? layers : Stream.concat(layers, parents.stream().flatMap(Modules::allLayers));
    }

    /**
     Return a stream of the module layers that are defined to a class loader and their ancestors.

     @return a stream of the module layers defined to the class loader and their ancestors
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static Stream<ModuleLayer> allLayers(ClassLoader loader) {
        return layers(loader).flatMap(Modules::allLayers);
    }

    /**
     Return a stream of the modules that are defined to a class loader.

     @return a stream of the modules that are defined to the class loader
     */
    public static Stream<Module> modules(ClassLoader loader) {
        return layers(loader).flatMap(layer -> layer.modules().stream());
    }

    /**
     Return a stream of the modules that are defined to a class loader and those in their layers' ancestors.

     @return a stream of the modules that are defined to the class loader and those in their layers' ancestors
     */
    @Deprecated(since = "4.6.0", forRemoval = true)
    public static Stream<Module> allModules(ClassLoader loader) {
        return allLayers(loader).flatMap(layer -> layer.modules().stream());
    }
}
