// OpenCL Kernel Function for element by element vector addition

__kernel void vecAdd(__global const double *a, __global const double *b,
                     __global double *c, int n) {
  // get index into global data array
  int id = get_global_id(0);

  // bounds check
  if (id < n) {
    c[id] = a[id] + b[id];
  }
}
