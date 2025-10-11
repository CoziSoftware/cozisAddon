package com.example.addon;

import com.example.addon.commands.CommandExample;
import com.example.addon.hud.EntityList;
import com.example.addon.modules.PortalMaker;
import com.example.addon.modules.ChatToWeb;
import com.example.addon.modules.DiscordNotifications;
import com.example.addon.modules.SignASign;
import com.example.addon.modules.StashFinderPlus;
import com.example.addon.modules.SignHistory;
import com.example.addon.modules.Pitch40Util;
import com.example.addon.modules.NewChunksPlus;
import com.example.addon.modules.PearlOwner;
import com.example.addon.modules.searcharea.SearchArea;
import com.example.addon.modules.TrailFollower;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
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
         Modules.get().add(new ChatToWeb());
         Modules.get().add(new DiscordNotifications());
         Modules.get().add(new SignASign());
         Modules.get().add(new SignHistory());
         Modules.get().add(new StashFinderPlus());
         Modules.get().add(new Pitch40Util());
         Modules.get().add(new NewChunksPlus());
         Modules.get().add(new PearlOwner());
         Modules.get().add(new SearchArea());
         
         // Only add TrailFollower if Baritone is available
         try {
             Class.forName("baritone.api.BaritoneAPI");
             Modules.get().add(new TrailFollower());
             LOG.info("TrailFollower loaded (Baritone detected)");
         } catch (ClassNotFoundException e) {
             LOG.info("TrailFollower not loaded (Baritone not found)");
         }
         
        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(EntityList.INFO);
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
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("CoziSoftware", "cozisAddon");
    }
}
