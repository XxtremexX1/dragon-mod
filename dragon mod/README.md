# Light Build Dragon

Fabric mod scaffold for a tamable, rideable flying dragon.

## Target

This project is set up for Minecraft 1.20.1, Fabric API, and GeckoLib. It needs Java 17 or newer to build and run.

## Model assets

Your Blockbench `.bbmodel` is an editor file. Export it from Blockbench as GeckoLib assets and place the exported files here:

- `src/main/resources/assets/lightbuilddragon/geo/light_build_dragon.geo.json`
- `src/main/resources/assets/lightbuilddragon/textures/entity/light_build_dragon.png`
- `src/main/resources/assets/lightbuilddragon/animations/light_build_dragon.animation.json`

The code currently expects animation names matching the Blockbench tabs: `Idle`, `Walk`, `Sprint`, `Sprint Takeoff`, `fly`, `Glide`, `Dive`, and `Sit`.

## Controls

- Tame with raw cod or raw salmon.
- Sneak right-click while owned to sit or stay.
- Right-click while owned to ride.
- Right-click with a stick while owned to toggle Follow and Wander mode.
- Hold jump while riding to fly upward.
- Press F while riding to shoot a fire charge.
- Press G while riding to do a close melee attack.
