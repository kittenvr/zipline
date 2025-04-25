package com.claude.config;

public class ModConfig {
    public boolean useTeleportation = true;
    public double maxDistancePerPacket = 10.0;
    public int packetDelayMs = 50;
    public int chunkLoadPollIntervalMs = 200;
    public int maxWaitForChunkLoadMs = 5000;
    public int maxFailedTeleportAttempts = 5;
    public boolean fallbackToNormal = true;
    public int resumeTeleportAfterBlocksMs = 1000;
}
