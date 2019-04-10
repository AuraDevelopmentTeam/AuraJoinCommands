package dev.aura.joincommands.command;

import dev.aura.joincommands.AuraJoinCommands;
import dev.aura.joincommands.permission.PermissionRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandBase {
  public static final String BASE_PERMISSION = PermissionRegistry.COMMAND;

  public static void register(AuraJoinCommands plugin) {
    CommandSpec updatechecker =
        CommandSpec.builder()
            .description(Text.of("Base command for the plugin. Does nothing on its own."))
            .child(CommandReload.create(plugin), "reload", "r", "rl", "re", "rel")
            .build();

    Sponge.getCommandManager()
        .register(
            plugin,
            updatechecker,
            AuraJoinCommands.ID,
            "joincommands",
            "jc",
            "join",
            "joincmds",
            "jcmds");
  }
}
