package com.breakinblocks.neovitae.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.breakinblocks.neovitae.common.world.BoundTreasureLeases;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z"
            )
    )
    private boolean neovitae$keepBoundTreasureOpen(boolean original) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        return original || BoundTreasureLeases.keepOpen(self, self.containerMenu);
    }

    @ModifyExpressionValue(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"
            )
    )
    private AbstractContainerMenu neovitae$blockUnreachableBoundTreasureMenu(AbstractContainerMenu original) {
        if (original != null && BoundTreasureLeases.shouldBlockMenu(original)) {
            original.removed((ServerPlayer) (Object) this);
            return null;
        }
        return original;
    }
}
