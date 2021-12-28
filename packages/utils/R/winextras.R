#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

# In GNU R win.version is based on the Windows GetVersionEx API function
# Here we use system properties to achive a similar result
win.version <- function() {
  import(java.lang.System)
  paste(
    System$getProperty("os.name"),
    System$getProperty("os.arch"),
    paste0("(", System$getProperty("os.version"), ")")
  )
}