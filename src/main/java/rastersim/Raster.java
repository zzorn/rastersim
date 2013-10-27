package rastersim;


import org.flowutils.Check;

import static org.flowutils.Maths.*;

/**
 * Two dimensional float array with utility functions.
 */
public final class Raster {
    private final int w;
    private final int h;
    private final boolean wrap;

    private final float [] data ;

    public Raster(int w, int h, boolean wrap) {
        this.w = w;
        this.h = h;
        this.wrap = wrap;
        data = new float[w * h];
    }

    public float get(int x, int y) {
        return data[index(x, y)];
    }

    public float getInterpolated(float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        float cx = x - x0;
        float cy = y - y0;
        final float yr0 = mix(cx, get(x0, y0), get(x1, y0));
        final float yr1 = mix(cx, get(x0, y1), get(x1, y1));
        return mix(cy, yr0, yr1);
    }

    public void set(int x, int y, float v) {
        data[index(x, y)] = v;
    }

    public void set(int x, int y, float v, boolean wrap) {
        if (wrap || (x >= 0 && x < w && y >= 0 && y < h)) data[index(x, y)] = v;
    }

    public void fillRect(int x, int y, int w, int h, float v, boolean wrap) {
        for (int yp = y; yp < y+h; yp++) {
            for (int xp = x; xp < x+w; xp++) {
                set(xp, yp, v, wrap);
            }
        }
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    private int index(int x, int y) {
        if (wrap) {
            // Wrap edges
            while (x < 0) x += w;
            while (y < 0) y += h;
            x = x % w;
            y = y % h;
        }
        else  {
            // Clamp to edges
            if (x < 0) x = 0;
            if (x > w) x = w;
            if (y < 0) y = 0;
            if (y > h) y = h;
        }

        return x + y * w;
    }

    public void copyFrom(Raster other) {
        System.arraycopy(other.data, 0, data, 0, data.length);
    }

    public void add(Raster source, float scale, float offset) {
        checkSizeMatches(source);

        for (int i = 0; i < data.length; i++) {
            data[i] += source.data[i] * scale + offset;
        }
    }

    public void diffuse(Raster source, float diffusion, float deltaTime, float cellSizeMeter, int b) {
        checkSizeMatches(source);

        for (int k = 0; k < 20; k++) {
            diffuseStep(source, diffusion, deltaTime, cellSizeMeter);

            setBoundaries(b);
        }
    }

    public void setBoundaries(int b) {
        for (int x = 1; x < w-1; x++) {
            data[x]           = (b==2 ? -1 : 1) * data[x + 1*w];
            data[x + (h-1)*w] = (b==2 ? -1 : 1) * data[x + (h-2)*w];
        }
        for (int y = 1; y < h - 1; y++) {
            data[y*w]       = (b==1 ? -1 : 1) * data[1   + y * w];
            data[w-1 + y*w] = (b==1 ? -1 : 1) * data[w-2 + y * w];
        }

        set(0,   0,   (get(1,   0  ) + get(0,   1  )) / 2f);
        set(0,   h-1, (get(1,   h-1) + get(0,   h-2)) / 2f);
        set(w-1, 0,   (get(w-2,   0) + get(w-1, 1  )) / 2f);
        set(w-1, h-1, (get(1,   h-1) + get(0,   h-2)) / 2f);
    }

    public void diffuseStep(Raster source, float diffusion, float deltaTime, float cellSizeMeter) {
        checkSizeMatches(source);

        float cellsPerMeter = 1f / cellSizeMeter;
        float a = deltaTime * diffusion * cellsPerMeter * cellsPerMeter;

        final float[] sourceData = source.data;

        for (int y = 1; y < h-1; y++) {
            for (int x = 1; x < w-1; x++) {
                data[x+y*w] = (sourceData[x + y*w] +
                             a * (sourceData[x+1 + y*w] +
                                  sourceData[x-1 + y*w] +
                                  sourceData[x + (y+1)*w] +
                                  sourceData[x + (y-1)*w]))
                            / (1 + 4 * a);
            }
        }
    }

    public void advect(int b, Raster source, Raster xVel, Raster yVel, float deltaTime, float cellSizeMeter) {
        checkSizeMatches(source);
        checkSizeMatches(xVel);
        checkSizeMatches(yVel);

        float cellsPerMeter = 1f / cellSizeMeter;
        float dt0 = deltaTime * cellsPerMeter;

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                float xSource = x - dt0 * xVel.get(x, y);
                float ySource = y - dt0 * yVel.get(x, y);

                clamp(xSource, 0.5f, w - 0.5f);
                clamp(ySource, 0.5f, h - 0.5f);

                data[x + y * w] = source.getInterpolated(xSource, ySource);
            }
        }

        setBoundaries(b);
    }

    public void densitySimulationStep(Raster previousDensity, Raster xVelocity, Raster yVelocity, float diffusion, float deltaTimeSeconds, float cellSizeMeter) {
        add(previousDensity, deltaTimeSeconds, 0);
        previousDensity.diffuse(this, diffusion, deltaTimeSeconds, cellSizeMeter, 0);
        advect(0, previousDensity, xVelocity, yVelocity, deltaTimeSeconds, cellSizeMeter);
    }

    public static void velocityStep(Raster xVel, Raster yVel, Raster prevXVel, Raster prevYVel, Raster xForce, Raster yForce, float viscosity, float deltaTimeSeconds, float cellSizeMeter) {
        xVel.add(xForce, deltaTimeSeconds, 0);
        yVel.add(yForce, deltaTimeSeconds, 0);

        prevXVel.diffuse(xVel, viscosity, deltaTimeSeconds, cellSizeMeter, 1);
        prevYVel.diffuse(yVel, viscosity, deltaTimeSeconds, cellSizeMeter, 2);



        // TODO
    }

    public static void project(Raster xVel, Raster yVel, Raster p, Raster div, float cellSizeMeter) {
        


    }



    private void checkSizeMatches(Raster source) {
        Check.equal(source.w, "source width", w, "target width");
        Check.equal(source.h, "source height", h, "target height");
    }


}