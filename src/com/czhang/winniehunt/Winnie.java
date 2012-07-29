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
public class Winnie extends OverlapableShape {
  public static final int SIZE_DP = 50;

  /**
   * x,y specify top left.
   */
  public Winnie(int x, int y, Bitmap bitmap) {
    super(new Rect(x, y, x + SIZE_DP, y + SIZE_DP), bitmap);
  }

  /**
   * x,y specify center coordinates.
   */
  public void moveTo(int x, int y) {
    geometry = new Rect(x - SIZE_DP / 2, y - SIZE_DP / 2, x + SIZE_DP / 2, y + SIZE_DP / 2);
  }
}
