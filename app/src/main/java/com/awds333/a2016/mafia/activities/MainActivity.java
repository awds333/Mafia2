package com.awds333.a2016.mafia.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.dialogs.ExitDialog;
import com.awds333.a2016.mafia.dialogs.ImagePickDialog;
import com.awds333.a2016.mafia.dialogs.NamePickDialog;
import com.awds333.a2016.mafia.dialogs.ServNamePickDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {
    Button b1, b2;
    ImageButton imBt;
    DialogFragment naimDialog, servNameDialog, exit, imageDialog;
    File directory;
    Handler handler;
    Bitmap image;
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        b1 = (Button) findViewById(R.id.newserver);
        b2 = (Button) findViewById(R.id.findeserv);
        imBt = (ImageButton) findViewById(R.id.imageButton);
        SharedPreferences.Editor editor = getSharedPreferences("pref", MODE_PRIVATE).edit();
        editor.putBoolean("remember", true);
        editor.commit();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
                //new server creating
                servNameDialog = new ServNamePickDialog();
                servNameDialog.show(getFragmentManager(), "mytag");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
                //locking for server
                naimDialog = new NamePickDialog();
                naimDialog.show(getFragmentManager(), "mytag");
            }
        });
        imBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        String permissions[] = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(context, permissions, 1);
                        return;
                    }
                }
                createDirectory();
                imageDialog = new ImagePickDialog();
                ((ImagePickDialog) imageDialog).setHandler(handler);
                imageDialog.show(getFragmentManager(), "myTag");
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    imageDialog.dismiss();
                    boolean tr = true;
                    if (Build.VERSION.SDK_INT >= 23)
                        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                            tr = false;
                    if (tr) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 1);
                    }

                } else if (msg.what == 2) {
                    imageDialog.dismiss();
                    boolean tr = true;
                    if (Build.VERSION.SDK_INT >= 23)
                        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            tr = false;
                    if (tr) {
                        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        pickIntent.setType("image/*");
                        startActivityForResult(pickIntent, 2);
                    }
                } else if (msg.what == 3) {
                    imageDialog.dismiss();
                    if (image != null) {
                        imBt.setImageResource(android.R.drawable.ic_menu_camera);
                        imBt.setBackgroundResource(android.R.drawable.btn_default);
                        imBt.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        image = null;
                    }
                }
            }
        };
        SharedPreferences sPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        if (sPreferences.getBoolean("rememberImage", false)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    return;
            }
            createDirectory();
            image = BitmapFactory.decodeFile(directory.getPath() + "/imageplayer.jpeg");
            double height = image.getHeight();
            double width = image.getWidth();
            double ot = height / width;
            Log.d("awdsawds", image.getWidth() + " " + image.getHeight());
            if (ot > 1.0)
                image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth());
            else
                image = Bitmap.createBitmap(image, 0, 0, image.getHeight(), image.getHeight());
            Log.d("awdsawds", ot + "");
            Log.d("awdsawds", image.getHeight() + " " + image.getWidth());
            imBt.setLayoutParams(new LinearLayout.LayoutParams(360, 360));
            Log.d("awdsawds", imBt.getHeight() + " " + imBt.getWidth());
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), image);
            imBt.setImageBitmap(null);
            imBt.setBackground(bitmapDrawable);
        }
    }

    public void saveImage() {
        if (image != null) {
            SharedPreferences.Editor editor = getSharedPreferences("pref", MODE_PRIVATE).edit();
            editor.putBoolean("rememberImage", true);
            editor.commit();
            OutputStream out = null;
            try {
                out = new FileOutputStream(new File(directory.getPath() + "/imageplayer.jpeg"));
                image.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                return;
        }
        imBt.callOnClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1 || requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {
                    Object obj = intent.getExtras().get("data");
                    image = (Bitmap) obj;
                } else {
                    Uri selectedImage = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    image = BitmapFactory.decodeFile(filePath);
                }
                double height = image.getHeight();
                double width = image.getWidth();
                double ot = height / width;
                Log.d("awdsawds", image.getWidth() + " " + image.getHeight());
                if (ot > 1.0)
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth());
                else
                    image = Bitmap.createBitmap(image, 0, 0, image.getHeight(), image.getHeight());
                Log.d("awdsawds", ot + "");
                Log.d("awdsawds", image.getHeight() + " " + image.getWidth());
                imBt.setLayoutParams(new LinearLayout.LayoutParams(360, 360));
                Log.d("awdsawds", imBt.getHeight() + " " + imBt.getWidth());
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), image);
                imBt.setImageBitmap(null);
                imBt.setBackground(bitmapDrawable);
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    @Override
    public void onBackPressed() {
        exit = new ExitDialog();
        exit.show(getFragmentManager(), "mytag");
    }

    private void createDirectory() {
        directory = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyFolder");
        if (!directory.exists())
            directory.mkdirs();
    }
}
