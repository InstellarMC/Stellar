package io.papermc.paper;

import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.plugin.entrypoint.classloader.group.PaperPluginClassLoaderStorage;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage;
import io.papermc.paper.util.MCUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.lucko.spark.paper.api.Compatibility;
import me.lucko.spark.paper.api.PaperClassLookup;
import me.lucko.spark.paper.api.PaperScheduler;
import me.lucko.spark.paper.api.PaperSparkModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.util.ExceptionCollector;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;

// It's like electricity.
public final class SparksFly {
    public static final String ID = "spark";
    public static final String COMMAND_NAME = "spark";

    private static final String PREFER_SPARK_PLUGIN_PROPERTY = "paper.preferSparkPlugin";

    private static final int SPARK_YELLOW = 0xffc93a;

    private final Logger logger;
    private PaperSparkModule spark; // Stellar

    private boolean enabled;
    private boolean disabledInConfigurationWarningLogged;

    public SparksFly(final Server server) {
        this.logger = Logger.getLogger(ID);
        this.logger.log(Level.INFO, "This server bundles the spark profiler. For more information please visit https://docs.papermc.io/paper/profiling");
        // Stellar start
        boolean flag = false;
        try {
            Class.forName(PaperSparkModule.class.getName(), true, this.getClass().getClassLoader()); // ensure class is loaded before we delegate to another classloader
            final var method = PaperSparkModule.class.getMethod("create", Compatibility.class, Server.class, Logger.class, PaperScheduler.class, PaperClassLookup.class);
            final var instance = method.invoke(null, Compatibility.VERSION_1_0, server, this.logger, new PaperScheduler() {
                @Override
                public void executeAsync(final Runnable runnable) {
                    MCUtil.scheduleAsyncTask(this.catching(runnable, "asynchronous"));
                }

                @Override
                public void executeSync(final Runnable runnable) {
                    MCUtil.ensureMain(this.catching(runnable, "synchronous"));
                }

                private Runnable catching(final Runnable runnable, final String type) {
                    return () -> {
                        try {
                            runnable.run();
                        } catch (final Throwable t) {
                            SparksFly.this.logger.log(Level.SEVERE, "An exception was encountered while executing a " + type + " spark task", t);
                        }
                    };
                }
            }, new PaperClassLookup() {
                @Override
                public Class<?> lookup(final String className) throws Exception {
                    final ExceptionCollector<ClassNotFoundException> exceptions = new ExceptionCollector<>();
                    try {
                        return Class.forName(className);
                    } catch (final ClassNotFoundException e) {
                        exceptions.add(e);
                        for (final ConfiguredPluginClassLoader loader : ((PaperPluginClassLoaderStorage) PaperClassLoaderStorage.instance()).getGlobalGroup().getClassLoaders()) {
                            try {
                                final Class<?> loadedClass = loader.loadClass(className, true, false, true);
                                if (loadedClass != null) {
                                    return loadedClass;
                                }
                            } catch (final ClassNotFoundException exception) {
                                exceptions.add(exception);
                            }
                        }
                        exceptions.throwIfPresent();
                        return null;
                    }
                }
            });

            if (instance instanceof PaperSparkModule) {
                this.spark = (PaperSparkModule) instance;
                flag = true;
            }
        } catch (final ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ignored) {
            flag = false;
        }

        if (!flag) {
            this.spark = new PaperSparkModule() {
                @Override
                public void enable() {
                    // no-op
                }

                @Override
                public void disable() {
                    // no-op
                }

                @Override
                public Collection<String> getPermissions() {
                    return List.of();
                }

                @Override
                public void onServerTickStart() {
                    // no-op
                }

                @Override
                public void onServerTickEnd(final double duration) {
                    // no-op
                }

                @Override
                public void executeCommand(final CommandSender sender, final String[] args) {
                    sender.sendMessage(Component.text("The spark profiler is not available. Please install the spark plugin from https://lucko.me/projects/spark/", TextColor.color(SPARK_YELLOW)));
                }

                @Override
                public List<String> tabComplete(final CommandSender sender, final String[] args) {
                    return List.of();
                }

                @Override
                public boolean hasPermission(CommandSender commandSender) {
                    return false;
                }
            };
        }
        // Stellar end
    }

    public void enableEarlyIfRequested() {
        if (!isPluginPreferred() && shouldEnableImmediately()) {
            this.enable();
        }
    }

    public void enableBeforePlugins() {
        if (!isPluginPreferred()) {
            this.enable();
        }
    }

    public void enableAfterPlugins(final Server server) {
        final boolean isPluginPreferred = isPluginPreferred();
        final boolean isPluginEnabled = isPluginEnabled(server);
        if (!isPluginPreferred || !isPluginEnabled) {
            if (isPluginPreferred && !this.enabled) {
                this.logger.log(Level.INFO, "The spark plugin has been preferred but was not loaded. The bundled spark profiler will enabled instead.");
            }
            this.enable();
        }
    }

    private void enable() {
        this.enabled = false;
        /* Stellar start - fixme
        if (!this.enabled) {
            if (GlobalConfiguration.get().spark.enabled) {
                this.enabled = true;
                this.spark.enable();
            } else {
                if (!this.disabledInConfigurationWarningLogged) {
                    this.logger.log(Level.INFO, "The spark profiler will not be enabled because it is currently disabled in the configuration.");
                    this.disabledInConfigurationWarningLogged = true;
                }
            }
        }
        */// Stellar end
    }

    public void disable() {
        if (this.enabled) {
            this.spark.disable();
            this.enabled = false;
        }
    }

    public void registerCommandBeforePlugins(final Server server) {
        if (!isPluginPreferred()) {
            this.registerCommand(server);
        }
    }

    public void registerCommandAfterPlugins(final Server server) {
        if ((!isPluginPreferred() || !isPluginEnabled(server)) && server.getCommandMap().getCommand(COMMAND_NAME) == null) {
            this.registerCommand(server);
        }
    }

    private void registerCommand(final Server server) {
        server.getCommandMap().register(COMMAND_NAME, "paper", new CommandImpl(COMMAND_NAME, this.spark.getPermissions()));
    }

    public void tickStart() {
        this.spark.onServerTickStart();
    }

    public void tickEnd(final double duration) {
        this.spark.onServerTickEnd(duration);
    }

    void executeCommand(final CommandSender sender, final String[] args) {
        this.spark.executeCommand(sender, args);
    }

    List<String> tabComplete(final CommandSender sender, final String[] args) {
        return this.spark.tabComplete(sender, args);
    }

    public static boolean isPluginPreferred() {
        return Boolean.getBoolean(PREFER_SPARK_PLUGIN_PROPERTY);
    }

    private static boolean isPluginEnabled(final Server server) {
        return server.getPluginManager().isPluginEnabled(ID);
    }

    private static boolean shouldEnableImmediately() {
        return GlobalConfiguration.get().spark.enableImmediately;
    }

    public static final class CommandImpl extends Command {
        CommandImpl(final String name, final Collection<String> permissions) {
            super(name);
            this.setPermission(String.join(";", permissions));
        }

        @Override
        public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
            final SparksFly spark = ((CraftServer) sender.getServer()).spark;
            if (spark.enabled) {
                spark.executeCommand(sender, args);
            } else {
                sender.sendMessage(Component.text("The spark profiler is currently disabled.", TextColor.color(SPARK_YELLOW)));
            }
            return true;
        }

        @Override
        public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws IllegalArgumentException {
            final SparksFly spark = ((CraftServer) sender.getServer()).spark;
            if (spark.enabled) {
                return spark.tabComplete(sender, args);
            }
            return List.of();
        }
    }
}
