package a2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;
import graphicslib3D.Matrix3D;
import graphicslib3D.MatrixStack;
import graphicslib3D.Vertex3D;
import graphicslib3D.shape.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.util.Random;

import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;


public class Ass2 extends JFrame implements GLEventListener, ActionListener, MouseWheelListener, KeyListener {
    public static float zoom = 0.0f;
    private Dimension dimention = new Dimension(1000, 1000);
    private GLCanvas myCanvas;
    private MatrixStack mvStack = new MatrixStack(5);
    private int vao[] = new int[1];
    private int vbo[] = new int[3];
    private float cameraX, cameraY, cameraZ;
    private float cubeLocX, cubeLocY, cubeLocZ;
    private float skyBoxLocX, skyBoxLocY, skyBoxLocZ;
    private int rendering_program;
    private int VAO[] = new int[1];
    private GLSLUtils util = new GLSLUtils();
    private float upDown = 0.0f;
    private boolean animated = true;
    private Animator animator;
    private boolean autozoom = true;

    private Random rand;

    private Sphere mySphere = new Sphere(100);

    public Ass2() {
        int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap imap = this.getRootPane().getInputMap(mapName);
        KeyStroke upKey = KeyStroke.getKeyStroke("w");
        KeyStroke downKey = KeyStroke.getKeyStroke("s");
        ActionMap amap = this.getRootPane().getActionMap();
        imap.put(upKey, "zoomin");
        imap.put(downKey, "zoomout");
        zoomIn zoomin = new zoomIn();
        amap.put("zoomin", zoomin);
        zoomOut zoomout = new zoomOut();
        amap.put("zoomout", zoomout);

        setTitle("Assignment 2 CSC155");
        setSize(dimention);
        this.addMouseWheelListener(this);
        myCanvas = new GLCanvas();
        myCanvas.addGLEventListener(this);
        rand = new Random();
        add(myCanvas);
        setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }


    public void init(GLAutoDrawable drawable) {


        this.requestFocus();
        GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderPrograms(drawable);
        setupVertices(gl);
        cameraX = 0.0f;
        cameraY = 0.0f;
        cameraZ = 10.0f;
        cubeLocX = 0.0f;
        cubeLocY = -0.5f;
        cubeLocZ = 0.0f;
        skyBoxLocX = 0.0f;
        skyBoxLocY = 0.0f;
        skyBoxLocZ = 0.0f;
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
        GL4 gl = (GL4) drawable.getGL();

        gl.glClear(GL_DEPTH_BUFFER_BIT);
        FloatBuffer background = FloatBuffer.allocate(4);
        gl.glClearBufferfv(GL_COLOR, 0, background);

        gl.glUseProgram(rendering_program);

        int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
        int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");

        float aspect = myCanvas.getWidth() / myCanvas.getHeight();
        Matrix3D pMat = perspective(50.0f, aspect, 0.1f, 1000.0f);


        mvStack.pushMatrix();
        mvStack.translate(-cameraX, -cameraY, -cameraZ);
        mvStack.translate(0, 0, -zoom);
        double amt = (double) (System.currentTimeMillis() % 360000) / 1000.0;


        // ----------------------   == sun
        mvStack.pushMatrix();
        mvStack.translate(skyBoxLocX, skyBoxLocY, skyBoxLocZ);
        mvStack.scale(2, 2, 2);
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0,
                (System.currentTimeMillis() % 3600) / 10.0,
                (System.currentTimeMillis() % 3600) / 10.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();

        //-----------------------  cube == planet
        mvStack.pushMatrix();
        mvStack.translate(Math.sin(amt) * 10f, 0.0f, Math.cos(amt) * 10f);
        mvStack.pushMatrix();
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 1.0, 0.0);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();

