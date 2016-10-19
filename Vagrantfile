# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "ubuntu/xenial32"
  config.vm.provision :shell, inline: "apt-get update && apt-get install maven openjdk-8-jdk gcc-4.7 g++-4.7 gcc-4.7-plugin-dev gfortran-4.7 -y"
  config.vm.synced_folder ".", "/home/ubuntu/renjin"
  
  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 2
  end
end
