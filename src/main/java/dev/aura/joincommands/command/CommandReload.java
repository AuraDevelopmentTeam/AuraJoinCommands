package dev.aura.joincommands.command;

import com.google.common.collect.ImmutableMap;
import dev.aura.joincommands.AuraJoinCommands;
import dev.aura.joincommands.message.PluginMessages;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandReload implements CommandExecutor {
  public static final String RELOAD_PERMISSION = CommandBase.BASE_PERMISSION + ".reload";

  private final AuraJoinCommands plugin;

  public static CommandSpec create(AuraJoinCommands plugin) {
    return CommandSpec.builder()
        .permission(RELOAD_PERMISSION)
        .description(Text.of("Reloads the plugin."))
        .executor(new CommandReload(plugin))
        .build();
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    Sponge.getScheduler()
        .createTaskBuilder()
        .async()
        .execute(
            () -> {
              try {
                plugin.reload(null);

                src.sendMessage(PluginMessages.ADMIN_REALOAD_SUCCESSFUL.getMessage());
              } catch (Exception e) {
                AuraJoinCommands.getLogger().error("Error while reloading the plugin:", e);
                src.sendMessage(
                    PluginMessages.ADMIN_REALOAD_NOT_SUCCESSFUL.getMessage(
                        ImmutableMap.of("error", e.getMessage())));
              }
            })
        .submit(plugin);

    return CommandResult.success();
  }
}
