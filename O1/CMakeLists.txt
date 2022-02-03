cmake_minimum_required(VERSION 3.16)
project(IDATT2104_NETT)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread -fsanitize=address -Wall -Wextra")

add_executable(IDATT2104_NETT main.cpp)
