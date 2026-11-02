package org.openrow.probe;

/** Read-only workout contract. Games cannot change targets, clocks or recovery through this API. */
public interface GameWorkoutAdapter {
    void workoutChanged(Frame frame);
    final class Frame {
        public final String sessionId, guidance;
        public final TrainingCore.Phase phase;
        public final TrainingCore.Category category;
        public final double visualReferenceSpeed;
        public final int visualReferenceSamples;
        public final double elapsedSeconds,remainingBlockSeconds;
        public final int effort;
        public final boolean paused,finished,telemetryAvailable;
        public final double distanceMeters,rowingSeconds,missingTelemetrySeconds;
        public boolean campaignWorkEligible(boolean moving){return moving&&phase==TrainingCore.Phase.WORK&&telemetryAvailable&&!paused&&!finished;}
        public boolean campaignBossWindow(boolean moving){return campaignWorkEligible(moving)&&remainingBlockSeconds>=60;}
        public Frame(TrainingCore.Runner runner){this(runner,TrainingVisuals.empty());}
        public Frame(TrainingCore.Runner runner,TrainingVisuals.Profile profile){sessionId=runner.id;phase=runner.current().phase;
            guidance=TrainingCore.guidanceFor(runner.effort());elapsedSeconds=runner.elapsed;
            remainingBlockSeconds=runner.finished?0:runner.current().seconds-runner.within;
            effort=runner.effort();category=runner.current().category;visualReferenceSpeed=profile.speed(category,effort);visualReferenceSamples=profile.samples(category,effort);paused=runner.paused;finished=runner.finished;
            telemetryAvailable=runner.telemetryAvailable&&!runner.paused&&!runner.finished;
            distanceMeters=runner.distance;rowingSeconds=runner.rowingSeconds;missingTelemetrySeconds=runner.missingSeconds;}
    }
}
