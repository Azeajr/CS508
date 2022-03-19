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
  static std::mutex mutex;

public:
  /**
   * @brief finalSum  acts as the global sum shared by all instances of the 
   * class. Each Summation object will have its own intersum.
   */
  static unsigned long long finalSum;
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
     * @brief Putting this thread to sleep to increase the chance that they 
     * will make the program misbehave
     */
    std::this_thread::sleep_for(
        std::chrono::milliseconds(1 + std::rand() / ((RAND_MAX + 1u) / 1000)));

    /**
     * @brief As far as I understand this, lock_guard does the work of locking 
     * the mutex(mutual exclusive) for a portion of code and unlocking the 
     * mutex once control leaves the scope in which the lock_guard was created.
     * 
     * I expect this to have the same effect as synchronized in java
     * 
     * @return std::lock_guard<std::mutex> 
     */
    std::lock_guard<std::mutex> lock(mutex);
    finalSum += interSum;
  }
};

/**
 * @brief Creates a vector of size arraySize with int values from 0 to maxValue inclusive.
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
  // Summation *sum = new Summation(testData);
  std::vector<std::thread *> threads;
  // std::thread threads[numThreads];

  int smallStep = testData.size() / numThreads;
  int overflow = testData.size() % numThreads;

  int startIndex, endIndex = -1;

  auto start = std::chrono::steady_clock::now();

  for (int i = 0; i < numThreads; i++) {

    startIndex = endIndex + 1;
    if (i < overflow) {
      endIndex = startIndex + smallStep;
    } else {
      endIndex = startIndex + smallStep - 1;
    }
    threads.push_back(new std::thread(&Summation::run, new Summation(testData),
                                      startIndex, endIndex));
  }
  for (auto &&thread : threads) {
    thread->join();
  }
  auto end = std::chrono::steady_clock::now();

  return std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
}

unsigned long long Summation::finalSum = 0;
std::mutex Summation::mutex;

int main(int argc, char const *argv[]) {

  std::vector<int> testing = randIntegerArray(10000, INT_MAX - 1);

  /**
   * @brief Test summing the vector using 1 through 100 threads.
   * 
   */
  for (size_t i = 1; i <= 100; i++) {
    std::cout << "Thread #: " << i
              << "\tDuration: " << timeTrial(testing, i).count()
              << " milliseconds" << std::endl;
    std::cout << "Sum: " << Summation::finalSum << std::endl;
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
