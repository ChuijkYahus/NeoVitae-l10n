package com.breakinblocks.neovitae.datagen.book.alchemy_array;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.datagen.book.page.BookAthanorRecipePageModel;
import com.breakinblocks.neovitae.datagen.book.page.BookSentientUpgradeTablePageModel;
import com.breakinblocks.neovitae.datagen.book.page.BookTabulaVitaeRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class UnboundedFlightUpgradeEntry extends EntryProvider {

    public UnboundedFlightUpgradeEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookSentientUpgradeTablePageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Unbounded Flight");
        this.pageText("The armor no longer glides on borrowed wings. It lifts its wearer freely, rising, hovering and "
                + "turning at will. This upgrade cannot be trained; it must be inscribed via "
                + "[#](8B0000)Tome of Unbounded Flight[#]().");

        this.page("details", () -> BookTextPageModel.create()
                .withText(this.context().pageText()));
        this.pageText("[#](B8860B)Requires[#](): Sentient Armor that has completed both the [#](8B0000)Ritual of "
                + "Sentient Evolution[#]() and the [#](8B0000)Deus Sentient Evolution[#](). Less evolved armor "
                + "rejects the tome.\\\n\\\n"
                + "Costs [#](4A0080)20 Upgrade Points[#](). While you are in the air, the armor draws "
                + "[#](4A0080)5 EV per tick[#]() from your soul network. Nothing is drawn on the ground or in "
                + "water. When the network runs dry, you fall.");

        this.page("deus_shard", () -> BookTextPageModel.create()
                .withText(this.context().pageText()));
        this.pageText("The tome is bound around a [#](8B0000)Deus Shard[#](). Wrap eight [#](8B0000)Revenant Plates[#]() "
                + "around [#](8B0000)Hellforged Parts[#]() to make [#](8B0000)Revenant Plating[#](), then lay four of them "
                + "and a Nether Star in an [#](8B0000)Athanor[#]() guided by a Divinus Orb of Vitae. The work drinks "
                + "10,000 mB of Essentia Vitae and [#](4A0080)20 of every Spiritus[#]() from the chunk.");

        this.page("plating_recipe", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath(NeoVitae.MODID, "revenant_plating")));

        this.page("deus_shard_recipe", () -> BookAthanorRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath(NeoVitae.MODID, "athanor/orb/deus_shard")));

        this.page("recipe", () -> BookTabulaVitaeRecipePageModel.create()
                .withRecipeId1("neovitae:alchemytable/unbounded_flight_tome"));
    }

    @Override
    protected String entryName() {
        return "Unbounded Flight";
    }

    @Override
    protected String entryDescription() {
        return "Fully evolved armor that carries its wearer through the sky.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.FEATHER);
    }

    @Override
    protected String entryId() {
        return "upgrade_unbounded_flight";
    }
}
