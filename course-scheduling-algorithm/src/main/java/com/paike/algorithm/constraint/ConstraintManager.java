package com.paike.algorithm.constraint;

import com.paike.algorithm.model.Gene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConstraintManager {

    private final List<ConstraintChecker> checkers = new ArrayList<>();

    @Autowired
    public ConstraintManager(List<ConstraintChecker> checkerList) {
        this.checkers.addAll(checkerList);
    }

    public boolean checkAllHardConstraints(Gene gene, List<Gene> existingGenes) {
        for (ConstraintChecker checker : checkers) {
            if (!checker.checkHardConstraints(gene, existingGenes)) {
                return false;
            }
        }
        return true;
    }

    public double calculateTotalSoftConstraintScore(Gene gene, List<Gene> existingGenes) {
        double totalScore = 0;
        for (ConstraintChecker checker : checkers) {
            totalScore += checker.calculateSoftConstraintScore(gene, existingGenes);
        }
        return totalScore;
    }

    public int countViolations(Gene gene, List<Gene> existingGenes) {
        int violations = 0;
        for (ConstraintChecker checker : checkers) {
            if (!checker.checkHardConstraints(gene, existingGenes)) {
                violations++;
            }
        }
        return violations;
    }

    public List<String> getViolationDetails(Gene gene, List<Gene> existingGenes) {
        List<String> details = new ArrayList<>();
        for (ConstraintChecker checker : checkers) {
            if (!checker.checkHardConstraints(gene, existingGenes)) {
                details.add(checker.getConstraintName());
            }
        }
        return details;
    }

    public void registerChecker(ConstraintChecker checker) {
        this.checkers.add(checker);
    }

    public void removeChecker(ConstraintChecker checker) {
        this.checkers.remove(checker);
    }

    public List<ConstraintChecker> getCheckers() {
        return new ArrayList<>(checkers);
    }
}
