package com.paike.algorithm.constraint;

import com.paike.algorithm.model.Gene;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeacherConflictChecker implements ConstraintChecker {

    @Override
    public boolean checkHardConstraints(Gene gene, List<Gene> existingGenes) {
        Long teacherId = getTeacherId(gene);
        if (teacherId == null) {
            return true;
        }

        for (Gene existing : existingGenes) {
            if (gene.getTimeSlot().equals(existing.getTimeSlot())) {
                Long existingTeacherId = getTeacherId(existing);
                if (teacherId.equals(existingTeacherId)) {
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
        return "教师时间冲突检查";
    }

    private Long getTeacherId(Gene gene) {
        Object teacherId = gene.getProperty("teacherId");
        if (teacherId instanceof Long) {
            return (Long) teacherId;
        } else if (teacherId instanceof Integer) {
            return ((Integer) teacherId).longValue();
        }
        return null;
    }
}
