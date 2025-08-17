package dev.instellar.stellar.configuration;

import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Comment;
import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Setting;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.Configuration;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.PaperConfigurations;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class WorldConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final int CURRENT_VERSION = 1;

    private final transient ResourceLocation worldKey;

    WorldConfiguration(final ResourceLocation worldKey) {
        this.worldKey = worldKey;
    }

    public boolean isDefault() {
        return this.worldKey.equals(PaperConfigurations.WORLD_DEFAULTS_KEY);
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Players players;

    public class Players extends ConfigurationPart {

        @Comment("""
        Configurates tracking statistics that count time spent for an action (i.e. time played or sneak time)
        are updated every X ticks. With an interval of 20, reduces roughly 3ms per tick on a server w/ 80 players.""")
        public int intervalToTickActionStatistics = 20;

    }

}
