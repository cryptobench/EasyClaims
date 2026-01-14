package com.easyclaims.map;

/**
 * 5x7 bitmap font for rendering text on map tiles.
 * Each character is stored as an array of 7 rows, where each row is a 5-bit bitmask.
 */
public class BitmapFont {

    public static final int CHAR_WIDTH = 5;
    public static final int CHAR_HEIGHT = 7;
    public static final int CHAR_SPACING = 1;

    // Font data: 5x7 pixels per character
    private static final int[][] GLYPHS = new int[128][];

    static {
        // Letters A-Z (5x7)
        GLYPHS['A'] = new int[]{0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001};
        GLYPHS['B'] = new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110};
        GLYPHS['C'] = new int[]{0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110};
        GLYPHS['D'] = new int[]{0b11110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b11110};
        GLYPHS['E'] = new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111};
        GLYPHS['F'] = new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000};
        GLYPHS['G'] = new int[]{0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110};
        GLYPHS['H'] = new int[]{0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001};
        GLYPHS['I'] = new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b11111};
        GLYPHS['J'] = new int[]{0b00111, 0b00010, 0b00010, 0b00010, 0b00010, 0b10010, 0b01100};
        GLYPHS['K'] = new int[]{0b10001, 0b10010, 0b10100, 0b11000, 0b10100, 0b10010, 0b10001};
        GLYPHS['L'] = new int[]{0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111};
        GLYPHS['M'] = new int[]{0b10001, 0b11011, 0b10101, 0b10101, 0b10001, 0b10001, 0b10001};
        GLYPHS['N'] = new int[]{0b10001, 0b11001, 0b10101, 0b10101, 0b10011, 0b10001, 0b10001};
        GLYPHS['O'] = new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110};
        GLYPHS['P'] = new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000};
        GLYPHS['Q'] = new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10101, 0b10010, 0b01101};
        GLYPHS['R'] = new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001};
        GLYPHS['S'] = new int[]{0b01110, 0b10001, 0b10000, 0b01110, 0b00001, 0b10001, 0b01110};
        GLYPHS['T'] = new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100};
        GLYPHS['U'] = new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110};
        GLYPHS['V'] = new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100};
        GLYPHS['W'] = new int[]{0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001};
        GLYPHS['X'] = new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b01010, 0b10001, 0b10001};
        GLYPHS['Y'] = new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100};
        GLYPHS['Z'] = new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111};

        // Lowercase maps to uppercase
        for (char c = 'a'; c <= 'z'; c++) {
            GLYPHS[c] = GLYPHS[Character.toUpperCase(c)];
        }

        // Numbers 0-9
        GLYPHS['0'] = new int[]{0b01110, 0b10001, 0b10011, 0b10101, 0b11001, 0b10001, 0b01110};
        GLYPHS['1'] = new int[]{0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110};
        GLYPHS['2'] = new int[]{0b01110, 0b10001, 0b00001, 0b00110, 0b01000, 0b10000, 0b11111};
        GLYPHS['3'] = new int[]{0b01110, 0b10001, 0b00001, 0b00110, 0b00001, 0b10001, 0b01110};
        GLYPHS['4'] = new int[]{0b00010, 0b00110, 0b01010, 0b10010, 0b11111, 0b00010, 0b00010};
        GLYPHS['5'] = new int[]{0b11111, 0b10000, 0b11110, 0b00001, 0b00001, 0b10001, 0b01110};
        GLYPHS['6'] = new int[]{0b00110, 0b01000, 0b10000, 0b11110, 0b10001, 0b10001, 0b01110};
        GLYPHS['7'] = new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b01000, 0b01000};
        GLYPHS['8'] = new int[]{0b01110, 0b10001, 0b10001, 0b01110, 0b10001, 0b10001, 0b01110};
        GLYPHS['9'] = new int[]{0b01110, 0b10001, 0b10001, 0b01111, 0b00001, 0b00010, 0b01100};

        // Special characters
        GLYPHS[' '] = new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000};
        GLYPHS['.'] = new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b01100, 0b01100};
        GLYPHS[','] = new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00100, 0b00100, 0b01000};
        GLYPHS[':'] = new int[]{0b00000, 0b01100, 0b01100, 0b00000, 0b01100, 0b01100, 0b00000};
        GLYPHS['-'] = new int[]{0b00000, 0b00000, 0b00000, 0b11111, 0b00000, 0b00000, 0b00000};
        GLYPHS['_'] = new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b11111};
        GLYPHS['+'] = new int[]{0b00000, 0b00100, 0b00100, 0b11111, 0b00100, 0b00100, 0b00000};
        GLYPHS['\''] = new int[]{0b00100, 0b00100, 0b01000, 0b00000, 0b00000, 0b00000, 0b00000};
        GLYPHS['!'] = new int[]{0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00000, 0b00100};
        GLYPHS['?'] = new int[]{0b01110, 0b10001, 0b00010, 0b00100, 0b00100, 0b00000, 0b00100};
    }

    /**
     * Calculate the width in pixels needed to render a string.
     */
    public static int getTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        return text.length() * CHAR_WIDTH + (text.length() - 1) * CHAR_SPACING;
    }

    /**
     * Draw text onto an image data array.
     */
    public static void drawText(int[] imageData, int imageWidth, int imageHeight,
                                String text, int startX, int startY, int color) {
        if (text == null) return;

        int x = startX;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            drawChar(imageData, imageWidth, imageHeight, c, x, startY, color);
            x += CHAR_WIDTH + CHAR_SPACING;
        }
    }

    /**
     * Draw text centered horizontally within the image.
     */
    public static void drawTextCentered(int[] imageData, int imageWidth, int imageHeight,
                                        String text, int centerY, int color) {
        int textWidth = getTextWidth(text);
        int startX = (imageWidth - textWidth) / 2;
        drawText(imageData, imageWidth, imageHeight, text, startX, centerY, color);
    }

    /**
     * Draw text with an outline for better visibility.
     */
    public static void drawTextWithOutline(int[] imageData, int imageWidth, int imageHeight,
                                           String text, int startX, int startY,
                                           int textColor, int outlineColor) {
        // Draw outline in all 8 directions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    drawText(imageData, imageWidth, imageHeight, text, startX + dx, startY + dy, outlineColor);
                }
            }
        }
        // Draw main text on top
        drawText(imageData, imageWidth, imageHeight, text, startX, startY, textColor);
    }

    /**
     * Draw text with a shadow/outline for better visibility.
     */
    public static void drawTextWithShadow(int[] imageData, int imageWidth, int imageHeight,
                                          String text, int startX, int startY,
                                          int textColor, int shadowColor) {
        // Draw shadow offset by 1 pixel
        drawText(imageData, imageWidth, imageHeight, text, startX + 1, startY + 1, shadowColor);
        // Draw main text on top
        drawText(imageData, imageWidth, imageHeight, text, startX, startY, textColor);
    }

    /**
     * Draw text centered with outline.
     */
    public static void drawTextCenteredWithOutline(int[] imageData, int imageWidth, int imageHeight,
                                                   String text, int centerY,
                                                   int textColor, int outlineColor) {
        int textWidth = getTextWidth(text);
        int startX = (imageWidth - textWidth) / 2;
        drawTextWithOutline(imageData, imageWidth, imageHeight, text, startX, centerY, textColor, outlineColor);
    }

    /**
     * Draw text centered with shadow.
     */
    public static void drawTextCenteredWithShadow(int[] imageData, int imageWidth, int imageHeight,
                                                   String text, int centerY,
                                                   int textColor, int shadowColor) {
        int textWidth = getTextWidth(text);
        int startX = (imageWidth - textWidth) / 2;
        drawTextWithShadow(imageData, imageWidth, imageHeight, text, startX, centerY, textColor, shadowColor);
    }

    /**
     * Draw a single character.
     */
    private static void drawChar(int[] imageData, int imageWidth, int imageHeight,
                                 char c, int x, int y, int color) {
        int[] glyph = (c < GLYPHS.length) ? GLYPHS[c] : null;
        if (glyph == null) {
            glyph = GLYPHS[' ']; // Default to space for unknown chars
        }

        for (int row = 0; row < CHAR_HEIGHT; row++) {
            int rowBits = glyph[row];
            for (int col = 0; col < CHAR_WIDTH; col++) {
                // Check if this pixel should be drawn
                boolean pixelOn = (rowBits & (1 << (CHAR_WIDTH - 1 - col))) != 0;
                if (pixelOn) {
                    int px = x + col;
                    int py = y + row;
                    if (px >= 0 && px < imageWidth && py >= 0 && py < imageHeight) {
                        imageData[py * imageWidth + px] = color;
                    }
                }
            }
        }
    }

    /**
     * Pack RGBA values into an int.
     */
    public static int packColor(int r, int g, int b, int a) {
        return (r & 255) << 24 | (g & 255) << 16 | (b & 255) << 8 | (a & 255);
    }

    /**
     * Common colors for map text.
     */
    public static final int WHITE = packColor(255, 255, 255, 255);
    public static final int BLACK = packColor(0, 0, 0, 255);
    public static final int SHADOW = packColor(30, 30, 30, 255);
    public static final int YELLOW = packColor(255, 255, 100, 255);
    public static final int GREEN = packColor(100, 255, 100, 255);
    public static final int RED = packColor(255, 100, 100, 255);
}
