/**
 * Copyright (c) 2012, Carey Zhang.
 */
package com.czhang.winniehunt;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.google.common.collect.Lists;

/**
 * Thread handling rendering and gameplay.
 *
 * @author czhang
 */
public class ControllerThread extends Thread {
  private static final int NUM_TREATS = 10;

  private static final int TEXT_SIZE = 50;
  private static final int TEXT_COLOR = Color.WHITE;

  private static final int BACKGROUND_COLOR = Color.BLACK;

  /** Vibration patterns. */
  private static final long[] EAT_PATTERN = new long[] {0, 30};
  private static final long[] CAUGHT_PATTERN = new long[] {0, 100, 200, 300, 400, 500};

  /** Safe zone within which no Carey's can be generated. */
  private static final int SAFE_ZONE_RADIUS = 100;

  /** Random number generator used for picking images. */
  private static final Random RAND = new Random();

  /** Maximum delay between taps to count as double tap. */
  private static final int DOUBLE_TAP_DELAY_MS = 250;

  /**
   * Enum for the state of the thread.
   */
  public enum State {
    PAUSE,
    READY,    // Display "tap to start" overlay.
    RUNNING,
    END       // Display "Carey caught Winnie FOREVERRRR" overlay.
  }

  /** Handle to the surface manager object we interact with */
  private final SurfaceHolder surfaceHolder;

  /** Handle to the application context, used to e.g. fetch Drawables. */
  private final Context context;

  /** State of the game. */
  private State state;

  /** If the game is over. */
  private boolean running;

  /** Canvas dimensions. */
  private int canvasWidth, canvasHeight;

  /** List of all the treats. */
  private List<Treat> treats;

  /** List of all the Careys. */
  private List<Carey> careys;

  /** The Winnie. */
  private Winnie winnie;

  /** Paint used for drawing text overlays. */
  private final Paint textPaint;

  /** Paint used for drawing background. */
  private final Paint backgroundPaint;

  /** Image bitmaps used for Winnie. */
  private final List<Bitmap> winnieImages;

  /** Image bitmaps used for Careys. */
  private final List<Bitmap> careyImages;

  /** Image bitmaps used for Treats. */
  private final List<Bitmap> treatImages;

  /**
   * Image index used so Carey images chosen in sequential order.
   * Between 0 and {@code careyImages.size - 1}.   *
   */
  private int careyImageIndex;

  /** Vibrator used for certain parts of the game. */
  private final Vibrator vibrator;

  /**
   * Time of the last tap event or 0 if never set.
   * Only set when handling motion events in the END {@link State}.
   */
  private long lastTapTime;

