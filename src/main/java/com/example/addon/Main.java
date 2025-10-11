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
    public static final Category CATEGORY = new Category("cozisAddon");
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
        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(EntityList.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
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
