package io.p13i.ra.input;
// Imports the Google Cloud client library

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import io.p13i.ra.utils.CharacterUtils;
import io.p13i.ra.utils.LoggerUtils;
import io.p13i.ra.utils.StringUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Represents a speech input mechanism
 */
public class SpeechInputMechanism extends AbstractInputMechanism implements ResponseObserver<StreamingRecognizeResponse> {

    private static Logger LOGGER = LoggerUtils.getLogger(SpeechInputMechanism.class);

    /**
     * The responses returned from the API
     */
    private ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

    /**
     * The number of recognition sessions to run
     */
    private int numberOfRunsPerInvokation;

    /**
     * The duration of each recognition seession
     */
    private int durationPerInvokation;

    /**
     * The speech client
     */
    private SpeechClient mClient;


    public SpeechInputMechanism(int numberOfRunsPerInvokation, int durationPerInvokation) {
        this.numberOfRunsPerInvokation = numberOfRunsPerInvokation;
        this.durationPerInvokation = durationPerInvokation;
    }

    @Override
    public void startInputMechanism() {
        try {
            startInputInternal();
        } catch (IOException | LineUnavailableException e) {
            LOGGER.throwing(SpeechInputMechanism.class.getSimpleName(), "recognizeFromMicrophone", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Internal method for performing speech recognition
     *
     * @throws IOException              an error opening a file
     * @throws LineUnavailableException microphone is unavailable
     */
    private void startInputInternal() throws IOException, LineUnavailableException {

        mClient = SpeechClient.create();

        ClientStream<StreamingRecognizeRequest> clientStream = mClient
                .streamingRecognizeCallable()
                .splitCall(this);

        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode("en-US")
                .setSampleRateHertz(16000)
                .build();

        StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig
                .newBuilder()
                .setConfig(recognitionConfig)
                .build();

        StreamingRecognizeRequest request = StreamingRecognizeRequest
                .newBuilder()
                .setStreamingConfig(streamingRecognitionConfig)
                .build(); // The first request in a streaming call has to be a config

        clientStream.send(request);

        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, audioFormat); // Set the system information to read from the microphone audio stream

        if (!AudioSystem.isLineSupported(targetInfo)) {
            throw new LineUnavailableException("Microphone not available");
        }

        // Target data line captures the audio stream the microphone produces.
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        inputMechanismEventsListenerCallback.onInputReady(this);

        for (int i = 0; i < numberOfRunsPerInvokation; i++) {
            long startTime = System.currentTimeMillis();
            // Audio Input Stream
            AudioInputStream audio = new AudioInputStream(targetDataLine);

            while (true) {

                long estimatedTime = System.currentTimeMillis() - startTime;
                byte[] data = new byte[6400];
                int bytesRead = audio.read(data);

                if (estimatedTime > durationPerInvokation * 1000) {
                    targetDataLine.stop();
                    targetDataLine.close();
                    break;
                }
                request = StreamingRecognizeRequest
                        .newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build();
                clientStream.send(request);
            }

            this.onComplete();
        }
    }

    @Override
    public void closeInputMechanism() {
        if (mClient != null) {
            mClient.close();
        }
    }


    @Override
    public void onStart(StreamController controller) {
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        responses.add(response);
    }

    @Override
    public void onComplete() {
        this.responses.stream()
                .map(StreamingRecognizeResponse::getResultsList)
                .flatMap(List::stream)
                .map(StreamingRecognitionResult::getAlternativesList)
                .flatMap(List::stream)
                .map(SpeechRecognitionAlternative::getTranscript)
                .flatMap(StringUtils::toCharStream)
                .map(CharacterUtils::toUpperCase)
                .forEach(c -> inputMechanismEventsListenerCallback.onInput(this, c));
    }

    @Override
    public void onError(Throwable t) {
        LOGGER.throwing(SpeechInputMechanism.class.getSimpleName(), "onError", t);
        throw new RuntimeException(t);
    }

}
