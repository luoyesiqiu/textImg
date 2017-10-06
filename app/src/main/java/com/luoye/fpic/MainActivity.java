package com.luoye.fpic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.luoye.fpic.util.MediaScanner;
import com.luoye.fpic.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final int IMAGE = 0x100;
    private Button selectButton;
    private  Button convertButton;
    private EditText textEdit;
    private ImageView imageView;
    private SeekBar seekBar;
    private TextView seekBarText;
    private File imgPath;
    private boolean isSelect=false;
    private ProgressDialog progressDialog;
    private String SDCARD= Environment.getExternalStorageDirectory().getAbsolutePath();
    private  ConvertThread convertThread;
    private  boolean isFinish=false;
    private RadioGroup radioGroup;
    private RadioButton radioButtonText;
    private  RadioButton radioButtonBlock;
    private  EditText backColorEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectButton=(Button)findViewById(R.id.select_file_button);
        textEdit=(EditText)findViewById(R.id.text_edit) ;
        seekBar=(SeekBar)findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        seekBarText=(TextView)findViewById(R.id.seek_bar_text);
        imageView=(ImageView)findViewById(R.id.main_iv);
        convertButton=(Button)findViewById(R.id.convert_button);
        backColorEditText=(EditText) findViewById(R.id.back_color_edit);
        radioGroup=(RadioGroup)findViewById(R.id.main_radio_group);
        radioButtonText=(RadioButton)findViewById(R.id.text_radio_button) ;
        radioButtonBlock=(RadioButton)findViewById(R.id.block_radio_button) ;

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("转换中,请稍等...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);

        selectButton.setOnClickListener(new MyOnClickEvents());
        convertButton.setOnClickListener(new MyOnClickEvents());
        imageView.setOnClickListener(new MyOnClickEvents());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarText.setText(i+"");
                if(i==0)
                {
                    seekBar.setProgress(1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                if(checkId==R.id.text_radio_button)
                {
                    textEdit.setEnabled(true);
                    backColorEditText.setEnabled(true);
                }else {
                    textEdit.setEnabled(false);
                    backColorEditText.setEnabled(false);
                }
            }
        });

    }

    private MediaScanner mediaScanner;
    private Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1)
            {
                byte[] data=(byte[])msg.obj;
                if(data==null)
                {
                    showToast("转换失败");
                }
                else {
                    isFinish=true;
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                    if(progressDialog!=null)
                        progressDialog.dismiss();
                    showToast("转换成功，保存路径："+getOutputFile().getAbsolutePath());
                    if (mediaScanner == null)
                        mediaScanner = new MediaScanner(MainActivity.this);
                    mediaScanner.scanFile(getOutputFile().getAbsolutePath(), "image/*");
                }
            }
        }
    };


    private   void shareImage(File imagePath) {
        Uri imageUri = Uri.fromFile(imagePath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private  class MyOnClickEvents implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            if(view.getId()==R.id.select_file_button) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
            else if(view.getId()==R.id.convert_button)
            {
                if(!isSelect)
                {
                    showToast("请先选择图片");
                    return;
                }
                if(TextUtils.isEmpty(textEdit.getText())&&radioButtonText.isChecked())
                {
                    showToast("请输入文本");
                    return ;
                }
                if(TextUtils.isEmpty(backColorEditText.getText())&&radioButtonText.isChecked())
                {
                    showToast("请输入背景颜色值");
                    return ;
                }
                int backColor=0;
                try {
                    backColor= Color.parseColor(backColorEditText.getText().toString());//Integer.parseInt(backColorEditText.getText().toString(),16);
                }catch (Exception e)
                {
                    showToast("颜色值输入不正确");
                    e.printStackTrace();
                    return;
                }
                File output=getOutputFile();
                if(output==null)
                {
                    showToast("无法写入文件");
                    return;
                }

                progressDialog.show();
                if(radioButtonBlock.isChecked()) {
                    convertThread = new ConvertThread(handler, imgPath, output, seekBar.getProgress());
                }
                else if(radioButtonText.isChecked())
                {
                    convertThread = new ConvertThread(handler, imgPath, output,backColor,textEdit.getText().toString(), seekBar.getProgress());
                }
                convertThread.start();
            }
            else if(view.getId()==R.id.main_iv)
            {
                //生成了才分享
                if(isFinish)
                    shareImage(getOutputFile());
            }
        }
    }
    private  File getOutputFile() {
        File dir = new File(SDCARD+File.separator+"Pictures" + File.separator + "LuoyePic");
        if (!dir.exists() ) {
            if(!dir.mkdirs())
            {
                return null;
            }
        }
        String name = imgPath.getName();
        File file = new File(dir, name);
        return  file;
    }
    private  class ConvertThread extends   Thread
    {
        private Handler handler;
        private  File in;
        private File out;
        private String text;
        private int fontSize;
        private  int style=0;
        private  int backColor;
        public ConvertThread(Handler handler,File in,File out,int backColor,String text,int fontSize)
        {
            this.handler=handler;
            this.in=in;
            this.out=out;
            this.text=text;
            this.fontSize=fontSize;
            this.style=0;
            this.backColor=backColor;
        }
        public ConvertThread(Handler handler,File in,File out,int fontSize)
        {
            this.handler=handler;
            this.in=in;
            this.out=out;
            this.fontSize=fontSize;
            this.style=1;
        }
        @Override
        public void run() {
            byte[] data=convert(in,out,text,fontSize);
            handler.sendMessage(handler.obtainMessage(1,data));
        }

        /**
         * 转换
         * @param input
         * @param output
         * @param text
         * @param fontSize
         */
        private  byte[] convert(File input,File output,String text,int fontSize)
        {
            Bitmap bitmap= BitmapFactory.decodeFile(input.getAbsolutePath());
            Bitmap target=null;
            if(style==0) {
                 target = Utils.getTextBitmap(bitmap,backColor, text, fontSize);
            }else
            {
                target = Utils.getBlockBitmap(bitmap, fontSize);
            }
            FileOutputStream fileOutputStream=null;
            ByteArrayOutputStream byteArrayOutputStream=null;
            try {
                 fileOutputStream=new FileOutputStream(output);
                 byteArrayOutputStream=new ByteArrayOutputStream();

                target.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
                byte[] data=byteArrayOutputStream.toByteArray();
                fileOutputStream.write(data,0,data.length);
                fileOutputStream.flush();
                return data;

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if(fileOutputStream!=null)
                {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(byteArrayOutputStream!=null)
                {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            imgPath=new File(imagePath);
            c.close();
            selectButton.setText(imgPath.getName());
            isSelect=true;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.help)
        {
            new AlertDialog.Builder(this).setTitle("帮助")
                    .setMessage(getResources().getString(R.string.help))
                    .setPositiveButton("知道啦",null)
                    .create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 显示toast
     * @param text
     */
    private Toast toast;
    private void showToast(CharSequence text) {
        if (toast == null) {
            toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }
}
