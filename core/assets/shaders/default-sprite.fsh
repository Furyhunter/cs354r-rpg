#version 150

in vec2 m_texcoord;

out vec4 f_FragColor;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform sampler2D m_texture;

void main() {
    f_FragColor = texture(m_texture, m_texcoord);
    //f_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}