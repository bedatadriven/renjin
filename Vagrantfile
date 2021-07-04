# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version
VAGRANTFILE_API_VERSION = "2"

# Override host locale variable
ENV["LC_ALL"] = "en_US.UTF-8"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "ubuntu/bionic64"
  config.vm.provision :shell, inline: "apt-get update && apt-get install openjdk-8-jdk maven make gcc-4.8 gcc-4.8-plugin-dev gfortran-4.8 g++-4.8 gcc-4.8.multilib g++-4.8-multilib unzip libz-dev -y"
  config.vm.synced_folder ".", "/home/ubuntu/renjin"

  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 2
  end
end
