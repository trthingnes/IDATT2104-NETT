#include <iostream>
#include <functional>
#include <thread>
#include <vector>
#include <list>
#include <mutex>
#include <condition_variable>

using namespace std;

struct WorkerPool {
    vector<thread> threads;
    list<function<void ()>> tasks;
    mutex tasks_mutex;
    condition_variable tasks_cv;

    int thread_count;
    bool join_called;

    explicit WorkerPool(int threads) {
        thread_count = threads;
        join_called = true;
    }

    /**
     * Create internal threads and run them.
     */
    void start() {
        if(!join_called) {
            // Worker pool is already started. Ignore duplicate call.
            return;
        }

        // Make threads wait if task list empty.
        join_called = false;

        for(int i = 0; i < thread_count; i++) {
            threads.emplace_back([i, this] {
                while(true) {
                    function<void()> task;
                    {
                        // When we make this the mutex gets locked automatically.
                        unique_lock<mutex> lock(tasks_mutex);

                        // Wait in loop for tasks to come in, and release lock.
                        while(tasks.empty()) {
                            cout << to_string(i) + ": No tasks to work on.\n";
                            if(join_called) return;
                            cout << to_string(i) + ": Going to sleep.\n";
                            tasks_cv.wait(lock);
                            cout << to_string(i) + ": Got woken up.\n";
                        }

                        // Pop first task and release lock.
                        task = *tasks.begin();
                        tasks.pop_front();

                        // When we exit, the unique_lock is destroyed and mutex gets released.
                    }
                    if(task) {
                        // Run the task if we retrieved one.
                        cout << to_string(i) + ": Running task.\n";
                        task();
                    }
                }
            });
        }
    }

    /**
     * Add function to run.
     * @param f Function reference
     */
    void post(const function<void()>& f) {
        unique_lock<mutex> lock(tasks_mutex);
        tasks.emplace_back(f);

        // Let a worker thread know new work is available.
        tasks_cv.notify_one();
    }

    /**
     * Add function to run after given amount of time.
     * @param f Function to run after timeout.
     * @param timeout_ms Timeout duration.
     */
    void post_timeout(const function<void()>& f, long timeout_ms) {
        post([&f, timeout_ms] {
            this_thread::sleep_for(chrono::milliseconds(timeout_ms));
            f();
        });
    }

    /**
     * Join internal threads.
     */
    void join() {
        // Make threads return if task list is empty.
        join_called = true;

        // Wake all threads to make sure none wait forever.
        tasks_cv.notify_all();

        // Wait for all threads to finish their work and return.
        for(auto &thread : threads) {
            thread.join();
            cout << "Thread returned.\n";
        }
    }
};

int main() {
    WorkerPool worker_threads(3);

    worker_threads.start(); // Create internal threads

    worker_threads.post([] {
        cout << "Function 1 running.\n";
    });
    worker_threads.post([] {
        cout << "Function 2 running.\n";
    });
    worker_threads.post([] {
        cout << "Function 3 running.\n";
    });
    worker_threads.post_timeout([] {
        cout << "Function 4 running.\n";
    }, 4000);

    cout << "Calling join.\n";
    worker_threads.join(); // Calls join() on the worker threads

    return 0;
}
