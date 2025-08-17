package dev.instellar.stellar.configuration;

import com.mohistmc.org.spongepowered.configurate.ConfigurateException;
import com.mohistmc.org.spongepowered.configurate.ConfigurationOptions;
import com.mohistmc.org.spongepowered.configurate.objectmapping.ObjectMapper;
import com.mohistmc.org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import com.mojang.logging.LogUtils;
import io.papermc.paper.configuration.ConfigurationPart;
import io.papermc.paper.configuration.Configurations;
import io.papermc.paper.configuration.mapping.InnerClassFieldDiscoverer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.mohistmc.io.leangen.geantyref.GenericTypeReflector.erase;

@NullMarked
public final class StellarConfigurations extends Configurations<GlobalConfiguration, WorldConfiguration> {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final String GLOBAL_CONFIG_FILE_NAME = "stellar-global.yml";
    static final String WORLD_DEFAULTS_CONFIG_FILE_NAME = "stellar-world-defaults.yml";
    static final String WORLD_CONFIG_FILE_NAME = "stellar-world.yml";
    public static final String CONFIG_DIR = "paper-config"; // intentional dirname
    public static final String PROPERTY_CONFIG_DIR = "stellar-settings-directory";
    public static final String PROPERTY_CONFIG_PATH = "stellar-settings";

    private static final String GLOBAL_HEADER = """
        This is the global configuration file for Stellar.
        As you can see, there's a lot to configure. Some options may impact gameplay, so use
        with caution, and make sure you know what each option does before configuring.
        
        If you need help with the configuration or have any questions related to Stellar,
        join us in our Discord.
        
        The world configuration options have been moved inside
        their respective world folder. The files are named %s
        
        Discord: https://instellar.dev/discord""".formatted(WORLD_CONFIG_FILE_NAME);

    private static final String WORLD_DEFAULTS_HEADER = """
        This is the world defaults configuration file for Stellar.
        As you can see, there's a lot to configure. Some options may impact gameplay, so use
        with caution, and make sure you know what each option does before configuring.
        
        If you need help with the configuration or have any questions related to Stellar,
        join us in our Discord.
        
        Configuration options here apply to all worlds, unless you specify overrides inside
        the world-specific config file inside each world folder.
        
        Discord: https://instellar.dev/discord""";

    public StellarConfigurations(final Path globalDirectory) {
        super(globalDirectory, GlobalConfiguration.class, WorldConfiguration.class, GLOBAL_CONFIG_FILE_NAME, WORLD_DEFAULTS_CONFIG_FILE_NAME, WORLD_CONFIG_FILE_NAME);
    }

    @Override
    protected int globalConfigVersion() {
        return GlobalConfiguration.CURRENT_VERSION;
    }

    @Override
    protected int worldConfigVersion() {
        return WorldConfiguration.CURRENT_VERSION;
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder()
                .defaultOptions(StellarConfigurations::defaultOptions);
    }

    private static ConfigurationOptions defaultOptions(final ConfigurationOptions options) {
        return options;
    }

    @Override
    protected ObjectMapper.Factory.Builder createGlobalObjectMapperFactoryBuilder() {
        return defaultGlobalFactoryBuilder(super.createGlobalObjectMapperFactoryBuilder());
    }

    private static ObjectMapper.Factory.Builder defaultGlobalFactoryBuilder(final ObjectMapper.Factory.Builder builder) {
        return builder.addDiscoverer(InnerClassFieldDiscoverer.globalConfig());
    }

    @Override
    protected YamlConfigurationLoader.Builder createGlobalLoaderBuilder() {
        return super.createGlobalLoaderBuilder()
                .defaultOptions(StellarConfigurations::defaultGlobalOptions);
    }

    private static ConfigurationOptions defaultGlobalOptions(final ConfigurationOptions options) {
        return options.header(GLOBAL_HEADER);
    }

    @Override
    public GlobalConfiguration initializeGlobalConfiguration(final RegistryAccess registryAccess) throws ConfigurateException {
        final var configuration = super.initializeGlobalConfiguration(registryAccess);
        GlobalConfiguration.set(configuration);
        return configuration;
    }

    @Override
    public WorldConfiguration createWorldConfig(final ContextMap contextMap) {
        final String levelName = contextMap.require(WORLD_NAME);
        try {
            return super.createWorldConfig(contextMap);
        } catch (IOException exception) {
            throw new RuntimeException("Could not create world config for " + levelName, exception);
        }
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(erase(type));
    }

    public void reloadConfigs(final MinecraftServer server) {
        try {
            this.initializeGlobalConfiguration(reloader(this.globalConfigClass, GlobalConfiguration.get()));
            this.initializeWorldDefaultsConfiguration(server.registryAccess());
            for (final var level : server.getAllLevels()) {
                this.createWorldConfig(createWorldContextMap(level), reloader(this.worldConfigClass, level.stellarConfig()));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not reload paper configuration files", ex);
        }
    }

    private static ContextMap createWorldContextMap(final ServerLevel level) {
        return createWorldContextMap(level.convertable.levelDirectory.path(), level.serverLevelData.getLevelName(), level.dimension().location(), level.registryAccess(), level.getGameRules());
    }

    public static ContextMap createWorldContextMap(final Path dir, final String levelName, final ResourceLocation worldKey, final RegistryAccess registryAccess, final GameRules gameRules) {
        return ContextMap.builder()
                .put(WORLD_DIRECTORY, dir)
                .put(WORLD_NAME, levelName)
                .put(WORLD_KEY, worldKey)
                .put(REGISTRY_ACCESS, registryAccess)
                .put(GAME_RULES, gameRules)
                .build();
    }

    public static StellarConfigurations setup(final Path configDir) throws Exception {
        try {
            createDirectoriesSymlinkAware(configDir);
            return new StellarConfigurations(configDir);
        } catch (final IOException e) {
            throw new RuntimeException("Could not setup StellarConfigurations", e);
        }
    }

    // Symlinks are not correctly checked in createDirectories
    static void createDirectoriesSymlinkAware(final Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);
        }
    }

}
