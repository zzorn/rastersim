package rastersim;

/**
 *
 */
public class Simulator implements Runnable {

    private static final double NANOSEC_TO_SEC = 0.000000001;
    private final Simulation simulation;
    private final Thread thread;

    private double simulationTime;
    private double timeStep = 0.02;
    private double maxTimeStep = timeStep * 25;

    private boolean running = false;

    public Simulator(Simulation simulation) {
        this.simulation = simulation;

        thread = new Thread(this);
        thread.setDaemon(true); // Stop when other threads have stopped.
        start();
    }

    private void start() {
        if (!running) {
            running = true;
            thread.start();
        }
    }

    private void stop() {
        running = false;
    }


    @Override
    public void run() {
        double oldTime = currentTimeInSeconds();
        double remainingTime = 0.0;

        while(running) {
            double frameStartTime = currentTimeInSeconds();
            double frameTime = frameStartTime - oldTime;
            if (frameTime > maxTimeStep) frameTime = maxTimeStep;

            oldTime = frameStartTime;

            remainingTime += frameTime;

            // Update simulation
            while ( remainingTime >= timeStep ) {
                simulation.update(simulationTime, timeStep);
                remainingTime -= timeStep;
                simulationTime += timeStep;
            }

            // Render simulation
            simulation.render();

            // Idle until next frame
            final double frameEndTime = currentTimeInSeconds();
            final double frameDuration = frameEndTime - frameStartTime;
            double timeLeftInFrame = timeStep - frameDuration;
            delay(timeLeftInFrame);
        }
    }

    private double currentTimeInSeconds() {
        return System.nanoTime() * NANOSEC_TO_SEC;
    }

    private void delay(final double timeSeconds) {
        if (timeSeconds > 0) {
            // Sleep
            try {
                Thread.sleep((long) (timeSeconds *1000));
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}