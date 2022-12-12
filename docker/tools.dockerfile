FROM rust:1.65.0

RUN apt-get update && apt-get install -y tree

WORKDIR /tools
COPY ./Cargo* /tools/
COPY ./tools /tools/tools
COPY ./src /tools/src

RUN tree /tools
RUN rustup target add x86_64-unknown-linux-musl
RUN cargo build --release --all-targets --target x86_64-unknown-linux-musl
