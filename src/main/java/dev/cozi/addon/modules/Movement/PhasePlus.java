package dev.cozi.addon.modules.Movement;

import dev.cozi.addon.Main;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PhasePlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSafety = settings.createGroup("Safety");

    // General Settings
    private final Setting<Double> phaseDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("phase-distance")
        .description("Distance to phase into the block.")
        .defaultValue(0.5)
        .min(0.1)
        .max(2.0)
        .sliderRange(0.1, 2.0)
        .build()
    );

    private final Setting<Integer> phaseDelay = sgGeneral.add(new IntSetting.Builder()
        .name("phase-delay")
        .description("Ticks to wait before attempting to phase.")
        .defaultValue(5)
        .min(1)
        .max(20)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> autoPhase = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-phase")
        .description("Automatically phase when looking at a block.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> requireEnderPearl = sgGeneral.add(new BoolSetting.Builder()
        .name("require-ender-pearl")
        .description("Require ender pearl in inventory to phase.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notifyPhase = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-phase")
        .description("Send chat notification when phasing.")
        .defaultValue(true)
        .build()
    );

    // Safety Settings
    private final Setting<Boolean> checkForVoid = sgSafety.add(new BoolSetting.Builder()
        .name("check-for-void")
        .description("Prevent phasing into void.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> checkForLava = sgSafety.add(new BoolSetting.Builder()
        .name("check-for-lava")
        .description("Prevent phasing into lava.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> checkForBedrock = sgSafety.add(new BoolSetting.Builder()
        .name("check-for-bedrock")
        .description("Prevent phasing into bedrock.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> checkForBarrier = sgSafety.add(new BoolSetting.Builder()
        .name("check-for-barrier")
        .description("Prevent phasing into barrier blocks.")
        .defaultValue(true)
        .build()
    );

    // State tracking
    private int phaseTimer = 0;
    private boolean isPhasing = false;
    private BlockPos targetBlock = null;
    private Vec3d originalPos = null;

    public PhasePlus() {
        super(Main.MOVEMENT, "phase-plus", "Phase into blocks using ender pearls.");
    }

    @Override
    public void onActivate() {
        phaseTimer = 0;
        isPhasing = false;
        targetBlock = null;
        originalPos = null;
    }

    @Override
    public void onDeactivate() {
        if (isPhasing) {
            // Reset player position if still phasing
            if (mc.player != null && originalPos != null) {
                mc.player.setPosition(originalPos);
            }
        }
        resetPhaseState();
    }

    private void resetPhaseState() {
        phaseTimer = 0;
        isPhasing = false;
        targetBlock = null;
        originalPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (isPhasing) {
            handlePhasing();
            return;
        }

        if (autoPhase.get()) {
            checkForPhaseTarget();
        }
    }

    private void checkForPhaseTarget() {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
        BlockPos targetPos = hitResult.getBlockPos();
        Block targetBlockType = mc.world.getBlockState(targetPos).getBlock();

        if (isValidPhaseTarget(targetPos, targetBlockType)) {
            attemptPhase(targetPos);
        }
    }

    private boolean isValidPhaseTarget(BlockPos pos, Block block) {
        // Check if block is air or replaceable
        if (block == Blocks.AIR || block == Blocks.VOID_AIR || block == Blocks.CAVE_AIR) {
            return false;
        }

        // Safety checks
        if (checkForVoid.get()) {
            BlockPos below = pos.down();
            if (mc.world.getBlockState(below).getBlock() == Blocks.AIR) {
                // Check if there's solid ground below
                boolean hasGround = false;
                for (int i = 1; i <= 10; i++) {
                    BlockPos checkPos = below.down(i);
                    if (mc.world.getBlockState(checkPos).getBlock() != Blocks.AIR) {
                        hasGround = true;
                        break;
                    }
                }
                if (!hasGround) {
                    return false;
                }
            }
        }

        if (checkForLava.get() && block == Blocks.LAVA) {
            return false;
        }

        if (checkForBedrock.get() && block == Blocks.BEDROCK) {
            return false;
        }

        if (checkForBarrier.get() && block == Blocks.BARRIER) {
            return false;
        }

        return true;
    }

    private void attemptPhase(BlockPos targetPos) {
        if (requireEnderPearl.get()) {
            FindItemResult enderPearl = InvUtils.find(Items.ENDER_PEARL);
            if (!enderPearl.found()) {
                if (notifyPhase.get()) {
                    info("No ender pearl found in inventory!");
                }
                return;
            }
        }

        targetBlock = targetPos;
        originalPos = mc.player.getPos();
        isPhasing = true;
        phaseTimer = 0;

        if (notifyPhase.get()) {
            info("Attempting to phase into block at " + targetPos.toShortString());
        }
    }

    private void handlePhasing() {
        phaseTimer++;

        if (phaseTimer >= phaseDelay.get()) {
            if (targetBlock != null && mc.player != null) {
                // Calculate phase position
                Vec3d targetCenter = Vec3d.ofCenter(targetBlock);
                Direction facing = mc.player.getHorizontalFacing();
                
                // Calculate offset based on facing direction
                Vec3d offset = Vec3d.of(facing.getVector()).multiply(phaseDistance.get());
                Vec3d phasePos = targetCenter.add(offset);

                // Move player to phase position
                mc.player.setPosition(phasePos);
                
                if (notifyPhase.get()) {
                    info("Successfully phased into block!");
                }
            }
            
            resetPhaseState();
        }
    }

    // Public method to manually trigger phase
    public void phaseIntoBlock() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
            BlockPos targetPos = hitResult.getBlockPos();
            Block targetBlockType = mc.world.getBlockState(targetPos).getBlock();

            if (isValidPhaseTarget(targetPos, targetBlockType)) {
                attemptPhase(targetPos);
            } else {
                if (notifyPhase.get()) {
                    info("Cannot phase into this block type!");
                }
            }
        } else {
            if (notifyPhase.get()) {
                info("No valid block target found!");
            }
        }
    }
}
