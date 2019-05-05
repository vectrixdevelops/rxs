/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ichorpowered.rxs;

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
