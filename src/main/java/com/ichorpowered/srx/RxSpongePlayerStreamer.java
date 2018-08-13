package com.ichorpowered.srx;

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
