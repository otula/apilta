package fi.tut.pori.otula.roadroamer.utils;

import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/*
Reference:
[1] http://stackoverflow.com/questions/28003186/capture-picture-without-preview-using-camera2-api
[2] https://gist.github.com/RoundSparrow/142b840ca86ba7a46639f23c5c0d195b

Problem
   1.  BufferQueue has been abandoned  from ImageCapture
 */
public class SimpleCamera2ServicePublish extends Service {
    protected static final String TAG = SimpleCamera2ServicePublish.class.getSimpleName();
    protected static final int CAMERA_CHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;
    protected boolean isCameraReady = false;
    private String _cameraId = null;
    private int _cameraSensorOrientation = 90;  //typically 90 or 270, will be set during setup of camera

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            isCameraReady = true;
//            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback onDisconnected");
            isCameraReady = false;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
            isCameraReady = false;
        }
    };

    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
//            Log.d(TAG, "CameraCaptureSession.StateCallback onConfigured");
            SimpleCamera2ServicePublish.this.session = session;
            try {
                session.capture(createCaptureRequest(), null, null);
                //session.setRepeatingRequest(createCaptureRequest(), null, null);
            } catch (CameraAccessException e){
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Configuration failed");
        }
    };

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireLatestImage();
            if (img != null) {
                processImage(img);
                img.close();
            }
            session.close();
            session = null;
        }
    };

    public void readyCamera(){
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager.getCameraIdList());
            if(pickedCamera == null){
                Log.e(TAG, "Suitable Camera Device not found");
                _cameraId = null;
                return;
            }else{
                _cameraId = pickedCamera;
            }
            manager.openCamera(_cameraId, cameraStateCallback, null);

            Size cameraReaderSize = getMaxOutputSize();
            imageReader = ImageReader.newInstance(cameraReaderSize.getWidth(), cameraReaderSize.getHeight(), ImageFormat.JPEG, 2 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);

            _cameraSensorOrientation = getCameraSensorOrientation();
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand flags " + flags + " startId " + startId);

        readyCamera();

        return super.onStartCommand(intent, flags, startId);
    }

    public void takeSingleImage(){
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public boolean isCameraReady(){
        return isCameraReady;
    }

    @Override
    public void onDestroy() {
        try {
            isCameraReady = false;
            if(session != null){
                session.abortCaptures();
            }
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        if(session != null){
            session.close();
        }
    }

    /**
     *  Process image data as desired.
     */
    protected void processImage(Image image){
        //Process image data
    }

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_EDOF);
            builder.set(CaptureRequest.JPEG_ORIENTATION, getJPEGOrientation());

            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     *  Return the Camera Id which matches the field CAMERA_CHOICE.
     */
    private String getCamera(String[] cameraIdList){
        for (String cameraId : cameraIdList) {
            CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
            if(characteristics == null){
                continue;
            }
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CAMERA_CHOICE) {
                return cameraId;
            }
        }
        return null;
    }

    private Size getMaxOutputSize(){
        CameraCharacteristics characteristics = getCameraCharacteristics(_cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
        if(sizes == null){
            return null;
        }else{
            Size largest = Collections.max(Arrays.asList(sizes), new CompareSizesByArea());
            return largest;
        }
    }

    private int getCameraSensorOrientation(){
        CameraCharacteristics characteristics = getCameraCharacteristics(_cameraId);
        return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getJPEGOrientation() {
        int rotation = this.getSystemService(WindowManager.class).getDefaultDisplay().getRotation();
        int deviceOrientation = ORIENTATIONS.get(rotation);
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        if(CAMERA_CHOICE == CameraCharacteristics.LENS_FACING_FRONT) {    // Reverse device orientation for front-facing cameras
            deviceOrientation = -deviceOrientation;
        }
        return (deviceOrientation + _cameraSensorOrientation + 270) % 360;
    }

    private CameraCharacteristics getCameraCharacteristics(String cameraId){
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            return manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
