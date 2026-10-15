package com.breakinblocks.neovitae.client.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import com.breakinblocks.neovitae.common.network.SetOutputFacePayload;
import com.breakinblocks.neovitae.common.network.SetSideConfigPayload;
import com.breakinblocks.neovitae.common.sideconfig.OutputFaceConfigurable;
import com.breakinblocks.neovitae.common.sideconfig.SlotSideConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public class SideConfigPanel {

    private final Map<Direction, SideToggleButton> buttons = new EnumMap<>(Direction.class);
    private final BlockPos pos;
    private final SlotSideConfig config;
    private final IntSupplier activeSlot;
    private final IntFunction<Component> slotName;
    private final int pushSlot;
    @Nullable
    private final OutputFaceConfigurable pushTarget;

    public SideConfigPanel(BlockPos pos, SlotSideConfig config, IntSupplier activeSlot, IntFunction<Component> slotName,
                           int pushSlot, @Nullable OutputFaceConfigurable pushTarget) {
        this.pos = pos;
        this.config = config;
        this.activeSlot = activeSlot;
        this.slotName = slotName;
        this.pushSlot = pushSlot;
        this.pushTarget = pushTarget;
    }

    public void create(Consumer<SideToggleButton> add, int x, int y) {
        buttons.clear();
        addButton(add, x, y, Direction.DOWN, "D");
        addButton(add, x + 18, y, Direction.UP, "U");
        addButton(add, x, y + 18, Direction.NORTH, "N");
        addButton(add, x + 18, y + 18, Direction.SOUTH, "S");
        addButton(add, x, y + 36, Direction.WEST, "W");
        addButton(add, x + 18, y + 36, Direction.EAST, "E");
        tick();
    }

    private void addButton(Consumer<SideToggleButton> add, int x, int y, Direction direction, String label) {
        SideToggleButton button = new SideToggleButton(
                x, y, direction, Component.literal(label),
                () -> isAllowed(direction),
                () -> isPushing(direction),
                btn -> toggleAllowed(direction));
        button.visible = false;
        buttons.put(direction, button);
        add.accept(button);
    }

    private boolean isAllowed(Direction direction) {
        int slot = activeSlot.getAsInt();
        return slot >= 0 && config.isAllowed(slot, direction);
    }

    private boolean isPushing(Direction direction) {
        return pushTarget != null && activeSlot.getAsInt() == pushSlot && pushTarget.getOutputFace() == direction;
    }

    private void toggleAllowed(Direction direction) {
        int slot = activeSlot.getAsInt();
        if (slot < 0) return;
        boolean newState = !config.isAllowed(slot, direction);
        config.setAllowed(slot, direction, newState);
        PacketDistributor.sendToServer(new SetSideConfigPayload(pos, slot, direction.get3DDataValue(), newState));
        refreshTooltips();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 1 || pushTarget == null || activeSlot.getAsInt() != pushSlot) return false;
        for (SideToggleButton toggle : buttons.values()) {
            if (toggle.visible && toggle.isMouseOver(mouseX, mouseY)) {
                Direction face = pushTarget.getOutputFace() == toggle.getDirection() ? null : toggle.getDirection();
                pushTarget.setOutputFace(face);
                PacketDistributor.sendToServer(new SetOutputFacePayload(pos, face == null ? -1 : face.get3DDataValue()));
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, 1.0f);
                }
                refreshTooltips();
                return true;
            }
        }
        return false;
    }

    public void tick() {
        boolean active = activeSlot.getAsInt() >= 0;
        for (SideToggleButton button : buttons.values()) {
            button.visible = active;
        }
        if (active) refreshTooltips();
    }

    private void refreshTooltips() {
        int slot = activeSlot.getAsInt();
        if (slot < 0) return;
        Component name = slotName.apply(slot);
        for (Map.Entry<Direction, SideToggleButton> entry : buttons.entrySet()) {
            Direction dir = entry.getKey();
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable(
                    "gui.neovitae.tabula_vitae.side_button",
                    Component.translatable("gui.neovitae.side." + dir.getName()).withStyle(ChatFormatting.WHITE),
                    name));
            lines.add(config.isAllowed(slot, dir)
                    ? Component.translatable("gui.neovitae.tabula_vitae.side_allowed").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("gui.neovitae.tabula_vitae.side_blocked").withStyle(ChatFormatting.RED));
            lines.add(Component.translatable("gui.neovitae.tabula_vitae.side_hint").withStyle(ChatFormatting.GRAY));
            if (pushTarget != null && slot == pushSlot) {
                lines.add(isPushing(dir)
                        ? Component.translatable("gui.neovitae.side_pushing").withStyle(ChatFormatting.GOLD)
                        : Component.translatable("gui.neovitae.side_push_hint").withStyle(ChatFormatting.GRAY));
            }
            entry.getValue().setTooltip(Tooltip.create(joinLines(lines)));
        }
    }

    private static Component joinLines(List<Component> lines) {
        MutableComponent root = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) root.append("\n");
            root.append(lines.get(i));
        }
        return root;
    }
}
