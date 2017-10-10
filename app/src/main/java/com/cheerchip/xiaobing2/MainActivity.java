package com.cheerchip.xiaobing2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.namee.permissiongen.PermissionGen;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.upload)
    Button upload;
    @BindView(R.id.choisepic)
    Button choisepic;
    @BindView(R.id.img)
    ImageView img;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;
    private final int LOGO_IMAGE = 379;
    @BindView(R.id.tv)
    TextView tv;
    private String url;
    private String base64;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int faceAuthScore = OkHttpUtils.findScoreFromString(analyzeResult);
            Toast.makeText(MainActivity.this, "" + faceAuthScore, Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "" + content, Toast.LENGTH_SHORT).show();
            tv.setText(content);
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    };
    private String analyzeResult;
    private String content;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在上传图片...");
        PermissionGen.with(MainActivity.this).addRequestCode(100)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).request();
    }

    @OnClick({R.id.upload, R.id.choisepic})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.upload:
                progressDialog.show();
                if (url != null) {
                    //获取base64
                    base64 = PicToStringUtils.getBase64(url);
                    //   base64上传到微软服务器
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String result1 = OkHttpUtils.getUploadPicResult(base64);//获取返回值
                            Log.e("run1: ", result1);
                            analyzeResult = OkHttpUtils.analyzeImage(result1);
                            Log.e("run2: ", analyzeResult);
                            try {
                                JSONObject json = new JSONObject(analyzeResult);
                                content = json.getJSONObject("content").getString("text");
                                handler.sendEmptyMessage(0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                } else {
                    Toast.makeText(this, "选择图片为空", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.choisepic:
                Intent intent;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(intent, LOGO_IMAGE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                if (requestCode == LOGO_IMAGE) {
                    url = ContentHelper.absolutePathFromUri(this, imageUri);
                    Bitmap logoImage = BitmapFactory.decodeFile(url);
                    img.setImageBitmap(logoImage);
                    Toast.makeText(this, "Logo image added.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e( "onActivityResult: ",e.toString() );
                if (requestCode == LOGO_IMAGE) {
                    Toast.makeText(this, "Failed to add the logo image.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
