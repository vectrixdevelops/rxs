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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;

public class RxSpongeDataStreamer extends AbstractSpongeStreamer {

    public RxSpongeDataStreamer(final Object plugin, final RxSpongeScheduler syncScheduler,
                                final RxSpongeScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    @SafeVarargs
    public final <E extends DataHolder, T extends BaseValue<?>> @NonNull Observable<ChangeDataHolderEvent.ValueChange> observeData(final @NonNull Key<T> dataKey,
                                                                                                                                   final @NonNull Class<? extends E>... dataHolders) {
        return this.observeDataRaw(dataKey, dataHolders).compose(this.getSyncTransformer());
    }

    @SafeVarargs
    public final <E extends DataHolder, T extends BaseValue<?>> @NonNull Observable<ChangeDataHolderEvent.ValueChange> observeData(final boolean ignoreCancelled,
                                                                                                                                   final @NonNull Key<T> dataKey,
                                                                                                                                   final @NonNull Class<? extends E>... dataHolders) {
        return this.observeDataRaw(ignoreCancelled, dataKey, dataHolders).compose(this.getSyncTransformer());
    }

    @SafeVarargs
    public final <E extends DataHolder, T extends BaseValue<?>> @NonNull Observable<ChangeDataHolderEvent.ValueChange> observeDataRaw(final @NonNull Key<T> dataKey,
                                                                                                                                      final @NonNull Class<? extends E>... dataHolders) {
        return this.observeDataRaw(false, dataKey, dataHolders);
    }

    @SafeVarargs
    public final <E extends DataHolder, T extends BaseValue<?>> @NonNull Observable<ChangeDataHolderEvent.ValueChange> observeDataRaw(final boolean ignoreCancelled,
                                                                                                                                      final @NonNull Key<T> dataKey,
                                                                                                                                      final @NonNull Class<? extends E>... dataHolders) {
        return Observable.unsafeCreate(subscriber -> {
            for (final Class<? extends E> dataHolder : dataHolders) {
                final EventListener<ChangeDataHolderEvent.ValueChange> eventListener = event -> {
                    if (!ignoreCancelled && event.isCancelled()) return;

                    try {
                        subscriber.onNext(event);
                    } catch (Throwable throwable) {
                        Exceptions.throwOrReport(throwable, subscriber);
                    }
                };

                dataKey.registerEvent(dataHolder, eventListener);

                // Unsubscribe action
                subscriber.add(Subscriptions.create(() -> Sponge.getEventManager().unregisterListeners(eventListener)));
            }

            Sponge.getEventManager().registerListener(this.plugin, GameStoppingEvent.class, Order.DEFAULT, event -> subscriber.onCompleted());
        });
    }

}
