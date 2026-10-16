package com.breakinblocks.neovitae.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.BloodOrbItem;

public class SanguineCrafterMenu extends AbstractBlockEntityMenu<SanguineCrafterBlockEntity> {

    public static final int TEMPLATE_START = SanguineCrafterBlockEntity.SLOT_COUNT;
    public static final int PLAYER_START = TEMPLATE_START + SanguineCrafterBlockEntity.TEMPLATE_SIZE;

    public static final int DATA_STATUS = 0;
    public static final int DATA_PROGRESS = 1;
    public static final int DATA_CHUNK_LOADING = 2;
    public static final int DATA_PARALLEL = 3;
    public static final int DATA_SPEED = 4;
    public static final int DATA_COUNT = 5;

    private final ContainerData data = new ContainerData() {
        private final int[] client = new int[DATA_COUNT];

        @Override
        public int get(int index) {
            if (tile.getLevel() != null && !tile.getLevel().isClientSide()) {
                return switch (index) {
                    case DATA_STATUS -> tile.getStatus().ordinal();
                    case DATA_PROGRESS -> tile.getCraftTime() <= 0 ? 0 : tile.getProgress() * 1000 / tile.getCraftTime();
                    case DATA_CHUNK_LOADING -> tile.getChunkLoading().ordinal();
                    case DATA_PARALLEL -> tile.getParallel();
                    case DATA_SPEED -> (int) Math.round(tile.getEffectiveSpeedMultiplier() * 100);
                    default -> 0;
                };
            }
            return client[index];
        }

        @Override
        public void set(int index, int value) {
            client[index] = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public SanguineCrafterMenu(int containerId, Inventory playerInventory, SanguineCrafterBlockEntity tile) {
        super(NVMenus.SANGUINE_CRAFTER.get(), containerId, tile, PLAYER_START);

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new SlotItemHandler(tile.inv, SanguineCrafterBlockEntity.INPUT_START + r * 9 + c, 8 + 18 * c, 112 + 18 * r));
            }
        }
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                this.addSlot(new SlotItemHandler(tile.inv, SanguineCrafterBlockEntity.OUTPUT_START + r * 3 + c, 94 + 18 * c, 18 + 18 * r) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        this.addSlot(new SlotItemHandler(tile.inv, SanguineCrafterBlockEntity.ORB_SLOT, 158, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BloodOrbItem;
            }
        });
        this.addSlot(new SlotItemHandler(tile.inv, SanguineCrafterBlockEntity.GEM_SLOT, 176, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(NVDataComponents.SPIRITUS_AMOUNT);
            }
        });

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                this.addSlot(new AbstractGhostMenu.GhostSlot(tile.template, r * 3 + c, 8 + 18 * c, 18 + 18 * r));
            }
        }

        MenuSlotHelper.addPlayerInventory(this::addSlot, playerInventory, 162, 220);
        this.addDataSlots(data);
    }

    public SanguineCrafterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, (SanguineCrafterBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getData(int index) {
        return data.get(index);
    }

    public static boolean isTemplateSlot(int index) {
        return index >= TEMPLATE_START && index < PLAYER_START;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isTemplateSlot(slotId)) {
            Slot slot = this.getSlot(slotId);
            ItemStack carried = this.getCarried();
            if (button == 0 && !carried.isEmpty()) {
                slot.set(carried.copyWithCount(1));
                if (tile.getMode() == SanguineCrafterBlockEntity.Mode.AUTO) {
                    tile.setMode(SanguineCrafterBlockEntity.Mode.CRAFTING);
                }
            } else if (button == 0 || button == 1) {
                slot.set(ItemStack.EMPTY);
            }
            return;
        }
        if (slotId >= 0 && slotId < SanguineCrafterBlockEntity.SLOT_COUNT) {
            Slot slot = this.getSlot(slotId);
            if (!slot.hasItem() && this.getCarried().isEmpty()) {
                int group = SanguineCrafterBlockEntity.groupOf(slotId);
                tile.activeGroup = tile.activeGroup == group ? -1 : group;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    protected boolean handleQuickMoveStack(int index, ItemStack slotStack, ItemStack originalCopy, Slot slot) {
        if (isTemplateSlot(index)) {
            return false;
        }
        if (index >= SanguineCrafterBlockEntity.OUTPUT_START && index < SanguineCrafterBlockEntity.OUTPUT_START + SanguineCrafterBlockEntity.OUTPUT_COUNT) {
            if (!moveToPlayer(slotStack, true)) {
                return false;
            }
            slot.onQuickCraft(slotStack, originalCopy);
        } else if (isPlayerSlot(index)) {
            int target = targetSlotFor(slotStack);
            if (target >= 0 && moveToTileSlots(slotStack, target, target + 1)) {
                return true;
            }
            if (!moveToTileSlots(slotStack, SanguineCrafterBlockEntity.INPUT_START,
                    SanguineCrafterBlockEntity.INPUT_START + SanguineCrafterBlockEntity.INPUT_COUNT)) {
                return false;
            }
        } else if (!moveToPlayer(slotStack, false)) {
            return false;
        }
        return true;
    }

    private static int targetSlotFor(ItemStack stack) {
        if (stack.getItem() instanceof BloodOrbItem) return SanguineCrafterBlockEntity.ORB_SLOT;
        if (stack.has(NVDataComponents.SPIRITUS_AMOUNT)) return SanguineCrafterBlockEntity.GEM_SLOT;
        return -1;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()), player, NVBlocks.SANGUINE_CRAFTER.block().get());
    }
}
