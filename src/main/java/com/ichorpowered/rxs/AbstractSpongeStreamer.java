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
import rx.Observable;

public abstract class AbstractSpongeStreamer {

    protected final Object plugin;
    protected final RxSpongeScheduler syncScheduler;
    protected final RxSpongeScheduler asyncScheduler;

    public AbstractSpongeStreamer(final Object plugin, final RxSpongeScheduler syncScheduler,
                                  final RxSpongeScheduler asyncScheduler) {
        this.plugin = plugin;
        this.syncScheduler = syncScheduler;
        this.asyncScheduler = asyncScheduler;
    }

    public <T> Observable.Transformer<T, T> getSyncTransformer() {
        return tObservable -> tObservable.subscribeOn(this.syncScheduler);
    }

    public <T> Observable.Transformer<T, T> getAsyncTransformer() {
        return tObservable -> tObservable.subscribeOn(this.asyncScheduler);
    }

    public @NonNull RxSpongeScheduler getSyncScheduler() {
        return this.syncScheduler;
    }

    public @NonNull RxSpongeScheduler getAsyncScheduler() {
        return this.asyncScheduler;
    }

    public @NonNull Object getPlugin() {
        return this.plugin;
    }

}
