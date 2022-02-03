#include <iostream>
#include <thread>
#include <vector>
#include <array>
#include <string>

#define THREADS 5
#define MIN 0
#define MAX 100

using namespace std;

bool is_prime(long n) {
    if(n == 0 || n == 1) return false;

    for(long i = 2; i <= n/2; i++) {
        if(n % i == 0) return false;
    }

    return true;
}

int main() {
    vector<thread> threads;
    array<string, THREADS> results;

    threads.reserve(THREADS);

    // Make the correct number of threads.
    for(int id = 0; id < THREADS; id++) {
        threads.emplace_back([id, &results] {   
            // Determine fraction of range for this thread.
            long first = ((float) id/THREADS) * (MAX + 1 - MIN) + MIN;
            long last = ((float) (id + 1)/THREADS) * (MAX + 1 - MIN) + MIN - 1;
            long primes[last-first];
            long index = 0;

            // Check all numbers in this threads range.
            for(long i = first; i <= last; i++) {
                if(is_prime(i)) {
                    primes[index++] = i;
                }
            }

            string result = to_string(primes[0]);
            for(long i = 1; i < index; i++) {
                result += ", " + to_string(primes[i]);
            }

            results[id] = result;
        });
    }

    // Wait for all threads to finish.
    for(auto &thread : threads) {
        thread.join();
    }

    // Print results on separate lines to show threading.
    for(auto &result : results) {
        cout << result << endl;
    }

    return 0;
}
