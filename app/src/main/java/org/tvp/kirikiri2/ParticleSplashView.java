package org.tvp.kirikiri2;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Random;



public class ParticleSplashView extends View {


    private final Paint paint = new Paint();

    private final Random random = new Random();


    private Particle[] particles;


    private boolean running = true;


    private long lastFrameTime = 0;



    private static final int MAX_PARTICLES = 80;



    public ParticleSplashView(Context context) {

        super(context);


        paint.setAntiAlias(true);


        particles =
                new Particle[MAX_PARTICLES];


        for(int i=0;i<particles.length;i++){

            particles[i] =
                    new Particle();

        }


        setBackgroundColor(Color.BLACK);

    }







    @Override
    protected void onDraw(Canvas canvas) {


        super.onDraw(canvas);



        if(!running){

            return;

        }



        long now =
                System.currentTimeMillis();



        // 限制约60FPS
        if(now-lastFrameTime < 16){

            invalidate();

            return;

        }


        lastFrameTime = now;



        int w=getWidth();

        int h=getHeight();



        canvas.drawColor(Color.BLACK);



        drawLogo(canvas,w,h);



        drawParticles(canvas,w,h);



        invalidate();

    }








    private void drawLogo(
            Canvas canvas,
            int w,
            int h){


        paint.setTextAlign(
                Paint.Align.CENTER
        );


        paint.setColor(
                Color.WHITE
        );



        paint.setTextSize(
                Math.min(w,h)*0.12f
        );



        canvas.drawText(
                "K",
                w/2f,
                h/2f,
                paint
        );



        paint.setTextSize(
                Math.min(w,h)*0.035f
        );



        canvas.drawText(
                "KIRIKIRI2",
                w/2f,
                h/2f+70,
                paint
        );


    }









    private void drawParticles(
            Canvas canvas,
            int w,
            int h){



        paint.setColor(
                Color.argb(
                        180,
                        120,
                        180,
                        255
                )
        );



        for(Particle p:particles){


            p.update(w,h);



            canvas.drawCircle(
                    p.x,
                    p.y,
                    p.size,
                    paint
            );


        }


    }








    public void stopAnimation(){


        running=false;


    }






    public void startAnimation(){


        running=true;


        invalidate();


    }









    class Particle{


        float x;

        float y;

        float speedX;

        float speedY;

        float size;




        Particle(){


            reset();

        }






        void reset(){


            x=random.nextFloat();

            y=random.nextFloat();


            speedX =
                    (random.nextFloat()-0.5f)
                            *0.8f;


            speedY =
                    (random.nextFloat()-0.5f)
                            *0.8f;



            size =
                    random.nextInt(5)+2;


        }







        void update(
                int w,
                int h){



            x += speedX;

            y += speedY;



            if(x<0 || x>w){

                speedX=-speedX;

            }



            if(y<0 || y>h){

                speedY=-speedY;

            }


        }


    }






    @Override
    protected void onDetachedFromWindow(){


        super.onDetachedFromWindow();


        running=false;


    }


}