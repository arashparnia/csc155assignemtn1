#version 430
layout (location = 0) in vec4 offset;
layout (location = 1) in float scale;
layout (location = 2) in float gradient;
layout (location = 3) in float n;
layout (location = 4) in float spike;
out vec4 vs_color;
out float noise;
float rand()
{
  return fract(sin(dot(vec2(spike,spike), vec2(12.9898, 78.233)))* 43758.5453);
}

void main(void) {
    const vec4 vertices[36] = vec4[36](
      //outerstar
      vec4( (0.5 * scale) * spike, (0.5 * scale) * spike , 0.5, 1.0) ,
      vec4( 0.1 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , 0.1 * scale, 0.5, 1.0) ,

      vec4( 0.5 * scale * spike , -0.5 * scale * spike , 0.5, 1.0) ,
      vec4( 0 * scale , -0.1 * scale, 0.5, 1.0) ,
      vec4( 0.1 * scale , 0 * scale, 0.5, 1.0) ,

      vec4( -0.5 * scale * spike, -0.5 * scale * spike , 0.5, 1.0) ,
      vec4( -0.1 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , -0.1 * scale, 0.5, 1.0) ,

      vec4( -0.5 * scale * spike, 0.5 * scale * spike, 0.5, 1.0) ,
      vec4( 0 * scale , 0.1 * scale, 0.5, 1.0) ,
      vec4( -0.1 * scale , 0 * scale, 0.5, 1.0) ,
      //crossstar
      vec4( 0 * scale * spike, 0.5 * scale * spike, 0.5, 1.0) ,
      vec4( 0.1 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( -0.1 * scale , 0 * scale, 0.5, 1.0) ,

      vec4( 0 * scale * spike, -0.5 * scale * spike, 0.5, 1.0) ,
      vec4( -0.1 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( 0.1 * scale , 0 * scale, 0.5, 1.0) ,

      vec4( 0.5 * scale * spike, 0 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , -0.1 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , 0.1 * scale, 0.5, 1.0) ,

      vec4( -0.5 * scale * spike , 0 * scale * spike, 0.5, 1.0) ,
      vec4( 0 * scale , 0.1 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , -0.1 * scale, 0.5, 1.0),
      //innerstar
      vec4( 0.25 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , -0.05 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , 0.05 * scale, 0.5, 1.0) ,

      vec4( -0.25 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , 0.05 * scale, 0.5, 1.0) ,
      vec4( 0 * scale , -0.05 * scale, 0.5, 1.0),

      vec4( 0 * scale , 0.25 * scale, 0.5, 1.0) ,
      vec4( 0.05 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( -0.05 * scale , 0 * scale, 0.5, 1.0) ,

      vec4( 0 * scale , -0.25 * scale, 0.5, 1.0) ,
      vec4( -0.05 * scale , 0 * scale, 0.5, 1.0) ,
      vec4( 0.05 * scale , 0 * scale, 0.5, 1.0) );

if (gradient != 2){
    const vec4 colors[36] = vec4[36](
        //outerstar
        vec4( 1.0, 0.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),

        vec4( 1.0, 0.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),

        vec4( 1.0, 0.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),

        vec4( 1.0, 0.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),
        vec4( 0.0, 1.0*gradient, 1.0, 1.0),
        //crossstar
        vec4( 1.0*gradient, 0.0, 1.0, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),

        vec4( 1.0*gradient, 0.0, 1.0, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),

        vec4( 1.0*gradient, 0.0, 1.0, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),

        vec4( 1.0*gradient, 0.0, 1.0, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),
        vec4( 0.0, 1.0, 1.0*gradient, 1.0),
        //inerstar
        vec4( 0.5, 1.0*gradient, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),

        vec4( 0.5, 1.0*gradient, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),

        vec4( 0.5, 1.0*gradient, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),

        vec4( 0.5, 1.0*gradient, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0),
        vec4( 1.0*gradient, 0.5, 0.0, 1.0));

        vs_color = colors[gl_VertexID];
} else if (gradient == 2){
         vs_color = vec4( 1.0, 0.0, 0.0, 1.0);
}
    noise = n;
    gl_Position = vertices[gl_VertexID] + offset;

}
