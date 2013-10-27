package rastersim;

import org.flowutils.gradient.ColorGradient;
import org.flowutils.rawimage.RawImagePanel;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Random;

/**
 *
 */
public class RandomSimulation implements Simulation {
    private final Random random = new Random();
    private final RawImagePanel screen;
    private final int width;
    private final int height;

    private Raster updatedRaster;
    private Raster visibleRaster;

    private RasterRenderer renderer;

    private PixelFont font;

    private float messageTimeLeft = 0;
    private int messageX;
    private int messageY;
    private String message;

    public RandomSimulation() {
        this(200, 160);
    }

    public RandomSimulation(int width, int height) {
        this.width = width;
        this.height = height;

        final ColorGradient gradient = new ColorGradient();
        gradient.addColor(-0.4, 0.0, 0.0, 0.0);
        gradient.addColor( 0.0, 0.3, 0.3, 0.2);
        gradient.addColor( 0.2, 0.6, 0.2, 0.1);
        gradient.addColor( 0.4, 0.9, 0.2, 0.0);
        gradient.addColor( 0.6, 1.0, 0.4, 0.0);
        gradient.addColor( 0.8, 1.0, 1.0, 0.0);
        gradient.addColor( 1.0, 1.0, 1.0, 0.5);

        font = new PixelFont(new File("assets/font.png"));

        updatedRaster = new Raster(width, height, true);
        visibleRaster = new Raster(width, height, true);
        renderer = new RasterRenderer(visibleRaster, gradient, 0, 0, 1, 4);
        screen = new RawImagePanel(renderer);

        final String msg = "*** Hello World! ***           FiRe EfFeCt wItH SpaRkS aNd sCrOlLEr. .. :: \\-o_o-/ :: ..   .        .                  .";

        scrollMessage(msg, 1000, (int) (height*0.5));

        /*
        if (random.nextFloat() < 0.01) {
            messageTimeLeft = random.nextFloat() * 5 + 3;
            messageX = width;
            messageY = random.nextInt(height/2);
            message = pickOneRandomly(random, msg, "I'm on Fire!", "Pretty sparks","O_o");
        }
        */

    }

    private void scrollMessage(String message, float showTimeSeconds, int yPos) {
        messageTimeLeft = showTimeSeconds;
        messageX = width;
        messageY = yPos;
        this.message = message;
    }

    private void flip() {
        Raster t = visibleRaster;
        visibleRaster = updatedRaster;
        updatedRaster = t;
        renderer.setRaster(visibleRaster);
        updatedRaster.copyFrom(visibleRaster);
    }

    @Override
    public void update(double currentTime, double deltaTime) {

        if (messageTimeLeft > 0) {
            int y = (int) (Math.sin(currentTime * 2) * 5 + messageY);
            int fontSize = 3;
            messageTimeLeft -= deltaTime;
/*            font.drawString(visibleRaster,
                            messageX,
                            y+1,
                            message,
                            fontSize,
                            -0.2f, 0);
            font.drawString(visibleRaster,
                            messageX+1,
                            y,
                            message,
                            fontSize,
                            0.1f, -0.1f);
*/
/*            font.drawString(visibleRaster,
                            messageX-1,
                            y,
                            message,
                            fontSize,
                            -0.801f, 0);
                            */
            font.drawString(visibleRaster,
                            messageX + 1,
                            y-1,
                            message,
                            fontSize,
                            0.85f, -0.17f);
            font.drawString(visibleRaster,
                            messageX,
                            y,
                            message,
                            fontSize,
                            1.3f, 0);
            messageX -= 1;
        }


        for (int x = 0; x < width ; x++) {
            for (int y = 0; y < height; y++) {

                float old = visibleRaster.get(x, y);

                if (random.nextFloat() < 0.0001f) {
                    final float value = old + 0.0f + 0.2f * (float) (random.nextGaussian());
                    visibleRaster.set(x,y, value);
                    visibleRaster.set(x,y+1, value);
                    visibleRaster.set(x,y-1, value);
                    visibleRaster.set(x+1,y, value);
                    visibleRaster.set(x-1,y, value);
                }
                if (random.nextFloat() < 0.00002f) {
                    final float value = old + 0.5f + 2.0f * (float) (random.nextGaussian());
                    visibleRaster.set(x,y, value);
                    visibleRaster.set(x,y+1, value);
                    visibleRaster.set(x,y-1, value);
                    visibleRaster.set(x+1,y, value);
                    visibleRaster.set(x-1,y, value);
                }

                old = visibleRaster.get(x, y);
                final float up = visibleRaster.get(x, y-1);
                final float down = visibleRaster.get(x, y+1);
                final float left = visibleRaster.get(x-1, y+1);
                final float right = visibleRaster.get(x+1, y+1);

                float cooldown = random.nextFloat() * 0.01f;
                float newValue = (old * 3 + down * 5 + left + right + up) / 11f - cooldown;
                updatedRaster.set(x,y, newValue);

                // Bottom source
                if (y == height - 1) {
                    updatedRaster.set(x,y, 0.2f+1.2f*(float) (Math.max(-0.15f, random.nextGaussian())));
                }

            }
        }



    }

    private <T> T pickOneRandomly(List<T> alternatives, Random random) {
        return alternatives.get(random.nextInt(alternatives.size()));
    }

    private String pickOneRandomly(Random random, String ... alternatives) {
        return alternatives[random.nextInt(alternatives.length)];
    }

    @Override public void render() {
        flip();

        screen.reRender();
    }

    @Override public JComponent getUI() {
        return screen;
    }
}