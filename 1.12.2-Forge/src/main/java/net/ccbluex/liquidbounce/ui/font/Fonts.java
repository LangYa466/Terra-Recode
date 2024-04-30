package net.ccbluex.liquidbounce.ui.font;

import net.ccbluex.liquidbounce.api.minecraft.client.gui.IFontRenderer;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

public class Fonts extends MinecraftInstance {

    @FontDetails(fontName = "Minecraft Font")
    public static final IFontRenderer minecraftFont = mc.getFontRendererObj();

    private static final HashMap<FontInfo, IFontRenderer> CUSTOM_FONT_RENDERERS = new HashMap<>();

    @FontDetails(fontName = "Roboto Medium", fontSize = 25)
    public static IFontRenderer font25;
    @FontDetails(fontName = "Roboto Medium", fontSize = 30)
    public static IFontRenderer font30;
    @FontDetails(fontName = "Roboto Medium", fontSize = 35)
    public static IFontRenderer font35;
    @FontDetails(fontName = "Roboto Medium", fontSize = 40)
    public static IFontRenderer font40;
    @FontDetails(fontName = "Roboto Medium", fontSize = 45)
    public static IFontRenderer font45;

    @FontDetails(fontName = "Roboto Bold", fontSize = 80)
    public static IFontRenderer font80;
    @FontDetails(fontName = "Roboto Bold", fontSize = 95)
    public static IFontRenderer font95;
    @FontDetails(fontName = "Roboto Bold", fontSize = 120)
    public static IFontRenderer font120;
    @FontDetails(fontName = "Roboto Bold", fontSize = 180)
    public static IFontRenderer font180;

    @FontDetails(fontName = "Posterama Text", fontSize = 25)
    public static IFontRenderer posterama25;
    @FontDetails(fontName = "Posterama Text", fontSize = 30)
    public static IFontRenderer posterama30;
    @FontDetails(fontName = "Posterama Text", fontSize = 35)
    public static IFontRenderer posterama35;
    @FontDetails(fontName = "Posterama Text", fontSize = 40)
    public static IFontRenderer posterama40;

    @FontDetails(fontName = "SF UI Display Regular", fontSize = 30)
    public static IFontRenderer sfui30;
    @FontDetails(fontName = "SF UI Display Regular", fontSize = 35)
    public static IFontRenderer sfui35;
    @FontDetails(fontName = "SF UI Display Regular", fontSize = 40)
    public static IFontRenderer sfui40;
    @FontDetails(fontName = "SF UI Display Regular", fontSize = 45)
    public static IFontRenderer sfui45;

    @FontDetails(fontName = "Nunito Bold", fontSize = 25)
    public static IFontRenderer rise25;
    @FontDetails(fontName = "Nunito Bold", fontSize = 30)
    public static IFontRenderer rise30;
    @FontDetails(fontName = "Nunito Bold", fontSize = 35)
    public static IFontRenderer rise35;
    @FontDetails(fontName = "Nunito Bold", fontSize = 40)
    public static IFontRenderer rise40;
    @FontDetails(fontName = "Nunito Bold", fontSize = 45)
    public static IFontRenderer rise45;
    @FontDetails(fontName = "Nunito Bold", fontSize = 60)
    public static IFontRenderer rise60;

