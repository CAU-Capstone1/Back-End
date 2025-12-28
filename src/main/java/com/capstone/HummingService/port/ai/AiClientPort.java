package com.capstone.HummingService.port.ai;

import java.io.IOException;
public interface AiClientPort {

    byte[] composeWithSongForm(
            String publicJobId,
            String hummingStartPath,
            String hummingMainPath,
            String hummingEndPath,
            String imagePath,
            String style,
            String mood,
            Integer durationSec,
            String keyInfo,
            String tempoInfo,
            String instrument,
            String prompt
    ) throws IOException;
}