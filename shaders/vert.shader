#version 430 core
// "vs_color" is an output that will be sent to the next shader stage
layout (location = 0) in vec4 offset;
out vec4 vs_color;
void main(void) {
    const vec4 vertices[3] = vec4[3](
        vec4( 0.25, -0.25, 0.5, 1.0),
        vec4(-0.25, -0.25, 0.5, 1.0),
        vec4( 0.25, 0.25, 0.5, 1.0)
    );
     const vec4 colors[3] = vec4[3](
        vec4( 1.0, 0.0, 0.0, 1.0),
        vec4( 0.0, 1.0, 0.0, 1.0),
        vec4( 0.0, 0.0, 1.0, 1.0)
    );
    // Add "offset" to our hard-coded vertex position
    gl_Position = vertices[gl_VertexID] + offset;
    // Output a fixed value for vs_color
    vs_color = color[gl_VertexID];
}
