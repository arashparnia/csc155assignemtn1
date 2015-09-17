package a1;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import graphicslib3D.GLSLUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL.GL_TRIANGLES;

public class Code extends JFrame implements GLEventListener {
    private Dimension dimention = new Dimension(1000,1000);
    private GLCanvas myCanvas;
    private int rendering_program;
    private int VAO[] = new int[1];
    private GLSLUtils util = new GLSLUtils();

    public Code()
    {	setTitle("Assigment 1 CSC155");
        setSize(dimention);
        myCanvas = new GLCanvas();
       // FPSAnimator animtr = new FPSAnimator(myCanvas.get);

        myCanvas.addGLEventListener(this);
        getContentPane().add(myCanvas);
        setVisible(true);
    }

    public void display(GLAutoDrawable drawable)
    {
        GL4 gl = (GL4) drawable.getGL();
        FloatBuffer color = FloatBuffer.allocate(4);
        color.put(0,(float) (Math.sin(System.currentTimeMillis()/300.0)*0.5 + 0.5));
        color.put(1,(float) (Math.cos(System.currentTimeMillis()/300.0)*0.5 + 0.5));
        color.put(2,0.0f);
        color.put(3,1.0f);
        gl.glClearBufferfv(gl.GL_COLOR,0,color);
        gl.glUseProgram(rendering_program);
        FloatBuffer attrib = FloatBuffer.allocate(4);
        attrib.put(0, (float)(Math.sin(System.currentTimeMillis()/300.0) * 0.5f));
        attrib.put(1, (float)(Math.cos(System.currentTimeMillis()/300.0) * 0.6f));
        attrib.put(2,0.0f);
        attrib.put(3,0.0f);
        //gl.glVertexAttrib4fv(0, attrib);
        gl.glDrawArrays(GL_TRIANGLES,0,3);
        }

    public void init(GLAutoDrawable drawable)
    {	GL4 gl = (GL4) drawable.getGL();
        rendering_program = createShaderPrograms(drawable);
        gl.glGenVertexArrays(VAO.length, VAO, 0);
        gl.glBindVertexArray(VAO[0]);
    }

    private int createShaderPrograms(GLAutoDrawable drawable)
    {	GL4 gl = (GL4) drawable.getGL();
        String currentDirectory;
        currentDirectory = System.getProperty("user.dir");
        System.out.println("Current working directory : "+currentDirectory);
        String vshaderSource[] = util.readShaderSource(currentDirectory+ "/shaders/vert.shader");
        String fshaderSource[] = util.readShaderSource(currentDirectory+ "/shaders/frag.shader");
        int lengths[];

        int vShader = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
        int fShader = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);

        lengths = new int[vshaderSource.length];
        for (int i = 0; i < lengths.length; i++)
        {	lengths[i] = vshaderSource[i].length();
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


    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void dispose(GLAutoDrawable drawable) {}
}