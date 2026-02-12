package com.paike.algorithm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "algorithm.greedy")
public class GreedyAlgorithmConfig {

    private Boolean enableLocalBacktrack = true;
    private Integer maxBacktrackDepth = 3;
    private Integer maxRetryAttempts = 10;

    public Boolean getEnableLocalBacktrack() {
        return enableLocalBacktrack;
    }

    public void setEnableLocalBacktrack(Boolean enableLocalBacktrack) {
        this.enableLocalBacktrack = enableLocalBacktrack;
    }

    public Integer getMaxBacktrackDepth() {
        return maxBacktrackDepth;
    }

    public void setMaxBacktrackDepth(Integer maxBacktrackDepth) {
        this.maxBacktrackDepth = maxBacktrackDepth;
    }

    public Integer getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(Integer maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }
}
