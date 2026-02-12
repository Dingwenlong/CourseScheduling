package com.paike.algorithm.genetic;

import com.paike.algorithm.config.GeneticAlgorithmConfig;
import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import com.paike.algorithm.model.*;
import com.paike.algorithm.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeneticScheduler implements SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(GeneticScheduler.class);

    @Autowired
    private GeneticAlgorithmConfig config;

    private List<TaskData> tasks;
    private List<ClassroomData> classrooms;
    private int daysPerWeek = 5;
    private int slotsPerDay = 10;
    private Random random = new Random();

    @Override
    public SchedulingResult schedule(SchedulingRequest request, List<TaskData> tasks, List<ClassroomData> classrooms) {
        long startTime = System.currentTimeMillis();
        log.info("开始遗传算法排课，任务数量: {}, 教室数量: {}", tasks.size(), classrooms.size());

        this.tasks = tasks;
        this.classrooms = classrooms;
        
        if (request.getDaysPerWeek() != null) {
            this.daysPerWeek = request.getDaysPerWeek();
        }
        if (request.getSlotsPerDay() != null) {
            this.slotsPerDay = request.getSlotsPerDay();
        }

        Population population = initializePopulation();
        population.sort();
        
        int generation = 0;
        int noImprovementCount = 0;
        double bestFitness = population.getBest().getFitness();

        int maxGenerations = config.getMaxGenerations();
        if (request.getMaxGenerations() != null) {
            maxGenerations = request.getMaxGenerations();
        }

        while (generation < maxGenerations && noImprovementCount < config.getMaxNoImprovementGenerations()) {
            population = evolve(population);
            population.sort();

            double currentBestFitness = population.getBest().getFitness();
            if (currentBestFitness > bestFitness + 0.001) {
                bestFitness = currentBestFitness;
                noImprovementCount = 0;
            } else {
                noImprovementCount++;
            }

            generation++;

            if (generation % 50 == 0) {
                log.info("第 {} 代，最优适应度: {}, 平均适应度: {}", 
                        generation, bestFitness, population.getAverageFitness());
            }

            if (config.getTargetFitness() != null && bestFitness >= config.getTargetFitness()) {
                log.info("达到目标适应度 {}，停止进化", config.getTargetFitness());
                break;
            }
        }

        Chromosome bestChromosome = population.getBest();
        List<ScheduledTask> scheduledTasks = convertToScheduledTasks(bestChromosome);

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("遗传算法排课完成，迭代次数: {}, 最优适应度: {}, 冲突数: {}, 耗时: {}ms",
                generation, bestFitness, bestChromosome.getConflictCount(), executionTime);

        SchedulingResult result = SchedulingResult.success(
                scheduledTasks, 
                tasks.size(), 
                scheduledTasks.size() - bestChromosome.getConflictCount(), 
                bestChromosome.getConflictCount()
        );
        result.setGenerations(generation);
        result.setExecutionTime(executionTime);
        result.setSatisfactionScore(java.math.BigDecimal.valueOf(bestFitness));
        return result;
    }

    private Population initializePopulation() {
        Population population = new Population(config.getPopulationSize());
        
        for (int i = 0; i < config.getPopulationSize(); i++) {
            Chromosome chromosome = createRandomChromosome();
            calculateFitness(chromosome);
            population.addChromosome(chromosome);
        }
        
        return population;
    }

    private Chromosome createRandomChromosome() {
        Chromosome chromosome = new Chromosome();
        
        for (TaskData task : tasks) {
            TimeSlot randomSlot = getRandomTimeSlot();
            ClassroomData randomClassroom = getRandomClassroom(task);
            
            if (randomClassroom != null) {
                Gene gene = new Gene(task.getTaskId(), randomSlot, randomClassroom.getClassroomId());
                gene.setProperty("teacherId", task.getTeacherId());
                gene.setProperty("classId", task.getClassId());
                gene.setProperty("courseId", task.getCourseId());
                chromosome.addGene(gene);
            }
        }
        
        return chromosome;
    }

    private TimeSlot getRandomTimeSlot() {
        int day = random.nextInt(daysPerWeek) + 1;
        int slot = random.nextInt(slotsPerDay - 1) + 1;
        return new TimeSlot(day, slot);
    }

    private ClassroomData getRandomClassroom(TaskData task) {
        List<ClassroomData> suitable = classrooms.stream()
                .filter(c -> c.matches(task))
                .collect(Collectors.toList());
        
        if (suitable.isEmpty()) {
            return classrooms.isEmpty() ? null : classrooms.get(random.nextInt(classrooms.size()));
        }
        
        return suitable.get(random.nextInt(suitable.size()));
    }

    private void calculateFitness(Chromosome chromosome) {
        int conflictCount = countConflicts(chromosome);
        chromosome.setConflictCount(conflictCount);
        
        double fitness = 1.0 / (1.0 + conflictCount);
        
        fitness += calculateSoftConstraintScore(chromosome);
        
        chromosome.setFitness(fitness);
    }

    private int countConflicts(Chromosome chromosome) {
        int conflicts = 0;
        List<Gene> genes = chromosome.getGenes();
        
        for (int i = 0; i < genes.size(); i++) {
            for (int j = i + 1; j < genes.size(); j++) {
                Gene g1 = genes.get(i);
                Gene g2 = genes.get(j);
                
                if (!g1.getTimeSlot().equals(g2.getTimeSlot())) {
                    continue;
                }
                
                if (g1.getClassroomId().equals(g2.getClassroomId())) {
                    conflicts++;
                }
                
                Object teacher1 = g1.getProperty("teacherId");
                Object teacher2 = g2.getProperty("teacherId");
                if (teacher1 != null && teacher1.equals(teacher2)) {
                    conflicts++;
                }
                
                Object class1 = g1.getProperty("classId");
                Object class2 = g2.getProperty("classId");
                if (class1 != null && class1.equals(class2)) {
                    conflicts++;
                }
            }
        }
        
        return conflicts;
    }

    private double calculateSoftConstraintScore(Chromosome chromosome) {
        double score = 0;
        
        for (Gene gene : chromosome.getGenes()) {
            TimeSlot slot = gene.getTimeSlot();
            
            if (slot.getSlotNo() >= 1 && slot.getSlotNo() <= 4) {
                score += 0.01;
            }
            
            if (slot.getDayOfWeek() >= 1 && slot.getDayOfWeek() <= 4) {
                score += 0.005;
            }
        }
        
        return score;
    }

    private Population evolve(Population population) {
        Population newPopulation = new Population(config.getPopulationSize());
        
        for (int i = 0; i < config.getEliteCount() && i < population.size(); i++) {
            newPopulation.addChromosome(population.getChromosome(i).clone());
        }
        
        while (newPopulation.size() < config.getPopulationSize()) {
            Chromosome parent1 = select(population);
            Chromosome parent2 = select(population);
            
            Chromosome[] offspring;
            if (random.nextDouble() < config.getCrossoverRate()) {
                offspring = crossover(parent1, parent2);
            } else {
                offspring = new Chromosome[] { parent1.clone(), parent2.clone() };
            }
            
            for (Chromosome child : offspring) {
                if (random.nextDouble() < config.getMutationRate()) {
                    mutate(child);
                }
                calculateFitness(child);
                newPopulation.addChromosome(child);
                
                if (newPopulation.size() >= config.getPopulationSize()) {
                    break;
                }
            }
        }
        
        return newPopulation;
    }

    private Chromosome select(Population population) {
        int tournamentSize = 5;
        Chromosome best = null;
        
        for (int i = 0; i < tournamentSize; i++) {
            int index = random.nextInt(population.size());
            Chromosome candidate = population.getChromosome(index);
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        
        return best;
    }

    private Chromosome[] crossover(Chromosome parent1, Chromosome parent2) {
        int size = parent1.size();
        int crossoverPoint = random.nextInt(size);
        
        Chromosome child1 = new Chromosome();
        Chromosome child2 = new Chromosome();
        
        for (int i = 0; i < size; i++) {
            if (i < crossoverPoint) {
                child1.addGene(parent1.getGene(i).clone());
                child2.addGene(parent2.getGene(i).clone());
            } else {
                child1.addGene(parent2.getGene(i).clone());
                child2.addGene(parent1.getGene(i).clone());
            }
        }
        
        return new Chromosome[] { child1, child2 };
    }

    private void mutate(Chromosome chromosome) {
        int geneIndex = random.nextInt(chromosome.size());
        Gene gene = chromosome.getGene(geneIndex);
        
        int mutationType = random.nextInt(3);
        switch (mutationType) {
            case 0:
                gene.setTimeSlot(getRandomTimeSlot());
                break;
            case 1:
                TaskData task = findTaskById(gene.getTaskId());
                if (task != null) {
                    ClassroomData newClassroom = getRandomClassroom(task);
                    if (newClassroom != null) {
                        gene.setClassroomId(newClassroom.getClassroomId());
                    }
                }
                break;
            case 2:
                gene.setTimeSlot(getRandomTimeSlot());
                TaskData taskData = findTaskById(gene.getTaskId());
                if (taskData != null) {
                    ClassroomData newClassroom = getRandomClassroom(taskData);
                    if (newClassroom != null) {
                        gene.setClassroomId(newClassroom.getClassroomId());
                    }
                }
                break;
        }
    }

    private TaskData findTaskById(Long taskId) {
        return tasks.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    private List<ScheduledTask> convertToScheduledTasks(Chromosome chromosome) {
        List<ScheduledTask> result = new ArrayList<>();
        
        for (Gene gene : chromosome.getGenes()) {
            TaskData task = findTaskById(gene.getTaskId());
            ClassroomData classroom = findClassroomById(gene.getClassroomId());
            
            if (task != null && classroom != null) {
                ScheduledTask scheduledTask = new ScheduledTask();
                scheduledTask.setTaskId(task.getTaskId());
                scheduledTask.setCourseId(task.getCourseId());
                scheduledTask.setCourseName(task.getCourseName());
                scheduledTask.setTeacherId(task.getTeacherId());
                scheduledTask.setTeacherName(task.getTeacherName());
                scheduledTask.setClassId(task.getClassId());
                scheduledTask.setClassName(task.getClassName());
                scheduledTask.setClassroomId(classroom.getClassroomId());
                scheduledTask.setClassroomName(classroom.getRoomName());
                scheduledTask.setTimeSlot(gene.getTimeSlot());
                scheduledTask.setStudentCount(task.getStudentCount());
                scheduledTask.setPriority(task.getPriority());
                result.add(scheduledTask);
            }
        }
        
        return result;
    }

    private ClassroomData findClassroomById(Long classroomId) {
        return classrooms.stream()
                .filter(c -> c.getClassroomId().equals(classroomId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAlgorithmName() {
        return "遗传算法";
    }

    @Override
    public String getAlgorithmDescription() {
        return "基于遗传算法的排课优化，通过选择、交叉、变异操作迭代寻找最优解";
    }
}
