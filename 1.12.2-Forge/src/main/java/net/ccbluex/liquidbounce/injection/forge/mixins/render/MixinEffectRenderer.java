/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Queue;

@Mixin(ParticleManager.class)
@SideOnly(Side.CLIENT)
public abstract class MixinEffectRenderer {

    @Shadow
    @Final
    private final Queue<ParticleEmitter> particleEmitters = Queues.<ParticleEmitter>newArrayDeque();
    @Shadow
    @Final
    private Queue<Particle> queue;
    @Shadow
    @Final
    private ArrayDeque<Particle>[][] fxLayers;

    @Shadow
    protected abstract void updateEffectLayer(int layer);

    /**
     * @author CCBlueX (superblaubeere27)
     * @reason Terra
     */
    @Overwrite
    public void updateEffects() {
        try {
            for (int i = 0; i < 4; ++i) {
                this.updateEffectLayer(i);
            }

            if (!this.particleEmitters.isEmpty()) {
                List<ParticleEmitter> list = Lists.newArrayList();

                for (ParticleEmitter particleemitter : this.particleEmitters) {
                    particleemitter.onUpdate();

                    if (!particleemitter.isAlive()) {
                        list.add(particleemitter);
                    }
                }

                this.particleEmitters.removeAll(list);
            }

            if (!this.queue.isEmpty()) {
                for (Particle particle = this.queue.poll(); particle != null; particle = this.queue.poll()) {
                    int j = particle.getFXLayer();
                    int k = particle.shouldDisableDepth() ? 0 : 1;

                    if (this.fxLayers[j][k].size() >= 16384) {
                        this.fxLayers[j][k].removeFirst();
                    }

                    this.fxLayers[j][k].add(particle);
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }

    @Inject(method = {"addBlockDestroyEffects",}, at = @At("HEAD"), cancellable = true)
    private void removeBlockBreakingParticles(CallbackInfo ci) {
        if (Animations.noBlockDestroyParticles.get())
            ci.cancel();
    }
}