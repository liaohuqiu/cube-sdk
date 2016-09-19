package in.srain.cube.views.mix;

import android.os.Handler;
import android.os.Looper;

/**
 * A player who can play a @Playable object. It can play next till end and play previous till head.
 * <p/>
 * Once it go to the last element, it can play by the reverse order or jump to the first and play again.
 * <p/>
 * Between each frame, there is a pause, you can call `setTimeInterval()` to set the time interval you want.
 *
 * @author huqiu.lhq
 */
public class AutoPlayer {

    /**
     * Define how an object can be auto-playable.
     */
    public interface Playable {

        public void playTo(int to);

        public void playNext();

        public void playPrevious();

        public int getTotal();

        public int getCurrent();
    }

    public enum PlayDirection {
        to_left, to_right
    }

    public enum PlayRecycleMode {
        repeat_from_start, play_back
    }

    private PlayDirection mDirection = PlayDirection.to_right;
    private PlayRecycleMode mPlayRecycleMode = PlayRecycleMode.repeat_from_start;
    private int mTimeInterval = 5000;
    private Playable mPlayable;

    private Runnable mTimerTask;

    private boolean mSkipNext = false;
    private int mTotal;
    private boolean mPlaying = false;
    private boolean mPaused = false;

    public AutoPlayer(Playable playable) {
        mPlayable = playable;
    }

    public void play() {
        play(0, PlayDirection.to_right);
    }

    public void skipNext() {
        mSkipNext = true;
    }

    public void play(int start, PlayDirection direction) {
        if (mPlaying)
            return;
        mTotal = mPlayable.getTotal();
        if (mTotal <= 1) {
            return;
        }
        mPlaying = true;
        playTo(start);

        final Handler handler = new Handler(Looper.myLooper());
        mTimerTask = new Runnable() {
            @Override
            public void run() {
                if (!mPaused) {
                    playNextFrame();
                }
                if (mPlaying) {
                    handler.postDelayed(mTimerTask, mTimeInterval);
                }
            }
        };
        handler.postDelayed(mTimerTask, mTimeInterval);
    }

    public void play(int start) {
        play(start, PlayDirection.to_right);
    }

    public void stop() {
        if (!mPlaying) {
            return;
        }

        mPlaying = false;
    }

    public AutoPlayer setTimeInterval(int timeInterval) {
        mTimeInterval = timeInterval;
        return this;
    }

    public AutoPlayer setPlayRecycleMode(PlayRecycleMode playRecycleMode) {
        mPlayRecycleMode = playRecycleMode;
        return this;
    }

    private void playNextFrame() {
        if (mSkipNext) {
            mSkipNext = false;
            return;
        }
        int current = mPlayable.getCurrent();
        if (mDirection == PlayDirection.to_right) {
            if (current == mTotal - 1) {
                if (mPlayRecycleMode == PlayRecycleMode.play_back) {
                    mDirection = PlayDirection.to_left;
                    playNextFrame();
                } else {
                    playTo(0);
                }
            } else {
                playNext();
            }
        } else {
            if (current == 0) {
                if (mPlayRecycleMode == PlayRecycleMode.play_back) {
                    mDirection = PlayDirection.to_right;
                    playNextFrame();
                } else {
                    playTo(mTotal - 1);
                }
            } else {
                playPrevious();
            }
        }
    }

    public void pause() {
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
    }

    private void playTo(int to) {
        mPlayable.playTo(to);
    }

    private void playNext() {
        mPlayable.playNext();
    }

    private void playPrevious() {
        mPlayable.playPrevious();
    }
}
