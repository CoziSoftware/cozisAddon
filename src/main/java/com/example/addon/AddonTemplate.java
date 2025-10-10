package com.example.addon;

import com.example.addon.commands.CommandExample;
import com.example.addon.hud.EntityList;
import com.example.addon.modules.AutoPortal;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("cozyClient");
    public static final HudGroup HUD_GROUP = new HudGroup("cozyClient");

    @Override
    public void onInitialize() {
        LOG.info("Initializing cozyClient");

        // Modules
        Modules.get().add(new AutoPortal());

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
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
