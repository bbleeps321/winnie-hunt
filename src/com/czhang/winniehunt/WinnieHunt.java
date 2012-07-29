package com.czhang.winniehunt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class WinnieHunt extends Activity {

  public static final String TAG = "winniehunt";

  /** Handle to view in which game is running. **/
  private ControllerView gameView;

  /** MediaPlayer used to play the background music. **/
  private MediaPlayer mediaPlayer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.controller_view);
    setContentView(R.layout.activity_main);

    // Initialize media player.
    mediaPlayer = MediaPlayer.create(this, R.raw.music_background);
    mediaPlayer.setLooping(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, 0, 0, R.string.menu_hi);
    return true;
  }

  @Override
  public void onDestroy() {
    mediaPlayer.release();
    super.onDestroy();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Only one item, so just do that one thing.
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.alert_hi)
       .setCancelable(false)
       .setPositiveButton("I love you too!", new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int id) {
           dialog.cancel();
         }
       })
       .setNegativeButton("Heng!", new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int id) {
           // Do nothing.
         }
       });
    builder.create();
    builder.show();
    return true;
  }

  /**
   * Handles when the start button is clicked.
   */
  public void onStartButtonClick(View view) {
    setContentView(R.layout.controller_view);

    // Get handle to the game view.
    gameView = (ControllerView) findViewById(R.id.controllerView);

    // Start the thread.
    gameView.getThread().doStart();

    // Start playing the music.
    mediaPlayer.start();
  }

  /**
   * Invoked when the Activity loses user focus.
   */
  @Override
  protected void onPause() {
    super.onPause();

    // We don't handle pause and resume, so we just destroy.
    finish();
  }
}
