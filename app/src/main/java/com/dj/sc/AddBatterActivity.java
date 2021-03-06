package com.dj.sc;

/*
AddBatterActivity.java - Add Batter screen of Scorecard application

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

public class AddBatterActivity extends AppCompatActivity {
  private static final String TAG = AddBatterActivity.class.getName();
  
  int batter_number = 0;
  String batter_name = "";
  String batter_position = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.addbatter_activity);
    hideNavigation(getWindow().getDecorView());
    Intent setIntent = new Intent();

    EditText newBatterEdit = (EditText) findViewById(R.id.newBatterEditText);
    newBatterEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          batter_name = view.getText().toString();
        }
        //Log.i(TAG, "new batter batter_name=" + batter_name);
        return false;
      }
    });
    // endregion

    // region Button selectBatterNumberButton
    final PopupMenu select_batter_number_popup = new PopupMenu(this, findViewById(R.id.select_batter_number_popup_insert_point));
    for (int i = 0; i < 100; i++) select_batter_number_popup.getMenu().add(Integer.toString(i));
    final Button selectBatterNumber = (Button) findViewById(R.id.selectBatterNumberButton);
    selectBatterNumber.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        select_batter_number_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            batter_number = Integer.parseInt(item.getTitle().toString());
            Toast.makeText(AddBatterActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
          }
        });
        select_batter_number_popup.show();
      }
    });  // End numberBatters.setOnClickListener
    // endregion

    // region Button selectBatterPositionButton
    final PopupMenu position_popup = new PopupMenu(this, findViewById(R.id.select_batter_number_popup_insert_point));
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
    final Button selectBatterPosition = (Button) findViewById(R.id.selectBatterPositionButton);
    selectBatterPosition.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        position_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            batter_position = item.getTitle().toString();
            Toast.makeText(AddBatterActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
          }
        });
        position_popup.show();
      }
    });  // End selectBatterPosition.setOnClickListener
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
        setIntent.putExtra("BatterName", batter_name);
        setIntent.putExtra("BatterNumber", batter_number);
        setIntent.putExtra("BatterPosition", batter_position);
        setResult(RESULT_OK, setIntent);
        finish();
      }
    });  // End setButton
  }  // End onCreate
}  // End AddBatterActivity