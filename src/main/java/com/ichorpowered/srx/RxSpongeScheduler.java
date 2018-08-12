package com.ichorpowered.srx;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.CompositeSubscription;

import java.util.concurrent.TimeUnit;

public class RxSpongeScheduler extends Scheduler {

    private final Object plugin;
    private final boolean async;

    public RxSpongeScheduler(final Object plugin, final boolean async) {
        this.plugin = plugin;
        this.async = async;
    }

    private Task simpleSchedule(final @NonNull Action0 action) {
        final Task.Builder builder = Sponge.getScheduler().createTaskBuilder()
                .execute(action::call);

        if (this.async) builder.async();

        return builder.submit(this.plugin);
    }

    private Task delaySchedule(final @NonNull Action0 action, final long delay, final @NonNull TimeUnit timeUnit) {
        final Task.Builder builder = Sponge.getScheduler().createTaskBuilder()
                .execute(action::call)
                .delay(delay, timeUnit);

        if (this.async) builder.async();

        return builder.submit(this.plugin);
    }

    private Task intervalSchedule(final @NonNull Action0 action, final long delay, final long interval, final @NonNull TimeUnit timeUnit) {
        final Task.Builder builder = Sponge.getScheduler().createTaskBuilder()
                .execute(action::call)
                .delay(delay, timeUnit)
                .interval(interval, timeUnit);

        if (this.async) builder.async();

        return builder.submit(this.plugin);
    }

    @Override
    public Worker createWorker() {
        return new RxSpongeWorker();
    }

    private final class RxSpongeWorker extends Worker {

        private final CompositeSubscription allSubscriptions = new CompositeSubscription();

        @Override
        public Subscription schedule(final Action0 action) {
            return new RxSpongeTaskSubscription(action, this.allSubscriptions);
        }

        @Override
        public Subscription schedule(final Action0 action, final long delay, final TimeUnit timeUnit) {
            return new RxSpongeTaskSubscription(action, delay, timeUnit, this.allSubscriptions);
        }

        @Override
        public Subscription schedulePeriodically(final Action0 action, final long delay, final long interval, final TimeUnit timeUnit) {
            return new RxSpongeTaskSubscription(action, delay, interval, timeUnit, this.allSubscriptions);
        }

        @Override
        public void unsubscribe() {
            this.allSubscriptions.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return this.allSubscriptions.isUnsubscribed();
        }

    }

    private final class RxSpongeTaskSubscription implements Subscription {

        private final Task task;
        private final CompositeSubscription parent;

        private boolean unsubscribed;

        private RxSpongeTaskSubscription(final Action0 action, final CompositeSubscription parent) {
            this.task = simpleSchedule(() -> {
                try {
                    action.call();
                } finally {
                    this.unsubscribed = true;
                    parent.remove(RxSpongeTaskSubscription.this);
                }
            });
            this.parent = parent;

            parent.add(this);
        }

        private RxSpongeTaskSubscription(final Action0 action, final long delay, final TimeUnit timeUnit, final CompositeSubscription parent) {
            this.task = delaySchedule(() -> {
                try {
                    action.call();
                } finally {
                    this.unsubscribed = true;
                    parent.remove(RxSpongeTaskSubscription.this);
                }
            }, delay, timeUnit);
            this.parent = parent;

            parent.add(this);
        }

        private RxSpongeTaskSubscription(final Action0 action, final long delay, final long interval, final TimeUnit timeUnit, final CompositeSubscription parent) {
            this.task = intervalSchedule(action, delay, interval, timeUnit);
            this.parent = parent;

            parent.add(this);
        }

        @Override
        public void unsubscribe() {
            this.unsubscribed = true;
            this.task.cancel();
            this.parent.remove(RxSpongeTaskSubscription.this);
        }

        @Override
        public boolean isUnsubscribed() {
            return this.unsubscribed;
        }

    }

}
