package rastersim;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 *
 */
public class PixelFont {

    private final BufferedImage fontImage;
    private final int firstCharCode;
    private final int charW;
    private final int charH;
    private final int xGap = 0;
    private final int yGap = 2;
    private final int charsAlongX;
    private final int charsAlongY;
    private final int lastCharCode;

    private Random random = new Random();


    public PixelFont(File fontFile) {
        this(fontFile, 8, 8);
    }

    public PixelFont(File fontFile, int charW, int charH) {
        this(fontFile, charW, charH, 0);
    }

    public PixelFont(File fontFile, int charW, int charH, int firstCharCode) {
        this.charW = charW;
        this.charH = charH;
        this.firstCharCode = firstCharCode;

        try {
            fontImage = ImageIO.read(fontFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not load font file " + fontFile.getPath() + ": " + e.getMessage());
        }

        charsAlongX = fontImage.getWidth() / charW;
        charsAlongY = fontImage.getHeight() / charH;
        int numChars = charsAlongX * charsAlongY;
        lastCharCode = firstCharCode + numChars;
    }

    public void drawString(Raster raster, int x, int y, String text, int fontSize, float intensityScale, float intensityOffset) {
        int xPos = x;
        int yPos = y;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);

            if (c == '\n') {
                // Next line
                xPos = x;
                yPos = yPos + charH * fontSize + yGap * fontSize;
            }
            else {
                drawChar(raster, xPos, yPos, c, fontSize, intensityScale, intensityOffset);

                xPos += charW * fontSize + xGap * fontSize;
            }
        }
    }

    private void drawChar(Raster raster, int xPos, int yPos, char c, int fontSize, float intensityScale, float intensityOffset) {
        if (c >= firstCharCode && c <= lastCharCode) {
            int srcX = charW * ((c - firstCharCode) % charsAlongX);
            int srcY = charH * ((c - firstCharCode) / charsAlongX);

            int pixelSize = Math.max(1, fontSize - 1);

            for (int y = 0; y < charH; y++) {
                for (int x = 0; x < charW; x++) {
                    final int rgbaColor = fontImage.getRGB(srcX + x, srcY + y);
                    float value = (rgbaColor & 0xFF) / 255f;  // TODO: Get luminosity
                    if (value > 0.2f) {
                        final float old = raster.get(xPos + x, yPos + y);
                        final float v = 0.0f*old + intensityScale*(value * (1 +0.05f*(float) random.nextGaussian())) + intensityOffset;
                        raster.fillRect(xPos + x*fontSize, yPos + y*fontSize, pixelSize, pixelSize, v, false);
                    }
                }
            }

        }
    }
}
