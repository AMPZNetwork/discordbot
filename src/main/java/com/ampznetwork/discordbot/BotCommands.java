package com.ampznetwork.discordbot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.User;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;

public class BotCommands {
    @Command(permission = "8")
    public Object promote(@Command.Arg User user, @Command.Arg String minecraft, @Command.Arg PromotionTarget target) {
        return "wip";
    }

    @Command(permission = "8")
    public Object announce(@Command.Arg String message) {
        return "wip";
    }

    @Command(permission = "8")
    public Object eval(@Command.Arg String code) {
        return "wip";
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public enum PromotionTarget implements Named {
        user(774034225304698940L),
        tester(1010851881813299291L),
        builder(1079483093058072627L),
        helper(523986482541756430L),
        moderator(495515597942292511L),
        developer(1312325120395837460L);

        long discordRoleId;
    }
}
