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

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

public class Ass1 extends JFrame implements GLEventListener, ActionListener, MouseWheelListener {
    private Dimension dimention = new Dimension(1000, 1000);
    private GLCanvas myCanvas;
    private int rendering_program;
    private int VAO[] = new int[1];
    private GLSLUtils util = new GLSLUtils();
    private float upDown = 0.0f;
    private float size = 1.0f;


    public Ass1() {
        setTitle("Assignment 1 CSC155");
        setSize(dimention);
        this.addMouseWheelListener(this);
        //this.addMouseListener(this);
        //setLayout(null);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        //getContentPane().add(myCanvas);

        //JPanel buttonPanel = new JPanel();
        //buttonPanel.setBounds(0, 0, dimention.width, 50);
        //buttonPanel.setLayout(null);
        //JPanel glPanel = new JPanel();
        //glPanel.setBounds(0, buttonPanel.getHeight(), dimention.width, dimention.height-buttonPanel.getHeight());

        //add(buttonPanel);

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
        Animator animator = new Animator(myCanvas);
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
        color.put(0, (float) (Math.sin(System.currentTimeMillis() / 300.0) * 0.5 + 0.5));
        color.put(1, (float) (Math.cos(System.currentTimeMillis() / 300.0) * 0.5 + 0.5));
        color.put(2, 0.0f);
        color.put(3, 1.0f);

        FloatBuffer scale = FloatBuffer.allocate(1);
        scale.put(0, size);//(float) Math.abs((Math.cos(System.currentTimeMillis() / 800.0) * 1.0f) + size));

        FloatBuffer attrib = FloatBuffer.allocate(4);
        attrib.put(0, (float) (Math.sin(System.currentTimeMillis() / 400.0) * 0.9f));
        attrib.put(1, upDown);//(float) (Math.cos(System.currentTimeMillis() / 300.0) * 0.6f));
        attrib.put(2, 0.0f);
        attrib.put(3, 0.0f);


        GL4 gl = (GL4) drawable.getGL();
        gl.glClearBufferfv(GL_COLOR, 0, color);
        gl.glVertexAttrib4fv(1, scale);
        gl.glVertexAttrib4fv(0, attrib);
        gl.glUseProgram(rendering_program);
        gl.glDrawArrays(GL_TRIANGLES, 0, 3);

    }


    //        String currentpath = System.getProperty("user.dir");
//        System.out.println("current path is " + currentpath);
//        String vshaderSource[] = util.readShaderSource(currentpath+ "/" +"vert.shader");
//        System.out.println("Loading shaders/vert.shader");
//        String fshaderSource[] = util.readShaderSource(currentpath+ "/" +"frag.shader");
//        System.out.println("Loading shaders/frag.shader");
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