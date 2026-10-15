package com.breakinblocks.neovitae.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import net.minecraft.resources.ResourceLocation;
import com.mojang.datafixers.util.Pair;

public class RitualAnchorEntry extends EntryProvider {

    public RitualAnchorEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual Anchor");
        this.pageText("A ritual works around its [#](8B0000)Master Ritual Stone[#](), which makes it hard to fit several circles around one spot. The [#](8B0000)Ritual Anchor[#]() lends a ritual a new center.\\\n\\\n"
                + "Right-click the anchor with a [#](8B0000)Ritual Configurator[#](), then right-click a Master Ritual Stone within 64 blocks. From then on the ritual works around the anchor instead: its areas, the Spiritus it draws, and the streams it sends all start from the crystal.");

        this.page("linking", () -> BookTextPageModel.create()
                .withText(this.context().pageText()));
        this.pageText("Any number of stones can share one anchor, so you can build the circles out of the way and stack their effects on a single room.\\\n\\\n"
                + "Repeat the link with the same anchor to unlink a stone. A redstone signal on the anchor pauses every ritual linked to it, while a signal on one stone pauses only that ritual.\\\n\\\n"
                + "[#](2E8B57)If the anchor is broken, its rituals stop. Dungeon rituals cannot use an anchor.[#]()");

        this.page("glow", () -> BookTextPageModel.create()
                .withText(this.context().pageText()));
        this.pageText("The crystal stays dark until a linked ritual is running. It then pulses in the color of the Spiritus its rituals draw, or blood red when they draw none or more than one kind.\\\n\\\n"
                + "Look at the anchor while carrying a [#](8B0000)Seer's Sigil[#]() to see the areas of every ritual linked to it.");

        this.page("crafting", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath(NeoVitae.MODID, "ritual_anchor")));
    }

    @Override
    protected String entryName() {
        return "Ritual Anchor";
    }

    @Override
    protected String entryDescription() {
        return "Give a ritual a new center.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(NVBlocks.RITUAL_ANCHOR.asItem());
    }

    @Override
    protected String entryId() {
        return "ritual_anchor";
    }
}
