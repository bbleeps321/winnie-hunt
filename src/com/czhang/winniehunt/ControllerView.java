/**
 * Copyright (c) 2012, Carey Zhang.
 */
package com.czhang.winniehunt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * The view that handles the drawing and also the interaction between
 * main thread and controller thread.
 *
 * @author czhang
 */
public class ControllerView extends SurfaceView implements SurfaceHolder.Callback {
  private final ControllerThread thread;

  public ControllerView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Register surface holder callback.
    SurfaceHolder holder = getHolder();
    holder.addCallback(this);

    // Create thread. Started in surfaceCreated().
    thread = new ControllerThread(holder, context);
    setFocusable(true);;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
    thread.setSurfaceSize(width, height);
    thread.setupLayout();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    thread.setRunning(true);
    thread.start();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    boolean retry = true;
    thread.setRunning(false);
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Standard window-focus override. Notice focus lost so we can pause on
   * focus lost. e.g. user switches to take a call.
   */
  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    if (!hasWindowFocus) {

    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return thread.handleMotionEvent(event);
  }

  public ControllerThread getThread() {
    return thread;
  }
}
