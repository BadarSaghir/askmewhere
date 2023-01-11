

package org.blind.help.object.detection;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;

import android.widget.Toast;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

import org.blind.help.object.detection.env.DoubleClickListener;
import org.blind.help.object.detection.env.ImageUtils;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, SensorEventListener {
  public String voice_text = "";
  public TextToSpeech mTTS;

  public boolean  isSpeaking=false;
  public boolean isHelpMenu = false;
  public int trackI=0;
  private SensorManager sensorManager;
  private Sensor sensor;
  public static double focal=0.0d;
public  static SizeF cameraPhysicalSize;
  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  protected int previewWidth = 1080;
  protected int previewHeight = 2177;
  private boolean debug = false;
  private final float[] rotationMatrix = new float[9];
  public  float luminousityValue=0.0f;

  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;
  public String re = ".*apple.*|.*sink.*|.*car.*|.*cat.*|.*microwave.*|.*mobile.*|.*mug.*|.*person.*|.*bowl.*|.*remote.*|.*toothbrush.*|.*bottle.*|.*toilet.*|.*spoon.*|.*toaster.*|.*suitcase.*|.*laptop.*";
 public float xChange =0;
 public float yChange = 0;
 public float [] history = new float[2];
 public String [] direction = {"NONE","NONE"};
 public  boolean isWelcome=true;
  public boolean isStopLooking=false;
  public boolean isAllLooking=false;
  public  static float horizontalViewField=0.0f;
  public String flashMode="auto";
  public String cmd0="Tap on screen search for object or double tap for the help?";
  public CharSequence cmdWelcome=  "Welcome, You can use following command in this app.\n1You can detect object By Tapping on Screen and saying any sentence containing any of object from above objects\n" +
          "\n2. You can search all objects by saying 'all'  objects after tapping on the screen"+
          "\n3. Tap on screen, then speak the object you are looking for or double tap for the help?\n4. It can detect following objects: Person,Apple,Mug,Car,Cat,bowl,mobile phone,remote control,microwave oven,laptop,toilet," +
          "spoon,toothbrush,bottle,toaster,suitcase, and sink." +
          "\n5. Say turn on, turn off or turn auto to set Mobile Flash Light Mode." +
           "\n6 You can Check light in surrounding, by saying lightness, which include dark, day light, dim light ,moderate light, Too much Light. \n7 Close app by long press";

  public boolean speakWelcome=true;
//  public String cmdWelcome2="Person,Apple,Mug,Car,Cat,bowl, watch,mobile phone,remote control,microwave oven,laptop,toilet,spoon,fork,chair,table,bottle,toaster,suitcase, and platter";
  public TriggerEventListener triggerEventListener;
  public float movementAfterDetection=0;
  public int  REQUEST_CODE_SPEECH_INPUT=1000;
  Button speechBtn;
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
// todo:added sensor

    super.onCreate(null);

Intent intent= getIntent();
   boolean flag=intent.getBooleanExtra("flash",false);
   boolean welcome=intent.getBooleanExtra("welcome",true);
   Bundle bundle = intent.getExtras();
   if (bundle!=null) {
     voice_text = String.valueOf(bundle.getString("voice_text"));
     flashMode=String.valueOf(bundle.getString("mode"));
     isStopLooking = bundle.getBoolean("isStopLooking");
   isAllLooking =  bundle.getBoolean("isAllLooking");


     Log.i("flash", flashMode);
   }
   CameraConnectionFragment.flash=flag;
    mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if(status==TextToSpeech.SUCCESS){
          int rs2=mTTS.setSpeechRate(0.6f);
          int rs= mTTS.setLanguage(Locale.UK);

          if(rs==TextToSpeech.LANG_MISSING_DATA || rs==TextToSpeech.LANG_NOT_SUPPORTED ||rs2==TextToSpeech.ERROR){
            Toast.makeText(getApplicationContext(),"Not Supported Language",Toast.LENGTH_SHORT).show();
          }else{
            if (welcome)
              mTTS.speak(cmdWelcome,TextToSpeech.QUEUE_FLUSH,null,null);
            else if(flag)
              mTTS.speak("Low light turning On Flash Light ",TextToSpeech.QUEUE_FLUSH,null,null);
            else
              mTTS.speak("Moderate light turning off Flash Light ",TextToSpeech.QUEUE_FLUSH,null,null);


          }
        }else {
          Toast.makeText(getApplicationContext(),"Initialization Failed",Toast.LENGTH_SHORT).show();

        }
      }
    }, "com.google.android.tts");
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_camera);


    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    speechBtn= findViewById(R.id.button);
    speechBtn.setText(cmdWelcome);