    public static void loadFonts() {

        font25 = classProvider.wrapFontRenderer(new GameFontRenderer(getMedium(25)));
        font30 = classProvider.wrapFontRenderer(new GameFontRenderer(getMedium(30)));
        font35 = classProvider.wrapFontRenderer(new GameFontRenderer(getMedium(35)));
        font40 = classProvider.wrapFontRenderer(new GameFontRenderer(getMedium(40)));
        font45 = classProvider.wrapFontRenderer(new GameFontRenderer(getMedium(45)));

        font80 = classProvider.wrapFontRenderer(new GameFontRenderer(getBold(80)));
        font95 = classProvider.wrapFontRenderer(new GameFontRenderer(getBold(95)));
        font120 = classProvider.wrapFontRenderer(new GameFontRenderer(getBold(120)));
        font180 = classProvider.wrapFontRenderer(new GameFontRenderer(getBold(180)));

        posterama25 = classProvider.wrapFontRenderer(new GameFontRenderer(getPosterama(25)));
        posterama30 = classProvider.wrapFontRenderer(new GameFontRenderer(getPosterama(30)));
        posterama35 = classProvider.wrapFontRenderer(new GameFontRenderer(getPosterama(35)));
        posterama40 = classProvider.wrapFontRenderer(new GameFontRenderer(getPosterama(40)));

        sfui30 = classProvider.wrapFontRenderer(new GameFontRenderer(getSFUI(30)));
        sfui35 = classProvider.wrapFontRenderer(new GameFontRenderer(getSFUI(35)));
        sfui40 = classProvider.wrapFontRenderer(new GameFontRenderer(getSFUI(40)));
        sfui45 = classProvider.wrapFontRenderer(new GameFontRenderer(getSFUI(45)));

        rise25 = classProvider.wrapFontRenderer(new GameFontRenderer(getRise(25)));
        rise30 = classProvider.wrapFontRenderer(new GameFontRenderer(getRise(30)));
        rise35 = classProvider.wrapFontRenderer(new GameFontRenderer(getRise(35)));
        rise40 = classProvider.wrapFontRenderer(new GameFontRenderer(getRise(40)));
        rise45 = classProvider.wrapFontRenderer(new GameFontRenderer(getRise(45)));
        rise60 = classProvider.wrapFontRenderer(new GameFontRenderer(getRise(60)));

    }

    public static IFontRenderer getFontRenderer(final String name, final int size) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                Object o = field.get(null);

                if (o instanceof IFontRenderer) {
                    FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    if (fontDetails.fontName().equals(name) && fontDetails.fontSize() == size)
                        return (IFontRenderer) o;
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return CUSTOM_FONT_RENDERERS.getOrDefault(new FontInfo(name, size), minecraftFont);
    }

    public static FontInfo getFontDetails(final IFontRenderer fontRenderer) {
        for (final Field field : Fonts.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                final Object o = field.get(null);

                if (o.equals(fontRenderer)) {
                    final FontDetails fontDetails = field.getAnnotation(FontDetails.class);

                    return new FontInfo(fontDetails.fontName(), fontDetails.fontSize());
                }
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<FontInfo, IFontRenderer> entry : CUSTOM_FONT_RENDERERS.entrySet()) {
            if (entry.getValue() == fontRenderer)
                return entry.getKey();
        }

        return null;
    }

    private static Font getMedium(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("terra/font/pingfang.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", 0, size);
        }
        return font;
    }

    private static Font getBold(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("terra/font/bold.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", 0, size);
        }
        return font;
    }

    private static Font getPosterama(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("terra/font/posterama.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", 0, size);
        }
        return font;
    }

    private static Font getSFUI(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("terra/font/sfui.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", 0, size);
        }
        return font;
    }

    private static Font getRise(int size) {
        Font font;
        try {
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("terra/font/rise.ttf")).getInputStream();
            font = Font.createFont(0, is);
            font = font.deriveFont(0, size);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error loading font");
            font = new Font("default", 0, size);
        }
        return font;
    }


    public static List<IFontRenderer> getFonts() {
        final List<IFontRenderer> fonts = new ArrayList<>();

        for (final Field fontField : Fonts.class.getDeclaredFields()) {
            try {
                fontField.setAccessible(true);

                final Object fontObj = fontField.get(null);

                if (fontObj instanceof IFontRenderer) fonts.add((IFontRenderer) fontObj);
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        fonts.addAll(Fonts.CUSTOM_FONT_RENDERERS.values());

        return fonts;
    }

    public static class FontInfo {
        private final String name;
        private final int fontSize;

        public FontInfo(String name, int fontSize) {
            this.name = name;
            this.fontSize = fontSize;
        }

        public String getName() {
            return name;
        }

        public int getFontSize() {
            return fontSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FontInfo fontInfo = (FontInfo) o;

            if (fontSize != fontInfo.fontSize) return false;
            return Objects.equals(name, fontInfo.name);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + fontSize;
            return result;
        }
    }

}