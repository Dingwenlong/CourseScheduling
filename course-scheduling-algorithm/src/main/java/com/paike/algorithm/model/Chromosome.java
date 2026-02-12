package com.paike.algorithm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chromosome implements Serializable, Comparable<Chromosome> {

    private static final long serialVersionUID = 1L;

    private List<Gene> genes;
    private Double fitness;
    private Integer conflictCount;
    private Map<String, Object> metrics;

    public Chromosome() {
        this.genes = new ArrayList<>();
        this.metrics = new HashMap<>();
        this.conflictCount = 0;
        this.fitness = 0.0;
    }

    public Chromosome(List<Gene> genes) {
        this.genes = new ArrayList<>(genes);
        this.metrics = new HashMap<>();
        this.conflictCount = 0;
        this.fitness = 0.0;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public void setGenes(List<Gene> genes) {
        this.genes = genes;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    public Integer getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(Integer conflictCount) {
        this.conflictCount = conflictCount;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public void addGene(Gene gene) {
        this.genes.add(gene);
    }

    public Gene getGene(int index) {
        return genes.get(index);
    }

    public int size() {
        return genes.size();
    }

    @Override
    public int compareTo(Chromosome other) {
        return other.fitness.compareTo(this.fitness);
    }

    @Override
    public Chromosome clone() {
        Chromosome chromosome = new Chromosome();
        for (Gene gene : this.genes) {
            chromosome.addGene(gene.clone());
        }
        chromosome.setFitness(this.fitness);
        chromosome.setConflictCount(this.conflictCount);
        chromosome.setMetrics(new HashMap<>(this.metrics));
        return chromosome;
    }
}