//light



    speechBtn.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        mTTS.stop();
        isSpeaking=false;
        mTTS.speak("App Has been closed",TextToSpeech.QUEUE_FLUSH,null,"close");
        trackI=0;
        isHelpMenu=true;
        try {
          Thread.sleep(3000);
          System.exit(0);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if(!mTTS.isSpeaking());
        return true;
      }
    });
    speechBtn.setOnClickListener(new DoubleClickListener() {
      @Override
      public void onDoubleClick() {
        mTTS.stop();
        isSpeaking=false;

        trackI=0;
        isHelpMenu=true;
      }
      @Override
      public void onSingleClick() {
        mTTS.stop();
        isSpeaking=false;
        trackI=0;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        try {

          mTTS.stop();
          startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT );//speech
        }catch (Exception e){
          Toast.makeText(getApplicationContext(), " "+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
        }
      }
    });
    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }




    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);






  }

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }





  /** Callback for android.hardware.Camera API */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
       horizontalViewField=  camera.getParameters().getHorizontalViewAngle();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    super.onResume();
    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    if(mTTS!=null){
      mTTS.stop();
      mTTS.shutdown();
    }
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    mTTS.stop();

    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode==0) return;
    switch (requestCode){
      case 1000:
        if (resultCode == RESULT_OK && null != data) {
          ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

          String text = result.get(0).toLowerCase(Locale.ROOT);


          if (text.matches(re)) {
            voice_text = "Move your phone in the environment to find object?";
            mTTS.speak(voice_text, TextToSpeech.QUEUE_FLUSH, null);
            voice_text = result.get(0);
            isStopLooking = false;
            isAllLooking = false;
          } else if (text.matches(".*stop.*")) {
            voice_text = "Object Detection Has Been Stop";
            mTTS.speak(voice_text, TextToSpeech.QUEUE_FLUSH, null);
            voice_text = "";
            isStopLooking = true;
            isAllLooking = false;

          } else if (text.matches(".* all.*")) {
            voice_text = "Looking for all objects, move your phone In the environment";
            mTTS.speak(voice_text, TextToSpeech.QUEUE_FLUSH, null);
            isStopLooking = false;
            isAllLooking = true;
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
          } else if (text.toLowerCase(Locale.ROOT).matches(".*turn auto.*")){
            flashMode = "auto";
            mTTS.speak("Setting Flash Auto Mode",TextToSpeech.QUEUE_FLUSH,null);

          }else if (text.toLowerCase(Locale.ROOT).matches(".*turn on.*")){
            flashMode = "on";
            mTTS.speak("Setting Flash On",TextToSpeech.QUEUE_FLUSH,null);

          }else if (text.toLowerCase(Locale.ROOT).matches(".*turn off.*")){
            flashMode = "off";
            mTTS.speak("Setting Flash off",TextToSpeech.QUEUE_FLUSH,null);

          }else if(text.toLowerCase(Locale.ROOT).matches(".*light.*")){
            if(luminousityValue<10)
            mTTS.speak("It is really here Dark, ",TextToSpeech.QUEUE_FLUSH,null);
            else if(luminousityValue<30){
              mTTS.speak("There is Dim Lighting",TextToSpeech.QUEUE_FLUSH,null);
            }else if (luminousityValue<95){
              mTTS.speak("It sees there is moderate light",TextToSpeech.QUEUE_FLUSH,null);
            }else if (luminousityValue<170){
              mTTS.speak("It seems light is same as day light",TextToSpeech.QUEUE_FLUSH,null);
            }else{
              mTTS.speak("It seems there is too much light",TextToSpeech.QUEUE_FLUSH,null);

            }

          }else{
           // isStopLooking=false;
           // isAllLooking=false;
            mTTS.speak("I am not sure what are you looking for try different object",TextToSpeech.QUEUE_FLUSH,null);
            voice_text="";

          }
        }
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + requestCode);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

//  public void turnFlash(boolean flag){
//  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
                CameraActivity.this,
                "Camera permission is required for this demo",
                Toast.LENGTH_LONG)
            .show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }


        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API =
            (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
       cameraPhysicalSize= characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
        focal =characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
        return cameraId;
      }
    } catch (CameraAccessException e) {
    }

    return null;
  }

  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  CameraActivity.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize()

          );
//      camera2Fragment.turnFlash(true);
      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
      getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

//            camera2Fragment.turnFlash(true);


    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
      getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

    }

  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    setUseNNAPI(isChecked);
  }

  @Override
  public void onClick(View v) {

  }
  protected abstract void processImage();

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

  protected abstract void setNumThreads(int numThreads);

  protected abstract void setUseNNAPI(boolean isChecked);

  @Override
  public void onSensorChanged(SensorEvent event) {
    Log.i("onSensorChanged: ",String.valueOf(event.values.length));
    if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
      Log.i("onSensorChanged: ", String.valueOf(event.values[0]+","+event.values[1]+","+event.values[2]));

      float tempx=history[0] - event.values[0];
      float tempy = history[1] - event.values[1];

      Log.i("onSensorChanged X: ", String.valueOf(xChange+" , error,"+ tempx/(tempx-xChange)*100));

       xChange =tempx;

      double aX= event.values[0];
      double aY= event.values[1];
      //aZ= event.values[2];
      double angle = Math.atan2(aX, aY)/(Math.PI/180);
        yChange =tempy;
      Log.i("onSensorChanged Angle: ", String.valueOf(angle));



      history[0] = event.values[0];
      history[1] = event.values[1];

      if (xChange > 2){
        direction[0] = "LEFT";
      }
      else if (xChange < -2){
        direction[0] = "RIGHT";
      }

    }
  }





}






