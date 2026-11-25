#ifndef POJAV_PERF_TUNING_H
#define POJAV_PERF_TUNING_H

/** Pin the calling thread to the CPU core with the highest maximum frequency. */
void bigcore_set_affinity(void);

/**
 * Call from the render thread whenever a window/context becomes current.
 * Applies the "big core" affinity once per thread, if POJAV_BIG_CORE_AFFINITY is set.
 */
void perf_on_context_current(void);

/**
 * Call from the render thread right after every buffer swap.
 * Counts the frame (readable from the launcher process through pojav_environ) and,
 * if POJAV_TARGET_FPS is set, sleeps so that the frame rate does not exceed it.
 */
void perf_on_frame_swapped(void);

#endif //POJAV_PERF_TUNING_H
