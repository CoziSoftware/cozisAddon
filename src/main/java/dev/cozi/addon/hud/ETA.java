package dev.cozi.addon.hud;

import dev.cozi.addon.Main;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.math.Vec3d;
import baritone.api.BaritoneAPI;

public class ETA extends HudElement {
    public static final HudElementInfo<ETA> INFO = new HudElementInfo<>(
        Main.HUD_GROUP,
        "eta",
        "Displays estimated time of arrival to Baritone goal.",
        ETA::new
    );

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    private final Setting<Boolean> showTitle = sgGeneral.add(new BoolSetting.Builder()
        .name("show-title")
        .description("Display the HUD title.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Show distance to goal.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showSpeed = sgGeneral.add(new BoolSetting.Builder()
        .name("show-speed")
        .description("Show current speed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showETA = sgGeneral.add(new BoolSetting.Builder()
        .name("show-eta")
        .description("Show estimated time of arrival.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> textScale = sgGeneral.add(new DoubleSetting.Builder()
        .name("text-scale")
        .description("Scale of the text.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderRange(0.1, 3.0)
        .build()
    );

    private final Setting<Boolean> debugMode = sgGeneral.add(new BoolSetting.Builder()
        .name("debug-mode")
        .description("Show debug information for speed calculation.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> speedUpdateInterval = sgGeneral.add(new IntSetting.Builder()
        .name("speed-update-interval")
        .description("Minimum milliseconds between speed updates.")
        .defaultValue(180)
        .min(50)
        .max(1000)
        .sliderRange(50, 500)
        .build()
    );

    private final Setting<SettingColor> titleColor = sgColors.add(new ColorSetting.Builder()
        .name("title-color")
        .description("Color of the title.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> distanceColor = sgColors.add(new ColorSetting.Builder()
        .name("distance-color")
        .description("Color of the distance text.")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .build()
    );

    private final Setting<SettingColor> speedColor = sgColors.add(new ColorSetting.Builder()
        .name("speed-color")
        .description("Color of the speed text.")
        .defaultValue(new SettingColor(0, 255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> etaColor = sgColors.add(new ColorSetting.Builder()
        .name("eta-color")
        .description("Color of the ETA text.")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .build()
    );

    private final Setting<SettingColor> noGoalColor = sgColors.add(new ColorSetting.Builder()
        .name("no-goal-color")
        .description("Color when no Baritone goal is set.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );

    // Speed tracking variables
    private Vec3d lastPosition = null;
    private long lastUpdateTime = 0;
    private double currentSpeed = 0.0;
    private double smoothedSpeed = 0.0;

    public ETA() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (MeteorClient.mc.player == null || MeteorClient.mc.world == null) {
            if (isInEditor()) {
                renderer.text("ETA", x, y, titleColor.get(), true, textScale.get());
                setSize(renderer.textWidth("ETA", true, textScale.get()), renderer.textHeight(true, textScale.get()));
            }
            return;
        }

        try {
            // Check if Baritone is available
            Class.forName("baritone.api.BaritoneAPI");
        } catch (ClassNotFoundException e) {
            renderNoBaritone(renderer);
            return;
        }

        // Get Baritone goal
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        var goal = baritone.getCustomGoalProcess().getGoal();

        if (goal == null) {
            renderNoGoal(renderer);
            return;
        }

        // Calculate current speed
        updateSpeed();

        // Get goal position
        Vec3d goalPos = getGoalPosition(goal);
        if (goalPos == null) {
            renderNoGoal(renderer);
            return;
        }

        // Calculate distance
        Vec3d playerPos = MeteorClient.mc.player.getPos();
        double distance = playerPos.distanceTo(goalPos);

        // Calculate ETA
        String etaText = calculateETA(distance);

        // Render the HUD
        renderETA(renderer, distance, etaText);
    }

    private void updateSpeed() {
        Vec3d currentPos = MeteorClient.mc.player.getPos();
        long currentTime = System.currentTimeMillis();

        // Only update speed if enough time has passed since last update
        if (lastPosition != null && lastUpdateTime != 0 && 
            (currentTime - lastUpdateTime) >= speedUpdateInterval.get()) {
            
            double timeDiff = (currentTime - lastUpdateTime) / 1000.0; // Convert to seconds
            if (timeDiff > 0) {
                double distance = currentPos.distanceTo(lastPosition);
                double newSpeed = distance / timeDiff; // blocks per second
                
                // Smooth the speed calculation to reduce jitter
                if (smoothedSpeed == 0.0) {
                    smoothedSpeed = newSpeed;
                } else {
                    smoothedSpeed = smoothedSpeed * 0.7 + newSpeed * 0.3; // Exponential smoothing
                }
                currentSpeed = smoothedSpeed;
            }
            
            // Update tracking variables only after calculating speed
            lastPosition = currentPos;
            lastUpdateTime = currentTime;
        } else if (lastPosition == null) {
            // Initialize on first call
            lastPosition = currentPos;
            lastUpdateTime = currentTime;
        }
    }

    private Vec3d getGoalPosition(baritone.api.pathing.goals.Goal goal) {
        if (goal instanceof baritone.api.pathing.goals.GoalXZ goalXZ) {
            return new Vec3d(goalXZ.getX(), MeteorClient.mc.player.getY(), goalXZ.getZ());
        } else if (goal instanceof baritone.api.pathing.goals.GoalBlock goalBlock) {
            return new Vec3d(goalBlock.getGoalPos().getX(), goalBlock.getGoalPos().getY(), goalBlock.getGoalPos().getZ());
        } else if (goal instanceof baritone.api.pathing.goals.GoalNear goalNear) {
            return new Vec3d(goalNear.getGoalPos().getX(), goalNear.getGoalPos().getY(), goalNear.getGoalPos().getZ());
        }
        return null;
    }

    private String calculateETA(double distance) {
        if (currentSpeed <= 0.01) { // Very slow or stationary
            return "∞";
        }

        double timeInSeconds = distance / currentSpeed;
        
        if (timeInSeconds < 60) {
            return String.format("%.1fs", timeInSeconds);
        } else if (timeInSeconds < 3600) {
            int minutes = (int) (timeInSeconds / 60);
            int seconds = (int) (timeInSeconds % 60);
            return String.format("%dm %ds", minutes, seconds);
        } else {
            int hours = (int) (timeInSeconds / 3600);
            int minutes = (int) ((timeInSeconds % 3600) / 60);
            return String.format("%dh %dm", hours, minutes);
        }
    }

    private void renderETA(HudRenderer renderer, double distance, String etaText) {
        double curX = this.x;
        double curY = this.y;
        double scale = textScale.get();
        double maxWidth = 0;
        double height = 0;
        double textHeight = renderer.textHeight(true, scale);
        double spacing = 2;

        if (showTitle.get()) {
            String title = "ETA";
            double titleWidth = renderer.textWidth(title, true, scale);
            renderer.text(title, curX, curY, titleColor.get(), true, scale);
            curY += textHeight + spacing;
            height += textHeight + spacing;
            maxWidth = Math.max(maxWidth, titleWidth);
        }

        if (showDistance.get()) {
            String distanceText = String.format("Distance: %.1f", distance);
            double distanceWidth = renderer.textWidth(distanceText, true, scale);
            renderer.text(distanceText, curX, curY, distanceColor.get(), true, scale);
            curY += textHeight + spacing;
            height += textHeight + spacing;
            maxWidth = Math.max(maxWidth, distanceWidth);
        }

        if (showSpeed.get()) {
            String speedText = String.format("Speed: %.1f b/s", currentSpeed);
            double speedWidth = renderer.textWidth(speedText, true, scale);
            renderer.text(speedText, curX, curY, speedColor.get(), true, scale);
            curY += textHeight + spacing;
            height += textHeight + spacing;
            maxWidth = Math.max(maxWidth, speedWidth);
        }

        if (showETA.get()) {
            String etaDisplayText = String.format("ETA: %s", etaText);
            double etaWidth = renderer.textWidth(etaDisplayText, true, scale);
            renderer.text(etaDisplayText, curX, curY, etaColor.get(), true, scale);
            curY += textHeight + spacing;
            height += textHeight + spacing;
            maxWidth = Math.max(maxWidth, etaWidth);
        }

        if (debugMode.get()) {
            String debugText = String.format("Debug: LastPos=%s, TimeDiff=%dms, SmoothedSpeed=%.2f", 
                lastPosition != null ? String.format("%.1f,%.1f,%.1f", lastPosition.x, lastPosition.y, lastPosition.z) : "null",
                lastUpdateTime != 0 ? (int)(System.currentTimeMillis() - lastUpdateTime) : 0,
                smoothedSpeed);
            double debugWidth = renderer.textWidth(debugText, true, scale);
            renderer.text(debugText, curX, curY, new SettingColor(128, 128, 128, 255), true, scale);
            height += textHeight;
            maxWidth = Math.max(maxWidth, debugWidth);
        }

        setSize(maxWidth, height);
    }

    private void renderNoGoal(HudRenderer renderer) {
        double curX = this.x;
        double curY = this.y;
        double scale = textScale.get();
        double maxWidth = 0;
        double height = 0;
        double textHeight = renderer.textHeight(true, scale);
        double spacing = 2;

        if (showTitle.get()) {
            String title = "ETA";
            double titleWidth = renderer.textWidth(title, true, scale);
            renderer.text(title, curX, curY, titleColor.get(), true, scale);
            curY += textHeight + spacing;
            height += textHeight + spacing;
            maxWidth = Math.max(maxWidth, titleWidth);
        }

        String noGoalText = "No Baritone goal set";
        double noGoalWidth = renderer.textWidth(noGoalText, true, scale);
        renderer.text(noGoalText, curX, curY, noGoalColor.get(), true, scale);
        height += textHeight;
        maxWidth = Math.max(maxWidth, noGoalWidth);

        setSize(maxWidth, height);
    }

    private void renderNoBaritone(HudRenderer renderer) {
        double curX = this.x;
        double curY = this.y;
        double scale = textScale.get();
        double maxWidth = 0;
        double height = 0;
        double textHeight = renderer.textHeight(true, scale);
        double spacing = 2;

        if (showTitle.get()) {
            String title = "ETA";
            double titleWidth = renderer.textWidth(title, true, scale);
            renderer.text(title, curX, curY, titleColor.get(), true, scale);
            curY += textHeight + spacing;
            height += textHeight + spacing;
            maxWidth = Math.max(maxWidth, titleWidth);
        }

        String noBaritoneText = "Baritone not available";
        double noBaritoneWidth = renderer.textWidth(noBaritoneText, true, scale);
        renderer.text(noBaritoneText, curX, curY, noGoalColor.get(), true, scale);
        height += textHeight;
        maxWidth = Math.max(maxWidth, noBaritoneWidth);

        setSize(maxWidth, height);
    }
}
