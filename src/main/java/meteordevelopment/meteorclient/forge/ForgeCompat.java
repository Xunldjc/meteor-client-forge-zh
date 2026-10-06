package meteordevelopment.meteorclient.forge;

public final class ForgeCompat {
    private ForgeCompat() {}

    public static boolean shadersEnabled() {
        for (String name : new String[] {"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"}) {
            try {
                Class<?> api = Class.forName(name);
                Object instance = api.getMethod("getInstance").invoke(null);
                return (boolean) api.getMethod("isShaderPackInUse").invoke(instance);
            } catch (ClassNotFoundException ignored) {
                // Oculus versions use different package names.
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot query shader state", e);
            }
        }
        return false;
    }
}
