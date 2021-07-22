package com.example.sc;

/*
StartUpActivity.java - start up code for Scorecard application

Copyright 2021 Dave Jaffe

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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import static java.lang.Math.*;
import static java.lang.Integer.parseInt;

public class StartUpActivity extends AppCompatActivity {
  // region Declarations
  private static final String TAG = StartUpActivity.class.getName();

  final static int ACTIVITY_MAIN = 0, ACTIVITY_SETTINGS = 1;

  static int number_innings = 30;
  static int number_innings_regulation;
  static int default_number_innings_regulation = 9;
  static int number_batters;
  static int default_number_batters = 9;
  static int max_number_batters = 20;
  static int max_number_pitchers = 13;
  static int max_players_on_roster = 50;
  static int number_atBats = number_innings * max_number_batters;
  static String[][] visitor_player_info = new String[max_players_on_roster][3];
  static String[][] home_player_info = new String[max_players_on_roster][3];
  static int inning = 1;
  static int inning_half = 0; // 0=Top, 1=Bottom
  static int team_up = 0;
  static boolean game_over, game_complete;
  static String date_str = "", time_str;

  // All team arrays use index 0=Visitors, 1=Home team
  static String[][][] player_info = new String[max_players_on_roster][3][2];
  static int[] number_players = {0, 0};
  static String[][] batter_number_names = new String[max_number_batters][2];
  static String[][] batter_position = new String[max_number_batters][2];
  static int[][] batter_pa = new int[max_number_batters][2];
  static int[][] batter_abs = new int[max_number_batters][2];
  static int[][] batter_runs = new int[max_number_batters][2];
  static int[][] batter_hits = new int[max_number_batters][2];
  static int[][] batter_rbi = new int[max_number_batters][2];
  static int[][] batter_errors = new int[max_number_batters][2];

  static int[] current_pitcher_index = {0, 0};
  static String[][] pitcher_number_names = new String[max_number_pitchers][2];
  static int[][] pitcher_batters_faced = new int[max_number_pitchers][2];
  static int[][] pitcher_outs = new int[max_number_pitchers][2];
  static int[][] pitcher_hits = new int[max_number_pitchers][2];
  static int[][] pitcher_runs = new int[max_number_pitchers][2];
  static int[][] pitcher_earned_runs = new int[max_number_pitchers][2];
  static int[][] pitcher_bb = new int[max_number_pitchers][2];
  static int[][] pitcher_k = new int[max_number_pitchers][2];
  static int[][] pitcher_errors = new int[max_number_pitchers][2];
  static int[][] pitcher_pitches = new int[max_number_pitchers][2];
  static int[][] pitcher_balls = new int[max_number_pitchers][2];
  static int[][] pitcher_strikes = new int[max_number_pitchers][2];
  static boolean track_b_s = false;
  
  static String[] team_name = new String[2];
  static int[] team_color = {0, 0};
  static String[][] team_info = new String[40][2];
  static int[] team_abs = {0, 0};
  static int[] team_runs = {0, 0};
  static int[] team_hits = {0, 0};
  static int[] team_rbi = {0, 0};
  static int[] team_errors = {0, 0};
  static int[][] atBat_state_array = new int[number_atBats][2]; // 0=Not yet, 1=AtBat, 2=OnBase, 3=Finished
  static int[][] atBat_sequence_array = new int[number_atBats][2]; // sequential atBatInds per team
  static int[]  atBat_sequence_index = {0, 0};
  static int[] next_up = {0, 0};
  static int[] outs = {0,0};
  static int[] on_base = {0,0};
  static Bitmap[][] thumbnail_bitmap_array = new Bitmap[number_atBats][2];
  static int[][] inning_header_values = new int[number_innings][2];
  static int[][] inning_runs = new int[number_innings][2];
  static int[][] inning_hits = new int[number_innings][2];
  static int[][] inning_lob = new int[number_innings][2];
  static int inning_half_batters = 0;

  static int base_text_size;
  static float density, widthPx, heightPx, real_aspect_ratio;
  static int thumbnail_size, s, h, t, m, mr, ml, mb, mt, ts, sw, o_r;
  static float[] thumbnail_bases;
  static float[] outline;
  static float[] bottom_line;
  static float[] right_line;
  static float[] o_loc, oc_loc, ot_loc, ll, lr, ur, ul;
  static float[][] thumbnail_locations = new float[5][2];

  static String[][] atBat_result_types = {
      {"", "Strikeout Swinging", "Strikeout Looking", "Ground Out", "Force Out", "Double Play", "Triple Play", "Line Out To Infield", "Pop Up Out", "Fly Out",
          "Infield Fly Out", "Sacrifice Bunt", "Sacrifice Fly"},  // Outs
      {"Single", "Walk", "Hit By Pitch", "Fielder's Choice", "Safe on Dropped 3rd Strike", "1B Error", "Catcher Interference"},  // Safe - 1 base
      {"Double", "2B Error"},    // Safe - 2 base
      {"Triple", "3B Error"},    // Safe - 3 base
      {"Home Run", "4B Error"},  // Safe - 4 base
      {"Advance One Base by Hit/Walk/PB/WP/Sacrifice/FC/Error", "Advance Two Bases by Hit/Walk/PB/WP/Sacrifice/FC/Error",
       "Advance Three Bases by Hit/Walk/PB/WP/Sacrifice/FC/Error", "Stolen Base", "Caught Stealing",
       "Picked Off", "Out on ground out/double play/triple play/thrown out"}   // OnBase
  };

  static String[][] atBat_result_text = {
      {"", "K", "\uA4D8", "GO", "FO", "DP", "TP", "LO", "PU", "FLO", "IF", "SB", "SF"},  // Outs
      {"1B", "BB", "HBP", "FC", "KD", "E", "CI"},  // Safe - 1 base
      {"2B", "E"},   // Safe - 2 base
      {"3B", "E"},   // Safe - 3 base
      {"HR", "E"},   // Safe - 4 base
      {"+1", "+2", "+3", "SB", "CS", "PO", "OUT"}    // OnBase
  };

  static boolean[][] atBat_result_is_ab = {
      {true, true, true, true, true, true, true, true, true, true, true, false, false},  // Outs
      {true, false, false, true, true, true, false},  // Safe - 1 base
      {true, true},   // Safe - 2 base
      {true, true},   // Safe - 3 base
      {true, true}    // Safe - 4 base
  };

  static boolean[][] atBat_result_is_hit = {
      {false, false, false, false, false, false, false, false, false, false, false, false, false},  // Outs
      {true, false, false, false, false, false, false},  // Safe - 1 base
      {true, false},   // Safe - 2 base
      {true, false},   // Safe - 3 base
      {true, false}    // Safe - 4 base
  };
  
  static int result_ind = 0;

  static public class atBat {
    String batter_number_name = "";
    String pitcher_number_name = "";
    int inning;
    int inning_half;
    int out_number = 0;
    int ob[] = {0, 0, 0, 0, 0};  // ob[0] is how out was made during atBat, ob[i] shows how batter/runner reached base i or how out was made going to base i
    int current_base = 0;
    Boolean error_during_at_bat_or_on_base = false;
    float[] ball_in_play_location = {0.0f, 0.0f};
    int RBIs = 0;
    int stolen_base = 0;
    boolean picked_off = false;
    boolean scored = false;
    int balls = 0;
    int strikes = 0;  // Strikes thrown during at bat including foul balls on 2 strikes
    String comment = "";
  }

  static atBat[][] atBat_array = new atBat[number_atBats][2];
  
  static boolean visiting_team_name_set = false;
  static boolean home_team_name_set = false;

  static String directory_path;
  static String rosters_filename = "rosters.json";
  static String roster_json_str;
  static File game_file;
  static BufferedWriter game_file_bufferedWriter;

  static String game_file_header_json;

  static Intent intent;

  Canvas thumbnail_canvas;
  Paint thumbnail_paint;
  // endregion Declarations

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.startup_activity);
    intent = new Intent(this, MainActivity.class);

    hideNavigation(getWindow().getDecorView());

    // Get screen info not including height of navigation bar at bottom
    density = getResources().getDisplayMetrics().density;
    heightPx = getResources().getDisplayMetrics().heightPixels;
    widthPx = getResources().getDisplayMetrics().widthPixels;
    //Log.i(TAG, "Dimension: density=" + density + " widthPx=" + widthPx + " heightPx=" +  heightPx);

    // Get full size of display
    Display display = this.getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getRealSize(size);
    real_aspect_ratio = (float) size.x / (float) size.y;
    String real_aspect_ratio_str = String.format("%4.2f", real_aspect_ratio);
    //Log.i(TAG, "Dimension: real width=" + size.x + " real height=" + size.y + " real aspect ratio=" + real_aspect_ratio_str);

    // Find heights of status bar at top and navigation bar at bottom
    int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
    int navigation_bar_height = getResources().getDimensionPixelSize(resourceId);
    resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    int status_bar_height = getResources().getDimensionPixelSize(resourceId);
    //Log.i(TAG, "Dimension: status bar height=" + status_bar_height + " navigation_bar_height=" + navigation_bar_height);

    // Read specified action bar height and left margin from dimensions.xml; apply to full size to calculate available pixels
    float action_bar_height = getResources().getDimension(R.dimen.action_bar_height); // Action bar height in pixels
    float main_layout_left_margin = getResources().getDimension(R.dimen.main_layout_left_margin); // Main layout left margin in pixels
    heightPx = size.y - status_bar_height - action_bar_height;
    widthPx = size.x - 2*main_layout_left_margin;  // widthPx/heightPx now is total screen width/height available for layout
    //Log.i(TAG, "Dimension: action_bar_height=" + action_bar_height + " available: heightPx=" +  heightPx);
    //Log.i(TAG, "Dimension: main_layout_left_margin=" + main_layout_left_margin +" available: widthPx=" + widthPx);

    initialize_vars(default_number_batters, default_number_innings_regulation);

    // region Read rosters and setup team popups
    directory_path = this.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/";
    //Log.i(TAG, "directory path=" + directory_path);

    if (new File(directory_path + rosters_filename).isFile())
      roster_json_str = read_file(directory_path, rosters_filename);
    else roster_json_str = "{\"Teams\": [{\"Name\": \"Team 1\", \"Color\": \"#0000FF\", \"Players\":" +
      "[]}, {\"Name\": \"Team 2\", \"Color\": \"#FF0000\", \"Players\":[]}]}";
    //Log.i(TAG, "roster_json_str: " + roster_json_str);

    team_info = parse_roster_json_team_info(roster_json_str);
    //Log.i(TAG, "team_info length: " + team_info.length);

    final PopupMenu team_popup = new PopupMenu(this, findViewById(R.id.popup_insert_point));
    for (int i = 0; i < team_info.length; i++) team_popup.getMenu().add(0, i, 0, team_info[i][0]);
    // endregion

    // region Button selectVisitors
    final Button selectVisitors = (Button) findViewById(R.id.visitingTeamButton);
    selectVisitors.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
      team_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
          //Toast.makeText(StartUpActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
          team_name[0] = item.getTitle().toString();
          visiting_team_name_set = true;
          team_color[0] = Color.parseColor(team_info[item.getItemId()][1]);
          visitor_player_info = parse_roster_json_player_info(roster_json_str, team_name[0]);
          number_players[0] = visitor_player_info.length;
          //Log.i(TAG, "visitor_player_info length: " + visitor_player_info.length);
          for (int i = 0; i < visitor_player_info.length; i++) {
            for (int j = 0; j < 3; j++) {
              Log.i(TAG, "visitor_player_info[" + i + "][" + j + "]: " + visitor_player_info[i][j]);
              player_info[i][j][0] = visitor_player_info[i][j];
            }
          }
          selectVisitors.setText(team_name[0]);
          selectVisitors.setTextColor(team_color[0]);
          if (visiting_team_name_set && home_team_name_set) play_ball(team_name, intent);
          return true;
        }
      });
      team_popup.show();
      }
    });  // End selectVisitors.setOnClickListener
    // endregion

    // region Button selectHome
    final Button selectHome = (Button) findViewById(R.id.homeTeamButton);
    selectHome.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
      team_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
          //Toast.makeText(StartUpActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
          team_name[1] = item.getTitle().toString();
          home_team_name_set = true;
          team_color[1] = Color.parseColor(team_info[item.getItemId()][1]);
          home_player_info = parse_roster_json_player_info(roster_json_str, team_name[1]);
          number_players[1] = home_player_info.length;
          //Log.i(TAG, "home_player_info length: " + home_player_info.length);
          for (int i = 0; i < home_player_info.length; i++) {
            for (int j = 0; j < 3; j++) {
              Log.i(TAG, "home_player_info[" + i + "][" + j + "]: " + home_player_info[i][j]);
              player_info[i][j][1] = home_player_info[i][j];
            }
          }
          selectHome.setText(team_name[1]);
          selectHome.setTextColor(team_color[1]);
          if (visiting_team_name_set && home_team_name_set) play_ball(team_name, intent);
          return true;
        }
      });
      team_popup.show();
      }
    });  // End selectHome.setOnClickListener
    // endregion
  } // End OnCreate
  
  public void initialize_vars(int n_batters, int n_innings_regulation) {

    // region Initialize non-arrays
    number_batters = n_batters;
    number_innings_regulation = n_innings_regulation;
    //Log.i(TAG, "In initialize_vars: number_batters=" + number_batters + " number_innings_regulation=" + number_innings_regulation);
    max_players_on_roster = 50;
    visitor_player_info = new String[max_players_on_roster][3];
    home_player_info = new String[max_players_on_roster][3];
    inning = 1;
    inning_half = 0; // 0=Top, 1=Bottom
    team_up = 0;
    game_over = false;
    // endregion Initialize non-arrays

    // region Initialize arrays
    for (int i = 0; i < number_atBats; i++) {
      atBat_array[i][0] = new atBat();
      atBat_array[i][1] = new atBat();
      atBat_state_array[i][0] = 0;
      atBat_state_array[i][1] = 0;
      atBat_sequence_array[i][0] = 0;
      atBat_sequence_array[i][1] = 0;
    }
    atBat_state_array[0][0] = 1;

    for (int i = 0; i < number_batters; i++) {
      batter_number_names[i][0] = "";
      batter_number_names[i][1] = "";
      batter_position[i][0] = "";
      batter_position[i][1] = "";
      batter_pa[i][0] = 0;
      batter_pa[i][1] = 0;
      batter_abs[i][0] = 0;
      batter_abs[i][1] = 0;
      batter_runs[i][0] = 0;
      batter_runs[i][1] = 0;
      batter_hits[i][0] = 0;
      batter_hits[i][1] = 0;
      batter_rbi[i][0] = 0;
      batter_rbi[i][1] = 0;
      batter_errors[i][0] = 0;
      batter_errors[i][1] = 0;
    }

    for (int i = 0; i < max_number_pitchers; i++) {
      pitcher_number_names[i][0] = "";
      pitcher_number_names[i][1] = "";
      pitcher_batters_faced[i][0] = 0;
      pitcher_batters_faced[i][1] = 0;
      pitcher_outs[i][0] = 0;
      pitcher_outs[i][1] = 0;
      pitcher_hits[i][0] = 0;
      pitcher_hits[i][1] = 0;
      pitcher_runs[i][0] = 0;
      pitcher_runs[i][1] = 0;
      pitcher_earned_runs[i][0] = 0;
      pitcher_earned_runs[i][1] = 0;
      pitcher_bb[i][0] = 0;
      pitcher_bb[i][1] = 0;
      pitcher_k[i][0] = 0;
      pitcher_k[i][1] = 0;
      pitcher_errors[i][0] = 0;
      pitcher_errors[i][1] = 0;
      pitcher_pitches[i][0] = 0;
      pitcher_pitches[i][1] = 0;
      pitcher_balls[i][0] = 0;
      pitcher_balls[i][1] = 0;
      pitcher_strikes[i][0] = 0;
      pitcher_strikes[i][1] = 0;
    }

    for (int i = 0; i < number_innings; i++) {
      inning_header_values[i][0] = i+1;
      inning_header_values[i][1] = i+1;
      inning_runs[i][0] = 0;
      inning_runs[i][1] = 0;
      inning_hits[i][0] = 0;
      inning_hits[i][1] = 0;
      inning_lob[i][0] = 0;
      inning_lob[i][1] = 0;
    }
    // endregion Initialize arrays

    // region Set up dimensions

    // General dimensions
    base_text_size = getResources().getDimensionPixelSize(R.dimen.base_text_size);
    if (density > 3) base_text_size = (int) floor(base_text_size *.8);
    //Log.i(TAG, "Dimension: base_text_size in pixels=" + base_text_size);

    // Thumbnail dimensions
    thumbnail_size = (int) floor(0.72 * heightPx/number_batters);
    s = thumbnail_size;
    h = (int) floor(s / 2.0);  // half thumbnail size
    float hf =  (float) (s / 2.0);  // half thumbnail size as a float
    t = (int) floor(s / 3.0);  // half thumbnail size
    m = (int) ceil(s / 10.0);  // margin_for_thumbnail_bases
    ml = m;                         // left margin for thumbnail base hit marks
    mr = 3 * m;                     // right margin for thumbnail base hit marks
    mb = m;                         // bottom margin for thumbnail base hit marks
    mt = 2 * m;                     // top margin for thumbnail base hit marks
    sw = (int) ceil(s / 20.0) + 1;  // stroke width for thumbnail base hits lines
    ts = 2 * m;                     // text size for thumbnail base hit marks
    thumbnail_bases = new float[]{hf, s - m, s - m, hf, hf, m, m, hf, hf, s - m};
    //Log.i(TAG, "Dimension: thumbnail_size=" + thumbnail_size + " m=" + m + " sw=" + sw);
    o_loc = new float[]{0.9f*hf, t}; // location of number of outs text in thumbnail
    o_r = (int) ceil(s/6.0);    // radius of circle around number of outs text in thumbnail
    //Log.i(TAG, "o_loc[0]=" + o_loc[0] + " o_loc[1]=" + o_loc[1] + " t=" + t + " o_r=" + o_r);
    oc_loc = new float[]{hf, 0.88f*t};  // location of circle around outs in thumbnail
    ot_loc = new float[]{0.79f*hf, 2.1f*t};  // location of outs text in thumbnail
    ll = new float[]{ml, s - mb};   // Locations for thumbnail base hit markers
    lr = new float[]{s - mr, s - mb};
    ur = new float[]{s - mr, mt};
    ul = new float[]{ml, mt};
    outline = new float[]{0, 0, 0, s, 0, s, s, s, s, s, s, 0, s, 0, 0, 0};
    bottom_line = new float[]{0, s, s, s};
    right_line = new float[]{s, 0, s, s};
    thumbnail_locations[0] = null; thumbnail_locations[1] = lr; thumbnail_locations[2] = ur; thumbnail_locations[3] = ul; thumbnail_locations[4] = ll;
    // endregion Set up dimensions

    // region Set up main grid
    
    // Main atBat grid - visitors
    for (int i = 0; i < number_atBats; i++) {
      thumbnail_bitmap_array[i][0] = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
      create_canvas(thumbnail_bitmap_array[i][0], Color.LTGRAY, 2, 2, s - 2, s - 2,
          Color.BLACK, ts, "" /*Integer.toString(i+1)*/, h, (int) floor(.6 * s), false);
      thumbnail_canvas = new Canvas(thumbnail_bitmap_array[i][0]);
      thumbnail_paint = new Paint();
      drawBasePaths(thumbnail_canvas, thumbnail_paint, Color.DKGRAY, 1, thumbnail_bases, 4, false);
      thumbnail_paint.setStrokeWidth(2);
      if (i == 0) drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.BLACK, 2, outline);
    }

    // Main atBat grid - home
    for (int i = 0; i < number_atBats; i++) {
      thumbnail_bitmap_array[i][1] = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
      create_canvas(thumbnail_bitmap_array[i][1], Color.LTGRAY, 1, 1, s - 1, s - 1,
          Color.BLACK, ts, "" /*Integer.toString(i+1)*/, h, (int) floor(.6 * s), false);
      thumbnail_canvas = new Canvas(thumbnail_bitmap_array[i][1]);
      thumbnail_paint = new Paint();
      drawBasePaths(thumbnail_canvas, thumbnail_paint, Color.DKGRAY, 1, thumbnail_bases, 4, false);
    }
    // endregion
  }

  public void play_ball(String[] team_name, final Intent intent) {
    //Log.i(TAG, "In play_ball: " + team_name[0] + " at " + team_name[1]);
    // Create file to hold game history
    DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
    String year_day_str = df.format(Calendar.getInstance().getTime());
    String game_filename_base = year_day_str + " " + team_name[0] + "_at_" + team_name[1];
    game_filename_base = game_filename_base.replace(" ", "_");
    try {
      game_file = new File(directory_path + game_filename_base + ".json");
      int file_iteration = 1;
      while (!game_file.createNewFile()) {   // If file exists, add number to end, increment if necessary
        String game_filename_with_suffix = game_filename_base + "_" + file_iteration++ + ".json";
        game_file = new File(directory_path + game_filename_with_suffix);
      }
      //Log.i(TAG, "game file: " + game_file.getAbsoluteFile());
      game_file_bufferedWriter = new BufferedWriter(new FileWriter(game_file));
      df = new SimpleDateFormat("EEEE, MMMM d, yyyy");
      date_str = df.format(Calendar.getInstance().getTime());
      SimpleDateFormat tf = new SimpleDateFormat("h:mm a");
      time_str = tf.format(Calendar.getInstance().getTime());
      game_file_header_json = "{\"Game\":{\"Visitors\":\"" + team_name[0] +
          "\", \"Home\":\"" + team_name[1]  + "\",\n \"Date\":\"" + date_str + "\", \"Time\":\"" + time_str + "\",\n " + "\"NumberBatters\":" + number_batters +
          ", \"NumberInningsRegulation\":" + number_innings_regulation + ", \"Track_b_s\":" + track_b_s +"},\n\"Innings\":\n[\n";
      game_file_bufferedWriter.write(game_file_header_json);
      game_file_bufferedWriter.flush();
    }  catch (IOException ex) {
      //Log.i(TAG, "game file IOException" + ex.getMessage());
      Toast.makeText(StartUpActivity.this, "game file IOException" + ex.getMessage(), Toast.LENGTH_SHORT).show();
    }

    // Set up Play Ball button
    Button playBall = (Button) findViewById(R.id.playBallButtonLayout);
    playBall.setVisibility(View.VISIBLE);
    playBall.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        intent.putExtra("TeamIndex", 0);  // Start with visitors screen
        startActivityForResult(intent, ACTIVITY_MAIN);
      }
    });
  }

  public String read_file(String directory_path, String filename) {
    byte[] buffer = new byte[10000];
    String output_str = new String(buffer);
    try {
      File file = new File(directory_path + filename);
      //Log.i(TAG, "read_file: file=" + file.getAbsoluteFile());
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      StringBuilder stringBuilder = new StringBuilder();

      while ((output_str = bufferedReader.readLine()) != null) {
        stringBuilder.append(output_str + System.getProperty("line.separator"));
      }
      output_str = stringBuilder.toString();
      bufferedReader.close();
    } catch (FileNotFoundException ex) {
      //Log.i(TAG,"read_file " + directory_path + filename + ": FileNotFnd " + ex.getMessage());
      Toast.makeText(StartUpActivity.this, "read_file " + directory_path + filename + ":  file not found", Toast.LENGTH_LONG).show();
    } catch (IOException ex) {
      //Log.i(TAG, "read_file " + directory_path + filename + ": IOException " + ex.getMessage());
      Toast.makeText(StartUpActivity.this, "read_file " + directory_path + filename + ": IO Exception" +
          ex.getMessage(), Toast.LENGTH_SHORT).show();
    }
    return output_str;
  }

  public String[][] parse_roster_json_team_info(String json_str) {
    ArrayList<String[]> team_array = new ArrayList<String[]>();
    try {
      JSONObject jsonObj = new JSONObject(json_str);
      JSONArray teams = jsonObj.getJSONArray("Teams");
      //Log.i(TAG, "Found " + teams.length() + " teams");
      for (int i = 0; i < teams.length(); i++) {
        String[] t = new String[2];
        JSONObject team_info = teams.getJSONObject(i);
        t[0] = team_info.getString("Name");
        t[1] = team_info.getString("Color");
        team_array.add(t);
      }
    } catch (JSONException ex) {
      //Log.i("json Exception", ex.getMessage());
    }
    return team_array.toArray(new String[team_array.size()][2]);
  }

  public String[][] parse_roster_json_player_info(String json_str, String team_name) {
    ArrayList<String[]> player_array = new ArrayList<String[]>();
    try {
      JSONObject jsonObj = new JSONObject(json_str);
      JSONArray teams = jsonObj.getJSONArray("Teams");
      for (int i = 0; i < teams.length(); i++) {
        JSONObject team_info = teams.getJSONObject(i);
        //Log.i(TAG, "team_name=" + team_name + " " + team_info.getString("Name"));
        if (team_name.equals(team_info.getString("Name"))) {
          JSONArray players = team_info.getJSONArray("Players");
          //Log.i(TAG, "Found " + players.length() + " players");
          for (int j = 0; j < players.length(); j++) {
            String[] t = new String[3];
            JSONObject player_info = players.getJSONObject(j);
            t[0] = player_info.getString("Number");
            t[1] = player_info.getString("Name");
            t[2] = player_info.getString("Pos");
            player_array.add(t);
          }
          break;
        }
      }

    } catch (JSONException ex) {
      //Log.i("json Exception", ex.getMessage());
    }
    return player_array.toArray(new String[player_array.size()][3]);
  }

  public void initialize_from_game_file(String game_file_json_str, boolean game_complete, final Intent intent) {
    //Log.i(TAG, "Initializing game: " + game_file_json_str + ", game_complete="+ game_complete);
    Bitmap thumbnail_bitmap;
    try{
      JSONObject jsonObj = new JSONObject(game_file_json_str);
      JSONObject game = jsonObj.getJSONObject("Game");
      team_name[0] = game.getString("Visitors");
      team_name[1] = game.getString("Home");
      date_str = game.getString("Date");
      time_str = game.getString("Time");
      //Log.i(TAG, "Visitors=" + team_name[0] + " Home=" + team_name[1] + " Date=" + date_str + " Time=" + time_str);
      number_batters = parseInt(game.getString("NumberBatters"));
      number_innings_regulation = parseInt(game.getString("NumberInningsRegulation"));
      track_b_s = Boolean.parseBoolean(game.getString("Track_b_s"));
      initialize_vars(number_batters, number_innings_regulation);
      //Log.i(TAG, "number_batters=" + number_batters + " number_innings_regulation=" + number_innings_regulation);
      int i;
      i = 0; while (!team_info[i++][0].equals(team_name[0])); team_color[0] = Color.parseColor(team_info[i-1][1]);
      i = 0; while (!team_info[i++][0].equals(team_name[1])); team_color[1] = Color.parseColor(team_info[i-1][1]);
      //Log.i(TAG, "Visitors color=" + team_color[0] + " Home color=" + team_color[1]);
      JSONArray innings = jsonObj.getJSONArray("Innings");
      //Log.i(TAG, "found " + innings.length()/2.0 + " innings");
      for (i = 0; i < innings.length(); i++) {
        JSONObject inning_info = innings.getJSONObject(i);
        inning = parseInt(inning_info.getString("Inning"));
        inning_half = parseInt(inning_info.getString("InningHalf"));
        JSONObject rhl = inning_info.getJSONObject("RHL");
        inning_runs[inning-1][inning_half] = parseInt(rhl.getString("runs"));
        inning_hits[inning-1][inning_half] = parseInt(rhl.getString("hits"));
        inning_lob[inning-1][inning_half] = parseInt(rhl.getString("lob"));
        JSONObject visitorStats = inning_info.getJSONObject("VisitorStats");
        team_abs[0] = parseInt(visitorStats.getString("AB"));
        team_runs[0] = parseInt(visitorStats.getString("R"));
        team_hits[0] = parseInt(visitorStats.getString("H"));
        team_rbi[0] = parseInt(visitorStats.getString("RBI"));
        team_errors[0] = parseInt(visitorStats.getString("E"));
        JSONObject homeStats = inning_info.getJSONObject("HomeStats");
        team_abs[1] = parseInt(homeStats.getString("AB"));
        team_runs[1] = parseInt(homeStats.getString("R"));
        team_hits[1] = parseInt(homeStats.getString("H"));
        team_rbi[1] = parseInt(homeStats.getString("RBI"));
        team_errors[1] = parseInt(homeStats.getString("E"));
        JSONArray batterInfo = inning_info.getJSONArray("VisitorBatterInfo");
        for (int j = 0; j < batterInfo.length(); j++) {
          JSONObject batter = batterInfo.getJSONObject(j);
          batter_number_names[j][0] = batter.getString("NumberName");
          batter_position[j][0] = batter.getString("Pos");
          batter_pa[j][0] = parseInt(batter.getString("PA"));
          batter_abs[j][0] = parseInt(batter.getString("AB"));
          batter_runs[j][0] = parseInt(batter.getString("R"));
          batter_hits[j][0] = parseInt(batter.getString("H"));
          batter_rbi[j][0] = parseInt(batter.getString("RBI"));
          batter_errors[j][0] = parseInt(batter.getString("E"));
        }
        JSONArray pitcherInfo = inning_info.getJSONArray("VisitorPitcherInfo");
        for (int j = 0; j < pitcherInfo.length(); j++) {
          current_pitcher_index[0] = pitcherInfo.length() - 1;
          JSONObject pitcher = pitcherInfo.getJSONObject(j);
          pitcher_number_names[j][0] = pitcher.getString("NumberName");
          pitcher_batters_faced[j][0] = parseInt(pitcher.getString("BattersFaced"));
          pitcher_outs[j][0] = parseInt(pitcher.getString("Outs"));
          pitcher_hits[j][0] = parseInt(pitcher.getString("H"));
          pitcher_runs[j][0] = parseInt(pitcher.getString("R"));
          pitcher_earned_runs[j][0] = parseInt(pitcher.getString("ER"));
          pitcher_bb[j][0] = parseInt(pitcher.getString("BB"));
          pitcher_k[j][0] = parseInt(pitcher.getString("K"));
          pitcher_errors[j][0] = parseInt(pitcher.getString("E"));
          pitcher_pitches[j][0] = parseInt(pitcher.getString("P"));
          pitcher_balls[j][0] = parseInt(pitcher.getString("B"));
          pitcher_strikes[j][0] = parseInt(pitcher.getString("S"));
        }
        batterInfo = inning_info.getJSONArray("HomeBatterInfo");
        for (int j = 0; j < batterInfo.length(); j++) {
          JSONObject batter = batterInfo.getJSONObject(j);
          batter_number_names[j][1] = batter.getString("NumberName");
          batter_position[j][1] = batter.getString("Pos");
          batter_pa[j][1] = parseInt(batter.getString("PA"));
          batter_abs[j][1] = parseInt(batter.getString("AB"));
          batter_runs[j][1] = parseInt(batter.getString("R"));
          batter_hits[j][1] = parseInt(batter.getString("H"));
          batter_rbi[j][1] = parseInt(batter.getString("RBI"));
          batter_errors[j][1] = parseInt(batter.getString("E"));
        }
        pitcherInfo = inning_info.getJSONArray("HomePitcherInfo");
        for (int j = 0; j < pitcherInfo.length(); j++) {
          current_pitcher_index[1] = pitcherInfo.length() - 1;
          JSONObject pitcher = pitcherInfo.getJSONObject(j);
          pitcher_number_names[j][1] = pitcher.getString("NumberName");
          pitcher_batters_faced[j][1] = parseInt(pitcher.getString("BattersFaced"));
          pitcher_outs[j][1] = parseInt(pitcher.getString("Outs"));
          pitcher_hits[j][1] = parseInt(pitcher.getString("H"));
          pitcher_runs[j][1] = parseInt(pitcher.getString("R"));
          pitcher_earned_runs[j][1] = parseInt(pitcher.getString("ER"));
          pitcher_bb[j][1] = parseInt(pitcher.getString("BB"));
          pitcher_k[j][1] = parseInt(pitcher.getString("K"));
          pitcher_errors[j][1] = parseInt(pitcher.getString("E"));
          pitcher_pitches[j][1] = parseInt(pitcher.getString("P"));
          pitcher_balls[j][1] = parseInt(pitcher.getString("B"));
          pitcher_strikes[j][1] = parseInt(pitcher.getString("S"));
        }
        JSONArray inningHeaders = inning_info.getJSONArray("InningHeaders");
        for (int j = 0; j < inningHeaders.length(); j++) {
          inning_header_values[j][inning_half] = parseInt(inningHeaders.getString(j));
        }
        JSONArray atBats = inning_info.getJSONArray("AtBats");
        for (int j = 0; j < atBats.length(); j++) {
          JSONObject atBat = atBats.getJSONObject(j);
          int atBatInd = parseInt(atBat.getString("AtBatInd"));
          atBat_sequence_array[atBat_sequence_index[inning_half]][inning_half] = atBatInd;
          ++atBat_sequence_index[inning_half];
          atBat_state_array[atBatInd][inning_half] = parseInt(atBat.getString("State"));;
          atBat ab = new atBat();
          ab.batter_number_name = atBat.getString("Batter");
          ab.pitcher_number_name = atBat.getString("Pitcher");
          ab.inning = parseInt(atBat.getString("Inning"));
          ab.inning_half = parseInt(atBat.getString("InningHalf"));
          ab.out_number = parseInt(atBat.getString("OutNumber"));
          ab.current_base = parseInt(atBat.getString("CurrentBase"));
          ab.error_during_at_bat_or_on_base = Boolean.parseBoolean(atBat.getString("ErrorDuringAtBatOrOnBase"));
          ab.ball_in_play_location[0] = Float.parseFloat(atBat.getString("ball_in_play_locationX"));
          ab.ball_in_play_location[1] = Float.parseFloat(atBat.getString("ball_in_play_locationY"));
          ab.RBIs = parseInt(atBat.getString("RBIs"));
          ab.stolen_base = parseInt(atBat.getString("StolenBase"));
          ab.picked_off = Boolean.parseBoolean(atBat.getString("PickedOff"));
          ab.scored = Boolean.parseBoolean(atBat.getString("Scored"));
          ab.comment = atBat.getString("Comment");
          ab.balls = parseInt(atBat.getString("Balls"));
          ab.strikes = parseInt(atBat.getString("Strikes"));
          ab.ob[0] = parseInt(atBat.getString("OnBaseArray0"));
          ab.ob[1] = parseInt(atBat.getString("OnBaseArray1"));
          ab.ob[2] = parseInt(atBat.getString("OnBaseArray2"));
          ab.ob[3] = parseInt(atBat.getString("OnBaseArray3"));
          ab.ob[4] = parseInt(atBat.getString("OnBaseArray4"));
          atBat_array[atBatInd][inning_half] = ab;

          thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][inning_half];
          thumbnail_canvas = new Canvas(thumbnail_bitmap);
          thumbnail_paint = new Paint();
          boolean is_safe = false, is_out = false, fill = false;
          float[] thumbnail_location = {0, 0};
          // Mark how batter got on base and then apply on base events one at a time, marking how they got from base to base, eg. SB, +1B
          int c_b = 0;  // Current base for purposes of drawing
          for (int k=0; k <= atBat_array[atBatInd][inning_half].current_base; k++) {
            int ob_step = atBat_array[atBatInd][inning_half].ob[k];
            //Log.i(TAG, "In initialize: atbat " + j + " onBase_step=" + ob_step + "  k=" + k +"  current_base=" +
            //  atBat_array[atBatInd][inning_half].current_base);
            if (ob_step > 0) {  // Eg. skip first base in the case of a double
              int number_bases = ob_step / 100;
              int result_type_ind = ob_step % 100;
              String result_text = atBat_result_text[number_bases][result_type_ind];
              if (number_bases == 0) is_out = true; // Out at bat
              else if (number_bases > 0  && number_bases < 5) {  // Safe at bat
                is_safe = true;
                c_b = number_bases;
                thumbnail_location = thumbnail_locations[number_bases];
                fill = false; if (number_bases == 4) fill = true;
              }  // End Safe at bat
              else {  // On base event
                if (result_type_ind < 4) {    // Safe on base
                  is_safe = true;
                  if (result_type_ind == 0 || result_type_ind == 3) c_b += 1;
                  if (result_type_ind == 1) c_b += 2;
                  if (result_type_ind == 2) c_b += 3;
                  if (c_b > 4) c_b = 4; // Just in case
                  if (c_b == 4) fill = true;
                  thumbnail_location = thumbnail_locations[c_b];
                }  // End Safe on base
                else {   // Out on base
                  is_safe = false; is_out = true;
                }  // End Out on base
              }  // End On base event

              drawBasePaths(thumbnail_canvas, thumbnail_paint, team_color[inning_half], sw, thumbnail_bases, c_b, fill);
              if (is_safe) markHit(thumbnail_canvas, thumbnail_paint, team_color[inning_half], ts, result_text, thumbnail_location);
              else markOut(thumbnail_canvas, thumbnail_paint, team_color[inning_half], ts, atBat_array[atBatInd][inning_half].out_number,
                  result_text, o_loc, oc_loc, o_r, ot_loc);
              drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.WHITE, 2, outline);
              thumbnail_bitmap_array[atBatInd][inning_half] = thumbnail_bitmap;
            }  // End if (ob_step > 0)
          }  // End for (int k=0; k <= atBat_array[atBatInd][inning_half].current_base; k++)
        }  // End for (int j = 0; j < atBats.length(); j++)
        // Draw line underneath last batter in inning
        --atBat_sequence_index[inning_half];
        int last_up_ind = atBat_sequence_array[atBat_sequence_index[inning_half] - 1][inning_half];
        thumbnail_bitmap = thumbnail_bitmap_array[last_up_ind][inning_half];
        thumbnail_canvas = new Canvas(thumbnail_bitmap);
        thumbnail_paint = new Paint();
        drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[inning_half], sw, bottom_line);
        thumbnail_bitmap_array[last_up_ind][inning_half] = thumbnail_bitmap;
        //Log.i(TAG, "Initialize: atBat_sequence_index[" + inning_half +"]="  + atBat_sequence_index[inning_half]);
      }  // End for (i = 0; i < innings.length(); i++)
    } catch (JSONException ex) {
      //Log.i("json Exception", ex.getMessage());
    }
    if (!game_complete) {
      // Read visitors roster
      visitor_player_info = parse_roster_json_player_info(roster_json_str, team_name[0]);
      number_players[0] = visitor_player_info.length;
      //Log.i(TAG, "visitor_player_info length: " + visitor_player_info.length);
      for (int i = 0; i < visitor_player_info.length; i++) {
        for (int j = 0; j < 3; j++) {
          //Log.i(TAG, "visitor_player_info[" + i + "][" + j + "]: " + visitor_player_info[i][j]);
          player_info[i][j][0] = visitor_player_info[i][j];
        }
      }
      // Read home team roster
      home_player_info = parse_roster_json_player_info(roster_json_str, team_name[1]);
      number_players[1] = home_player_info.length;
      //Log.i(TAG, "home_player_info length: " + home_player_info.length);
      for (int i = 0; i < home_player_info.length; i++) {
        for (int j = 0; j < 3; j++) {
          //Log.i(TAG, "home_player_info[" + i + "][" + j + "]: " + home_player_info[i][j]);
          player_info[i][j][1] = home_player_info[i][j];
        }
      }
      if (atBat_sequence_index[inning_half] < 0) { // This is here for malformed game file with no at bats but toast doesn't show!
        Toast.makeText(StartUpActivity.this, "Error in game file, exiting", Toast.LENGTH_LONG).show();
        this.finishAffinity();
        finish();
        System.exit(0);
      }
      next_up[inning_half] = atBat_sequence_array[atBat_sequence_index[inning_half]][inning_half];
      //Log.i(TAG, "Initialize1: Before Main inning=" + inning + " inning_half=" + inning_half +
      //   " next_up[" + inning_half + "]=" + next_up[inning_half] + " atBat_state_array[next_up[inning_half]][inning_half]=" + atBat_state_array[next_up[inning_half]][inning_half]);
      // Increment to next inning half and start MainActivity with appropriate team up
      if (inning_half == 1) { inning_half = 0; inning++; } else inning_half = 1;
      next_up[inning_half] = atBat_sequence_array[atBat_sequence_index[inning_half]][inning_half];
      atBat_state_array[next_up[inning_half]][inning_half] = 1;
      thumbnail_bitmap = thumbnail_bitmap_array[next_up[inning_half]][inning_half];
      thumbnail_canvas = new Canvas(thumbnail_bitmap);
      thumbnail_paint = new Paint();
      drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[inning_half], 2, outline);
      thumbnail_bitmap_array[next_up[inning_half]][inning_half] = thumbnail_bitmap;
      //Log.i(TAG, "Initialize2: Before Main inning=" + inning + " inning_half=" + inning_half +
      //  " next_up[" + inning_half + "]=" + next_up[inning_half]
      //  + " atBat_state_array[next_up[inning_half]][inning_half]=" + atBat_state_array[next_up[inning_half]][inning_half]);
    }  // End if (!game_complete)
    else { // If game_complete just draw last half inning with complete game header
      //Log.i(TAG, "Initialize3: Before Main inning=" + inning + " inning_half=" + inning_half +
      // " next_up[" + inning_half + "]=" + next_up[inning_half]
      // + " atBat_state_array[next_up[inning_half]][inning_half]=" + atBat_state_array[next_up[inning_half]][inning_half]);
      game_over = true;
    }
    team_up = inning_half;
    intent.putExtra("TeamIndex", inning_half);
    //Log.i(TAG, "In initialize_from_game_file: starting MainActivity");
    startActivityForResult(intent, ACTIVITY_MAIN);
  }  // End initialize_from_game_file

  static public void create_canvas(Bitmap bitmap, int background_color, int left, int top, int right,
    int bottom, int text_color, int text_size, String text, int text_x, int text_y, boolean monospace) {
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setColor(background_color);
    canvas.drawRect(left, top, right, bottom, paint);
    paint.setColor(text_color);
    paint.setTextSize(text_size);
    if (monospace) paint.setTypeface(Typeface.MONOSPACE);
    paint.setFakeBoldText(true);
    canvas.drawText(text, text_x, text_y, paint);
    return;
  }

  static public void drawBasePaths(Canvas canvas, Paint paint, int color, int stroke_width, float[] bases, int number_of_bases, boolean fill){
    paint.setColor(color);
    paint.setStrokeWidth(stroke_width);
    if (fill) paint.setStyle(Paint.Style.FILL); else paint.setStyle(Paint.Style.STROKE);
    Path path = new Path();
    path.moveTo(bases[0], bases[1]);
    for (int i=1; i <= number_of_bases; i++) {
      path.lineTo(bases[2*i], bases[2*i+1]);
    }
    canvas.drawPath(path, paint);
    paint.setStyle(Paint.Style.FILL); // Need to do this to prevent munging of subsequent markHit (why?)
  }

  static public void drawField(Canvas canvas, Paint paint, int color, int stroke_width, float canvas_width, float canvas_height,
       float margin_x, float margin_y) {
    // Assumes canvas_width = 1.414 * canvas_height
    float s = .5f * canvas_width;  // side of full right triangle formed by baseline and side of canvas without margins
    float r = canvas_height;       // radius of arc that makes outfield wall
    float sp = .707f * (r - margin_y);  // sprime - s with margins included
    paint.setColor(color);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(stroke_width);
    Path path = new Path();
    path.moveTo(s, r-margin_y);
    path.lineTo(s-sp, r-sp);
    path.addArc(s-r+margin_x, margin_y, s+r-margin_x, 2*r-margin_y, 225f, 90f);
    path.lineTo(s, r-margin_y);
    canvas.drawPath(path, paint);
  }

  static public void drawLinesColor(Canvas canvas, Paint paint, int color, int stroke_width, float[] points){
    paint.setColor(color);
    paint.setStrokeWidth(stroke_width);
    canvas.drawLines(points, paint);
  }

  static public void markHit(Canvas canvas, Paint paint, int color, int text_size, String text, float[] location){
    //Log.i(TAG, "text_size=" + text_size + " location=" + location[0] +", " + location[1]);
    paint.setColor(color);
    paint.setTextSize(text_size);
    paint.setTypeface(Typeface.DEFAULT_BOLD);
    paint.setStyle(Paint.Style.FILL);
    canvas.drawText(text, location[0], location[1], paint);
  }

  static public void markOut(Canvas canvas, Paint paint, int color, int text_size, int outs, String out_text, float[] outs_loc,
                             float[] outs_circle_loc, float radius, float[] text_loc){
    paint.setColor(color);
    paint.setTextSize(text_size);
    paint.setTypeface(Typeface.DEFAULT_BOLD);
    canvas.drawText(Integer.toString(outs), outs_loc[0], outs_loc[1], paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(2);
    canvas.drawCircle(outs_circle_loc[0], outs_circle_loc[1], radius, paint);
    paint.setStyle(Paint.Style.FILL);
    canvas.drawText(out_text, text_loc[0], text_loc[1], paint);
  }

  static public void hideNavigation(View decorView){
    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    //Log.i(TAG, "In onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);
    if (requestCode == ACTIVITY_MAIN) {
      //Log.i(TAG, "Return from MainActivity with requestCode=" + requestCode + ", exiting");
      try {
        game_file_bufferedWriter.close();
        //Log.i(TAG, "Closed game file");
      } catch (IOException ex) {
        //Log.i(TAG, "Close game file IOException " + ex.getMessage());
        Toast.makeText(StartUpActivity.this, "close game file IO Exception" + ex.getMessage(), Toast.LENGTH_SHORT).show();
      }
      this.finishAffinity();
      finish();
      System.exit(0);
    }
    else if (requestCode == ACTIVITY_SETTINGS) {
      int new_number_batters = 0;
      int new_number_innings_regulation = 0;
      if (resultCode == RESULT_OK) {
        new_number_innings_regulation= data.getIntExtra("new_number_innings_regulation", 0);
        new_number_batters= data.getIntExtra("new_number_batters", 0);
        if (new_number_batters == 0) new_number_batters = number_batters;
        if (new_number_innings_regulation == 0) new_number_innings_regulation = number_innings_regulation;
        track_b_s = data.getBooleanExtra("track_b_s", false);
        initialize_vars(new_number_batters, new_number_innings_regulation);
      }
      //Log.i(TAG, "Return from SettingsActivity: resultCode=" + resultCode + " new_number_batters=" + new_number_batters
      //  + " new_number_innings_regulation=" + new_number_innings_regulation + " track_b_s=" + track_b_s);
    }
  }  // End onActivityResult

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.startup_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        //region
        Intent intent_settings = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent_settings, ACTIVITY_SETTINGS);
        return true;
        //endregion
      case R.id.action_open_game_file:
        //region
        //Log.i(TAG, "Open game file selected. Directory path=" + directory_path);
        String[] filenames = new File(directory_path).list();
        for (String filename : filenames) Log.i(TAG, "game_filename: " + filename);
        final PopupMenu game_file_popup = new PopupMenu(this, findViewById(R.id.popup_insert_point));
        for (String filename : filenames) if (filename.startsWith("20")) game_file_popup.getMenu().add(filename);
        game_file_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            String game_filename = item.getTitle().toString();
            File game_file = new File(directory_path + game_filename);
            String game_file_json_str = read_file(directory_path, game_filename);
            if (!game_file_json_str.contains("GameOver")) {  // Continuing game, make game file writable, write contents back to it
              Toast.makeText(StartUpActivity.this, "Continuing game from file " +  game_filename, Toast.LENGTH_SHORT).show();
              game_complete = false;
              try {
                game_file_bufferedWriter = new BufferedWriter(new FileWriter(game_file));
                game_file_bufferedWriter.write(game_file_json_str);
                game_file_bufferedWriter.flush();
              }  catch (IOException ex) {
                //Log.i(TAG, "game file IOException" + ex.getMessage());
                Toast.makeText(StartUpActivity.this, "game file IOException" + ex.getMessage(), Toast.LENGTH_SHORT).show();
              }
              game_file_json_str += "]}\n";  // Add the final json closing brackets to enable parsing
            }  // End if (!game_file_json_str.contains("GameOver"))
            else {  // Game file contains complete game
              Toast.makeText(StartUpActivity.this, "Reading complete game from file " +  game_filename, Toast.LENGTH_SHORT).show();
              game_complete = true;
            }
            int json_len = game_file_json_str.length();
            //Log.i(TAG, "game_file_json_str final length=" + json_len);
            //Log.i(TAG,  "game_file_json_str final first part=" + game_file_json_str);
            if (json_len > 3000) //Log.i(TAG,  "game_file_json_str final last part=" + game_file_json_str.substring(json_len-3000));
            initialize_from_game_file(game_file_json_str, game_complete, intent);
            return true;
          }
        });
        game_file_popup.show();
        break;
        //endregion
      case R.id.action_exit:
        //region
        try {
          game_file_bufferedWriter.close();
          //Log.i(TAG, "Closed game file");
        } catch (IOException ex) {
          //Log.i(TAG, "Close game file IOException " + ex.getMessage());
          Toast.makeText(StartUpActivity.this, "close game file IO Exception" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "Exiting", Toast.LENGTH_SHORT).show();
        this.finishAffinity();
        finish();
        System.exit(0);
        break;
        // endregion
      default:
        break;
    }
    return true;
  }  // End onOptionsItemSelected

  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    hideNavigation(getWindow().getDecorView());
  }
} // End StartUpActivity
