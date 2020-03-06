package com.appskimo.app.ktube.service;

import com.appskimo.app.ktube.Constants;
import com.appskimo.app.ktube.On;
import com.appskimo.app.ktube.domain.YoutubeVideo;

import lombok.Getter;
import lombok.Setter;

public abstract class PlayerBean {
    @Getter protected PlayMode currentMode = PlayMode.STANDARD;
    @Getter @Setter protected Constants.VideoDomain videoDomain;
    @Getter @Setter protected YoutubeVideo currentVideo;

    public void nextMode() {
        currentMode = currentMode.next();
    }

    public abstract void clear();

    public abstract void loadVideo(YoutubeVideo video, On<YoutubeVideo> on);

    public abstract void loadVideo(On<YoutubeVideo> on);

    public abstract void loadNextVideo(boolean force, On<YoutubeVideo> on);

    public abstract void loadPrevVideo(boolean force, On<YoutubeVideo> on);

    public enum PlayMode {
        STANDARD, REPEAT_ALL, REPEAT_ONE, RANDOM;

        PlayMode next() {
            PlayMode mode = STANDARD;
            switch (this) {
                case STANDARD:
                    mode = REPEAT_ALL;
                    break;
                case REPEAT_ALL:
                    mode = REPEAT_ONE;
                    break;
                case REPEAT_ONE:
                    mode = RANDOM;
                    break;
                case RANDOM:
                    mode = STANDARD;
                    break;
            }
            return mode;
        }

        public boolean isStandard() {
            return this == STANDARD;
        }

        public boolean isRepeatOne() {
            return this == REPEAT_ONE;
        }

        public boolean isRandom() {
            return this == RANDOM;
        }

        public boolean isRepeatAll() {
            return this == REPEAT_ALL;
        }
    }
}
