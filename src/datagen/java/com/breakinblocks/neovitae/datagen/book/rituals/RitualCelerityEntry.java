package com.breakinblocks.neovitae.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.datagen.book.page.BookRitualInfoPageModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

public class RitualCelerityEntry extends EntryProvider {

    public RitualCelerityEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("multiblock", () -> BookMultiblockPageModel.create()
                .withMultiblockId(ResourceLocation.fromNamespaceAndPath(NeoVitae.MODID, "ritual/celerity"))
                .withMultiblockName("Ritual of Celerity")
                .withText(this.context().pageText()));
        this.pageText("[#](2E8B57)Use a Ritual Diviner [Tenebrae] for easier construction.[#]()");

        this.page("stats", () -> BookRitualInfoPageModel.create()
                .withText(RitualStatsHelper.generateStats("celerity")));

        this.page("info", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("The Quickened Hearth");
        this.pageText("Every [#](8B0000)Athanor[#](), [#](8B0000)Hellfire Forge[#](), [#](8B0000)Tabula Vitae[#]() and "
                + "[#](8B0000)Sanguine Crafter[#]() within the circle works at [#](8B0000)twice[#]() its usual pace. "
                + "This adds to whatever already hastens the machine, such as an Athanor's tool or a rich Aura.\\\n\\\n"
                + "A craft costs no more than it would without the ritual. The ritual itself draws "
                + "[#](4A0080)Essentia Vitae[#]() each second for every machine that is working; idle machines cost nothing.\\\n\\\n"
                + "[#](2E8B57)Its Deus form works ten times as fast.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual of Celerity";
    }

    @Override
    protected String entryDescription() {
        return "Quickens the machines of the art.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(NVBlocks.ATHANOR_BLOCK.asItem());
    }

    @Override
    protected String entryId() {
        return "ritual_celerity";
    }
}
