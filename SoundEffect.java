import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

/**
 * Built-in sound effect generator using Java MIDI.
 * Provides subtle audio feedback for flipping cards, grading, and completing sessions.
 * Requires zero external audio files and handles sound synthesis gracefully.
 */
public final class SoundEffect {
    private static boolean muted = false;
    private static Synthesizer synth;
    private static MidiChannel channel;
    private static boolean initialized = false;

    private SoundEffect() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            if (channels != null && channels.length > 0) {
                channel = channels[0];
                // Use Acoustic Grand Piano or Marimba
                channel.programChange(0);
            }
        } catch (Exception ignored) {
            channel = null;
        }
    }

    public static boolean isMuted() { return muted; }
    public static void setMuted(boolean m) { muted = m; }
    public static void toggleMute() { muted = !muted; }

    public static void playFlip() {
        if (muted) return;
        playNote(76, 50, 70); // subtle woodblock-like tick
    }

    public static void playAgain() {
        if (muted) return;
        playNote(53, 65, 180); // gentle low tone
    }

    public static void playHard() {
        if (muted) return;
        playNote(60, 60, 160); // neutral middle C
    }

    public static void playGood() {
        if (muted) return;
        playNotes(new int[]{64, 71}, 65, 120); // ascending pleasant chord
    }

    public static void playEasy() {
        if (muted) return;
        playNotes(new int[]{64, 67, 72}, 70, 110); // bright major triad
    }

    public static void playCelebration() {
        if (muted) return;
        playNotes(new int[]{60, 64, 67, 72, 76}, 75, 140); // triumphant fanfare
    }

    private static void playNote(int note, int velocity, int durationMs) {
        new Thread(() -> {
            try {
                init();
                if (channel == null) return;
                channel.noteOn(note, velocity);
                Thread.sleep(durationMs);
                channel.noteOff(note);
            } catch (Exception ignored) {}
        }).start();
    }

    private static void playNotes(int[] notes, int velocity, int stepMs) {
        new Thread(() -> {
            try {
                init();
                if (channel == null) return;
                for (int note : notes) {
                    channel.noteOn(note, velocity);
                    Thread.sleep(stepMs);
                }
                Thread.sleep(200);
                for (int note : notes) {
                    channel.noteOff(note);
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
