package dev.cozi.addon;

import com.mojang.logging.LogUtils;
import dev.cozi.addon.hud.EntityList;
import dev.cozi.addon.hud.TotemCount;
import dev.cozi.addon.hud.CrystalCount;
import dev.cozi.addon.hud.DubCountGUI;
import dev.cozi.addon.hud.SignDisplay;
import dev.cozi.addon.modules.Hunting.NewChunksPlus;
import dev.cozi.addon.modules.Movement.AFKVanillaFly;
import dev.cozi.addon.modules.Movement.ElytraRedeploy;
import dev.cozi.addon.modules.Movement.ElytraFlyPlusPlus;
import dev.cozi.addon.modules.Movement.GrimScaffold;
import dev.cozi.addon.modules.Utility.GrimAirPlace;
import dev.cozi.addon.modules.Hunting.StashFinderPlus;
import dev.cozi.addon.modules.Hunting.TrailFollower;
import dev.cozi.addon.modules.Movement.Pitch40Util;
import dev.cozi.addon.modules.Movement.searcharea.SearchArea;
import dev.cozi.addon.modules.Render.PearlOwner;
import dev.cozi.addon.modules.Render.SignRender;
import dev.cozi.addon.modules.Utility.AntiSpam;
import dev.cozi.addon.modules.Utility.AutoLogPlus;
import dev.cozi.addon.modules.Utility.AutoShulker;
import dev.cozi.addon.modules.Utility.DiscordNotifications;
import dev.cozi.addon.modules.Utility.DubCount;
import dev.cozi.addon.modules.Utility.PortalMaker;
import dev.cozi.addon.modules.Utility.ElytraSwap;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Main extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category UTILS = new Category("cozisUtil");
    public static final Category RENDER = new Category("cozisRender");
    public static final Category MOVEMENT = new Category("cozisMovement");
    public static final Category HUNTING = new Category("cozisHunting");
    public static final HudGroup HUD_GROUP = new HudGroup("cozisAddon");


    @Override
    public void onInitialize() {
        LOG.info("Initializing cozisAddon");

        // Modules
         Modules.get().add(new PortalMaker());
         Modules.get().add(new DiscordNotifications());
         Modules.get().add(new StashFinderPlus());
         Modules.get().add(new Pitch40Util());
         Modules.get().add(new NewChunksPlus());
         Modules.get().add(new PearlOwner());
         Modules.get().add(new SignRender());
         Modules.get().add(new SearchArea());
         Modules.get().add(new AntiSpam());
         Modules.get().add(new AutoLogPlus());
         Modules.get().add(new AFKVanillaFly());
         Modules.get().add(new AutoShulker());
         Modules.get().add(new ElytraRedeploy());
         Modules.get().add(new DubCount());
         Modules.get().add(new GrimScaffold());
         Modules.get().add(new GrimAirPlace());
         Modules.get().add(new ElytraSwap());
         
         // Only add modules that require Baritone if Baritone is available
         try {
             Class.forName("baritone.api.BaritoneAPI");
             Modules.get().add(new ElytraFlyPlusPlus());
             Modules.get().add(new TrailFollower());
             LOG.info("ElytraFlyPlusPlus and TrailFollower loaded (Baritone detected)");
         } catch (ClassNotFoundException e) {
             LOG.info("ElytraFlyPlusPlus and TrailFollower not loaded (Baritone not found)");
         }
         
        // Commands
        // Commands.add(new CommandExample());

        // HUD
        Hud.get().register(EntityList.INFO);
        Hud.get().register(TotemCount.INFO);
        Hud.get().register(CrystalCount.INFO);
        Hud.get().register(DubCountGUI.INFO);
        Hud.get().register(SignDisplay.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(UTILS);
        Modules.registerCategory(RENDER);
        Modules.registerCategory(MOVEMENT);
        Modules.registerCategory(HUNTING);
    }

    @Override
    public String getPackage() {
        return "dev.cozi.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("CoziSoftware", "cozisAddon");
    }
}
