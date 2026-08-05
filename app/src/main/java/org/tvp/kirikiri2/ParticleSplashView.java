package org.tvp.kirikiri2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import java.util.Random;

public class ParticleSplashView extends View {
    private Paint paint = new Paint();
    private Random random = new Random();
    private Particle[] particles;

    public ParticleSplashView(Context context){
        super(context);
        particles = new Particle[80];
        for(int i=0; i<particles.length; i++){
            particles[i] = new Particle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        canvas.drawColor(Color.BLACK);

        // 绘制Logo文字
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(90);
        paint.setColor(Color.WHITE);
        canvas.drawText("K", w/2, h/2, paint);
        paint.setTextSize(28);
        canvas.drawText("KIRIKIRI2", w/2, h/2 + 60, paint);

        // 绘制粒子
        for(Particle p : particles){
            p.update(w, h);
            paint.setColor(Color.argb(180, 120, 180, 255));
            canvas.drawCircle(p.x, p.y, p.size, paint);
        }
        invalidate(); // 持续重绘，形成动画
    }

    class Particle{
        float x, y, speedX, speedY, size;
        Particle(){
            x = random.nextInt(1080);
            y = random.nextInt(1920);
            speedX = random.nextFloat() - 0.5f;
            speedY = random.nextFloat() - 0.5f;
            size = random.nextInt(5) + 2;
        }
        void update(int w, int h){
            x += speedX;
            y += speedY;
            if(x<0 || x>w) speedX = -speedX;
            if(y<0 || y>h) speedY = -speedY;
        }
    }
}