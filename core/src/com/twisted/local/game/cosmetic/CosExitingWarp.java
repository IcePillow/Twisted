package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;

public class CosExitingWarp extends Cosmetic{

    //required parameters
    private final float gridRad;
    private final Vector2 destGridLoc, warpDir;
    private final Ship ship;
    private final Color color;

    //drawing
    private final Polygon poly;

    //state
    private final Vector2 pos;
    private final Vector2 lastShipWarpPos;
    private float offsetTime;


    /**
     * Constructor
     * @param warpDir Vector pointing from origin grid to destination grid. Normalized.
     */
    public CosExitingWarp(int gridId, float gridRad, Vector2 destGridLoc, Ship ship, Vector2 warpDir,
                          Color color) {
        super(gridId);

        this.gridRad = gridRad;
        this.destGridLoc = destGridLoc;
        this.ship = ship;
        this.warpDir = warpDir.cpy().scl(-1);
        this.color = color;

        //prep
        this.pos = new Vector2();
        this.lastShipWarpPos = new Vector2();
        this.offsetTime = 0;

        //prepare graphics
        poly = new Polygon(ship.entityModel().getVertices());
        poly.scale(LTR);
        poly.setPosition(pos.x*LTR, pos.y*LTR);
        poly.rotate((float) (ship.rot*180/Math.PI)-90);
    }

    /* Action Methods */

    @Override
    public boolean tick(float delta) {
        //check if there has been an update to the ship's warp position
        if(!(lastShipWarpPos.dst(ship.warpPos) == 0)){
            lastShipWarpPos.set(ship.warpPos);
            offsetTime = 0;
        }
        else {
            offsetTime += delta;
        }

        float ttl = ship.warpPos.dst(destGridLoc)/ship.model.warpSpeed - offsetTime; //time to land
        pos.set(warpDir).nor().scl(ship.model.warpSpeed * 0.05f * ttl*ttl*ttl).add(ship.warpLandPos);
        poly.setPosition(pos.x*LTR, pos.y*LTR);

        return ship.grid == -1 && ship.warpCharge < 0.8f;
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        //check if it would be drawn on grid
        if(poly.getX()*poly.getX() + poly.getY()*poly.getY() < gridRad*gridRad * LTR*LTR){
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(color);
            shape.polygon(poly.getTransformedVertices());
            shape.end();
        }
    }


    /* State Methods */

    public boolean showsThroughFog(ClientGameState state){
        return ship.owner == state.myId;
    }
}
