package com.luoye.fpic.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by zyw on 2017/8/17.
 */
public class Utils {
    /**
     * 核心，文本转成图片
     * @param bitmap 原图片
     * @param text 文本
     * @param fontSize 文字大小
     * @return
     */
    public static Bitmap getTextBitmap(Bitmap bitmap, String text, int fontSize)
    {
        if(bitmap==null)
            throw  new IllegalArgumentException("Bitmap cannot be null.");
        int picWidth=bitmap.getWidth();
        int picHeight=bitmap.getHeight();
        Bitmap back= Bitmap.createBitmap((bitmap.getWidth()%fontSize==0)?bitmap.getWidth():((bitmap.getWidth()/fontSize)*fontSize)
                ,(bitmap.getHeight()%fontSize==0)?bitmap.getHeight():((bitmap.getHeight()/fontSize)*fontSize)
                , Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(back);
        canvas.drawColor(0xfff);
        int idx=0;
        for(int y=0;y<picHeight;y+=fontSize)
        {
            for(int x=0;x<picWidth;x+=fontSize)
            {
                int[] colors=getPixels(bitmap,x,y,fontSize,fontSize );

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
        }

        return back;
    }

    /**
     * 获取某一块的所有像素的颜色
     * @param bitmap
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
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

    /**
     * 求取多个颜色的平均值
     * @param colors
     * @return
     */
    private static   int getAverage (int[] colors)
    {
        //int alpha=0;
        int red=0;
        int green=0;
        int blue=0;
        for(int color:colors)
        {
            red += ((color&0xff0000)>>16);
            green += ((color&0xff00)>>8);
            blue += (color&0x0000ff);
        }
        float len=colors.length;
        //alpha=Math.round(alpha/len);
        red=Math.round(red/len);
        green=Math.round(green/len);
        blue=Math.round(blue/len);

        return Color.argb(0xff,red,green,blue);
    }

    private static  void log(String log)
    {
        System.out.println("-------->Utils:"+log);
    }
}
