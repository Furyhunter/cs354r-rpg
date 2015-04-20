attribute vec3 a_position;

uniform mat4 u_pvmMatrix;

uniform vec4 u_color;

void main() {
    gl_Position = u_pvmMatrix * vec4(a_position, 1);
}
