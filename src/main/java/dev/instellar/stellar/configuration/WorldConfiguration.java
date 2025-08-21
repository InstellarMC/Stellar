package dev.instellar.stellar.configuration;

import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Comment;
import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Setting;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.Configuration;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.Configurations;
import io.papermc.paper.configuration.type.number.IntOr;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class WorldConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final int CURRENT_VERSION = 1;

    private final transient ResourceLocation worldKey;

    WorldConfiguration(final ResourceLocation worldKey) {
        this.worldKey = worldKey;
    }

    public boolean isDefault() {
        return this.worldKey.equals(Configurations.WORLD_DEFAULTS_KEY);
    }

    @Setting(Configuration.VERSION_FIELD)
    public int version = CURRENT_VERSION;

    public Players players;

    public class Players extends ConfigurationPart {

        @Comment("Configurates whether players can have infinite saturation.")
        public boolean infiniteSaturation = false;

        @Comment("""
        Configurates tracking statistics that count time spent for an action (i.e. time played or sneak time)
        are updated every X ticks. With an interval of 20, reduces roughly 3ms per tick on a server w/ 80 players.""")
        public int intervalToTickActionStatistics = 20;

    }

    public Chunks chunks;
    public class Chunks extends ConfigurationPart {
        @Comment("""
                Instead of running random ticking once every tick,
                you can run it once every *n* ticks, but when randomly
                ticked chunks wil lbe ticked *n* times more.
                
                While this does affect vanilla behaviour, the nature of
                random ticking is that this effect is barely noticeable,
                but it can have a significant impact on TPS.
                """)
        public IntOr.Disabled randomTickBatching = IntOr.Disabled.DISABLED;
    }

}
