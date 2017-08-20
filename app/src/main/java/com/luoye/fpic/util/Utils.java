package com.luoye.fpic.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zyw on 2017/8/17.
 */
public class Utils {

    public static Bitmap getFontBitmap(Bitmap bitmap,String text,int fontSize)
    {
        if(bitmap==null)
            throw  new IllegalArgumentException("Bitmap cannot be null.");
        int picWidth=bitmap.getWidth();
        int picHeight=bitmap.getHeight();
        //log(picWidth+","+picHeight);
        Bitmap back= Bitmap.createBitmap((bitmap.getWidth()%fontSize==0)?bitmap.getWidth():((bitmap.getWidth()/fontSize+1)*fontSize)
                ,(bitmap.getHeight()%fontSize==0)?bitmap.getHeight():((bitmap.getHeight()/fontSize+1)*fontSize)
                , Bitmap.Config.ARGB_8888);
        //log("back:"+back.getWidth()+","+back.getHeight());
        Canvas canvas=new Canvas(back);
        canvas.drawColor(0xfff);
        //log("canvas:"+canvas.getWidth()+","+canvas.getHeight());
        int idx=0;
        for(int y=0;y<picHeight;y+=fontSize)
        {
            for(int x=0;x<picWidth;x+=fontSize)
            {
                int[] colors=getPixels(bitmap,x,y,fontSize,fontSize );//逐行扫描

                Paint paint=new Paint();
                paint.setAntiAlias(true);
                paint.setColor(getAverage(colors));
                paint.setTextSize(fontSize);
                Paint.FontMetrics fontMetrics =paint.getFontMetrics();
                float padding=(y==0)?(fontSize+fontMetrics.ascent):((fontSize+fontMetrics.ascent)*2);

                canvas.drawText(String.valueOf(text.charAt(idx++)),x,y-padding,paint);
                if(idx==text.length())
                {
                    idx=0;
                }

            }
            //log("draw:---------------------------");
        }

        return back;
    }

    private static int[] getPixels(Bitmap bitmap,int x,int y,int w,int h)
    {
        int[] colors=new int[w*h];
        int idx=0;
        for (int i=y;(i<h+y)&&(i<bitmap.getHeight());i++)
        {
            for (int j=x;(j<w+x)&&(j<bitmap.getWidth());j++)
            {
                int color=bitmap.getPixel(j,i);
                colors[idx++]=color;
            }
        }
        return colors;
    }

    private static   int getAverage (int[] colors)
    {
        int alpha=0;
        int red=0;
        int green=0;
        int blue=0;
        for(int color:colors)
        {
            //alpha+=(color&0xff000000)>>6;
            red += ((color&0xff0000)>>16);
            green += ((color&0xff00)>>8);
            blue += (color&0x0000ff);
        }

        //log("1*alpha:"+alpha+",red:"+red+",green:"+green+",blue:"+blue);
        //log("----------------------------------->");
        float len=colors.length;
        alpha=Math.round(alpha/len);
        red=Math.round(red/len);
        green=Math.round(green/len);
        blue=Math.round(blue/len);

        //log("2*alpha:"+alpha+",red:"+red+",green:"+green+",blue:"+blue+",len:"+len);
        return Color.argb(0xff,red,green,blue);
    }

    private static  void log(String log)
    {
        System.out.println("-------->Utils:"+log);
    }
}
