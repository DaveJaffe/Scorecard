package com.dj.sc;

/*
AddPitcherActivity.java - Add Pitcher screen of Scorecard application

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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.dj.sc.StartUpActivity.hideNavigation;

public class AddPitcherActivity extends AppCompatActivity {
  private static final String TAG = AddBatterActivity.class.getName();

  int pitcher_number = 0;
  String pitcher_name = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.addpitcher_activity);
    hideNavigation(getWindow().getDecorView());
    Intent setIntent = new Intent();

    EditText newPitcherEdit = (EditText) findViewById(R.id.newPitcherEditText);
    newPitcherEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          //Log.i(TAG, "new pitcher 0");
          pitcher_name = view.getText().toString();
        }
        //Log.i(TAG, "new pitcher 2 pitcher_name=" + pitcher_name);
        return false;
      }
    });
    // endregion

    // region Button selectPitcherNumberButton
    final PopupMenu select_pitcher_number_popup = new PopupMenu(this, findViewById(R.id.select_pitcher_number_popup_insert_point));
    for (int i = 0; i < 100; i++) select_pitcher_number_popup.getMenu().add(Integer.toString(i));
    final Button selectPitcherNumber = (Button) findViewById(R.id.selectPitcherNumberButton);
    selectPitcherNumber.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        select_pitcher_number_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            pitcher_number = Integer.parseInt(item.getTitle().toString());
            Toast.makeText(AddPitcherActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
          }
        });
        select_pitcher_number_popup.show();
      }
    });  // End numberPitchers.setOnClickListener
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
        setIntent.putExtra("PitcherName", pitcher_name);
        setIntent.putExtra("PitcherNumber", pitcher_number);
        setResult(RESULT_OK, setIntent);
        finish();
      }
    });  // End setButton
  }  // End onCreate
}  // End AddPitcherActivity