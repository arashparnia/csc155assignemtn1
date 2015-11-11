package utilities;

import com.jogamp.opengl.awt.GLCanvas;

public class Animator {
    private GLCanvas myCanvas;
    private long frameRate = 60;
    private boolean anim = true;

    public Animator(GLCanvas inCanvas) {
        myCanvas = inCanvas;
    }

    public void start() {
        while (true) {
            if (anim) myCanvas.display();
            try {
                Thread.sleep(frameRate);
            } catch (InterruptedException e) {
            }
        }
    }

    public void pause(boolean an) {
        anim = !an;
    }
}
