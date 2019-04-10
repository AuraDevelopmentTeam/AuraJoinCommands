package dev.aura.joincommands.event;

import dev.aura.joincommands.AuraJoinCommands;
import dev.aura.joincommands.config.Config;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

@RequiredArgsConstructor
public class PlayerEvents {
  private static final Pattern PATTERN_PLAYER =
      Pattern.compile("%player%", Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
  private static final Pattern PATTERN_UUID =
      Pattern.compile("%uuid%", Pattern.CASE_INSENSITIVE | Pattern.LITERAL);

  private final boolean debug = AuraJoinCommands.getConfig().getGeneral().getDebug();
  private final Logger logger = AuraJoinCommands.getLogger();
  private final Config.Commands config = AuraJoinCommands.getConfig().getCommands();
  private final CommandManager commandManager = Sponge.getCommandManager();
  private final CommandSource console = Sponge.getServer().getConsole();

  @Listener(order = Order.POST)
  public void onPlayerJoin(ClientConnectionEvent.Join event) {
    final Player player = event.getTargetEntity();
    final boolean firstJoin = !player.hasPlayedBefore();

    if (firstJoin) {
      executeCommands(player, config.getFirstJoinCommands());
    }

    if (!firstJoin || config.getNormalJoinOnFirstJoin()) {
      executeCommands(player, config.getJoinCommands());
    }
  }

  private final void executeCommands(Player player, List<String> commands) {
    for (String command : commands) {
      final String processedCommand = replacePlaceholders(player, command);
      final String commandLog =
          " command \""
              + processedCommand
              + "\" for player "
              + player.getName()
              + '('
              + player.getUniqueId().toString()
              + ')';

      try {
        logDebug("Executing" + commandLog + "...");

        commandManager.process(console, processedCommand);

        logTrace("Executed" + commandLog + " successfully!");
      } catch (RuntimeException e) {
        logger.warn("Error while executing" + commandLog + '!', e);
      }
    }
  }

  private final void logDebug(String message) {
    if (debug) {
      logger.info(message);
    } else {
      logger.debug(message);
    }
  }

  private final void logTrace(String message) {
    if (debug) {
      logger.debug(message);
    } else {
      logger.trace(message);
    }
  }

  private static String replacePlaceholders(Player player, String command) {
    String result = command;

    if (result.charAt(0) == '/') {
      result = result.substring(1);
    }

    result = PATTERN_PLAYER.matcher(result).replaceAll(player.getName());
    result = PATTERN_UUID.matcher(result).replaceAll(player.getUniqueId().toString());

    return result;
  }
}
