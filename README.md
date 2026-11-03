# Ex Nihilo Automation: Omnia — Fabric 26.3, version 1.0.0

A separate automation companion for Joshua's Ex Nihilo Omnia Fabric fork.
Includes the Auto Sieve, Auto Hammer, Auto Silker, three machine upgrades,
and reinforced wood, stone, iron, gold, diamond and netherite hammers.

## Install

Place `exnihilo-automation-fabric-26.3-1.0.0.jar` in your mods folder alongside:

- Minecraft 26.3 and Java 25.
- Fabric Loader 0.19.5 or newer.
- Fabric API 0.160.7+26.3 or a compatible newer 26.3 release.
- Ex Nihilo Omnia 0.5.0-alpha.3 (the optional-JEI release) or newer with the same API.

Install the companion on both the client and server. Keep your existing Ex Nihilo
JAR: this adds machines; it does not replace the base mod. JEI is optional;
ordinary crafting recipes are automatically discoverable through JEI if installed.

## Using the machines

Right-click a machine to open its labeled inventory. Input goes in the first
slot, mesh/hammer/silkworm in the second, fuel below the input, and upgrades
beside the fuel. Twelve output slots are on the right. Shift-click routes input,
tools, fuel and upgrades to compatible slots. Hover over the energy bar for its
stored energy. A redstone signal pauses processing; removing it resumes work.

Machines have integrated fuel power, so no separate generator mod is needed.
Accepted fuel is coal, charcoal, coal blocks, blaze rods or lava buckets.
Coal supplies 16,000 energy by default; a normal sieve/hammer operation uses
100 energy and a silker operation uses 25. Energy capacity is 10,000; excess
fuel energy is retained in a saved reserve and transferred as room becomes
available. A lava bucket leaves an empty bucket that can be removed.
This release does not connect to external FE/RF/other-mod power cables.

### Auto Sieve

Install any Omnia mesh: string, flint, iron or diamond. Accepts all seven manual
sieve materials, regular or compressed: dirt, gravel, sand, dust, soul sand,
Nether gravel and End gravel. Uses Omnia's actual live drop table and mesh gates.
A compressed input runs **nine independent rolls**, matching the heavy sieve.
Meshes are retained, matching the manual sieve. Normal processing takes 5 seconds.

### Auto Hammer

Install an Omnia hammer or a companion reinforced hammer. Uses Omnia's hammer
conversions: cobblestone/cobbled deepslate to gravel, gravel to sand, sand/red
sand to dust, netherrack to Nether gravel and End stone to End gravel. The
compressed cobblestone to gravel to sand to dust chain remains compressed.
Normal inputs use one durability; compressed inputs use nine. To process
compressed blocks, install a Reinforced Upgrade or use a reinforced hammer.
Reinforced hammers have four times the base material durability and also work
as handheld hammers. Normal processing takes 5 seconds.

### Auto Silker

Insert ordinary Omnia-supported leaves and live silkworms. Produces one string
per leaf, with one worm lasting **four leaves**. Leftover worm uses survive
saving/reloading. All fourteen supported leaf species, including cherry,
work without depending on biome tinting. Base processing takes 1.25 seconds.

### Upgrades

Three upgrade slots are available. Each holds one upgrade.

- **Speed Upgrade:** each reduces processing time by 20% of the base; three
  reduce processing time to 40%. Energy is charged per working tick.
- **Bonus Upgrade:** each gives a 25% chance of doubling the operation's output,
  up to 75% with three. Sieves apply the bonus to successful reward stacks;
  mesh requirements still apply.
- **Reinforced Upgrade:** enables compressed processing in an Auto Hammer with
  a regular hammer. Additional reinforcement upgrades give no extra benefit.

## Hopper and pipe setup

- **Top:** material and the compatible mesh/hammer/worm.
- **Sides:** fuel.
- **Bottom:** outputs and empty fuel buckets. Installed tools and upgrades stay put.

Fabric item-transfer storage is also registered for compatible pipes. Sided
access follows the same rules. Upgrades are installed through the interface.

Finished rewards are stored if they cannot fit. A blocked machine does not
reroll its results, discard overflow, or start another operation until the
queued rewards fit. Inventories, work progress, queued rewards, stored power,
fuel reserve and silkworm uses survive saving/reloading. Breaking the machine
returns its inventory and any queued rewards; work progress and stored power
are not carried by the dropped block.

## Crafting

For each machine, place iron ingots in the four corners, redstone dust in the
four edge positions, and its center ingredient in the middle:

| Machine | Center ingredient |
| --- | --- |
| Auto Sieve | Omnia wooden sieve |
| Auto Hammer | Omnia diamond hammer |
| Auto Silker | Omnia bone crook |

Upgrades use the same layout with iron in the corners and redstone in the
center. Edge ingredients are sugar for Speed, emeralds for Bonus, or obsidian
for Reinforced. One recipe produces one upgrade.

A reinforced hammer is crafted with an ordinary Omnia hammer in the center
and obsidian above, below, left and right. The resulting hammer has full
reinforced durability.

This companion reuses Omnia's existing one-level compressed blocks; it does
not introduce conflicting duplicates or add highly/atomic compressed tiers.

## Configuration

`config/exnihiloauto.properties` is created on first launch. Stop the server,
edit it, and restart. Properties control sieve/hammer/silker ticks, energy cost,
energy capacity and fuel energy per burn tick. Values are range checked.

## Validation

- Compiled against the actual Fabric 26.3 / Java 25 APIs.
- Dedicated Fabric game-test server: all 10 required companion tests passed
  (nine custom scenarios plus Fabric's built-in registration check).
- Existing Omnia suite: all 29 required tests passed, plus its leaf tint checks.
- Tests cover live nine-roll equivalence, meshes, hammer wear and reinforcement,
  silkworm use, persistence, full outputs, queued rewards, break recovery,
  redstone, sided insertion/extraction, shift-click and speed upgrades.
- Client screen code compiles against 26.3's rendering API. Visual appearance
  and compatibility with the complete player modpack still require in-game testing.

## Build the source

Use Java 25 and run `./gradlew build` (or `gradlew.bat build` on Windows).
Place your existing `exnihiloomnia-fabric-26.3-0.5.0-alpha.3-jei.jar` in `libs/`
first. The base mod JAR is not included in this source archive. If an Omnia
checkout instead exists at `../omnia/fabric-26.3`, Gradle uses that source project.
`./gradlew runGameTest` runs the companion server checks. Development tests are
not included in the release JAR.

All generated JSON resources and textures needed by the release are included.
The optional resource-generation script expects an adjacent checkout of the
upstream automation repository at `../upstream-automation` (branch `1.21.4`).
Running the script is not required to build.

## Credits and scope

Adapted from lazyMods/exnihilo-automation, branch `1.21.4`, commit
`204875ccb43c11ba706198a1420173ef2be4ccee`. Original artwork and mechanics:
lazynessmind and contributors; MIT license included in LICENSE.md and the JAR.
Omnia integration targets teamaonn/ExNihiloOmnia, branch
`fabric-26.3-optional-jei`, commit `cd6d3ae81f504efecd350850e7e98544210ecb86`.
Original Omnia artwork is referenced through the base mod rather than duplicated.

The port replaces NeoForge/Sequentia integration with Fabric/Omnia integration.
It retains the three machines and upgrade concepts while using the existing
Omnia compression and drop systems. Fuel power and saved reward buffering are
part of this adaptation. No NeoForge or Sequentia installation is required.
