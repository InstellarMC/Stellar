package dev.instellar.stellar.configuration;

import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Comment;
import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.PostProcess;
import com.mohistmc.org.spongepowered.configurate.objectmapping.meta.Setting;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.NestedSetting;
import io.papermc.paper.configuration.constraint.Constraints;
import io.papermc.paper.configuration.type.number.IntOr;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.slf4j.Logger;

import java.util.Map;

public final class GlobalConfiguration extends ConfigurationPart {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final int CURRENT_VERSION = 1;
    private static GlobalConfiguration INSTANCE;
    public static boolean isFirstStart = false;
    public static GlobalConfiguration get() {
        return INSTANCE;
    }
    static void set(final GlobalConfiguration configuration) {
        INSTANCE = configuration;
    }

    public Players players;

    public class Players extends ConfigurationPart {

        public boolean useAsync = false;

    }

    public Items items;

    public class Items extends ConfigurationPart {

        @Comment("Configurates interval in ticks at which the Map item update task runs.")
        @Constraints.Min(1)
        @NestedSetting({"map", "update-interval"})
        public IntOr.Default mapUpdateInterval = IntOr.Default.USE_DEFAULT;
    }

    public SpamLimiter spamLimiter;

    public class SpamLimiter extends ConfigurationPart {

        @Constraints.Min(1)
        public int chatSpamIncrement = 1;
        @Constraints.Min(5)
        public int chatSpamLimit = 20;
        @Constraints.Min(1)
        public int dropSpamIncrement = 1;
        @Constraints.Min(5)
        public int dropSpamLimit = 20;
    }

    @Setting("world-generation")
    public WorldGeneration levelgen;

    public class WorldGeneration extends ConfigurationPart {

        @Comment("If enabled, the server will use Noisium for world generation instead of the default Minecraft world generator.")
        public boolean useNoisiumWorldGen = false;

        @PostProcess
        private void postProcess() {
            NoiseBasedChunkGenerator.USE_NOISIUM_WORLDGEN = this.useNoisiumWorldGen;
        }
    }

    public SQL sql;

    public class SQL extends ConfigurationPart {

        public boolean enabled = false;

        @Setting("use-as-player-io")
        public boolean useAsPlayerIO = true;

        public String defaultJdbcConnectionUri = "jdbc:h2:steller.db";

        public Map<String, String> aliases = Map.of(
                //"sqlite", "org.sqlite.JDBC",
                "mysql", "com.mysql.cj.jdbc.Driver",
                //"mariadb", "org.mariadb.jdbc.Driver",
                //"postgresql", "org.postgresql.Driver",
                "h2", "org.h2.Driver"
        );
    }

    public Performance performance;

    public class Performance extends ConfigurationPart {

        public boolean asyncPlayerJoin = false;
        public int updateEntityLineOfSight = 4;
        @Setting("use-vt-for-user-authentication")
        public boolean useVT4UserAuthentication = false;
        @Setting("use-vt-for-chat-executor")
        public boolean useVT4ChatExecutor = false;
        public boolean turboStructureGeneratingSequence = false;
    }

    public Compatibility compatibility;

    public class Compatibility extends ConfigurationPart {

        @Comment("If enabled, the server will not play Pixelmon death sounds.")
        public boolean ignorePixelmonDeathSound = true;

        @Comment("If enabled, the server will convert old playerdata to the new format.")
        public boolean convertOldUsers = false;
    }

    public Verbosity verbosity;

    public class Verbosity extends ConfigurationPart {

        public boolean offlineMode = false;
    }
}