  /**
   * Creates a new instance of the thread. Does not start the game yet.
   */
  public ControllerThread(SurfaceHolder surfaceHolder, Context context) {
    this.surfaceHolder = surfaceHolder;
    this.context = context;

    // Set up text Paint.
    textPaint = new Paint();
    textPaint.setAntiAlias(true);
    textPaint.setColor(TEXT_COLOR);
    textPaint.setTextSize(TEXT_SIZE);
    textPaint.setTextAlign(Align.CENTER);

    // Set up background Paint.
    backgroundPaint = new Paint();
    backgroundPaint.setColor(BACKGROUND_COLOR);

    // Set up images.
    winnieImages = Lists.newArrayListWithCapacity(5);
    careyImages = Lists.newArrayListWithCapacity(5);
    treatImages = Lists.newArrayListWithCapacity(5);
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie2));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie3));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie4));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie5));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie6));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie7));
    winnieImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.winnie8));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey2));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey3));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey4));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey5));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey6));
    careyImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.carey7));
    treatImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.treat));
    treatImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.treat2));
    treatImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.treat3));
    treatImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.treat4));
    treatImages.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.treat5));
    careyImageIndex = RAND.nextInt(careyImages.size());

    // Set up vibrator.
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

  /**
   * Handles a motion event.
   */
  public boolean handleMotionEvent(MotionEvent event) {
    synchronized (surfaceHolder) {
      switch (state) {
        case PAUSE:
          return false;
        case READY:
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            state = State.RUNNING;
            return true;
          }
          return false;
        case RUNNING:
          winnie.moveTo((int) event.getX(), (int) event.getY());
          return true;
        case END:
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            long time = event.getEventTime();
            if (time - lastTapTime < DOUBLE_TAP_DELAY_MS) {
              reset();
            }
            lastTapTime = time;
            return true;
          }
          return false;
      }
      return false;
    }
  }

  @Override
  public void run() {
    while (running) {
      Canvas canvas = null;
      try {
        canvas = surfaceHolder.lockCanvas(null);
        synchronized (surfaceHolder) {
          step();
          doDraw(canvas);
        }
      } finally {
        // Do this in a finally so that if an exception is thrown
        // during the above, we don't leave the Surface in an
        // inconsistent state.
        if (canvas != null) {
          surfaceHolder.unlockCanvasAndPost(canvas);
        }
      }
    }
  }

  /**
   * Prepares and starts the game and thread.
   */
  public void doStart() {
    synchronized (surfaceHolder) {
      // Set up initial states
      state = State.READY;
      careys = Lists.newArrayList();
    }
  }

  /**
   * Resets the game. Winnie in the center, one Carey.
   */
  private void reset() {
    synchronized (surfaceHolder) {
      // Reset back to one Carey.
      careys = Lists.newArrayList();
      addCarey();

      // Move Winnie to center.
      int x = (canvasWidth - Winnie.SIZE_DP) / 2;
      int y = (canvasHeight - Winnie.SIZE_DP) / 2;
      winnie = new Winnie(x, y, winnieImages.get(RAND.nextInt(treatImages.size())));

      // Reset treats.
      setupTreats();

      // Reset state.
      state = State.READY;
    }
  }

  /**
   * Updates the location of objects based on the physics of things.
   */
  private void step() {
    // Step all the Carey's
    for (Carey carey : careys) {
      carey.step();
    }

    // Stop here if game not in RUNNING state.
    if (state != State.RUNNING) {
      return;
    }

    // Check overlap treats.
    checkOverlapTreats();

    // If all treats eaten, add another Carey and reset treats.
    // Otherwise, we check if Winnie overlaps and Careys.
    if (treatsRemaining() == 0) {
      setupTreats();
      addCarey();
    } else {
      checkOverlapCareys();
    }
  }

  /**
   * Checks and handles if Winnie overlaps a treat.
   */
  private void checkOverlapTreats() {
    // Eat any overlapping treats.
    for (int i = 0; i < treats.size(); i++) {
      if (winnie.overlaps(treats.get(i))) {
        treats.get(i).setEaten(true);
        treats.remove(i);
        i--;
        vibrator.vibrate(EAT_PATTERN, -1);
      }
    }
  }

  /**
   * Checks and handles if Winnie overlaps a Carey.
   */
  private void checkOverlapCareys() {
    boolean overlaps = false;
    for (Carey carey : careys) {
      if (winnie.overlaps(carey)) {
        overlaps = true;
        break;
      }
    }

    // If there was an overlap. End the game and move to the next state.
    if (overlaps) {
      state = State.END;
      vibrator.vibrate(CAUGHT_PATTERN, -1);
    }
  }

  /**
   * Draws the game using the specified canvas.
   * @param canvas
   */
  private void doDraw(Canvas canvas) {
    if (canvas == null) {
      return;
    }

    // Draw background, clears previously drawn items.
    canvas.drawColor(BACKGROUND_COLOR);

    // Always draw these, regardless of the state.
    for (Treat treat : treats) {
      if (!treat.isEaten()) {
        treat.draw(canvas);
      }
    }
    for (Carey carey : careys) {
      carey.draw(canvas);
    }
    winnie.draw(canvas);

    // For certain states, we want to draw a text overlay.
    int x = canvasWidth/2;
    int y = canvasHeight/2;
    switch (state) {
      case READY:
        canvas.drawText(context.getResources().getString(R.string.message_ready), x, y, textPaint);
        break;
      case END:
        canvas.drawText(context.getResources().getString(R.string.message_done1),
            x, y - TEXT_SIZE / 2, textPaint);
        canvas.drawText(context.getResources().getString(R.string.message_done2),
            x, y + TEXT_SIZE / 2, textPaint);
        break;
    }
  }

  /**
   * Sets up the visible parts of the game. Requires that {@link #canvasWidth} and
   * {@link #canvasHeight} be set already to function properly.
   */
  public void setupLayout() {
    synchronized (surfaceHolder) {
      setupTreats();
      setupWinnie();
      addCarey();
    }
  }

  /**
   * Creates and populates {@link #treats} with {@link #NUM_TREATS} treats.
   */
  private void setupTreats() {
    treats = Lists.newArrayList();

    for (int i = 0; i < NUM_TREATS; i++) {
      int x = (int) (Math.random() * (canvasWidth - Treat.SIZE_DP));
      int y = (int) (Math.random() * (canvasHeight - Treat.SIZE_DP));
      treats.add(new Treat(x, y, treatImages.get(RAND.nextInt(treatImages.size()))));
    }
  }

  /**
   * Returns the number of treats yet to be eaten.
   */
  private int treatsRemaining() {
    return treats.size();
  }

  /**
   * Creates a Winnie at the center of the screen.
   */
  private void setupWinnie() {
    // Get center coordinates.
    int x = (canvasWidth - Winnie.SIZE_DP) / 2;
    int y = (canvasHeight - Winnie.SIZE_DP) / 2;
    winnie = new Winnie(x, y, winnieImages.get(RAND.nextInt(treatImages.size())));
  }

  /**
   * Adds another Carey at a random location with a random
   */
  private void addCarey() {
    // Ensure that no Careys are made within the safe zone.
    int x = (int) (Math.random() * (canvasWidth - Carey.SIZE_DP));
    int y = (int) (Math.random() * (canvasHeight - Carey.SIZE_DP));
    Rect safeZone = new Rect(x - SAFE_ZONE_RADIUS, y - SAFE_ZONE_RADIUS,
        x + SAFE_ZONE_RADIUS, y + SAFE_ZONE_RADIUS);
    while (safeZone.contains(x, y)) {
      x = (int) (Math.random() * (canvasWidth - Carey.SIZE_DP));
      y = (int) (Math.random() * (canvasHeight - Carey.SIZE_DP));
    }

    careys.add(new Carey(x, y, canvasWidth, canvasHeight,
        careyImages.get(nextCareyImageIndex())));
  }

  /**
   * Returns the next index for Carey picture.
   */
  private int nextCareyImageIndex() {
    careyImageIndex++;
    careyImageIndex = (careyImageIndex >= careyImages.size()) ? 0 : careyImageIndex;
    return careyImageIndex;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  public void setSurfaceSize(int width, int height) {
    synchronized (surfaceHolder) {
      canvasWidth = width;
      canvasHeight = height;
    }
  }
}
