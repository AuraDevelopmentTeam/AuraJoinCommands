package dev.aura.joincommands.message;

import dev.aura.joincommands.AuraJoinCommands;
import dev.aura.lib.messagestranslator.Message;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@RequiredArgsConstructor
public enum PluginMessages implements Message {
  // Admin Messages
  ADMIN_REALOAD_SUCCESSFUL("reloadSuccessful"),
  ADMIN_REALOAD_NOT_SUCCESSFUL("reloadNotSuccessful");

  @Getter private final String stringPath;

  public Text getMessage() {
    return getMessage(null);
  }

  public Text getMessage(Map<String, String> replacements) {
    final String message =
        AuraJoinCommands.getTranslator().translateWithFallback(this, replacements);

    return TextSerializers.FORMATTING_CODE.deserialize(message);
  }
}
