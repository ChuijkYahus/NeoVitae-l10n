package com.breakinblocks.neovitae.datagen.book.tabula_vitae;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.neovitae.datagen.book.page.BookFlaskRecipePageModel;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.common.item.potion.ItemAlchemyFlask;
import com.breakinblocks.neovitae.common.datacomponent.FlaskEffects;
import com.breakinblocks.neovitae.common.datacomponent.EffectHolder;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import net.minecraft.world.item.ItemStack;
import com.mojang.datafixers.util.Pair;

public class SoulSnareFlaskEntry extends EntryProvider {

    public SoulSnareFlaskEntry(CategoryProviderBase parent) {
        super(parent);
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Soul Snare");
        this.pageText("The [#](8B0000)Soul Snare[#]() elixir binds a hostile creature's spirit to its body. "
                + "If a snared monster dies by your hand, or soon after you have struck it, its spirit is torn free "
                + "as [#](4A0080)Spiritus Essence[#](), more from creatures with greater health.\\\n\\\n"
                + "Thrown as a splash or lingering flask, it snares a whole group at once, which you may then "
                + "cut down with any weapon.\\\n\\\n"
                + "[#](2E8B57)A monster that dies without you, to a trap or a fall, gives up nothing.[#]()");

        this.page("recipe1", () -> BookFlaskRecipePageModel.create()
                .withRecipeId1("neovitae:flask/spiritus_snare")
                .withRecipeId2("neovitae:flask/length_spiritus_snare"));
        this.page("recipe2", () -> BookFlaskRecipePageModel.create()
                .withRecipeId1("neovitae:flask/length_average_spiritus_snare"));
    }

    @Override
    protected String entryName() {
        return "Soul Snare";
    }

    @Override
    protected String entryDescription() {
        return "Snares hostile creatures so they give up Spiritus Essence when slain.";
    }

    @Override
    protected Pair<Integer, Integer> entryBackground() {
        return EntryBackground.DEFAULT;
    }

    @Override
    protected BookIconModel entryIcon() {
        ItemStack flask = new ItemStack(NVItems.ALCHEMY_FLASK.get());
        ItemAlchemyFlask.setFlaskEffects(flask, FlaskEffects.single(EffectHolder.create(NVMobEffects.SPIRITUS_SNARE.getDelegate(), 3600, 0)));
        return BookIconModel.create(flask);
    }

    @Override
    protected String entryId() {
        return "flask_soul_snare";
    }
}
