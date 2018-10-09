# RXS (ReactiveX for Sponge)

RXS is a ReactiveX wrapper over Sponge events and scheduler with a few common utilities.

# Details

- RxJava 1.3.8
- SpongeAPI 7.1.0

# Using

RXS is designed to be shaded into your plugin jar and initialized once for your plugin.

```java
final RxSpongeScheduler syncScheduler = new RxSpongeScheduler(this, false);
final RxSpongeScheduler asyncScheduler = new RxSpongeScheduler(this, true);
final RxSpongeEventStreamer eventStreamer = new RxSpongeEventStreamer(this, syncScheduler, asyncScheduler);
final RxSpongePlayerStreamer playerStreamer = new RxSpongePlayerStreamer(this, syncScheduler, asyncScheduler);
```
