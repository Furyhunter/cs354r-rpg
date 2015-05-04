package rpg.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import rpg.scene.RenderItem;

import java.util.List;

public class Spatial2DBoundingBoxRenderer extends Component implements Renderable {
    private static Mesh mesh;
    private static ShaderProgram shaderProgram;

    @Override
    public RenderItem render() {
        List<Spatial2D> spatials = getParent().findComponents(Spatial2D.class);
        Rectangle bound = spatials.stream().map(Spatial2D::getRectangle).reduce(new Rectangle(), Rectangle::merge);

        if (mesh == null) {
            mesh = new Mesh(true, 5, 0, VertexAttribute.Position());
            mesh.setVertices(new float[]{0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0,});
        }
        if (shaderProgram == null) {
            shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/solid-color.vsh"), Gdx.files.internal("shaders/solid-color.fsh"));
            if (!shaderProgram.isCompiled()) {
                Log.error("Shader", "Shader compile error:\n" + shaderProgram.getLog());
            }
        }

        Matrix4 matrix = new Matrix4();
        Vector3 worldPos = getParent().getTransform().getWorldPosition();
        Vector3 worldScale = getParent().getTransform().getWorldScale();
        matrix.translate(worldPos.x, worldPos.y, worldPos.z);
        matrix.scale(worldScale.x, worldScale.y, worldScale.z);

        matrix.translate(bound.x, bound.y, 0);
        matrix.scale(bound.width, bound.height, 1);

        RenderItem r = new RenderItem();
        r.setShader(shaderProgram);
        r.setMesh(mesh);
        r.setPrimitiveType(GL20.GL_LINE_STRIP);
        r.setModelMatrix(matrix);
        r.setAbsoluteModelPosition(true);
        r.setUniformSetFunction(Spatial2DBoundingBoxRenderer::setShaderUniforms);

        return r;
    }

    private static void setShaderUniforms(ShaderProgram p) {
        p.setUniformf(p.getUniformLocation("u_color"), Color.WHITE);
    }
}
