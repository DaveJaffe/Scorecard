package com.dj.sc;

/*
SettingsActivity.java - settings code for Scorecard application

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


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import static com.dj.sc.StartUpActivity.hideNavigation;
import static com.dj.sc.StartUpActivity.number_innings;

public class SettingsActivity extends AppCompatActivity {
  private static final String TAG = SettingsActivity.class.getName();
  int new_number_batters;
  int new_number_innings_regulation;
  CheckBox track_b_s_checkbox, use_ghost_runner_checkbox;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_activity);
    hideNavigation(getWindow().getDecorView());
    Intent setIntent = new Intent();
    track_b_s_checkbox = (CheckBox) findViewById(R.id.track_b_s_checkbox);
    use_ghost_runner_checkbox = (CheckBox) findViewById(R.id.use_ghost_runner);
    
    // region Button selectNumberBatters
    final PopupMenu number_batters_popup = new PopupMenu(this, findViewById(R.id.popup_insert_point1));
    for (int i = 3; i < 13; i++) number_batters_popup.getMenu().add(Integer.toString(i));
    final Button numberBatters = (Button) findViewById(R.id.numberBattersButton);
    numberBatters.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        number_batters_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            Toast.makeText(SettingsActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
            new_number_batters = Integer.parseInt(item.getTitle().toString());
            return true;
          }
        });
        number_batters_popup.show();
      }
    });  // End numberBatters.setOnClickListener
    // endregion

    // region Button selectInningsReg
    final PopupMenu number_innings_regulation_popup = new PopupMenu(this, findViewById(R.id.popup_insert_point1));
    for (int i = 3; i <= number_innings; i++) number_innings_regulation_popup.getMenu().add(Integer.toString(i));
    final Button numberInningsReg = (Button) findViewById(R.id.numberInningsRegButton);
    numberInningsReg.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        number_innings_regulation_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            Toast.makeText(SettingsActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
            new_number_innings_regulation = Integer.parseInt(item.getTitle().toString());
            return true;
          }
        });
        number_innings_regulation_popup.show();
      }
    });  // End numberBatters.setOnClickListener
    // endregion

    // region Cancel and Set buttons
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
        setIntent.putExtra("new_number_innings_regulation", new_number_innings_regulation);
        setIntent.putExtra("new_number_batters", new_number_batters);
        if (track_b_s_checkbox.isChecked()) setIntent.putExtra("track_b_s", true);
        else setIntent.putExtra("track_b_s", false);
        if (use_ghost_runner_checkbox.isChecked()) setIntent.putExtra("use_ghost_runner", true);
        else setIntent.putExtra("use_ghost_runner", false);

        setResult(RESULT_OK, setIntent);
        finish();
      }
    });  // End setButton
    // endregion

  }  // End onCreate
}  // End SettingsActivity