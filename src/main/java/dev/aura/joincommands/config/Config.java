package dev.aura.joincommands.config;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {
  @Setting private General general = new General();
  @Setting private Commands commands = new Commands();

  @ConfigSerializable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class General {
    @Setting(comment = "Enable debug logging")
    private boolean debug = false;

    //    @Setting(
    //      comment =
    //          "Select which language from the lang dir to use.\n"
    //              + "You can add your own translations in there. If you name your file
    // \"test.lang\", choose \"test\" here."
    //    )
    //    private String language = MessagesTranslator.DEFAULT_LANGUAGE;
  }

  @ConfigSerializable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Commands {
    @Setting(
      comment =
          "Specify the list of all commands that should be executed when a player first joins.\n"
              + "You can use the placeholders `%player%` and `%uuid%` (without the `s) for the player name and their UUID respectively."
    )
    private List<String> firstJoinCommands =
        ImmutableList.of(
            "title %player% subtitle {text:\"Nice to see you here!\"}",
            "title %player% title {text:\"Welcome %player%!\"}");

    @Setting(
      comment =
          "Specify the list of all commands that should be executed when a player joins.\n"
              + "You can use the placeholders `%player%` and `%uuid%` (without the `s) for the player name and their UUID respectively."
    )
    private List<String> joinCommands =
        ImmutableList.of(
            "title %player% subtitle {text:\"Nice to see you here again!\"}",
            "title %player% title {text:\"Welcome back %player%!\"}");

    @Setting(
      comment =
          "Determines whether the normal joinCommands are executed after the firstJoinCommands when a new player joins."
    )
    private boolean normalJoinOnFirstJoin = false;
  }
}
