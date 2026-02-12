package com.paike.algorithm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "algorithm.genetic")
public class GeneticAlgorithmConfig {

    private Integer populationSize = 100;
    private Integer maxGenerations = 500;
    private Double crossoverRate = 0.8;
    private Double mutationRate = 0.15;
    private Integer eliteCount = 10;
    private Double targetFitness = 0.95;
    private Integer maxNoImprovementGenerations = 50;

    public Integer getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(Integer populationSize) {
        this.populationSize = populationSize;
    }

    public Integer getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(Integer maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public Double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(Double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public Double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(Double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public Integer getEliteCount() {
        return eliteCount;
    }

    public void setEliteCount(Integer eliteCount) {
        this.eliteCount = eliteCount;
    }

    public Double getTargetFitness() {
        return targetFitness;
    }

    public void setTargetFitness(Double targetFitness) {
        this.targetFitness = targetFitness;
    }

    public Integer getMaxNoImprovementGenerations() {
        return maxNoImprovementGenerations;
    }

    public void setMaxNoImprovementGenerations(Integer maxNoImprovementGenerations) {
        this.maxNoImprovementGenerations = maxNoImprovementGenerations;
    }
}
