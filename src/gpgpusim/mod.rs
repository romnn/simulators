#![allow(warnings)]

pub mod config;

use anyhow::Result;
use tc::client::bollard::Client;
use testcontainers_rs as tc;
// use testcontainers_rs::{
//     client::{bollard::Client, DockerClient},
//     Container, DockerImage,
// };

struct Container {
    container: tc::Container<Client>,
}

impl Container {
    pub async fn new() -> Result<Self> {
        use tc::client::DockerClient;
        let client = Client::new().await?;
        let image = tc::DockerImage::new("nginx");
        //.with_mapped_port(80, 80);
        let container = client.create(image).await?;
        Ok(Self { container })
    }

    pub async fn start(&self) -> Result<()> {
        self.container.start().await?;
        Ok(())
    }
}

struct ContainerRequest {
    pub image: String,
}

/*
refactor the rust tools into a library
- module for each simulator
- automate running a gpgpusim benchmark inside a docker container
- get and parse the logs
*/

#[cfg(test)]
mod tests {
    use super::*;
    use anyhow::Result;

    // (flavor = "multi_thread", worker_threads = 2)]

    #[tokio::test]
    async fn it_works() -> Result<()> {
        let container = Container::new().await?;
        println!("starting container");
        container.start().await?;

        // let resp = reqwest::get(container.uri().await?).await?.text().await?;
        // println!("{:#?}", resp);
        println!("container is started");
        println!("terminating");
        Ok(())
    }
}
