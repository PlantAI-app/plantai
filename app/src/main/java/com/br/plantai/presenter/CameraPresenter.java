package com.br.plantai.presenter;

import android.content.res.AssetManager;

import com.br.plantai.model.Classifier;
import com.br.plantai.model.TensorFlowImageClassifier;

public class CameraPresenter implements CameraMVP.CameraPresenterImpl {

    public Classifier classifier;

    /**
     * Initializer constructor for the tensorflow classifier.
     *
     * @param assetManager asset manager files.
     * @param modelPath    path in which the generated model is found (.lite).
     * @param labelPath    path where the classifier's labels are located (.txt).
     * @param inputSize    input dimensions of the classifier.
     * @return classifier.
     */
    @Override
    public Classifier initTensorFlowAndLoadModel(final AssetManager assetManager,
                                                 final String modelPath,
                                                 final String labelPath,
                                                 final int inputSize) {

        try {
            classifier = TensorFlowImageClassifier.create( assetManager, modelPath,
                    labelPath, inputSize );
        } catch (final Exception e) {
            throw new RuntimeException( "Error initializing TensorFlow!", e );
        }

        return (classifier);
    }
}

