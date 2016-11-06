package com.takeoffandroid.videochatheads.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.takeoffandroid.videochatheads.R;

/**
 * Created by chandrasekar on 28/10/16.
 */

public class Utils {


    public static void setBackground(Context context, ImageView imageView, int drawableID) {
        if (Build.VERSION.SDK_INT >= 21) {
            imageView.setBackground(context.getResources().getDrawable(drawableID,null));
        }else if(Build.VERSION.SDK_INT >= 16){
            imageView.setBackground(context.getResources().getDrawable(drawableID));
        }else {
            imageView.setBackgroundDrawable(context.getResources().getDrawable(drawableID));
        }

    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dipValue, metrics);



    }


}
