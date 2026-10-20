package com.breakinblocks.neovitae.common.blockentity;

import net.minecraft.core.Direction;

import java.util.Locale;

public final class AthanorSides {

    private AthanorSides() {}

    public enum Face {
        FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM;

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Face of(Direction facing, Direction side) {
            if (side == Direction.UP) return TOP;
            if (side == Direction.DOWN) return BOTTOM;
            if (side == facing) return FRONT;
            if (side == facing.getOpposite()) return BACK;
            return side == facing.getClockWise() ? LEFT : RIGHT;
        }

        public Mode defaultRole() {
            return switch (this) {
                case TOP -> Mode.TOOL;
                case BOTTOM -> Mode.OUTPUTS;
                default -> Mode.INPUTS;
            };
        }
    }

    public enum Mode {
        DEFAULT, INPUTS, OUTPUTS, BOTH, TOOL, OFF;

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public Mode cycle(int step) {
            Mode[] values = values();
            return values[Math.floorMod(ordinal() + step, values.length)];
        }

        public static Mode byOrdinal(int ordinal) {
            Mode[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DEFAULT;
        }

        public static Mode byName(String name) {
            for (Mode mode : values()) {
                if (mode.getName().equals(name)) return mode;
            }
            return DEFAULT;
        }
    }
}
