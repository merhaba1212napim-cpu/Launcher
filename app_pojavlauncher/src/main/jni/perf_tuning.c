//
// Frame counter, frame limiter and big-core pinning for the render thread.
//
// NOTE: the launcher (Dalvik) side and the JVM side each load their own copy of this
// library, so plain globals are NOT shared between them. Anything the launcher UI needs
// to read (the frame counter) therefore lives in the shared pojav_environ struct.
//
#define _GNU_SOURCE

#include <errno.h>
#include <jni.h>
#include <pthread.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include <environ/environ.h>
#include "perf_tuning.h"

#define NS_PER_SEC 1000000000LL
#define MIN_TARGET_FPS 10
#define MAX_TARGET_FPS 1000

static pthread_once_t g_perf_once = PTHREAD_ONCE_INIT;
static int64_t g_frame_interval_ns = 0; // 0 = no limit
static bool g_pin_big_core = false;

// Per render thread state. Only the thread that swaps buffers touches these.
static __thread int64_t t_next_frame_ns = 0;
static __thread bool t_affinity_applied = false;

static int perf_env_int(const char* name, int fallback) {
    const char* value = getenv(name);
    if(value == NULL || value[0] == 0) return fallback;
    char* end = NULL;
    long parsed = strtol(value, &end, 10);
    if(end == value) return fallback;
    return (int) parsed;
}

static void perf_init(void) {
    int target_fps = perf_env_int("POJAV_TARGET_FPS", 0);
    if(target_fps >= MIN_TARGET_FPS && target_fps <= MAX_TARGET_FPS) {
        g_frame_interval_ns = NS_PER_SEC / target_fps;
        printf("perf: frame limiter enabled, target %d FPS\n", target_fps);
    } else {
        g_frame_interval_ns = 0;
    }
    g_pin_big_core = getenv("POJAV_BIG_CORE_AFFINITY") != NULL;
}

static int64_t perf_now_ns(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (int64_t) ts.tv_sec * NS_PER_SEC + ts.tv_nsec;
}

static void perf_sleep_until(int64_t deadline_ns) {
    struct timespec ts;
    ts.tv_sec = (time_t) (deadline_ns / NS_PER_SEC);
    ts.tv_nsec = (long) (deadline_ns % NS_PER_SEC);
    int rc;
    do {
        // Returns the error number directly (it does not use errno)
        rc = clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, &ts, NULL);
    } while(rc == EINTR);
}

void perf_on_context_current(void) {
    pthread_once(&g_perf_once, perf_init);
    if(!g_pin_big_core || t_affinity_applied) return;
    t_affinity_applied = true;
    bigcore_set_affinity();
}

void perf_on_frame_swapped(void) {
    pthread_once(&g_perf_once, perf_init);
    atomic_fetch_add_explicit(&pojav_environ->frameCounter, 1, memory_order_relaxed);

    // The adaptive override (battery/thermal, see AdaptiveFpsController.java) can only make the
    // cap STRICTER than the user's own choice, never looser: we keep whichever interval is larger.
    int64_t interval = g_frame_interval_ns;
    int override_fps = atomic_load_explicit(&pojav_environ->overrideTargetFps, memory_order_relaxed);
    if(override_fps >= MIN_TARGET_FPS && override_fps <= MAX_TARGET_FPS) {
        int64_t override_interval = NS_PER_SEC / override_fps;
        if(interval <= 0 || override_interval > interval) interval = override_interval;
    }
    if(interval <= 0) return;

    int64_t now = perf_now_ns();
    if(t_next_frame_ns == 0 || now >= t_next_frame_ns + interval) {
        // First frame, or we fell behind by more than a whole frame: restart the schedule
        // instead of trying to catch up with a burst of unlimited frames.
        t_next_frame_ns = now + interval;
        return;
    }
    if(now < t_next_frame_ns) perf_sleep_until(t_next_frame_ns);
    t_next_frame_ns += interval;
}

/** Launcher-side getter: how many frames the game has swapped since it started. */
JNIEXPORT jlong JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_getFrameCount(JNIEnv *env, jclass clazz) {
    (void) env;
    (void) clazz;
    return (jlong) atomic_load_explicit(&pojav_environ->frameCounter, memory_order_relaxed);
}

/**
 * Launcher-side setter (called from AdaptiveFpsController.java): fps<=0 clears the adaptive
 * override and returns to the user's own cap (or unlimited); fps>0 asks for at most that many
 * frames per second. It never loosens a stricter cap the user already picked (see above).
 */
JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_utils_JREUtils_setAdaptiveFpsCap(JNIEnv *env, jclass clazz, jint fps) {
    (void) env;
    (void) clazz;
    atomic_store_explicit(&pojav_environ->overrideTargetFps, (int) fps, memory_order_relaxed);
}
