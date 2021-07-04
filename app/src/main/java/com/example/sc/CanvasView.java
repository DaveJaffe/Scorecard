package com.example.sc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CanvasView extends View {

  Bitmap bitmap;
  int left, top, right, bottom;

  public CanvasView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawBitmap(bitmap,null,new Rect(left, top, right, bottom),null);
  }
}

