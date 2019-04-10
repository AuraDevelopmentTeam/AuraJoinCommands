package dev.aura.joincommands;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.aura.joincommands.command.CommandBase;
import dev.aura.joincommands.config.Config;
import dev.aura.joincommands.permission.PermissionRegistry;
import dev.aura.lib.messagestranslator.MessagesTranslator;
import dev.aura.lib.messagestranslator.PluginMessagesTranslator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.MetricsLite2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

@Plugin(
  id = AuraJoinCommands.ID,
  name = AuraJoinCommands.NAME,
  version = AuraJoinCommands.VERSION,
  description = AuraJoinCommands.DESCRIPTION,
  url = AuraJoinCommands.URL,
  authors = {AuraJoinCommands.AUTHOR_BRAINSTONE}
)
public class AuraJoinCommands {
  public static final String ID = "@id@";
  public static final String NAME = "@name@";
  public static final String VERSION = "@version@";
  public static final String DESCRIPTION = "@description@";
  public static final String URL = "https://github.com/AuraDevelopmentTeam/AuraJoinCommands";
  public static final String AUTHOR_BRAINSTONE = "The_BrainStone";

  @NonNull @Getter private static AuraJoinCommands instance = null;

  @Inject @NonNull protected PluginContainer container;
  @Inject protected MetricsLite2 metrics;
  @Inject @NonNull protected Logger logger;

  @Inject protected GuiceObjectMapperFactory factory;

  @Inject
  @DefaultConfig(sharedRoot = false)
  protected ConfigurationLoader<CommentedConfigurationNode> loader;

  @Inject
  @ConfigDir(sharedRoot = false)
  @NonNull
  protected Path configDir;

  @NonNull protected Config config;
  protected PermissionRegistry permissionRegistry;
  @NonNull protected MessagesTranslator translator;

  protected List<Object> eventListeners = new LinkedList<>();

  protected AuraJoinCommands() {
    if (instance != null) throw new IllegalStateException("Instance already exists!");

    instance = this;
  }

  public static Logger getLogger() {
    return instance.logger;
  }

  public static Config getConfig() {
    return instance.config;
  }

  public static Path getConfigDir() {
    return instance.configDir;
  }

  public static MessagesTranslator getTranslator() {
    return instance.translator;
  }

  @Listener
  public void init(GameInitializationEvent event) throws IOException, ObjectMappingException {
    logger.info("Initializing " + NAME + " Version " + VERSION);

    if (VERSION.contains("SNAPSHOT")) {
      logger.warn("WARNING! This is a snapshot version!");
      logger.warn("Use at your own risk!");
    }
    if (VERSION.contains("DEV")) {
      logger.info("This is a unreleased development version!");
      logger.info("Things might not work properly!");
    }

    loadConfig();

    if (permissionRegistry == null) {
      permissionRegistry = new PermissionRegistry(this);
      logger.debug("Registered permissions");
    }

    translator =
        new PluginMessagesTranslator(
            new File(getConfigDir().toFile(), "lang"), config.getGeneral().getLanguage(), this, ID);

    CommandBase.register(this);
    logger.debug("Registered commands");

    //    addEventListener(new PlayerEvents());
    logger.debug("Registered events");

    logger.info("Loaded successfully!");
  }

  private void loadConfig() throws IOException, ObjectMappingException {
    final TypeToken<Config> configToken = TypeToken.of(Config.class);

    logger.debug("Loading config...");

    CommentedConfigurationNode node =
        loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory));

    config = node.<Config>getValue(configToken, Config::new);

    logger.debug("Saving/Formatting config...");
    node.setValue(configToken, config);
    loader.save(node);
  }

  @Listener
  public void reload(GameReloadEvent event) throws Exception {
    Cause cause =
        Cause.builder()
            .append(this)
            .build(EventContext.builder().add(EventContextKeys.PLUGIN, container).build());

    // Unregistering everything
    GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
    stop(gameStoppingEvent);

    // Starting over
    GameInitializationEvent gameInitializationEvent =
        SpongeEventFactory.createGameInitializationEvent(cause);
    init(gameInitializationEvent);

    logger.info("Reloaded successfully!");
  }

  @Listener
  public void stop(GameStoppingEvent event) throws Exception {
    logger.info("Shutting down " + NAME + " Version " + VERSION);

    stopTasks();
    logger.debug("Stopped tasks");

    removeCommands();
    logger.debug("Unregistered commands");

    removeEventListeners();
    logger.debug("Unregistered events");

    //    config = null;
    logger.debug("Unloaded config");

    logger.info("Unloaded successfully!");
  }

  private void addEventListener(Object listener) {
    eventListeners.add(listener);

    Sponge.getEventManager().registerListeners(this, listener);
  }

  private void removeCommands() {
    final CommandManager commandManager = Sponge.getCommandManager();

    commandManager.getOwnedBy(this).forEach(commandManager::removeMapping);
  }

  private void stopTasks() {
    final Scheduler scheduler = Sponge.getScheduler();

    scheduler.getScheduledTasks(this).forEach(Task::cancel);
  }

  private void removeEventListeners() throws Exception {
    for (Object listener : eventListeners) {
      Sponge.getEventManager().unregisterListeners(listener);

      if (listener instanceof AutoCloseable) {
        ((AutoCloseable) listener).close();
      }
    }

    eventListeners.clear();
  }
}
