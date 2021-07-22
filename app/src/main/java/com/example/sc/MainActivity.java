package com.example.sc;

/*
MainActivity.java - main screen of Scorecard application

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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static java.lang.Math.*;

import static com.example.sc.StartUpActivity.atBat;
import static com.example.sc.StartUpActivity.atBat_array;
import static com.example.sc.StartUpActivity.atBat_sequence_array;
import static com.example.sc.StartUpActivity.atBat_sequence_index;
import static com.example.sc.StartUpActivity.base_text_size;
import static com.example.sc.StartUpActivity.batter_pa;
import static com.example.sc.StartUpActivity.bottom_line;
import static com.example.sc.StartUpActivity.create_canvas;
import static com.example.sc.StartUpActivity.atBat_state_array;
import static com.example.sc.StartUpActivity.current_pitcher_index;
import static com.example.sc.StartUpActivity.date_str;
import static com.example.sc.StartUpActivity.density;
import static com.example.sc.StartUpActivity.drawLinesColor;
import static com.example.sc.StartUpActivity.game_file;
import static com.example.sc.StartUpActivity.game_file_bufferedWriter;
import static com.example.sc.StartUpActivity.heightPx;
import static com.example.sc.StartUpActivity.hideNavigation;
import static com.example.sc.StartUpActivity.inning;
import static com.example.sc.StartUpActivity.inning_half;
import static com.example.sc.StartUpActivity.inning_half_batters;
import static com.example.sc.StartUpActivity.pitcher_balls;
import static com.example.sc.StartUpActivity.pitcher_pitches;
import static com.example.sc.StartUpActivity.pitcher_strikes;
import static com.example.sc.StartUpActivity.real_aspect_ratio;
import static com.example.sc.StartUpActivity.right_line;
import static com.example.sc.StartUpActivity.next_up;
import static com.example.sc.StartUpActivity.number_atBats;
import static com.example.sc.StartUpActivity.number_batters;
import static com.example.sc.StartUpActivity.number_innings;
import static com.example.sc.StartUpActivity.number_innings_regulation;
import static com.example.sc.StartUpActivity.number_players;
import static com.example.sc.StartUpActivity.outline;
import static com.example.sc.StartUpActivity.outs;
import static com.example.sc.StartUpActivity.on_base;
import static com.example.sc.StartUpActivity.pitcher_batters_faced;
import static com.example.sc.StartUpActivity.pitcher_bb;
import static com.example.sc.StartUpActivity.pitcher_earned_runs;
import static com.example.sc.StartUpActivity.pitcher_errors;
import static com.example.sc.StartUpActivity.pitcher_hits;
import static com.example.sc.StartUpActivity.pitcher_k;
import static com.example.sc.StartUpActivity.pitcher_number_names;
import static com.example.sc.StartUpActivity.pitcher_outs;
import static com.example.sc.StartUpActivity.pitcher_runs;
import static com.example.sc.StartUpActivity.player_info;
import static com.example.sc.StartUpActivity.batter_number_names;
import static com.example.sc.StartUpActivity.batter_position;
import static com.example.sc.StartUpActivity.batter_abs;
import static com.example.sc.StartUpActivity.batter_runs;
import static com.example.sc.StartUpActivity.batter_hits;
import static com.example.sc.StartUpActivity.batter_rbi;
import static com.example.sc.StartUpActivity.batter_errors;
import static com.example.sc.StartUpActivity.sw;
import static com.example.sc.StartUpActivity.team_name;
import static com.example.sc.StartUpActivity.team_color;
import static com.example.sc.StartUpActivity.team_abs;
import static com.example.sc.StartUpActivity.team_runs;
import static com.example.sc.StartUpActivity.team_hits;
import static com.example.sc.StartUpActivity.team_rbi;
import static com.example.sc.StartUpActivity.team_errors;
import static com.example.sc.StartUpActivity.team_up;
import static com.example.sc.StartUpActivity.thumbnail_bitmap_array;
import static com.example.sc.StartUpActivity.inning_header_values;
import static com.example.sc.StartUpActivity.inning_runs;
import static com.example.sc.StartUpActivity.inning_hits;
import static com.example.sc.StartUpActivity.inning_lob;
import static com.example.sc.StartUpActivity.game_over;
import static com.example.sc.StartUpActivity.thumbnail_size;
import static com.example.sc.StartUpActivity.track_b_s;
import static com.example.sc.StartUpActivity.widthPx;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();
  
  final static int ACTIVITY_ATBAT = 0, ACTIVITY_PITCHING = 1, ACTIVITY_ADDBATTER = 2, ACTIVITY_ADDPITCHER = 3;
  static int team_displayed = 0, atBatInd;
  static boolean exit_selected_once = false;

  Canvas canvas;
  Bitmap thumbnail_bitmap, thumbnail_bitmap_next_batter;
  Canvas thumbnail_canvas;
  Paint thumbnail_paint;
  TextView msg_textbox_tv;
  TextView score_textbox_tv;
  Menu options_menu;

  int batterIdx, teamIdx;
  String batter_name, batter_number_name;
  String pitcher_name, pitcher_number_name;

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    hideNavigation(getWindow().getDecorView());

    team_displayed = getIntent().getIntExtra("TeamIndex", 0);
    //Log.i(TAG, " In Main: team name: " + team_name[team_displayed] + ", team up=" + team_name[team_up]);
    
    drawMain(team_displayed);
  }

  public void drawMain(final int team_displayed) {
    //Log.i(TAG, "In drawMain w/ team_displayed=" + team_name[team_displayed] + " team up=" + team_name[team_up] + " inning=" + inning);

    final Intent intent_atbat = new Intent(this, AtBatActivity.class);
    final Intent intent_addbatter = new Intent(this, AddBatterActivity.class);
    final Intent intent_addpitcher = new Intent(this, AddPitcherActivity.class);

    final int DarkerGray = ResourcesCompat.getColor(getResources(), R.color.DarkerGray, null);

    // region Initialize dimensions and bitmaps

    final int scoreboard_teams_layout_width = (int) round(0.24 * widthPx);
    int scoreboard_layout_width = (int) round(0.32 * widthPx);
    if (real_aspect_ratio > 2.0) scoreboard_layout_width = (int) round(scoreboard_layout_width * 0.8);  // Cut #innings shown for extra wide devices
    final int scoreboard_rhe_layout_width = (int) round(0.10 * widthPx);
    final int scoreboard_pitchers_layout_width = (int) round(0.24 * widthPx);
    final int pitch_count_layout_width = (int) round(0.15 * widthPx);

    final int top_layout_height = (int) round(0.18 * heightPx);
    final int inning_header_height = (int) round(0.05 * heightPx);
    final int main_layout_height = (int) round(0.72 * heightPx);      // AtBat thumbnails only - used in StartUp to calculate thumbnail size
    final int main_scrollview_height = (int) round(0.82 * heightPx);  // Includes inning header and footer
    final int inning_footer_height = (int) round(0.05 * heightPx);

    final int batters_layout_width = (int) round(0.24 * widthPx);
    final int positions_layout_width = (int) round(0.04 * widthPx);
    int main_layout_width = (int) round(0.54 * widthPx);
    if (real_aspect_ratio > 2.0) main_layout_width = (int) round(main_layout_width * 0.8);  // Cut #innings shown for extra wide devices
    final int sums_layout_width = (int) round(0.18 * widthPx);

    //Log.i(TAG, "Dimension: scoreboard_teams_layout_width=" + scoreboard_teams_layout_width +
    //  " scoreboard_layout_width=" + scoreboard_layout_width);
    //Log.i(TAG, "Dimension: scoreboard_rhe_layout_width=" + scoreboard_rhe_layout_width +
    //  " scoreboard_pitchers_layout_width=" + scoreboard_pitchers_layout_width + " pitch_count_layout_width=" + pitch_count_layout_width);
    //Log.i(TAG, "Dimension: main_layout_width=" + main_layout_width + " main_scrollview_height=" + main_scrollview_height);
    //Log.i(TAG, "Dimension: batters_layout_width=" + batters_layout_width + " positions_layout_width=" + positions_layout_width);

    ImageView[] scoreboard_teams = new ImageView[3];
    Bitmap[] scoreboard_teams_bitmaps = new Bitmap[3];
    ImageView[] scoreboard = new ImageView[3*number_innings];
    Bitmap[] scoreboard_bitmaps = new Bitmap[3*number_innings];
    ImageView[] scoreboard_rhe = new ImageView[3];
    Bitmap[] scoreboard_rhe_bitmaps = new Bitmap[3];
    ImageView[] pitchers = new ImageView[3];
    final Bitmap[] pitcher_bitmaps = new Bitmap[3];
    ImageView[] pitch_counts = new ImageView[3];
    Bitmap[] pitch_count_bitmaps = new Bitmap[3];
    ImageView[] inning_headers = new ImageView[number_innings];
    Bitmap[] inning_header_bitmaps = new Bitmap[number_innings];
    ImageView[] inning_footers = new ImageView[number_innings];
    Bitmap[] inning_footer_bitmaps = new Bitmap[number_innings];
    ImageView[] batters = new ImageView[number_batters + 2];
    final Bitmap[] batter_bitmaps = new Bitmap[number_batters + 2];
    ImageView[] positions = new ImageView[number_batters + 2];
    final Bitmap[] position_bitmaps = new Bitmap[number_batters + 2];
    ImageView[] sums = new ImageView[number_batters + 2];
    Bitmap[] sum_bitmaps = new Bitmap[number_batters + 2];
    ImageView[] thumbnail_array = new ImageView[number_atBats];
    // endregion

    // region Write title
    String home_visitor, team_str, title;
    if (team_displayed == 0) home_visitor = "Visitors";
    else home_visitor = "Home Team";
    team_str = team_name[team_displayed] + " - " + home_visitor;
    title = String.format("%-38s %s", team_str, date_str);
    this.setTitle(title);
    this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(team_color[team_displayed]));
    // endregion

    // region Set up batter and position popup menus
    final PopupMenu batter_popup = new PopupMenu(this, findViewById(R.id.batter_insert_point));
    //Log.i(TAG, "number of players=" + number_players[team_displayed]);
    batter_popup.getMenu().add("    New");
    for (int i=0; i < number_players[team_displayed]; i++){
      //Log.i(TAG, "player: " + player_info[i][0][team_displayed] + "  " + player_info[i][1][team_displayed] + "  " + player_info[i][2][team_displayed]);
      batter_popup.getMenu().add(String.format("%3d %s",Integer.parseInt(player_info[i][0][team_displayed]), player_info[i][1][team_displayed]));
    }

    final PopupMenu position_popup = new PopupMenu(this, findViewById(R.id.batter_insert_point));
    position_popup.getMenu().add("P");
    position_popup.getMenu().add("C");
    position_popup.getMenu().add("1B");
    position_popup.getMenu().add("2B");
    position_popup.getMenu().add("3B");
    position_popup.getMenu().add("SS");
    position_popup.getMenu().add("LF");
    position_popup.getMenu().add("CF");
    position_popup.getMenu().add("RF");
    position_popup.getMenu().add("DH");
    position_popup.getMenu().add("LC");
    position_popup.getMenu().add("RC");
    // endregion

    // region Set up two pitcher popup menus (visitors and home) - use all batters
    final PopupMenu pitcher_popup_visitors = new PopupMenu(this, findViewById(R.id.batter_insert_point));
    pitcher_popup_visitors.getMenu().add("    New");
    for (int i=0; i < number_players[0]; i++){
      pitcher_popup_visitors.getMenu().add(String.format("%3d %s",Integer.parseInt(player_info[i][0][0]), player_info[i][1][0]));
    }

    final PopupMenu pitcher_popup_home = new PopupMenu(this, findViewById(R.id.batter_insert_point));
    pitcher_popup_home.getMenu().add("    New");
    for (int i=0; i < number_players[1]; i++){
      pitcher_popup_home.getMenu().add(String.format("%3d %s",Integer.parseInt(player_info[i][0][1]), player_info[i][1][1]));
    }
    // endregion

    // region Set up scoreboard
    final int scoreboard_row_height = (int) floor(top_layout_height/3.0);
    int scoreboard_cell_width = (int) floor(scoreboard_layout_width/10.0);

    GridLayout scoreboardTeamsLayout = findViewById(R.id.scoreboard_teams_layout);
    scoreboardTeamsLayout.removeAllViews();
    scoreboardTeamsLayout.setRowCount(3);
    for (int row = 0; row < 3; row++) {
      scoreboard_teams_bitmaps[row] = Bitmap.createBitmap(scoreboard_teams_layout_width, scoreboard_row_height, Bitmap.Config.ARGB_8888);
      scoreboard_teams[row] = new ImageView(this);
      scoreboard_teams[row].setImageBitmap(scoreboard_teams_bitmaps[row]);
      if (row == 1) create_canvas(scoreboard_teams_bitmaps[row], Color.WHITE, 0, 0, scoreboard_teams_layout_width, scoreboard_row_height, team_color[0],
          base_text_size, team_name[0], 0, (int) floor(.67 * scoreboard_row_height), false);
      else if (row == 2) create_canvas(scoreboard_teams_bitmaps[row], Color.WHITE, 0, 0, scoreboard_teams_layout_width, scoreboard_row_height, team_color[1],
          base_text_size, team_name[1], 0, (int) floor(.67 * scoreboard_row_height), false);
      scoreboardTeamsLayout.addView(scoreboard_teams[row]);
    }

    String scoreboard_cell_value = "";
    HorizontalScrollView scoreboardScrollViewLayout = findViewById(R.id.scoreboard_scrollview_layout);
    scoreboardScrollViewLayout.setLayoutParams(new LinearLayout.LayoutParams(scoreboard_layout_width, top_layout_height));
    GridLayout scoreboardLayout = findViewById(R.id.scoreboard_grid_layout);
    scoreboardLayout.removeAllViews();
    scoreboardLayout.setRowCount(3);
    for (int i = 0; i < number_innings; i++) {
      //Log.i(TAG, "scoreboard: i=" + i + " inning=" + inning + " team_up=" + team_up +
      // " inning_runs[i][0]=" + inning_runs[i][0] + " inning_runs[i][1]=" + inning_runs[i][1]);
      for (int row = 0; row < 3; row++) {
        scoreboard_bitmaps[3*i + row] = Bitmap.createBitmap(scoreboard_cell_width, scoreboard_row_height, Bitmap.Config.ARGB_8888);
        scoreboard[3*i + row] = new ImageView(this);
        scoreboard[3*i + row].setImageBitmap(scoreboard_bitmaps[3*i + row]);
        if (row == 0) {
          if (i < max(number_innings_regulation, inning)) scoreboard_cell_value = Integer.toString(i + 1); else scoreboard_cell_value = "";
          create_canvas(scoreboard_bitmaps[3*i + row], Color.WHITE, 0, 0, scoreboard_cell_width - 2,
              scoreboard_row_height - 2, team_color[team_displayed], base_text_size, scoreboard_cell_value,
              (int) floor(.4 * scoreboard_cell_width), (int) floor(.67 * scoreboard_row_height), true);
        }
        else if (row == 1) {  // Visitors
          if (i < inning-1) scoreboard_cell_value = String.format("%2s", inning_runs[i][0]);
          else if (i == inning-1 && team_up == 1) scoreboard_cell_value = String.format("%2s", inning_runs[i][0]);
          else if (i == inning-1 && team_up == 0 && inning_runs[i][0] == 0) scoreboard_cell_value = "-";
          else if (i == inning-1 && team_up == 0 && inning_runs[i][0] > 0) scoreboard_cell_value = String.format("%2s", inning_runs[i][0]);
          else scoreboard_cell_value = "";
          create_canvas(scoreboard_bitmaps[3*i + row], Color.LTGRAY, 0, 0, scoreboard_cell_width - 2,
              scoreboard_row_height - 2, Color.BLACK, base_text_size, scoreboard_cell_value, 0,
              (int) floor(.67 * scoreboard_row_height), true);
        }
        else {  // Home team
          if (i < inning-1) scoreboard_cell_value = String.format("%2s", inning_runs[i][1]);
          else if (i == inning-1 && team_up == 1 && inning_runs[i][1] == 0) scoreboard_cell_value = "-";
          else if (i == inning-1 && team_up == 1 && inning_runs[i][1] > 0) scoreboard_cell_value = String.format("%2s", inning_runs[i][1]);
          else scoreboard_cell_value = "";
          create_canvas(scoreboard_bitmaps[3*i + row], Color.LTGRAY, 0, 0, scoreboard_cell_width - 2,
              scoreboard_row_height - 2 , Color.BLACK,base_text_size, scoreboard_cell_value, 0,
              (int) floor(.67 * scoreboard_row_height), true);
        }
        scoreboardLayout.addView(scoreboard[3*i + row]);
      }  // End for (int row = 0; row < 3; row++)
    }  // End for (int i = 0; i < number_innings; i++)

    String scoreboard_rhe_value = "";
    GridLayout scoreboardRHELayout = findViewById(R.id.scoreboard_rhe_layout);
    scoreboardRHELayout.removeAllViews();
    scoreboardRHELayout.setRowCount(3);
    for (int row = 0; row < 3; row++) {
      scoreboard_rhe_bitmaps[row] = Bitmap.createBitmap(scoreboard_rhe_layout_width, scoreboard_row_height, Bitmap.Config.ARGB_8888);
      scoreboard_rhe[row] = new ImageView(this);
      scoreboard_rhe[row].setImageBitmap(scoreboard_rhe_bitmaps[row]);
      if (row == 0) {
        create_canvas(scoreboard_rhe_bitmaps[row], Color.WHITE, 0, 0, scoreboard_rhe_layout_width - 2,
            scoreboard_row_height - 2, team_color[team_displayed], base_text_size, " R  H  E",
            0, (int) floor(.67 * scoreboard_row_height), true);
      }
      else if (row == 1) {  // Visitors
        scoreboard_rhe_value = String.format("%2s%3s%3s", team_runs[0], team_hits[0], team_errors[0]);
        create_canvas(scoreboard_rhe_bitmaps[row], Color.LTGRAY, 0, 0, scoreboard_rhe_layout_width - 2,
            scoreboard_row_height - 2, Color.BLACK, base_text_size, scoreboard_rhe_value,
            0, (int) floor(.67 * scoreboard_row_height), true);
      } else {  // Home team
        scoreboard_rhe_value = String.format("%2s%3s%3s", team_runs[1], team_hits[1], team_errors[1]);
        create_canvas(scoreboard_rhe_bitmaps[row], Color.LTGRAY, 0, 0, scoreboard_rhe_layout_width - 2,
            scoreboard_row_height - 2, Color.BLACK, base_text_size, scoreboard_rhe_value,
            0, (int) floor(.67 * scoreboard_row_height), true);
      }
      scoreboardRHELayout.addView(scoreboard_rhe[row]);
    }  // End for (int row = 0; row < 3; row++)

    GridLayout scoreboardPitchersLayout = findViewById(R.id.scoreboard_pitchers_layout);
    scoreboardPitchersLayout.removeAllViews();
    scoreboardPitchersLayout.setRowCount(3);
    for (int row = 0; row < 3; row++) {
      pitcher_bitmaps[row] = Bitmap.createBitmap(scoreboard_pitchers_layout_width, scoreboard_row_height, Bitmap.Config.ARGB_8888);
      pitchers[row] = new ImageView(this);
      pitchers[row].setImageBitmap(pitcher_bitmaps[row]);
      if (row == 0) create_canvas(pitcher_bitmaps[row], Color.WHITE, 2, 2, scoreboard_pitchers_layout_width-2,
          scoreboard_row_height-2, team_color[team_displayed], base_text_size, "Pitching",
          (int) floor(.05 * scoreboard_pitchers_layout_width), (int) floor(.67 * scoreboard_row_height), false);
      else if (row == 1) create_canvas(pitcher_bitmaps[row], DarkerGray,2, 2, scoreboard_pitchers_layout_width-2,
          scoreboard_row_height-2, team_color[0], base_text_size, pitcher_number_names[current_pitcher_index[0]][0],
          0, (int) floor(.67 * scoreboard_row_height), false);
      else create_canvas(pitcher_bitmaps[row], DarkerGray, 2, 2, scoreboard_pitchers_layout_width-2,
          scoreboard_row_height-2,team_color[1], base_text_size, pitcher_number_names[current_pitcher_index[1]][1], 0,
          (int) floor(.67 * scoreboard_row_height), false);
      scoreboardPitchersLayout.addView(pitchers[row]);
      if (row == 1) {
        pitchers[row].setOnClickListener(new View.OnClickListener() {
          public void onClick(final View v) {
            final int rowId = (v.getId());
            pitcher_popup_visitors.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                String pitcher_number_name = item.getTitle().toString();
                String pitcher_name = pitcher_number_name.substring(4);
                teamIdx = 0;
                if (pitcher_name.equals("New")) startActivityForResult(intent_addpitcher, ACTIVITY_ADDPITCHER);
                if (pitcher_batters_faced[current_pitcher_index[0]][0] > 0)   // This is a substitution if previous pitcher in slot has faced at least 1 scoreboard_pitcher
                  ++current_pitcher_index[0];
                pitcher_number_names[current_pitcher_index[0]][0] = pitcher_number_name;
                //Log.i(TAG, "pitcher_number_name: " + pitcher_number_names[current_pitcher_index[0]][0]);
                create_canvas(pitcher_bitmaps[1], DarkerGray, 2, 2, scoreboard_pitchers_layout_width-2, scoreboard_row_height-2,
                    team_color[0], base_text_size, pitcher_number_name, 0, (int) floor(.67 * scoreboard_row_height),false);
                v.invalidate();
                return true;
              }
            });
            pitcher_popup_visitors.show();
          }
        });
      }  // End if (row == 1)
      if (row == 2) {
        pitchers[row].setOnClickListener(new View.OnClickListener() {
          public void onClick(final View v) {
            pitcher_popup_home.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                String pitcher_number_name = item.getTitle().toString();
                String pitcher_name = pitcher_number_name.substring(4);
                teamIdx = 1;
                if (pitcher_name.equals("New")) startActivityForResult(intent_addpitcher, ACTIVITY_ADDPITCHER);
                if (pitcher_batters_faced[current_pitcher_index[1]][1] > 0)   // This is a substitution if previous pitcher in slot has faced at least 1 scoreboard_pitcher
                  ++current_pitcher_index[1];
                pitcher_number_names[current_pitcher_index[1]][1] = pitcher_number_name;
                //Log.i(TAG, "pitcher_number_name: " + pitcher_number_names[current_pitcher_index[1]][1]);
                create_canvas(pitcher_bitmaps[2], DarkerGray, 2, 2, scoreboard_pitchers_layout_width-2, scoreboard_row_height-2,
                    team_color[1], base_text_size, pitcher_number_name, 0, (int) floor(.67 * scoreboard_row_height),false);
                v.invalidate();
                return true;
              }
            });
            pitcher_popup_home.show();
          }
        });
      }  // End if (row == 2)
    }  // End for (int row = 0; row < 3; row++)

    String pitch_count_str;
    GridLayout pitch_count_layout = findViewById(R.id.pitch_count_layout);
    pitch_count_layout.removeAllViews();
    pitch_count_layout.setRowCount(3);
    for (int row = 0; row < 3; row++) {
      pitch_count_bitmaps[row] = Bitmap.createBitmap(pitch_count_layout_width, scoreboard_row_height, Bitmap.Config.ARGB_8888);
      pitch_counts[row] = new ImageView(this);
      pitch_counts[row].setImageBitmap(pitch_count_bitmaps[row]);
    }
    // row 0 - outs/game over
    String outs_text;
    if (outs[team_up] == 1) outs_text = "  " + outs[team_up] +" OUT"; else outs_text = "  " + outs[team_up] +" OUTS";
    int color = team_color[team_displayed];
    if (game_over) { outs_text = "  GAME OVER"; color = Color.BLACK; }
    create_canvas(pitch_count_bitmaps[0], Color.WHITE, 0,0, pitch_count_layout_width, scoreboard_row_height, color,
      base_text_size, outs_text,0, (int) floor(.67 * scoreboard_row_height), false);
    pitch_count_layout.addView(pitch_counts[0]);

    if (track_b_s) {
      for (int row = 1; row < 3; row++) {
        if (row == 1) {  // Visitors
          pitch_count_str = pitcher_pitches[current_pitcher_index[0]][0] + "P/" + pitcher_balls[current_pitcher_index[0]][0] + "B/" +
              pitcher_strikes[current_pitcher_index[0]][0] + "S";
          create_canvas(pitch_count_bitmaps[row], Color.WHITE, 0, 0, pitch_count_layout_width,
              scoreboard_row_height, team_color[0], base_text_size, pitch_count_str,
              0, (int) floor(.67 * scoreboard_row_height), false);
        } else {  // Home team
          pitch_count_str = pitcher_pitches[current_pitcher_index[1]][1] + "P/" + pitcher_balls[current_pitcher_index[1]][1] + "B/" +
              pitcher_strikes[current_pitcher_index[1]][1] + "S";
          create_canvas(pitch_count_bitmaps[row], Color.WHITE, 0, 0, pitch_count_layout_width,
              scoreboard_row_height, team_color[1], base_text_size, pitch_count_str,
              0, (int) floor(.67 * scoreboard_row_height), false);
        }
      pitch_count_layout.addView(pitch_counts[row]);
      }  // End for (int row = 1; row < 3; row++)
    }  // End if (track_b_s)

    // endregion

    // region Set up main grid
    String inning_text = "";
    String rhlob_text = "";
    int display_inning, display_inning_prev;

    // Inning headers
    for (int i = 0; i < number_innings; i++) {
      inning_header_bitmaps[i] = Bitmap.createBitmap(thumbnail_size, inning_header_height, Bitmap.Config.ARGB_8888);
      display_inning = inning_header_values[i][team_displayed];
      inning_text = Integer.toString(display_inning);
      if (i > 0) {
        display_inning_prev = inning_header_values[i - 1][team_displayed];
        if (display_inning > max(number_innings_regulation, inning) || display_inning == display_inning_prev) inning_text = "";
      }
      create_canvas(inning_header_bitmaps[i], Color.WHITE, 0, 0, thumbnail_size, inning_header_height,
          team_color[team_displayed], base_text_size, inning_text, (int) floor(.4 * thumbnail_size),
          (int) floor(.67 * inning_header_height), true);
    }

    // Inning footers
    for (int i = 0; i < number_innings; i++) {
      inning_footer_bitmaps[i] = Bitmap.createBitmap(thumbnail_size, inning_footer_height, Bitmap.Config.ARGB_8888);
      display_inning = inning_header_values[i][team_displayed];
      //Log.i(TAG, "display_inning=" + display_inning + " hits=" + inning_hits[display_inning-1][team_displayed]);
      rhlob_text = inning_runs[display_inning-1][team_displayed] + "/" + inning_hits[display_inning-1][team_displayed] +
          "/" + inning_lob[display_inning-1][team_displayed];
      if (i > 0) {
        display_inning_prev = inning_header_values[i - 1][team_displayed];
        if (display_inning > max(number_innings_regulation, inning) || display_inning == display_inning_prev) rhlob_text = "";
      }
      int small_text_size =  (int) round(0.75 * base_text_size);
      if (real_aspect_ratio > 2.0) small_text_size = (int) round(0.7 * small_text_size);
      create_canvas(inning_footer_bitmaps[i], Color.WHITE, 0, 0, thumbnail_size, inning_footer_height, team_color[team_displayed],
          small_text_size, rhlob_text,2, (int) floor(.67 * inning_footer_height),true);
    }

    batter_bitmaps[0] = Bitmap.createBitmap(batters_layout_width, inning_header_height, Bitmap.Config.ARGB_8888);
    create_canvas(batter_bitmaps[0], Color.WHITE, 0, 0, batters_layout_width, inning_header_height,
            team_color[team_displayed], base_text_size, "", 0, (int) floor(.67 * inning_footer_height),false);

    // Batters
    for (int i = 0; i < number_batters; i++) {
      batter_bitmaps[i+1] = Bitmap.createBitmap(batters_layout_width, thumbnail_size, Bitmap.Config.ARGB_8888);
      create_canvas(batter_bitmaps[i+1], DarkerGray, 2, 2, batters_layout_width - 2, thumbnail_size - 2,
              team_color[team_displayed], base_text_size , i+1 + ")  " +
              batter_number_names[i][team_displayed], 10, (int) floor(.6 * thumbnail_size),false);
      //Log.i(TAG, "bnn[" + i + "]=" + batter_number_names[i][team_displayed]);
    }

    batter_bitmaps[number_batters + 1] = Bitmap.createBitmap(batters_layout_width, inning_footer_height, Bitmap.Config.ARGB_8888);
    create_canvas(batter_bitmaps[number_batters + 1], Color.WHITE, 0, 0, batters_layout_width, inning_footer_height,
            team_color[team_displayed], base_text_size, "TOTALS (R/H/LOB)", 4, (int) floor(.67 * inning_footer_height),false);

    position_bitmaps[0] = Bitmap.createBitmap(positions_layout_width, inning_header_height, Bitmap.Config.ARGB_8888);
    create_canvas(position_bitmaps[0], Color.WHITE, 0, 0, positions_layout_width, inning_header_height,
            team_color[team_displayed], base_text_size, "POS", 0, (int) floor(.67 * inning_footer_height),false);

    // Positions
    for (int i = 0; i < number_batters; i++) {
      position_bitmaps[i + 1] = Bitmap.createBitmap(positions_layout_width, thumbnail_size, Bitmap.Config.ARGB_8888);
      create_canvas(position_bitmaps[i +1], DarkerGray, 2, 2, positions_layout_width - 2, thumbnail_size - 2,
          team_color[team_displayed], base_text_size , batter_position[i][team_displayed], round(7*density),
          (int) floor(.6 * thumbnail_size),false);
    }

    position_bitmaps[number_batters + 1] = Bitmap.createBitmap(positions_layout_width, inning_footer_height, Bitmap.Config.ARGB_8888);
    create_canvas(position_bitmaps[number_batters + 1], Color.WHITE, 0, 0, positions_layout_width, inning_footer_height,
            team_color[team_displayed], base_text_size, "", 0, (int) floor(.67 * inning_footer_height),false);

    sum_bitmaps[0] = Bitmap.createBitmap(sums_layout_width, inning_header_height, Bitmap.Config.ARGB_8888);
    create_canvas(sum_bitmaps[0], Color.WHITE, 0, 0, sums_layout_width - 2, inning_header_height,
            team_color[team_displayed], base_text_size, " AB  R  H RBI E", 0, (int) floor(.67 * inning_header_height), true);

    for (int i = 0; i < number_batters; i++) {
      sum_bitmaps[i + 1] = Bitmap.createBitmap(sums_layout_width, thumbnail_size, Bitmap.Config.ARGB_8888);
      String batter_stats_str = String.format("%3d%3d%3d%3d%3d", batter_abs[i][team_displayed], batter_runs[i][team_displayed],
              batter_hits[i][team_displayed], batter_rbi[i][team_displayed], batter_errors[i][team_displayed]);
      create_canvas(sum_bitmaps[i + 1], DarkerGray, 0, 0, sums_layout_width - 2, thumbnail_size - 2,
              team_color[team_displayed], base_text_size, batter_stats_str, 0, (int) floor(.6 * thumbnail_size), true);
    }

    sum_bitmaps[number_batters + 1] = Bitmap.createBitmap(sums_layout_width, inning_footer_height, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(sum_bitmaps[number_batters + 1]);
    String team_stats_str = String.format("%3d%3d%3d%3d%3d", team_abs[team_displayed], team_runs[team_displayed],
            team_hits[team_displayed], team_rbi[team_displayed], team_errors[team_displayed]);
    create_canvas(sum_bitmaps[number_batters + 1], Color.WHITE, 0, 0, sums_layout_width-2, inning_footer_height,
            team_color[team_displayed], base_text_size, team_stats_str, 0, (int) floor(.67 * inning_footer_height), true);

    GridLayout batterGridLayout = findViewById(R.id.batter_grid_layout);
    batterGridLayout.removeAllViews();
    batterGridLayout.setRowCount(number_batters + 2);
    for (int i = 0; i < number_batters + 2; i++) {
      batters[i] = new ImageView(this);
      batters[i].setId(i);
      batters[i].setImageBitmap(batter_bitmaps[i]);
      batterGridLayout.addView(batters[i]);
      if (i > 0 && i < number_batters + 1) {
        batters[i].setOnClickListener(new View.OnClickListener() {
          public void onClick(final View v) { 
            int batterId = (v.getId());
            batterIdx = batterId - 1;
            //Log.i(TAG, "batter " + batterIdx + " selected");
            batter_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(MainActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                batter_number_name = item.getTitle().toString();
                batter_name = batter_number_name.substring(4);
                if (batter_name.equals("New")) {
                  startActivityForResult(intent_addbatter, ACTIVITY_ADDBATTER);
                }
                else {
                  int i = 0;
                  while (!player_info[i++][1][team_displayed].equals(batter_name)){}  // Locate batter in player_info
                  batter_position[batterIdx][team_displayed] = player_info[i-1][2][team_displayed];
                }
                batter_number_names[batterIdx][team_displayed] = batter_number_name;
                //Log.i(TAG, "batter_number_name: " + batter_number_names[batterIdx][team_displayed]);
                create_canvas(batter_bitmaps[batterIdx+1], DarkerGray, 0, 0, batters_layout_width - 2, thumbnail_size - 2,
                    team_color[team_displayed], base_text_size , batter_number_name, 10, (int) floor(.6 * thumbnail_size), false);
                create_canvas(position_bitmaps[batterIdx+1], DarkerGray, 0, 0, positions_layout_width - 2, thumbnail_size - 2,
                    team_color[team_displayed], base_text_size, batter_position[batterIdx][team_displayed], round(7*density),
                    (int) floor(.6 * thumbnail_size), false);
                v.invalidate();

                if (batter_pa[batterIdx][team_displayed] > 0) {  // This is a substitution if previous batter in slot has made a plate appearance
                  thumbnail_bitmap = thumbnail_bitmap_array[batterIdx + (max(0, inning-2))*number_batters][team_displayed];
                  thumbnail_canvas = new Canvas(thumbnail_bitmap);
                  thumbnail_paint = new Paint();
                  drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[team_displayed], sw, right_line);
                  //Log.i(TAG, "Drawing right_line");
                }
                return true;
              }
            });
            batter_popup.show();
          }
        });
      }
    }

    GridLayout positionGridLayout = findViewById(R.id.position_grid_layout);
    positionGridLayout.removeAllViews();
    positionGridLayout.setRowCount(number_batters+2);
    for (int i = 0; i < number_batters + 2; i++) {
      positions[i] = new ImageView(this);
      positions[i].setId(i);
      positions[i].setImageBitmap(position_bitmaps[i]);
      positionGridLayout.addView(positions[i]);
      if (i > 0 && i < number_batters + 1) {
        positions[i].setOnClickListener(new View.OnClickListener() {
          public void onClick(final View v) {
            int positionId = (v.getId());
            final int positionIdx = positionId - 1;
            String msg = "position " + positionId + " selected";
            //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            position_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(MainActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                batter_position[positionIdx][team_displayed] = item.getTitle().toString();
                create_canvas(position_bitmaps[positionIdx+1], DarkerGray, 0, 0, positions_layout_width - 2, thumbnail_size - 2,
                    team_color[team_displayed], base_text_size , batter_position[positionIdx][team_displayed], round(7*density),
                    (int) floor(.6 * thumbnail_size), false);
                v.invalidate();
                return true;
              }
            });
            position_popup.show();
          }
        });
      }
    }

    HorizontalScrollView mainScrollViewLayout = findViewById(R.id.main_scrollview_layout);
    mainScrollViewLayout.setLayoutParams(new LinearLayout.LayoutParams(main_layout_width, main_scrollview_height));
    GridLayout mainGridLayout = findViewById(R.id.main_grid_layout);
    mainGridLayout.removeAllViews();
    mainGridLayout.setRowCount(number_batters+2);
    for (int i_inning = 0; i_inning < number_innings; i_inning++) {
      inning_headers[i_inning] = new ImageView(this);
      inning_headers[i_inning].setImageBitmap(inning_header_bitmaps[i_inning]);
      mainGridLayout.addView(inning_headers[i_inning]);
      for (int i = number_batters * i_inning; i < number_batters * (i_inning + 1); i++) {
        //Log.i(TAG, "i_inning=" + i_inning + " i=" + i);
        thumbnail_array[i] = new ImageView(this);
        thumbnail_array[i].setId(i);
        thumbnail_array[i].setImageBitmap(thumbnail_bitmap_array[i][team_displayed]);
        mainGridLayout.addView(thumbnail_array[i]);
        thumbnail_array[i].setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
          atBatInd = v.getId();
          String msg = "thumbnail " + atBatInd + " selected";
          //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
          //Log.i(TAG, "Before AtBatActivity team_displayed=" + team_displayed +" atBatInd=" + atBatInd + " State=" + atBat_state_array[atBatInd][team_displayed]);
          intent_atbat.putExtra("ATBATIDX", atBatInd);
          intent_atbat.putExtra("TEAM_DISP", team_displayed);
          startActivityForResult(intent_atbat, ACTIVITY_ATBAT);
          }
        });
      }
      inning_footers[i_inning] = new ImageView(this);
      inning_footers[i_inning].setImageBitmap(inning_footer_bitmaps[i_inning]);
      mainGridLayout.addView(inning_footers[i_inning]);
    }

    GridLayout sumsGridLayout = findViewById(R.id.sums_grid_layout);
    sumsGridLayout.removeAllViews();
    sumsGridLayout.setRowCount(number_batters+2);
    for (int i = 0; i < number_batters + 2; i++) {
      sums[i] = new ImageView(this);
      sums[i].setImageBitmap(sum_bitmaps[i]);
      sumsGridLayout.addView(sums[i]);
    }
    // endregion
  } // End drawMain
  
  public void writeInningDataToGameFile(){
    String gfij = // game file inning json
      "{\"Inning\":" + inning + ", \"InningHalf\":" + inning_half  + ", " +
      "\"RHL\":{\"runs\":" + inning_runs[inning - 1][inning_half] + ", " +
      "\"hits\":" + inning_hits[inning - 1][inning_half] + ", " +
      "\"lob\":" + inning_lob[inning - 1][inning_half] + "},\n" +
      "\"VisitorStats\":{\"AB\":" + team_abs[0] + ", \"R\":" + team_runs[0] +
        ", \"H\":" + team_hits[0] + ", \"RBI\":" + team_rbi[0] + ", \"E\":" + team_errors[0] + "},\n" +
      "\"HomeStats\":{\"AB\":" + team_abs[1] + ", \"R\":" + team_runs[1] +
        ", \"H\":" + team_hits[1] + ", \"RBI\":" + team_rbi[1] + ", \"E\":" + team_errors[1] + "},\n";
    gfij = gfij + "\"VisitorBatterInfo\":[\n";
    for (int i = 0; i < number_batters; i++) {
      gfij = gfij + "{\"NumberName\":\"" + batter_number_names[i][0] + "\", \"Pos\":\"" + batter_position[i][0] + "\", " +
          "\"PA\":" + batter_pa[i][0] + ", \"AB\":" + batter_abs[i][0] + ", \"R\":" + batter_runs[i][0] +
          ", \"H\":" + batter_hits[i][0] + ", \"RBI\":" + batter_rbi[i][0] + ", \"E\":" + batter_errors[i][0];
      if (i == number_batters-1) gfij = gfij + "}\n"; else gfij = gfij + "},\n";
    }
    gfij = gfij + "],\n";
    gfij = gfij + "\"VisitorPitcherInfo\":[\n";
    for (int i = 0; i <= current_pitcher_index[0]; i++) {
      gfij = gfij + "{\"NumberName\":\"" + pitcher_number_names[i][0] + "\", \"BattersFaced\":\"" + pitcher_batters_faced[i][0] + "\", " +
          "\"Outs\":" + pitcher_outs[i][0] + ", \"H\":" + pitcher_hits[i][0] + ", \"R\":" + pitcher_runs[i][0] +
          ", \"ER\":" + pitcher_earned_runs[i][0] + ", \"BB\":" + pitcher_bb[i][0] + ", \"K\":" + pitcher_k[i][0] + ", \"E\":" + pitcher_errors[i][0] +
          ", \"P\":" + pitcher_pitches[i][0] + ", \"B\":" + pitcher_balls[i][0] + ", \"S\":" + pitcher_strikes[i][0];
      if (i == current_pitcher_index[0]) gfij = gfij + "}\n"; else gfij = gfij + "},\n";
    }
    gfij = gfij + "],\n";
    gfij = gfij + "\"HomeBatterInfo\":[\n";
    for (int i = 0; i < number_batters; i++) {
      gfij = gfij + "{\"NumberName\":\"" + batter_number_names[i][1] + "\", \"Pos\":\"" + batter_position[i][1] + "\", " +
          "\"PA\":" + batter_pa[i][1] + ", \"AB\":" + batter_abs[i][1] + ", \"R\":" + batter_runs[i][1] +
          ", \"H\":" + batter_hits[i][1] + ", \"RBI\":" + batter_rbi[i][1] + ", \"E\":" + batter_errors[i][1];
      if (i == number_batters-1) gfij = gfij + "}\n"; else gfij = gfij + "},\n";
    }
    gfij = gfij + "],\n";
    gfij = gfij + "\"HomePitcherInfo\":[\n";
    for (int i = 0; i <= current_pitcher_index[1]; i++) {
      gfij = gfij + "{\"NumberName\":\"" + pitcher_number_names[i][1] + "\", \"BattersFaced\":\"" + pitcher_batters_faced[i][1] + "\", " +
          "\"Outs\":" + pitcher_outs[i][1] + ", \"H\":" + pitcher_hits[i][1] + ", \"R\":" + pitcher_runs[i][1] +
          ", \"ER\":" + pitcher_earned_runs[i][1] + ", \"BB\":" + pitcher_bb[i][1] + ", \"K\":" + pitcher_k[i][1] + ", \"E\":" + pitcher_errors[i][1] +
          ", \"P\":" + pitcher_pitches[i][1] + ", \"B\":" + pitcher_balls[i][1] + ", \"S\":" + pitcher_strikes[i][1];
      if (i == current_pitcher_index[1]) gfij = gfij + "}\n"; else gfij = gfij + "},\n";
    }
    gfij = gfij + "],\n";
    gfij = gfij + "\"InningHeaders\":[";
    for (int i = 0; i < number_innings; i++) {
      gfij = gfij + inning_header_values[i][inning_half];
      if (i != number_innings - 1) gfij = gfij + ", ";
    }
    gfij = gfij + "],\n";
    gfij = gfij + "\"AtBats\":[\n";
    //Log.i(TAG, "atBat_sequence_index[team_up]=" + atBat_sequence_index[team_up] + " inning_half_batters=" + inning_half_batters);
    for (int i = atBat_sequence_index[team_up] - inning_half_batters; i <= atBat_sequence_index[team_up]; i++) {
      int atBatInd = atBat_sequence_array[i][team_up];
      //Log.i(TAG, "In writeInningData: atBat_sequence_array[" + i +"][" + team_up + "]=" + atBatInd);
      gfij = gfij + "{";
      gfij = gfij + "\"AtBatInd\":" + atBatInd + ", ";
      gfij = gfij + "\"State\":" + atBat_state_array[atBatInd][team_up] + ", ";
      atBat ab = atBat_array[atBatInd][team_up];
      gfij = gfij + "\"Batter\":\"" + ab.batter_number_name + "\", ";
      gfij = gfij + "\"Pitcher\":\"" + ab.pitcher_number_name + "\", ";
      gfij = gfij + "\"Inning\":\"" + ab.inning + "\", ";
      gfij = gfij + "\"InningHalf\":\"" + ab.inning_half + "\", ";
      gfij = gfij + "\"OutNumber\":\"" + ab.out_number + "\", ";
      gfij = gfij + "\"CurrentBase\":\"" + ab.current_base + "\", ";
      gfij = gfij + "\"ErrorDuringAtBatOrOnBase\":\"" + ab.error_during_at_bat_or_on_base + "\", ";
      gfij = gfij + "\"ball_in_play_locationX\":\"" + ab.ball_in_play_location[0] + "\", ";
      gfij = gfij + "\"ball_in_play_locationY\":\"" + ab.ball_in_play_location[1] + "\", ";
      gfij = gfij + "\"RBIs\":\"" + ab.RBIs + "\", ";
      gfij = gfij + "\"StolenBase\":\"" + ab.stolen_base + "\", ";
      gfij = gfij + "\"PickedOff\":\"" + ab.picked_off + "\", ";
      gfij = gfij + "\"Scored\":\"" + ab.scored + "\", ";
      gfij = gfij + "\"Comment\":\"" + ab.comment + "\", ";
      gfij = gfij + "\"Balls\":\"" + ab.balls + "\", ";
      gfij = gfij + "\"Strikes\":\"" + ab.strikes + "\", ";
      gfij = gfij + "\"OnBaseArray0\":\"" + ab.ob[0] + "\", ";
      gfij = gfij + "\"OnBaseArray1\":\"" + ab.ob[1] + "\", ";
      gfij = gfij + "\"OnBaseArray2\":\"" + ab.ob[2] + "\", ";
      gfij = gfij + "\"OnBaseArray3\":\"" + ab.ob[3] + "\", ";
      gfij = gfij + "\"OnBaseArray4\":\"" + ab.ob[4] + "\"";
      if (i == atBat_sequence_index[team_up]) gfij = gfij + "}\n"; else gfij = gfij + "},\n";
    }
    gfij = gfij + "]}";
    //Log.i(TAG, "gfij=" + gfij );
    try {
      if (!(inning == 1 && inning_half == 0)) game_file_bufferedWriter.write(",");
      game_file_bufferedWriter.write(gfij + "\n");
      game_file_bufferedWriter.flush();
    } catch (IOException ex) {
      //Log.i(TAG, "End inning file IOException " + ex.getMessage());
      Toast.makeText(MainActivity.this, "End inning IO Exception" + ex.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }
    
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ACTIVITY_ATBAT) {
      if (resultCode == RESULT_OK) {
        //Log.i(TAG, "In onActivityResult: team_up=" + team_up + " team_displayed=" + team_displayed);
        //Log.i(TAG, "In onActivityResult: outs[team_up]=" + outs[team_up] + " outs[team_displayed]=" + outs[team_displayed]);
        // Replace "Other Team" with "End Inning" if 3 outs or if less than 3 outs in bottom of last inning or extra innings and home team is ahead
        // But don't put up "End Inning" once game is over
        if (outs[team_up] == 3 ||
            (inning >= number_innings_regulation && team_runs[1] > team_runs[0] && team_up == 1 && !game_over)) {
          options_menu.removeItem(R.id.action_switch);
          if (options_menu.size() == 2) {  // Don't add action_end_inning multiple times
            options_menu.add(0, R.id.action_end_inning, 0, "End Inning").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
          }
        }
        drawMain(team_up);  // To update stats
      }
    }
    else if (requestCode == ACTIVITY_PITCHING) Log.i(TAG, "Return from Pitching Activity");
    else if (requestCode == ACTIVITY_ADDBATTER) {
      if (resultCode == RESULT_OK) {
        batter_name = data.getStringExtra("batter_name");
        int batter_number = data.getIntExtra("batter_number", 0);
        batter_number_name = String.format("%3d %s", batter_number, batter_name);
        //Log.i(TAG, "Return from AddBatterActivity: resultCode=" + resultCode + " batter_number_name=" + batter_number_name);
        batter_number_names[batterIdx][team_displayed] = batter_number_name;
        drawMain(team_displayed);
      }
    }  // End  else if (requestCode == ACTIVITY_ADDBATTER)
    else if (requestCode == ACTIVITY_ADDPITCHER) {
      if (resultCode == RESULT_OK) {
        pitcher_name = data.getStringExtra("pitcher_name");
        int pitcher_number = data.getIntExtra("pitcher_number", 0);
        pitcher_number_name = String.format("%3d %s", pitcher_number, pitcher_name);
        //Log.i(TAG, "Return from AddPitcherActivity: resultCode=" + resultCode + " pitcher_number_name=" + pitcher_number_name + " teamIdx=" + teamIdx);
        pitcher_number_names[current_pitcher_index[teamIdx]][teamIdx]= pitcher_number_name;
        drawMain(team_displayed);
      }
    }  // End  else if (requestCode == ACTIVITY_ADDPITCHER)
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main_menu, menu);
    options_menu = menu;
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_switch:
        // region
        // Switch to other team
        if (team_displayed == 0) team_displayed = 1; else team_displayed = 0;
        //Log.i(TAG, "Action Switch: Calling drawMain w/ team name = " +  team_name[team_displayed]);
        drawMain(team_displayed);
        break;
        // endregion
      case R.id.action_return:
        // region
        AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(this);
        exitDialogBuilder
          .setCancelable(true)
          .setTitle("Exit/Game Over")
          .setItems(
            new CharSequence[] {"Cancel", "Game Over", "Exit without saving game file", "Exit and save game file"},
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                  case 0:  // Cancel
                    //Log.i(TAG, "exitDialogBuilder: Cancel");
                    // Do nothing
                    break;
                  case 1:  // Game Over
                    // region
                    game_over = true;
                    for (int i =  0; i < inning_half_batters; i++) {
                      atBatInd = atBat_sequence_array[i][team_up];
                      //Log.i(TAG, "atBat_sequence_array[" + i +"][" + team_up + "]=" + atBatInd + " inning=" + atBat_array[atBatInd][team_up].inning );
                      if (atBat_state_array[atBatInd][team_up] == 2) {   // Left on base
                        //Log.i(TAG, "  sequence: atBat_state_array[" + atBatInd + "][" + team_up + "]=" + atBat_state_array[atBatInd][team_up]);
                        atBat_state_array[atBatInd][team_up] = 3;
                        ++inning_lob[inning - 1][team_up];
                      }
                    }
                    if (atBat_state_array[atBatInd + 1][team_up] == 1) {  // On deck batter
                      //Log.i(TAG, "  sequence: atBat_state_array[" + (atBatInd + 1) +"][" + team_up + "]=" + atBat_state_array[atBatInd + 1][team_up]);
                      atBat_state_array[atBatInd + 1][team_up] = 0;
                    }

                    writeInningDataToGameFile();

                    //Log.i(TAG, "Game Over: Calling drawMain w/ team name = " +  team_name[team_displayed]);
                    drawMain(team_displayed);
                    // Finalize game file
                    DateFormat df = new SimpleDateFormat(" h:mm a");
                    String date_str = df.format(Calendar.getInstance().getTime());
                    String score_str;
                    if (team_runs[0] > team_runs[1]) score_str = team_name[0] + " " + team_runs[0] + ", " + team_name[1] + " " + team_runs[1];
                    else score_str = team_name[1] + " " + team_runs[1] + ", " + team_name[0] + " " + team_runs[0];
                    String game_file_footer_json = "]\n,\"GameOver\":{\"Final Score\":\"" + score_str +"\", \"Time\":\"" + date_str  +"\"}\n}";
                    try {
                      game_file_bufferedWriter.write(game_file_footer_json);
                      game_file_bufferedWriter.flush();
                      //Log.i(TAG, "json footer=" + game_file_footer_json);
                    } catch (IOException ex) {
                      //Log.i(TAG, "Write game file footer IOException " + ex.getMessage());
                      Toast.makeText(MainActivity.this, "Write game file footer IO Exception" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    //endregion
                    break;
                  case 2: // Exit without saving game file (i.e. delete saved game file)
                    // region
                    //Log.i(TAG, "exitDialogBuilder: Exit without saving game file");
                    if (game_file.exists()) game_file.delete();
                    setResult(RESULT_OK, getIntent());
                    finish();
                    // endregion
                    break;
                  case 3:  // Exit and save game file (i.e. leave saved game file alone)
                    // region
                    //Log.i(TAG, "exitDialogBuilder: Exit and save game file");
                    setResult(RESULT_OK, getIntent());
                    finish();
                    // endregion
                    break;
                  }
                }
              });
        exitDialogBuilder.create().show();
        break;
        // endregion
      case R.id.action_end_inning:
        // region
        int atBatInd = atBat_sequence_array[atBat_sequence_index[team_up]][team_up];
        //Log.i(TAG, "End Inning: atBat_sequence_index=" + atBat_sequence_index[team_up] + " atBatInd=" + atBatInd);
        //Log.i(TAG, "End Inning: atBat_state_array[atBatInd][team_up]=" + atBat_state_array[atBatInd][team_up]);

        if (atBat_state_array[atBatInd][team_up] == 1) {   // 3rd out occurred on base; resume with current batter next inning
          atBat_state_array[atBatInd][team_up] = 0;
          // Place horizontal line under previous batter
          int atBatInd_prev = atBat_sequence_array[atBat_sequence_index[team_up]-1][team_up];
          thumbnail_bitmap = thumbnail_bitmap_array[atBatInd_prev][team_up];
          thumbnail_canvas = new Canvas(thumbnail_bitmap);
          thumbnail_paint = new Paint();
          drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[team_up], sw, bottom_line);
          thumbnail_bitmap_array[atBatInd_prev][team_up] = thumbnail_bitmap;
          // Set outline of batter who was at bat when out was made to white
          thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][team_up];
          thumbnail_canvas = new Canvas(thumbnail_bitmap);
          thumbnail_paint = new Paint();
          drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.WHITE, 2, outline);
          thumbnail_bitmap_array[atBatInd][team_up] = thumbnail_bitmap;
          next_up[team_up] = atBatInd + number_batters;  // Start next inning in same row, next column
          //Log.i(TAG, "End Inning 3rd out occurred on base: Next up calc: atBatInd=" + atBatInd +" Next up =" + next_up[team_up]);
          atBat_sequence_array[atBat_sequence_index[team_up]][team_up] = next_up[team_up];
          //Log.i(TAG, "End Inning - last out occurred on base: atBat_sequence_index[team_up]=" + atBat_sequence_index[team_up] +
          //   " atBat_sequence_array[atBat_sequence_index[team_up]][team_up]=" + atBat_sequence_array[atBat_sequence_index[team_up]][team_up]);
        }  // End 3rd out occurred on base; resume with current batter next inning
        else {  // 3rd out occurred at bat
          if ((atBatInd + 1) % number_batters == 0)
            next_up[team_up] = atBatInd + 1; //Start next inning at top of order in next column (next in atBatInd sequence)
          else
            next_up[team_up] = atBatInd + number_batters + 1;  // Start next inning in middle of order - next row, next column
          //Log.i(TAG, "End Inning 3rd out occurred at bat: Next up calc: atBatInd=" + atBatInd +" Next up=" + next_up[team_up]);
          // Set outline of batter who made last out to white, draw horizontal line underneath
          thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][team_up];
          thumbnail_canvas = new Canvas(thumbnail_bitmap);
          thumbnail_paint = new Paint();
          drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.WHITE, 2, outline);
          drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[team_up], sw, bottom_line);
          thumbnail_bitmap_array[atBatInd][team_up] = thumbnail_bitmap;
          ++atBat_sequence_index[team_up];
          atBat_sequence_array[atBat_sequence_index[team_up]][team_up] = next_up[team_up];
          //Log.i(TAG, "End Inning - last out occurred at bat: atBat_sequence_index[team_up]=" + atBat_sequence_index[team_up] +
          //  " atBat_sequence_array[atBat_sequence_index[team_up]][team_up]=" + atBat_sequence_array[atBat_sequence_index[team_up]][team_up]);
        }  // End 3rd out occurred at bat

        for (int i =  0; i < inning_half_batters; i++) {
          atBatInd = atBat_sequence_array[i][team_up];
          //Log.i(TAG, "atBat_sequence_array[" + i +"][" + team_up + "]=" + atBatInd + " inning=" + atBat_array[atBatInd][team_up].inning );
          if (atBat_state_array[atBatInd][team_up] == 2) {   // Left on base
            //Log.i(TAG, "  sequence: atBat_state_array[" + atBatInd + "][" + team_up + "]=" + atBat_state_array[atBatInd][team_up]);
            atBat_state_array[atBatInd][team_up] = 3;
            ++inning_lob[inning - 1][team_up];
          }
        }

        writeInningDataToGameFile();

        outs[team_up] = 0;
        on_base[team_up] = 0;
        inning_half_batters = 0;
        //Log.i(TAG, "Action End Inning: Calling drawMain w/ team name = " +  team_name[team_displayed]);
        drawMain(team_displayed);

        if (inning >= number_innings_regulation &&                 // We have a winner!
            ((team_runs[1] > team_runs[0]) || (team_up == 1 && team_runs[0] > team_runs[1]))) {
          DateFormat df = new SimpleDateFormat(" h:mm a");
          String date_str = df.format(Calendar.getInstance().getTime());
          String score_str;
          if (team_runs[0] > team_runs[1]) score_str = team_name[0] + " " + team_runs[0] + ", " + team_name[1] + " " + team_runs[1];
          else score_str = team_name[1] + " " + team_runs[1] + ", " + team_name[0] + " " + team_runs[0];
          String game_file_footer_json = "]\n,\"GameOver\":{\"Final Score\":\"" + score_str +"\", \"Time\":\"" + date_str  +"\"}\n}";
          try {
            game_file_bufferedWriter.write(game_file_footer_json);
            game_file_bufferedWriter.flush();
            //Log.i(TAG, "json footer=" + game_file_footer_json);
          } catch (IOException ex) {
            //Log.i(TAG, "Write game file footer IOException " + ex.getMessage());
            Toast.makeText(this, "Write game file footer IO Exception" + ex.getMessage(), Toast.LENGTH_SHORT).show();
          }
          game_over = true;
        }
        else {
          // Switch to other team, mark next batter with team color
          if (team_up == 0) team_up = 1; else team_up = 0;
          if (team_displayed == 0) team_displayed = 1; else team_displayed = 0;
          atBat_state_array[next_up[team_up]][team_up] = 1;
          // Set outline of next batter to team color
          thumbnail_bitmap_next_batter = thumbnail_bitmap_array[next_up[team_up]][team_up];
          thumbnail_canvas = new Canvas(thumbnail_bitmap_next_batter);
          thumbnail_paint = new Paint();
          drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[team_up], 2, outline);
          thumbnail_bitmap_array[next_up[team_up]][team_up] = thumbnail_bitmap_next_batter;

          if (inning_half == 1) { inning_half = 0; inning++; } else inning_half = 1;
        }
        options_menu.removeItem(R.id.action_end_inning);
        options_menu.add(0, R.id.action_switch, 0, "Other Team").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //Log.i(TAG, "Action End Inning: Calling drawMain w/ team name = " + team_name[team_displayed]);
        drawMain(team_displayed);
        break;
        // endregion
      case R.id.action_pitching:
        //region
        Intent intent_pitching = new Intent(this, PitchingActivity.class);
        startActivityForResult(intent_pitching, ACTIVITY_PITCHING);
        return true;
      //endregion
      default:
        break;
    }
    return true;
  }

  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    View decorView = getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    decorView.setSystemUiVisibility(uiOptions);
  }
} // End MainActivity
  