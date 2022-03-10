#include <cstdlib>
#include <ctime>
#include <vector>
#include <iostream>

class Summation {
public:
  int finalSum = 0;
  int interSum;
  std::vector<int> randInts;
  int startIndex, endIndex;
  Summation(std::vector<int> randInts, int startIndex, int endIndex) {
    interSum = 0;
    this->randInts = randInts;
    this->startIndex = startIndex;
    this->endIndex = endIndex;
  }
  void run();
};

void Summation::run() {
  for (size_t i = startIndex; i < endIndex; i++) {
    interSum += randInts.at(i);
  }
  finalSum += interSum;
}

std::vector<int> randIntegerArray(int arraySize, int maxValue) {
  std::vector<int> randInts;
  std::srand(std::time(nullptr));
  for (size_t i = 0; i < arraySize; i++)
  {
    randInts.push_back(1 + std::rand() / ((RAND_MAX + 1u) / maxValue));
  }

  return randInts;
}

int main(int argc, char const *argv[]) {
  std::vector<int> testing = randIntegerArray(100, 25);

  for (auto &element : testing) {
    std::cout << element << std::endl;
  }
  return 0;
}
