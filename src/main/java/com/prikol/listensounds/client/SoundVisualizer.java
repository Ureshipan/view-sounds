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
import net.minecraft.util.Mth;

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

        if (ModConfig.DEBUG_MODE.get()) {
            System.out.println("Rendering sound at position: " + sound.position);
        }

        // Преобразуем координаты мира в координаты экрана
        int[] screenCoords = worldToScreenCoordinates(sound.position);
        if (screenCoords == null) {
            return; // Объект вне поля зрения
        }
        
        int screenX = screenCoords[0];
        int screenY = screenCoords[1];
        
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

    /**
     * Преобразует координаты мира в координаты экрана
     * @param worldPosition позиция в мире
     * @return массив [x, y] координат экрана или null если объект вне поля зрения
     */
    private static int[] worldToScreenCoordinates(Vec3 worldPosition) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return null;
    
        // Получаем позицию камеры (глаз игрока)
        Vec3 cameraPos = minecraft.player.getEyePosition(1.0f); // Исправлено для получения точной позиции глаз
        Vec3 direction = worldPosition.subtract(cameraPos);
        
        // Углы поворота камеры в радианах
        float yaw = minecraft.player.getYRot();
        float pitch = minecraft.player.getXRot();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        
        // Поворот вектора относительно камеры
        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;
        
        // 1. Компенсация рысканья (yaw)
        double cosYaw = Math.cos(-yawRad); // Исправлен знак для согласованности с системой координат
        double sinYaw = Math.sin(-yawRad);
        double rotatedX = dx * cosYaw - dz * sinYaw; // Исправлена формула поворота
        double rotatedZ = dx * sinYaw + dz * cosYaw;
        
        // 2. Компенсация тангажа (pitch)
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double cameraY = dy * cosPitch + rotatedZ * sinPitch; // Исправлена формула поворота
        double cameraZ = rotatedZ * cosPitch - dy * sinPitch;
        
        // Если объект позади камеры
        if (cameraZ <= 0.1) return null; // Добавлен небольшой порог для стабильности
        
        // Параметры экрана и проекции
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        double aspectRatio = (double) screenWidth / screenHeight;
        double fov = minecraft.options.fov().get();
        double fovRad = Math.toRadians(fov);
        
        // Корректный расчет FOV с учетом соотношения сторон
        double verticalFactor = Math.tan(fovRad / 2);
        double horizontalFactor = verticalFactor * aspectRatio; // Горизонтальный FOV
        
        // Нормализованные координаты (-1..1)
        double nx = rotatedX / (cameraZ * horizontalFactor);
        double ny = cameraY / (cameraZ * verticalFactor); // Убран лишний минус
        
        // Проверка видимости объекта
        if (Math.abs(nx) > 1 || Math.abs(ny) > 1) return null;
        
        // Преобразование в координаты экрана
        int screenX = (int) (screenWidth / 2 * (1 - nx)); // Исправлен знак
        int screenY = (int) (screenHeight / 2 * (1 - ny)); // Инвертирован знак
        
        return new int[]{
            Math.max(0, Math.min(screenX, screenWidth)),
            Math.max(0, Math.min(screenY, screenHeight))
        };
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