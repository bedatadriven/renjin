/* GCC plugin APIs.

   Copyright (C) 2009, 2010, 2011 Mingjie Xing, mingjie.xing@gmail.com. 

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. */

#define _GNU_SOURCE

#include <stddef.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <ctype.h>

/* GCC header files.  */


#include "gcc-plugin.h"
#include "plugin.h"
#include "plugin-version.h"


#include "tree.h"
#include "gimple.h"
#include "tree-flow.h"
#include "tree-pass.h"
#include "cfgloop.h"
#include "cgraph.h"
#include "options.h"

/* plugin license check */

int plugin_is_GPL_compatible;


/* Post pass */

void pass_finished_callback(void *gcc_data, void *user_data) { 
  struct opt_pass * p = (struct opt_pass*)gcc_data;
 
  printf("Renjin: pass '%s'\n", p->name);

   struct cgraph_node *node;
   for (node = cgraph_nodes; (node); 
         node = node->next)
    {
       printf("  function '%s'\n", cgraph_node_name(node));
    } 
}

/* Plugin initialization.  */

int
plugin_init (struct plugin_name_args *plugin_info,
             struct plugin_gcc_version *version)
{
  int i;
  int argc = plugin_info->argc;
  struct plugin_argument *argv = plugin_info->argv;

  printf("Renjin Hello world\n");

  register_callback("renjin", PLUGIN_PASS_EXECUTION, (plugin_callback_func)pass_finished_callback, 0);


   
  return 0;
}

