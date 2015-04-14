package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import rpg.scene.RenderItem;

public class SpriteRenderer extends Component implements Renderable {

    private Texture texture;
    private static ShaderProgram shader = null;
    private static Mesh mesh = null;

    @Override
    public RenderItem render() {
        if (shader == null) {
            shader = new ShaderProgram(Gdx.files.internal("shaders/default-sprite.vsh"), Gdx.files.internal("shaders/default-sprite.fsh"));
            String log = shader.getLog();
            if (log != null && log.length() > 0) {
                System.err.println(log);
            }
        }
        if (mesh == null) {
            mesh = new Mesh(Mesh.VertexDataType.VertexBufferObject, true, 4, 6,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, "position"),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "texcoord"));
            mesh.setVertices(new float[]{
                    -.5f, -.5f, 0, 0, 0,
                    .5f, -5.f, 0, 1, 0,
                    .5f, .5f, 0, 1, 1,
                    -.5f, .5f, 0, 0, 1
            });
        }
        RenderItem ri = new RenderItem(shader, texture, mesh, null, GL20.GL_TRIANGLE_FAN);
        return ri;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
}
