package com.paike.algorithm.constraint;

import com.paike.algorithm.model.Gene;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClassroomConflictChecker implements ConstraintChecker {

    @Override
    public boolean checkHardConstraints(Gene gene, List<Gene> existingGenes) {
        Long classroomId = gene.getClassroomId();
        if (classroomId == null) {
            return true;
        }

        for (Gene existing : existingGenes) {
            if (gene.getTimeSlot().equals(existing.getTimeSlot())) {
                if (classroomId.equals(existing.getClassroomId())) {
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
        return "教室冲突检查";
    }
}
