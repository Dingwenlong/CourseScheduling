package com.paike.algorithm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Population implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Chromosome> chromosomes;
    private Integer size;
    private Chromosome bestChromosome;
    private Integer generation;

    public Population(int size) {
        this.size = size;
        this.chromosomes = new ArrayList<>();
        this.generation = 0;
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void setChromosomes(List<Chromosome> chromosomes) {
        this.chromosomes = chromosomes;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Chromosome getBestChromosome() {
        return bestChromosome;
    }

    public void setBestChromosome(Chromosome bestChromosome) {
        this.bestChromosome = bestChromosome;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    public Chromosome getChromosome(int index) {
        return chromosomes.get(index);
    }

    public int size() {
        return chromosomes.size();
    }

    public void sort() {
        chromosomes.sort(Comparator.comparingDouble(Chromosome::getFitness).reversed());
        if (!chromosomes.isEmpty()) {
            bestChromosome = chromosomes.get(0);
        }
    }

    public Chromosome getBest() {
        if (bestChromosome == null && !chromosomes.isEmpty()) {
            sort();
        }
        return bestChromosome;
    }

    public double getAverageFitness() {
        if (chromosomes.isEmpty()) {
            return 0.0;
        }
        return chromosomes.stream()
                .mapToDouble(Chromosome::getFitness)
                .average()
                .orElse(0.0);
    }
}
