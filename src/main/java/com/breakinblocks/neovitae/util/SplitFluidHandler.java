package com.breakinblocks.neovitae.util;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class SplitFluidHandler implements IFluidHandler {

    private final IFluidHandler fillInto;
    private final IFluidHandler drainFrom;

    public SplitFluidHandler(IFluidHandler fillInto, IFluidHandler drainFrom) {
        this.fillInto = fillInto;
        this.drainFrom = drainFrom;
    }

    private boolean isFillTank(int tank) {
        return tank < fillInto.getTanks();
    }

    private int drainTank(int tank) {
        return tank - fillInto.getTanks();
    }

    @Override
    public int getTanks() {
        return fillInto.getTanks() + drainFrom.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return isFillTank(tank) ? fillInto.getFluidInTank(tank) : drainFrom.getFluidInTank(drainTank(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return isFillTank(tank) ? fillInto.getTankCapacity(tank) : drainFrom.getTankCapacity(drainTank(tank));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return isFillTank(tank) && fillInto.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fillInto.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return drainFrom.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drainFrom.drain(maxDrain, action);
    }
}
