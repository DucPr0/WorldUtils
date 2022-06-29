# WorldUtils
Utilities for working with Minecraft worlds asynchronously

Simple utility functions, can be used mainly for creating minigame worlds from an existing template world.

# Installation
I would figure out how to make this a Maven dependency later, but for now since there's only one class, it can be directly copied into your plugin. The following dependency must be added and shaded in:
```
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.11.0</version>
</dependency>
```

# Usage
Do the following in your onEnable():
```
WorldUtils.setPlugin(this);
WorldUtils.registerWorldInitListener();
```
This will register a listener that listens for WorldInitEvent, and disables keepSpawnInMemory to optimize loading times further. Now, for copying a template world, it's just a matter of:
```
WorldUtils.copyAndLoadWorld("template", "id1").thenAccept(success -> {
    // processing code
});
```
