package com.capstone.HummingService.port.ai;

public interface AiClientPort {
    void compose(String publicJobId, String hummingPath, String mood, String style);

}
