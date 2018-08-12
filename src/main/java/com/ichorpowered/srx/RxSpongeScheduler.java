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

    private @NonNull Task simpleSchedule(final @NonNull Action0 action) {
        final Task.Builder builder = Sponge.getScheduler().createTaskBuilder()
                .execute(action::call);

        if (this.async) builder.async();

        return builder.submit(this.plugin);
    }

    private @NonNull Task delaySchedule(final @NonNull Action0 action, final long delay, final @NonNull TimeUnit timeUnit) {
        final Task.Builder builder = Sponge.getScheduler().createTaskBuilder()
                .execute(action::call)
                .delay(delay, timeUnit);

        if (this.async) builder.async();

        return builder.submit(this.plugin);
    }

    private @NonNull Task intervalSchedule(final @NonNull Action0 action, final long delay, final long interval, final @NonNull TimeUnit timeUnit) {
        final Task.Builder builder = Sponge.getScheduler().createTaskBuilder()
                .execute(action::call)
                .delay(delay, timeUnit)
                .interval(interval, timeUnit);

        if (this.async) builder.async();

        return builder.submit(this.plugin);
    }

    @Override
    public @NonNull Worker createWorker() {
        return new RxSpongeWorker();
    }

    private final class RxSpongeWorker extends Worker {

        private final CompositeSubscription allSubscriptions = new CompositeSubscription();

        @Override
        public @NonNull Subscription schedule(final @NonNull Action0 action) {
            return new RxSpongeTaskSubscription(action, this.allSubscriptions);
        }

        @Override
        public @NonNull Subscription schedule(final @NonNull Action0 action, final long delay, final @NonNull TimeUnit timeUnit) {
            return new RxSpongeTaskSubscription(action, delay, timeUnit, this.allSubscriptions);
        }

        @Override
        public @NonNull Subscription schedulePeriodically(final @NonNull Action0 action, final long delay, final long interval,
                                                          final @NonNull TimeUnit timeUnit) {
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
