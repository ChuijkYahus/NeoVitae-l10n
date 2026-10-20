package com.breakinblocks.neovitae.gametest;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.registry.NVRegistries;
import com.breakinblocks.neovitae.common.sentient.SentientEffectComponents;
import com.breakinblocks.neovitae.common.sentient.SentientUpgrade;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class SentientClientSyncTests {

    private static SentientUpgrade upgrade(GameTestHelper helper, String name) {
        HolderLookup.RegistryLookup<SentientUpgrade> lookup = helper.getLevel().registryAccess().lookupOrThrow(NVRegistries.Keys.SENTIENT_UPGRADES);
        return lookup.getOrThrow(ResourceKey.create(NVRegistries.Keys.SENTIENT_UPGRADES, NeoVitae.rl(name))).value();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void clientCodecKeepsElytraFromDataFile(GameTestHelper helper) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        JsonElement file = SentientUpgrade.CODEC.encodeStart(ops, upgrade(helper, "elytra")).getOrThrow();
        SentientUpgrade client = SentientUpgrade.CLIENT_CODEC.parse(ops, file).getOrThrow();
        helper.assertTrue(client.effects().has(SentientEffectComponents.ELYTRA.get()),
                "A client loading the elytra upgrade from its own data must still see the elytra flag");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void clientCodecKeepsElytraOverNetwork(GameTestHelper helper) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
        Tag sent = SentientUpgrade.CLIENT_CODEC.encodeStart(ops, upgrade(helper, "elytra")).getOrThrow();
        SentientUpgrade client = SentientUpgrade.CLIENT_CODEC.parse(ops, sent).getOrThrow();
        helper.assertTrue(client.effects().has(SentientEffectComponents.ELYTRA.get()),
                "A client receiving the elytra upgrade over the network must see the elytra flag");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void clientCodecSkipsComplexEffects(GameTestHelper helper) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        JsonElement file = SentientUpgrade.CODEC.encodeStart(ops, upgrade(helper, "speed")).getOrThrow();
        SentientUpgrade client = SentientUpgrade.CLIENT_CODEC.parse(ops, file).getOrThrow();
        helper.assertTrue(client.effects().isEmpty(), "Only on/off flags should reach the client, got " + client.effects());
        helper.succeed();
    }
}
