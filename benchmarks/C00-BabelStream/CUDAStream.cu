// Copyright (c) 2015-16 Tom Deakin, Simon McIntosh-Smith,
// University of Bristol HPC
//
// For full license terms please see the LICENSE file distributed with this
// source code
#include <cstring>
#include <iostream>
#include <vector>
#include <iomanip>

#define TBSIZE 1024
#define DOT_NUM_BLOCKS 256

// Array values
#define startA (0.1)
#define startB (0.2)
#define startC (0.0)
#define startScalar (0.4)

// Default size of 2^25
int ARRAY_SIZE = 33554432;
unsigned int num_times = 100;
unsigned int deviceIndex = 0;
bool use_float = false;
bool mibibytes = false;

enum class Benchmark { All, Triad, Nstream };

// Selected run options.
Benchmark selection = Benchmark::All;

void parseArguments(int argc, char *argv[]);

template <typename T>
void check_solution(const unsigned int ntimes, std::vector<T> &a,
                    std::vector<T> &b, std::vector<T> &c, T &sum);

template <typename T> void run();

void check_error(void) {
  cudaError_t err = cudaGetLastError();
  if (err != cudaSuccess) {
    std::cerr << "Error: " << cudaGetErrorString(err) << std::endl;
    exit(err);
  }
}

std::string getDeviceName(const int device) {
  cudaDeviceProp props;
  cudaGetDeviceProperties(&props, device);
  check_error();
  return std::string(props.name);
}

void listDevices(void) {
  // Get number of devices
  int count;
  cudaGetDeviceCount(&count);
  check_error();

  // Print device names
  if (count == 0) {
    std::cerr << "No devices found." << std::endl;
  } else {
    std::cout << std::endl;
    std::cout << "Devices:" << std::endl;
    for (int i = 0; i < count; i++) {
      std::cout << i << ": " << getDeviceName(i) << std::endl;
    }
    std::cout << std::endl;
  }
}

