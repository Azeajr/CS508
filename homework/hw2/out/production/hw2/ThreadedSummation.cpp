#include <chrono>
#include <cstdlib>
#include <ctime>
#include <iostream>
// #include <mutex>
#include <thread>
#include <unistd.h>
#include <vector>

class Summation {
  // std::mutex m;

public:
  static int finalSum;
  int interSum;
  std::vector<int> randInts;
  int startIndex, endIndex;
  Summation(std::vector<int> randInts) {
    interSum = 0;
    this->randInts = randInts;
  }
  void run(int startIndex, int endIndex);
};

void Summation::run(int startIndex, int endIndex) {
  for (size_t i = startIndex; i <= endIndex; i++) {
    this->interSum += randInts.at(i);
  }

  {
    // m.lock();
    finalSum += interSum;
    // m.unlock();
  }
}

std::vector<int> randIntegerArray(int arraySize, int maxValue) {
  std::vector<int> randInts;
  std::srand(std::time(nullptr));
  for (size_t i = 0; i < arraySize; i++) {
    randInts.push_back(1 + std::rand() / ((RAND_MAX + 1u) / maxValue));
  }
  return randInts;
}

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

int Summation::finalSum = 0;

int main(int argc, char const *argv[]) {

  std::vector<int> testing = randIntegerArray(100000000, INT_MAX - 1);

  // Summation sum1 = Summation(testing);
  // Summation sum2 = Summation(testing);

  // std::cout << "Sum1: " << sum1.finalSum << std::endl;
  // sum1.run(0, 49);
  // std::cout << "Sum1: " << sum1.finalSum << std::endl;

  // std::cout << "Sum2: " << sum2.finalSum << std::endl;
  // sum2.run(50, 99);
  // std::cout << "Sum2: " << sum2.finalSum << std::endl;

  // std::cout << "Sum1: " << sum1.finalSum << std::endl;

  // for (size_t i = 1; i <= 10; i++) {
  //   std::cout << "Thread #: " << i
  //             << "\tDuration: " << timeTrial(testing, i).count()
  //             << " milliseconds" << std::endl;
  //   std::cout << "Sum: " << Summation::finalSum << std::endl;
  // }

  std::cout << "Thread #: " << 100
              << "\tDuration: " << timeTrial(testing, 100).count()
              << " milliseconds" << std::endl;
    std::cout << "Sum: " << Summation::finalSum << std::endl;

  int temp = 0;
  for (auto &element : testing) {
    temp += element;
  }

  std::cout << temp << std::endl;

  return 0;
}
