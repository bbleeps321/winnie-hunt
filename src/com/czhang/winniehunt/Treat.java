/**
 * Copyright (c) 2012, Carey Zhang.
 */
package com.czhang.winniehunt;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * @author czhang
 *
 */
public class Treat extends OverlapableShape {
  public static final int SIZE_DP = 50;

  private boolean eaten;

  /**
   * x and y specify the top left coordinates.
   */
  public Treat(int x, int y, Bitmap bitmap) {
    super(new Rect(x, y, x + SIZE_DP, y + SIZE_DP), bitmap);
    eaten = false;
  }

  public void setEaten(boolean eaten) {
    this.eaten = eaten;
  }

  public boolean isEaten() {
    return eaten;
  }
}
