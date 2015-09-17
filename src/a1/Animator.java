package a1;

import com.jogamp.opengl.awt.GLCanvas;

public class Animator {
    private GLCanvas myCanvas;
    private long frameRate = 60;

    public Animator(GLCanvas inCanvas) {
        myCanvas = inCanvas;
    }

    public void start() {
        while (true) {
            myCanvas.display();
            try {
                Thread.sleep(frameRate);
            } catch (InterruptedException e) {
            }
        }
    }
}