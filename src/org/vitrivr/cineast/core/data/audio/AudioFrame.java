package org.vitrivr.cineast.core.data.audio;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents a single audio-frame containing a specific number of samples (the number depends on the decoder that
 * created the AudioFrame). Sample data is stored in a byte array and internally represented as 16bit int PCM i.e. each sample
 * is represented by a signed 16bit short between -32767 and 32767.
 *
 * The AudioFrame class supports different sample-rates and an arbitrary number of samples and is compatible with the
 * Java Audio API.
 *
 * @author rgasser
 * @version 1.0
 * @created 30.11.16
 */
public class AudioFrame {

    /** Default empty audio frame. Encodes a single, mute sample for one channel. */
    public final static AudioFrame EMPTY_FRAME = new AudioFrame(1,22050, 1, new byte[2], 0.0f);

    /** Number of bits in a sample. */
    public final static int BITS_PER_SAMPLE = 16;

    /** ByteBuffer holding the raw 16bit int data. */
    private final ByteBuffer data;

    /** Incremental index of the AudioFrame usually generated in the decoding context (e.g. i-th frame of the decoded file). */
    private final int id;

    /** Sample rate of this AudioFrame. */
    private final int sampleRate;

    /** Number of channels in this AudioFrame. */
    private final int channels;

    /** Number of samples per channel in this AudioFrame. */
    private final int numberOfSamples;

    /** Start (in seconds) of the audio-frame, relative to the file it is contained in. */
    private final float start;

    /** End (in seconds) of the audio-frame, relative to the file it is contained in. */
    private final float end;

    /**
     * Default constructor.
     *
     * @param id Incremental ID of the new AudioFrame.
     * @param sampleRate Sample-rate of the new AudioFrame.
     * @param channels Number of channels of the new AudioFrame.
     * @param data Byte array containing 16bit signed PCM data.
     */
    public AudioFrame(int id, int sampleRate, int channels, byte[] data, float start) {
        this.id = id;
        this.data = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.numberOfSamples = data.length/(2 * this.channels);
        this.start = start;
        this.end = this.start + (float)this.numberOfSamples/(float)this.sampleRate;
    }

    /**
     * Returns a Java AudioFormat object that specifies the arrangement of the audio-data in the
     * current AudioFrame.
     *
     * @return AudioFormat
     */
    public final AudioFormat getFormat() {
        return new AudioFormat(this.sampleRate, BITS_PER_SAMPLE, this.channels, true, false);
    }

    /**
     * Returns the size of the audio data in bytes.
     *
     * @return
     */
    public final int size() {
        return this.data.array().length;
    }

    /**
     * Returns the total number of samples per channel in this AudioFrame.
     *
     * @return
     */
    public final int numberOfSamples() {
        return this.numberOfSamples;
    }

    /**
     *
     * @return
     */
    public final int getId() {
        return id;
    }

    /**
     * Getter for the raw byte array.
     *
     * @return Byte array containing the audio data of this AudioFrame.
     */
    public final byte[] getData() {
        return data.array();
    }

    /**
     * Getter for sample-rate.
     *
     * @return Sample rate of this AudioFrame.
     */
    public final int getSampleRate() {
        return this.sampleRate;
    }

    /**
     * Returns the duration of the AudioFrame in seconds.
     *
     * @return
     */
    public final float getDuration() {
        return this.end - this.start;
    }

    /**
     * Returns the relative start of the AudioFrame in seconds.
     *
     * @return
     */
    public final float getStart() {
        return this.start;
    }

    /**
     * Returns the relative end of the AudioFrame in seconds.
     *
     * @return
     */
    public final float getEnd() {
        return this.end;
    }

    /**
     * Getter for the number of channels.
     *
     * @return Number of channels in this AudioFrame.
     */
    public final int getChannels() {
        return channels;
    }

    /**
     * Returns the sample specified sample in the specified channel as short value.
     *
     * @param idx Index of the sample (zero-based)
     * @param channel Index of the channel (zero-based)
     * @return Sample value for the specified channel at the specified index.
     */
    public final short getSampleAsShort(int idx, int channel) {
        if (channel < this.channels) {
            return this.data.getShort(2*idx * this.channels + 2*channel);
        } else {
            throw new IllegalArgumentException("The channel indexed must not exceed the number of channels!");
        }
    }

    /**
     * Returns the sample specified sample in the specified channel as double
     * value between -1.0 and 1.0.
     *
     * @param idx Index of the sample (zero-based)
     * @param channel Index of the channel (zero-based)
     * @return Sample value for the specified channel at the specified index.
     */
    public final double getSampleAsDouble(int idx, int channel) {
        return ((double)this.getSampleAsShort(idx,  channel)/(double)Short.MAX_VALUE);
    }

    /**
     * Calculates and returns the mean sample value (across all channels)
     * at the specified sample index and returns it as short value.
     *
     * @param idx Index of the sample (zero-based)
     * @return Mean value of the sample at the specified index.
     */
    public final short getMeanSampleAsShort(int idx) {
        int meanSample = 0;
        for (int i=0;i<this.channels;i++) {
            meanSample += this.getSampleAsShort(idx, i);
        }
        return (short)(meanSample/this.channels);
    }

    /**
     * Calculates and returns the mean sample value (across all channels) at the
     * specified sample index and returns it as double value between -1.0 and 1.0
     *
     * @param idx Index of the sample (zero-based)
     * @return Mean value of the sample at the specified index as float.
     */
    public final double getMeanSampleAsDouble(int idx) {
        float meanSample = 0;
        for (int i=0;i<this.channels;i++) {
            meanSample += this.getSampleAsShort(idx, i);
        }
        return (meanSample/(this.channels * Short.MAX_VALUE));
    }
}