        //-----------------------  smaller cube == moon
        mvStack.pushMatrix();
        mvStack.translate(0.0f, Math.sin(amt) * 2, Math.cos(amt) * 2);
        mvStack.rotate((System.currentTimeMillis() % 3600) / 10.0, 0.0, 0.0, 1.0);
        mvStack.scale(0.25, 0.25, 0.25);
        gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
        gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(0);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CW);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glVertexAttrib4fv(1, noise);
        gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
        mvStack.popMatrix();
        mvStack.popMatrix();
        mvStack.popMatrix();

    }


    private void setupVertices(GL4 gl) {

        Vertex3D[] vertices = mySphere.getVertices();
        int[] indices = mySphere.getIndices();

        float[] fvalues = new float[indices.length * 3];
        float[] tvalues = new float[indices.length * 2];
        float[] nvalues = new float[indices.length * 3];

        for (int i = 0; i < indices.length; i++) {
            fvalues[i * 3] = (float) (vertices[indices[i]]).getX();
            fvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getY();
            fvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getZ();
            tvalues[i * 2] = (float) (vertices[indices[i]]).getS();
            tvalues[i * 2 + 1] = (float) (vertices[indices[i]]).getT();
            nvalues[i * 3] = (float) (vertices[indices[i]]).getNormalX();
            nvalues[i * 3 + 1] = (float) (vertices[indices[i]]).getNormalY();
            nvalues[i * 3 + 2] = (float) (vertices[indices[i]]).getNormalZ();
        }

        gl.glGenVertexArrays(vao.length, vao, 0);
        gl.glBindVertexArray(vao[0]);
        gl.glGenBuffers(vbo.length, vbo, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);
        FloatBuffer vertBuf = FloatBuffer.wrap(fvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertBuf.limit() * 4, vertBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[1]);
        FloatBuffer texBuf = FloatBuffer.wrap(tvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, texBuf.limit() * 4, texBuf, GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[2]);
        FloatBuffer norBuf = FloatBuffer.wrap(nvalues);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, norBuf.limit() * 4, norBuf, GL.GL_STATIC_DRAW);
    }


    private Matrix3D perspective(float fovy, float aspect, float n, float f) {
        float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
        float A = q / aspect;
        float B = (n + f) / (n - f);
        float C = (2.0f * n * f) / (n - f);
        Matrix3D r = new Matrix3D();
        Matrix3D rt;// = new Matrix3D();
        r.setElementAt(0, 0, A);
        r.setElementAt(1, 1, q);
        r.setElementAt(2, 2, B);
        r.setElementAt(2, 3, -1.0f);
        r.setElementAt(3, 2, C);
        rt = r.transpose();
        return rt;
    }

    private int createShaderPrograms(GLAutoDrawable drawable) {
        int[] vertCompiled = new int[1];
        int[] fragCompiled = new int[1];
        int[] linked = new int[1];

        GL4 gl = (GL4) drawable.getGL();

        String vshaderSource[] = GLSLUtils.readShaderSource("a2shaders/vert.glsl");
        String fshaderSource[] = GLSLUtils.readShaderSource("a2shaders/frag.glsl");
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
        GLSLUtils.printOpenGLError(drawable);  // can use returned boolean
        gl.glGetShaderiv(vShader, GL4.GL_COMPILE_STATUS, vertCompiled, 0);
        if (vertCompiled[0] == 1) {
            System.out.println("vertex compilation success");
        } else {
            System.out.println("vertex compilation failed");
            GLSLUtils.printShaderInfoLog(drawable, vShader);
        }

        gl.glCompileShader(fShader);
        GLSLUtils.printOpenGLError(drawable);  // can use returned boolean
        gl.glGetShaderiv(fShader, GL4.GL_COMPILE_STATUS, vertCompiled, 0);
        if (vertCompiled[0] == 1) {
            System.out.println("fragment compilation success");
        } else {
            System.out.println("fragment compilation failed");
            GLSLUtils.printShaderInfoLog(drawable, fShader);
        }

        int vfprogram = gl.glCreateProgram();
        gl.glAttachShader(vfprogram, vShader);
        gl.glAttachShader(vfprogram, fShader);
        gl.glLinkProgram(vfprogram);
        GLSLUtils.printOpenGLError(drawable);
        gl.glGetProgramiv(vfprogram, GL4.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 1) {
            System.out.println("linking succeeded");
        } else {
            System.out.println("linking failed");
            GLSLUtils.printProgramInfoLog(drawable, vfprogram);
        }
        return vfprogram;
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void actionPerformed(ActionEvent e) {
//        if ("up".equals(e.getActionCommand()) && upDown < 1) {
//            upDown += 0.1;
//        } else if ("down".equals(e.getActionCommand()) && upDown > -1) {
//            upDown -= 0.1;
//        } else if ("color".equals(e.getActionCommand())) {
//            if (g != 2) g = 2;
//            else g = 1;
//        } else
//        if ("noise".equals(e.getActionCommand())) {
//            if (n != 0.0) n = 0.0f;
//            else n = 1.0f;
//        }
//        else if ("autozoom".equals(e.getActionCommand())) {
//            autozoom = !autozoom;
//        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() < 0) {
            System.out.println("wheels up size is" + zoom);
            zoom += 1f;
        } else {
            System.out.println("wheels down size is " + zoom);
            zoom -= 1f;
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class zoomIn extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.zoom -= 1.0f;
            //System.out.println("zoom + 1.0");
        }
    }

    private class zoomOut extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Ass2.zoom += 1.0f;
            //System.out.println("zoom - 1.0");
        }
    }
}
