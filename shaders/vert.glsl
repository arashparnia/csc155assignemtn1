#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec2 texPos;
//layout (location = 1) in float n;
//layout (location=1) in vec3 instancePosition;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform sampler2D s;

out vec4 vColor;
out vec2 tc;
//out float noise;
float rand(){
  return  0;//(fract(sin(dot(vec2(gl_InstanceID,gl_InstanceID), vec2(12.9898, 78.233)))* 43758.5453) ) ;
}
void main(void){
	//noise = n;
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0) ;//+ vec4(rand() * mod(20 ,rand())* 10 ,rand() * mod(gl_InstanceID, 13) * 10 ,1,1.0) ;
	vColor = vec4(position,1.0)*2.0 + vec4(0.5, 0.5, 0.5, 0.0);
	tc = texPos;
}