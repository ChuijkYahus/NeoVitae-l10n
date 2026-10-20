# Automating Spiritus

A mature setup turns [Spiritus](Spiritus-Aspects-and-Crystals) into a self-sustaining farm: crystals grow, a ritual harvests them, the harvest is burned back into the chunk's Aura, and routing carries the surplus to your rituals, the [Hellfire Forge](Hellfire-Forge-and-Sentient-Equipment), and the [Athanor](Athanor-and-Materials). This page walks the full life cycle; the underlying mechanics are on the [Spiritus, Aspects and Crystals](Spiritus-Aspects-and-Crystals) page.

## The life cycle

1. **Grow.** A **Crystallarium Maleficum** with open air above it seeds a one-spire crystal cluster after 50 seconds once its chunk holds at least **99** of its dominant Aspect, using up those 99. The cluster then grows on its own up to 7 spires, each spire costing **45** of its Aspect from the chunk. Growth speed scales with how much of the cluster's own Aspect the chunk holds, so a fuller chunk grows faster.
2. **Aspect it (optional).** A Crystallarium seeds whichever Aspect is highest in its chunk, so a fresh chunk seeds Raw. To start another Aspect, right-click a fully grown (7-spire) Raw cluster with a **Spiritus Ruina, Nihilum, Vindicta or Invictus Catalyst** while carrying an **Animus Mote**. The catalyst and one mote are used up, and the cluster restarts as a one-spire cluster of that Aspect. Grow it out and burn its crystals; once that Aspect outweighs Raw and reaches 99, the Crystallarium seeds it directly. An aspected cluster in a chunk with none of its own Aspect falls back to Raw at 90 per spire and 60% speed.
3. **Harvest.** The **Crystallum Fractura** ritual (see [Rituals](Rituals)) harvests fully grown clusters in range. Spiritus clusters are not broken: each resets to one spire and regrows, yielding 6 Spiritus Crystals (more with Fortune). Drops go into a chest directly above the Master Ritual Stone, which the Ritual Configurator can move up to 5 blocks away; with no chest they fall on the ground, and a full chest pauses the ritual. While running it **doubles** crystal growth and adds **+25%** to Spiritus injected in every chunk its area touches. To steer that bonus, right-click the Master Ritual Stone with the **Ritual Configurator** and pick an Aspect; the extra 25% on Raw injections then goes to that Aspect instead. Harvests gain Fortune from the chunk's density of each crystal's own Aspect (Fortune I above 30, II above about 53, III above about 77), so a Ruina-heavy chunk boosts Ruina crystals, and each Fortune harvest has about a 42% chance to use up 1 point of that Aspect. Geode clusters such as amethyst use the selected Aspect's density, or Raw.
4. **Burn.** An unpowered **Spiritus Crucible** burns one Spiritus Crystal at a time while the chunk holds less than **50** of that crystal's Aspect, releasing **50** into the Aura (62.5 inside Crystallum Fractura's area). A spire costs 45 to grow, so every crystal is a net gain of 5, and far more with catalysts (see below).
5. **Route.** Put an **Input Routing Node** on the harvest chest and an **Output Routing Node** against the Spiritus Crucible, both bound to a **Master Routing Node**, and the crystals feed themselves back in. To move Aura without a network, chain **Spiritus Conduit** pylons toward your worksite: each one checks the point 16 blocks away in each cardinal direction and pulls up to 1 per tick from any richer chunk until the two even out.
6. **Bank and distribute.** A **Spirit Accumulator** on the farm chunk skims its Aspect above 30 into a 1,000-point reserve, and an **Output Routing Node** in another chunk keeps that chunk stocked from the same network. Each network link spans up to 16 blocks, so a chain of Routing Conduits carries Aura as far as you build it.
7. **Consume.** Rituals such as Green Grove, Regeneration and Speed draw on the Aura for extra effects, a redstone-powered Spiritus Crucible fills the gem inside it from the chunk's Aura, and the Athanor's ore-yield boost scales with local Raw Spiritus.

## The closed loop

Catalyst + Crystallum Fractura + routing is a hands-off farm: the ritual harvests and the nodes ship the crystals back into the Spiritus Crucible. A **same-Aspect catalyst** on a matching cluster cuts the cost to **25** per spire and makes it grow ten times faster for the next 10 spires (two can be stacked), which turns the cycle strongly net-positive.

## Banking the surplus

The Crucible stops burning crystals once their Aspect reaches 50, so a busy farm soon backs up with unburned crystals. Park a **[Spirit Accumulator](Spiritus-Aspects-and-Crystals)** in the farm chunk, right-click it to cycle to the farmed Aspect (or right-click it with a crystal of that Aspect, which attunes and locks it in one step), and crouch-click to lock it. It skims that Aspect above **30** into a **1,000**-point reserve at up to 25 per tick, which keeps the Crucible burning and banks the output. Crouch-clicking a locked Accumulator vents 50 back into the chunk, and unlocks it once it is empty.

The Accumulator joins the network like any node: it auto-binds to a Master, or to any node already on the network, within 16 blocks. Now put an **Output Routing Node** wherever you want Aura, open its **Spiritus** tab, pick the Aspect and a **Keep** figure, and the network holds that chunk at that level, up to the chunk's cap. The node needs no adjacent block; it can stand alone in the chunk it feeds. An Omni Routing Node has the same tab.

This holds a chunk at a set level rather than evening it out with its neighbors the way a Spiritus Conduit does. One Accumulator on the farm plus one Output Routing Node per worksite keeps a ritual chunk, an Athanor chunk, and a Crystallarium chunk all topped up from a single crystal farm.

> A Spirit Accumulator in the **same chunk** as an Output Routing Node stocking the **same Aspect** will skim back whatever the node delivers above 30. Keep them in separate chunks, or set the node's Keep figure to 30 or lower.

## Running multiple Aspects

The Aura is **chunk-based** and caps at **100 per Aspect** by default (server-configurable), and a Crystallarium seeds only its chunk's highest Aspect. So farm each Aspect in its own chunk: keep that chunk biased to the Aspect by burning its crystals there, run a Crystallarium, a Spiritus Crucible and a Crystallum Fractura set to it, and carry the Aura out with Spiritus Conduits or a Spirit Accumulator on the routing network. Repeat per Aspect (Raw, Ruina, Nihilum, Vindicta, Invictus) for a full Spiritus supply.

Because an Accumulator is locked to one Aspect at a time, run one per farmed Aspect. They can all share a single network; each Output Routing Node names the Aspect it wants and draws only from the matching Accumulators.

See also: **[Spiritus, Aspects and Crystals](Spiritus-Aspects-and-Crystals)** (all the mechanics and numbers), **[Rituals](Rituals)** (Crystallum Fractura), **[Routing Network](Routing-Network)** (node setup).
