# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "ubuntu/trusty32"
  config.vm.provision :shell, inline: "apt-get update && apt-get install maven gcc-4.6 gcc-4.6-plugin-dev gfortran-4.6 -y"
  config.vm.synced_folder ".", "/home/vagrant/renjin"
end
