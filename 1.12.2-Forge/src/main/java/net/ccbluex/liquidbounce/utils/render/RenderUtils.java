/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.utils.render;

import net.ccbluex.liquidbounce.Terra;
import net.ccbluex.liquidbounce.api.enums.WDefaultVertexFormats;
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.player.IEntityPlayer;
import net.ccbluex.liquidbounce.api.minecraft.client.render.ITessellator;
import net.ccbluex.liquidbounce.api.minecraft.client.render.IWorldRenderer;
import net.ccbluex.liquidbounce.api.minecraft.renderer.entity.IRenderManager;
import net.ccbluex.liquidbounce.api.minecraft.util.*;
import net.ccbluex.liquidbounce.injection.backend.Backend;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.*;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.utils.particles.Particle;
import net.ccbluex.liquidbounce.utils.particles.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static java.lang.Math.*;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static org.lwjgl.opengl.GL11.*;

public final class RenderUtils extends MinecraftInstance {
    private static final ShaderUtil roundedGradientShader = new ShaderUtil("roundedRectGradient");
    public static ShaderUtil roundedShader = new ShaderUtil("roundedRect");
    private static final Map<String, Map<Integer, Boolean>> glCapMap = new HashMap<>();
    private static final int[] DISPLAY_LISTS_2D = new int[4];
    public static int deltaTime;
    private static final Frustum frustrum = new Frustum();

    static {
        for (int i = 0; i < DISPLAY_LISTS_2D.length; i++) {
            DISPLAY_LISTS_2D[i] = glGenLists(1);
        }

        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE);

        quickDrawRect(-7F, 2F, -4F, 3F);
        quickDrawRect(4F, 2F, 7F, 3F);
        quickDrawRect(-7F, 0.5F, -6F, 3F);
        quickDrawRect(6F, 0.5F, 7F, 3F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE);

        quickDrawRect(-7F, 3F, -4F, 3.3F);
        quickDrawRect(4F, 3F, 7F, 3.3F);
        quickDrawRect(-7.3F, 0.5F, -7F, 3.3F);
        quickDrawRect(7F, 0.5F, 7.3F, 3.3F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE);

        quickDrawRect(4F, -20F, 7F, -19F);
        quickDrawRect(-7F, -20F, -4F, -19F);
        quickDrawRect(6F, -20F, 7F, -17.5F);
        quickDrawRect(-7F, -20F, -6F, -17.5F);

