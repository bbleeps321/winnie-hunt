/**
 * Copyright (c) 2012, Carey Zhang.
 */
package com.czhang.winniehunt;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

/**
 * A Carey will bounce around the screen, with a random initial velocity.
 *
 * @author czhang
 */
public class Carey extends OverlapableShape {
  public static final int SIZE_DP = 50;
  private static final int COLOR = Color.RED;
  private static final int MAX_VELOCITY = 5;
  private static final int MIN_VELOCITY = 1;

  /** Canvas dimensions. **/
  private final int canvasWidth, canvasHeight;

  /** Velocity of movement, in dp per draw cycle. **/
  private int dx, dy;

  /**
   * Creates a Carey at the specified x,y (top left coordinate). Has a random velocity.
   */
  public Carey(int x, int y, int canvasWidth, int canvasHeight, Bitmap bitmap) {
    super(new Rect(x, y, x + SIZE_DP, y + SIZE_DP), bitmap);
    this.canvasWidth = canvasWidth;
    this.canvasHeight = canvasHeight;

    dx = randomVelocity();
    dy = randomVelocity();
  }

  /**
   * Steps the motion of the Carey, reversing direction if out of bounds.
   */
  public void step() {
    // Reverse x velocity if out of bounds.
    if (getLeft() < 0) {
      dx = Math.abs(dx);
    } else if (getRight() > canvasWidth) {
      dx = -Math.abs(dx);
    }

    // Reverse y velocity if out of bounds.
    if (getTop() < 0) {
      dy = Math.abs(dy);
    } else if (getBottom() > canvasHeight) {
      dy = -Math.abs(dy);
    }

    geometry.offset(dx, dy);
  }

  private int randomVelocity() {
    // Uniform distribution of speed.
    int dv = (int) (Math.random() * (MAX_VELOCITY - MIN_VELOCITY)) + MIN_VELOCITY;

    // 50% chance of opposite direction;
    return (Math.random() < 0.5) ? dv : -dv;
  }

//  @Override
//  public void draw(Canvas canvas) {
//    canvas.drawRect(geometry, paint);
//  }
}
