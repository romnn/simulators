FROM romnn/m2s-base

COPY ./benchmarks /benchmarks
WORKDIR /benchmarks /benchmarks

# try if the cuda sdk works at least
# WORKDIR /sdk
# RUN git clone https://github.com/Multi2Sim/m2s-bench-cudasdk-6.5.git /sdk