        glEndList();

        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE);

        quickDrawRect(7F, -20F, 7.3F, -17.5F);
        quickDrawRect(-7.3F, -20F, -7F, -17.5F);
        quickDrawRect(4F, -20.3F, 7.3F, -20F);
        quickDrawRect(-7.3F, -20.3F, -4F, -20F);

        glEndList();
    }

    public static void drawOutlinedString(String str, int x, int y, int color,int color2) {
        mc.getFontRendererObj().drawString(str, (int) ((int)x - 1.0F), y, color2);
        mc.getFontRendererObj().drawString(str, (int) (x + 1.0F), y,color2);
        mc.getFontRendererObj().drawString(str, x, (int) (y + 1.0F), color2);
        mc.getFontRendererObj().drawString(str, x, (int) (y -1.0F),color2);
        mc.getFontRendererObj().drawString(str, x, y, color);
    }

    //RandomSplash
    public static int loadGlTexture(BufferedImage bufferedImage) {
        int textureId = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(),
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, ImageUtils.readImageToBuffer(bufferedImage));

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return textureId;
    }

    //Draw Effects Rect
    public static void drawRectPotion(final float x, final float y, final float x2, final float y2, final int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glPushMatrix();
        glColor(color);

        glBegin(GL_QUADS);
        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);
        glEnd();

        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawShadowWithCustomAlpha(float x, float y, float width, float height, float alpha) {
        drawTexturedRectWithCustomAlpha(x - 9, y - 9, 9, 9, "paneltopleft", alpha);
        drawTexturedRectWithCustomAlpha(x - 9, y + height, 9, 9, "panelbottomleft", alpha);
        drawTexturedRectWithCustomAlpha(x + width, y + height, 9, 9, "panelbottomright", alpha);
        drawTexturedRectWithCustomAlpha(x + width, y - 9, 9, 9, "paneltopright", alpha);
        drawTexturedRectWithCustomAlpha(x - 9, y, 9, height, "panelleft", alpha);
        drawTexturedRectWithCustomAlpha(x + width, y, 9, height, "panelright", alpha);
        drawTexturedRectWithCustomAlpha(x, y - 9, width, 9, "paneltop", alpha);
        drawTexturedRectWithCustomAlpha(x, y + height, width, 9, "panelbottom", alpha);
    }

    public static void drawTexturedRectWithCustomAlpha(float x, float y, float width, float height, String image, float alpha) {
        glPushMatrix();
        final boolean enableBlend = glIsEnabled(GL_BLEND);
        final boolean disableAlpha = !glIsEnabled(GL_ALPHA_TEST);
        if (!enableBlend) glEnable(GL_BLEND);
        if (!disableAlpha) glDisable(GL_ALPHA_TEST);
        GlStateManager.color(1F, 1F, 1F, alpha);
        mc.getTextureManager().bindTexture2(new ResourceLocation("terra/shadow/" + image + ".png"));
        drawModalRectWithCustomSizedTexture2(x, y, 0, 0, width, height, width, height);
        if (!enableBlend) glDisable(GL_BLEND);
        if (!disableAlpha) glEnable(GL_ALPHA_TEST);
        GlStateManager.resetColor();
        glPopMatrix();
    }

    public static void drawModalRectWithCustomSizedTexture2(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        ITessellator tessellator = classProvider.getTessellatorInstance();
        IWorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, classProvider.getVertexFormatEnum(WDefaultVertexFormats.POSITION_TEX));
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + (float) height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + (float) width) * f, (v + (float) height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + (float) width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static double getAnimationState2(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }
    /////////////////////////Draw Potion Effects Rect

    public static void drawRoundedCornerRect(float x, float y, float x1, float y1, float radius, int color) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        final boolean hasCull = glIsEnabled(GL_CULL_FACE);
        glDisable(GL_CULL_FACE);

        glColor(color);
        drawRoundedCornerRect(x, y, x1, y1, radius);

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        setGlState(GL_CULL_FACE, hasCull);
    }

    public static void drawRoundedCornerRect(float x, float y, float x1, float y1, float radius) {
        glBegin(GL_POLYGON);

        float xRadius = (float) Math.min((x1 - x) * 0.5, radius);
        float yRadius = (float) Math.min((y1 - y) * 0.5, radius);
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270, 4);
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180, 4);
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90, 4);
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360, 4);

        glEnd();
    }

    private static void quickPolygonCircle(float x, float y, float xRadius, float yRadius, int start, int end, int split) {
        for (int i = end; i >= start; i -= split) {
            glVertex2d(x + Math.sin(i * Math.PI / 180.0D) * xRadius, y + Math.cos(i * Math.PI / 180.0D) * yRadius);
        }
    }
    //////////////////////RandomSplash

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (col1 >> 24 & 0xFF) / 255.0F;
        float f1 = (col1 >> 16 & 0xFF) / 255.0F;
        float f2 = (col1 >> 8 & 0xFF) / 255.0F;
        float f3 = (col1 & 0xFF) / 255.0F;

        float f4 = (col2 >> 24 & 0xFF) / 255.0F;
        float f5 = (col2 >> 16 & 0xFF) / 255.0F;
        float f6 = (col2 >> 8 & 0xFF) / 255.0F;
        float f7 = (col2 & 0xFF) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);

        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);

        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    //LLL Sca Draw
    public static void drawRoundedRect(float x, float y, float x1, float y1, int borderC, int insideC) {
        enableGL2D();
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        drawVLine(x *= 2.0f, (y *= 2.0f) + 1.0f, (y1 *= 2.0f) - 2.0f, borderC);
        drawVLine((x1 *= 2.0f) - 1.0f, y + 1.0f, y1 - 2.0f, borderC);
        drawHLine(x + 2.0f, x1 - 3.0f, y, borderC);
        drawHLine(x + 2.0f, x1 - 3.0f, y1 - 1.0f, borderC);
        drawHLine(x + 1.0f, x + 1.0f, y + 1.0f, borderC);
        drawHLine(x1 - 2.0f, x1 - 2.0f, y + 1.0f, borderC);
        drawHLine(x1 - 2.0f, x1 - 2.0f, y1 - 2.0f, borderC);
        drawHLine(x + 1.0f, x + 1.0f, y1 - 2.0f, borderC);
        drawRect(x + 1.0f, y + 1.0f, x1 - 1.0f, y1 - 1.0f, insideC);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        disableGL2D();
        Gui.drawRect(0, 0, 0, 0, 0);
    }
    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }
    public static void drawVLine(float x, float y, float x1, int y1) {
        if (x1 < y) {
            float var5 = y;
            y = x1;
            x1 = var5;
        }
        drawRect(x, y + 1.0f, x + 1.0f, x1, y1);
    }
    public static void drawHLine(float par1, float par2, float par3, int par4) {
        if (par2 < par1) {
            float var5 = par1;
            par1 = par2;
            par2 = var5;
        }
        drawRect(par1, par3, par2 + 1.0f, par3 + 1.0f, par4);
    }
    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }
    //////////////////////////////////////////////////////////////////////////

    public static void drawBlockBox(final WBlockPos blockPos, final Color color, final boolean outline) {
        final IRenderManager renderManager = mc.getRenderManager();
        final ITimer timer = mc.getTimer();

        final double x = blockPos.getX() - renderManager.getRenderPosX();
        final double y = blockPos.getY() - renderManager.getRenderPosY();
        final double z = blockPos.getZ() - renderManager.getRenderPosZ();

        IAxisAlignedBB axisAlignedBB = classProvider.createAxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final IBlock block = BlockUtils.getBlock(blockPos);

        if (block != null) {
            final IEntityPlayer player = mc.getThePlayer();

            final double posX = player.getLastTickPosX() + (player.getPosX() - player.getLastTickPosX()) * (double) timer.getRenderPartialTicks();
            final double posY = player.getLastTickPosY() + (player.getPosY() - player.getLastTickPosY()) * (double) timer.getRenderPartialTicks();
            final double posZ = player.getLastTickPosZ() + (player.getPosZ() - player.getLastTickPosZ()) * (double) timer.getRenderPartialTicks();

            if (Backend.MINECRAFT_VERSION_MINOR < 12) {
                block.setBlockBoundsBasedOnState(mc.getTheWorld(), blockPos);
            }

            axisAlignedBB = block.getSelectedBoundingBox(mc.getTheWorld(), mc.getTheWorld().getBlockState(blockPos), blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color);

            drawSelectionBoundingBox(axisAlignedBB);
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    //KeyStrokes
    public static void drawLimitedCircle(final float lx, final float ly, final float x2, final float y2, final int xx, final int yy, final float radius, final Color color) {
        int sections = 50;
        double dAngle = 2 * Math.PI / sections;
        float x, y;

        glPushAttrib(GL_ENABLE_BIT);

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_TRIANGLE_FAN);

        glColor(color);
        for (int i = 0; i < sections; i++) {
            x = (float) (radius * Math.sin((i * dAngle)));
            y = (float) (radius * Math.cos((i * dAngle)));
            glVertex2f(Math.min(x2, Math.max(xx + x, lx)), Math.min(y2, Math.max(yy + y, ly)));
        }

        GlStateManager.color(0, 0, 0);

        glEnd();

        glPopAttrib();
    }

    public static void drawSelectionBoundingBox(IAxisAlignedBB boundingBox) {
        ITessellator tessellator = classProvider.getTessellatorInstance();
        IWorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL_LINE_STRIP, classProvider.getVertexFormatEnum(WDefaultVertexFormats.POSITION));

        // Lower Rectangle
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ()).endVertex();
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxZ()).endVertex();
        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxZ()).endVertex();
        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMinZ()).endVertex();
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ()).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMinZ()).endVertex();
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMaxZ()).endVertex();
        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()).endVertex();
        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMinZ()).endVertex();
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMinZ()).endVertex();

        // Upper Rectangle
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMaxZ()).endVertex();
        worldrenderer.pos(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxZ()).endVertex();

        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxZ()).endVertex();
        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ()).endVertex();

        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMinZ()).endVertex();
        worldrenderer.pos(boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMinZ()).endVertex();

        tessellator.draw();
    }

    public static void drawEntityBox(final IEntity entity, final Color color, final boolean outline) {
        final IRenderManager renderManager = mc.getRenderManager();
        final ITimer timer = mc.getTimer();

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.getLastTickPosX() + (entity.getPosX() - entity.getLastTickPosX()) * timer.getRenderPartialTicks()
                - renderManager.getRenderPosX();
        final double y = entity.getLastTickPosY() + (entity.getPosY() - entity.getLastTickPosY()) * timer.getRenderPartialTicks()
                - renderManager.getRenderPosY();
        final double z = entity.getLastTickPosZ() + (entity.getPosZ() - entity.getLastTickPosZ()) * timer.getRenderPartialTicks()
                - renderManager.getRenderPosZ();

        final IAxisAlignedBB entityBox = entity.getEntityBoundingBox();
        final IAxisAlignedBB axisAlignedBB = classProvider.createAxisAlignedBB(
                entityBox.getMinX() - entity.getPosX() + x - 0.05D,
                entityBox.getMinY() - entity.getPosY() + y,
                entityBox.getMinZ() - entity.getPosZ() + z - 0.05D,
                entityBox.getMaxX() - entity.getPosX() + x + 0.05D,
                entityBox.getMaxY() - entity.getPosY() + y + 0.15D,
                entityBox.getMaxZ() - entity.getPosZ() + z + 0.05D
        );

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        glColor(color.getRed(), color.getGreen(), color.getBlue(), outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDepthMask(true);
        resetCaps();
    }

    public static void drawAxisAlignedBB(final IAxisAlignedBB axisAlignedBB, final Color color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);
        glLineWidth(2F);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColor(color);
        drawFilledBox(axisAlignedBB);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public static void drawPlatform(final double y, final Color color, final double size) {
        final IRenderManager renderManager = mc.getRenderManager();
        final double renderY = y - renderManager.getRenderPosY();

        drawAxisAlignedBB(classProvider.createAxisAlignedBB(size, renderY + 0.02D, size, -size, renderY, -size), color);
    }

    public static void drawPlatform(final IEntity entity, final Color color) {
        final IRenderManager renderManager = mc.getRenderManager();
        final ITimer timer = mc.getTimer();

        final double x = entity.getLastTickPosX() + (entity.getPosX() - entity.getLastTickPosX()) * timer.getRenderPartialTicks()
                - renderManager.getRenderPosX();
        final double y = entity.getLastTickPosY() + (entity.getPosY() - entity.getLastTickPosY()) * timer.getRenderPartialTicks()
                - renderManager.getRenderPosY();
        final double z = entity.getLastTickPosZ() + (entity.getPosZ() - entity.getLastTickPosZ()) * timer.getRenderPartialTicks()
                - renderManager.getRenderPosZ();

        final IAxisAlignedBB axisAlignedBB = entity.getEntityBoundingBox()
                .offset(-entity.getPosX(), -entity.getPosY(), -entity.getPosZ())
                .offset(x, y, z);

        drawAxisAlignedBB(
                classProvider.createAxisAlignedBB(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY() + 0.2, axisAlignedBB.getMinZ(), axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY() + 0.26, axisAlignedBB.getMaxZ()),
                color
        );
    }

    public static void drawFilledBox(final IAxisAlignedBB axisAlignedBB) {
        final ITessellator tessellator = classProvider.getTessellatorInstance();
        final IWorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, classProvider.getVertexFormatEnum(WDefaultVertexFormats.POSITION));

        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();

        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();

        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();

        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();

        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();

        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMinX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMinZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMaxY(), axisAlignedBB.getMaxZ()).endVertex();
        worldRenderer.pos(axisAlignedBB.getMaxX(), axisAlignedBB.getMinY(), axisAlignedBB.getMaxZ()).endVertex();
        tessellator.draw();
    }

    public static void quickDrawRect(final float x, final float y, final float x2, final float y2) {
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void drawRect(final float x, final float y, final float x2, final float y2, final int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        glBegin(GL_QUADS);

        glVertex2f(x2, y);
        glVertex2f(x, y);
        glVertex2f(x, y2);
        glVertex2f(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawRect(final int x, final int y, final int x2, final int y2, final int color) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color);
        glBegin(GL_QUADS);

        glVertex2i(x2, y);
        glVertex2i(x, y);
        glVertex2i(x, y2);
        glVertex2i(x2, y2);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    /**
     * Like {@link #drawRect(float, float, float, float, int)}, but without setup
     */
    public static void quickDrawRect(final float x, final float y, final float x2, final float y2, final int color) {
        glColor(color);
        glBegin(GL_QUADS);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void drawRect(final float x, final float y, final float x2, final float y2, final Color color) {
        drawRect(x, y, x2, y2, color.getRGB());
    }

    public static void drawBorderedRect(final float x, final float y, final float x2, final float y2, final float width,
                                        final int color1, final int color2) {
        drawRect(x, y, x2, y2, color2);
        drawBorder(x, y, x2, y2, width, color1);
    }

    public static void drawBorder(float x, float y, float x2, float y2, float width, int color1) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);

        glColor(color1);
        glLineWidth(width);

        glBegin(GL_LINE_LOOP);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static void quickDrawBorderedRect(final float x, final float y, final float x2, final float y2, final float width, final int color1, final int color2) {
        quickDrawRect(x, y, x2, y2, color2);

        glColor(color1);
        glLineWidth(width);

        glBegin(GL_LINE_LOOP);

        glVertex2d(x2, y);
        glVertex2d(x, y);
        glVertex2d(x, y2);
        glVertex2d(x2, y2);

        glEnd();
    }

    public static void drawLoadingCircle(float x, float y) {
        for (int i = 0; i < 4; i++) {
            int rot = (int) ((System.nanoTime() / 5000000 * i) % 360);
            drawCircle(x, y, i * 10, rot - 180, rot);
        }
    }

    public static void drawCircle(float x, float y, float radius, int start, int end) {
        classProvider.getGlStateManager().enableBlend();
        classProvider.getGlStateManager().disableTexture2D();
        classProvider.getGlStateManager().tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor(Color.WHITE);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2F);
        glBegin(GL_LINE_STRIP);
        for (float i = end; i >= start; i -= (360 / 90.0f)) {
            glVertex2f((float) (x + (cos(i * PI / 180) * (radius * 1.001F))), (float) (y + (sin(i * PI / 180) * (radius * 1.001F))));
        }
        glEnd();
        glDisable(GL_LINE_SMOOTH);

        classProvider.getGlStateManager().enableTexture2D();
        classProvider.getGlStateManager().disableBlend();
    }

    public static void drawFilledCircle(final int xx, final int yy, final float radius, final Color color) {
        int sections = 50;
        double dAngle = 2 * Math.PI / sections;
        float x, y;

        glPushAttrib(GL_ENABLE_BIT);

        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glBegin(GL_TRIANGLE_FAN);

        for (int i = 0; i < sections; i++) {
            x = (float) (radius * Math.sin((i * dAngle)));
            y = (float) (radius * Math.cos((i * dAngle)));

            glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
            glVertex2f(xx + x, yy + y);
        }

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glEnd();

        glPopAttrib();
    }

    public static void drawImage(IResourceLocation image, int x, int y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    //RandomSplash
    public static void drawImage4(ResourceLocation image, int x, int y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture2(image);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        ITessellator tessellator = classProvider.getTessellatorInstance();
        IWorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, classProvider.getVertexFormatEnum(WDefaultVertexFormats.POSITION_TEX));
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void glColor(final Color color) {
        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void glColor(final int hex) {
        glColor(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF, hex >> 24 & 0xFF);
    }

    public static void draw2D(final IEntityLivingBase entity, final double posX, final double posY, final double posZ, final int color, final int backgroundColor) {
        GL11.glPushMatrix();
        GL11.glTranslated(posX, posY, posZ);
        GL11.glRotated(-mc.getRenderManager().getPlayerViewY(), 0F, 1F, 0F);
        GL11.glScaled(-0.1D, -0.1D, 0.1D);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthMask(true);

        glColor(color);

        glCallList(DISPLAY_LISTS_2D[0]);

        glColor(backgroundColor);

        glCallList(DISPLAY_LISTS_2D[1]);

        GL11.glTranslated(0, 21 + -(entity.getEntityBoundingBox().getMaxY() - entity.getEntityBoundingBox().getMinY()) * 12, 0);

        glColor(color);
        glCallList(DISPLAY_LISTS_2D[2]);

        glColor(backgroundColor);
        glCallList(DISPLAY_LISTS_2D[3]);

        // Stop render
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        GL11.glPopMatrix();
    }

    public static void draw2D(final WBlockPos blockPos, final int color, final int backgroundColor) {
        final IRenderManager renderManager = mc.getRenderManager();

        final double posX = (blockPos.getX() + 0.5) - renderManager.getRenderPosX();
        final double posY = blockPos.getY() - renderManager.getRenderPosY();
        final double posZ = (blockPos.getZ() + 0.5) - renderManager.getRenderPosZ();

        GL11.glPushMatrix();
        GL11.glTranslated(posX, posY, posZ);
        GL11.glRotated(-mc.getRenderManager().getPlayerViewY(), 0F, 1F, 0F);
        GL11.glScaled(-0.1D, -0.1D, 0.1D);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDepthMask(true);

        glColor(color);

        glCallList(DISPLAY_LISTS_2D[0]);

        glColor(backgroundColor);

        glCallList(DISPLAY_LISTS_2D[1]);

        GL11.glTranslated(0, 9, 0);

        glColor(color);

        glCallList(DISPLAY_LISTS_2D[2]);

        glColor(backgroundColor);

        glCallList(DISPLAY_LISTS_2D[3]);

        // Stop render
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        GL11.glPopMatrix();
    }

    public static void renderNameTag(final String string, final double x, final double y, final double z) {
        final IRenderManager renderManager = mc.getRenderManager();

        glPushMatrix();
        glTranslated(x - renderManager.getRenderPosX(), y - renderManager.getRenderPosY(), z - renderManager.getRenderPosZ());
        glNormal3f(0F, 1F, 0F);
        glRotatef(-mc.getRenderManager().getPlayerViewY(), 0F, 1F, 0F);
        glRotatef(mc.getRenderManager().getPlayerViewX(), 1F, 0F, 0F);
        glScalef(-0.05F, -0.05F, 0.05F);
        setGlCap(GL_LIGHTING, false);
        setGlCap(GL_DEPTH_TEST, false);
        setGlCap(GL_BLEND, true);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        final int width = Fonts.font35.getStringWidth(string) / 2;

        drawRect(-width - 1, -1, width + 1, Fonts.font35.getFontHeight(), Integer.MIN_VALUE);
        Fonts.font35.drawString(string, -width, 1.5F, Color.WHITE.getRGB(), true);

        resetCaps();
        glColor4f(1F, 1F, 1F, 1F);
        glPopMatrix();
    }

    public static void drawLine(final double x, final double y, final double x1, final double y1, final float width) {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(width);
        glBegin(GL_LINES);
        glVertex2d(x, y);
        glVertex2d(x1, y1);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void makeScissorBox(final float x, final float y, final float x2, final float y2) {
        final IScaledResolution scaledResolution = classProvider.createScaledResolution(mc);
        final int factor = scaledResolution.getScaleFactor();
        glScissor((int) (x * factor), (int) ((scaledResolution.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    /**
     * GL CAP MANAGER
     * <p>
     * TODO: Remove gl cap manager and replace by something better
     */

    public static void resetCaps(final String scale) {
        if(!glCapMap.containsKey(scale)) {
            return;
        }
        Map<Integer, Boolean> map = glCapMap.get(scale);
        map.forEach(RenderUtils::setGlState);
        map.clear();
    }
    public static void resetCaps() {
        resetCaps("COMMON");
    }


    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void enableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, true);
    }

    public static void disableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void setGlCap(final int cap, final boolean state) {
        setGlCap(cap, state, "COMMON");
    }
    public static void disableGlCap(final int cap, final String scale) {
        setGlCap(cap, false, scale);
    }
    public static void enableGlCap(final int cap, final String scale) {
        setGlCap(cap, true, scale);
    }
    //Text
    /**
     * 在LWJGL中渲染AWT图形
     * @param shape 准备渲染的图形
     * @param epsilon 图形精细度，传0不做处理
     */
    public static void drawAWTShape(Shape shape, double epsilon, GLUtessellatorCallbackAdapter gluTessCallback) {
        PathIterator path=shape.getPathIterator(new AffineTransform());
        Double[] cp=new Double[2]; // 记录上次操作的点用于计算曲线

        GLUtessellator tess = GLU.gluNewTess(); // 创建GLUtessellator用于渲染凹多边形（GL_POLYGON只能渲染凸多边形）

        tess.gluTessCallback(GLU.GLU_TESS_BEGIN, gluTessCallback);
        tess.gluTessCallback(GLU.GLU_TESS_END, gluTessCallback);
        tess.gluTessCallback(GLU.GLU_TESS_VERTEX, gluTessCallback);
        tess.gluTessCallback(GLU.GLU_TESS_COMBINE, gluTessCallback);

        switch (path.getWindingRule()){
            case PathIterator.WIND_EVEN_ODD:{
                tess.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
                break;
            }
            case PathIterator.WIND_NON_ZERO:{
                tess.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
                break;
            }
        }

        // 缓存单个图形路径上的点用于精简以提升性能
        ArrayList<Double[]> pointsCache = new ArrayList<>();

        tess.gluTessBeginPolygon(null);

        while (!path.isDone()){
            double[] segment=new double[6];
            int type=path.currentSegment(segment);
            switch (type){
                case PathIterator.SEG_MOVETO:{
                    tess.gluTessBeginContour();
                    pointsCache.add(new Double[] {segment[0], segment[1]});
                    cp[0] = segment[0];
                    cp[1] = segment[1];
                    break;
                }
                case PathIterator.SEG_LINETO:{
                    pointsCache.add(new Double[] {segment[0], segment[1]});
                    cp[0] = segment[0];
                    cp[1] = segment[1];
                    break;
                }
                case PathIterator.SEG_QUADTO:{
                    Double[][] points= MathUtils.getPointsOnCurve(new Double[][]{new Double[]{cp[0], cp[1]}, new Double[]{segment[0], segment[1]}, new Double[]{segment[2], segment[3]}}, 10);
                    pointsCache.addAll(Arrays.asList(points));
                    cp[0] = segment[2];
                    cp[1] = segment[3];
                    break;
                }
                case PathIterator.SEG_CUBICTO:{
                    Double[][] points=MathUtils.getPointsOnCurve(new Double[][]{new Double[]{cp[0], cp[1]}, new Double[]{segment[0], segment[1]}, new Double[]{segment[2], segment[3]}, new Double[]{segment[4], segment[5]}}, 10);
                    pointsCache.addAll(Arrays.asList(points));
                    cp[0] = segment[4];
                    cp[1] = segment[5];
                    break;
                }
                case PathIterator.SEG_CLOSE:{
                    // 精简路径上的点
                    for(Double[] point : MathUtils.simplifyPoints(pointsCache.toArray(new Double[0][0]), epsilon)){
                        tessVertex(tess, new double[] {point[0], point[1], 0.0, 0.0, 0.0, 0.0});
                    }
                    // 清除缓存以便画下一个图形
                    pointsCache.clear();
                    tess.gluTessEndContour();
                    break;
                }
            }
            path.next();
        }

        tess.gluEndPolygon();
        tess.gluDeleteTess();
    }
    public static void tessVertex(GLUtessellator tessellator, double[] coords) {
        tessellator.gluTessVertex(coords, 0, new VertexData(coords));
    }
    public static void directDrawAWTShape(Shape shape, double epsilon) {
        drawAWTShape(shape, epsilon, DirectTessCallback.INSTANCE);
    }
    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void drawScaledCustomSizeModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        ITessellator tessellator = classProvider.getTessellatorInstance();
        IWorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, classProvider.getVertexFormatEnum(WDefaultVertexFormats.POSITION_TEX));
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + (float) vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + (float) uWidth) * f, (v + (float) vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + (float) uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    //Forever Mark
    public static void drawCircleESP(final IEntity entity, final double rad, final int color, final boolean shade) {
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glHint(3153, 4354);
        GL11.glDepthMask(false);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        if (shade) GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        final double x = entity.getLastTickPosX() + (entity.getPosX() - entity.getLastTickPosX()) * mc.getTimer().getRenderPartialTicks() - (mc.getRenderManager()).getRenderPosX();
        final double y = (entity.getLastTickPosY() + (entity.getPosY() - entity.getLastTickPosY()) * mc.getTimer().getRenderPartialTicks() - (mc.getRenderManager()).getRenderPosY()) + Math.sin(System.currentTimeMillis() / 2E+2) + 1;
        final double z = entity.getLastTickPosZ() + (entity.getPosZ() - entity.getLastTickPosZ()) * mc.getTimer().getRenderPartialTicks() - (mc.getRenderManager()).getRenderPosZ();

        java.awt.Color c;

        for (float i = 0; i < Math.PI * 2; i += Math.PI * 2 / 64.F) {
            final double vecX = x + rad * Math.cos(i);
            final double vecZ = z + rad * Math.sin(i);

            c = ColorUtils.INSTANCE.rainbow();
            //c = new Color(255, 75, 75);
            if (shade) {
                GL11.glColor4f(c.getRed() / 255.F,
                        c.getGreen() / 255.F,
                        c.getBlue() / 255.F,
                        0
                );
                GL11.glVertex3d(vecX, y - Math.cos(System.currentTimeMillis() / 2E+2) / 2.0F, vecZ);
                GL11.glColor4f(c.getRed() / 255.F,
                        c.getGreen() / 255.F,
                        c.getBlue() / 255.F,
                        0.85F
                );
            }
            GL11.glVertex3d(vecX, y, vecZ);
        }

        GL11.glEnd();
        if (shade) GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glDisable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        GL11.glColor3f(255, 255, 255);
    }
    ///////////////////////Forever Mark

    //RiseButton
    public static void quickDrawGradientSidewaysH(double left, double top, double right, double bottom, int col1, int col2) {
        glBegin(GL_QUADS);

        glColor(col1);
        glVertex2d(left, top);
        glVertex2d(left, bottom);
        glColor(col2);
        glVertex2d(right, bottom);
        glVertex2d(right, top);

        glEnd();
    }

    public static void originalRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

        double x1 = (double) (paramXStart + radius);
        double y1 = (double) (paramYStart + radius);
        double x2 = (double) (paramXEnd - radius);
        double y2 = (double) (paramYEnd - radius);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        worldrenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION);

        double degree = Math.PI / 180;
        for (double i = 0; i <= 90; i += 1)
            worldrenderer.pos(x2 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius, 0.0D).endVertex();
        for (double i = 90; i <= 180; i += 1)
            worldrenderer.pos(x2 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius, 0.0D).endVertex();
        for (double i = 180; i <= 270; i += 1)
            worldrenderer.pos(x1 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius, 0.0D).endVertex();
        for (double i = 270; i <= 360; i += 1)
            worldrenderer.pos(x1 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius, 0.0D).endVertex();

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius, int color) {
        drawRoundedRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true);
    }

    public static void drawRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius, int color, boolean popPush) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

        double x1 = paramXStart + radius;
        double y1 = paramYStart + radius;
        double x2 = paramXEnd - radius;
        double y2 = paramYEnd - radius;

        if (popPush) glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);

        glColor4f(red, green, blue, alpha);
        glBegin(GL_POLYGON);

        double degree = Math.PI / 180;
        for (double i = 0; i <= 90; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        for (double i = 90; i <= 180; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 180; i <= 270; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 270; i <= 360; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        if (popPush) glPopMatrix();
    }

    public static void customRounded(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float rTL, float rTR, float rBR, float rBL, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        float z = 0;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

        double xTL = paramXStart + rTL;
        double yTL = paramYStart + rTL;

        double xTR = paramXEnd - rTR;
        double yTR = paramYStart + rTR;

        double xBR = paramXEnd - rBR;
        double yBR = paramYEnd - rBR;

        double xBL = paramXStart + rBL;
        double yBL = paramYEnd - rBL;

        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);

        glColor4f(red, green, blue, alpha);
        glBegin(GL_POLYGON);

        double degree = Math.PI / 180;
        if (rBR <= 0)
            glVertex2d(xBR, yBR);
        else for (double i = 0; i <= 90; i += 1)
            glVertex2d(xBR + Math.sin(i * degree) * rBR, yBR + Math.cos(i * degree) * rBR);

        if (rTR <= 0)
            glVertex2d(xTR, yTR);
        else for (double i = 90; i <= 180; i += 1)
            glVertex2d(xTR + Math.sin(i * degree) * rTR, yTR + Math.cos(i * degree) * rTR);

        if (rTL <= 0)
            glVertex2d(xTL, yTL);
        else for (double i = 180; i <= 270; i += 1)
            glVertex2d(xTL + Math.sin(i * degree) * rTL, yTL + Math.cos(i * degree) * rTL);

        if (rBL <= 0)
            glVertex2d(xBL, yBL);
        else for (double i = 270; i <= 360; i += 1)
            glVertex2d(xBL + Math.sin(i * degree) * rBL, yBL + Math.cos(i * degree) * rBL);
        glEnd();

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }
    //RiseButton

    //Wings
    public static void glColor(final Color color, final float alpha) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }
    //Wings

    //Noti
    public static void drawGradientSidewaysH(double left, double top, double right, double bottom, int col1, int col2) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);

        quickDrawGradientSidewaysH(left, top, right, bottom, col1, col2);

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
    }
    //Noti

    //nametags potions
    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, float zLevel) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos((double) (x + 0), (double) (y + height), (double) zLevel).tex((double) ((float) (textureX + 0) * f), (double) ((float) (textureY + height) * f1)).endVertex();
        bufferBuilder.pos((double) (x + width), (double) (y + height), (double) zLevel).tex((double) ((float) (textureX + width) * f), (double) ((float) (textureY + height) * f1)).endVertex();
        bufferBuilder.pos((double) (x + width), (double) (y + 0), (double) zLevel).tex((double) ((float) (textureX + width) * f), (double) ((float) (textureY + 0) * f1)).endVertex();
        bufferBuilder.pos((double) (x + 0), (double) (y + 0), (double) zLevel).tex((double) ((float) (textureX + 0) * f), (double) ((float) (textureY + 0) * f1)).endVertex();
        tessellator.draw();
    }
    //nametags potions

    //Particles
    public static void renderParticles(final List<Particle> particles) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int i = 0;
        try {
            for (final Particle particle : particles) {
                i++;
                final Vec3 v = particle.position;
                boolean draw = true;

                final double x = v.xCoord - (mc2.getRenderManager()).renderPosX;
                final double y = v.yCoord - (mc2.getRenderManager()).renderPosY;
                final double z = v.zCoord - (mc2.getRenderManager()).renderPosZ;

                final double distanceFromPlayer = mc2.player.getDistance(v.xCoord, v.yCoord - 1, v.zCoord);
                int quality = (int) (distanceFromPlayer * 4 + 10);

                if (quality > 350)
                    quality = 350;

                if (!isInViewFrustrum(new EntityEgg(mc2.world, v.xCoord, v.yCoord, v.zCoord)))
                    draw = false;

                if (i % 10 != 0 && distanceFromPlayer > 25)
                    draw = false;

                if (i % 3 == 0 && distanceFromPlayer > 15)
                    draw = false;

                if (draw) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y, z);

                    final float scale = 0.04F;
                    GL11.glScalef(-scale, -scale, -scale);

                    GL11.glRotated(-mc2.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
                    GL11.glRotated(mc2.getRenderManager().playerViewX, mc2.gameSettings.thirdPersonView == 2 ? -1.0D : 1.0D, 0.0D, 0.0D);
                    final Color c = new Color(ColorUtils.INSTANCE.getColor(-(1 + 5 * 1.7f), 0.7f, 1));
                    drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality);

                    if (distanceFromPlayer < 4)
                        drawFilledCircleNoGL(0, 0, 1.4, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).hashCode(), quality);

                    if (distanceFromPlayer < 20)
                        drawFilledCircleNoGL(0, 0, 2.3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30).hashCode(), quality);

                    GL11.glScalef(0.8F, 0.8F, 0.8F);
                    GL11.glPopMatrix();
                }
            }
        } catch (final ConcurrentModificationException ignored) {
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glColor3d(255, 255, 255);
    }
    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = mc2.getRenderViewEntity();
        if (current != null) {
            frustrum.setPosition(current.posX, current.posY, current.posZ);
        }
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void drawFilledCircleNoGL(final int x, final int y, final double r, final int c, final int quality) {
        final float f = ((c >> 24) & 0xff) / 255F;
        final float f1 = ((c >> 16) & 0xff) / 255F;
        final float f2 = ((c >> 8) & 0xff) / 255F;
        final float f3 = (c & 0xff) / 255F;

        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        for (int i = 0; i <= 360 / quality; i++) {
            final double x2 = Math.sin(((i * quality * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * quality * Math.PI) / 180)) * r;
            GL11.glVertex2d(x + x2, y + y2);
        }

        GL11.glEnd();
    }

    //Inventory
    public static void drawRoundedRect2(float left, float top, float right, float bottom, float radius, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        if (left < right) left = left + right - (right = left);
        if (top < bottom) top = top + bottom - (bottom = top);

        float[][] corners = {
                {right + radius, top - radius, 270},
                {left - radius, top - radius, 360},
                {left - radius, bottom + radius, 90},
                {right + radius, bottom + radius, 180}};

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.alphaFunc(516, 0.003921569F);
        GlStateManager.color(f, f1, f2, f3);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder renderer = tessellator.getBuffer();
        renderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION);
        for (float[] c : corners) {
            for (int i = 0; i <= color; i++) {
                double anglerad = (Math.PI * (c[2] + i * 90.0F / color) / 180.0f);
                renderer.pos(c[0] + (Math.sin(anglerad) * radius), c[1] + (Math.cos(anglerad) * radius), 0).endVertex();
            }
        }

        tessellator.draw();
        disableBlend();
        enableTexture2D();
    }

    //DrawCFImg
    public static void drawCFImg(ResourceLocation image, float x, float y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        glTranslatef(x, y, x);
        mc.getTextureManager().bindTexture2(image);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
        glTranslatef(-x, -y, -x);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static int width() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
    }
    public static int height() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
    }

    //

    //HurtCam
    public static void drawGradientSidewaysV2(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, bottom);
        GL11.glVertex2d(right, bottom);
        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, top);
        GL11.glVertex2d(left, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
        Gui.drawRect(0, 0, 0, 0, 0);
    }

    public static void drawGradientSidewaysV(double left, double top, double right, double bottom, int col1, int col2) {
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glShadeModel(GL_SMOOTH);

        quickDrawGradientSidewaysV(left, top, right, bottom, col1, col2);

        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
    }

    public static void quickDrawGradientSidewaysV(double left, double top, double right, double bottom, int col1, int col2) {
        glBegin(GL_QUADS);

        glColor(col1);
        glVertex2d(right, top);
        glVertex2d(left, top);
        glColor(col2);
        glVertex2d(left, bottom); // TODO: Fix this, this may have been a mistake
        glVertex2d(right, bottom);

        glEnd();
    }

    //ESP new
    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtil roundedTexturedShader) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedTexturedShader.setUniformf("location", x * sr.getScaleFactor(),
                (Minecraft.getMinecraft().displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        roundedTexturedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedTexturedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }
    public static void drawGradientRound(float x, float y, float width, float height, float radius, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        roundedGradientShader.init();
        setupRoundedRectUniforms(x, y, width, height, radius, roundedGradientShader);
        // Bottom Left
        roundedGradientShader.setUniformf("color1", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f, bottomLeft.getAlpha() / 255f);
        //Top left
        roundedGradientShader.setUniformf("color2", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f, topLeft.getAlpha() / 255f);
        //Bottom Right
        roundedGradientShader.setUniformf("color3", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f, bottomRight.getAlpha() / 255f);
        //Top Right
        roundedGradientShader.setUniformf("color4", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f, topRight.getAlpha() / 255f);
        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedGradientShader.unload();
        GlStateManager.disableBlend();
    }

    //ESP Real2D
    public static void glColorHex(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    //Shadow
    public static void drawShadow(int x, int y, float width, float height) {
        drawImage(classProvider.createResourceLocation(Terra.CLIENT_NAME.toLowerCase() + "/shadow/shadow_250_125.png"), x - 25, y - 25, (int) (width + 50), (int) (height + 50));
    }

    public static void drawShadow(final float x, final float y, final float width, final float height) {
        drawTexturedRect(x - 9.0f, y - 9.0f, 9.0f, 9.0f, "paneltopleft");
        drawTexturedRect(x - 9.0f, y + height, 9.0f, 9.0f, "panelbottomleft");
        drawTexturedRect(x + width, y + height, 9.0f, 9.0f, "panelbottomright");
        drawTexturedRect(x + width, y - 9.0f, 9.0f, 9.0f, "paneltopright");
        drawTexturedRect(x - 9.0f, y, 9.0f, height, "panelleft");
        drawTexturedRect(x + width, y, 9.0f, height, "panelright");
        drawTexturedRect(x, y - 9.0f, width, 9.0f, "paneltop");
        drawTexturedRect(x, y + height, width, 9.0f, "panelbottom");
    }
    public static void drawTexturedRect(final float x, final float y, final float width, final float height, final String image) {
        GL11.glPushMatrix();
        final boolean enableBlend = GL11.glIsEnabled(3042);
        final boolean disableAlpha = !GL11.glIsEnabled(3008);
        if (!enableBlend) {
            GL11.glEnable(3042);
        }
        if (!disableAlpha) {
            GL11.glDisable(3008);
        }
        mc2.getTextureManager().bindTexture(new ResourceLocation(Terra.CLIENT_NAME.toLowerCase() + ("/shader/noti/" + image + ".png")));
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        if (!enableBlend) {
            GL11.glDisable(3042);
        }
        if (!disableAlpha) {
            GL11.glEnable(3008);
        }
        GL11.glPopMatrix();
    }

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void setAlphaLimit(final float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * 0.01));
    }

    //DrawRound
    public static void drawRound(float x, float y, float width, float height, float radius, Color color) {
        drawRound(x, y, width, height, radius, false, color);
    }

    public static void drawRoundScale(float x, float y, float width, float height, float radius, Color color, float scale) {
        drawRound(x + width - width * scale, y + height / 2f - ((height / 2f) * scale),
                width * scale, height * scale, radius, false, color);
    }
    public static void drawRound(float x, float y, float width, float height, float radius, boolean blur, Color color) {
        resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void clearCaps(final String scale) {
        if(!glCapMap.containsKey(scale)) {
            return;
        }
        Map<Integer, Boolean> map = glCapMap.get(scale);
        if(!map.isEmpty()) {
            //ClientUtils.INSTANCE.logWarn("Cap map is not empty! [" + map.size() + "]");
        }
        map.clear();
    }

    public static void clearCaps() {
        clearCaps("COMMON");
    }



    public static void setGlCap(final int cap, final boolean state, final String scale) {
        if(!glCapMap.containsKey(scale)) {
            glCapMap.put(scale, new HashMap<>());
        }
        glCapMap.get(scale).put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void enableSmoothLine(float width) {
        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(2884);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glLineWidth(width);
    }
    public static void disableSmoothLine() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDepthMask(true);
        GL11.glCullFace(1029);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

}