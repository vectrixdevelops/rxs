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

                Sponge.getEventManager().registerListener(this.plugin, GameStoppingEvent.class, Order.DEFAULT, event -> subscriber.onCompleted());
            }
        });
    }

}
