#version 430

in vec3 position;
in vec4 vColor;
in float noise;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

float rand(vec2 n)
{
  return fract(sin(dot(n.xy, vec2(12.9898, 78.233)))* 43758.5453);
}

void main(void)
{
    //color = vColor;
	//float x = rand(vs_color);
    if (noise == 1){
	  color = vColor *  rand(vColor.xy);
  } else {
    color = vColor;
  }
}
