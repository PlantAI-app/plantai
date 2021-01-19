package com.br.plantai.presenter;

import android.content.res.AssetManager;

import com.br.plantai.model.Classifier;

public interface CameraMVP {

    interface CameraPresenterImpl {
        Classifier initTensorFlowAndLoadModel(final AssetManager assetManager,
                                              final String modelPath,
                                              final String labelPath,
                                              final int inputSize);
    }

    interface CameraViewImpl {
        void makeButtonVisible();
    }
}