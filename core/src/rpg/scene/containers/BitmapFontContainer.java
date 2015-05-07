package rpg.scene.containers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import rpg.scene.systems.GdxAssetManagerSystem;

public class BitmapFontContainer extends AssetContainer<BitmapFont> {
    @Override
    protected void loadAsset() {
        AssetManager am = GdxAssetManagerSystem.getSingleton().getAssetManager();
        am.load(path, BitmapFont.class);
        am.finishLoadingAsset(path);
        ref = am.get(path, BitmapFont.class);
    }
}
