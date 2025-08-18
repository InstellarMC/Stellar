package dev.instellar.stellar.configuration;

import com.mohistmc.io.leangen.geantyref.TypeToken;
import com.mohistmc.org.spongepowered.configurate.CommentedConfigurationNode;
import com.mohistmc.org.spongepowered.configurate.ConfigurateException;
import com.mohistmc.org.spongepowered.configurate.ConfigurationNode;
import com.mohistmc.org.spongepowered.configurate.ConfigurationOptions;
import com.mohistmc.org.spongepowered.configurate.objectmapping.ObjectMapper;
import com.mohistmc.org.spongepowered.configurate.serialize.SerializationException;
import com.mohistmc.org.spongepowered.configurate.util.CheckedFunction;
import com.mohistmc.org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import com.mojang.logging.LogUtils;
import dev.instellar.stellar.configuration.serializer.StorageEngineSerializer;
import io.papermc.paper.configuration.*;
import io.papermc.paper.configuration.mapping.InnerClassFieldDiscoverer;
import io.papermc.paper.configuration.serializer.EnumValueSerializer;
import io.papermc.paper.configuration.serializer.StringRepresentableSerializer;
import io.papermc.paper.configuration.serializer.collections.FastutilMapSerializer;
import io.papermc.paper.configuration.serializer.collections.TableSerializer;
import io.papermc.paper.configuration.serializer.registry.RegistryHolderSerializer;
import io.papermc.paper.configuration.serializer.registry.RegistryValueSerializer;
import io.papermc.paper.configuration.type.DespawnRange;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

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

    private static final Function<ContextMap, String> WORLD_HEADER = map -> String.format("""
                    This is a world configuration file for Stellar.
                    This file may start empty but can be filled with settings to override ones in the %s/%s
                    
                    World: %s (%s)""",
            StellarConfigurations.CONFIG_DIR,
            StellarConfigurations.WORLD_DEFAULTS_CONFIG_FILE_NAME,
            map.require(Configurations.WORLD_NAME),
            map.require(Configurations.WORLD_KEY)
    );

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
        return options.serializers(builder -> builder
                .register(new EnumValueSerializer())
        );
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
        return options
                .header(GLOBAL_HEADER)
                .serializers(builder -> builder
                        .register(new StorageEngineSerializer())
                );
    }

    @Override
    public GlobalConfiguration initializeGlobalConfiguration(final RegistryAccess registryAccess) throws ConfigurateException {
        final var configuration = super.initializeGlobalConfiguration(registryAccess);
        GlobalConfiguration.set(configuration);
        return configuration;
    }

    @Override
    protected ContextMap.Builder createDefaultContextMap(final RegistryAccess registryAccess) {
        return super.createDefaultContextMap(registryAccess);
    }

    @Override
    protected ObjectMapper.Factory.Builder createWorldObjectMapperFactoryBuilder(final ContextMap contextMap) {
        final Map<Class<?>, Object> overrides = Map.of(WorldConfiguration.class, createWorldConfigInstance(contextMap));
        final var discoverer = new InnerClassFieldDiscoverer(overrides);

        return super.createWorldObjectMapperFactoryBuilder(contextMap)
                .addNodeResolver(new NestedSetting.Factory())
                .addDiscoverer(discoverer);
    }

    private static WorldConfiguration createWorldConfigInstance(final ContextMap contextMap) {
        return new WorldConfiguration(contextMap.require(Configurations.WORLD_KEY));
    }

    @Override
    protected GlobalConfiguration initializeGlobalConfiguration(final CheckedFunction<ConfigurationNode, GlobalConfiguration, SerializationException> creator) throws ConfigurateException {
        final Path configFile = this.globalFolder.resolve(this.globalConfigFileName);
        final YamlConfigurationLoader loader = this.createGlobalLoaderBuilder()
                .defaultOptions(this.applyObjectMapperFactory(this.createGlobalObjectMapperFactoryBuilder().build()))
                .path(configFile)
                .build();
        final ConfigurationNode node;
        if (Files.notExists(configFile)) {
            node = CommentedConfigurationNode.root(loader.defaultOptions());
            node.node(Configuration.VERSION_FIELD).raw(this.globalConfigVersion());
            GlobalConfiguration.isFirstStart = true;
        } else {
            node = loader.load();
            this.verifyGlobalConfigVersion(node);
        }
        this.applyGlobalConfigTransformations(node);
        final GlobalConfiguration instance = creator.apply(node);
        trySaveFileNode(loader, node, configFile.toString(), "Stellar", "");
        return instance;
    }

    @Override
    protected YamlConfigurationLoader.Builder createWorldConfigLoaderBuilder(final ContextMap contextMap) {
        final var access = contextMap.require(REGISTRY_ACCESS);
        return super.createWorldConfigLoaderBuilder(contextMap)
                .defaultOptions(options -> options
                        .header(contextMap.require(WORLD_NAME).equals(WORLD_DEFAULTS) ? WORLD_DEFAULTS_HEADER : WORLD_HEADER.apply(contextMap))
                        .serializers(serializers -> serializers
                                .register(new TypeToken<>() {
                                }, new FastutilMapSerializer.SomethingToPrimitive<Reference2IntMap<?>>(Reference2IntOpenHashMap::new, Integer.TYPE))
                                .register(new TypeToken<>() {
                                }, new FastutilMapSerializer.SomethingToPrimitive<Reference2LongMap<?>>(Reference2LongOpenHashMap::new, Long.TYPE))
                                .register(new TypeToken<>() {
                                }, new TableSerializer())
                                .register(DespawnRange.class, DespawnRange.SERIALIZER)
                                .register(StringRepresentableSerializer::isValidFor, new StringRepresentableSerializer())
                                .register(new RegistryValueSerializer<>(new TypeToken<>() {
                                }, access, Registries.ENTITY_TYPE, true))
                                .register(new RegistryValueSerializer<>(Item.class, access, Registries.ITEM, true))
                                .register(new RegistryValueSerializer<>(Block.class, access, Registries.BLOCK, true))
                                .register(new RegistryHolderSerializer<>(new TypeToken<>() {
                                }, access, Registries.CONFIGURED_FEATURE, false))
                        )
                );
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
            throw new RuntimeException("Could not reload stellar configuration files", ex);
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
            PaperConfigurations.createDirectoriesSymlinkAware(configDir);
            return new StellarConfigurations(configDir);
        } catch (final IOException e) {
            throw new RuntimeException("Could not setup StellarConfigurations", e);
        }
    }

    @Deprecated
    public YamlConfiguration createLegacyObject(final MinecraftServer server) {
        YamlConfiguration global = YamlConfiguration.loadConfiguration(this.globalFolder.resolve(this.globalConfigFileName).toFile());
        ConfigurationSection worlds = global.createSection("__________WORLDS__________");
        worlds.set("__defaults__", YamlConfiguration.loadConfiguration(this.globalFolder.resolve(this.defaultWorldConfigFileName).toFile()));
        for (ServerLevel level : server.getAllLevels()) {
            worlds.set(level.getWorld().getName(), YamlConfiguration.loadConfiguration(getWorldConfigFile(level).toFile()));
        }
        return global;
    }
}
