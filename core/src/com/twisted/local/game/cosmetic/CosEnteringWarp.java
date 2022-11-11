package com.twisted.local.game.cosmetic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.twisted.local.game.state.ClientGameState;
import com.twisted.logic.descriptors.Grid;
import com.twisted.logic.entities.ship.Ship;

public class CosEnteringWarp extends Cosmetic{

    //required parameters
    private final float gridRad;
    private final Vector2 originGridLoc, warpDir, shipStartPos;
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
     */
    public CosEnteringWarp(int gridId, float gridRad, Vector2 originGridLoc, Ship ship,
                           Vector2 warpDir, Color color) {
        super(gridId);

        //copy
        this.ship = ship;
        this.originGridLoc = originGridLoc;
        this.gridRad = gridRad;
        this.warpDir = warpDir.cpy();
        this.color = color;

        //prep
        this.shipStartPos = new Vector2(ship.pos);
        this.pos = new Vector2(ship.pos);
        this.lastShipWarpPos = new Vector2();
        this.offsetTime = 0;

        //prepare graphics
        poly = new Polygon(ship.entityModel().getVertices());
        poly.scale(LTR);
        poly.translate(ship.pos.x*LTR, ship.pos.y*LTR);
        poly.rotate((float) (ship.rot*180/Math.PI)-90 );
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

        float t = originGridLoc.dst(ship.warpPos)/ship.model.warpSpeed + offsetTime;
        pos.set(warpDir).nor().scl(ship.model.warpSpeed * 0.05f * t*t*t).add(shipStartPos);
        poly.setPosition(pos.x*LTR, pos.y*LTR);

        return (pos.x*pos.x + pos.y*pos.y) < gridRad*gridRad;
    }
    @Override
    public void draw(ShapeRenderer shape, Grid g) {
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);
        shape.polygon(poly.getTransformedVertices());
        shape.end();
    }


    /* State Methods */

    public boolean showsThroughFog(ClientGameState state){
        return ship.owner == state.myId;
    }
}
