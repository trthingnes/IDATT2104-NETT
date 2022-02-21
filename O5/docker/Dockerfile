FROM gcc:latest

RUN mkdir /cpp
WORKDIR /cpp
COPY temp/program.cpp /cpp/program.cpp
RUN g++ -o program /cpp/program.cpp
CMD ["./program"]
