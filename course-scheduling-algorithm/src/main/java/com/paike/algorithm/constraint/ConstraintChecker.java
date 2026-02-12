package com.paike.algorithm.constraint;

import com.paike.algorithm.model.Gene;

import java.util.List;

public interface ConstraintChecker {

    boolean checkHardConstraints(Gene gene, List<Gene> existingGenes);

    double calculateSoftConstraintScore(Gene gene, List<Gene> existingGenes);

    String getConstraintName();
}
