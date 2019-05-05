# RXS (ReactiveX for Sponge)

RXS is a ReactiveX wrapper over Sponge events and scheduler with a few common utilities.

# Details

- RxJava 1.3.8
- SpongeAPI 7.1.0

# Using

RXS is a plugin dependency that you can depend on with your plugin to use ReactiveX
with Sponge. This also comes with a handy `rxdebug` command to toggle assembly tracking.

```java
final RxSpongeScheduler syncScheduler = new RxSpongeScheduler(this, false);
final RxSpongeScheduler asyncScheduler = new RxSpongeScheduler(this, true);
final RxSpongeEventStreamer eventStreamer = new RxSpongeEventStreamer(this, syncScheduler, asyncScheduler);
final RxSpongePlayerStreamer playerStreamer = new RxSpongePlayerStreamer(this, syncScheduler, asyncScheduler);

eventStreamer.observeEvent(ClientConnectionEvent.Join.class)
                .subscribe(result -> result.getTargetEntity().sendMessage(Text.of("Hello there.")));
```

# Credits

 - [Redemptive Core](https://github.com/Twister915/redemptive)
