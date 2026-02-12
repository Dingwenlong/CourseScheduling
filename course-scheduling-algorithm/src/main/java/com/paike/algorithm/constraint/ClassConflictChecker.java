package com.paike.algorithm.constraint;

import com.paike.algorithm.model.Gene;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClassConflictChecker implements ConstraintChecker {

    @Override
    public boolean checkHardConstraints(Gene gene, List<Gene> existingGenes) {
        Long classId = getClassId(gene);
        if (classId == null) {
            return true;
        }

        for (Gene existing : existingGenes) {
            if (gene.getTimeSlot().equals(existing.getTimeSlot())) {
                Long existingClassId = getClassId(existing);
                if (classId.equals(existingClassId)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public double calculateSoftConstraintScore(Gene gene, List<Gene> existingGenes) {
        return 0;
    }

    @Override
    public String getConstraintName() {
        return "班级时间冲突检查";
    }

    private Long getClassId(Gene gene) {
        Object classId = gene.getProperty("classId");
        if (classId instanceof Long) {
            return (Long) classId;
        } else if (classId instanceof Integer) {
            return ((Integer) classId).longValue();
        }
        return null;
    }
}
