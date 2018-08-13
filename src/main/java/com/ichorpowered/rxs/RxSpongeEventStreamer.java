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
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;

public class RxSpongeEventStreamer extends AbstractSpongeStreamer {

    public RxSpongeEventStreamer(final Object plugin, final RxSpongeScheduler syncScheduler,
                                 final RxSpongeScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    public final <T extends Event> @NonNull Observable<T> observeEvent(final @NonNull Class<? extends T>... eventTypes) {
        return observeEventRaw(eventTypes).compose(this.getSyncTransformer());
    }

    public final <T extends Event> @NonNull Observable<T> observeEvent(final @NonNull Order order, final @NonNull Class<? extends T>... eventTypes) {
        return observeEventRaw(order, eventTypes).compose(this.getSyncTransformer());
    }

    public final <T extends Event> @NonNull Observable<T> observeEvent(final @NonNull Order order, final boolean ignoreCancelled, final @NonNull Class<? extends T>... eventTypes) {
        return observeEventRaw(order, ignoreCancelled, eventTypes).compose(this.getSyncTransformer());
    }

    public final <T extends Event> @NonNull Observable<T> observeEventRaw(final @NonNull Class<? extends T>... eventTypes) {
        return observeEventRaw(Order.DEFAULT, eventTypes);
    }

    public final <T extends Event> @NonNull Observable<T> observeEventRaw(final @NonNull Order order, final @NonNull Class<? extends T>... eventTypes) {
        return observeEventRaw(order, false, eventTypes);
    }

    public final <T extends Event> @NonNull Observable<T> observeEventRaw(final @NonNull Order order, final boolean ignoreCancelled,
                                                                          final @NonNull Class<? extends T>... eventTypes) {
        return Observable.unsafeCreate(subscriber -> {
            for (final Class<? extends T> eventType : eventTypes) {
                final EventListener<T> eventListener = event -> {
                    if (!ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()) return;

                    try {
                        subscriber.onNext(event);
                    } catch (Throwable throwable) {
                        Exceptions.throwOrReport(throwable, subscriber);
                    }
                };

                Sponge.getEventManager().registerListener(this.plugin, eventType, order, eventListener);

                // Unsubscribe action
                subscriber.add(Subscriptions.create(() -> Sponge.getEventManager().unregisterListeners(eventListener)));

                Sponge.getEventManager().registerListener(this.plugin, GameStoppingEvent.class, Order.DEFAULT, event -> subscriber.onCompleted());
            }
        });
    }

}
