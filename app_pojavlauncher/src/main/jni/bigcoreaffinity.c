//
// Created by maks on 19.06.2023.
//

#define _GNU_SOURCE // we are GNU GPLv3

#include <errno.h>
#include <fcntl.h>
#include <linux/limits.h>
#include <sched.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "perf_tuning.h"

#define FREQ_MAX 256

// Overridable at compile time so the scan logic can be unit-tested against fake sysfs trees
#ifndef BIGCORE_CPUFREQ_PATH_FMT
#define BIGCORE_CPUFREQ_PATH_FMT "/sys/devices/system/cpu/cpu%u/cpufreq/cpuinfo_max_freq"
#endif

static void bigcore_format_cpu_path(char* buffer, unsigned int cpu_core) {
    snprintf(buffer, PATH_MAX, BIGCORE_CPUFREQ_PATH_FMT, cpu_core);
}

/**
 * Scans cpu0..cpu(cpu_count-1) and returns the id of the core with the highest maximum
 * frequency (the last one wins on a tie, as before), or -1 if no frequency could be read.
 * Offline cores (missing cpufreq node) and unparsable nodes are skipped, they do not end the scan.
 */
static int bigcore_find(long cpu_count, unsigned long* out_max_freq) {
    char path_buffer[PATH_MAX];
    char freq_buffer[FREQ_MAX];
    unsigned long max_freq = 0;
    int big_core_id = -1;

    for(long cpu = 0; cpu < cpu_count; cpu++) {
        bigcore_format_cpu_path(path_buffer, (unsigned int) cpu);
        int corefreqfd = open(path_buffer, O_RDONLY | O_CLOEXEC);
        if(corefreqfd == -1) continue; // offline or unreadable core, keep scanning

        ssize_t read_count = read(corefreqfd, freq_buffer, FREQ_MAX - 1);
        close(corefreqfd);
        if(read_count <= 0) continue;
        freq_buffer[read_count] = 0;

        unsigned long core_freq = strtoul(freq_buffer, NULL, 10);
        if(core_freq >= max_freq) {
            max_freq = core_freq;
            big_core_id = (int) cpu;
        }
    }
    if(out_max_freq != NULL) *out_max_freq = max_freq;
    return big_core_id;
}

/**
 * Pins the CALLING THREAD to the CPU core that reports the highest maximum frequency.
 *
 * Fixes compared to the previous version:
 *  - offline cores (missing cpufreq node) no longer end the scan early, they are skipped;
 *  - the frequency buffer can no longer be written out of bounds (read() result was used
 *    as an index without checking for -1 / FREQ_MAX);
 *  - sched_setaffinity() gets the size in BYTES (sizeof(cpu_set_t)), not CPU_SETSIZE bits;
 *  - the error message uses errno (sched_setaffinity returns -1, not an errno value).
 */
void bigcore_set_affinity(void) {
    long cpu_count = sysconf(_SC_NPROCESSORS_CONF);
    if(cpu_count < 1) cpu_count = 1;
    if(cpu_count > CPU_SETSIZE) cpu_count = CPU_SETSIZE;

    unsigned long max_freq = 0;
    int big_core_id = bigcore_find(cpu_count, &max_freq);
    if(big_core_id < 0) {
        printf("bigcore: could not read any CPU frequency, affinity left unchanged\n");
        return;
    }

    printf("bigcore: big CPU number is %d, frequency %lu kHz\n", big_core_id, max_freq);
    cpu_set_t bigcore_affinity_set;
    CPU_ZERO(&bigcore_affinity_set);
    CPU_SET(big_core_id, &bigcore_affinity_set);
    if(sched_setaffinity(0, sizeof(cpu_set_t), &bigcore_affinity_set) != 0) {
        printf("bigcore: setting affinity failed: %s\n", strerror(errno));
    } else {
        printf("bigcore: forced current thread onto big core\n");
    }
}
