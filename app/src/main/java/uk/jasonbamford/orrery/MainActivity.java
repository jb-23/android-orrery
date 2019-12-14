/* ***********  copyright Jason Bamford 2019 ** please visit http://jasonbamford.uk/  *********** */
package uk.jasonbamford.orrery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.Button;

import android.widget.SeekBar;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.app.Dialog;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;

public class MainActivity extends AppCompatActivity {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    private ShapeDrawable[] discDrawables = new ShapeDrawable[10];

    private boolean animate = true;
    private double frameTime = 0.0;
    private double frameRate = 1.0;
    private double frameScale = 3.0;

    Handler handler = new Handler();
    Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            advanceTime();
            drawPlanets(frameTime,frameScale);
            handler.postDelayed(this,20);
        }
    };

    public void userChooseDate(View view) {
        DatePickerDialog.OnDateSetListener dialistener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Date d;
                try {
                    d = dateFormatter.parse(String.format("%d/%d/%d", day+1, month+1, year));
                } catch (Exception e) {
                    d = new Date();
                }
                updateTime((d.getTime() - 946728000000L) / 86400000L);
            }
        };

        Date d = new Date((long)frameTime * 86400000L + 946728000000L);

        Dialog dialog = new DatePickerDialog(MainActivity.this, dialistener, 1900 + d.getYear(), d.getMonth(), d.getDate());

        dialog.show();
    }

    public void userAnimationToggle(View view) {
        animate = !animate;
        if (animate) {
            handler.postDelayed(animationRunnable,0);
        } else {
            handler.removeCallbacks(animationRunnable);
        }
    }

    private void userSetScale(double p) {
        frameScale = 100 - 99 * Math.pow(p,0.5);
        frameRate = Math.sqrt(Math.pow(frameScale / 3.0,1.5));
        drawPlanets(frameTime,frameScale);
    }

    private void advanceTime() {
        frameTime += frameRate;

        showTime();
    }

    private void updateTime(double time) {
        frameTime = time;
        drawPlanets(frameTime,frameScale);
        showTime();
    }

    private void showTime() {
        Date date = new Date((long)frameTime * 86400000L + 946728000000L);

        Button userButton = findViewById(R.id.buttonDate);
        userButton.setText(dateFormatter.format(date));
    }

    private void drawPlanets(double time, double viewDiameter) {
        ImageView userImageView;
        Bitmap bitmap;
        Canvas canvas;


        userImageView = findViewById(R.id.imageView);

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        int iw = userImageView.getWidth();
        int ih = userImageView.getHeight();
        if (iw == 0 | ih == 0)  return;
        bitmap = Bitmap.createBitmap(iw,ih,config);

        canvas = new Canvas(bitmap);

        Paint paintBackground = new Paint();
        paintBackground.setColor(0xFF000000);

        canvas.drawBitmap(bitmap, 0, 0, paintBackground);


        int ix;
        if (iw > ih) ix = ih;
        else ix = iw;

        int icx = iw / 2;
        int icy = ih / 2;

        int rPlanet = ix / 125;   // radius of planet
        int rSun = ix / 65;

        double scale = ix / viewDiameter * 0.88;
        for (int p=0; p<9; ++p) {
            PlanetPosition k = Kepler.planet_at(p,time);
            int x = icx + (int)(k.x * scale);
            int y = icy - (int)(k.y * scale);
            discDrawables[p+1].setBounds(x-rPlanet,y-rPlanet,x+rPlanet,y+rPlanet);
            discDrawables[p+1].draw(canvas);
        }
        discDrawables[0].setBounds(icx-rSun,icy-rSun,icx+rSun,icy+rSun);
        discDrawables[0].draw(canvas);

        userImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
    }

    /* ****************************************************************************************** */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuAbout:
                userAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        int[] colours = {
                0xffffff88,0xffaaaaaa,0xffe0e0aa,0xff00aaff,0xffcc6666,
                0xffe0e066,0xffffaa00,0xff88eeee,0xff0088dd,0xffaa8844
        };
        for (int i=0; i<10; ++i) {
            ShapeDrawable sd = new ShapeDrawable(new OvalShape());
            sd.getPaint().setColor(colours[i]);
            discDrawables[i] = sd;
        }

        Date d = new Date();
        frameTime = (d.getTime() - 946728000000L) / 86400000L;

        ImageView userImageView = findViewById(R.id.imageView);
        userImageView.post(new Runnable() {
            @Override
            public void run() {
                drawPlanets(frameTime,frameScale);
                showTime();
            }
        });

        SeekBar seekScale = findViewById(R.id.seekScale);
        seekScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                userSetScale(progress / (double)seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("FRAMEANIMATE", animate);
        bundle.putDouble("FRAMETIME", frameTime);
        bundle.putDouble("FRAMERATE", frameRate);
        bundle.putDouble("FRAMESCALE", frameScale);

        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        animate = bundle.getBoolean("FRAMEANIMATE");
        frameTime = bundle.getDouble("FRAMETIME");
        frameRate = bundle.getDouble("FRAMERATE");
        frameScale = bundle.getDouble("FRAMESCALE");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animate) handler.postDelayed(animationRunnable,0);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(animationRunnable);
        super.onPause();
    }

    public void userAbout() {
        try {
            final String message = "Orrery v0.85, Copyright 2019\n\nDeveloper - Jason Bamford\n\nhttp://jasonbamford.uk";
            final SpannableString message1 = new SpannableString(message);
            Linkify.addLinks(message1, Linkify.WEB_URLS);

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("About Orrery");
            alertDialog.setMessage(message1);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception e) {
            return;
        }
    }
}
