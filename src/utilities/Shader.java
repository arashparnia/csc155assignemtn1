package utilities;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import graphicslib3D.GLSLUtils;

/**
 * Created by arash on 10/29/2015.
 */
public class Shader {
    public int createShaderPrograms(GLAutoDrawable drawable,String vert,String frag)
    {
        int[] vertCompiled = new int[1];
        int[] fragCompiled = new int[1];
        int[] linked = new int[1];

        GL4 gl = (GL4) drawable.getGL();

        String vshaderSource[] = GLSLUtils.readShaderSource(vert);
        String fshaderSource[] = GLSLUtils.readShaderSource(frag);
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
        gl.glGetShaderiv(fShader, GL4.GL_COMPILE_STATUS, fragCompiled, 0);
        if (fragCompiled[0] == 1) {
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
}
