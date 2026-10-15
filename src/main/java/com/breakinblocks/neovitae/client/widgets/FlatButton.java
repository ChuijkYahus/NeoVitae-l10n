package com.breakinblocks.neovitae.client.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FlatButton extends AbstractButton {

    private static final int FILL = 0xFF2A1416;
    private static final int BORDER_HOVER = 0xFFFFFFFF;
    private static final int BORDER_IDLE = 0xFF5A1C22;
    private static final int TEXT_COLOR = 0xFFE0D0D0;

    private final Supplier<Component> label;
    private final Supplier<ItemStack> icon;
    private final Consumer<FlatButton> onPress;

    public FlatButton(int x, int y, int width, int height, Supplier<Component> label, Consumer<FlatButton> onPress) {
        this(x, y, width, height, label, () -> ItemStack.EMPTY, onPress);
    }

    public FlatButton(int x, int y, int width, int height, Supplier<Component> label, Supplier<ItemStack> icon,
                      Consumer<FlatButton> onPress) {
        super(x, y, width, height, label.get());
        this.label = label;
        this.icon = icon;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        int border = isHoveredOrFocused() ? BORDER_HOVER : BORDER_IDLE;

        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, FILL);
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        g.fill(x, y, x + 1, y + h, border);
        g.fill(x + w - 1, y, x + w, y + h, border);

        ItemStack stack = icon.get();
        if (!stack.isEmpty()) {
            g.renderItem(stack, x + (w - 16) / 2, y + (h - 16) / 2);
            return;
        }
        Font font = Minecraft.getInstance().font;
        setMessage(label.get());
        g.drawCenteredString(font, getMessage(), x + w / 2, y + (h - 8) / 2, TEXT_COLOR);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
