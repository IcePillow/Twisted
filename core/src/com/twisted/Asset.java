package com.twisted;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.twisted.logic.entities.Entity;

import java.util.HashMap;

/**
 * Handles asset access. Asset access through this class should be static.
 */
public class Asset {

    /* Storage */

    //main
    private static final HashMap<TextureAsset, TextureRegionDrawable> textureAssets = new HashMap<>();
    private static final HashMap<FontAsset, BitmapFont> fontAssets = new HashMap<>();

    //entities
    private static final HashMap<Entity.Subtype, TextureRegionDrawable> entityIconAssets = new HashMap<>();
    private static final HashMap<Entity.Subtype, String> entityBlurbAssets = new HashMap<>();


    /* Accessing */

    //normal retrieving
    public static TextureRegionDrawable retrieve(TextureAsset key){
        TextureRegionDrawable asset = textureAssets.get(key);
        if(asset == null){
            asset = new TextureRegionDrawable(new Texture(Gdx.files.internal(key.getPath())));
            textureAssets.put(key, asset);
        }
        return asset;
    }
    public static BitmapFont retrieve(FontAsset key){
        BitmapFont asset = fontAssets.get(key);
        if(asset == null){
            asset = new BitmapFont(Gdx.files.internal(key.getPath()));
            fontAssets.put(key, asset);
        }
        return asset;
    }

    //retrieving assets for modification
    public static TextureRegionDrawable retrieveBlank(TextureAsset key){
        return new TextureRegionDrawable(new Texture(Gdx.files.internal(key.getPath())));
    }

    //retrieving entity specific
    public static TextureRegionDrawable retrieveEntityIcon(Entity.Subtype subtype){
        TextureRegionDrawable asset = entityIconAssets.get(subtype);
        if(asset == null){
            asset = new TextureRegionDrawable(new Texture(Gdx.files.internal("images/entities/"
                    + subtype.getFilename() + "-icon.png")));
            entityIconAssets.put(subtype, asset);
        }
        return asset;
    }
    public static String retrieveEntityBlurb(Entity.Subtype subtype){
        String asset = entityBlurbAssets.get(subtype);
        if(asset == null){
            asset = Gdx.files.internal("text/blurbs/" + subtype.getFilename() + ".txt").readString();
            entityBlurbAssets.put(subtype, asset);
        }
        return asset;
    }


    /* Storage Management */

    public static void clear(){
        textureAssets.clear();
    }


    /* Utility */

    public static Label.LabelStyle labelStyle(Avenir font){
        return new Label.LabelStyle(Asset.retrieve(font), Color.WHITE);
    }


    /* Asset Enums */

    public interface BaseAsset {
        String getPath();
    }

    public interface TextureAsset extends BaseAsset {}
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
        //TODO break this into two distinct enums: circles and pixels
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
        PIXEL_SPACE("pixels/space"),
        PIXEL_WHITE("pixels/white");

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
        ARROW_1("arrow-1"),
        ARROW_2("arrow-2"),
        ARROW_3("arrow-3"),
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

    public interface FontAsset extends BaseAsset{}
    public enum Avenir implements FontAsset {
        LIGHT_12("light_12"),
        LIGHT_16("light_16"),

        MEDIUM_12("medium_12"),
        MEDIUM_12_ITALIC("medium_12_italic"),
        MEDIUM_14("medium_14"),
        MEDIUM_16("medium_16"),

        HEAVY_16("heavy_16"),
        HEAVY_20("heavy_20"),

        BLACK_24("black_24"),
        BLACK_32("black_32"),
        BLACK_48("black_48");

        private final String path;
        public String getPath(){
            return path;
        }

        Avenir(String string){
            this.path = "fonts/avenir/" + string + ".fnt";
        }
    }


}
