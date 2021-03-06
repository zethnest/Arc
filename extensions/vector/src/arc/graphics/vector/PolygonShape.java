package arc.graphics.vector;

import arc.math.geom.*;
import arc.struct.*;

public class PolygonShape extends Shape{
    private final FloatSeq points = new FloatSeq();

    public PolygonShape(FloatSeq points){
        this.points.addAll(points);
    }

    public PolygonShape(Vec2... points){
        for(Vec2 point : points){
            this.points.add(point.x);
            this.points.add(point.y);
        }
    }

    public PolygonShape(float... points){
        this.points.addAll(points);
    }

    public void addPoint(Vec2 point){
        this.points.add(point.x);
        this.points.add(point.y);
        markDataChanged();
    }

    public void addPoint(float x, float y){
        this.points.add(x);
        this.points.add(y);
        markDataChanged();
    }

    public void addPoints(FloatSeq points){
        this.points.addAll(points);
        markDataChanged();
    }

    public void addPoints(Vec2... points){
        for(Vec2 point : points){
            this.points.add(point.x);
            this.points.add(point.y);
        }
        markDataChanged();
    }

    public void addPoints(float... points){
        this.points.addAll(points);
        markDataChanged();
    }

    public void setPoint(int index, Vec2 point){
        setPoint(index, point.x, point.y);
    }

    public void setPoint(int index, float x, float y){
        int coordinatesIndex = index * 2;
        float oldX = points.get(coordinatesIndex);
        float oldY = points.get(coordinatesIndex + 1);
        boolean changed = false;

        if(oldX != x){
            points.set(coordinatesIndex, x);
            changed = true;
        }

        if(oldY != y){
            points.set(coordinatesIndex + 1, y);
            changed = true;
        }

        if(changed){
            markDataChanged();
        }
    }

    public float getX(int index){
        return points.get(index * 2);
    }

    public float getY(int index){
        return points.get(index * 2 + 1);
    }

    public Vec2 getPoint(int index, Vec2 out){
        int coordinatesIndex = index * 2;
        out.x = points.get(coordinatesIndex);
        out.y = points.get(coordinatesIndex + 1);
        return out;
    }

    public int getSize(){
        return points.size / 2;
    }

    @Override
    protected void initPath(Path path){
        if(points.size < 4){
            return;
        }

        int i = 0;
        path.moveTo(points.get(i++), points.get(i++));
        while(i < points.size){
            path.lineTo(points.get(i++), points.get(i++));
        }
        path.close();
    }

    @Override
    public void reset(){
        super.reset();
        points.clear();
    }
}
