package org.cocos2dx.lib;

import android.content.Context;
import android.media.SoundPool;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.HttpStatus;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxSound {
    private static final int INVALID_SOUND_ID = -1;
    private static final int INVALID_STREAM_ID = -1;
    private static int LOAD_TIME_OUT = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    private static final int MAX_SIMULTANEOUS_STREAMS_DEFAULT = 5;
    private static final int MAX_SIMULTANEOUS_STREAMS_I9100 = 3;
    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 5;
    private static final float SOUND_RATE = 1.0f;
    private static final String TAG = "Cocos2dxSound";
    private final Context mContext;
    private float mLeftVolume;
    private float mRightVolume;
    private SoundPool mSoundPool;
    private final HashMap<String, ArrayList<Integer>> mPathStreamIDsMap = new HashMap<>();
    private final HashMap<String, Integer> mPathSoundIDMap = new HashMap<>();
    private ConcurrentHashMap<Integer, SoundInfoForLoadedCompleted> mPlayWhenLoadedEffects = new ConcurrentHashMap<>();

    public Cocos2dxSound(Context context) {
        this.mContext = context;
        initData();
    }

    private void initData() {
        if (Cocos2dxHelper.getDeviceModel().contains("GT-I9100")) {
            this.mSoundPool = new SoundPool(3, 3, 5);
        } else {
            this.mSoundPool = new SoundPool(5, 3, 5);
        }
        this.mSoundPool.setOnLoadCompleteListener(new OnLoadCompletedListener());
        this.mLeftVolume = 0.5f;
        this.mRightVolume = 0.5f;
    }

    public int preloadEffect(String path) {
        Integer soundID = this.mPathSoundIDMap.get(path);
        if (soundID == null) {
            soundID = Integer.valueOf(createSoundIDFromAsset(path));
            if (soundID.intValue() != -1) {
                this.mPathSoundIDMap.put(path, soundID);
            }
        }
        return soundID.intValue();
    }

    public void unloadEffect(String path) {
        ArrayList<Integer> streamIDs = this.mPathStreamIDsMap.get(path);
        if (streamIDs != null) {
            for (Integer steamID : streamIDs) {
                this.mSoundPool.stop(steamID.intValue());
            }
        }
        this.mPathStreamIDsMap.remove(path);
        Integer soundID = this.mPathSoundIDMap.get(path);
        if (soundID != null) {
            this.mSoundPool.unload(soundID.intValue());
            this.mPathSoundIDMap.remove(path);
        }
    }

    public int playEffect(String path, boolean loop, float pitch, float pan, float gain) {
        int streamID;
        Integer soundID = this.mPathSoundIDMap.get(path);
        if (soundID != null) {
            streamID = doPlayEffect(path, soundID.intValue(), loop, pitch, pan, gain);
        } else {
            Integer soundID2 = Integer.valueOf(preloadEffect(path));
            if (soundID2.intValue() == -1) {
                return -1;
            }
            SoundInfoForLoadedCompleted info = new SoundInfoForLoadedCompleted(path, loop, pitch, pan, gain);
            this.mPlayWhenLoadedEffects.putIfAbsent(soundID2, info);
            synchronized (info) {
                try {
                    info.wait(LOAD_TIME_OUT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            streamID = info.effectID;
            this.mPlayWhenLoadedEffects.remove(soundID2);
        }
        return streamID;
    }

    public void stopEffect(int steamID) {
        this.mSoundPool.stop(steamID);
        for (String pPath : this.mPathStreamIDsMap.keySet()) {
            if (this.mPathStreamIDsMap.get(pPath).contains(Integer.valueOf(steamID))) {
                this.mPathStreamIDsMap.get(pPath).remove(this.mPathStreamIDsMap.get(pPath).indexOf(Integer.valueOf(steamID)));
                return;
            }
        }
    }

    public void pauseEffect(int steamID) {
        this.mSoundPool.pause(steamID);
    }

    public void resumeEffect(int steamID) {
        this.mSoundPool.resume(steamID);
    }

    public void pauseAllEffects() {
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Map.Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator<Integer> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    int steamID = it.next().intValue();
                    this.mSoundPool.pause(steamID);
                }
            }
        }
    }

    public void resumeAllEffects() {
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Map.Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator<Integer> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    int steamID = it.next().intValue();
                    this.mSoundPool.resume(steamID);
                }
            }
        }
    }

    public void stopAllEffects() {
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Map.Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator<Integer> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    int steamID = it.next().intValue();
                    this.mSoundPool.stop(steamID);
                }
            }
        }
        this.mPathStreamIDsMap.clear();
    }

    public float getEffectsVolume() {
        return (this.mLeftVolume + this.mRightVolume) / 2.0f;
    }

    public void setEffectsVolume(float volume) {
        if (volume < 0.0f) {
            volume = 0.0f;
        }
        if (volume > SOUND_RATE) {
            volume = SOUND_RATE;
        }
        this.mRightVolume = volume;
        this.mLeftVolume = volume;
        if (!this.mPathStreamIDsMap.isEmpty()) {
            for (Map.Entry<String, ArrayList<Integer>> entry : this.mPathStreamIDsMap.entrySet()) {
                Iterator<Integer> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    int steamID = it.next().intValue();
                    this.mSoundPool.setVolume(steamID, this.mLeftVolume, this.mRightVolume);
                }
            }
        }
    }

    public void end() {
        this.mSoundPool.release();
        this.mPathStreamIDsMap.clear();
        this.mPathSoundIDMap.clear();
        this.mPlayWhenLoadedEffects.clear();
        this.mLeftVolume = 0.5f;
        this.mRightVolume = 0.5f;
        initData();
    }

    public int createSoundIDFromAsset(String path) {
        int soundID;
        try {
            if (path.startsWith("/")) {
                soundID = this.mSoundPool.load(path, 0);
            } else {
                soundID = this.mSoundPool.load(this.mContext.getAssets().openFd(path), 0);
            }
        } catch (Exception e) {
            soundID = -1;
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
        if (soundID == 0) {
            return -1;
        }
        return soundID;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int doPlayEffect(String path, int soundId, boolean loop, float pitch, float pan, float gain) {
        float leftVolume = this.mLeftVolume * gain * (SOUND_RATE - clamp(pan, 0.0f, SOUND_RATE));
        float rightVolume = this.mRightVolume * gain * (SOUND_RATE - clamp(-pan, 0.0f, SOUND_RATE));
        float soundRate = clamp(SOUND_RATE * pitch, 0.5f, 2.0f);
        int streamID = this.mSoundPool.play(soundId, clamp(leftVolume, 0.0f, SOUND_RATE), clamp(rightVolume, 0.0f, SOUND_RATE), 1, loop ? -1 : 0, soundRate);
        ArrayList<Integer> streamIDs = this.mPathStreamIDsMap.get(path);
        if (streamIDs == null) {
            streamIDs = new ArrayList<>();
            this.mPathStreamIDsMap.put(path, streamIDs);
        }
        streamIDs.add(Integer.valueOf(streamID));
        return streamID;
    }

    public void onEnterBackground() {
        this.mSoundPool.autoPause();
    }

    public void onEnterForeground() {
        this.mSoundPool.autoResume();
    }

    public class SoundInfoForLoadedCompleted {
        public int effectID = -1;
        public float gain;
        public boolean isLoop;
        public float pan;
        public String path;
        public float pitch;

        public SoundInfoForLoadedCompleted(String path, boolean isLoop, float pitch, float pan, float gain) {
            this.path = path;
            this.isLoop = isLoop;
            this.pitch = pitch;
            this.pan = pan;
            this.gain = gain;
        }
    }

    public class OnLoadCompletedListener implements SoundPool.OnLoadCompleteListener {
        public OnLoadCompletedListener() {
        }

        @Override // android.media.SoundPool.OnLoadCompleteListener
        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            SoundInfoForLoadedCompleted info;
            if (status == 0 && (info = (SoundInfoForLoadedCompleted) Cocos2dxSound.this.mPlayWhenLoadedEffects.get(Integer.valueOf(sampleId))) != null) {
                info.effectID = Cocos2dxSound.this.doPlayEffect(info.path, sampleId, info.isLoop, info.pitch, info.pan, info.gain);
                synchronized (info) {
                    info.notifyAll();
                }
            }
        }
    }
}
