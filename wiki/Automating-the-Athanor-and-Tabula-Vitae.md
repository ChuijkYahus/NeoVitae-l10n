# Automating the Athanor and Tabula Vitae

Both the [Athanor](Athanor-and-Materials) and the [Tabula Vitae](Tabula-Vitae-Flasks-and-Anointments) are sided crafting stations, so a [Routing Network](Routing-Network) (or plain hoppers) can run them unattended. They pair naturally: the Tabula Vitae brews the **Cutting Fluid** the Athanor's ore chain consumes, so a single line can turn raw ore into a steady stream of ingots.

## The Athanor

The Athanor is sided like a furnace:

- **Top** accepts only the **tool** (Cutting Fluid, Resonator, Sanguine Reverter, etc.).
- **Sides** accept the **inputs** (ore, fragments, gravel) and the fluid bucket slots.
- **Bottom** dispenses the **outputs**.

These are the defaults. Each group of slots can be opened to other sides, and the outputs pushed out of a chosen side, from the Athanor's menu; see [Athanor and Materials](Athanor-and-Materials).

A typical ore line: keep a Cutting Fluid (or the appropriate tool) in the top, pipe ore and any fluid in from the sides, and pull finished items out of the bottom into storage. Remember the chain has several steps (ore -> fragments -> gravel -> dust -> ingot) and the **Resonator** step needs the Resonator tool, so multi-stage automation may use more than one Athanor, each with its own tool.

Two things to keep supplied:

- **Fuel.** A Primitive Fuel Cell (128 operations) or a Lava Crystal (draws EV from your [Anima](Blood-Orbs-and-Anima)).
- **Raw Spiritus.** The Athanor runs at half speed with no spiritus and double speed near 100, and consumes a little while working. Keep the chunk saturated; see **[Automating Spiritus](Automating-Spiritus)**.

The Sanguine Reverter's reversion and **disenchanting** (books + an enchanted item) automate the same way: route the inputs in from the sides, pull the results from the bottom.

## The Tabula Vitae

The Tabula Vitae has **per-side slot configuration**: open its UI and assign which of its slots each face exposes, then point Routing Nodes or hoppers at the configured faces. Feed the ingredient slots and a bound **Orb of Vitae**, and pull from the output slot, or select the output slot and right-click a side button to have the table push finished items out of that side itself.

Automate it to mass-produce:

- **Cutting Fluids** for the Athanor's ore chain.
- **Tabula** slates (Rasa, Robur, Animata, and up) for downstream crafting.

The **[Hellfire Forge](Hellfire-Forge-and-Sentient-Equipment)** uses the same per-side slot configuration, gem slot included. Keep each ingredient on its own side so the forge receives one of each, and let the gem refill from the chunk's Aura or swap gems in through its configured face. It can push its output out of a chosen side the same way.

Both stations work behind an AE2 pattern provider or a Refined Storage autocrafter: expose only the ingredient slots on the provider's face, turn on the provider's blocking mode, and push the output back into the provider. For running these recipes in bulk, the **[Sanguine Crafter](Sanguine-Crafter)** crafts Tabula Vitae and Hellfire Forge recipes several at a time from one buffer.

## Tying them together

Run the Tabula Vitae's Cutting Fluid output straight into an Athanor's input side, feed ore from a separate store, and pull ingots from the bottom. With the chunk's Raw Spiritus kept topped up, the line refines ore hands-off. Cross-reference **[Routing Network](Routing-Network)** for node setup and **[Automating the Ara Vitae](Automating-the-Ara-Vitae)** for altar-side automation.
