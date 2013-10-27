package rastersim;

import org.flowutils.gradient.ColorFunction;
import org.flowutils.rawimage.RawImage;
import org.flowutils.rawimage.RawImageRenderer;

import java.awt.*;

/**
 *
 */
public class RasterRenderer implements RawImageRenderer {

    private Raster raster;
    private int leftBorder;
    private int rightBorder;
    private int topBorder;
    private int bottomBorder;

    private ColorFunction colorFunction;

    public RasterRenderer(Raster raster, ColorFunction colorFunction) {
        this(raster, colorFunction, 0, 0, 0, 0);
    }

    public RasterRenderer(Raster raster,
                          ColorFunction colorFunction,
                          int leftBorder,
                          int rightBorder,
                          int topBorder,
                          int bottomBorder) {
        this.raster = raster;
        this.colorFunction = colorFunction;
        this.leftBorder = leftBorder;
        this.rightBorder = rightBorder;
        this.topBorder = topBorder;
        this.bottomBorder = bottomBorder;
    }

    public Raster getRaster() {
        return raster;
    }

    public void setRaster(Raster raster) {
        this.raster = raster;
    }

    public ColorFunction getColorFunction() {
        return colorFunction;
    }

    public void setColorFunction(ColorFunction colorFunction) {
        this.colorFunction = colorFunction;
    }

    @Override public void renderImage(RawImage target) {

        int w = target.getWidth();
        int h = target.getHeight();

        int rasterW = raster.getW() - leftBorder - rightBorder;
        int rasterH = raster.getH() - topBorder - bottomBorder;

        if (raster != null) {
            float pixelW = 1f * w / rasterW;
            float pixelH = 1f * h / rasterH;

            for (int y = 0; y < rasterH; y++) {
                for (int x = 0; x < rasterW; x++) {
                    int x1 = (int) (x     * pixelW);
                    int y1 = (int) (y     * pixelH);

                    final float value = raster.get(x + leftBorder, y + topBorder);
                    final int colorCode = colorFunction.colorCodeForValue(value);

                    target.fillRect(x1, y1, (int)pixelW + 1, (int)pixelH + 1, colorCode);
                }
            }
        }
        else {
            target.fillRect(0,0, w, h, Color.WHITE.getRGB());
        }

    }

}
