uniform mat4 u_pvmMatrix;

uniform vec4 u_color;
uniform float u_time;

void main() {
    gl_FragColor = u_color * ((sin(u_time) + 1) / 2);
}