template <typename T> void run() {
  std::streamsize ss = std::cout.precision();

  if (selection == Benchmark::All)
    std::cout << "Running kernels " << num_times << " times" << std::endl;
  else if (selection == Benchmark::Triad) {
    std::cout << "Running triad " << num_times << " times" << std::endl;
    std::cout << "Number of elements: " << ARRAY_SIZE << std::endl;
  }

  if (sizeof(T) == sizeof(float))
    std::cout << "Precision: float" << std::endl;
  else
    std::cout << "Precision: double" << std::endl;

  if (mibibytes) {
    // MiB = 2^20
    std::cout << std::setprecision(1) << std::fixed
              << "Array size: " << ARRAY_SIZE * sizeof(T) * pow(2.0, -20.0)
              << " MiB"
              << " (=" << ARRAY_SIZE * sizeof(T) * pow(2.0, -30.0) << " GiB)"
              << std::endl;
    std::cout << "Total size: "
              << 3.0 * ARRAY_SIZE * sizeof(T) * pow(2.0, -20.0) << " MiB"
              << " (=" << 3.0 * ARRAY_SIZE * sizeof(T) * pow(2.0, -30.0)
              << " GiB)" << std::endl;
  } else {
    // MB = 10^6
    std::cout << std::setprecision(1) << std::fixed
              << "Array size: " << ARRAY_SIZE * sizeof(T) * 1.0E-6 << " MB"
              << " (=" << ARRAY_SIZE * sizeof(T) * 1.0E-9 << " GB)"
              << std::endl;
    std::cout << "Total size: " << 3.0 * ARRAY_SIZE * sizeof(T) * 1.0E-6
              << " MB"
              << " (=" << 3.0 * ARRAY_SIZE * sizeof(T) * 1.0E-9 << " GB)"
              << std::endl;
  }
  std::cout.precision(ss);

  /* Stream<T> *stream; */

  /* stream = new CUDAStream<T>(ARRAY_SIZE, deviceIndex); */
  /* stream->init_arrays(startA, startB, startC); */

  /* // Result of the Dot kernel, if used. */
  /* T sum = 0.0; */

  /* std::vector<std::vector<double>> timings; */

  /* switch (selection) { */
  /* case Benchmark::All: */
  /*   timings = run_all<T>(stream, sum); */
  /*   break; */
  /* case Benchmark::Triad: */
  /*   timings = run_triad<T>(stream); */
  /*   break; */
  /* case Benchmark::Nstream: */
  /*   timings = run_nstream<T>(stream); */
  /*   break; */
  /* }; */

  /* // Check solutions */
  /* // Create host vectors */
  /* std::vector<T> a(ARRAY_SIZE); */
  /* std::vector<T> b(ARRAY_SIZE); */
  /* std::vector<T> c(ARRAY_SIZE); */

  /* stream->read_arrays(a, b, c); */
  /* check_solution<T>(num_times, a, b, c, sum); */

  /* std::cout << std::left << std::setw(12) << "Function" << std::left */
  /*           << std::setw(12) << ((mibibytes) ? "MiBytes/sec" : "MBytes/sec") */
  /*           << std::left << std::setw(12) << "Min (sec)" << std::left */
  /*           << std::setw(12) << "Max" << std::left << std::setw(12) << "Average" */
  /*           << std::endl */
  /*           << std::fixed; */

  /* if (selection == Benchmark::All || selection == Benchmark::Nstream) { */

  /*   std::vector<std::string> labels; */
  /*   std::vector<size_t> sizes; */

  /*   if (selection == Benchmark::All) { */
  /*     labels = {"Copy", "Mul", "Add", "Triad", "Dot"}; */
  /*     sizes = {2 * sizeof(T) * ARRAY_SIZE, 2 * sizeof(T) * ARRAY_SIZE, */
  /*              3 * sizeof(T) * ARRAY_SIZE, 3 * sizeof(T) * ARRAY_SIZE, */
  /*              2 * sizeof(T) * ARRAY_SIZE}; */
  /*   } else if (selection == Benchmark::Nstream) { */
  /*     labels = {"Nstream"}; */
  /*     sizes = {4 * sizeof(T) * ARRAY_SIZE}; */
  /*   } */

  /*   for (int i = 0; i < timings.size(); ++i) { */
  /*     // Get min/max; ignore the first result */
  /*     auto minmax = */
  /*         std::minmax_element(timings[i].begin() + 1, timings[i].end()); */

  /*     // Calculate average; ignore the first result */
  /*     double average = */
  /*         std::accumulate(timings[i].begin() + 1, timings[i].end(), 0.0) / */
  /*         (double)(num_times - 1); */

  /*     // Display results */
  /*     if (output_as_csv) { */
  /*       std::cout << labels[i] << csv_separator << num_times << csv_separator */
  /*                 << ARRAY_SIZE << csv_separator << sizeof(T) << csv_separator */
  /*                 << ((mibibytes) ? pow(2.0, -20.0) : 1.0E-6) * sizes[i] / */
  /*                        (*minmax.first) */
  /*                 << csv_separator << *minmax.first << csv_separator */
  /*                 << *minmax.second << csv_separator << average << std::endl; */
  /*     } else { */
  /*       std::cout << std::left << std::setw(12) << labels[i] << std::left */
  /*                 << std::setw(12) << std::setprecision(3) */
  /*                 << ((mibibytes) ? pow(2.0, -20.0) : 1.0E-6) * sizes[i] / */
  /*                        (*minmax.first) */
  /*                 << std::left << std::setw(12) << std::setprecision(5) */
  /*                 << *minmax.first << std::left << std::setw(12) */
  /*                 << std::setprecision(5) << *minmax.second << std::left */
  /*                 << std::setw(12) << std::setprecision(5) << average */
  /*                 << std::endl; */
  /*     } */
  /*   } */
  /* } else if (selection == Benchmark::Triad) { */
  /*   // Display timing results */
  /*   double total_bytes = 3 * sizeof(T) * ARRAY_SIZE * num_times; */
  /*   double bandwidth = ((mibibytes) ? pow(2.0, -30.0) : 1.0E-9) * */
  /*                      (total_bytes / timings[0][0]); */

  /*   if (output_as_csv) { */
  /*     std::cout << "function" << csv_separator << "num_times" << csv_separator */
  /*               << "n_elements" << csv_separator << "sizeof" << csv_separator */
  /*               << ((mibibytes) ? "gibytes_per_sec" : "gbytes_per_sec") */
  /*               << csv_separator << "runtime" << std::endl; */
  /*     std::cout << "Triad" << csv_separator << num_times << csv_separator */
  /*               << ARRAY_SIZE << csv_separator << sizeof(T) << csv_separator */
  /*               << bandwidth << csv_separator << timings[0][0] << std::endl; */
  /*   } else { */
  /*     std::cout << "--------------------------------" << std::endl */
  /*               << std::fixed << "Runtime (seconds): " << std::left */
  /*               << std::setprecision(5) << timings[0][0] << std::endl */
  /*               << "Bandwidth (" << ((mibibytes) ? "GiB/s" : "GB/s") */
  /*               << "):  " << std::left << std::setprecision(3) << bandwidth */
  /*               << std::endl; */
  /*   } */
  /* } */

  /* delete stream; */
}

