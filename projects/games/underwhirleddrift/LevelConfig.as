package {

import flash.display.DisplayObject;
import flash.display.BitmapData;

import flash.geom.Point;
import flash.geom.Matrix;

import com.threerings.util.Line;
import com.threerings.util.HashMap;

public class LevelConfig 
{
    public static const OBJECT_STARTING_LINE_POINT :int = 101;
    public static const OBJECT_STARTING_POSITION   :int = 102;
    public static const OBJECT_JUMP_RAMP           :int = 103;
    // boost lines only point in the cardinal direction, and are defined by two points that
    // create a line perpendicular to the boost direction.  If these numbers are changed, they 
    // should remain in numerical progression, with EAST set to the lowest number.
    public static const OBJECT_BOOST_POINT_EAST    :int = 104;
    public static const OBJECT_BOOST_POINT_WEST    :int = 105;
    public static const OBJECT_BOOST_POINT_NORTH   :int = 106;
    public static const OBJECT_BOOST_POINT_SOUTH   :int = 107;

    public static const OBJECT_BONUS               :int = 201;
    public static const OBJECT_OBSTACLE            :int = 202;

    public function LevelConfig (objectsLayer :Class, map :HashMap) 
    {
        _startingPoints = new Array();
        _obstacles = new Array();
        _bonuses = new Array();
        var layerImg :DisplayObject = new objectsLayer() as DisplayObject;
        var layerBitmap :BitmapData = new BitmapData(layerImg.width, layerImg.height, true, 0);
        var trans :Matrix = new Matrix();
        trans.translate(layerImg.width / 2, layerImg.height / 2);
        layerBitmap.draw(layerImg, trans);
        var boostPoints :Array = [[], [], [], []];
        var log :Log = Log.getLog(UnderwhirledDrift);
        for (var h :int = 0; h < layerImg.height; h++) {
            for (var w :int = 0; w < layerImg.width; w++) {
                if ((layerBitmap.getPixel32(w, h) & 0xFF000000) != 0) {
                    var obj :Object = map.get(layerBitmap.getPixel(w, h));
                    var point :Point = new Point(w - layerImg.width / 2, h - layerImg.height / 2);
                    if (obj != null) {
                        switch (obj.type) {
                        case OBJECT_STARTING_LINE_POINT:
                            if (_startingLine == null) {
                                _startingLine = new Line(point, null);
                            } else {
                                _startingLine.stop = point;
                            }
                            break;
                        case OBJECT_BOOST_POINT_EAST:
                        case OBJECT_BOOST_POINT_WEST:
                        case OBJECT_BOOST_POINT_NORTH:
                        case OBJECT_BOOST_POINT_SOUTH:
                            boostPoints[obj.type - OBJECT_BOOST_POINT_EAST].push(point);
                            break;
                        case OBJECT_STARTING_POSITION:
                            _startingPoints.push(point);
                            break;
                        case OBJECT_OBSTACLE:
                            _obstacles.push({cls: obj.cls, point: point, type: Scenery.OBSTACLE});
                            break;
                        case OBJECT_BONUS:
                            _bonuses.push({cls: obj.cls, point: point, type: Scenery.BONUS});
                            break;
                        }
                    } else {
                        log.debug("unkown color found @ " + point + ": " + layerBitmap.getPixel(
                            w, h).toString(16));
                    }
                }
            }
        }
        findBoostLines(boostPoints);
        
        // sort the starting points based on distance from the starting line
        trans.identity();
        trans.translate(-_startingLine.start.x, -_startingLine.start.y);
        trans.rotate(-Math.atan2(Math.abs(_startingLine.start.y - _startingLine.stop.y),
            Math.abs(_startingLine.start.x - _startingLine.stop.x)));
        var translatedY :Number = trans.transformPoint(_startingLine.stop).y;
        _startingPoints.sort(function (obj1 :Object, obj2 :Object) :int {
            var dist1 :Number = Math.abs(translatedY - trans.transformPoint(obj1 as Point).y);
            var dist2 :Number = Math.abs(translatedY - trans.transformPoint(obj2 as Point).y);
            return dist1 < dist2 ? -1 : (dist2 < dist1 ? 1 : 0);
        });
    }

    public function getStartingLine () :Line
    {
        return _startingLine;
    }

    public function getStartingPoint (position :int) :Point
    {
        return _startingPoints[position];
    }

    public function getObstacles () :Array
    {
        return _obstacles;
    }

    public function getBonuses () :Array 
    {
        return _bonuses;
    }

    public function getBoosts () :Array
    { 
        return _boosts;
    }

    protected function findBoostLines (boostPoints :Array) :void
    {
        _boosts = new Array();
        for (var ii: int = 0; ii < 4; ii++) {
            var thesePoints :Array = boostPoints[ii];
            var type :int = ii + OBJECT_BOOST_POINT_EAST;
            while (thesePoints.length > 0) {
                var firstP :Point = thesePoints.pop();
                for (var next :int = 0; next < thesePoints.length; next++) {
                    if (type == OBJECT_BOOST_POINT_EAST || type == OBJECT_BOOST_POINT_WEST) {
                        // the next point encountered with a matching x is this one's mate, if the
                        // objects art is sane
                        if (thesePoints[next].x == firstP.x) {
                            _boosts.push({ line: new Line(firstP, thesePoints[next]), type: type });
                            thesePoints.splice(next, 1);
                            break;
                        }
                    } else {
                        // the next point encountered with a matching y is this one's mate, if the
                        // objects are is sane
                        if (thesePoints[next].y == firstP.y) {
                            _boosts.push({ line: new Line(firstP, thesePoints[next]), type: type });
                            thesePoints.splice(next, 1);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected var _startingLine :Line;
    protected var _startingPoints :Array;
    protected var _obstacles :Array;
    protected var _bonuses :Array;
    protected var _boosts :Array;
}
}
