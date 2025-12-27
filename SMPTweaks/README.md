# SMPTweaks

This is the source code of SMPTweaks. SMPTweaks is a plugin that tweaks some game mechanics and adds extra features. Every one of these tweaks can be disabled and enabled inside the config: `/plugins/SMPTweaks/config.yml`. You can download the SMPTweaks [here](https://github.com/serbinskis/minecraft-plugins/raw/refs/heads/master/SMPTweaks/build/SMPTweaks.jar).

## Custom Potions

Adds to the server different new potions and new brewing recipes. To get more info about potions use the command: `/smptweaks execute cpotions info`.

- `/gamerule custom_potions false` (default: true)

</br>

- Recall -> Teleport to spawn point
- Wormhole -> Shoot a player and then use the potion
- Unbinding -> Drops armor with the curse of binding
- Explosion -> Explodes
- Screamer -> Prank a friend

<p align="center"><img width="586" height="332" src="https://i.imgur.com/1lQELw2.png"></p>

## No Arrow Infinity

Allows players to use a bow with infinity without arrows.
- `/gamerule bow_infinity_arrows false` (default: true)

## Holograms

Admin tool to make holograms with display text entity. Crouch and right-click a block with a book to create and hit to remove. Stick - Right click: toggle following | Left click:  toggle see through. Compass - Left click: cycle rotation. Blaze Rod - Left click: cycle alignment. Special book - Right-click block: teleport hologram.

<p align="center"><img width="1300" height="935" src="https://i.imgur.com/26ufsgu.png" style="border-radius:12px;border:2px solid #555;"></p>

## Auto Trade

Allows to automatically trade with villagers within a distance of 2 blocks.

- `/gamerule auto_trade false` (default: true)

<p align="center" float="left">
  <img width="500" height="325" src="https://i.imgur.com/cGCJNSP.png">
  <img width="500" height="325" src="https://i.imgur.com/0AHLowt.gif">
</p>

## Magnet Block

Allows to pull nearby items toward the block from a set distance and at a set speed.

- `/gamerule magnet_block false` (default: true)

<p align="center" float="left">
  <img width="500" height="325" src="https://i.imgur.com/tXmRkff.png">
  <img width="500" height="325" src="https://i.imgur.com/fRUHC1a.gif">
</p>

## Chunk Loader

Allows loading chunks as if a player were standing there. Crops do grow and mobs also spawn.

- `/gamerule chunk_loaders false` (default: true)

<p align="center" float="left">
  <img width="500" height="325" src="https://i.imgur.com/UchF5xf.png">
  <img width="500" height="325" src="https://i.imgur.com/PIzYgd3.png">
</p>

## Better Furnaces

Adds more and faster furnaces.

- `/gamerule better_furnaces false` (default: true)

<p align="center" float="left">
  <img width="500" height="325" src="https://i.imgur.com/G87HGUY.png">
  <img width="500" height="325" src="https://i.imgur.com/S3HlsUB.png">
</p>

## Fast Leaf Decay

Makes leafs decay much faster.

- `/gamerule fast_leaf_decay false` (default: true)

<p align="center"><img width="800" height="500" src="https://i.imgur.com/z3X7h7E.gif" style="border-radius:12px;border:2px solid #555;"></p>

## Cycle Villager Trades

Allows cycling trades inside a villager.

- `/gamerule villager_trade_cycle false` (default: true)

<p align="center"><img width="550" height="330" src="https://i.imgur.com/oyIcjc2.png"></p>

## No End Portal

Disable end portal with custom gamerule. This will prevent placing ender eyes, and also prevent from going through the end portal.

- `/gamerule allow_end_portal false` (default: true)

## No Advancements

Disable advancements per world with custom gamerule.

- `/gamerule allow_advancements false` (default: true)

## Global Trading

Allows to share cured villager prices among all players.

- `/gamerule villager_global_trading false` (default: true)

## Falling Block Duplication

Allows falling block duplication glitch with end portal on PaperMC servers.

- `/gamerule falling_block_duplication false` (default: true)

## No Too Expensive

Removes "Too expensive" from anvils and allows you to spend any level if you have enough.

- `/gamerule disable_anvil_too_expensive false` (default: true)

## Repair With XP

Allow repairing mending tools with XP. Put item with mending in offhand and crouch.

- `/gamerule repair_with_xp false` (default: true)

## Head Drops

Drop player head on death if killed by player.

- `/gamerule player_head_drops true` (default: false)

## Instant Curing

Makes curing villagers instant.

- `/gamerule villager_instant_curing false` (default: true)

## All Crafting Recipes

Give all crafting recipes when a player joins.

- `/gamerule all_crafting_recipes false` (default: true)

## Anti Creeper Grief

Prevent creepers from exploding blocks.

- `/gamerule creeper_grief false` (default: true)

## Anti Enderman Grief

Prevent enderman from picking up blocks.

- `/gamerule enderman_grief false` (default: true)

## Drop Cursed Pumpkin

Drop cursed pumpkin on death when keep inventory is on.

- `/gamerule drop_cursed_pumpkin false` (default: true)

## Entity Limit

Limits entity spawn.

- `/gamerule entity_limit false` (default: true)