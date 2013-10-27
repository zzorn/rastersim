package rastersim;

import javax.swing.*;

/**
 *
 */
public interface Simulation {

    void update(double currentTime, double deltaTime);

    void render();

    JComponent getUI();
}