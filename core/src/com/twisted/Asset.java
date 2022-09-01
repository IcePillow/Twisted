package com.twisted;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;

/**
 * Handles asset access. Asset access through this class should be static.
 */
public class Asset {

    /* Storage */

    private static final HashMap<TextureAsset, TextureRegionDrawable> textureAssets = new HashMap<>();


    /* Accessing */

    public static TextureRegionDrawable retrieve(TextureAsset key){
        TextureRegionDrawable tex = textureAssets.get(key);

        if(tex == null){
            tex = new TextureRegionDrawable(new Texture(Gdx.files.internal(key.getPath())));
            textureAssets.put(key, tex);
        }

        return tex;
    }

    public static TextureRegionDrawable temporaryRetrieve(TextureAsset key){
        TextureRegionDrawable tex = textureAssets.get(key);

        if(tex == null){
            tex = new TextureRegionDrawable(new Texture(Gdx.files.internal(key.getPath())));
        }

        return tex;
    }

    public static void clear(){
        textureAssets.clear();
    }


    /* Asset Enums */

    public interface TextureAsset {
        String getPath();
    }

    public enum EntityIcon implements TextureAsset {
        FRIGATE("frigate"),
        CRUISER("cruiser"),
        BATTLESHIP("battleship"),
        BARGE("barge"),

        EXTRACTOR("extractor"),
        HARVESTER("harvester"),
        LIQUIDATOR("liquidator"),

        STATION("station");

        private final String path;
        public String getPath(){
            return path;
        }

        EntityIcon(String string){
            this.path = "images/entities/" + string + "-icon.png";
        }
    }
    public enum Gem implements TextureAsset {
        CALCITE("calcite"),
        CRYSTAL("crystal"),
        KERNITE("kernite"),
        PYRENE("pyrene");

        private final String path;
        public String getPath(){
            return path;
        }

        Gem(String string){
            this.path = "images/gems/" + string + ".png";
        }

    }
    public enum Shape implements TextureAsset {
        CIRCLE_BLACK("circles/black"),
        CIRCLE_BLUE("circles/blue"),
        CIRCLE_GRAY("circles/gray"),
        CIRCLE_ORANGE("circles/orange"),
        PIXEL_BLACK("pixels/black"),
        PIXEL_BLUE("pixels/blue"),
        PIXEL_DARKGRAY("pixels/darkgray"),
        PIXEL_DARKPURPLE("pixels/darkpurple"),
        PIXEL_GRAY("pixels/gray"),
        PIXEL_GREEN("pixels/green"),
        PIXEL_LIGHTGRAY("pixels/lightgray"),
        PIXEL_MAGENTA("pixels/magenta"),
        PIXEL_NAVY("pixels/navy"),
        PIXEL_SPACE("pixels/space");

        private final String path;
        public String getPath(){
            return path;
        }

        Shape(String string){
            this.path = "images/" + string + ".png";
        }
    }
    public enum UiBasic implements TextureAsset {
        CURSOR_1("cursor1"),
        GRAY_ARROW_1("gray-arrow-1"),
        GRAY_ARROW_2("gray-arrow-2"),
        GRAY_ARROW_3("gray-arrow-3"),
        WHITE_ARROW("white-arrow"),
        WHITE_SQUARE_1("white-square-1");

        private final String path;
        public String getPath(){
            return path;
        }

        UiBasic(String string){
            this.path = "images/ui/" + string + ".png";
        }
    }
    public enum UiIcon implements TextureAsset {
        DOCK("dock"),
        UNDOCK("undock"),
        STATION_ARMORED("station-armored"),
        STATION_DEPLOYMENT("station-deployment"),
        STATION_SHIELDED("station-shielded"),
        STATION_VULNERABLE("station-vulnerable");

        private final String path;
        public String getPath(){
            return path;
        }

        UiIcon(String string){
            this.path = "images/ui/icons/" + string + ".png";
        }
    }
    public enum UiButton implements TextureAsset {
        ALIGN("align"),
        BLASTER_OFF("blaster-off"),
        BLASTER_ON("blaster-on"),
        DEFAULT("default"),
        DOCK("dock"),
        UNDOCK("undock"),
        EXTRACTOR_OFF("extractor-off"),
        EXTRACTOR_ON("extractor-on"),
        HARVESTER_ON("harvester-off"),
        HARVESTER_OFF("harvester-on"),
        LIQUIDATOR_OFF("liquidator-off"),
        LIQUIDATOR_ON("liquidator-on"),
        MOVE("move"),
        ORBIT("orbit"),
        STOP("stop"),
        TARGET_OFF("target-off"),
        TARGET_ON("target-on"),
        TRANSFER_LEFT("transfer-left"),
        TRANSFER_NONE("transfer-none"),
        TRANSFER_RIGHT("transfer-right"),
        WARP("warp");

        private final String path;
        public String getPath(){
            return path;
        }

        UiButton(String string){
            this.path = "images/ui/buttons/" + string + ".png";
        }
    }

}
