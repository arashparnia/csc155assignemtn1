package a1;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

public class Ass1 extends JFrame implements GLEventListener, ActionListener, MouseWheelListener {
    private Dimension dimention = new Dimension(1000, 1000);
    private GLCanvas myCanvas;
    private int rendering_program;
    private int VAO[] = new int[1];
    private GLSLUtils util = new GLSLUtils();
    private float upDown = 0.0f;
    private float size = 0.1f;
    private float g = 1f;
    private boolean animated = true;
    private Animator animator;
    private boolean autozoom = true;
    private float n = 0.0f;
    private float sp = 1.0f;
    private Random rand;

    public Ass1() {
        setTitle("Assignment 1 CSC155");
        setSize(dimention);
        this.addMouseWheelListener(this);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        rand = new Random();

        JButton up = new JButton("UP");
        up.setVerticalTextPosition(AbstractButton.CENTER);
        up.setHorizontalTextPosition(AbstractButton.CENTER);
        up.setMnemonic(KeyEvent.VK_W);
        up.setActionCommand("up");
        up.setBounds(5, 0, 100, 50);
        up.addActionListener(this);
        add(up);
        JButton down = new JButton("DOWN");
        down.setVerticalTextPosition(AbstractButton.CENTER);
        down.setHorizontalTextPosition(AbstractButton.CENTER);
        down.setMnemonic(KeyEvent.VK_KP_DOWN);
        down.setActionCommand("down");
        down.setBounds(110, 0, 100, 50);
        down.addActionListener(this);
        add(down);
        JButton color = new JButton("COLOR");
        color.setVerticalTextPosition(AbstractButton.CENTER);
        color.setHorizontalTextPosition(AbstractButton.CENTER);
        color.setMnemonic(KeyEvent.VK_KP_DOWN);
        color.setActionCommand("color");
        color.setBounds(215, 0, 100, 50);
        color.addActionListener(this);
        add(color);
        JButton nse = new JButton("NOISE");
        nse.setVerticalTextPosition(AbstractButton.CENTER);
        nse.setHorizontalTextPosition(AbstractButton.CENTER);
        nse.setMnemonic(KeyEvent.VK_KP_DOWN);
        nse.setActionCommand("noise");
        nse.setBounds(320, 0, 100, 50);
        nse.addActionListener(this);
        add(nse);
        JButton pause = new JButton("AUTOZOOM");
        pause.setVerticalTextPosition(AbstractButton.CENTER);
        pause.setHorizontalTextPosition(AbstractButton.CENTER);
        pause.setMnemonic(KeyEvent.VK_KP_DOWN);
        pause.setActionCommand("autozoom");
        pause.setBounds(425, 0, 100, 50);
        pause.addActionListener(this);
        add(pause);

        add(myCanvas);
        setVisible(true);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    public void init(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderPrograms(drawable);
        gl.glGenVertexArrays(VAO.length, VAO, 0);
        gl.glBindVertexArray(VAO[0]);
        animator = new Animator(myCanvas);
        Thread thread =
                new Thread(new Runnable() {
                    public void run() {
                        animator.start();
                    }
                });
        thread.start();
    }

    public void display(GLAutoDrawable drawable) {
        FloatBuffer color = FloatBuffer.allocate(4);
        color.put(0, (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 0.5 + 0.5));
        color.put(1, (float) (Math.cos(System.currentTimeMillis() / 1000.0) * 0.5 + 0.5));
        color.put(2, (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 0.5 + 0.5));//0.0f);
        color.put(3, 1.0f);

        FloatBuffer scale = FloatBuffer.allocate(1);
        float s = 0.0f;
        if (autozoom) s = (float) Math.abs((Math.cos(System.currentTimeMillis() / 800.0) * 1.0f) + size);
        else s = size;
        if (s > 1.9) s = 1.9f;
        else if (s < 0.1) s = 0.1f;
        scale.put(0, s);

        FloatBuffer attrib = FloatBuffer.allocate(4);
        attrib.put(0, (float) (Math.sin(System.currentTimeMillis() / 1000.0) * 0.9f));
        attrib.put(1, upDown);//(float) (Math.cos(System.currentTimeMillis() / 300.0) * 0.6f));
        attrib.put(2, 0.0f);
        attrib.put(3, 0.0f);


        if (g < 2) {
            g = g + rand.nextFloat() / 10;
            if (g > 1) g = 0;
        }
        FloatBuffer gradient = FloatBuffer.allocate(1);
        gradient.put(0, g);


        FloatBuffer noise = FloatBuffer.allocate(1);
        noise.put(0, n);


        if (sp > 1.5 || !(sp < 0.9)) sp = sp - rand.nextFloat() / 10;
        else if (sp < 0.9 || !(sp > 1.5)) sp = sp + rand.nextFloat() / 10;
        FloatBuffer spikescale = FloatBuffer.allocate(1);
        spikescale.put(0, sp);

        GL4 gl = (GL4) drawable.getGL();
        gl.glClearBufferfv(GL_COLOR, 0, color);
        gl.glVertexAttrib4fv(4, spikescale);
        gl.glVertexAttrib4fv(3, noise);
        gl.glVertexAttrib4fv(2, gradient);
        gl.glVertexAttrib4fv(1, scale);
        gl.glVertexAttrib4fv(0, attrib);
        gl.glUseProgram(rendering_program);
        gl.glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    private int createShaderPrograms(GLAutoDrawable drawable) {
        GL4 gl = (GL4) drawable.getGL();

        String vshaderSource[] = GLSLUtils.readShaderSource("vert.shader");
        String fshaderSource[] = GLSLUtils.readShaderSource("frag.shader");
        int lengths[];

        int vShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
        int fShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);

        lengths = new int[vshaderSource.length];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = vshaderSource[i].length();
        }
        gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, lengths, 0);

        lengths = new int[fshaderSource.length];
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = fshaderSource[i].length();
        }
        gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, lengths, 0);

        gl.glCompileShader(vShader);
        gl.glCompileShader(fShader);

        int vfprogram = gl.glCreateProgram();
        gl.glAttachShader(vfprogram, vShader);
        gl.glAttachShader(vfprogram, fShader);
        gl.glLinkProgram(vfprogram);
        return vfprogram;
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void actionPerformed(ActionEvent e) {
        if ("up".equals(e.getActionCommand()) && upDown < 1) {
            upDown += 0.1;
        } else if ("down".equals(e.getActionCommand()) && upDown > -1) {
            upDown -= 0.1;
        } else if ("color".equals(e.getActionCommand())) {
            if (g != 2) g = 2;
            else g = 1;
        } else if ("noise".equals(e.getActionCommand())) {
            if (n != 0.0) n = 0.0f;
            else n = 1.0f;
        } else if ("autozoom".equals(e.getActionCommand())) {
            autozoom = !autozoom;
        }
    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() < 1 && size < 1.9f) {
            System.out.println("wheels up size is" + size);
            size += 0.1f;
        } else if (size > 0.1f) {
            System.out.println("wheels down size is " + size);
            size -= 0.1f;
        }
    }


}
