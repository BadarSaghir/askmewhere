

package org.blind.help.object.detection;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.blind.help.object.detection.customview.OverlayView;
import org.blind.help.object.detection.customview.OverlayView.DrawCallback;
import org.blind.help.object.detection.env.BorderedText;
import org.blind.help.object.detection.env.ImageUtils;
import org.blind.help.object.detection.tflite.Classifier;
import org.blind.help.object.detection.tflite.YoloV4Classifier;
import org.blind.help.object.detection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    public static String speakThis = "";
    public String searchingClass = "";
    int o=0;

    private static final int TF_OD_API_INPUT_SIZE = 416;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "yolov4-416-fp16.tflite";
    public boolean IsDetectionFinish = false;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";
    public String cmd1Help = "App can do following functionalities:\n" +
            "\nIt has Flash Light mode on, off, auto can be activated by saying turn on, turn off, turn auto."+
            "\nYou can Check light in surrounding, by saying lightness, which include dark, day light, dim light ,moderate light, Too much Light."+
            "\nIt can detect following objects. Person, Apple, Mug, Car, Cat. bowl. toothbrush. mobile phone. remote control. microwave oven" +
            "laptop. toilet. spoon. bottle. toaster. suitcase. sink. \nClose App by long press";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(getScreenWidth(), getScreenHeight() * 2);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;
    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;
    String name;
    float confi;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            Button btn = (Button) findViewById(R.id.button);
            detector =
                    YoloV4Classifier.create(
                            btn.getText().toString(),
                            this,
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);


            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected void processImage() {

        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        /** background **/
        runInBackground(
                () -> {



                    Log.i("focal", String.valueOf(DetectorActivity.focal));

                    //todo: Navigation implementation
                    final long startTime = SystemClock.uptimeMillis();


                    final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                    if (!results.isEmpty() && results.get(0).getTitle().toLowerCase(Locale.ROOT).matches(re))
                        searchingClass = results.get(0).className;


                    trackI = trackI + 1;
                    if (trackI >= 25 && !isHelpMenu && !isSpeaking) {

                        speakThis = searchingClass.isEmpty() ? cmd0 : cmd0 + " or tap and say " + searchingClass + " for searching " + searchingClass;
                        trackI = 2;
                    }


                    if (mTTS.isSpeaking()) {
                        trackI = 0;
                        isSpeaking = true;
                    }
                    if (!mTTS.isSpeaking()) {
                        isSpeaking = false;

                    }


                    cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                    final Canvas canvas1 = new Canvas(cropCopyBitmap);
                    final Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Style.STROKE);
                    paint.setStrokeWidth(2.0f);

                    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    switch (MODE) {
                        case TF_OD_API:
                            minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                            break;
                    }
                    //todo: Add Help menu having list of commands
                    if (isHelpMenu && !isSpeaking) {

                        if (!results.isEmpty())
                            searchingClass = results.get(0).className;


                        speakThis = cmd1Help;

                        isHelpMenu = false;
                    }


                    final List<Classifier.Recognition> mappedRecognitions =
                            new LinkedList<Classifier.Recognition>();
                    int i = 0;

                    if (!isSpeaking && !isHelpMenu && voice_text.toLowerCase(Locale.ROOT).matches(re + "|.*stop.*|.*all.*"))
                        for (final Classifier.Recognition result : results) {
                            movementAfterDetection = 0;
                            final RectF location = result.getLocation();

                            name = result.getTitle().toLowerCase(Locale.ROOT);
                            confi = result.getConfidence();
                            String className = result.className.toLowerCase(Locale.ROOT);
                            Log.i("loop: Class Name: ", className);
                            Log.i("loop: voice text: ", className);
                            Log.i("loop: condition 1: ", String.valueOf(location != null && result.getConfidence() >= minimumConfidence && name.toLowerCase(Locale.ROOT).matches(re)));

                            if (location != null && result.getConfidence() >= minimumConfidence && name.toLowerCase(Locale.ROOT).matches(re)) {
                                trackI = 0;
                                if (!isAllLooking && !isStopLooking && voice_text.toLowerCase(Locale.ROOT).contains(className)) {

                                    i++;
                                    speakThis = speakThis + "\nobject" + i + "." + name + " with " + String.format("%.02f", confi * 100) + " confidence.\n";
                                } else if (isAllLooking) {
                                    i++;
                                    speakThis = speakThis + "\nobject" + i + "." + name + " with " + String.format("%.02f", confi * 100) + " confidence.\n";
                                } else {
                                    continue;
                                }

                                canvas1.drawRect(location, paint);
                                confi = 100 * result.getConfidence();
                                cropToFrameTransform.mapRect(location);

                                result.setLocation(location);
                                mappedRecognitions.add(result);


                            }
                            IsDetectionFinish = true;
                        }

                    tracker.trackResults(mappedRecognitions, currTimestamp);
                    trackingOverlay.postInvalidate();

                    computingDetection = false;

                    runOnUiThread(
                            () -> {

                               Log.i("csi",voice_text);
                                luminousityValue=calculateAverageLuminousity()+luminousityValue;

                                if (o>=5) {
                                    luminousityValue=luminousityValue/5;
                                    if(flashMode.toLowerCase(Locale.ROOT).matches("auto")&& !flashMode.isEmpty())
                                        flashLightChecking(luminousityValue);
                                    if(flashMode.toLowerCase(Locale.ROOT).matches("on") && !flashMode.isEmpty()){
                                        turnFlashLight(true,"");
                                        }
                                    else if(flashMode.toLowerCase(Locale.ROOT).matches("off")){
                                        turnFlashLight(false,"");
                                        flashMode="";
                                    }

                                    luminousityValue=0;
                                   o=0;
                                }
o++;


                                if (!speakThis.isEmpty()) {
                                    mTTS.speak(speakThis, TextToSpeech.QUEUE_FLUSH, null, null);
                                    speakThis = "";
                                    if (mTTS.isSpeaking()) {
                                        isSpeaking = true;

                                    }
                                    if (!mTTS.isSpeaking()) {
                                        isSpeaking = false;
                                    }
                                }

                            });
                });
    }


    private void flashLightChecking(float luminosity) {

//        turnFlashLight();

        int threshold = 25;  // Adjust this value to your liking

        if (luminosity<threshold && !CameraConnectionFragment.flash)
            turnFlashLight(true,"auto");
        else if (CameraConnectionFragment.flash && ((luminosity>65 && luminosity<80)||(luminosity>125) ))
            turnFlashLight(false,"auto");
        Log.i("luminous",String.valueOf(luminosity));




    }

    private float calculateAverageLuminousity() {
        int width = croppedBitmap.getWidth();
        int height = croppedBitmap.getHeight();
        int totalLuminance = 0;
        int numPixels = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = croppedBitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                int luminance = (int)(0.299 * r + 0.587 * g + 0.114 * b);  // Calculate luminance
                totalLuminance += luminance;
                numPixels++;
            }
        }
        float luminosity= totalLuminance/numPixels;
        return luminosity;
    }

    private void turnFlashLight(boolean flag, String mode) {
        Intent intent =new Intent(DetectorActivity.this, DetectorActivity.class);
        Bundle bundle =new Bundle();
        bundle.putString("voice_text",voice_text);
        bundle.putString("mode",mode);
//        isStopLooking = false;
//        isAllLooking = true;
        bundle.putBoolean("isStopLooking",isStopLooking);
        bundle.putBoolean("isAllLooking",isAllLooking);

        intent.putExtra("flash",flag);
        intent.putExtra("welcome",false);
        intent.putExtras(bundle);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    public static int getScreenWidth() {
        Log.i("wxh",""+Resources.getSystem().getDisplayMetrics().widthPixels+"x"+Resources.getSystem().getDisplayMetrics().heightPixels);
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {


        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }


}
