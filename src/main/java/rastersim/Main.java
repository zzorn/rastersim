package rastersim;


import org.flowutils.SimpleFrame;

/**
 *
 */
public class Main {
    public static void main(String[] args) {

        // Create simulation
        RandomSimulation simulation = new RandomSimulation();

        // Start simulator
        new Simulator(simulation);

        // Open frame showing simulated thing
        new SimpleFrame("Simulatator", simulation.getUI());
    }
}
