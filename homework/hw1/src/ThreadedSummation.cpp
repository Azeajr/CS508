#include <iostream>
#include <string>
#include <thread>

void Summation(int number, int step) {
  int sum = 0, temp = 0;
  std::string sumString = "";
  for (size_t i = 1; i <= number; i++) {
    temp = 1 + step * (i - 1);
    sum += temp;
    if (i != number) {
      sumString.append(std::to_string(temp)).append(", ");
    } else {
      sumString.append(std::to_string(temp));
    }
  }
  std::cout << "Thread: " << std::this_thread::get_id() << std::endl;
  std::cout << "Sum: " << sum << "\nSummation: " << sumString << std::endl;
}

int main(int argc, char const *argv[]) {
  std::cout << "Main thread: " << std::this_thread::get_id() << std::endl;
  std::thread t1(Summation, 100, 1);
  std::thread t2(Summation, 100, 2);
  std::thread t3(Summation, 100, 5);

  t1.join();
  t2.join();
  t3.join();

  std::cout << "Main thread: " << std::this_thread::get_id() << std::endl;

  return 0;
}
