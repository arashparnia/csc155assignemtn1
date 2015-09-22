Arash Parnia
CSC155 assignment 1
Dr. Scott Gordon

This assignment demonstrates use of JOGL to program OpenGL shaders. The java program resides in a1 package.
Program can be compiled using compile.bat file in the root directory of the project and can run using run.bat.

this project includes following files:
Starter.java    this class instantiates the main assignment class
Ass1.java       this class contains all the shader initialization code as well as all the code to build and control the interface
Animation.java  this class is used to create methods that help animating the display
vert.Shader     this shader file defines vertices and their color and accepts buffers to customize colors and vertices
frag.Shader     this shader file gets inputs from vert.Shader and pass them or uses input flags to customize them

program function explained:
    assignment 1 contains 36 vertices creating 12 triangles.
    triangles are places to form a star.
    triangles are in order at 0,45,90,135,180,225,270,315 degrees and 2 inner triangles at 0,90,180,270 degrees.
    An offset shifts all vertices by a value set within the program.
    The horizontal offset is set by a sin function.
    The vertical offset can be incremented or decremented using button on the top of the window.
    Each vertices multiples by the value scale that will result in scaling the triangles.
    Scale can be set by mouse wheel movement or by autoscale button that is another sin function.
    Tip vertices of major triangles have another scale called spike. spike randomly scales the vertices to create flickering effect.
    A random number causes colors to change in every frame.
    There is a random generator in frag shader that uses input positions to create noise within the pixels.
    All features can be turned on and off using the button on the left top corner of the GL window.

screen shot can be found on the root folder of a1

