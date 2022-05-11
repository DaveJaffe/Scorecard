package com.dj.sc;

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

import static org.jsoup.Jsoup.parse;

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
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.Math.*;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class StartUpActivity extends AppCompatActivity {
  // region Declarations
  private static final String TAG = StartUpActivity.class.getName();

  final static int ACTIVITY_MAIN = 0, ACTIVITY_SETTINGS = 1, ACTIVITY_ADDTEAM = 2, ACTIVITY_HELP = 3;

  static int number_innings = 30;
  static int number_innings_regulation;
  static int default_number_innings_regulation = 9;
  static int number_batters;
  static int default_number_batters = 9;
  static int max_number_batters = 20;
  static int max_number_pitchers = 13;
  static int max_players_on_roster = 60;
  static int number_atBats = number_innings * max_number_batters;
  static String[][] player_info_one_team = new String[max_players_on_roster][3];  // Number, Name, Pos
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
  static boolean use_ghost_runner = true;

  static String[][] MLB_teams = new String[40][3]; // Name, ShortName, Color
  static String[] roster_filenames= new String[50];
  static int n_rosters = 0;
  static String[] team_name = new String[2];
  static int[] team_color = {0, 0};
  static String[][] team_info = new String[40][2]; // Name, Color
  static int[] team_abs = {0, 0};
  static int[] team_runs = {0, 0};
  static int[] team_hits = {0, 0};
  static int[] team_rbi = {0, 0};
  static int[] team_errors = {0, 0};
  static int[][] atBat_state_array = new int[number_atBats][2]; // 0=Not yet, 1=AtBat, 2=OnBase, 3=Finished
  static int[][] atBat_sequence_array = new int[number_atBats][2]; // sequential atBatInds per team
  static int[]  atBat_sequence_index = {0, 0};
  static int[]  atBat_sequence_index_end_prev_inning = {0, 0};
  static int[]  atBat_sequence_index_end_curr_inning = {-1, -1};
  static int[] next_ghost_runner_slot = {0, 0};
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
      {"Double", "2B Error", "Ghost Runner"},    // Safe - 2 base
      {"Triple", "3B Error"},    // Safe - 3 base
      {"Home Run", "4B Error"},  // Safe - 4 base
      {"Advance One Base by Hit/Walk/PB/WP/Sacrifice/FC/Error/Balk/Throw", "Advance Two Bases by Hit/PB/WP/Sacrifice/FC/Error/Throw",
       "Advance Three Bases by Hit/PB/WP/Sacrifice/FC/Error/Throw", "Stolen Base", "Caught Stealing",
       "Picked Off", "Out on ground out/double play/triple play/thrown out"}   // OnBase
  };

  static String[][] atBat_result_text = {
      {"", "K", "\uA4D8", "GO", "FO", "DP", "TP", "LO", "PU", "FLO", "IF", "SB", "SF"},  // Outs
      {"1B", "BB", "HBP", "FC", "KD", "E", "CI"},  // Safe - 1 base
      {"2B", "E", "GR"},   // Safe - 2 base
      {"3B", "E"},   // Safe - 3 base
      {"HR", "E"},   // Safe - 4 base
      {"+1", "+2", "+3", "SB", "CS", "PO", "OUT"}    // OnBase
  };

  static boolean[][] atBat_result_is_ab = {
      {true, true, true, true, true, true, true, true, true, true, true, false, false},  // Outs
      {true, false, false, true, true, true, false},  // Safe - 1 base
      {true, true, false},   // Safe - 2 base
      {true, true},   // Safe - 3 base
      {true, true}    // Safe - 4 base
  };

  static boolean[][] atBat_result_is_hit = {
      {false, false, false, false, false, false, false, false, false, false, false, false, false},  // Outs
      {true, false, false, false, false, false, false},  // Safe - 1 base
      {true, false, false},   // Safe - 2 base
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
  
  static boolean team_set[] = {false, false};

  static String directory_path;
  static String roster_filename;
  static String roster_json_str;
  static File game_file;
  static BufferedWriter game_file_bufferedWriter;

  static Intent intent_main;

  static Button[] selectTeams = new Button[2];

  Canvas thumbnail_canvas;
  Paint thumbnail_paint;
  // endregion Declarations

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.startup_activity);
    intent_main = new Intent(this, MainActivity.class);
    final Intent intent_addteam = new Intent(this, AddTeamActivity.class);

    hideNavigation(getWindow().getDecorView());

    // Get screen info not including height of navigation bar at bottom
    density = getResources().getDisplayMetrics().density;
    heightPx = getResources().getDisplayMetrics().heightPixels;
    widthPx = getResources().getDisplayMetrics().widthPixels;
    //Log.i(TAG, "Dimension: density=" + density + " widthPx=" + widthPx + " heightPx=" +  heightPx);

    // Check some more metrics
    float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
    float densityDPI = getResources().getDisplayMetrics().densityDpi;
    float xDPI = getResources().getDisplayMetrics().xdpi;
    float yDPI = getResources().getDisplayMetrics().ydpi;
    //Log.i(TAG, "Dimension: scaledDensity=" + scaledDensity + " densityDPI=" + densityDPI + " xDPI=" + xDPI + " yDPI=" + yDPI);

    // Get full size of display
    Display display = this.getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getRealSize(size);
    real_aspect_ratio = (float) size.x / (float) size.y;
    String real_aspect_ratio_str = format(Locale.US, "%4.2f", real_aspect_ratio);
    //Log.i(TAG, "Dimension: real width=" + size.x + " real height=" + size.y + " real aspect ratio=" + real_aspect_ratio_str);

    // We will use the full screen except that we will hide the navigation bar at bottom
    //
    //   ____________________________________________________
    //   | (Time)  (Notifications)        (Status icons)    |  <-- Status bar
    //   | Scorecard                  SETTINGS GAMES HELP X |  <-- Action bar
    //   |                                                  |
    //   |                                                  |
    //   |  (app)                                           |
    //   |                                                  |
    //   |                                                  |
    //   |  (left arrow)        (circle)          (square)  |  <-- Navigation bar (hidden during execution)
    //   ----------------------------------------------------

    // Find heights of Status bar at top and Navigation bar at bottom
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
    //Log.i(TAG, "Dimension: action_bar_height=" + action_bar_height + " main_layout_left_margin=" + main_layout_left_margin);
    //Log.i(TAG, "Dimension: available: widthPx/heightPx=" + widthPx  + "/" +  heightPx);

    // region initialize MLB_teams
    MLB_teams[ 0][0] = "Arizona Diamondbacks";   MLB_teams[ 0][1] = "dbacks";      MLB_teams[ 0][2] = "#A71930";
    MLB_teams[ 1][0] = "Atlanta Braves";         MLB_teams[ 1][1] = "braves";      MLB_teams[ 1][2] = "#CE1141";
    MLB_teams[ 2][0] = "Baltimore Orioles";      MLB_teams[ 2][1] = "orioles";     MLB_teams[ 2][2] = "#DF4601";
    MLB_teams[ 3][0] = "Boston Red Sox";         MLB_teams[ 3][1] = "redsox";      MLB_teams[ 3][2] = "#BD3039";
    MLB_teams[ 4][0] = "Chicago Cubs";           MLB_teams[ 4][1] = "cubs";        MLB_teams[ 4][2] = "#0E3386";
    MLB_teams[ 5][0] = "Chicago White Sox";      MLB_teams[ 5][1] = "whitesox";    MLB_teams[ 5][2] = "#27251F";
    MLB_teams[ 6][0] = "Cincinnati Reds";        MLB_teams[ 6][1] = "reds";        MLB_teams[ 6][2] = "#C6011F";
    MLB_teams[ 7][0] = "Cleveland Guardians";    MLB_teams[ 7][1] = "guardians";   MLB_teams[ 7][2] = "#0C2340";
    MLB_teams[ 8][0] = "Colorado Rockies";       MLB_teams[ 8][1] = "rockies";     MLB_teams[ 8][2] = "#33006F";
    MLB_teams[ 9][0] = "Detroit Tigers";         MLB_teams[ 9][1] = "tigers";      MLB_teams[ 9][2] = "#0C2340";
    MLB_teams[10][0] = "Houston Astros";         MLB_teams[10][1] = "astros";      MLB_teams[10][2] = "#EB6E1F";
    MLB_teams[11][0] = "Kansas City Royals";     MLB_teams[11][1] = "royals";      MLB_teams[11][2] = "#004687";
    MLB_teams[12][0] = "Los Angeles Angels";     MLB_teams[12][1] = "angels";      MLB_teams[12][2] = "#BA0021";
    MLB_teams[13][0] = "Los Angeles Dodgers";    MLB_teams[13][1] = "dodgers";     MLB_teams[13][2] = "#005A9C";
    MLB_teams[14][0] = "Miami Marlins";          MLB_teams[14][1] = "marlins";     MLB_teams[14][2] = "#EF3340";
    MLB_teams[15][0] = "Milwaukee Brewers";      MLB_teams[15][1] = "brewers";     MLB_teams[15][2] = "#12284B";
    MLB_teams[16][0] = "Minnesota Twins";        MLB_teams[16][1] = "twins";       MLB_teams[16][2] = "#D31145";
    MLB_teams[17][0] = "New York Mets";          MLB_teams[17][1] = "mets";        MLB_teams[17][2] = "#002D72";
    MLB_teams[18][0] = "New York Yankees";       MLB_teams[18][1] = "yankees";     MLB_teams[18][2] = "#003087";
    MLB_teams[19][0] = "Oakland Athletics";      MLB_teams[19][1] = "athletics";   MLB_teams[19][2] = "#003831";
    MLB_teams[20][0] = "Philadelphia Phillies";  MLB_teams[20][1] = "phillies";    MLB_teams[20][2] = "#E81828";
    MLB_teams[21][0] = "Pittsburgh Pirates";     MLB_teams[21][1] = "pirates";     MLB_teams[21][2] = "#27251F";
    MLB_teams[22][0] = "St. Louis Cardinals";    MLB_teams[22][1] = "cardinals";   MLB_teams[22][2] = "#C41E3A";
    MLB_teams[23][0] = "San Diego Padres";       MLB_teams[23][1] = "padres";      MLB_teams[23][2] = "#2F241D";
    MLB_teams[24][0] = "San Francisco Giants";   MLB_teams[24][1] = "giants";      MLB_teams[24][2] = "#FD5A1E";
    MLB_teams[25][0] = "Seattle Mariners";       MLB_teams[25][1] = "mariners";    MLB_teams[25][2] = "#005C5C";
    MLB_teams[26][0] = "Tampa Bay Rays";         MLB_teams[26][1] = "rays";        MLB_teams[26][2] = "#092C5C";
    MLB_teams[27][0] = "Texas Rangers";          MLB_teams[27][1] = "rangers";     MLB_teams[27][2] = "#C0111F";
    MLB_teams[28][0] = "Toronto Blue Jays";      MLB_teams[28][1] = "bluejays";    MLB_teams[28][2] = "#134A8E";
    MLB_teams[29][0] = "Washington Nationals";   MLB_teams[29][1] = "nationals";   MLB_teams[29][2] = "#AB0003";
    // endregion

    initialize_vars(default_number_batters, default_number_innings_regulation);

    // region Buttons selectTeams
    selectTeams[0] = (Button) findViewById(R.id.visitingTeamButton);
    selectTeams[1] = (Button) findViewById(R.id.homeTeamButton);
    // Find any roster files in external files directory
    directory_path = this.getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/";
    //Log.i(TAG, "directory path=" + directory_path);
    //String[] roster_filenames= new String[50];
    String[] game_and_roster_filenames = new File(directory_path).list();
    for (String roster_filename : game_and_roster_filenames) if (roster_filename.startsWith("roster")) {
      roster_filenames[n_rosters++] = roster_filename;
      //Log.i(TAG, "roster_filename: " + roster_filename);
    }
    for (int visitor_or_home = 0; visitor_or_home < 2; visitor_or_home++) {
      final int visitor_or_home_final = visitor_or_home;
      // Set up roster popup including submenus for MLB teams and existing rosters
      final PopupMenu roster_popup = new PopupMenu(this, findViewById(R.id.popup_insert_point));

      SubMenu MLB_teams_menu = roster_popup.getMenu().addSubMenu("Download MLB Team Roster");
      for (int i = 0; i < 30; i++) MLB_teams_menu
        .add(0, i, 0, MLB_teams[i][0])
        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
          @Override
          // What to do when an MLB team is selected
          public boolean onMenuItemClick(MenuItem item) {
            //Log.i(TAG, item.getTitle().toString() + " selected");
            team_name[visitor_or_home_final] = item.getTitle().toString();
            //Log.i(TAG, "Download MLB Team Roster->" + team_name[visitor_or_home_final] + " selected");
            // Look up team name in MLB_teams array to get short name and team color
            int k = 0; while (!team_name[visitor_or_home_final].equals(MLB_teams[k][0])) k++;
            //Log.i(TAG, "roster: k=" + k + " team=" + MLB_teams[k][0]);
            team_color[visitor_or_home_final] = Color.parseColor(MLB_teams[k][2]);
            // Download team roster from mlb.com using team short name
            player_info_one_team = downloadMLBTeamRoster(MLB_teams[k][1]);
            if (player_info_one_team.length == 0) {  // Caused by internet error - toast in downloadMLBTeamRoster informs user
              //Log.i(TAG, "roster: length 0");
              return true;
            }
            number_players[visitor_or_home_final] = player_info_one_team.length;
            //Log.i(TAG, "roster: number_players[" + visitor_or_home_final + "]=" + number_players[visitor_or_home_final]);
            //for (int i = 0; i < number_players[visitor_or_home_final]; i++) Log.i(TAG, "roster: " +
            //  player_info_one_team[i][0] + " " + player_info_one_team[i][1] + " " + player_info_one_team[i][2]);
            for (int i = 0; i < number_players[visitor_or_home_final]; i++) {
              for (int j = 0; j < 3; j++) {
                player_info[i][j][visitor_or_home_final] = player_info_one_team[i][j];
              } // End for (int j = 0; j < 3; j++)
            } // End for (int i = 0; i < number_players[visitor_or_home_final]; i++)
            //Log.i(TAG, "team[" + visitor_or_home_final + "] name=" + team_name[visitor_or_home_final]);
            team_set[visitor_or_home_final] = true;
            selectTeams[visitor_or_home_final].setText(team_name[visitor_or_home_final]);
            selectTeams[visitor_or_home_final].setTextColor(team_color[visitor_or_home_final]);
            if (team_set[0] && team_set[1]) play_ball(team_name, intent_main);
            return true;
          }  // End public boolean onMenuItemClick
        });

      SubMenu existing_roster_menu = roster_popup.getMenu().addSubMenu("Use Existing Team Roster File");
      for (String roster_filename : roster_filenames) existing_roster_menu
        .add(roster_filename)
        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
          @Override
          // What to do when an existing roster is selected
          public boolean onMenuItemClick(MenuItem item) {
            //Log.i(TAG, item.getTitle().toString() + " selected");
            String roster_fname = item.getTitle().toString();
            if (new File(directory_path + roster_fname).isFile())
              roster_json_str = read_file(directory_path, roster_fname);
              // Log.i(TAG, "roster_json_str: " + roster_json_str);
            try {
              JSONObject jsonObj = new JSONObject(roster_json_str);
              team_name[visitor_or_home_final] = jsonObj.getString("Name");
              team_color[visitor_or_home_final] = Color.parseColor(jsonObj.getString("Color"));
            } catch (JSONException e) {
              e.printStackTrace();
            }
            //Log.i(TAG, "team_name=" + team_name[visitor_or_home_final] + " team_color=" + team_color[visitor_or_home_final]);
            player_info_one_team = parse_roster_json_player_info(roster_json_str);
            number_players[visitor_or_home_final] = player_info_one_team.length;
            //Log.i(TAG, "roster: number_players[" + visitor_or_home_final + "]=" + number_players[visitor_or_home_final]);
            //for (int i = 0; i < number_players[visitor_or_home_final]; i++) Log.i(TAG, "roster: " +
            //  player_info_one_team[i][0] + " " + player_info_one_team[i][1] + " " + player_info_one_team[i][2]);
            for (int i = 0; i < number_players[visitor_or_home_final]; i++) {
              for (int j = 0; j < 3; j++) {
                player_info[i][j][visitor_or_home_final] = player_info_one_team[i][j];
              } // End for (int j = 0; j < 3; j++)
            } // End for (int i = 0; i < number_players[visitor_or_home_final]; i++)
            //Log.i(TAG, "team[" + visitor_or_home_final + "] name=" + team_name[visitor_or_home_final]);
            team_set[visitor_or_home_final] = true;
            selectTeams[visitor_or_home_final].setText(team_name[visitor_or_home_final]);
            selectTeams[visitor_or_home_final].setTextColor(team_color[visitor_or_home_final]);
            if (team_set[0] && team_set[1]) play_ball(team_name, intent_main);
            return true;
          } // End public boolean onMenuItemClick
        });

      roster_popup.getMenu().add("Create New Team");

      // Set up onClick listener for roster popup
      selectTeams[visitor_or_home].setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          roster_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
              //Toast.makeText(StartUpActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
              switch (item.getTitle().toString()) {
                case "Download MLB Team Roster":
                  //Log.i(TAG, "Download MLB Team Roster selected");
                  return true;
                case "Use Existing Team Roster File":
                  //Log.i(TAG, "Use Existing Team Roster File selected, " + n_rosters + " found");
                  if (n_rosters == 0) {
                    Toast.makeText(StartUpActivity.this, "No roster files available", Toast.LENGTH_SHORT).show();
                  }
                  return true;
                case "Create New Team":
                  //region
                  //Log.i(TAG, "Create New Team selected");
                  intent_addteam.putExtra("VisitorOrHome", visitor_or_home_final);
                  startActivityForResult(intent_addteam, ACTIVITY_ADDTEAM);
                  
                  return true;
                //endregion
                default:
                break;
              }
              return true;
            }
          });
          roster_popup.show();
        }
      });  // End selectTeams[visitor_or_home].setOnClickListener
    }  // End for (int visitor_or_home = 0; visitor_or_home < 2; visitor_or_home++) {

    // endregion

  } // End OnCreate
  
  public void initialize_vars(int n_batters, int n_innings_regulation) {

    // region Initialize non-arrays
    number_batters = n_batters;
    number_innings_regulation = n_innings_regulation;
    //Log.i(TAG, "In initialize_vars: number_batters=" + number_batters + " number_innings_regulation=" + number_innings_regulation);
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
    if (density >= 3.0) base_text_size = (int) floor(base_text_size *.76);
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
    // Set up Play Ball button
    Button playBall = findViewById(R.id.playBallButtonLayout);
    playBall.setVisibility(View.VISIBLE);
    playBall.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // Create file to hold game history
        DateFormat df = new SimpleDateFormat("yyyy_MM_dd", Locale.US);
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
          df = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US);
          date_str = df.format(Calendar.getInstance().getTime());
          SimpleDateFormat tf = new SimpleDateFormat("h:mm a", Locale.US);
          time_str = tf.format(Calendar.getInstance().getTime());
          String gfhj; // game_file_header_json
          gfhj = "{\"Game\":{\"Visitors\":\"" + team_name[0] + "\", \"Home\":\"" + team_name[1]  +
            "\",\n\"VisitorsColor\":\"" + format("#%06X", (0xFFFFFF & team_color[0])) +
            "\", \"HomeColor\":\"" + format("#%06X", (0xFFFFFF & team_color[1]))  +
            "\",\n\"Date\":\"" + date_str + "\", \"Time\":\"" + time_str + "\",\n" + "\"NumberBatters\":" + number_batters +
            ", \"NumberInningsRegulation\":" + number_innings_regulation + ", \"Track_b_s\":" + track_b_s +
            "},\n\"VisitorPlayers\":\n[\n";
          for (int i = 0; i < number_players[0]; i++) {
            gfhj = gfhj + "  { \"Number\": " + player_info[i][0][0] + ", \"Name\": \"" + player_info[i][1][0] +
              "\", \"Pos\": \"" + player_info[i][2][0] + "\" }";
            if (i == number_players[0] - 1) gfhj = gfhj + "\n"; else gfhj = gfhj + ",\n";
          }
          gfhj = gfhj + "],";
          gfhj = gfhj + "\n\"HomePlayers\":\n[\n";
          for (int i = 0; i < number_players[1]; i++) {
            gfhj = gfhj +"  { \"Number\": " + player_info[i][0][1] + ", \"Name\": \"" + player_info[i][1][1] +
              "\", \"Pos\": \"" + player_info[i][2][1] + "\" }";
            if (i == number_players[1] - 1) gfhj = gfhj + "\n"; else gfhj = gfhj + ",\n";
          }
          gfhj = gfhj + "],";
          gfhj = gfhj + "\n\"Innings\":\n[\n";
          game_file_bufferedWriter.write(gfhj);
          game_file_bufferedWriter.flush();
        }  catch (IOException ex) {
        //Log.i(TAG, "game file IOException" + ex.getMessage());
          Toast.makeText(StartUpActivity.this, "game file IOException" + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

  public String[][] downloadMLBTeamRoster(String team_short_name){
    String[][] unsorted_player_info; // Number, name, position
    int n_players;
    downloadMLBTeamRosterThread dmtrThread = new downloadMLBTeamRosterThread(team_short_name);
    dmtrThread.start();
    try {
      dmtrThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    n_players = dmtrThread.getNumberPlayers();
    if (n_players == 0) {
      Toast.makeText(StartUpActivity.this, "Need Internet connection to download MLB rosters", Toast.LENGTH_SHORT).show();
      return new String[0][0];
    }
    if (n_players == -1) {
      Toast.makeText(StartUpActivity.this,
          "Mismatch between number of player names and number of player numbers in MLB data for this team", Toast.LENGTH_SHORT).show();
      return new String[0][0];
    }
    unsorted_player_info = dmtrThread.getPlayerInfo();
    //Log.i(TAG, "roster: " + team_short_name + ": " + n_players + " players");
    String[][] sorted_player_info = new String[n_players][3]; // Number, name, position
    // Sort roster by player number (use alphabetical sort with " 1" etc.
    for (int i = 0; i < n_players; i++) sorted_player_info[i] = unsorted_player_info[i];
    Arrays.sort(sorted_player_info, (entry1, entry2) -> {
      final String s1 = entry1[0];
      final String s2 = entry2[0];
      return s1.compareTo(s2);
    });
    // Dedupe player info list (MLB includes multiple positions per player)
    ArrayList<String[]> sorted_deduped_player_info = new ArrayList<>();
    String current_name = sorted_player_info[0][1];
    sorted_deduped_player_info.add(sorted_player_info[0]);
    for (int i = 1; i < n_players - 1; i++){
      if (!sorted_player_info[i][1].equals(current_name)) {
        sorted_deduped_player_info.add(sorted_player_info[i]);
        current_name = sorted_player_info[i][1];
      }
    }
    return sorted_deduped_player_info.toArray(new String[sorted_deduped_player_info.size()][3]);
  }  // End public String[][] downloadMLBTeamRoster

  class downloadMLBTeamRosterThread extends Thread{
    String team_name;
    int n_players;
    downloadMLBTeamRosterThread(String team_name) {this.team_name = team_name;}
    private volatile String[][] player_info_temp = new String[max_players_on_roster][3]; // Number, name, position
    public void run() {
      try {
        n_players = 0;
        String[] position_group_names = {"Rotation", "Bullpen", "Catcher", "First Base", "Second Base", "Third Base", "Shortstop",
            "Left Field", "Center Field", "Right Field", "Designated Hitter"};
        String[] position_group_abbreviations = {"P", "P", "C", "1B", "2B", "3B", "SS", "LF", "CF", "RF", "DH"};
        Document doc = Jsoup.connect("https://www.mlb.com/" + team_name + "/roster/depth-chart/").get();
        Elements position_groups = doc.select("table.roster__table");
        for (Element position_group : position_groups) {
          String s = position_group.toString();
          int start = s.indexOf("<td colspan=\"2\">");
          int end = s.indexOf("</td>", start);
          String position = s.substring(start + 16, end);
          // Find which position this is
          int j = 0;
          while (!position.equals(position_group_names[j])) j++;
          String position_abbreviation = position_group_abbreviations[j];
          Document position_group_doc = parse(s);
          Elements player_numbers = position_group_doc.select("span.jersey");
          int number_player_numbers_in_position_group = player_numbers.size();
          //Log.i(TAG, "Roster - number of player numbers: " + position_abbreviation + ": " + number_player_numbers_in_position_group);
          int i = n_players;
          for (Element player_number : player_numbers) {
            if (player_number.text().equals("")) player_info_temp[i][0] = " 0";
            else player_info_temp[i][0] = format("%2s", player_number.text());
            ++i;
          }
          Elements player_names = position_group_doc.select("a[href^=/player]");
          int number_player_names_in_position_group = player_names.size();
          //Log.i(TAG, "Roster - number of player names: " + position_abbreviation + ": " + number_player_names_in_position_group);
          if (number_player_numbers_in_position_group != number_player_names_in_position_group) {  // This should never happen
            n_players = -1;
            return;
          }
          i = n_players;
          for (Element player_name : player_names) {
            player_info_temp[i][1] = player_name.text(); player_info_temp[i++][2] = position_abbreviation;
          }
          n_players += player_numbers.size();
        }
      } catch (IOException ex) {
        // NOTE: if an error like website not found or no internet connection, thread will return and n_players will be 0
      //Log.i(TAG, "jsoup exception:" + ex);
      }
    }  // End public void run
    public String[][] getPlayerInfo() { return player_info_temp; }
    public int getNumberPlayers() { return n_players; }
  } // End class downloadMLBTeamRosterThread

  public String[][] parse_roster_json_player_info(String json_str) {
    ArrayList<String[]> player_array = new ArrayList<>();
    try {
      JSONObject jsonObj = new JSONObject(json_str);
      JSONArray players = jsonObj.getJSONArray("Players");
      //Log.i(TAG, "Team: " + players.length() + " players");
      for (int j = 0; j < players.length(); j++) {
        String[] t = new String[3];
        JSONObject player_info = players.getJSONObject(j);
        t[0] = player_info.getString("Number");
        t[1] = player_info.getString("Name");
        t[2] = player_info.getString("Pos");
        player_array.add(t);
      }
    } catch (JSONException ex) {
    //Log.i("json Exception", ex.getMessage());
    }
    return player_array.toArray(new String[player_array.size()][3]);
  }

  public void initialize_from_game_file(String game_file_json_str, boolean game_complete, final Intent intent) {
    //Log.i(TAG, "Initializing game: " + game_file_json_str.substring(0,300) + "\ngame_complete="+ game_complete);
    Bitmap thumbnail_bitmap;
    try{
      JSONObject jsonObj = new JSONObject(game_file_json_str);
      JSONObject game = jsonObj.getJSONObject("Game");
      team_name[0] = game.getString("Visitors");
      team_name[1] = game.getString("Home");
      team_color[0] = Color.parseColor(game.getString("VisitorsColor"));
      team_color[1] = Color.parseColor(game.getString("HomeColor"));
      date_str = game.getString("Date");
      time_str = game.getString("Time");
      //Log.i(TAG, "Visitors=" + team_name[0] + " Home=" + team_name[1] + " Date=" + date_str + " Time=" + time_str);
      //Log.i(TAG, "Visitors color=" + team_color[0] + " Home color=" + team_color[1]);
      number_batters = parseInt(game.getString("NumberBatters"));
      number_innings_regulation = parseInt(game.getString("NumberInningsRegulation"));
      track_b_s = Boolean.parseBoolean(game.getString("Track_b_s"));
      initialize_vars(number_batters, number_innings_regulation);
      //Log.i(TAG, "number_batters=" + number_batters + " number_innings_regulation=" + number_innings_regulation);
      JSONArray players = jsonObj.getJSONArray("VisitorPlayers");
      number_players[0] = players.length();
      //Log.i(TAG, "Found " + players.length() + " visiting players");
      for (int i = 0; i < players.length(); i++) {
        JSONObject player_data = players.getJSONObject(i);
        player_info[i][0][0] = player_data.getString("Number");
        player_info[i][1][0] = player_data.getString("Name");
        player_info[i][2][0] = player_data.getString("Pos");
      }
      players = jsonObj.getJSONArray("HomePlayers");
      number_players[1] = players.length();
      //Log.i(TAG, "Found " + players.length() + " home players");
      for (int i = 0; i < players.length(); i++) {
        JSONObject player_data = players.getJSONObject(i);
        player_info[i][0][1] = player_data.getString("Number");
        player_info[i][1][1] = player_data.getString("Name");
        player_info[i][2][1] = player_data.getString("Pos");
      }
      JSONArray innings = jsonObj.getJSONArray("Innings");
     //Log.i(TAG, "found " + innings.length()/2.0 + " innings");
      for (int i = 0; i < innings.length(); i++) {
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
          //Log.i(TAG, "In initialize_from_game_file: atBat_sequence_index[" + inning_half + "]=" + atBat_sequence_index[inning_half]);
          atBat_sequence_array[atBat_sequence_index[inning_half]][inning_half] = atBatInd;
          ++atBat_sequence_index[inning_half];
          atBat_state_array[atBatInd][inning_half] = parseInt(atBat.getString("State"));
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
        // Draw line underneath last batter in inning except for last inning
        if (i < (innings.length()-1)) {
          //--atBat_sequence_index[inning_half];
          int last_up_ind = atBat_sequence_array[atBat_sequence_index[inning_half] - 1][inning_half];
          thumbnail_bitmap = thumbnail_bitmap_array[last_up_ind][inning_half];
          thumbnail_canvas = new Canvas(thumbnail_bitmap);
          thumbnail_paint = new Paint();
          drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[inning_half], sw, bottom_line);
          thumbnail_bitmap_array[last_up_ind][inning_half] = thumbnail_bitmap;
          //Log.i(TAG, "Initialize: atBat_sequence_index[" + inning_half +"]="  + atBat_sequence_index[inning_half]);
        }
      }  // End for (i = 0; i < innings.length(); i++)
    } catch (JSONException ex) {
    //Log.i("json Exception", ex.getMessage());
    }
    if (!game_complete) {
      if (atBat_sequence_index[inning_half] < 0) { // This is here for malformed game file with no at bats
        Toast.makeText(StartUpActivity.this, "Error in game file, exiting", Toast.LENGTH_LONG).show();
        this.finishAffinity();
        finish();
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
//      Toast.makeText(this, "Exiting", Toast.LENGTH_SHORT).show();  // Won't show with System.exit
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
        use_ghost_runner = data.getBooleanExtra("use_ghost_runner", true);
        initialize_vars(new_number_batters, new_number_innings_regulation);
      }
      //Log.i(TAG, "Return from SettingsActivity: resultCode=" + resultCode + " new_number_batters=" + new_number_batters//
      // + " new_number_innings_regulation=" + new_number_innings_regulation + " track_b_s=" + track_b_s
      // + " use_ghost_runner=" + use_ghost_runner);
    }
    else if (requestCode == ACTIVITY_ADDTEAM) {
      if (resultCode == RESULT_OK) {
        int visitor_or_home = data.getIntExtra("VisitorOrHome", 0);
        team_name[visitor_or_home]  = data.getStringExtra("TeamName");
        team_color[visitor_or_home] = data.getIntExtra("TeamColor", 0);
        int[] default_colors = {Color.RED, Color.BLUE};
        if (team_color[visitor_or_home] == 0) team_color[visitor_or_home] = default_colors[visitor_or_home];
        //Log.i(TAG, "team[" + visitor_or_home + "] name=" + team_name[visitor_or_home]);
        //Log.i(TAG, "team[" + visitor_or_home + "] color=" + team_color[visitor_or_home]);
        team_set[visitor_or_home] = true;
        selectTeams[visitor_or_home].setText(team_name[visitor_or_home]);
        selectTeams[visitor_or_home].setTextColor(team_color[visitor_or_home]);
        if (team_set[0] && team_set[1]) play_ball(team_name, intent_main);
        //Log.i(TAG, "Return from AddTeamActivity: resultCode=" + resultCode + " team_name=" + team_name[visitor_or_home]);
      }
    }  // End  else if (requestCode == ACTIVITY_ADDTEAM)
    else if (requestCode == ACTIVITY_HELP) {
      if (resultCode == RESULT_OK) {}
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
      case R.id.action_game_files:
        //region
        //Log.i(TAG, "Open game file selected. Directory path=" + directory_path);
        String[] filenames = new File(directory_path).list();
        //  for (String filename : filenames) Log.i(TAG, "game_filename: " + filename);
        final PopupMenu game_file_popup = new PopupMenu(this, findViewById(R.id.popup_insert_point));
        for (String filename : filenames) if (filename.startsWith("20")) game_file_popup.getMenu().add(filename);
        //Log.i(TAG, "number of games= " + game_file_popup.getMenu().size());
        if ((game_file_popup.getMenu().size()) == 0) {
          Toast.makeText(StartUpActivity.this, "No game files available", Toast.LENGTH_SHORT).show();
        }
        else {
          game_file_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
              String game_filename = item.getTitle().toString();
              game_file = new File(directory_path + game_filename);
              String game_file_json_str = read_file(directory_path, game_filename);
              if (!game_file_json_str.contains("GameOver")) {  // Continuing game, make game file writable, write contents back to it
                Toast.makeText(StartUpActivity.this, "Continuing game from file " + game_filename, Toast.LENGTH_SHORT).show();
                game_complete = false;
                try {
                  game_file_bufferedWriter = new BufferedWriter(new FileWriter(game_file));
                  game_file_bufferedWriter.write(game_file_json_str);
                  game_file_bufferedWriter.flush();
                } catch (IOException ex) {
                  //Log.i(TAG, "game file IOException" + ex.getMessage());
                  Toast.makeText(StartUpActivity.this, "game file IOException" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
                game_file_json_str += "]}\n";  // Add the final json closing brackets to enable parsing
              }  // End if (!game_file_json_str.contains("GameOver"))
              else {  // Game file contains complete game
                Toast.makeText(StartUpActivity.this, "Reading complete game from file " + game_filename, Toast.LENGTH_SHORT).show();
                game_complete = true;
              }
              int json_len = game_file_json_str.length();
              //Log.i(TAG, "game_file_json_str final length=" + json_len);
              //Log.i(TAG,  "game_file_json_str final first part=" + game_file_json_str);
              if (json_len > 3000) //Log.i(TAG,  "game_file_json_str final last part=" + game_file_json_str.substring(json_len-3000));
                initialize_from_game_file(game_file_json_str, game_complete, intent_main);
              return true;
            }
          });
          game_file_popup.show();
        }
        break;
        //endregion
      case R.id.action_help:
        //region
        Intent intent_help = new Intent(this, HelpActivity.class);
        startActivityForResult(intent_help, ACTIVITY_SETTINGS);
        return true;
      //endregion
      case R.id.action_exit:
        //region
        //Log.i(TAG, "Exit");
//        Toast.makeText(this, "Exiting", Toast.LENGTH_LONG).show();  // Won't show with System.exit()
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
