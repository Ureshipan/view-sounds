package com.prikol.listensounds.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.prikol.listensounds.ViewSoundsMod;
import com.prikol.listensounds.client.SoundDetectionManager.SoundInfo;
import com.prikol.listensounds.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ViewSoundsMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class SoundVisualizer {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ViewSoundsMod.isEnabled || !ViewSoundsMod.isCrouching) {
            return;
        }

        List<SoundInfo> sounds = SoundDetectionManager.getActiveSounds();
        if (sounds.isEmpty()) {
            return;
        }

        // Отладочная информация
        if (ModConfig.DEBUG_MODE.get()) {
            System.out.println("Rendering " + sounds.size() + " sounds");
        }

        // Рендерим частицы для каждого активного звука
        for (SoundInfo sound : sounds) {
            renderSoundParticle(event.getGuiGraphics(), sound);
        }
    }

    private static void renderSoundParticle(GuiGraphics guiGraphics, SoundInfo sound) {
        long timeRemaining = 1500 - (System.currentTimeMillis() - sound.timestamp);
        float alpha = Math.max(0.0f, (float) timeRemaining / 1500.0f) / 15;
        if (alpha <= 0.0f) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        // Находим ближайшую сущность к источнику звука
        Entity nearestEntity = findNearestEntityToSound(sound);
        if (nearestEntity == null) {
            if (ModConfig.DEBUG_MODE.get()) {
                System.out.println("No entity found for sound at " + sound.position);
            }
            return;
        }

        if (ModConfig.DEBUG_MODE.get()) {
            System.out.println("Found entity: " + nearestEntity.getType() + " at " + nearestEntity.position());
        }

        // Простая проекция на основе направления взгляда
        Vec3 entityPos = nearestEntity.position();
        Vec3 playerPos = minecraft.player.position();
        Vec3 direction = entityPos.subtract(playerPos).normalize();
        
        // Получаем направление взгляда игрока
        float yaw = minecraft.player.getYRot();
        float pitch = minecraft.player.getXRot();
        
        // Конвертируем в радианы
        double yawRad = Math.toRadians(yaw);
        
        // Вычисляем относительное направление
        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;
        
        // Поворачиваем на основе угла камеры (исправляем отзеркаливание)
        double rotatedX = dx * Math.cos(-yawRad) - dz * Math.sin(-yawRad);
        double rotatedZ = dx * Math.sin(-yawRad) + dz * Math.cos(-yawRad);
        
        // Получаем FOV игрока
        double fov = minecraft.options.fov().get();
        double fovRad = Math.toRadians(fov);
        
        // Простая проекция на экран с учетом FOV
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // Вычисляем углы для проекции
        double horizontalAngle = Math.atan2(rotatedX, rotatedZ);
        
        // Вычисляем вертикальный угол относительно игрока
        double distance = Math.sqrt(rotatedX * rotatedX + rotatedZ * rotatedZ);
        double verticalAngle = - Math.atan2(dy, distance);
        
        // Учитываем pitch игрока
        double pitchRad = Math.toRadians(pitch);
        verticalAngle -= pitchRad;
        
        // Ограничиваем углы FOV
        double maxHorizontalAngle = fovRad / 2.0;
        double maxVerticalAngle = fovRad / 2.0; // Используем тот же FOV для вертикали
        
        if (Math.abs(horizontalAngle) > maxHorizontalAngle * 2 || Math.abs(verticalAngle) > maxVerticalAngle) {
            return; // Объект вне поля зрения
        }
        
        // Проецируем на экран с правильной перспективой
        // Для горизонтали используем тангенс для правильного масштабирования
        int screenX = (screenWidth / 2 - (int)((Math.tan(horizontalAngle) / Math.tan(maxHorizontalAngle) * screenWidth / 2) / 2.5));
        // Для вертикали используем линейную проекцию, но с правильным вычислением угла
        int screenY = screenHeight / 2 + (int)(verticalAngle / maxVerticalAngle * screenHeight / 2);
        
        // Ограничиваем в пределах экрана
        screenX = Math.max(10, Math.min(screenWidth - 10, screenX));
        screenY = Math.max(10, Math.min(screenHeight - 10, screenY));
        
        if (ModConfig.DEBUG_MODE.get()) {
            System.out.println("Rendering circle at screen: " + screenX + ", " + screenY);
        }
        
        // Определяем цвет на основе типа сущности
        int entityColor = getSoundSourceColor(sound.soundSource);
        
        // Проверяем, включен ли этот тип звука в настройках
        if (!ModConfig.isSoundTypeEnabled(sound.soundSource)) {
            return; // Пропускаем этот звук
        }
        
        // Проверяем расстояние до источника звука
        double distanceToSound = minecraft.player.position().distanceTo(sound.position);
        if (distanceToSound > ModConfig.MAX_SOUND_DISTANCE.get()) {
            if (ModConfig.DEBUG_MODE.get()) {
                System.out.println("Sound too far: " + distanceToSound + " blocks (max: " + ModConfig.MAX_SOUND_DISTANCE.get() + ")");
            }
            return; // Звук слишком далеко
        }
        
        // Вычисляем размер круга на основе расстояния (5-30 пикселей)
        // Чем ближе моб, тем больше круг
        double distanceRatio = distanceToSound / ModConfig.MAX_SOUND_DISTANCE.get();
        int circleSize = (int)(30 - (distanceRatio * 25)); // 30 - (0-25) = 30-5

        // Рендерим пульсирующий круг
        renderPulsingCircle(guiGraphics, screenX, screenY, circleSize, alpha, entityColor);
    }

    private static Entity findNearestEntityToSound(SoundInfo sound) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return null;
        }

        Entity nearestEntity = null;
        double nearestDistance = Double.MAX_VALUE;
        Vec3 soundPos = sound.position;

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == minecraft.player) continue;
            
            double distance = entity.distanceToSqr(soundPos.x, soundPos.y, soundPos.z);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEntity = entity;
            }
        }

        return nearestEntity;
    }

    private static void renderPulsingCircle(GuiGraphics guiGraphics, int x, int y, int size, float alpha, int entityColor) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Пульсация на основе времени
        long time = System.currentTimeMillis();
        float pulse = (float) Math.sin(time * 0.01) * 0.3f + 0.7f;
        
        // Размер с пульсацией
        int baseSize = (int)(size * pulse);
        
        // Полупрозрачность (50%)
        float totalAlpha = 0.5f * alpha;
        
        int color = (int)(255 * totalAlpha) << 24 | entityColor;

        // Рендерим только один круг
        guiGraphics.fill(
            x - baseSize, y - baseSize,
            x + baseSize, y + baseSize,
            color
        );

        poseStack.popPose();
    }

    private static int getSoundSourceColor(SoundSource soundSource) {
        // Получаем цвет из конфигурации
        return ModConfig.getSoundTypeColor(soundSource);
    }
} 