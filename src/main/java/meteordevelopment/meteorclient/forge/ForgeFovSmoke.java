package meteordevelopment.meteorclient.forge;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Opt-in world regression; never invoked during ordinary gameplay. */
final class ForgeFovSmoke {
    private ForgeFovSmoke() {}

    static void run() {
        try {
            Method fov = method("getFov", "m_109141_");
            Field panorama = field("renderingPanorama", "f_109076_");
            boolean original = panorama.getBoolean(MeteorClient.mc.gameRenderer);
            try {
                panorama.setBoolean(MeteorClient.mc.gameRenderer, false);
                verify(fov, "normal");
                panorama.setBoolean(MeteorClient.mc.gameRenderer, true);
                verify(fov, "panorama");
            } finally {
                panorama.setBoolean(MeteorClient.mc.gameRenderer, original);
            }
            MeteorClient.LOG.info("REGRESSION_FOV_PASS normal=true panorama=true eventListenerRemoved=true panoramaStateRestored=true");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("FOV runtime regression failed", e);
        }
    }

    private static void verify(Method method, String mode) throws ReflectiveOperationException {
        double baseline = invoke(method);
        Probe probe = new Probe();
        double changed;
        MeteorClient.EVENT_BUS.subscribe(probe);
        try {
            changed = invoke(method);
        } finally {
            MeteorClient.EVENT_BUS.unsubscribe(probe);
        }
        double restored = invoke(method);
        boolean allowExternalOverride = Boolean.getBoolean("meteor.smoke.allowExternalFovOverride");
        boolean unchanged = Math.abs(changed - baseline) < 0.01;
        if (probe.calls != 1 || !Double.isFinite(changed) || (!allowExternalOverride && unchanged)
            || Math.abs(restored - baseline) > 0.00001) {
            throw new IllegalStateException("FOV event failed mode=" + mode + " calls=" + probe.calls
                + " baseline=" + baseline + " changed=" + changed + " restored=" + restored);
        }
        MeteorClient.LOG.info("REGRESSION_FOV mode={} calls={} baseline={} changed={} restored={} allowExternalOverride={} returnOverridden={}", mode, probe.calls, baseline, changed, restored, allowExternalOverride, unchanged);
    }

    private static double invoke(Method method) throws ReflectiveOperationException {
        return (double) method.invoke(MeteorClient.mc.gameRenderer, MeteorClient.mc.gameRenderer.getCamera(), 1.0f, true);
    }

    private static Method method(String... names) throws NoSuchMethodException {
        for (String name : names) {
            try {
                Method method = GameRenderer.class.getDeclaredMethod(name, Camera.class, float.class, boolean.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException("Canonical getFov");
    }

    private static Field field(String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                Field field = GameRenderer.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {}
        }
        throw new NoSuchFieldException("Panorama state");
    }

    private static final class Probe {
        int calls;

        @EventHandler(priority = Integer.MIN_VALUE)
        private void onFov(GetFovEvent event) {
            calls++;
            event.fov = 53.25;
        }
    }
}
