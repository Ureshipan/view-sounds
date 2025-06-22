package com.prikol.listensounds.client;

import com.prikol.listensounds.ViewSoundsMod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ViewSoundsMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        // Check for toggle key press
        if (KeyBindings.DISABLE_MOD.consumeClick()) {
            ViewSoundsMod.toggleMod();
            
            // Отправляем сообщение в чат о состоянии мода
            if (ViewSoundsMod.isManuallyDisabled) {
                minecraft.player.displayClientMessage(
                    Component.literal("§c[View Sounds] §fМод выключен"),
                    false
                );
            } else {
                minecraft.player.displayClientMessage(
                    Component.literal("§a[View Sounds] §fМод включен"),
                    false
                );
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        // Check if player is crouching
        boolean isCrouching = minecraft.player.isShiftKeyDown();
        ViewSoundsMod.setCrouching(isCrouching);
        
        // Обновляем таймер сидения на корточках
        ViewSoundsMod.updateCrouchTimer();

        // Активируем оверлей только когда мод включен и игрок присел
        boolean shouldShowOverlay = ViewSoundsMod.isEnabled && ViewSoundsMod.isCrouching;
        GrayscaleOverlay.setActive(shouldShowOverlay);
    }
} 