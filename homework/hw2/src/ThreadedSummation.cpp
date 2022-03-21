/**
 * @file ThreadedSummation.cpp
 * @author Antonio Zea
 * @brief Use threads in parallel to sum an array of integers.
 * Compile: g++ ThreadedSummation.cpp -std=c++11
 * 
 * @version 0.1
 * @date 2022-03-09
 * 
 * @copyright Copyright (c) 2022
 * 
 */

#include <chrono>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <mutex>
#include <thread>
#include <unistd.h>
#include <vector>

/**
 * @brief Summation class meant to facilitate the summation of large array of
 * numbers
 */
class Summation {
private:
  /**
   * @brief Needed to make this static so that the mutex object is shared
   * across all class objects.
   * Not used in current implementation.
   */
  // static std::mutex mutex;

public:
  /**
   * @brief finalSum  acts as the global sum shared by all instances of the
   * class. Each Summation object will have its own intersum.
   */
  static std::atomic_ullong finalSum;
  /**
   * @brief Previous iteration of program was non atomic and used mutex for 
   * thread safety
   * Not used in current implementation.
   */
  // static unsigned long long finalSum;
  unsigned long long interSum;
  std::vector<int> randInts;
  int startIndex, endIndex;
  /**
   * @brief Construct a new Summation object
   *
   * @param randInts A vector containing all the integers to be added
   */
  Summation(std::vector<int> randInts) {
    interSum = 0;
    this->randInts = randInts;
  }
  /**
   * @brief Class function that will be run by threads.
   *
   * @param startIndex
   * @param endIndex
   */
  void run(int startIndex, int endIndex) {
    for (size_t i = startIndex; i <= endIndex; i++) {
      this->interSum += randInts.at(i);
    }
    /**
     * @brief Putting this thread to sleep for a random amount of time to 
     * increase the chance that a race condition will occur
     */
    // std::this_thread::sleep_for(
    //     std::chrono::milliseconds(1 + std::rand() / ((RAND_MAX + 1u) /
    //     1000)));

    /**
     * @brief As far as I understand this, lock_guard does the work of locking
     * the mutex(mutual exclusive) for a portion of code and unlocking the
     * mutex once control leaves the scope in which the lock_guard was created.
     *
     * I expect this to have the same effect as synchronized in java
     * Not used in current implementation.
     * 
     * @return std::lock_guard<std::mutex>
     */
    // std::lock_guard<std::mutex> lock(mutex);
    /**
     * @brief Using mutex manually
     * Not used in current implementation.
     */
    // mutex.lock();

    finalSum += interSum;
    /**
     * @brief Not used in current implementation.
     */
    // mutex.unlock();
  }
};

/**
 * @brief Creates a vector of size arraySize with int values from 0 to maxValue
 * inclusive.
 *
 * @param arraySize
 * @param maxValue
 * @return std::vector<int>
 */
std::vector<int> randIntegerArray(int arraySize, int maxValue) {
  std::vector<int> randInts;
  std::srand(std::time(nullptr));
  for (size_t i = 0; i < arraySize; i++) {
    randInts.push_back(1 + std::rand() / ((RAND_MAX + 1u) / maxValue));
  }
  return randInts;
}

/**
 * @brief Takes the testData and splits up the work of adding the vector's
 * integers across numThreads many threads.
 *
 * @param testData Vector of integers to be summed
 * @param numThreads Number of threads to create
 * @return std::chrono::microseconds Duration of how long it took to sum the
 * integers using this many threads
 */
std::chrono::microseconds timeTrial(std::vector<int> testData, int numThreads) {
  Summation::finalSum = 0;
  std::vector<std::thread> threads;

  int smallStep = testData.size() / numThreads;
  int overflow = testData.size() % numThreads;

  int startIndex, endIndex = -1;

  auto start = std::chrono::steady_clock::now();

  /**
   * @brief Handle the balanceing of how many elements each thread will try to 
   * sum
   */
  for (int i = 0; i < numThreads; i++) {
    startIndex = endIndex + 1;
    if (i < overflow) {
      endIndex = startIndex + smallStep;
    } else {
      endIndex = startIndex + smallStep - 1;
    }
    threads.push_back(std::thread(&Summation::run, new Summation(testData),
                                  startIndex, endIndex));
  }
  for (auto &&thread : threads) {
    thread.join();
  }

  auto end = std::chrono::steady_clock::now();

  return std::chrono::duration_cast<std::chrono::microseconds>(end - start);
}

std::atomic_ullong Summation::finalSum(0);
/**
 * @brief Previous iterations of this program used non atomic finalSum and
 * mutex to lock the portion of the code that needed to be thread safe.
 * Not used in current implementation.
 */
// unsigned long long Summation::finalSum = 0;
// std::mutex Summation::mutex;

int main(int argc, char const *argv[]) {

  std::vector<int> testing = randIntegerArray(1000000, INT_MAX - 1);

  /**
   * @brief Test summing the vector using 1 through 100 threads.
   *
   */
  for (size_t i = 1; i <= 100; i++) {
    auto duration = timeTrial(testing, i).count();
    std::cout << "Thread #: " << i << "\tDuration: " << duration
              << " microseconds"
              << "\tSum: " << Summation::finalSum << std::endl;
  }

  /**
   * @brief Calculate the sum in the main thread to verify other sums
   *
   */
  unsigned long long temp = 0;
  for (auto &element : testing) {
    temp += element;
  }

  std::cout << temp << std::endl;

  return 0;
}
