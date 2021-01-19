package com.br.plantai.model;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


public class TensorFlowImageClassifier implements Classifier {

    private static final String TAG = "TensorFlowImageClassifier";

    // number of results in the ranking
    private static final int MAX_RESULTS = 3;

    // input dimension
    private static final int BATCH_SIZE = 1;
    private static final int PIXEL_SIZE = 3;
    private static final float THRESHOLD = 0.1f;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private int inputSize;
    // an instance of the driver class to run model inference with Tensorflow Lite
    private Interpreter interpreter;

    // labels that correspond to the output of the model
    private List<String> labelList;

    /**
     * Constructor for the tensorflow classifier.
     *
     * @param assetManager asset manager files.
     * @param modelPath    path in which the generated model is found (.lite).
     * @param labelPath    path where the classifier's labels are located (.txt).
     * @param inputSize    input dimensions of the classifier.
     * @return classifier.
     */
    @SuppressLint("LongLogTag")
    public static Classifier create(AssetManager assetManager, String modelPath, String labelPath,
                                    int inputSize) throws IOException {

        TensorFlowImageClassifier classifier = new TensorFlowImageClassifier();
        classifier.interpreter = new Interpreter( classifier.loadModelFile( assetManager, modelPath ) );
        classifier.labelList = classifier.loadLabelList( assetManager, labelPath );
        classifier.inputSize = inputSize;

        return classifier;
    }

    /**
     * Classifies the bitmap registered by the app camera.
     *
     * @param bitmap a bitmap corresponds to the app camera image.
     * @return An array list with the classified results ordered.
     */
    @SuppressLint("LongLogTag")
    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {

        ByteBuffer byteBuffer = convertBitmapToByteBuffer( bitmap );
        float[][] result = new float[1][labelList.size()];
        interpreter.run( byteBuffer, result );
        return getSortedResult( result );
    }

    @Override
    public void close() {
        interpreter.close();
        interpreter = null;
    }

    /**
     * Loads the model (.lite) of the classifier.
     *
     * @param assetManager asset manager files.
     * @param modelPath    path in which the generated model is found (.lite).
     * @return Loads a region of file into memory.
     */
    @SuppressLint("LongLogTag")
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws
            IOException {

        AssetFileDescriptor fileDescriptor = assetManager.openFd( modelPath );
        FileInputStream inputStream = new FileInputStream( fileDescriptor.getFileDescriptor() );
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map( FileChannel.MapMode.READ_ONLY, startOffset, declaredLength );
    }

    /**
     * Loads the labels(.txt) of the classifier.
     *
     * @param assetManager asset manager files.
     * @param labelPath    path where the classifier's labels are located (.txt).
     * @return An ArrayList with the labels read from labelPath.
     */
    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws
            IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader =
                new BufferedReader( new InputStreamReader( assetManager.open( labelPath ) ) );
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add( line );
        }
        reader.close();
        return labelList;
    }

    /**
     * Receives a bitmap and transforms to bytebuffer to classify.
     *
     * @param bitmap a bitmap corresponds to the app camera image.
     * @return a Bytebuffer object.
     */
    @SuppressLint("LongLogTag")
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(
                4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE );
        byteBuffer.order( ByteOrder.nativeOrder() );

        // Preallocate buffers for storing image data in.
        int[] intValues = new int[inputSize * inputSize];

        bitmap.getPixels( intValues, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight() );

        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {

                final int val = intValues[pixel++];
                byteBuffer.putFloat( (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD );
                byteBuffer.putFloat( (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD );
                byteBuffer.putFloat( (((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD );
            }
        }
        return byteBuffer;
    }

    /**
     * Sorts the probability vector in descending order.
     *
     * @param labelProbArray a float vector with the probabilities of the predicted labels.
     * @return an ArrayList with the highest probabilities achieved.
     */
    @SuppressLint({"DefaultLocale", "LongLogTag"})
    private List<Recognition> getSortedResult(float[][] labelProbArray) {

        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                return Float.compare( rhs.getConfidence(), lhs.getConfidence() );
                            }
                        } );

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = (labelProbArray[0][i]);
            if (confidence > THRESHOLD) {
                pq.add( new Recognition( "" + i,
                        labelList.size() > i ? labelList.get( i ) : "unknown",
                        confidence ) );
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min( pq.size(), MAX_RESULTS );
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add( pq.poll() );
        }

        return recognitions;
    }
}
