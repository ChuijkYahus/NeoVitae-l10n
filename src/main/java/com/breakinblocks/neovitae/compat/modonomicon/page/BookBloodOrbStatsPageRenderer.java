package com.breakinblocks.neovitae.compat.modonomicon.page;

import com.breakinblocks.neovitae.common.datamap.BloodOrb;
import com.breakinblocks.neovitae.common.datamap.NVDataMaps;
import com.klikli_dev.modonomicon.client.gui.book.entry.BookEntryScreen;
import com.klikli_dev.modonomicon.client.render.page.BookPageRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BookBloodOrbStatsPageRenderer extends BookPageRenderer<BookBloodOrbStatsPage> {

    private static final int ROW_HEIGHT = 20;
    private static final int ICON_X = 0;
    private static final int TEXT_X = 19;
    private static final int TITLE_HEIGHT = 14;

    public BookBloodOrbStatsPageRenderer(BookBloodOrbStatsPage page) {
        super(page);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int top = 0;
        if (this.page.hasTitle()) {
            this.renderTitle(guiGraphics, this.page.getTitle(), false, BookEntryScreen.PAGE_WIDTH / 2, 0);
            top = TITLE_HEIGHT;
        }

        List<OrbEntry> entries = collectOrbs();
        if (entries.isEmpty()) {
            return;
        }

        float scale = Math.min(1.0F, (float) (BookEntryScreen.PAGE_HEIGHT - top) / (entries.size() * ROW_HEIGHT));
        int textWidth = (int) ((BookEntryScreen.PAGE_WIDTH - TEXT_X * scale) / scale);
        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) ((mouseY - top) / scale);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, top, 0);
        guiGraphics.pose().scale(scale, scale, 1.0F);

        int y = 0;
        for (OrbEntry entry : entries) {
            this.parentScreen.renderItemStack(guiGraphics, ICON_X, y + 1, scaledMouseX, scaledMouseY, entry.stack);

            Component name = entry.stack.getHoverName().copy();
            renderFitted(guiGraphics, name, y, textWidth, 0x4A0080);

            Component line = Component.literal(String.format("T%d \u00B7 %s EV \u00B7 %s mB \u00B7 %d/t",
                    entry.orb.tier(),
                    formatNumber(entry.orb.animaCapacity()),
                    formatNumber(entry.orb.fluidCapacity()),
                    entry.orb.fillRate()));
            renderFitted(guiGraphics, line, y + 10, textWidth, 0x555555);
            y += ROW_HEIGHT;
        }

        guiGraphics.pose().popPose();
    }

    private void renderFitted(GuiGraphics guiGraphics, Component text, int y, int maxWidth, int color) {
        int width = this.font.width(text);
        if (width <= maxWidth) {
            guiGraphics.drawString(this.font, text, TEXT_X, y, color, false);
            return;
        }
        float lineScale = (float) maxWidth / width;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(TEXT_X, y + (this.font.lineHeight * (1.0F - lineScale)) / 2.0F, 0);
        guiGraphics.pose().scale(lineScale, lineScale, 1.0F);
        guiGraphics.drawString(this.font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    private static List<OrbEntry> collectOrbs() {
        List<OrbEntry> list = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            BloodOrb orb = BuiltInRegistries.ITEM.wrapAsHolder(item).getData(NVDataMaps.BLOOD_ORB_STATS);
            if (orb != null) {
                list.add(new OrbEntry(new ItemStack(item), orb));
            }
        }
        list.sort(Comparator.comparingInt(e -> e.orb.tier()));
        return list;
    }

    private static String formatNumber(int value) {
        if (value >= 1_000_000) {
            return compact(value / 1_000_000.0) + "M";
        }
        if (value >= 1_000) {
            return compact(value / 1_000.0) + "k";
        }
        return Integer.toString(value);
    }

    private static String compact(double value) {
        if (value == Math.floor(value)) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public Style getClickedComponentStyleAt(double x, double y) {
        return null;
    }

    private record OrbEntry(ItemStack stack, BloodOrb orb) {}
}
