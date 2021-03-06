package com.ryca.lyric.acitivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ryca.lyric.R;
import com.ryca.lyric.Utils.SvgRatingBar;
import com.ryca.lyric.adapter.ViewPagerLyricAdapter;
import com.ryca.lyric.api.Api;
import com.ryca.lyric.api.ServiceGenerator;
import com.ryca.lyric.model.Comment;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LyricActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "CLIENT";
    private ViewPager viewPager;
    private ImageView account;
    private String mName;
    private String mSinger;
    private int mId, mSid;
    private String mScore;
    private Dialog dialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivy_lyric);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mName = null;
                mSinger = null;
                mScore = null;
                mId = 0;
                mSid = 0;
            } else {
                mId = extras.getInt("ID");
                mSid = extras.getInt("SID");
                mName = extras.getString("NAME");
                mSinger = extras.getString("SINGER");
                mScore = extras.getString("SCORE");
            }
        } else {
            mName = (String) savedInstanceState.getSerializable("NAME");
            mSinger = (String) savedInstanceState.getSerializable("SINGER");
            mScore = (String) savedInstanceState.getSerializable("SCORE");
            mId = (int) savedInstanceState.getSerializable("ID");
            mSid = (int) savedInstanceState.getSerializable("SID");
        }
        TextView singer = findViewById(R.id.tv_singer);
        TextView title = findViewById(R.id.tv_title);
        singer.setText(mSinger);
        title.setText(mName);
        TextView score = findViewById(R.id.score);
        if (mScore == null)
            mScore = "5.00";
        score.setText(getString(R.string.rat1) + " "
                + String.format("%.2f", Double.parseDouble(mScore)) + " "
                + getString(R.string.of) + " " + 5);
        init();
    }

    private void init() {
        ImageView dot1 = findViewById(R.id.dot1);
        ImageView dot2 = findViewById(R.id.dot2);
        ImageView comment = findViewById(R.id.comment);
        account = findViewById(R.id.account);
        comment.setOnClickListener(v -> showDialogComment());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerLyricAdapter(mName, mId, mSid,
                getSupportFragmentManager(), getBaseContext()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        dot1.setImageResource(R.drawable.ic_dot2);
                        dot2.setImageResource(R.drawable.ic_dot1);
                        break;

                    case 1:
                        dot1.setImageResource(R.drawable.ic_dot1);
                        dot2.setImageResource(R.drawable.ic_dot2);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        account.setOnClickListener(v -> viewPager.setCurrentItem(1));
    }

    private void showDialogComment() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString("name", null);
        String city = prefs.getString("email", null);
        if (name == null) {
            Toasty.info(this, getString(R.string.in_name), Toasty.LENGTH_LONG).show();
            showDialogRegister();
        } else {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_send_comment);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            EditText text = dialog.findViewById(R.id.comment);

            SvgRatingBar rate = dialog.findViewById(R.id.rate);

            FloatingActionButton fab = dialog.findViewById(R.id.fab);
            fab.setOnClickListener(v -> {
                Comment comment = new Comment();
                comment.setComment(text.getText().toString().trim().replaceAll("\\s+", " "));
                comment.setRate((int) rate.getRating());
                String di = getDeviceID().trim().replaceAll("\\s+", " ");
                comment.setInfo(di);
                comment.setName(name);
                comment.setCity(city);
                comment.setAlbumId(mId);
                if (text.getText().toString().length() < 5) {
                    Toasty.error(LyricActivity.this, getString(R.string.error_comment), Toasty.LENGTH_LONG).show();
                    return;
                }

                Api service = null;
                try {
                    service = ServiceGenerator.createService(Api.class);
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                service.postComment(mId, comment).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        if (response.code() == 200)
                            Toasty.success(LyricActivity.this, getString(R.string.send_ok)).show();
                        else {
                            try {
                                Log.e(">> Error: ", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                dialog.dismiss();
            });
            dialog.show();
        }
    }

    private void showDialogRegister() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_name_email);
        dialog.setCancelable(true);

        EditText name = dialog.findViewById(R.id.name);
        EditText email = dialog.findViewById(R.id.email);
        FloatingActionButton reg = dialog.findViewById(R.id.fab);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name1 = prefs.getString("name", null);
        String city1 = prefs.getString("email", null);
        if (name1 != null)
            name.setText(name1);
        if (city1 != null)
            email.setText(city1);

        reg.setOnClickListener(v -> {
            if (name.length() < 3) {
                Toasty.error(LyricActivity.this, getString(R.string.error_name), Toasty.LENGTH_LONG).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("name", name.getText().toString().trim().replaceAll("\\s+", " "));
            editor.putString("email", email.getText().toString().trim().replaceAll("\\s+", " "));
            editor.apply();
            Toasty.success(LyricActivity.this, getString(R.string.done), Toasty.LENGTH_LONG).show();
            dialog.dismiss();
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LyricActivity.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
        finish();
    }

    public String getDeviceID() {
        String m_szDevIDShort = "35"
                +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10; // 13 digits
        String m_szAndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String m_szLongID = m_szDevIDShort + m_szAndroidID;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (m != null) {
            m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        }
        return getString(R.string.id) + "" + "#" +
                getString(R.string.if_model) + Build.MODEL + "#" +
                getString(R.string.if_manu) + Build.MANUFACTURER + "#" +
                getString(R.string.if_sd) + Build.VERSION.SDK + "#" +
                getString(R.string.if_brd) + Build.BRAND + "#" +
                getString(R.string.if_xyz) + Build.VERSION.RELEASE;

    }
}