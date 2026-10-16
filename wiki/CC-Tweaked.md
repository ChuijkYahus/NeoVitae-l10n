# CC: Tweaked

With CC: Tweaked installed, computers and turtles can read the state of most Neo Vitae machines directly, instead of through a comparator.

A computer finds a block when it sits next to it or is connected to it through a wired modem. A turtle can wrap the block it faces with `peripheral.wrap("front")`. Every block below has its own peripheral type, so `peripheral.find` picks it out by name:

```lua
local altar = peripheral.find("ara_vitae")
print(altar.getEssence() .. " / " .. altar.getCapacity() .. " EV")
```

Everything here is read-only. Blocks that hold items or fluids also keep CC's usual `inventory` and `fluid_storage` methods (`list`, `pushItems`, `tanks` and so on), so a computer can move items in and out as well as read them.

Progress values run from 0 to 1. Status and reason values are lowercase strings, and a method returns `nil` when there is nothing to report.

## Ara Vitae

Type `ara_vitae`.

| Method | Returns |
| --- | --- |
| `getEssence()` | EV in the basin |
| `getCapacity()` | Most EV the basin can hold |
| `getTier()` | Current altar tier |
| `getProgress()` | Progress of the current craft |
| `getEssenceRequired()` | Total EV the current craft needs, or 0 when idle |
| `getConsumptionRate()` | EV used per tick while crafting |

A turtle facing the altar also gets these numbers from `turtle.inspect()`, under `araVitae`:

```lua
local ok, block = turtle.inspect()
if ok and block.araVitae then
    print(block.araVitae.essence .. " / " .. block.araVitae.capacity)
end
```

The table holds `essence`, `capacity`, `tier`, `progress` and `essenceRequired`.

## Vitae Link

Type `vitae_link`.

| Method | Returns |
| --- | --- |
| `getTier()` | The tier the Link is capped at |
| `getMaxTier()` | The highest tier the Link can be set to with its current altar |
| `isLinked()` | Whether the Link is bound to an altar |
| `isCrafting()` | Whether the Link is crafting right now |
| `getEssenceProgress()` | EV put into the current craft so far |
| `getEssenceRequired()` | Total EV the current craft needs, or 0 when idle |

## Orb Vitae Link

Type `orb_vitae_link`.

| Method | Returns |
| --- | --- |
| `getAnima()` | EV in the Anima of the seated orb's owner |
| `getAnimaCapacity()` | Most EV the seated orb lets that Anima hold |
| `hasOrb()` | Whether an orb is seated |
| `isLinked()` | Whether the Link is bound to an altar |
| `isPaused()` | Whether a redstone signal is pausing the Link |

Both Anima methods return 0 when no bound orb is seated.

## Master Ritual Stone

Type `master_ritual_stone`. Works on the inverted stone too.

| Method | Returns |
| --- | --- |
| `isActive()` | Whether a ritual is running |
| `isSuspended()` | Whether redstone is holding the ritual paused |
| `getRitual()` | The running ritual's ID, such as `neovitae:crystallum_fractura`, or `nil` |
| `getRunningTime()` | Ticks the current ritual has been running |
| `getCooldown()` | Ticks left on a cooldown set with `/neovitae ritual <pos> cooldown`; the stone does nothing until it reaches 0 |
| `getOwnerAnima()` | EV in the ritual owner's Anima |

## Spirit Accumulator

Type `spiritus_storage`.

| Method | Returns |
| --- | --- |
| `getSpiritus()` | Spiritus banked in the Accumulator |
| `getSpiritusCapacity()` | Most Spiritus it can bank |
| `getSpiritusType()` | The attuned Aspect (`raw`, `ruina`, `nihilum`, `vindicta` or `invictus`), or `nil` |

## Athanor

Type `athanor`.

| Method | Returns |
| --- | --- |
| `getProgress()` | Progress of the current operation |
| `getIdleReason()` | Why it is stalled, or `nil` |

Idle reasons: `output_full`, `spent_tool`, `tool_unbound`, `not_enough_spiritus`, `not_enough_ev`.

## Tabula Vitae

Type `tabula_vitae`. Either half of a joined pair reports the pair's state.

| Method | Returns |
| --- | --- |
| `getProgress()` | Progress of the current craft |
| `getIdleReason()` | Why it is stalled, or `nil` |

Idle reasons: `no_recipe`, `no_orb`, `orb_unbound`, `tier_too_low`, `not_enough_ev`, `output_blocked`, `no_network`, `cutting_fluid_unbound`.

## Hellfire Forge

Type `hellfire_forge`.

| Method | Returns |
| --- | --- |
| `getProgress()` | Progress of the current craft |

## Sanguine Crafter

Type `sanguine_crafter`. Connect to the controller block.

| Method | Returns |
| --- | --- |
| `getProgress()` | Progress of the current craft |
| `getStatus()` | What the crafter is doing; matches the [status line](Sanguine-Crafter#status-messages) |
| `getMode()` | `auto`, `crafting`, `tabula` or `forge` |
| `isFormed()` | Whether the multiblock is complete |

Status values: `crafting`, `idle`, `not_formed`, `no_owner`, `disabled`, `no_template`, `no_recipe`, `missing_ingredients`, `no_orb`, `tier_too_low`, `not_enough_ev`, `not_enough_spiritus`, `output_full`, `limit_reached`.
