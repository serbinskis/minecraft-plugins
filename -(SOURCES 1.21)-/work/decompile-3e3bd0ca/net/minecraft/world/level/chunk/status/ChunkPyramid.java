package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public record ChunkPyramid(ImmutableList<ChunkStep> steps) {

    public static final ChunkPyramid GENERATION_PYRAMID = (new ChunkPyramid.a()).step(ChunkStatus.EMPTY, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.STRUCTURE_STARTS, (chunkstep_a) -> {
        return chunkstep_a.setTask(ChunkStatusTasks::generateStructureStarts);
    }).step(ChunkStatus.STRUCTURE_REFERENCES, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).setTask(ChunkStatusTasks::generateStructureReferences);
    }).step(ChunkStatus.BIOMES, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).setTask(ChunkStatusTasks::generateBiomes);
    }).step(ChunkStatus.NOISE, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).addRequirement(ChunkStatus.BIOMES, 1).blockStateWriteRadius(0).setTask(ChunkStatusTasks::generateNoise);
    }).step(ChunkStatus.SURFACE, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).addRequirement(ChunkStatus.BIOMES, 1).blockStateWriteRadius(0).setTask(ChunkStatusTasks::generateSurface);
    }).step(ChunkStatus.CARVERS, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).blockStateWriteRadius(0).setTask(ChunkStatusTasks::generateCarvers);
    }).step(ChunkStatus.FEATURES, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).addRequirement(ChunkStatus.CARVERS, 1).blockStateWriteRadius(1).setTask(ChunkStatusTasks::generateFeatures);
    }).step(ChunkStatus.INITIALIZE_LIGHT, (chunkstep_a) -> {
        return chunkstep_a.setTask(ChunkStatusTasks::initializeLight);
    }).step(ChunkStatus.LIGHT, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.INITIALIZE_LIGHT, 1).setTask(ChunkStatusTasks::light);
    }).step(ChunkStatus.SPAWN, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.BIOMES, 1).setTask(ChunkStatusTasks::generateSpawn);
    }).step(ChunkStatus.FULL, (chunkstep_a) -> {
        return chunkstep_a.setTask(ChunkStatusTasks::full);
    }).build();
    public static final ChunkPyramid LOADING_PYRAMID = (new ChunkPyramid.a()).step(ChunkStatus.EMPTY, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.STRUCTURE_STARTS, (chunkstep_a) -> {
        return chunkstep_a.setTask(ChunkStatusTasks::loadStructureStarts);
    }).step(ChunkStatus.STRUCTURE_REFERENCES, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.BIOMES, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.NOISE, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.SURFACE, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.CARVERS, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.FEATURES, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.INITIALIZE_LIGHT, (chunkstep_a) -> {
        return chunkstep_a.setTask(ChunkStatusTasks::initializeLight);
    }).step(ChunkStatus.LIGHT, (chunkstep_a) -> {
        return chunkstep_a.addRequirement(ChunkStatus.INITIALIZE_LIGHT, 1).setTask(ChunkStatusTasks::light);
    }).step(ChunkStatus.SPAWN, (chunkstep_a) -> {
        return chunkstep_a;
    }).step(ChunkStatus.FULL, (chunkstep_a) -> {
        return chunkstep_a.setTask(ChunkStatusTasks::full);
    }).build();

    public ChunkStep getStepTo(ChunkStatus chunkstatus) {
        return (ChunkStep) this.steps.get(chunkstatus.getIndex());
    }

    public static class a {

        private final List<ChunkStep> steps = new ArrayList();

        public a() {}

        public ChunkPyramid build() {
            return new ChunkPyramid(ImmutableList.copyOf(this.steps));
        }

        public ChunkPyramid.a step(ChunkStatus chunkstatus, UnaryOperator<ChunkStep.a> unaryoperator) {
            ChunkStep.a chunkstep_a;

            if (this.steps.isEmpty()) {
                chunkstep_a = new ChunkStep.a(chunkstatus);
            } else {
                chunkstep_a = new ChunkStep.a(chunkstatus, (ChunkStep) this.steps.getLast());
            }

            this.steps.add(((ChunkStep.a) unaryoperator.apply(chunkstep_a)).build());
            return this;
        }
    }
}
