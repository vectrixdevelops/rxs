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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import rx.Observable;
import rx.Scheduler;
import rx.exceptions.Exceptions;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.TimeUnit;

public class RxSpongePlayerStreamer extends AbstractSpongeStreamer {

    public RxSpongePlayerStreamer(final Object plugin, final RxSpongeScheduler syncScheduler,
                                  final RxSpongeScheduler asyncScheduler) {
        super(plugin, syncScheduler, asyncScheduler);
    }

    public final @NonNull Observable<Player> observePlayer(final @NonNull Player player, final long delay,
                                                           final @NonNull TimeUnit timeUnit) {
        return Observable.<Player>unsafeCreate(subscriber -> {
            subscriber.onStart();

            final Scheduler.Worker worker = this.syncScheduler.createWorker();
            worker.schedule(() -> {
                try {
                    subscriber.onNext(player);
                } catch (Throwable throwable) {
                    try {
                        worker.unsubscribe();
                    } finally {
                        Exceptions.throwOrReport(throwable, subscriber);
                    }
                }
            }, delay, timeUnit);

            subscriber.add(Subscriptions.create(worker::unsubscribe));
        })

        .takeWhile(User::isOnline)

        .compose(this.getSyncTransformer());
    }


    public final @NonNull Observable<Player> observePlayer(final @NonNull Player player, final long delay,
                                                           final long interval, final @NonNull TimeUnit timeUnit) {
        return Observable.<Player>unsafeCreate(subscriber -> {
            subscriber.onStart();

            final Scheduler.Worker worker = this.syncScheduler.createWorker();
            worker.schedulePeriodically(() -> {
                try {
                    subscriber.onNext(player);
                } catch (Throwable throwable) {
                    try {
                        worker.unsubscribe();
                    } finally {
                        Exceptions.throwOrReport(throwable, subscriber);
                    }
                }
            }, delay, interval, timeUnit);

            subscriber.add(Subscriptions.create(worker::unsubscribe));
        })

        .takeWhile(User::isOnline)

        .compose(this.getSyncTransformer());
    }

}
