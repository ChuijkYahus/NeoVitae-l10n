package com.breakinblocks.neovitae.api.routing;

import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import org.jetbrains.annotations.Nullable;

/**
 * A routing node that stocks the Spiritus Aura of its own chunk from the network.
 */
public interface ISpiritusExportNode {

    @Nullable
    SpiritusType getSpiritusExportType();

    boolean isSpiritusExportAll();

    int getSpiritusStockTarget();

    void setSpiritusExport(@Nullable SpiritusType type, int stockTarget);

    void setSpiritusExportAll(int stockTarget);

    default boolean exportsSpiritus() {
        return isSpiritusExportAll() || getSpiritusExportType() != null;
    }

    default int getSpiritusExportIndex() {
        if (isSpiritusExportAll()) return SpiritusType.values().length + 1;
        SpiritusType type = getSpiritusExportType();
        return type == null ? 0 : type.ordinal() + 1;
    }

    default void setSpiritusExportIndex(int index, int stockTarget) {
        SpiritusType[] types = SpiritusType.values();
        int clamped = Math.max(0, Math.min(types.length + 1, index));
        if (clamped == types.length + 1) {
            setSpiritusExportAll(stockTarget);
        } else {
            setSpiritusExport(clamped == 0 ? null : types[clamped - 1], stockTarget);
        }
    }

    default void cycleSpiritusType(int direction) {
        int states = SpiritusType.values().length + 2;
        setSpiritusExportIndex(Math.floorMod(getSpiritusExportIndex() + (direction < 0 ? -1 : 1), states), getSpiritusStockTarget());
    }

    default void adjustSpiritusStock(int delta) {
        setSpiritusExportIndex(getSpiritusExportIndex(), Math.min(1000, Math.max(0, getSpiritusStockTarget() + delta)));
    }
}
