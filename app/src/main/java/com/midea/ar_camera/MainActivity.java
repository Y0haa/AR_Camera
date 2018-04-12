package com.midea.ar_camera;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    public static Camera camera = null;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    private boolean previewing = false;
    RelativeLayout relativeLayout;
    RelativeLayout containerTag;

    public RequestManager mGlideRequestManger;
    ImageView iv_image;

    Uri selectedUri;
    private ViewGroup mSelectedImagesContainer;

    private float scale = 1f;

    private ScaleGestureDetector detector;

    private float xCoOrdinate, yCoOrdinate;

    private int xDelta;
    private int yDelta;

    private Matrix matrix = new Matrix();

    ViewPort viewPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGlideRequestManger = Glide.with(this);

        ImageView imageView = new ImageView( MainActivity.this);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        relativeLayout=(RelativeLayout) findViewById(R.id.containerImg);

        containerTag=(RelativeLayout) findViewById(R.id.container1);

        viewPort = new ViewPort(this);

        containerTag.addView(viewPort);

        cameraSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);

        cameraSurfaceHolder = cameraSurfaceView.getHolder();
        cameraSurfaceHolder.addCallback(this);

        iv_image = (ImageView) findViewById(R.id.iv_image);
        mSelectedImagesContainer = (ViewGroup) findViewById(R.id.selected_photos_container);

        detector = new ScaleGestureDetector(this,new ScaleListener(imageView));


        Button btn_single_show =(Button) findViewById(R.id.btn_single_show);

      //  ScaleAnimation scaleAnimation = new ScaleAnimation(0,1,0,1,ScaleAnimation.RELATIVE_TO_SELF,.5f,ScaleAnimation.RELATIVE_TO_SELF,.5f);
        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
        scale.setDuration(900);
        scale.setInterpolator(new BounceInterpolator());
        btn_single_show.startAnimation(scale);


        setSingleShowButton();

        final Button CameraBtn = (Button) findViewById(R.id.btn_Camera);
        Button ScreenLock = (Button) findViewById(R.id.snapshot);

        CameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(cameraShutterCallback,cameraPictureCallbackRaw,mPicture);
                Toast.makeText(MainActivity.this, "Image Captured", Toast.LENGTH_SHORT).show();

            }
        });

        ScreenLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null,null, mPicture1);// Needed for the preview
                CameraBtn.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Screen Locked", Toast.LENGTH_SHORT).show();

            }

        });

    }

    @Override
    protected void onStart() {
        if (PermissionHelper.hasPermission(this)) {
            super.onStart();
        } else {
            PermissionHelper.requestPermission(this);
        }
    }

//       Inform user of Permission Requirments
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!PermissionHelper.hasPermission(this)) {
            Toast.makeText(this,
                    "您需要相机和存储权限才能运行此应用程序，请授予权限继续使用该应用程序。", Toast.LENGTH_LONG).show();
            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                PermissionHelper.launchPermissionSettings(this);
            }
        } else {
            onStart();

        }

    }



    Camera.ShutterCallback cameraShutterCallback = new Camera.ShutterCallback()
    {
        @Override
        public void onShutter()
        {
            // TODO Auto-generated method stub
        }
    };
    Camera.PictureCallback cameraPictureCallbackRaw = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub
        }
    };


