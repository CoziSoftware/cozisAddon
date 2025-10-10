package com.example.addon.mixin;

import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.util.SelectionManager;
import org.spongepowered.asm.mixin.injection.Inject;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 **/
@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {

    @Shadow
    private int currentRow;
    @Shadow
    public abstract void close();
    @Shadow
    @Final
    protected SignBlockEntity blockEntity;
    @Shadow
    private @Nullable SelectionManager selectionManager;
    @Shadow
    protected abstract void setCurrentRowMessage(String message);

    protected AbstractSignEditScreenMixin(Text title) { super(title); }

    // See modules handling for sign actions; this mixin no longer mutates screen internals
    @Inject(method = "init", at = @At("TAIL"))
    public void stardustMixinInit(CallbackInfo ci) {
        if (this.client == null) return;
        Modules modules = Modules.get();

        if (modules == null) return;
        // Functionality for auto-confirm and restoration is handled via events in modules.
        // We intentionally avoid accessing private fields to ensure mixin compatibility across versions.
    }
}
