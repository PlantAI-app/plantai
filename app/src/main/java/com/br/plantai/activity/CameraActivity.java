package com.br.plantai.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.plantai.event.Classificacao;
import com.br.plantai.model.Classifier;
import com.br.plantai.presenter.CameraMVP;
import com.br.plantai.presenter.CameraPresenter;
import com.br.plantai.view.GeneralView;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements GeneralView,
        CameraMVP.CameraViewImpl {

    // TAG string
    private static final String TAG = "CameraActivity";

    // model vars
    private static final String MODEL_PATH = "optimized_graph.lite";
    private static final String LABEL_PATH = "retrained_labels.txt";
    private static final int INPUT_SIZE = 224;

    // presenter
    private static CameraMVP.CameraPresenterImpl mpresenter;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Classifier classifier;

    // activity vars
    private TextView textViewResult;
    private ImageView btnDetectObject;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private ImageView mMapa;
    private ImageView mClassify;

    // classify vars
    private String mAcuracia = null;
    private String mEspecie = null;
    private Boolean addMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_camera );

        // camera
        cameraView = findViewById( R.id.cameraView );

        // imageView
        mMapa = findViewById( R.id.ic_map );
        mClassify = findViewById( R.id.ic_classify );
        imageViewResult = findViewById( R.id.imageViewResult );

        // textViewResult
        textViewResult = findViewById( R.id.textViewResult );
        textViewResult.setMovementMethod( new ScrollingMovementMethod() );

        // var presenter
        if (mpresenter == null) {
            mpresenter = new CameraPresenter();
        }

        // button
        ImageView btnToggleCamera = findViewById( R.id.btnToggleCamera );
        btnDetectObject = findViewById( R.id.btnDetectObject );

        cameraView.addCameraKitListener( new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Log.d( TAG, "CameraActivity: AddCameraKitListener: onImage: takes the bitmap" );

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap( bitmap, INPUT_SIZE, INPUT_SIZE, false );

                imageViewResult.setImageBitmap( bitmap );
                final List<Classifier.Recognition> results = classifier.recognizeImage( bitmap );

                textViewResult.setText( results.toString() );

                if (!results.isEmpty()) {
                    mAcuracia = results.get( 0 ).getConfidence().toString();
                    mEspecie = results.get( 0 ).getTitle();
                    addMap = false;
                } else {
                    showToast( "Impossível marcar, classificação nula" );
                }
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        } );

        btnToggleCamera.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        } );

        btnDetectObject.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        } );

        // initializes the tensorflow and listener model of the classifier
        initModel();
        classifyListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d( TAG, "CameraActivity: onDestroy: Finishes the tensorflow classifier." );

        super.onDestroy();
        executor.execute( new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        } );
    }

    /**
     * Records the eventBus classification event.
     */
    private void registerEvent() {
        Classificacao mClassification = new Classificacao( mEspecie, mAcuracia );
        EventBus.getDefault().post( mClassification );
    }

    /**
     * Loads tensorflow model.
     */
    private void initModel() {
        Log.d( TAG, "CameraActivity: initTensorFlowAndLoadModel: Loading the tensorflow model." );

        executor.execute( new Runnable() {
            @Override
            public void run() {
                classifier = mpresenter.initTensorFlowAndLoadModel( getAssets(), MODEL_PATH,
                        LABEL_PATH, INPUT_SIZE );

                makeButtonVisible();
            }
        } );
    }

    /**
     * Starting the buttons and the listener of the classifier.
     */
    private void classifyListener() {
        Log.d( TAG, "CameraActivity: classifyListener: starting the listener of the classifier." );

        mMapa.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
            }
        } );

        mClassify.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // when activity starts
                if (mAcuracia == null && mEspecie == null) {
                    showToast( "Nenhuma classificação realizada." );
                } else {
                    // classification is done
                    if (!addMap) {
                        // classify event registered
                        registerEvent();
                        addMap = true;

                        showToast( "Espécie adicionada ao mapa." );
                    } else {
                        showToast( "Classificação já registrada." );
                    }
                }
            }
        } );
    }

    @Override
    public void makeButtonVisible() {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility( View.VISIBLE );
            }
        } );
    }

    @Override
    public void showToast(String message) {
        Toast.makeText( this, message, Toast.LENGTH_SHORT ).show();
    }
}
