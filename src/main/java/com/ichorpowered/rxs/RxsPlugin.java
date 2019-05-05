package com.ichorpowered.rxs;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import rx.plugins.RxJavaHooks;

@Plugin(
        id = RxsPlugin.PLUGIN_ID,
        name = RxsPlugin.PLUGIN_NAME,
        version = RxsPlugin.PLUGIN_VERSION,
        description = RxsPlugin.PLUGIN_DESCRIPTION,
        authors = { "Connor Hartley <vectrixu@gmail.com>" }
)
public class RxsPlugin {

    public static final String PLUGIN_ID = "@id@";
    public static final String PLUGIN_NAME = "@name@";
    public static final String PLUGIN_VERSION = "@version@";
    public static final String PLUGIN_DESCRIPTION = "@description@";

    private boolean assemblyTracking = false;

    @Listener
    public void onGameInitialization(final GameInitializationEvent event) {
        final CommandSpec debugCommand = CommandSpec.builder()
                .description(Text.of("Toggles the RX Assembly Tracking"))
                .permission("rxs.command.debug")
                .executor((CommandSource source, CommandContext context) -> {
                    if (this.assemblyTracking) RxJavaHooks.clearAssemblyTracking();
                    else RxJavaHooks.enableAssemblyTracking();

                    this.assemblyTracking = !this.assemblyTracking;

                    return CommandResult.success();
                })
                .build();

        Sponge.getCommandManager().register(this, debugCommand, "rxsdebug", "rxdebug");
    }

}
