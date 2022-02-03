cmake_minimum_required(VERSION 3.16)
project(O2)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS -pthread)

add_executable(O2 main.cpp)
