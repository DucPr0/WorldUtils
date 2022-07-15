# WorldUtils
Utilities for working with Minecraft worlds asynchronously

Simple utility functions, can be used mainly for creating minigame worlds from an existing template world.

# Installation
The class WorldUtils can be copied directly into your plugin.

# Usage
Do the following in your onEnable():
```
WorldUtils.setPlugin(this);
WorldUtils.registerWorldInitListener();
```
This will register a listener that listens for WorldInitEvent, and disables keepSpawnInMemory to optimize loading times further (can be specified). Now, for copying a template world, it's just a matter of:
```
WorldUtils.copyAndLoadWorldAsync("template", "id1").thenAccept(success -> {
    // processing code
});
```
