# renjin <a href='http://www.renjin.org/'><img src='http://www.renjin.org/assets/img/logo.svg' align="right" width="180"/></a>

![Building](https://build.renjin.org/job/Renjin/badge/icon)

Renjin is a JVM-based interpreter for the [The R Project for Statistical
Computing](http://www.r-project.org).

The primary goals of the project are to provide a modern interpreter
that serves as a drop-in replacement for GNU R, but is easier to
integrate with other systems, offers better performance, and is
more extensible.

For more information, please visit http://www.renjin.org.

## Installation

Standalone distributions of Renjin are available for ad-hoc analysis. Downloads
are available at [renjin.org](http://www.renjin.org).

Artifacts from the latest successful build are available from the build server [here](https://nexus.bedatadriven.com/content/groups/public/org/renjin/).

### Debian (and Ubuntu)

You can add our APT repository and get regular updates automatically:
```bash
# 1. Add the Renjin repository signing keys to be able to verify downloaded packages
sudo apt-key adv --keyserver hkp://packages.renjin.org:80 --recv-keys EB2514FC345926E9

# 2. Add the Renjin repository
echo deb http://packages.renjin.org/repo/apt stable main | sudo tee /etc/apt/sources.list.d/renjin.list

# 3. Update list of available packages
sudo apt-get update

# 4. Install Renjin
sudo apt-get install renjin
```

Please see our [Downloads](http://www.renjin.org/downloads.html) page.

## Adding to a Maven Project

Using a a modern build tool such a Maven is definitely recommended, particularly
if you're planning on including R packages which often have several levels of
transitive dependencies.

You can add Renjin to your Maven project by adding the following to your `pom.xml` file:

```.xml
<dependencies>
  <dependency>
    <groupId>org.renjin</groupId>
    <artifactId>renjin-script-engine</artifactId>
    <version>RELEASE</version>
  </dependency>
</dependencies>
<repositories>
  <repository>
    <id>bedatadriven</id>
    <name>bedatadriven public repo</name>
    <url>https://nexus.bedatadriven.com/content/groups/public/</url>
  </repository>
</repositories>
```

## Compiling from Source

At this time, building Renjin completely is only supported on Ubuntu 16.04.
The resulting Java builds are fully platform-independent.

See [BUILDING](BUILDING.md) for more information.

## License

Renjin is available under the GPLv2 (or higher) license, see [LICENSE](LICENSE.txt) for the
full text of the license. Renjin also includes code from R which is
redistributed here. R uses
[many different licenses](http://www.r-project.org/Licenses/) so check the file
sources to see which license applies.
