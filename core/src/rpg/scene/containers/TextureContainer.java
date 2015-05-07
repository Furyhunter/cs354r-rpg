package rpg.scene.containers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import rpg.scene.systems.GdxAssetManagerSystem;

public class TextureContainer extends AssetContainer<Texture> {
    @Override
    protected void loadAsset() {
        AssetManager am = GdxAssetManagerSystem.getSingleton().getAssetManager();
        am.load(path, Texture.class);
        am.finishLoadingAsset(path);
        ref = am.get(path, Texture.class);
    }
}
