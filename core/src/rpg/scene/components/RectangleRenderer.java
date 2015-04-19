package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import rpg.scene.RenderItem;
import rpg.scene.replication.Replicated;

public class RectangleRenderer extends Component implements Renderable {
    @Replicated
    protected Vector2 size = new Vector2(1, 1);

    private static Mesh mesh;
    private static ShaderProgram shader;

    @Override
    public RenderItem render() {
        if (mesh == null) {
            mesh = new Mesh(true, 4, 0,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
            mesh.setVertices(new float[]{
                    -.5f, -.5f, 0,
                    .5f, -.5f, 0,
                    .5f, .5f, 0,
                    -.5f, .5f, 0,
            });
        }
        if (shader == null) {
            shader = new ShaderProgram(Gdx.files.internal("shaders/solid-white.vsh"), Gdx.files.internal("shaders/solid-white.fsh"));
        }
        Matrix4 model = new Matrix4().mulLeft(new Matrix4().setToScaling(size.x, size.y, 1));
        return new RenderItem(shader, null, mesh, model, GL20.GL_TRIANGLE_FAN);
    }
}
