varying vec2 v_texCoord0;

uniform mat4 u_pvmMatrix;

uniform sampler2D u_texture1;

uniform mat3 u_texCoord0Transform;

void main() {
    gl_FragColor = texture2D(u_texture1, v_texCoord0);
    //gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
