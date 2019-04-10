package dev.aura.joincommands.event;

import dev.aura.joincommands.AuraJoinCommands;
import dev.aura.joincommands.config.Config;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
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

  @Listener(order = Order.POST)
  public void onPlayerJoin(ClientConnectionEvent.Join event) {
    final Player player = event.getTargetEntity();
    final boolean firstJoin = !player.hasPlayedBefore();
    final Config.Commands config = AuraJoinCommands.getConfig().getCommands();

    if (firstJoin) {
      executeCommands(player, config.getFirstJoinCommands());
    }

    if (!firstJoin || config.getNormalJoinOnFirstJoin()) {
      executeCommands(player, config.getJoinCommands());
    }
  }

  private static void executeCommands(Player player, List<String> commands) {
    final CommandManager commandManager = Sponge.getCommandManager();
    final CommandSource console = Sponge.getServer().getConsole();

    for (String command : commands) {
      commandManager.process(console, replacePlaceholders(player, command));
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
