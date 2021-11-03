package com.dj.sc;

/*
AddTeamActivity.java - Add Team screen of Scorecard application

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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.dj.sc.StartUpActivity.hideNavigation;

public class AddTeamActivity extends AppCompatActivity {
  private static final String TAG = AddTeamActivity.class.getName();
  
  String new_team_name = "";
  int new_team_color;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.addteam_activity);
    hideNavigation(getWindow().getDecorView());
    int visitor_or_home = getIntent().getIntExtra("VisitorOrHome", 0);
    String v_o_h;
    if (visitor_or_home == 0) v_o_h = "visiting"; else v_o_h = "home";

    TextView enter_team_name_tv = (TextView) findViewById(R.id.enterTeamNameTextbox);
    enter_team_name_tv.setText("Enter " + v_o_h + " team name and hit Done");

    // region Enter team name
    EditText newTeamNameEdit = (EditText) findViewById(R.id.newTeamNameEditText);
    newTeamNameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          new_team_name = view.getText().toString();
        }
        return false;
      }
    });
    // endregion

    TextView enter_team_color_tv = (TextView) findViewById(R.id.enterTeamColorTextbox);
    enter_team_color_tv.setText("Enter " + v_o_h + " team color (6 hex digits) and hit Done\neg red = FF0000, green = 00FF00, blue = 0000FF");

    // region Enter team color
    EditText newTeamColorEdit = (EditText) findViewById(R.id.newTeamColorEditText);
    newTeamColorEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          try {
            new_team_color = Color.parseColor("#" + view.getText().toString());
          } catch (IllegalArgumentException ex) {
            Toast.makeText(AddTeamActivity.this, "Bad color string: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
          }
        }
        return false;
      }
    });
    // endregion

    Button cancelButton = findViewById(R.id.cancelButton);
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent cancelIntent = new Intent();
        setResult(RESULT_CANCELED, cancelIntent);
        finish();
      }
    });

    Button setButton = findViewById(R.id.setButton);
    setButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent setIntent = new Intent();
        setIntent.putExtra("VisitorOrHome", visitor_or_home);
        setIntent.putExtra("TeamName", new_team_name);
        setIntent.putExtra("TeamColor", new_team_color);
        setResult(RESULT_OK, setIntent);
        finish();
      }
    });  // End setButton
  }  // End onCreate
}  // End AddTeamActivity