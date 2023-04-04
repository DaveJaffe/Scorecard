package com.dj.sc;

/*
AtBatActivity.java - At Bat screen of Scorecard application

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


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static java.lang.Math.*;
import static java.lang.String.format;

import static com.dj.sc.StartUpActivity.base_text_size;
import static com.dj.sc.StartUpActivity.density;
import static com.dj.sc.StartUpActivity.drawField;
import static com.dj.sc.StartUpActivity.heightPx;
import static com.dj.sc.StartUpActivity.hideNavigation;
import static com.dj.sc.StartUpActivity.outline;
import static com.dj.sc.StartUpActivity.real_aspect_ratio;
import static com.dj.sc.StartUpActivity.thumbnail_bases;
import static com.dj.sc.StartUpActivity.pitcher_balls;
import static com.dj.sc.StartUpActivity.pitcher_number_names;
import static com.dj.sc.StartUpActivity.pitcher_pitches;
import static com.dj.sc.StartUpActivity.pitcher_strikes;
import static com.dj.sc.StartUpActivity.track_b_s;
import static com.dj.sc.StartUpActivity.pitcher_batters_faced;
import static com.dj.sc.StartUpActivity.pitcher_earned_runs;
import static com.dj.sc.StartUpActivity.current_pitcher_index;
import static com.dj.sc.StartUpActivity.pitcher_bb;
import static com.dj.sc.StartUpActivity.pitcher_errors;
import static com.dj.sc.StartUpActivity.pitcher_hits;
import static com.dj.sc.StartUpActivity.pitcher_k;
import static com.dj.sc.StartUpActivity.pitcher_outs;
import static com.dj.sc.StartUpActivity.pitcher_runs;
import static com.dj.sc.StartUpActivity.batter_errors;
import static com.dj.sc.StartUpActivity.batter_pa;
import static com.dj.sc.StartUpActivity.batter_position;
import static com.dj.sc.StartUpActivity.batter_rbi;
import static com.dj.sc.StartUpActivity.team_errors;
import static com.dj.sc.StartUpActivity.team_rbi;
import static com.dj.sc.StartUpActivity.atBat_result_is_ab;
import static com.dj.sc.StartUpActivity.atBat_result_is_hit;
import static com.dj.sc.StartUpActivity.atBat_result_text;
import static com.dj.sc.StartUpActivity.atBat_result_types;
import static com.dj.sc.StartUpActivity.drawBasePaths;
import static com.dj.sc.StartUpActivity.drawLinesColor;
import static com.dj.sc.StartUpActivity.markHit;
import static com.dj.sc.StartUpActivity.markOut;
import static com.dj.sc.StartUpActivity.o_loc;
import static com.dj.sc.StartUpActivity.o_r;
import static com.dj.sc.StartUpActivity.oc_loc;
import static com.dj.sc.StartUpActivity.ot_loc;
import static com.dj.sc.StartUpActivity.result_ind;
import static com.dj.sc.StartUpActivity.sw;
import static com.dj.sc.StartUpActivity.thumbnail_locations;
import static com.dj.sc.StartUpActivity.ts;
import static com.dj.sc.StartUpActivity.inning_half_batters;
import static com.dj.sc.StartUpActivity.number_batters;
import static com.dj.sc.StartUpActivity.number_innings;
import static com.dj.sc.StartUpActivity.team_color;
import static com.dj.sc.StartUpActivity.thumbnail_bitmap_array;
import static com.dj.sc.StartUpActivity.atBat_array;
import static com.dj.sc.StartUpActivity.atBat_state_array;
import static com.dj.sc.StartUpActivity.batter_number_names;
import static com.dj.sc.StartUpActivity.batter_abs;
import static com.dj.sc.StartUpActivity.batter_runs;
import static com.dj.sc.StartUpActivity.batter_hits;
import static com.dj.sc.StartUpActivity.team_abs;
import static com.dj.sc.StartUpActivity.team_runs;
import static com.dj.sc.StartUpActivity.team_hits;
import static com.dj.sc.StartUpActivity.outs;
import static com.dj.sc.StartUpActivity.on_base;
import static com.dj.sc.StartUpActivity.inning;
import static com.dj.sc.StartUpActivity.inning_header_values;
import static com.dj.sc.StartUpActivity.inning_hits;
import static com.dj.sc.StartUpActivity.inning_runs;
import static com.dj.sc.StartUpActivity.next_up;
import static com.dj.sc.StartUpActivity.atBat_sequence_array;
import static com.dj.sc.StartUpActivity.atBat_sequence_index;
import static com.dj.sc.StartUpActivity.team_up;

public class AtBatActivity extends AppCompatActivity {
  private static final String TAG = AtBatActivity.class.getName();

  // region Declarations
  CanvasView focus_canvas_view;
  Canvas canvas;
  Paint paint;
  Bitmap focus_bitmap, thumbnail_bitmap, thumbnail_bitmap_next_batter;
  Canvas thumbnail_canvas;
  Paint thumbnail_paint;

  int atBatInd, number_bases, adv_n_bases = 0, quadrant;
  float[] thumbnail_location;
  TextView comment_textbox_tv, focus_textbox_tv;
  String batter_number_name, pitcher_number_name, result_text;

  int earned_runs;
  int n_errors_during_play;
  int[] errors_during_play;
  int n_balls = 0;
  int n_strikes = 0; // Strikes thrown during at bat including foul balls on 2 strikes
  int n_fouls = 0;   // Calculated as n_strikes - 2 since on 3 (real) strikes out is recorded

  boolean is_safe, is_out, is_ab, is_hit, is_run, is_stolen_base, fill, already_selected;

  int focus_height, focus_width, margin_x, margin_y, margin_r, margin_l, margin_b, margin_t, text_size, stroke_width, outs_radius;
  float fh, fw, fw_half, fw_third, fh_half, fh_third, fh_fourth, base_distance, outfield_fence_distance, base_side;
  float[] bases, outs_loc, outs_circle_loc, out_text_loc, lower_left, lower_right, upper_right, upper_left, location;
  float[][] locations = new float[5][2];
  float[] ball_in_play_location = {0.0f, 0.0f};

  int X, Y;
  // endregion

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.atbat_activity);
    
    hideNavigation(getWindow().getDecorView());

    // region Focus dimensions
    if (real_aspect_ratio < 2.0) focus_height = (int) round(0.92 * heightPx);
    else focus_height = (int) round(0.88 * heightPx);
    focus_width = (int) round(1.414 * focus_height);
    fh = (float) focus_height;
    fw = (float) focus_width;
    fw_half = fw/2.0f;  // Half of focus width
    fw_third = fw/3.0f;  // Third of focus width
    fh_half = fh/2.0f;  // Half of focus height
    fh_third = fh/3.0f;  // Third of focus height
    fh_fourth = fh/4.0f;  // Fourth of focus height
    margin_x = (int) round(0.02 * focus_width);  // Horizontal margin for field lines
    margin_y = (int) round(0.02 * focus_height); // Vertical margin for field lines
    margin_l = (int) round(0.30 * focus_width); // Left margin for focus base hit marks
    margin_r = (int) round(0.35 * focus_width); // Right margin for focus base hit marks
    margin_b = (int) round(0.10 * focus_height); // Bottom margin for focus base hit marks
    margin_t = (int) round(0.55 * focus_height); // Top margin for focus base hit marks
    stroke_width = (int) round(0.024 * focus_height); // Stroke width for focus base hit lines
    text_size = (int) floor(base_text_size * 1.5);
    outfield_fence_distance = fh - 2 * margin_y;
    base_distance = outfield_fence_distance/2.5f;
    base_side = .707f * base_distance;      // Side of triangle formed by base path and bottom of canvas
    bases = new float[]{fw_half, fh - margin_y,                          // Home plate
                        fw_half + base_side, fh - margin_y - base_side,  // First base
                        fw_half, fh - margin_y - 2*base_side,            // Second base
                        fw_half - base_side, fh - margin_y - base_side,  // Third base
                        fw_half, fh - margin_y};                         // Home plate
    outs_radius = (int) round(0.04 * focus_height); // Radius of circle around outs in focus
    outs_loc = new float[]{0.98f * fw_half, 0.98f * fh_third};  // Location of outs number
    outs_circle_loc = new float[]{fw_half, 0.92f * fh_third};   // Center of circle around outs in focus
    out_text_loc = new float[]{0.95f * fw_half, 3 * fh_fourth}; // Location of outs text
    lower_left = new float[]{margin_l, fh - margin_b}; // Locations for base hit markers
    lower_right = new float[]{fw - margin_r, fh - margin_b};
    upper_right = new float[]{fw - margin_r, margin_t};
    upper_left = new float[]{margin_l, margin_t};
    locations[0] = null; locations[1] = lower_right; locations[2] = upper_right; locations[3] = upper_left; locations[4] = lower_left;
    //Log.i(TAG, "Dimension: focus_width=" + focus_width + " focus_height=" + focus_height);
    // endregion

    // region Textboxes and action bar
    comment_textbox_tv = findViewById(R.id.comment_textbox);
    focus_textbox_tv = findViewById(R.id.focus_textbox);
    if (density > 3.0) focus_textbox_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,0.7f * focus_textbox_tv.getTextSize());

    atBatInd = getIntent().getIntExtra("ATBATIDX", 0);
    int team_displayed = getIntent().getIntExtra("TEAM_DISP", 0);
    //Log.i(TAG, "Upon entering AtBatActivity atBatInd=" + atBatInd + " team_up=" + team_up + " team_displayed=" + team_displayed);
    // If team_up != team_displayed this is just to view a previous at bat on the team that isn't currently up
    // All status should be 0 or 3. No activities are allowed. So set them equal for this at bat
    team_up = team_displayed;
    this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(team_color[team_up]));

    View insert_point = findViewById(R.id.insert_point);
    // endregion

    // region Set up popup menus

    // Outs
    final PopupMenu atbat_popup0 = new PopupMenu(this, insert_point);
    for (String result_type : atBat_result_types[0]) atbat_popup0.getMenu().add(result_type);
    atbat_popup0.getMenu().removeItem(0);  // Remove blank at front of list
    
    // 1 through 4 base events
    final PopupMenu atbat_popup1 = new PopupMenu(this, insert_point);
    for (String result_type : atBat_result_types[1]) atbat_popup1.getMenu().add(result_type);

    final PopupMenu atbat_popup2 = new PopupMenu(this, insert_point);
    for (String result_type : atBat_result_types[2]) atbat_popup2.getMenu().add(result_type);

    final PopupMenu atbat_popup3 = new PopupMenu(this, insert_point);
    for (String result_type : atBat_result_types[3]) atbat_popup3.getMenu().add(result_type);

    final PopupMenu atbat_popup4 = new PopupMenu(this, insert_point);
    for (String result_type : atBat_result_types[4]) atbat_popup4.getMenu().add(result_type);
    
    final PopupMenu onbase_popup = new PopupMenu(this, insert_point);
    for (String result_type : atBat_result_types[5]) onbase_popup.getMenu().add(result_type);

    final PopupMenu RBIs_popup = new PopupMenu(this, insert_point);
    for (int i = 1; i <= 4; i++) RBIs_popup.getMenu().add(Integer.toString(i));

    final PopupMenu errors_popup = new PopupMenu(this, insert_point);
    errors_popup.getMenu().add("P");
    errors_popup.getMenu().add("C");
    errors_popup.getMenu().add("1B");
    errors_popup.getMenu().add("2B");
    errors_popup.getMenu().add("3B");
    errors_popup.getMenu().add("SS");
    errors_popup.getMenu().add("LF");
    errors_popup.getMenu().add("CF");
    errors_popup.getMenu().add("RF");
    errors_popup.getMenu().add("DH");

    final PopupMenu strikeout_type_popup = new PopupMenu(this, insert_point);
    strikeout_type_popup.getMenu().add("Strikeout Swinging");
    strikeout_type_popup.getMenu().add("Strikeout Looking");
    
    // endregion

    // region Set up focus bitmap
    focus_canvas_view = findViewById(R.id.focus_canvas);
    focus_canvas_view.setLayoutParams(new LinearLayout.LayoutParams(focus_width, focus_height));
    focus_bitmap = Bitmap.createBitmap(focus_width, focus_height, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(focus_bitmap);
    paint = new Paint();
    paint.setColor(Color.LTGRAY);
    canvas.drawRect(0, 0, focus_width, focus_height, paint);
    paint.setColor(team_color[team_up]);
    drawField(canvas, paint, Color.DKGRAY, 5, focus_width, focus_height, margin_x, margin_y);
    drawBasePaths(canvas, paint, Color.DKGRAY, 5, bases, 4, false);
    focus_canvas_view.bitmap = focus_bitmap;
    focus_canvas_view.left = 0; focus_canvas_view.top = 0; focus_canvas_view.right = focus_width; focus_canvas_view.bottom = focus_height;
    focus_canvas_view.invalidate();
    // endregion

    // region Set up comments text edit
    EditText commentEdit = findViewById(R.id.commentEditText);
    if (!atBat_array[atBatInd][team_up].comment.equals("")) {
      //comment_textbox_tv.setText("");
      commentEdit.setText(atBat_array[atBatInd][team_up].comment);
      commentEdit.setSelection(commentEdit.getText().length());
    }
    commentEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          atBat_array[atBatInd][team_up].comment = view.getText().toString();
        }
        return false;
      }
    });
    // endregion

    // region Handle atBat
    is_safe = false; is_out = false;
    n_errors_during_play = 0;
    errors_during_play = new int[]{0, 0, 0, 0};
    String batter_name="", pitcher_name="";
    int team_in_field; if (team_up == 0) team_in_field = 1; else team_in_field = 0;

    // Set up AtBat layout depending on whether batter hasn't batted yet, is at bat, is on base, or is finished
    if (atBat_state_array[atBatInd][team_up] == 0) {  // Hasn't batted yet
      focus_textbox_tv.setText("At Bat: " + atBatInd + " AtBat State: " + atBat_state_array[atBatInd][team_up]);
      this.setTitle("");
    }  // End Hasn't batted yet
    else if (atBat_state_array[atBatInd][team_up] == 1) {  // Current batter
      focus_textbox_tv.setText("At Bat: " + atBatInd + " AtBat State: " + atBat_state_array[atBatInd][team_up]);
      String inning_half_str;
      if (team_up == 0) inning_half_str = "Top"; else inning_half_str = "Bottom";
      String inning_str = format(Locale.US, "%s of %d, %d outs", inning_half_str, inning, outs[team_up]);
      // Use only batter/pitcher name in the AtBat title if specified; if only number specified use that
      batter_number_name = batter_number_names[atBatInd % number_batters][team_up];
      //Log.i(TAG, "atBat current batter batter_number_name: |" + batter_number_name  + "|");
      if (batter_number_name.equals("")) batter_name = "";    // Neither number nor name specified
      else if (batter_number_name.length() == 4) batter_name = batter_number_name.substring(0,3); // Number only
      else if (batter_number_name.length() > 4) batter_name = batter_number_name.substring(4); // Name only
      //Log.i(TAG, "atBat current batter batter_name: |" + batter_name  + "|");
      pitcher_number_name = pitcher_number_names[current_pitcher_index[team_in_field]][team_in_field];
      //Log.i(TAG, "atBat current batter pitcher_number_name: |" + pitcher_number_name  + "|");
      if (pitcher_number_name.equals("")) pitcher_name = "";    // Neither number nor name specified
      else if (pitcher_number_name.length() == 4) pitcher_name = pitcher_number_name.substring(0,3); // Number only
      else if (pitcher_number_name.length() > 4) pitcher_name = pitcher_number_name.substring(4); // Name only
      //Log.i(TAG, "atBat current batter pitcher_name: |" + pitcher_name  + "|");
      this.setTitle(inning_str + ":   " + batter_name + " At Bat      Pitching: " +  pitcher_name);
      //Log.i(TAG, "Current batter: AtBatActivity: atBat_sequence_array[" + atBat_sequence_index[team_up] + "][" + team_up + "]="  +
      // atBat_sequence_array[atBat_sequence_index[team_up]][team_up]);
      atBat_array[atBatInd][team_up].batter_number_name = batter_number_name;
      atBat_array[atBatInd][team_up].pitcher_number_name = pitcher_number_names[current_pitcher_index[team_in_field]][team_in_field];
      atBat_array[atBatInd][team_up].inning = inning;
      atBat_array[atBatInd][team_up].inning_half = team_up;
      already_selected = false;
      focus_canvas_view.setOnTouchListener(new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
             X = (int) event.getX();
             Y = (int) event.getY();
            if (pow(pow((X - fw_half),2) + pow((Y - (fh - margin_y)),2),.5) >= outfield_fence_distance) quadrant = 4; // Home run
            else if (pow(pow((X - fw_half),2) + pow((Y - (fh - margin_y - base_side)),2),.5) < base_distance/2) quadrant = 0; // Circle in center of infield
            else if (X > fw_half && Y > (fh - margin_y - base_side)) quadrant = 1; // Lower right
            else if (X > fw_half && Y < (fh - margin_y - base_side)) quadrant = 2; // Upper right
            else if (X < fw_half && Y < (fh - margin_y - base_side)) quadrant = 3; // Upper left
            else quadrant = 4;                       // Lower left
            //Log.i(TAG, "X=" + X + "  Y=" + Y + " quadrant=" + quadrant);
          }
          return false;
        }
      });
      focus_canvas_view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          if (already_selected) {
            paint.setColor(team_color[team_up]);
            paint.setStrokeWidth(2);
            canvas.drawLine(bases[0], bases[1], X, Y, paint );
            focus_canvas_view.invalidate();
            ball_in_play_location[0] = X; ball_in_play_location[1] = Y;
            //Log.i(TAG, "Ball_in_play_location X= " + X + " Y=" + Y);
          }
          else {
            PopupMenu atbat_popup = atbat_popup0;
            if (quadrant == 1) atbat_popup = atbat_popup1;
            else if (quadrant == 2) atbat_popup = atbat_popup2;
            else if (quadrant == 3) atbat_popup = atbat_popup3;
            else if (quadrant == 4) atbat_popup = atbat_popup4;
            atbat_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem item) {
                already_selected = true;
                ProcessResult((String) item.getTitle());
                return true;
              }
            });
            atbat_popup.show();
          }
        }
      });

      final Button RBIButton = findViewById(R.id.RBIButton);
      RBIButton.setVisibility(View.VISIBLE);
      RBIButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          RBIs_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
              //Toast.makeText(AtBatActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
              int RBIs = Integer.parseInt(item.getTitle().toString());
              atBat_array[atBatInd][team_up].RBIs = RBIs;
              return true;
            }
          });
          RBIs_popup.show();
        }
      });  // End RBIs.setOnClickListener

      final Button errorButton = findViewById(R.id.ErrorButton);
      errorButton.setVisibility(View.VISIBLE);
      errorButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          errors_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
              //Toast.makeText(AtBatActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
              String error_position = item.getTitle().toString();
              atBat_array[atBatInd][team_up].error_during_at_bat_or_on_base = true;
              int team_in_field; if (team_up == 0) team_in_field = 1; else team_in_field = 0;
              int i = 0;
              while (!(batter_position[i++][team_in_field].equals(error_position)) && i < number_batters) {}  // Locate other team batter playing that position
              if (i == number_batters) {  // Error position not found among other team's batters
                if (error_position.equals("P")) {
                  errors_during_play[n_errors_during_play++] = -1;
                  //Log.i(TAG, "Fielding error: n_error_in_play=" + n_errors_during_play + " error_position=P");
                }
                else Toast.makeText(AtBatActivity.this,
                    "Nobody at that position. Hit undo button, add a player at that position and re-enter play.", Toast.LENGTH_SHORT).show();
              }
              else {
                errors_during_play[n_errors_during_play++] = i - 1;
                //Log.i(TAG, "Fielding error: n_error_in_play=" + n_errors_during_play + " error_position=" + error_position +
                //  " batting order pos of player who made E=" + (i - 1) + " name of of player who made E=" + batter_number_names[i - 1][team_in_field]);
              }
              return true;
            }
          });
          errors_popup.show();
        }
      });  // End errorButton.setOnClickListener

      if (track_b_s) {
        TextView track_bs_title_tv = findViewById(R.id.track_b_s_title);
        track_bs_title_tv.setVisibility(View.VISIBLE);

        // If balls and strikes were stored by a previous return, start with those values
        n_balls = atBat_array[atBatInd][team_up].balls;
        n_strikes = atBat_array[atBatInd][team_up].strikes;
        //Log.i(TAG, "Track_b_s: balls=" + n_balls + " strikes=" + n_strikes);

        final CheckBox ball1_checkbox = findViewById(R.id.ball1);
        final CheckBox ball2_checkbox = findViewById(R.id.ball2);
        final CheckBox ball3_checkbox = findViewById(R.id.ball3);
        final CheckBox ball4_checkbox = findViewById(R.id.ball4);

        ball1_checkbox.setVisibility(View.VISIBLE); ball1_checkbox.setEnabled(true);
        ball2_checkbox.setVisibility(View.VISIBLE); ball2_checkbox.setEnabled(false);
        ball3_checkbox.setVisibility(View.VISIBLE); ball3_checkbox.setEnabled(false);
        ball4_checkbox.setVisibility(View.VISIBLE); ball4_checkbox.setEnabled(false);

        final CheckBox strike1_checkbox = findViewById(R.id.strike1);
        final CheckBox strike2_checkbox = findViewById(R.id.strike2);
        final CheckBox strike3_checkbox = findViewById(R.id.strike3);
        final Button foul_button = findViewById(R.id.foulButton);
        final TextView number_of_fouls_tv = findViewById(R.id.number_of_fouls);

        strike1_checkbox.setVisibility(View.VISIBLE); strike1_checkbox.setEnabled(true);
        strike2_checkbox.setVisibility(View.VISIBLE); strike2_checkbox.setEnabled(false);
        strike3_checkbox.setVisibility(View.VISIBLE); strike3_checkbox.setEnabled(false);
        foul_button.setVisibility(View.INVISIBLE); foul_button.setEnabled(false);
        number_of_fouls_tv.setVisibility(View.INVISIBLE);

        if (n_balls > 0) {
          ball1_checkbox.setChecked(true);
          ball1_checkbox.setEnabled(false);
          ball2_checkbox.setEnabled(true);
        }
        else {
          ball1_checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              ++n_balls;
              ball2_checkbox.setEnabled(true);
              ball1_checkbox.setEnabled(false);
            }
          });
        }

        if (n_balls > 1) {
          ball2_checkbox.setChecked(true);
          ball2_checkbox.setEnabled(false);
          ball3_checkbox.setEnabled(true);
        }
        else {
          ball2_checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              ++n_balls;
              ball3_checkbox.setEnabled(true);
              ball2_checkbox.setEnabled(false);
            }
          });
        }

        if (n_balls > 2) {
          ball3_checkbox.setChecked(true);
          ball3_checkbox.setEnabled(false);
          ball4_checkbox.setEnabled(true);
        }
        else {
          ball3_checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              ++n_balls;
              ball4_checkbox.setEnabled(true);
              ball3_checkbox.setEnabled(false);
            }
          });
        }

        ball4_checkbox.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            ++n_balls;
            ball4_checkbox.setEnabled(false);
            strike1_checkbox.setEnabled(false);
            strike2_checkbox.setEnabled(false);
            strike3_checkbox.setEnabled(false);
            ProcessResult("Walk");
          }
        });

        if (n_strikes > 0) {
          strike1_checkbox.setChecked(true);
          strike1_checkbox.setEnabled(false);
          strike2_checkbox.setEnabled(true);
        }
        else {
          strike1_checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              ++n_strikes;
              strike2_checkbox.setEnabled(true);
              strike1_checkbox.setEnabled(false);
            }
          });
        }

        if (n_strikes > 1) {
          strike2_checkbox.setChecked(true);
          strike2_checkbox.setEnabled(false);
          strike3_checkbox.setEnabled(true);
          foul_button.setVisibility(View.VISIBLE); foul_button.setEnabled(true);
          number_of_fouls_tv.setVisibility(View.VISIBLE);
        }
        strike2_checkbox.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            ++n_strikes;
            strike3_checkbox.setEnabled(true);
            foul_button.setVisibility(View.VISIBLE); foul_button.setEnabled(true);
            number_of_fouls_tv.setVisibility(View.VISIBLE);
            strike2_checkbox.setEnabled(false);
          }
        });

        if (n_strikes > 2) {
          n_fouls = n_strikes - 2;
          if (n_fouls == 1) number_of_fouls_tv.setText(n_fouls + " Foul"); else number_of_fouls_tv.setText(n_fouls + " Fouls");
        }
        strike3_checkbox.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            ++n_strikes;
            strike3_checkbox.setChecked(true);
            strike3_checkbox.setEnabled(false);
            foul_button.setEnabled(false);
            ball1_checkbox.setEnabled(false);
            ball2_checkbox.setEnabled(false);
            ball3_checkbox.setEnabled(false);
            final Button StrikeoutTypeButton = findViewById(R.id.StrikeoutTypeButton);
            StrikeoutTypeButton.setVisibility(View.VISIBLE);
            StrikeoutTypeButton.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                strikeout_type_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  public boolean onMenuItemClick(MenuItem item) {
                    //Toast.makeText(AtBatActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                    ProcessResult((String) item.getTitle());
                    return true;
                  }
                });
                strikeout_type_popup.show();
              }
            });  // End StrikeoutTypes.setOnClickListener
          }
        });

        foul_button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            ++n_strikes;
            n_fouls = n_strikes - 2;
            if (n_fouls == 1) number_of_fouls_tv.setText(n_fouls + " Foul"); else number_of_fouls_tv.setText(n_fouls + " Fouls");
          }
        });

      }  // End if (track_b_s)
    }  // End Current batter
    else if (atBat_state_array[atBatInd][team_up]== 2) {  // Current on base
      focus_textbox_tv.setText("At Bat: " + atBatInd + " AtBat State: " + atBat_state_array[atBatInd][team_up]);
      String inning_half_str;
      if (team_up == 0) inning_half_str = "Top"; else inning_half_str = "Bottom";
      String inning_str = format("%s of %d, %d outs", inning_half_str, inning, outs[team_up]);
      batter_number_name = atBat_array[atBatInd][team_up].batter_number_name;
      if (!batter_number_name.equals(batter_number_names[atBatInd % number_batters][team_up])) {  // Check for pinch runner
        batter_number_name = batter_number_names[atBatInd % number_batters][team_up];
        atBat_array[atBatInd][team_up].batter_number_name = batter_number_name;
      }
      // Use only batter/pitcher name in the AtBat title if specified; if only number specified use that
      //Log.i(TAG, "atBat on base batter_number_name: |" + batter_number_name  + "|");
      if (batter_number_name.equals("")) batter_name = "";    // Neither number nor name specified
      else if (batter_number_name.length() == 4) batter_name = batter_number_name.substring(0,3); // Number only
      else if (batter_number_name.length() > 4) batter_name = batter_number_name.substring(4); // Name only
      //Log.i(TAG, "atBat on base batter_name: |" + batter_name  + "|");
      pitcher_number_name = pitcher_number_names[current_pitcher_index[team_in_field]][team_in_field];
      //Log.i(TAG, "atBat on base pitcher_number_name: |" + pitcher_number_name  + "|");
      if (pitcher_number_name.equals("")) pitcher_name = "";    // Neither number nor name specified
      else if (pitcher_number_name.length() == 4) pitcher_name = pitcher_number_name.substring(0,3); // Number only
      else if (pitcher_number_name.length() > 4) pitcher_name = pitcher_number_name.substring(4); // Name only
      //Log.i(TAG, "atBat on base pitcher_name: |" + pitcher_name  + "|");
      this.setTitle(inning_str + ":   " + batter_name + " On Base      Pitching: " +  pitcher_name);
      // Mark how batter got on base and then apply on base events one at a time, marking how they got from base to base, eg. SB, +1B
      int c_b = 0;  // Current base for purposes of drawing
      for (int i=1; i <= atBat_array[atBatInd][team_up].current_base; i++) {
        int ob_step = atBat_array[atBatInd][team_up].ob[i];
        //Log.i(TAG, "onBase_step=" + ob_step + "  i=" + i +"  current_base=" +  atBat_array[atBatInd][team_up].current_base);
        if (ob_step > 0) {  // Eg. skip first base in the case of a double
          number_bases = ob_step / 100;
          int result_type_ind = ob_step % 100;
          result_text = atBat_result_text[number_bases][result_type_ind];
          if (number_bases > 0 && number_bases < 5) {  // Safe at bat
            is_safe = true;
            c_b = number_bases;
            location = locations[number_bases];
            thumbnail_location = thumbnail_locations[number_bases];
            fill = number_bases == 4;
          }   // End Safe at bat
          else {  // On base event
            if (result_type_ind < 4) {    // Safe on base
              is_safe = true;
              if (result_type_ind == 0 || result_type_ind == 3) c_b += 1;
              if (result_type_ind == 1) c_b += 2;
              if (result_type_ind == 2) c_b += 3;
              if (c_b > 4) c_b = 4; // Just in case
              fill = c_b == 4;
              location = locations[c_b];
              thumbnail_location = thumbnail_locations[c_b];
            }  // End Safe on base
            else is_out = true;  // Out on base
          }  // End On base event
          drawBasePaths(canvas, paint, team_color[team_up], stroke_width, bases, c_b, fill);
          if (is_safe)
            markHit(canvas, paint, team_color[team_up], text_size, result_text, location);
          else markOut(canvas, paint, team_color[team_up], text_size, outs[team_up], result_text,
              outs_loc, outs_circle_loc, outs_radius, out_text_loc);
          if (atBat_array[atBatInd][team_up].ball_in_play_location[0] > 0.0 || atBat_array[atBatInd][team_up].ball_in_play_location[1] > 0.0) {
            paint.setColor(team_color[team_up]);
            paint.setStrokeWidth(2);
            canvas.drawLine(bases[0], bases[1], atBat_array[atBatInd][team_up].ball_in_play_location[0],
                atBat_array[atBatInd][team_up].ball_in_play_location[1], paint);
          }
          focus_canvas_view.invalidate();
        }  // End if (ob_step > 0)
      }  // End for (int i=1; i <= atBat_array[atBatInd][team_up].current_base; i++)
      already_selected = false;
      focus_canvas_view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          onbase_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
              //Toast.makeText(AtBatActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
              if (already_selected) {
                Toast.makeText(AtBatActivity.this, "You already made a selection use undo button to make change", Toast.LENGTH_SHORT).show();
                return true;
              }
              already_selected = true;
              ProcessResult((String) item.getTitle());
              return true;
            }
          });
          onbase_popup.show();
        }
      });

      final Button errorButton = findViewById(R.id.ErrorButton);
      errorButton.setVisibility(View.VISIBLE);
      errorButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          errors_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
              //Toast.makeText(AtBatActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
              String error_position = item.getTitle().toString();
              atBat_array[atBatInd][team_up].error_during_at_bat_or_on_base = true;
              int team_in_field; if (team_up == 0) team_in_field = 1; else team_in_field = 0;
              int i = 0;
              while (!(batter_position[i++][team_in_field].equals(error_position)) && i < number_batters) {}  // Locate other team batter playing that position
              if (i == number_batters) {  // Error position not found among other team's batters
                if (error_position.equals("P")) {
                  errors_during_play[n_errors_during_play++] = -1;
                  //Log.i(TAG, "Fielding error: n_error_in_play=" + n_errors_during_play + " error_position=P");
                }
                else Toast.makeText(AtBatActivity.this,
                    "Nobody at that position. Hit undo button, add a player at that position and re-enter play.", Toast.LENGTH_SHORT).show();
              }
              else {
                errors_during_play[n_errors_during_play++] = i - 1;
                //Log.i(TAG, "Fielding error: n_error_in_play=" + n_errors_during_play + " error_position=" + error_position +
                //  " batting order pos of player who made E=" + (i - 1) + " name of of player who made E=" + batter_number_names[i - 1][team_in_field]);
              }
              return true;
            }
          });
          errors_popup.show();
        }
      });  // End errorButton.setOnClickListener
    }  // End Current on base
    else if (atBat_state_array[atBatInd][team_up] == 3) {  // Finished
      focus_textbox_tv.setText("At Bat: " + atBatInd + " AtBat State: " + atBat_state_array[atBatInd][team_up]);
      batter_number_name = atBat_array[atBatInd][team_up].batter_number_name;
      if (batter_number_name.equals("")) {   // Check if batter_number_name was set after AtBat
        batter_number_name = batter_number_names[atBatInd % number_batters][team_up];
        atBat_array[atBatInd][team_up].batter_number_name = batter_number_name;
      }
      // Use only batter/pitcher name in the AtBat title if specified; if only number specified use that
      //Log.i(TAG, "atBat finished batter_number_name: |" + batter_number_name  + "|");
      if (batter_number_name.equals("")) batter_name = "";    // Neither number nor name specified
      else if (batter_number_name.length() == 4) batter_name = batter_number_name.substring(0,3); // Number only
      else if (batter_number_name.length() > 4) batter_name = batter_number_name.substring(4); // Name only
      //Log.i(TAG, "atBat finished batter_name: |" + batter_name  + "|");
      pitcher_number_name = atBat_array[atBatInd][team_up].pitcher_number_name;
      //Log.i(TAG, "atBat finished pitcher_number_name: |" + pitcher_number_name  + "|");
      if (pitcher_number_name.equals("")) pitcher_name = "";    // Neither number nor name specified
      else if (pitcher_number_name.length() == 4) pitcher_name = pitcher_number_name.substring(0,3); // Number only
      else if (pitcher_number_name.length() > 4) pitcher_name = pitcher_number_name.substring(4); // Name only
      //Log.i(TAG, "atBat finished pitcher_name: |" + pitcher_name  + "|");
      String inning_half_str;
      if (atBat_array[atBatInd][team_up].inning_half == 0) inning_half_str = "Top"; else inning_half_str = "Bottom";
      int balls = atBat_array[atBatInd][team_up].balls; int strikes = atBat_array[atBatInd][team_up].strikes;
      String title_str = format(Locale.US, "%s of %d", inning_half_str, atBat_array[atBatInd][team_up].inning);
      if (atBat_array[atBatInd][team_up].scored) title_str = title_str + ":   " + batter_name + " Scored";
      else if (atBat_array[atBatInd][team_up].out_number > 0) title_str = title_str + ":   " + batter_name + " Out";
      else title_str = title_str + ":   " + batter_number_name + " LOB";
      title_str = title_str + "    Pitching: " + pitcher_name;
      if (track_b_s) title_str = title_str + "  " + (balls + strikes) + "P/" + balls + "B/" + strikes + "S";
      this.setTitle(title_str);
      //Log.i(TAG, "current_base=" +  atBat_array[atBatInd][team_up].current_base);
      // Mark how batter got on base and then apply on base events one at a time, marking how they got from base to base, eg. SB, +1B
      int c_b = 0;  // Current base for purposes of drawing
      for (int i=0; i <= atBat_array[atBatInd][team_up].current_base; i++) {
        int ob_step = atBat_array[atBatInd][team_up].ob[i];
        //Log.i(TAG, "onBase_step=" + ob_step + "  i=" + i +"  current_base=" +  atBat_array[atBatInd][team_up].current_base);
        if (ob_step > 0) {  // Eg. skip first base in the case of a double
          number_bases = ob_step / 100;
          int result_type_ind = ob_step % 100;
          result_text = atBat_result_text[number_bases][result_type_ind];
          if (number_bases == 0) is_out = true; // Out at bat
          else if (number_bases > 0  && number_bases < 5) {  // Safe at bat
            is_safe = true;
            c_b = number_bases;
            location = locations[number_bases];
            thumbnail_location = thumbnail_locations[number_bases];
            fill = number_bases == 4;
          }  // End Safe at bat
          else {  // On base event
            if (result_type_ind < 4) {    // Safe on base
              is_safe = true;
              if (result_type_ind == 0 || result_type_ind == 3) c_b += 1;
              if (result_type_ind == 1) c_b += 2;
              if (result_type_ind == 2) c_b += 3;
              if (c_b > 4) c_b = 4; // Just in case
              if (c_b == 4) fill = true;
              location = locations[c_b];
              thumbnail_location = thumbnail_locations[c_b];
            }  // End Safe on base
            else {   // Out on base
              is_safe = false; is_out = true;
            }  // End Out on base
          }  // End On base event
          drawBasePaths(canvas, paint, team_color[team_up], stroke_width, bases, c_b, fill);
          if (is_safe) markHit(canvas, paint, team_color[team_up], text_size, result_text, location);
          else markOut(canvas, paint, team_color[team_up], text_size, atBat_array[atBatInd][team_up].out_number, result_text,
              outs_loc, outs_circle_loc, outs_radius, out_text_loc);
          if (atBat_array[atBatInd][team_up].ball_in_play_location[0] > 0.0 || atBat_array[atBatInd][team_up].ball_in_play_location[1] > 0.0) {
            paint.setColor(team_color[team_up]);
            paint.setStrokeWidth(2);
            canvas.drawLine(bases[0], bases[1], atBat_array[atBatInd][team_up].ball_in_play_location[0],
                atBat_array[atBatInd][team_up].ball_in_play_location[1], paint);
          }
          focus_canvas_view.invalidate();
        }  // End if (ob_step > 0)
      }  // End for (int i=1; i <= atBat_array[atBatInd][team_up].current_base; i++)
    }  // End Finished
    
    // endregion
    
  }  // End onCreate

  public void ProcessResult(String result) {
    //Log.i(TAG, "In ProcessResult result=" + result);
    // Search Result Types array for this result
    number_bases=0;
    int result_type_ind=0;
    for (int i = 0; i < 6; i++) {
      String[] a = atBat_result_types[i];
      for (int j = 0; j < a.length; j++) {
      //Log.i(TAG, "Finding result: array[" + i + "][" + j + "]=" + atBat_result_types[i][j]);
        if (a[j].equals(result)) {
            number_bases = i;
            result_type_ind = j;
        }
      }
    }
    // number_bases: 0=out, 1=reached 1st, 2=reached 2nd, 3=reached 3rd, 4=reached home, 5 indicates an on base event
    // result_type_ind is index of that type of result
    // encode number_bases and result_type_ind as an integer
    result_ind = 100*number_bases + result_type_ind;
    //Log.i(TAG, "Found result: array[" + number_bases + "][" + result_type_ind + "]=" +
    //  atBat_result_types[number_bases][result_type_ind] + "  result_ind=" + result_ind);
    result_text = atBat_result_text[number_bases][result_type_ind];
    is_run = false; is_safe = false; is_ab = false; is_hit = false; is_out = false;
    if (number_bases == 0) {    // Out made at bat
      is_out = true;
      atBat_array[atBatInd][team_up].current_base = 0;
      atBat_array[atBatInd][team_up].ob[0] = result_ind;
      //Log.i(TAG, "current_base=" + atBat_array[atBatInd][team_up].current_base + " result_ind=" + result_ind);
      ++outs[team_up];
      if (result_type_ind < 11) is_ab = true; // Don't count sacrifice bunt or fly as an at bat
    }  // End Out made at bat
    else if (number_bases > 0 && number_bases < 5) {  // Safe at bat
      is_safe = true;
      atBat_array[atBatInd][team_up].current_base = number_bases;
      atBat_array[atBatInd][team_up].ob[number_bases] = result_ind;
      //Log.i(TAG, "Safe at bat: current_base=" + atBat_array[atBatInd][team_up].current_base + " result_ind=" + result_ind);
      ++on_base[team_up];
      is_ab = atBat_result_is_ab[number_bases][result_type_ind];
      is_hit = atBat_result_is_hit[number_bases][result_type_ind];
      location = locations[number_bases];
      thumbnail_location = thumbnail_locations[number_bases];
      fill = false;
      if (number_bases == 4) {
        fill = true;
        atBat_array[atBatInd][team_up].scored = true;
        is_run = true;
      }
    }   // End Safe at bat
    else {  // On base event
      if (result_type_ind < 4) {    // Safe on base
        is_safe = true;
        if (result_type_ind == 0 || result_type_ind == 3) adv_n_bases = 1;
        if (result_type_ind == 1) adv_n_bases = 2;
        if (result_type_ind == 2) adv_n_bases = 3;
        atBat_array[atBatInd][team_up].current_base += adv_n_bases;
        if (atBat_array[atBatInd][team_up].current_base > 4) atBat_array[atBatInd][team_up].current_base = 4; // Just in case
        fill = false;
        if (atBat_array[atBatInd][team_up].current_base == 4) {
          fill = true;
          atBat_array[atBatInd][team_up].scored = true;
          is_run = true;
          --on_base[team_up];
        }
        is_stolen_base = false;
        if (result_type_ind == 3) { is_stolen_base = true; ++atBat_array[atBatInd][team_up].stolen_base; }
        atBat_array[atBatInd][team_up].ob[atBat_array[atBatInd][team_up].current_base] = result_ind;
        //Log.i(TAG, "Safe on base: current_base=" + atBat_array[atBatInd][team_up].current_base + " result_ind=" + result_ind);
        location = locations[atBat_array[atBatInd][team_up].current_base];
        thumbnail_location = thumbnail_locations[atBat_array[atBatInd][team_up].current_base];
      }  // End Safe on base
      else {   // Out on base
        is_out = true;
        ++outs[team_up];
        --on_base[team_up];
        if (result_type_ind == 5) atBat_array[atBatInd][team_up].picked_off = true;
        atBat_array[atBatInd][team_up].ob[atBat_array[atBatInd][team_up].current_base + 1] = result_ind;  // Store out on base in base runner was going to
        //Log.i(TAG, "Out on base: current_base=" + atBat_array[atBatInd][team_up].current_base + " result_ind=" + result_ind);
        ++atBat_array[atBatInd][team_up].current_base; // Now increment current base to point to final ob[] slot
      }  // End Out on base
    }  // End On base event
    //Log.i(TAG, "drawBasePaths: atBat_array[atBatInd][team_up].current_base=" + atBat_array[atBatInd][team_up].current_base);
    drawBasePaths(canvas, paint, team_color[team_up], stroke_width, bases, atBat_array[atBatInd][team_up].current_base, fill);
    if (is_safe) markHit(canvas, paint, team_color[team_up], text_size, result_text, location);
    else markOut(canvas, paint, team_color[team_up], text_size, outs[team_up], result_text,
        outs_loc, outs_circle_loc, outs_radius, out_text_loc);
    focus_canvas_view.invalidate();
  } // End ProcessResult

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.atbat_menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_undo_atbat:
        // region
        if (atBat_state_array[atBatInd][team_up] == 1) {  // If AtBat
          atBat_array[atBatInd][team_up].batter_number_name = "";
          atBat_array[atBatInd][team_up].inning = 0;
          atBat_array[atBatInd][team_up].out_number = 0;
          if (is_out) --outs[team_up];
          atBat_array[atBatInd][team_up].ob = new int[]{0, 0, 0, 0, 0};
          atBat_array[atBatInd][team_up].current_base = 0;
          atBat_array[atBatInd][team_up].stolen_base = 0;
          atBat_array[atBatInd][team_up].RBIs = 0;
          atBat_array[atBatInd][team_up].picked_off = false;
          atBat_array[atBatInd][team_up].scored = false;
          atBat_array[atBatInd][team_up].balls = 0;
          atBat_array[atBatInd][team_up].strikes = 0;
          atBat_array[atBatInd][team_up].comment = "";
          ball_in_play_location = new float[]{0.0f, 0.0f};
        }  // End if AtBat
        if (atBat_state_array[atBatInd][team_up] == 2) {  //If OnBase
          if (adv_n_bases > 0) atBat_array[atBatInd][team_up].ob[atBat_array[atBatInd][team_up].current_base] = 0;
          if (is_out) {
            --outs[team_up];
            ++on_base[team_up];
            --atBat_array[atBatInd][team_up].current_base;
          }
          else atBat_array[atBatInd][team_up].current_base -= adv_n_bases;
          if (atBat_array[atBatInd][team_up].current_base < 1) atBat_array[atBatInd][team_up].current_base = 1;
          atBat_array[atBatInd][team_up].scored = false;
          atBat_array[atBatInd][team_up].out_number = 0;
          if (is_stolen_base) --atBat_array[atBatInd][team_up].stolen_base;
          atBat_array[atBatInd][team_up].picked_off = false;
        }  // End if OnBase
        setResult(RESULT_OK, getIntent());
        finish();
        break;
        // endregion
      case R.id.action_return_atbat:
        // region
        if (is_safe || is_out) // If a hit or out was marked record it
          {
          int team_in_field; if (team_up == 0) team_in_field = 1; else team_in_field = 0;
          pitcher_earned_runs[current_pitcher_index[team_in_field]][team_in_field] += earned_runs;
          if (n_errors_during_play > 0) {  // Mark errors for other team - applies to atBat or onBase
            for (int i = 0; i < n_errors_during_play; i++) {
              ++team_errors[team_in_field];
              if (errors_during_play[i] == -1) ++pitcher_errors[current_pitcher_index[team_in_field]][team_in_field];
              else ++batter_errors[errors_during_play[i]][team_in_field];
            }
          }
          if (atBat_state_array[atBatInd][team_up] == 1) {  // If AtBat
            ++inning_half_batters;
            //Log.i(TAG, "inning_half_batters=" + inning_half_batters);
            ++batter_pa[atBatInd % number_batters][team_up];
            ++pitcher_batters_faced[current_pitcher_index[team_in_field]][team_in_field];
            batter_rbi[atBatInd % number_batters][team_up] += atBat_array[atBatInd][team_up].RBIs;
            team_rbi[team_up] += atBat_array[atBatInd][team_up].RBIs;
            atBat_array[atBatInd][team_up].ball_in_play_location = ball_in_play_location;
            //Log.i(TAG, "atBat_array[atBatInd][team_up].ball_in_play_location X= " + atBat_array[atBatInd][team_up].ball_in_play_location[0] +
            //  " Y=" + atBat_array[atBatInd][team_up].ball_in_play_location[1]);
            if (is_safe) {  // Safe AtBat
              atBat_state_array[atBatInd][team_up] = 2;
              if (atBat_array[atBatInd][team_up].scored) atBat_state_array[atBatInd][team_up] = 3;
              //Log.i(TAG, "return1 n_balls=" + n_balls + " n_strikes=" + n_strikes);
              ++n_strikes; // Assume all safe events on strike pitch except (see next)
              if (result_text.equals("BB")) {
                ++pitcher_bb[current_pitcher_index[team_in_field]][team_in_field];
                --n_strikes; // Safe event on ball
              }
              if (result_text.equals("KD")) {
                ++pitcher_k[current_pitcher_index[team_in_field]][team_in_field]; // Dropped 3rd strike pitcher gets a K
                --n_strikes;  // Already counted
              }
              if (result_text.equals("HBP")) {
                --n_strikes; ++n_balls; // Safe event on ball
              }
              //Log.i(TAG, "return2 n_balls=" + n_balls + " n_strikes=" + n_strikes + " result_text=" + result_text);
              atBat_array[atBatInd][team_up].balls = n_balls;
              atBat_array[atBatInd][team_up].strikes = n_strikes;
              pitcher_balls[current_pitcher_index[team_in_field]][team_in_field] += n_balls;
              pitcher_strikes[current_pitcher_index[team_in_field]][team_in_field] += n_strikes;
              pitcher_pitches[current_pitcher_index[team_in_field]][team_in_field] += (n_balls + n_strikes);
              thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][team_up];
              thumbnail_canvas = new Canvas(thumbnail_bitmap);
              thumbnail_paint = new Paint();
              drawBasePaths(thumbnail_canvas, thumbnail_paint, team_color[team_up], sw, thumbnail_bases, number_bases, fill);
              markHit(thumbnail_canvas, thumbnail_paint, team_color[team_up], ts, result_text, thumbnail_location);
              drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.WHITE, 2, outline);
              thumbnail_bitmap_array[atBatInd][team_up] = thumbnail_bitmap;
              if (is_ab) {
                ++batter_abs[atBatInd % number_batters][team_up];
                ++team_abs[team_up];
              }
              if (is_hit) {
                ++batter_hits[atBatInd % number_batters][team_up];
                ++team_hits[team_up];
                ++inning_hits[inning - 1][team_up];
                ++pitcher_hits[current_pitcher_index[team_in_field]][team_in_field];
              }
              if (is_run) {
                ++batter_runs[atBatInd % number_batters][team_up];
                ++team_runs[team_up];
                ++inning_runs[inning - 1][team_up];
                ++pitcher_runs[current_pitcher_index[team_in_field]][team_in_field];
                if (!atBat_array[atBatInd][team_up].error_during_at_bat_or_on_base) ++pitcher_earned_runs[current_pitcher_index[team_in_field]][team_in_field];
                //Log.i(TAG, "n_error=" + n_errors_during_play + " pitcher_earned_runs=" + pitcher_earned_runs[current_pitcher_index[team_in_field]][team_in_field]);
              }
            } // End if safe AtBat
            if (is_out) {  // At bat
              atBat_array[atBatInd][team_up].out_number = outs[team_up];
              atBat_array[atBatInd][team_up].current_base = 0;
              atBat_state_array[atBatInd][team_up] = 3;
              ++pitcher_outs[current_pitcher_index[team_in_field]][team_in_field];
              //Log.i(TAG, "return3 n_balls=" + n_balls + " n_strikes=" + n_strikes);
              ++n_strikes; // Assume all outs on strike pitch
              if (result_text.equals("K") || result_text.equals("\uA4D8")) {
                ++pitcher_k[current_pitcher_index[team_in_field]][team_in_field];
                --n_strikes; // Already incremented strikes in case of strike out
              }
              //Log.i(TAG, "return4 n_balls=" + n_balls + " n_strikes=" + n_strikes);
              atBat_array[atBatInd][team_up].balls = n_balls;
              atBat_array[atBatInd][team_up].strikes = n_strikes;
              pitcher_balls[current_pitcher_index[team_in_field]][team_in_field] += n_balls;
              pitcher_strikes[current_pitcher_index[team_in_field]][team_in_field] += n_strikes;
              pitcher_pitches[current_pitcher_index[team_in_field]][team_in_field] += (n_balls + n_strikes);
              if (is_ab) {
                ++batter_abs[atBatInd % number_batters][team_up];
                ++team_abs[team_up];
              }
              thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][team_up];
              thumbnail_canvas = new Canvas(thumbnail_bitmap);
              thumbnail_paint = new Paint();
              markOut(thumbnail_canvas, thumbnail_paint, team_color[team_up], ts, outs[team_up],
                  result_text, o_loc, oc_loc, o_r, ot_loc);
              drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.WHITE, 2, outline);
              thumbnail_bitmap_array[atBatInd][team_up] = thumbnail_bitmap;
            }  // End if out AtBat
            if (outs[team_up] < 3) {   // Next up calc - if 3 outs do this in End Inning
              //Log.i(TAG, "Next up calc: inning_half_batters=" + inning_half_batters);
              if (inning_half_batters == 2 * number_batters - 3) {
                Toast.makeText(AtBatActivity.this,
                    "3 batters left before exceeding batters per inning - will break app - you need to end inning by entering outs",
                    Toast.LENGTH_LONG).show();
                Toast.makeText(AtBatActivity.this,
                    "3 batters left before exceeding batters per inning - will break app - you need to end inning by entering outs",
                    Toast.LENGTH_LONG).show();
              }
              if ((atBatInd + 1) % number_batters == 0)
                next_up[team_up] = atBatInd - (number_batters - 1);   // Back to top of order
              else next_up[team_up] = atBatInd + 1;
              if ((atBat_state_array[next_up[team_up]][team_up] == 2) || (atBat_state_array[next_up[team_up]][team_up] == 3)) {  // Batted around - go to next column
                next_up[team_up] += number_batters;
                //Log.i(TAG, "Next up calc: batted around. next_up[team_up]=" +  next_up[team_up]);
                for (int i = inning; i < number_innings; i++) --inning_header_values[i][team_up];
              }
              //Log.i(TAG, "Next up calc: atBatInd=" + atBatInd + " Next up=" + next_up[team_up] +
              //  " Next up state=" + atBat_state_array[next_up[team_up]][team_up]);
              atBat_state_array[next_up[team_up]][team_up] = 1;
              ++atBat_sequence_index[team_up];
              atBat_sequence_array[atBat_sequence_index[team_up]][team_up] = next_up[team_up];
              thumbnail_bitmap_next_batter = thumbnail_bitmap_array[next_up[team_up]][team_up];
              thumbnail_canvas = new Canvas(thumbnail_bitmap_next_batter);
              thumbnail_paint = new Paint();
              drawLinesColor(thumbnail_canvas, thumbnail_paint, team_color[team_up], 2, outline);
            }  // End less than 3 outs
          } // End if AtBat
          else if (atBat_state_array[atBatInd][team_up] == 2) {  // If OnBase
            if (is_safe) {
              //Log.i(TAG, "In Action return at bat: last ob step=" + atBat_array[atBatInd][team_up].ob[atBat_array[atBatInd][team_up].current_base]);
              if (atBat_array[atBatInd][team_up].scored) atBat_state_array[atBatInd][team_up] = 3;
              thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][team_up];
              thumbnail_canvas = new Canvas(thumbnail_bitmap);
              thumbnail_paint = new Paint();
              drawBasePaths(thumbnail_canvas, thumbnail_paint, team_color[team_up], sw, thumbnail_bases, atBat_array[atBatInd][team_up].current_base, fill);
              markHit(thumbnail_canvas, thumbnail_paint, team_color[team_up], ts, result_text, thumbnail_location);
              drawLinesColor(thumbnail_canvas, thumbnail_paint, Color.WHITE, 2, outline);
              thumbnail_bitmap_array[atBatInd][team_up] = thumbnail_bitmap;
              if (is_run) {
                ++batter_runs[atBatInd % number_batters][team_up];
                ++team_runs[team_up];
                ++inning_runs[inning - 1][team_up];
                String pitcher_of_record = atBat_array[atBatInd][team_up].pitcher_number_name;
                int i = 0;
                while (!pitcher_of_record.equals(pitcher_number_names[i][team_in_field])) i++;
                ++pitcher_runs[i][team_in_field];
                if (!atBat_array[atBatInd][team_up].error_during_at_bat_or_on_base) ++pitcher_earned_runs[i][team_in_field];
                //Log.i(TAG, "pitcher_of_record=" + pitcher_of_record + " pitcher_number_names[i]=" + pitcher_number_names[i][team_in_field]);
              }
            } // End if safe OnBase
            if (is_out) {
              atBat_array[atBatInd][team_up].out_number = outs[team_up];
              ++pitcher_outs[current_pitcher_index[team_in_field]][team_in_field];
              thumbnail_bitmap = thumbnail_bitmap_array[atBatInd][team_up];
              thumbnail_canvas = new Canvas(thumbnail_bitmap);
              thumbnail_paint = new Paint();
              markOut(thumbnail_canvas, thumbnail_paint, team_color[team_up], ts, outs[team_up], result_text, o_loc, oc_loc, o_r, ot_loc);
              thumbnail_bitmap_array[atBatInd][team_up] = thumbnail_bitmap;
              atBat_state_array[atBatInd][team_up] = 3;
            }  // End if out OnBase
          } // End if OnBase
        }
        else { // If no hit or out was marked just record balls and strikes if you're tracking them
          if (track_b_s) {
            atBat_array[atBatInd][team_up].balls = n_balls;
            atBat_array[atBatInd][team_up].strikes = n_strikes;
          }
        }
        setResult(RESULT_OK, getIntent());
        finish();
        break;
        // endregion
      default:
        break;
    }
    return true;
  } // End onOptionsItemSelected(MenuItem item)

  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    hideNavigation(getWindow().getDecorView());
  }
} // End class AtBatActivity

