package com.dj.sc;

/*
HelpActivity.java - Add Team screen of Scorecard application

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
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import static com.dj.sc.StartUpActivity.base_text_size;
import static com.dj.sc.StartUpActivity.hideNavigation;

public class HelpActivity extends AppCompatActivity {
  private static final String TAG = HelpActivity.class.getName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.help_activity);
    hideNavigation(getWindow().getDecorView());

    TextView help_tv = (TextView) findViewById(R.id.helpTextbox);
    String h_s = "First, select visiting team and home team in one of three ways:\n";  // help string
    h_s = h_s + "  - Download current MLB roster\n";
    h_s = h_s + "  - Use an existing roster saved in Scorecard's storage on phone or tablet\n";
    h_s = h_s + "  - Create a new team by adding players as you go\n";
    h_s = h_s + "Then, click on Play Ball\n";
    h_s = h_s + "Optionally add pitchers and batters by clicking on appropriate box and selecting from dropdown or creating a new player\n";
    h_s = h_s + "Click on square corresponding to current at bat\n";
    h_s = h_s + "  - In large At Bat window click along 1st baseline, 2nd, 3rd or home for single, double, triple or home run\n";
    h_s = h_s + "  - Or click inside diamond for out\n";
    h_s = h_s + "  - Click on area of field where hit or out was made\n";
    h_s = h_s + "  - Click on RBIs or Error buttons to indicate either\n";
    h_s = h_s + "  - When finished, click on Return icon. If a mistake click on the Undo icon\n";
    h_s = h_s + "For a runner on base, click on square, then click anywhere in On Base window to show how they progressed to next base\n";
    h_s = h_s + "When inning is complete, click on End Inning\n";
    h_s = h_s + "Under Settings can set number of batters (default is 9), number of innings in regulation (default is 9),\n";
    h_s = h_s + "  whether to track balls and strikes (default is NO), and whether to use a ghost runner in extra innings (default it YES)\n";
    h_s = h_s + "Under Games can select pre-recorded games to view or continue\n";
    h_s = h_s + "NOTE: Before uninstalling/reinstalling app be sure to copy all saved games and rosters\n";
    h_s = h_s + "      from Android/data/com.dj.sc/files to user storage on your pad or phone\n";
    h_s = h_s + "      You will need to use a file program that can access hidden files, such as Files by Marc Apps\n";

    //Log.i(TAG, "Dimension: help base_text_size in pixels=" + base_text_size);
    help_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, base_text_size);
    help_tv.setText(h_s);

    Button returnButton = findViewById(R.id.returnButton);
    returnButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
      }
    });

  }  // End onCreate
}  // End HelpActivity