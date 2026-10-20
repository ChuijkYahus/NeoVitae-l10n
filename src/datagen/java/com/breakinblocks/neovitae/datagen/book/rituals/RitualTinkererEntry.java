package com.breakinblocks.neovitae.datagen.book.rituals;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.item.NVItems;
import net.minecraft.resources.ResourceLocation;
import com.mojang.datafixers.util.Pair;

public class RitualTinkererEntry extends EntryProvider {

    public RitualTinkererEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Ritual Configurator");
        this.pageText("The [#](8B0000)Ritual Configurator[#]() is an essential instrument for the practitioner who demands mastery over their [#](4A0080)ritual circles[#](). Right-click a [#](8B0000)Master Ritual Stone[#]() with it to open its panel, which names the ritual and lists every zone it uses with that zone's current size. Hover a zone to see how far it can reach.");

        this.page("crafting", () -> BookCraftingRecipePageModel.create()
                .withRecipeId1(ResourceLocation.fromNamespaceAndPath(NeoVitae.MODID, "ritual_reader")));

        this.page("modes", () -> BookTextPageModel.create()
                .withText(this.context().pageText()));
        this.pageText("- [#](8B0000)Edit area in world[#](): Select a zone in the panel and press this, then click two opposite corners to reshape it. A zone that is a single block, such as a chest, takes one click. Press Sneak and Use in the air to cancel."
                + "\n\n- [#](8B0000)Ritual settings[#](): A few rituals add their own controls: an [#](8B0000)Aspect[#]() row for Crystallum Fractura, a [#](8B0000)Fill shape[#]() for the Ritual of the Mason, a [#](8B0000)Mining[#]() choice for the Deus Endless Quarry, and a [#](8B0000)Keep per species[#]() count for the Ritual of Butchering. Each is described on its ritual's page.");

        this.page("define_area", () -> BookTextPageModel.create()
                .withText(this.context().pageText()));
        this.pageText("Rituals that draw from or deposit into a container list their [#](8B0000)chest[#](), [#](8B0000)tank[#](), or input/output zone in the panel too, so you can place that container wherever you like instead of directly atop the [#](8B0000)Master Ritual Stone[#]()."
                + "\\\n\\\nThe Configurator also links a ritual to a [#](8B0000)Ritual Anchor[#](): right-click the anchor, then the Master Ritual Stone."
                + "\\\n\\\n[#](2E8B57)Some rituals can be expanded far beyond their default range, but the EV cost scales to match. Tread carefully with your reserves.[#]()");
    }

    @Override
    protected String entryName() {
        return "Ritual Configurator";
    }

    @Override
    protected String entryDescription() {
        return "Fine-tune the reach and resonance of your circles.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(NVItems.RITUAL_READER.get());
    }

    @Override
    protected String entryId() {
        return "ritual_tinkerer";
    }
}
