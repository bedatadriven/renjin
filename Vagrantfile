# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version
VAGRANTFILE_API_VERSION = "2"

# Override host locale variable
ENV["LC_ALL"] = "en_US.UTF-8"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "ubuntu/xenial64"
  config.vm.provision :shell, inline: "add-apt-repository ppa:openjdk-r/ppa"
  config.vm.provision :shell, inline: "apt-get update && apt-get install openjdk-11-jdk maven make gcc-4.7 gcc-4.7-plugin-dev gfortran-4.7 g++-4.7 gcc-4.7.multilib g++-4.7-multilib unzip libz-dev -y"
  config.vm.synced_folder ".", "/home/ubuntu/renjin"

  # So we can do ./gradlew publishToMavenLocal and also save some disk space
  config.vm.synced_folder "~/.m2", "/home/vagrant/.m2"

  config.vm.provider "virtualbox" do |v|
    v.memory = 6144
    v.cpus = 2
  end
end
