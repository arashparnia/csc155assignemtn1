#version 430
in float noise;
in vec4 vs_color;
out vec4 color;


float rand(vec2 n)
{
  return fract(sin(dot(n.xy, vec2(12.9898, 78.233)))* 43758.5453);
}

void main(void) {
		//float x = rand(vs_color);
    if (noise == 1){
	  color = vs_color *  rand(vs_color.xy);
  } else {
    color = vs_color;
  }
}
