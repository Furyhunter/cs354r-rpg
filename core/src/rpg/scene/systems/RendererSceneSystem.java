package rpg.scene.systems;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import rpg.scene.Node;
import rpg.scene.RenderItem;
import rpg.scene.components.Renderable;
import rpg.scene.components.Transform;

import java.util.*;
import java.util.stream.Collectors;

public class RendererSceneSystem extends AbstractSceneSystem {

    private Matrix4 projectionMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();

    private Deque<Matrix4> modelMatrixStack = new ArrayDeque<>();

    private List<RenderItem> renderItems;

    @Override
    public void beginProcessing() {
        renderItems = new ArrayList<>();
        modelMatrixStack.clear();
        modelMatrixStack.push(new Matrix4());
    }

    @Override
    public void enterNode(Node n, float deltaTime) {
        Transform t = n.findComponent(Transform.class);
        Matrix4 newModel = new Matrix4(modelMatrixStack.peek());
        t.applyTransform(newModel);
        modelMatrixStack.push(newModel);
    }

    @Override
    public void processNode(Node n, float deltaTime) {
        // Get all renderable components on this node.
        List<Renderable> renderables = n.findComponents(Renderable.class);


        // Map Renderable to RenderItem with render() method, to get a RenderItem list
        List<RenderItem> items = renderables.stream().map(r -> {
            RenderItem i = r.render();
            i.setModelMatrix(modelMatrixStack.peek());
            return i;
        }).collect(Collectors.toList());

        // Append the collecting list of render items.
        renderItems.addAll(items);
    }

    @Override
    public void exitNode(Node n, float deltaTime) {
        modelMatrixStack.pop();
    }

    @Override
    public void endProcessing() {
        // Sort the render item list by GL texture handle (to minimize texture rebinds)
        renderItems.sort(Comparator.comparingInt(r -> r.getTextureToBind().getTextureObjectHandle()));

        // Draw the draw list
        Texture currentlyBoundTexture = null;
        for (RenderItem r : renderItems) {
            if (r.getTextureToBind() != currentlyBoundTexture) {
                r.getTextureToBind().bind(0);
                currentlyBoundTexture = r.getTextureToBind();
            }
            r.getShader().setUniformMatrix4fv("projectionMatrix", r.getModelMatrix().getValues(), 0, 16);
            r.getShader().setUniformMatrix4fv("viewMatrix", r.getModelMatrix().getValues(), 0, 16);
            r.getShader().setUniformMatrix4fv("modelMatrix", r.getModelMatrix().getValues(), 0, 16);
            r.getMesh().render(r.getShader(), r.getPrimitiveType());
        }

        renderItems.clear();
    }
}
