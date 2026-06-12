package tech.doshikawa.carerecord.common.util;

import org.springframework.stereotype.Component;

import tech.doshikawa.carerecord.common.config.SnowflakeProperties;

@Component
public class SnowflakeIdGenerator {
    
    private final long nodeId; 
    private final long epoch;
    private final long nodeIdBits;
    private final long sequenceBits;

    private final long TIMESTAMP_SHIFT;
    private final long NODE_ID_SHIFT;
    private final long SEQUENCE_MASK;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdGenerator(SnowflakeProperties props) {
        this.nodeId = props.getNodeId();
        this.epoch = props.getEpoch();
        this.nodeIdBits = props.getNodeIdBits();
        this.sequenceBits = props.getSequenceBits();
        
        this.NODE_ID_SHIFT = this.sequenceBits;
        this.TIMESTAMP_SHIFT = this.sequenceBits + this.nodeIdBits;
        this.SEQUENCE_MASK = ~(-1L << this.sequenceBits);

        long maxNodeId = ~(-1L << this.nodeIdBits);
        if (this.nodeId < 0 || this.nodeId > maxNodeId) {
            throw new IllegalArgumentException(String.format("Node ID must be between 0 and %d", maxNodeId));
        }
    }

    public synchronized long nextId() {
        long currentTimestamp = timeGen();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - currentTimestamp) + " milliseconds");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - epoch) << TIMESTAMP_SHIFT)
                | (nodeId << NODE_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = timeGen();
        }
        return currentTimestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}