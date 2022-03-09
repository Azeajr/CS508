#include <iostream>
#include <thread>

void hello(){
  std::cout << "Hello Concurrent World\n";
}

int main(int argc, char const *argv[])
{
  std::thread t(hello);
  t.join();

  return 0;
}
