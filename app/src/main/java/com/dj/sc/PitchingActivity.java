package com.dj.sc;

/*
PitchingActivity.java - pitching stats screen of Scorecard application

Copyright 2023 Dave Jaffe

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import static com.dj.sc.StartUpActivity.base_text_size;
import static com.dj.sc.StartUpActivity.create_canvas;
import static com.dj.sc.StartUpActivity.current_pitcher_index;
import static com.dj.sc.StartUpActivity.heightPx;
import static com.dj.sc.StartUpActivity.hideNavigation;
import static com.dj.sc.StartUpActivity.max_number_pitchers;
import static com.dj.sc.StartUpActivity.pitcher_balls;
import static com.dj.sc.StartUpActivity.pitcher_bb;
import static com.dj.sc.StartUpActivity.pitcher_earned_runs;
import static com.dj.sc.StartUpActivity.pitcher_errors;
import static com.dj.sc.StartUpActivity.pitcher_hits;
import static com.dj.sc.StartUpActivity.pitcher_k;
import static com.dj.sc.StartUpActivity.pitcher_number_names;
import static com.dj.sc.StartUpActivity.pitcher_outs;
import static com.dj.sc.StartUpActivity.pitcher_pitches;
import static com.dj.sc.StartUpActivity.pitcher_runs;
import static com.dj.sc.StartUpActivity.pitcher_strikes;
import static com.dj.sc.StartUpActivity.team_color;
import static com.dj.sc.StartUpActivity.track_b_s;
import static com.dj.sc.StartUpActivity.widthPx;

public class PitchingActivity extends AppCompatActivity {
  private static final String TAG = PitchingActivity.class.getName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pitching_activity);

    hideNavigation(getWindow().getDecorView());

    int DarkerGray = ResourcesCompat.getColor(getResources(), R.color.DarkerGray, null);

    int pitchers_layout_width = (int) Math.round(0.18 * widthPx);
    int pitchers_layout_height = (int) Math.floor(heightPx/14.0);
    int pitching_sums_layout_width = (int) Math.round(0.33 * widthPx);
    int small_text_size =  (int) Math.round(0.9 * base_text_size);
    //Log.i(TAG, "Dimension: pitchers_layout_width=" + pitchers_layout_width + " pitchers_layout_height=" + pitchers_layout_height +
    //  " pitching_sums_layout_width=" + pitching_sums_layout_width);

    String pitcher_name="", pitcher_number_name;

    String header = "  IP  H  R ER BB  K E";
    if (track_b_s) header = header + " P/B/S";

    ImageView[] pitchers = new ImageView[max_number_pitchers + 1];
    final Bitmap[] pitcher_bitmaps = new Bitmap[max_number_pitchers + 1];
    ImageView[] pitching_sums = new ImageView[max_number_pitchers + 1];
    final Bitmap[] pitching_sums_bitmaps = new Bitmap[max_number_pitchers + 1];

    GridLayout visitorPitcherGridLayout = findViewById(R.id.visitor_pitchers_grid_layout);
    visitorPitcherGridLayout.removeAllViews();
    visitorPitcherGridLayout.setRowCount(max_number_pitchers + 1);
    for (int i = 0; i <= current_pitcher_index[0] + 1; i++) {
      pitcher_bitmaps[i] = Bitmap.createBitmap(pitchers_layout_width, pitchers_layout_height, Bitmap.Config.ARGB_8888);
      if (i == 0) create_canvas(pitcher_bitmaps[0], Color.WHITE, 0, 0, pitchers_layout_width, pitchers_layout_height,
          team_color[0], small_text_size, "Visitors", 0, (int) Math.floor(.67 * pitchers_layout_height),false);
      else {
        // Use only pitcher name if specified; if only number specified use that
        pitcher_number_name = pitcher_number_names[i-1][0];
        if (pitcher_number_name.equals("")) pitcher_name = "";    // Neither number nor name specified
        else if (pitcher_number_name.length() == 4) pitcher_name = pitcher_number_name.substring(0,3); // Number only
        else if (pitcher_number_name.length() > 4) pitcher_name = pitcher_number_name.substring(4); // Name only
        create_canvas(pitcher_bitmaps[i], DarkerGray, 0, 0, pitchers_layout_width - 2, pitchers_layout_height - 2,
            team_color[0], small_text_size, i + ")  " + pitcher_name, 5,
            (int) Math.floor(.6 * pitchers_layout_height), false);
      }
      pitchers[i] = new ImageView(this);
      pitchers[i].setImageBitmap(pitcher_bitmaps[i]);
      visitorPitcherGridLayout.addView(pitchers[i]);
    }

    GridLayout visitorPitchingSumsGridLayout = findViewById(R.id.visitor_pitchers_sums_grid_layout);
    visitorPitchingSumsGridLayout.removeAllViews();
    visitorPitchingSumsGridLayout.setRowCount(max_number_pitchers + 1);
    for (int i = 0; i <= current_pitcher_index[0] + 1; i++) {
      pitching_sums_bitmaps[i] = Bitmap.createBitmap(pitching_sums_layout_width, pitchers_layout_height, Bitmap.Config.ARGB_8888);
      if (i == 0) create_canvas(pitching_sums_bitmaps[0], Color.WHITE, 0, 0, pitching_sums_layout_width, pitchers_layout_height,
          team_color[0], small_text_size, header, 0, (int) Math.floor(.67 * pitchers_layout_height),true);
      else {
        // Convert outs to innings pitched using eg 2 1/3 -> 2.1
        String sums = String.format("%2d.%1d%3d%3d%3d%3d%3d%2d", pitcher_outs[i-1][0]/3, pitcher_outs[i-1][0] % 3, pitcher_hits[i-1][0],
            pitcher_runs[i-1][0], pitcher_earned_runs[i-1][0], pitcher_bb[i-1][0], pitcher_k[i-1][0], pitcher_errors[i-1][0]);
        if (track_b_s) sums = sums + " " + pitcher_pitches[i-1][0] + "/" + pitcher_balls[i-1][0] + "/" + pitcher_strikes[i-1][0];
        create_canvas(pitching_sums_bitmaps[i], DarkerGray, 0, 0, pitching_sums_layout_width - 2, pitchers_layout_height - 2,
            team_color[0], small_text_size, sums, 0, (int) Math.floor(.6 * pitchers_layout_height), true);
      }
      pitching_sums[i] = new ImageView(this);
      pitching_sums[i].setImageBitmap(pitching_sums_bitmaps[i]);
      visitorPitchingSumsGridLayout.addView(pitching_sums[i]);
    }

    GridLayout homePitcherGridLayout = findViewById(R.id.home_pitchers_grid_layout);
    homePitcherGridLayout.removeAllViews();
    homePitcherGridLayout.setRowCount(max_number_pitchers + 1);
    for (int i = 0; i <= current_pitcher_index[1] + 1; i++) {
      pitcher_bitmaps[i] = Bitmap.createBitmap(pitchers_layout_width, pitchers_layout_height, Bitmap.Config.ARGB_8888);
      if (i == 0) create_canvas(pitcher_bitmaps[0], Color.WHITE, 0, 0, pitchers_layout_width, pitchers_layout_height,
          team_color[1], small_text_size, "Home", 0, (int) Math.floor(.67 * pitchers_layout_height),false);
      else {
        // Use only pitcher name if specified; if only number specified use that
        pitcher_number_name = pitcher_number_names[i-1][1];
        if (pitcher_number_name.equals("")) pitcher_name = "";    // Neither number nor name specified
        else if (pitcher_number_name.length() == 4) pitcher_name = pitcher_number_name.substring(0,3); // Number only
        else if (pitcher_number_name.length() > 4) pitcher_name = pitcher_number_name.substring(4); // Name only
        create_canvas(pitcher_bitmaps[i], DarkerGray, 0, 0, pitchers_layout_width - 2, pitchers_layout_height - 2,
            team_color[1], small_text_size, i + ")  " + pitcher_name, 5,
            (int) Math.floor(.6 * pitchers_layout_height), false);
      }
      pitchers[i] = new ImageView(this);
      pitchers[i].setImageBitmap(pitcher_bitmaps[i]);
      homePitcherGridLayout.addView(pitchers[i]);
    }

    GridLayout homePitchingSumsGridLayout = findViewById(R.id.home_pitchers_sums_grid_layout);
    homePitchingSumsGridLayout.removeAllViews();
    homePitchingSumsGridLayout.setRowCount(max_number_pitchers + 1);
    for (int i = 0; i <= current_pitcher_index[1] + 1; i++) {
      pitching_sums_bitmaps[i] = Bitmap.createBitmap(pitching_sums_layout_width, pitchers_layout_height, Bitmap.Config.ARGB_8888);
      if (i == 0) create_canvas(pitching_sums_bitmaps[0], Color.WHITE, 0, 0, pitching_sums_layout_width, pitchers_layout_height,
          team_color[1], small_text_size, header, 0, (int) Math.floor(.67 * pitchers_layout_height),true);
      else {
        // Convert outs to innings pitched using eg 2 1/3 -> 2.1
        //Log.i(TAG, "pitcher_outs=" + pitcher_outs[i-1][1]);
        String sums = String.format("%2d.%1d%3d%3d%3d%3d%3d%2d", pitcher_outs[i-1][1]/3, pitcher_outs[i-1][1] % 3, pitcher_hits[i-1][1],
            pitcher_runs[i-1][1], pitcher_earned_runs[i-1][1], pitcher_bb[i-1][1], pitcher_k[i-1][1], pitcher_errors[i-1][1]);
        if (track_b_s) sums = sums + " " + pitcher_pitches[i-1][1] + "/" + pitcher_balls[i-1][1] + "/" + pitcher_strikes[i-1][1];
        create_canvas(pitching_sums_bitmaps[i], DarkerGray, 0, 0, pitching_sums_layout_width - 2, pitchers_layout_height - 2,
            team_color[1], small_text_size, sums, 0, (int) Math.floor(.6 * pitchers_layout_height), true);
      }
      pitching_sums[i] = new ImageView(this);
      pitching_sums[i].setImageBitmap(pitching_sums_bitmaps[i]);
      homePitchingSumsGridLayout.addView(pitching_sums[i]);
    }
  }  // End onCreate

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.pitching_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_return_from_pitching:
          setResult(RESULT_OK, getIntent());
          finish();
      default:
        break;
    }
    return true;
  }
}  // End SettingsActivity