int main(int argc, char *argv[]) {

  parseArguments(argc, argv);

  if (use_float)
    run<float>();
  else
    run<double>();
}

int parseInt(const char *str, int *output) {
  char *next;
  *output = strtol(str, &next, 10);
  return !strlen(next);
}

int parseUInt(const char *str, unsigned int *output) {
  char *next;
  *output = strtoul(str, &next, 10);
  return !strlen(next);
}

void parseArguments(int argc, char *argv[]) {
  for (int i = 1; i < argc; i++) {
    if (!std::string("--list").compare(argv[i])) {
      listDevices();
      exit(EXIT_SUCCESS);
    } else if (!std::string("--device").compare(argv[i])) {
      if (++i >= argc || !parseUInt(argv[i], &deviceIndex)) {
        std::cerr << "Invalid device index." << std::endl;
        exit(EXIT_FAILURE);
      }
    } else if (!std::string("--arraysize").compare(argv[i]) ||
               !std::string("-s").compare(argv[i])) {
      if (++i >= argc || !parseInt(argv[i], &ARRAY_SIZE) || ARRAY_SIZE <= 0) {
        std::cerr << "Invalid array size." << std::endl;
        exit(EXIT_FAILURE);
      }
    } else if (!std::string("--numtimes").compare(argv[i]) ||
               !std::string("-n").compare(argv[i])) {
      if (++i >= argc || !parseUInt(argv[i], &num_times)) {
        std::cerr << "Invalid number of times." << std::endl;
        exit(EXIT_FAILURE);
      }
      if (num_times < 2) {
        std::cerr << "Number of times must be 2 or more" << std::endl;
        exit(EXIT_FAILURE);
      }
    } else if (!std::string("--float").compare(argv[i])) {
      use_float = true;
    } else if (!std::string("--triad-only").compare(argv[i])) {
      selection = Benchmark::Triad;
    } else if (!std::string("--nstream-only").compare(argv[i])) {
      selection = Benchmark::Nstream;
    } else if (!std::string("--help").compare(argv[i]) ||
               !std::string("-h").compare(argv[i])) {
      std::cout << std::endl;
      std::cout << "Usage: " << argv[0] << " [OPTIONS]" << std::endl
                << std::endl;
      std::cout << "Options:" << std::endl;
      std::cout << "  -h  --help               Print the message" << std::endl;
      std::cout << "      --list               List available devices"
                << std::endl;
      std::cout << "      --device     INDEX   Select device at INDEX"
                << std::endl;
      std::cout << "  -s  --arraysize  SIZE    Use SIZE elements in the array"
                << std::endl;
      std::cout
          << "  -n  --numtimes   NUM     Run the test NUM times (NUM >= 2)"
          << std::endl;
      std::cout << "      --float              Use floats (rather than doubles)"
                << std::endl;
      std::cout << "      --triad-only         Only run triad" << std::endl;
      std::cout << "      --nstream-only       Only run nstream" << std::endl;
      std::cout << std::endl;
      exit(EXIT_SUCCESS);
    } else {
      std::cerr << "Unrecognized argument '" << argv[i] << "' (try '--help')"
                << std::endl;
      exit(EXIT_FAILURE);
    }
  }
}