//     Capture image, store in dir and preview image for user
    private Camera.PictureCallback mPicture;
    {
        mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                //Create the bitmap for the camera frame and scaled screen size
                Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data,0, data.length);
                Bitmap cameraScaledBitmap = Bitmap.createScaledBitmap(cameraBitmap, cameraBitmap.getScaledWidth(1020),cameraBitmap.getScaledHeight(1025),true);
                final int wid = cameraScaledBitmap.getScaledWidth(0);
                int hgt = cameraScaledBitmap.getScaledHeight(0);
                Bitmap newImage = Bitmap.createBitmap(wid, hgt,Bitmap.Config.ARGB_8888); //First layer

                final Canvas canvas = new Canvas(newImage);
                canvas.drawBitmap(cameraScaledBitmap,0,0,null); // the background

                // get scaling and images from ViewPort
                try {
                    Bitmap images = loadBitmapFromView(viewPort,viewPort.getWidth(),viewPort.getHeight());// the overlay images

                    canvas.drawBitmap(images,0,0,null); // here we combine the two
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //Creating the storage Dir, and setting the name format
                File storagePath = new File(Environment.getExternalStorageDirectory()+ "/PhotoAR1/");
                storagePath.mkdirs();
                String finalName = Long.toString(System.currentTimeMillis());
                File myImage =new File (storagePath, finalName+ ".jpg");//

                String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+finalName+".jpg";


                camera.startPreview();


                try {
                    FileOutputStream fos = new FileOutputStream(myImage);
                    newImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }


                newImage.recycle();
                newImage = null;
                cameraBitmap.recycle();
                cameraBitmap = null;

               // getting the view
                Uri photoURI = null;
                try {
                    photoURI = FileProvider.getUriForFile(MainActivity.this,
                            getString(R.string.Application_Id) + ".provider",
                            myImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }


              //  Previewing the view/image
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);

                intent.setDataAndType(photoURI, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


                startActivity(intent);

            }
        };
    }

    public static Bitmap loadBitmapFromView(View v, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width , height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

//     Getting the screen background snapshot
    private Camera.PictureCallback mPicture1;
    {
        mPicture1 = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {


                ImageView imageView0= (ImageView) findViewById(R.id.iv_image);

                Bitmap bitmap = null;
                try {
                    bitmap = ((BitmapDrawable)  imageView0.getDrawable()).getBitmap();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data,0, data.length);
                Bitmap cameraScaledBitmap = Bitmap.createScaledBitmap(cameraBitmap, cameraBitmap.getWidth(),cameraBitmap.getHeight(),true);
                int wid = cameraScaledBitmap.getWidth();
                int hgt = cameraScaledBitmap.getHeight();
                Bitmap newImage = Bitmap.createBitmap(wid, hgt,Bitmap.Config.ARGB_8888); //First layer

                Canvas canvas = new Canvas(newImage);


                Bitmap images = loadBitmapFromView(viewPort,viewPort.getWidth(),viewPort.getHeight());
                canvas.drawBitmap(images,0,0,null); // here we combine the two

                canvas.drawBitmap(cameraScaledBitmap,0,0,null); // here we combine the two

                File storagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                storagePath.mkdirs();
                String finalName = Long.toString(System.currentTimeMillis());
                File myImage =new File (storagePath, finalName+ ".jpg");

                String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+finalName+".jpg";

                try {
                    FileOutputStream fos = new FileOutputStream(myImage);
                    newImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                canvas.drawBitmap(cameraScaledBitmap,0,0,null);

            }
        };
    }

//     On touch for images
    public boolean onTouchEvent(MotionEvent event ) {
//  re-route the Touch Events to the ScaleListener class

        ImageView imageView  = new ImageView(MainActivity.this);
        switch (event.getActionMasked()){

            case MotionEvent.ACTION_MOVE:
                imageView.animate().x(event.getRawX() +xCoOrdinate).y(event.getRawY() +yCoOrdinate).setDuration(0).start();
                break;

            case MotionEvent.ACTION_UP:
                xCoOrdinate = imageView.getX() - event.getRawX();
                yCoOrdinate = imageView.getY() - event.getRawY();

                break;

            default:
                return false;
        }

        detector.onTouchEvent(event);
        //  detector2.onTouchEvent(event);
        return true;
    }
    public void Restart()
    {
        this.recreate();
    }

    public void reset(View view) {
        Restart();
        Toast.makeText(this, "Camera Selection Reset", Toast.LENGTH_SHORT).show();
    }

//     Image scaling
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {


        float onScaleBegin = 1;
        float onScaleEnd = 0;


        ImageView imageView;
        public ScaleListener(ImageView imageView) {
            this.imageView = imageView;
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {


            scale *= detector.getScaleFactor();
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
            // Don't let the object get too small or too large.
            scale = Math.max(0.1f, Math.min(scale, 10.0f));
            matrix.setScale(scale,scale);
            imageView.setImageMatrix(matrix);


            return true;

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //    scaleImage(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY(), true);

            // Toast.makeText(getApplicationContext(),"Scale Begin" ,Toast.LENGTH_SHORT).show();
            onScaleBegin = scale;

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            // Toast.makeText(getApplicationContext(),"Scale Ended",Toast.LENGTH_SHORT).show();
            onScaleEnd = scale;

            if (onScaleEnd > onScaleBegin){
                //  Toast.makeText(getApplicationContext(),"Scaled Up by a factor of  " + String.valueOf( onScaleEnd / onScaleBegin ), Toast.LENGTH_SHORT  ).show();
            }

            if (onScaleEnd < onScaleBegin){
                //  Toast.makeText(getApplicationContext(),"Scaled Down by a factor of  " + String.valueOf( onScaleBegin / onScaleEnd ), Toast.LENGTH_SHORT  ).show();
            }

            super.onScaleEnd(detector);
        }
    }
//     On click Displaying the Bottom Item picker
    private void setSingleShowButton() {


        final Button btn_single_show = (Button) findViewById(R.id.btn_single_show);

        final Button btn = (Button) findViewById(R.id.btn_Camera); // Camera for Image capture
        final Button btn2 = (Button) findViewById(R.id.snapshot);
        btn_single_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn2.setVisibility(View.GONE);



                BottomItemPicker bottomSheetDialogFragment = new BottomItemPicker.Builder(MainActivity.this)
                        .setOnImageSelectedListener(new BottomItemPicker.OnImageSelectedListener() {
                            @SuppressLint("ClickableViewAccessibility")
                            @Override
                            public void onImageSelected(int resourceId) {

                                viewPort.add(resourceId);

                            }

                        })
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                        .setSelectedUri(selectedUri)
                        .showVideoMedia()
                        .setPeekHeight(900)
                        .create();

                bottomSheetDialogFragment.show(getSupportFragmentManager());


            }
        });
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height)
    {
        // TODO Auto-generated method stub

        if(previewing)
        {
            camera.stopPreview();
            previewing = false;
        }
        try
        {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setPictureSize(640, 480);
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(90);

            }

            // parameters.setRotation(90);
            camera.setParameters(parameters);

            camera.setPreviewDisplay(cameraSurfaceHolder);
            camera.startPreview();
            previewing = true;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        try
        {
            camera = Camera.open();
        }
        catch(RuntimeException e)
        {
            Toast.makeText(getApplicationContext(), "Device camera  is not working properly, please try after sometime.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }














}
