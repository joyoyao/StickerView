package com.abcew.stickerview;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.abcew.stickerview.sticker.ImageStickerConfig;
import com.abcew.stickerview.sticker.StickerHolderView;
import com.abcew.stickerview.sticker.TextStickerConfig;
import com.abcew.stickerview.utils.LocalDisplay;

public class MainActivity extends AppCompatActivity implements StickerHolderView.OnStickerSelectionCallback {

    StickerHolderView stickerHolderView;


    private final int DEFAULT_COLOR    = 0xFFFFFFFF; //ARGB
    private final int DEFAULT_BG_COLOR = 0x00FFFFFF; //ARGB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalDisplay.init(MainActivity.this);
        setContentView(R.layout.activity_main);
        stickerHolderView= (StickerHolderView) findViewById(R.id.stickerHolderView);
        stickerHolderView.setTextStickerSelectionCallback(this);



    }

    @Override
    public void onTextStickerSelected(TextStickerConfig config, boolean isNew) {

    }

    @Override
    public void onImageStickerSelected(ImageStickerConfig config, boolean isNew) {

    }


    public void OnClickAddImage(View view) {
        ImageStickerConfig imageStickerConfig=new ImageStickerConfig(R.string.sticker_name,R.drawable.icon_change_size,R.raw.sticker_mustache2);
        stickerHolderView.addStickerView(imageStickerConfig);


    }

    public void OnClickAddText(View view) {
        stickerHolderView.addStickerView(new TextStickerConfig("text",Paint.Align.LEFT, null,DEFAULT_COLOR,DEFAULT_BG_COLOR));


    }


    @Override
    public void onNoneStickerSelected() {

    }
}
