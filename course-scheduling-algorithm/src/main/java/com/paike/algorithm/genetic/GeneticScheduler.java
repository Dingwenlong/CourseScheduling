package com.paike.algorithm.genetic;

import com.paike.algorithm.config.GeneticAlgorithmConfig;
import com.paike.algorithm.dto.ClassroomData;
import com.paike.algorithm.dto.SchedulingRequest;
import com.paike.algorithm.dto.SchedulingResult;
import com.paike.algorithm.dto.TaskData;
import com.paike.algorithm.model.Chromosome;
import com.paike.algorithm.model.Gene;
import com.paike.algorithm.model.Population;
import com.paike.algorithm.model.ScheduledTask;
import com.paike.algorithm.model.TimeSlot;
import com.paike.algorithm.service.SchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GeneticScheduler implements SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(GeneticScheduler.class);
    private static final int SESSION_SLOT_SPAN = 2;

    @Autowired
    private GeneticAlgorithmConfig config;

    @Override
    public SchedulingResult schedule(SchedulingRequest request, List<TaskData> tasks, List<ClassroomData> classrooms) {
        long startTime = System.currentTimeMillis();
        log.info("开始遗传算法排课，任务数量: {}, 教室数量: {}", tasks.size(), classrooms.size());

        ExecutionContext context = buildContext(request, tasks, classrooms);
        Random random = new Random();

        Population population = initializePopulation(context, random);
        population.sort();

        int generation = 0;
        int noImprovementCount = 0;
        double bestFitness = population.getBest().getFitness();

        int maxGenerations = request.getMaxGenerations() != null
                ? request.getMaxGenerations()
                : config.getMaxGenerations();
        Double targetFitness = request.getTargetFitness() != null
                ? request.getTargetFitness()
                : config.getTargetFitness();

        while (generation < maxGenerations && noImprovementCount < config.getMaxNoImprovementGenerations()) {
            population = evolve(population, context, random);
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

            if (targetFitness != null && bestFitness >= targetFitness) {
                log.info("达到目标适应度 {}，停止进化", targetFitness);
                break;
            }
        }

        Chromosome bestChromosome = population.getBest();
        List<ScheduledTask> scheduledTasks = convertToScheduledTasks(bestChromosome, context);
        int scheduledTaskCount = countFullyScheduledTasks(bestChromosome, context);

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("遗传算法排课完成，迭代次数: {}, 最优适应度: {}, 冲突数: {}, 耗时: {}ms",
                generation, bestFitness, bestChromosome.getConflictCount(), executionTime);

        SchedulingResult result = SchedulingResult.success(
                scheduledTasks,
                context.tasks.size(),
                scheduledTaskCount,
                bestChromosome.getConflictCount()
        );
        result.setGenerations(generation);
        result.setExecutionTime(executionTime);
        result.setSatisfactionScore(java.math.BigDecimal.valueOf(bestFitness));
        return result;
    }

    private Population initializePopulation(ExecutionContext context, Random random) {
        Population population = new Population(config.getPopulationSize());
        for (int i = 0; i < config.getPopulationSize(); i++) {
            Chromosome chromosome = createRandomChromosome(context, random);
            calculateFitness(chromosome, context);
            population.addChromosome(chromosome);
        }
        return population;
    }

    private Chromosome createRandomChromosome(ExecutionContext context, Random random) {
        Chromosome chromosome = new Chromosome();

        for (TaskData task : context.tasks) {
            int sessions = context.sessionCountByTaskId.getOrDefault(task.getTaskId(), 1);
            Set<TimeSlot> selectedOccupiedSlots = new LinkedHashSet<>();
            Set<Integer> selectedDays = new LinkedHashSet<>();

            for (int sessionIndex = 0; sessionIndex < sessions; sessionIndex++) {
                TimeSlot randomSlot = getRandomTimeSlot(context, random, selectedOccupiedSlots, selectedDays);
                ClassroomData randomClassroom = getRandomClassroom(context, task, random);
                if (randomClassroom == null) {
                    continue;
                }

                Gene gene = new Gene(task.getTaskId(), randomSlot, randomClassroom.getClassroomId());
                gene.setProperty("teacherId", task.getTeacherId());
                gene.setProperty("classId", task.getClassId());
                gene.setProperty("courseId", task.getCourseId());
                gene.setProperty("sessionIndex", sessionIndex);
                chromosome.addGene(gene);

                selectedOccupiedSlots.addAll(getOccupiedSlots(randomSlot));
                selectedDays.add(randomSlot.getDayOfWeek());
            }
        }

        return chromosome;
    }

    private TimeSlot getRandomTimeSlot(ExecutionContext context, Random random) {
        int day = random.nextInt(context.daysPerWeek) + 1;
        int slot = random.nextInt(Math.max(1, context.slotsPerDay - SESSION_SLOT_SPAN + 1)) + 1;
        return new TimeSlot(day, slot);
    }

    private TimeSlot getRandomTimeSlot(ExecutionContext context,
                                       Random random,
                                       Set<TimeSlot> selectedOccupiedSlots,
                                       Set<Integer> selectedDays) {
        TimeSlot fallback = getRandomTimeSlot(context, random);
        int attempts = Math.max(10, context.daysPerWeek * context.slotsPerDay);
        for (int attempt = 0; attempt < attempts; attempt++) {
            TimeSlot candidate = getRandomTimeSlot(context, random);
            if (hasOccupiedSlotConflict(candidate, selectedOccupiedSlots)) {
                continue;
            }
            fallback = candidate;
            if (selectedDays.size() < context.daysPerWeek && selectedDays.contains(candidate.getDayOfWeek())) {
                continue;
            }
            return candidate;
        }
        return fallback;
    }

    private ClassroomData getRandomClassroom(ExecutionContext context, TaskData task, Random random) {
        List<ClassroomData> suitable = context.classrooms.stream()
                .filter(classroom -> classroom.matches(task))
                .collect(Collectors.toList());
        if (suitable.isEmpty()) {
            return context.classrooms.isEmpty() ? null : context.classrooms.get(random.nextInt(context.classrooms.size()));
        }
        return suitable.get(random.nextInt(suitable.size()));
    }

    private void calculateFitness(Chromosome chromosome, ExecutionContext context) {
        int conflictCount = countConflicts(chromosome);
        int incompleteTaskCount = countIncompleteTasks(chromosome, context);
        chromosome.setConflictCount(conflictCount);

        double fitness = 1.0 / (1.0 + conflictCount + incompleteTaskCount * 2.0);
        fitness += calculateSoftConstraintScore(chromosome, context);
        chromosome.setFitness(fitness);
        chromosome.getMetrics().put("incompleteTasks", incompleteTaskCount);
    }

    private int countConflicts(Chromosome chromosome) {
        int conflicts = 0;
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            for (int j = i + 1; j < genes.size(); j++) {
                Gene left = genes.get(i);
                Gene right = genes.get(j);

                if (!isOverlapping(left.getTimeSlot(), right.getTimeSlot())) {
                    continue;
                }
                if (left.getClassroomId().equals(right.getClassroomId())) {
                    conflicts++;
                }

                Object teacher1 = left.getProperty("teacherId");
                Object teacher2 = right.getProperty("teacherId");
                if (teacher1 != null && teacher1.equals(teacher2)) {
                    conflicts++;
                }

                Object class1 = left.getProperty("classId");
                Object class2 = right.getProperty("classId");
                if (class1 != null && class1.equals(class2)) {
                    conflicts++;
                }
            }
        }

        return conflicts;
    }

    private int countIncompleteTasks(Chromosome chromosome, ExecutionContext context) {
        Map<Long, Integer> actualSessions = countSessionsByTask(chromosome);
        int incompleteTasks = 0;
        for (Map.Entry<Long, Integer> entry : context.sessionCountByTaskId.entrySet()) {
            int actual = actualSessions.getOrDefault(entry.getKey(), 0);
            if (actual < entry.getValue()) {
                incompleteTasks++;
            }
        }
        return incompleteTasks;
    }

    private double calculateSoftConstraintScore(Chromosome chromosome, ExecutionContext context) {
        double score = 0;
        Map<Long, Set<Integer>> taskDays = new HashMap<>();
        Map<Long, Integer> taskSessionCount = new HashMap<>();

        for (Gene gene : chromosome.getGenes()) {
            TimeSlot slot = gene.getTimeSlot();
            if (slot.getSlotNo() >= 1 && slot.getSlotNo() <= 4) {
                score += 0.01;
            }
            if (slot.getDayOfWeek() >= 1 && slot.getDayOfWeek() <= 4) {
                score += 0.005;
            }

            taskDays.computeIfAbsent(gene.getTaskId(), key -> new LinkedHashSet<>()).add(slot.getDayOfWeek());
            taskSessionCount.merge(gene.getTaskId(), 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : taskSessionCount.entrySet()) {
            Long taskId = entry.getKey();
            int totalSessions = entry.getValue();
            int uniqueDays = taskDays.getOrDefault(taskId, Collections.emptySet()).size();
            if (totalSessions > 1) {
                score += uniqueDays * 0.02;
                score -= (totalSessions - uniqueDays) * 0.015;
            }
        }

        int completeTasks = countFullyScheduledTasks(chromosome, context);
        if (!context.tasks.isEmpty()) {
            score += (double) completeTasks / context.tasks.size() * 0.05;
        }

        return score;
    }

    private List<TimeSlot> getOccupiedSlots(TimeSlot startSlot) {
        List<TimeSlot> occupiedSlots = new ArrayList<>(SESSION_SLOT_SPAN);
        for (int offset = 0; offset < SESSION_SLOT_SPAN; offset++) {
            occupiedSlots.add(new TimeSlot(startSlot.getDayOfWeek(), startSlot.getSlotNo() + offset));
        }
        return occupiedSlots;
    }

    private boolean hasOccupiedSlotConflict(TimeSlot candidate, Set<TimeSlot> selectedOccupiedSlots) {
        for (TimeSlot occupiedSlot : getOccupiedSlots(candidate)) {
            if (selectedOccupiedSlots.contains(occupiedSlot)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlapping(TimeSlot left, TimeSlot right) {
        if (!Objects.equals(left.getDayOfWeek(), right.getDayOfWeek())) {
            return false;
        }
        int leftEnd = left.getSlotNo() + SESSION_SLOT_SPAN - 1;
        int rightEnd = right.getSlotNo() + SESSION_SLOT_SPAN - 1;
        return left.getSlotNo() <= rightEnd && right.getSlotNo() <= leftEnd;
    }

    private Population evolve(Population population, ExecutionContext context, Random random) {
        Population newPopulation = new Population(config.getPopulationSize());

        for (int i = 0; i < config.getEliteCount() && i < population.size(); i++) {
            newPopulation.addChromosome(population.getChromosome(i).clone());
        }

        while (newPopulation.size() < config.getPopulationSize()) {
            Chromosome parent1 = select(population, random);
            Chromosome parent2 = select(population, random);

            Chromosome[] offspring;
            if (random.nextDouble() < config.getCrossoverRate()) {
                offspring = crossover(parent1, parent2, random);
            } else {
                offspring = new Chromosome[]{parent1.clone(), parent2.clone()};
            }

            for (Chromosome child : offspring) {
                if (random.nextDouble() < config.getMutationRate()) {
                    mutate(child, context, random);
                }
                calculateFitness(child, context);
                newPopulation.addChromosome(child);
                if (newPopulation.size() >= config.getPopulationSize()) {
                    break;
                }
            }
        }

        return newPopulation;
    }

    private Chromosome select(Population population, Random random) {
        int tournamentSize = 5;
        Chromosome best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Chromosome candidate = population.getChromosome(random.nextInt(population.size()));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }

    private Chromosome[] crossover(Chromosome parent1, Chromosome parent2, Random random) {
        int size = parent1.size();
        if (size == 0) {
            return new Chromosome[]{parent1.clone(), parent2.clone()};
        }

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

        return new Chromosome[]{child1, child2};
    }

    private void mutate(Chromosome chromosome, ExecutionContext context, Random random) {
        if (chromosome.size() == 0) {
            return;
        }

        Gene gene = chromosome.getGene(random.nextInt(chromosome.size()));
        int mutationType = random.nextInt(3);
        switch (mutationType) {
            case 0:
                gene.setTimeSlot(getRandomTimeSlot(context, random));
                break;
            case 1:
                TaskData task = findTaskById(context, gene.getTaskId());
                if (task != null) {
                    ClassroomData newClassroom = getRandomClassroom(context, task, random);
                    if (newClassroom != null) {
                        gene.setClassroomId(newClassroom.getClassroomId());
                    }
                }
                break;
            case 2:
                gene.setTimeSlot(getRandomTimeSlot(context, random));
                TaskData taskData = findTaskById(context, gene.getTaskId());
                if (taskData != null) {
                    ClassroomData newClassroom = getRandomClassroom(context, taskData, random);
                    if (newClassroom != null) {
                        gene.setClassroomId(newClassroom.getClassroomId());
                    }
                }
                break;
            default:
                break;
        }
    }

    private TaskData findTaskById(ExecutionContext context, Long taskId) {
        return context.tasksById.get(taskId);
    }

    private List<ScheduledTask> convertToScheduledTasks(Chromosome chromosome, ExecutionContext context) {
        List<ScheduledTask> result = new ArrayList<>();
        for (Gene gene : chromosome.getGenes()) {
            TaskData task = findTaskById(context, gene.getTaskId());
            ClassroomData classroom = context.classroomsById.get(gene.getClassroomId());
            if (task == null || classroom == null) {
                continue;
            }

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
            scheduledTask.setWeeks(task.getWeeks());
            scheduledTask.setStudentCount(task.getStudentCount());
            scheduledTask.setPriority(task.getPriority());
            result.add(scheduledTask);
        }

        result.sort(Comparator.comparing((ScheduledTask item) -> item.getTimeSlot().getDayOfWeek())
                .thenComparing(item -> item.getTimeSlot().getSlotNo())
                .thenComparing(ScheduledTask::getTaskId));
        return result;
    }

    private int countFullyScheduledTasks(Chromosome chromosome, ExecutionContext context) {
        Map<Long, Integer> actualSessions = countSessionsByTask(chromosome);
        int completeTasks = 0;
        for (Map.Entry<Long, Integer> entry : context.sessionCountByTaskId.entrySet()) {
            int actual = actualSessions.getOrDefault(entry.getKey(), 0);
            if (actual >= entry.getValue()) {
                completeTasks++;
            }
        }
        return completeTasks;
    }

    private Map<Long, Integer> countSessionsByTask(Chromosome chromosome) {
        Map<Long, Integer> sessionCount = new HashMap<>();
        for (Gene gene : chromosome.getGenes()) {
            sessionCount.merge(gene.getTaskId(), 1, Integer::sum);
        }
        return sessionCount;
    }

    private ExecutionContext buildContext(SchedulingRequest request, List<TaskData> tasks, List<ClassroomData> classrooms) {
        int daysPerWeek = request.getDaysPerWeek() != null ? request.getDaysPerWeek() : 5;
        int slotsPerDay = request.getSlotsPerDay() != null ? request.getSlotsPerDay() : 10;
        Map<Long, TaskData> tasksById = tasks.stream()
                .collect(Collectors.toMap(TaskData::getTaskId, task -> task, (left, right) -> left, LinkedHashMap::new));
        Map<Long, Integer> sessionCountByTaskId = tasks.stream()
                .collect(Collectors.toMap(TaskData::getTaskId, this::calculateSessions, (left, right) -> left, LinkedHashMap::new));
        Map<Long, ClassroomData> classroomsById = classrooms.stream()
                .collect(Collectors.toMap(ClassroomData::getClassroomId, classroom -> classroom, (left, right) -> left, LinkedHashMap::new));
        return new ExecutionContext(tasks, tasksById, sessionCountByTaskId, classrooms, classroomsById, daysPerWeek, slotsPerDay);
    }

    private int calculateSessions(TaskData task) {
        int weeklyHours = task.getWeeklyHours() != null ? task.getWeeklyHours() : 2;
        return Math.max(1, (weeklyHours + 1) / 2);
    }

    private static class ExecutionContext {
        private final List<TaskData> tasks;
        private final Map<Long, TaskData> tasksById;
        private final Map<Long, Integer> sessionCountByTaskId;
        private final List<ClassroomData> classrooms;
        private final Map<Long, ClassroomData> classroomsById;
        private final int daysPerWeek;
        private final int slotsPerDay;

        private ExecutionContext(List<TaskData> tasks,
                                 Map<Long, TaskData> tasksById,
                                 Map<Long, Integer> sessionCountByTaskId,
                                 List<ClassroomData> classrooms,
                                 Map<Long, ClassroomData> classroomsById,
                                 int daysPerWeek,
                                 int slotsPerDay) {
            this.tasks = tasks;
            this.tasksById = tasksById;
            this.sessionCountByTaskId = sessionCountByTaskId;
            this.classrooms = classrooms;
            this.classroomsById = classroomsById;
            this.daysPerWeek = daysPerWeek;
            this.slotsPerDay = slotsPerDay;
        }
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
