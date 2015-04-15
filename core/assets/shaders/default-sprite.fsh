varying vec2 v_texCoord0;

uniform mat4 u_pvmMatrix;

uniform sampler2D m_texture;

void main() {
    gl_FragColor = texture2D(m_texture, v_texCoord0);
    //gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}