package rpg.scene.containers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import rpg.scene.systems.GdxAssetManagerSystem;

public class TextureAtlasContainer extends AssetContainer<TextureAtlas> {
    @Override
    protected void loadAsset() {
        AssetManager am = GdxAssetManagerSystem.getSingleton().getAssetManager();
        am.load(path, TextureAtlas.class);
        am.finishLoadingAsset(path);
        ref = am.get(path, TextureAtlas.class);
    }
}
