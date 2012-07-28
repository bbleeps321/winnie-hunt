/**
 * Copyright (c) 2012, Carey Zhang.
 */
package com.czhang.winniehunt;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Interface for shapes that can overlap each other.
 *
 * @author czhang
 */
public abstract class OverlapableShape {
  protected Rect geometry;
  protected final Paint paint;
  protected final Bitmap image;

  public OverlapableShape(Rect geometry, Bitmap bitmap) {
    this.geometry = geometry;
    image = bitmap;

    paint = new Paint();
    paint.setAntiAlias(true);
  }

  public Rect getGeometry() {
    return geometry;
  }

  public int getLeft() {
    return geometry.left;
  }

  public int getTop() {
    return geometry.top;
  }

  public int getRight() {
    return geometry.right;
  }

  public int getBottom() {
    return geometry.bottom;
  }

  public int getCenterX() {
    return geometry.centerX();
  }

  public int getCenterY() {
    return geometry.centerY();
  }

  public boolean overlaps(OverlapableShape that) {
    return this.geometry.intersect(that.getGeometry());
  }

  public void draw(Canvas canvas) {
    canvas.drawBitmap(image, null, geometry, paint);
  }
}
