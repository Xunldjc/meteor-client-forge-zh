package meteordevelopment.meteorclient.forge;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ForgeMetadata {
    private final Properties values = new Properties();

    public ForgeMetadata() {
        try (InputStream stream = getClass().getResourceAsStream("/meteor-build.properties")) {
            if (stream == null) throw new IllegalStateException("Missing meteor-build.properties");
            values.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load build metadata", e);
        }
    }

    public String getName() { return values.getProperty("name"); }
    public String getVersion() { return values.getProperty("version"); }
    public String getCommit() { return values.getProperty("commit", ""); }
}
