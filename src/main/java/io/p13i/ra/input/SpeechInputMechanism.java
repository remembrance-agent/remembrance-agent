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
import io.p13i.ra.utils.LINQList;
import io.p13i.ra.utils.LoggerUtils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SpeechInputMechanism extends AbstractInputMechanism implements ResponseObserver<StreamingRecognizeResponse> {

    private static Logger LOGGER = LoggerUtils.getLogger(SpeechInputMechanism.class);

    private ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();
    private int numberOfRunsPerInvokation;
    private int durationPerInvokation;

    public SpeechInputMechanism(int numberOfRunsPerInvokation, int durationPerInvokation) {
        this.numberOfRunsPerInvokation = numberOfRunsPerInvokation;
        this.durationPerInvokation = durationPerInvokation;
    }

    @Override
    public void startInput() {
        try {
            startInputInternal();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.throwing(SpeechInputMechanism.class.getSimpleName(), "recognizeFromMicrophone", e);
            throw new RuntimeException(e);
        }
    }

    private void startInputInternal() throws IOException, LineUnavailableException {

        SpeechClient client = SpeechClient.create();

        ClientStream<StreamingRecognizeRequest> clientStream = client
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
            LOGGER.warning("Microphone not supported");
            return;
        }

        // Target data line captures the audio stream the microphone produces.
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        inputEventsListenerCallback.onInputReady(this);

        for (int i = 0; i < numberOfRunsPerInvokation; i++) {
            long startTime = System.currentTimeMillis();
            // Audio Input Stream
            AudioInputStream audio = new AudioInputStream(targetDataLine);

            while (true) {

                long estimatedTime = System.currentTimeMillis() - startTime;
                byte[] data = new byte[6400];
                int bytesRead = audio.read(data);

                if (estimatedTime > durationPerInvokation * 1000) {
                    LOGGER.info("Stop speaking.");
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

    }


    @Override
    public void onStart(StreamController controller) {
    }

    @Override
    public void onResponse(StreamingRecognizeResponse response) {
        LOGGER.info("Got response: " + getTranscript(response));
        responses.add(response);
    }

    @Override
    public void onComplete() {
        for (StreamingRecognizeResponse response : responses) {
            String transcript = getTranscript(response);
            LOGGER.info("Transcript : " + getTranscript(response));

            LINQList.from(transcript)
                    .select(CharacterUtils::toUpperCase)
                    .forEach(inputEventsListenerCallback::onInput);
        }
    }

    private String getTranscript(StreamingRecognizeResponse response) {
        return LINQList.from(response.getResultsList())
                .aggregate(StreamingRecognitionResult::getAlternativesList)
                .select(SpeechRecognitionAlternative::getTranscript)
                .firstOrDefault();
    }

    @Override
    public void onError(Throwable t) {
        LOGGER.throwing(SpeechInputMechanism.class.getSimpleName(), "onError", t);
        throw new RuntimeException(t);
    }

}
