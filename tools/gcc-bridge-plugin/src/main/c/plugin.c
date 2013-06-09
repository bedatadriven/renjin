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
#include "diagnostic.h"
#include "tree-flow.h"
#include "tree-pass.h"
#include "cfgloop.h"
#include "cgraph.h"
#include "options.h"

/* plugin license check */

int plugin_is_GPL_compatible;

#define MAX_RECORD_TYPES 1000

tree record_types[MAX_RECORD_TYPES];
int record_type_count = 0;

FILE *json_f;

int json_indent_level = 0;
int json_needs_comma = 0;

#define JSON_ARRAY  1
#define JSON_OBJECT  2

#define TRACE(...) printf(__VA_ARGS__)

typedef struct json_context {
  int needs_comma;
  int type;
  int indent;
} json_context;

// GCC won't let us malloc and I don't want to mess
// around with GCC's internal memory management stuff,
// so we'll just use a fixed-size stack

json_context json_context_stack[128];
int json_context_head = 0;

void json_context_push(int type) {
  json_context_head ++;
  json_context_stack[json_context_head].needs_comma = false;
  json_context_stack[json_context_head].type = type;
}

void json_context_pop() {
  json_context_head--;
}

/* Json writing functions */

void json_indent() {
  int i;
  for(i=0;i<json_context_head;++i) {
    fprintf(json_f, "  ");
  }
}

void json_pre_value() {
  json_context *context = &json_context_stack[json_context_head];
  if(context->type == JSON_ARRAY) {
    if(context->needs_comma) {
      fprintf(json_f, ",");
    }
    context->needs_comma = 1;
    fprintf(json_f, "\n");
    json_indent(); 
  }
}

void json_start_object() {
  json_pre_value();
  fprintf(json_f, "{");
  json_context_push(JSON_OBJECT);
}

void json_start_array() {
  fprintf(json_f, "[");
  json_context_push(JSON_ARRAY);
}

void json_null() {
  json_pre_value();
  fprintf(json_f, "null");
}

void json_field(const char *name) {
  json_context *context = &json_context_stack[json_context_head];
  if(context->needs_comma) {
    fprintf(json_f, ",");
  }
  context->needs_comma = 1;
  fprintf(json_f, "\n");
  json_indent();
  fprintf(json_f, "\"%s\": ", name);

  json_context_stack[json_context_head].needs_comma = 1;
}


void json_string(const char *value, int length) {
  putc('"', json_f);
  while (--length >= 0) {
    char ch = *value++;
	  if (ch >= ' ' && ch < 127) {
	    if(ch == '\\' || ch == '"') {
	      putc('\\', json_f);
	    }
      putc(ch, json_f);
    } else {
	    fprintf(json_f, "\\u%04x", ch);	      
    }
  }
  putc('"', json_f);
}

void json_string_field(const char *name, const char *value) {
  json_field(name);
  json_string(value, strlen(value));
}

void json_string_field2(const char *name, const char *value, int length) {
  json_field(name);
  json_string(value, length);
}


void json_int(int value) {
  json_pre_value();
  fprintf(json_f, "%d", value);
}

void json_int_field(const char *name, int value) {
  json_field(name);
  json_int(value);
}

void json_real_field(const char *name, REAL_VALUE_TYPE value) {
  json_field(name);
  if (REAL_VALUE_ISINF (value)) {
    fprintf(json_f, "\"%s\"", REAL_VALUE_NEGATIVE (value) ? "-Inf" : "Inf");
  } else if(REAL_VALUE_ISNAN (value)) {
    fprintf(json_f, "\"%s\"", "NaN");
  } else {
    char string[100];
    real_to_decimal (string, &value, sizeof (string), 0, 1);
    fprintf(json_f, "%s", string);
  }
}

void json_bool_field(const char *name, int value) {
  json_field(name);
  fprintf(json_f, value ? "true" : "false");
}


void json_array_field(const char *name) {
  json_field(name);
  json_start_array();
}

void json_end_array() {
  fprintf(json_f, "\n");
  json_context_pop();
  json_indent();
  fprintf(json_f, "]");
}

void json_end_object() {
  json_context_pop();
  fprintf(json_f, "\n");
  json_indent();
  fprintf(json_f, "}");
}


/* Post pass */

static void dump_type(tree type);

static void dump_function_type(tree type) {
  json_field("returnType");
  dump_type(TREE_TYPE(type));
      
  // The TYPE_ARG_TYPES are a TREE_LIST of the argument types. 
  // The TREE_VALUE of each node in this list is the type of the 
  // corresponding argument; the TREE_PURPOSE is an expression 
  // for the default argument value, if any. If the last node in 
  // the list is void_list_node (a TREE_LIST node whose TREE_VALUE 
  // is the void_type_node), then functions of this type do not 
  // take variable arguments. 
  // Otherwise, they do take a variable number of arguments.  
  
  int variable_arguments;

  tree last_arg = TYPE_ARG_TYPES(type);
  tree next_arg = TREE_CHAIN(last_arg);

  json_array_field("argumentTypes");
  while(next_arg != NULL_TREE) {
    dump_type(TREE_VALUE(last_arg));
    last_arg = next_arg;
    next_arg = TREE_CHAIN(next_arg);
  }
  if(TREE_CODE(TREE_VALUE(last_arg)) == VOID_TYPE) {
    variable_arguments = 0;
  } else {
    variable_arguments = 1;
    dump_type(TREE_VALUE(last_arg));
  }
  json_end_array();
  
  json_bool_field("variableArguments", variable_arguments);
  
}

static void dump_record_type(tree type) {
  
  TRACE("dump_record_type: entering\n");
  json_int_field("id", DEBUG_TEMP_UID (type));
  
  //if(DECL_NAME(type)) {
  //  json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(type)));
  //}
  
  // have we already encountered this record type?
  int i;
  for(i=0;i<record_type_count;++i) {
    if(record_types[i] == type) {
      return;
    }
  }
  
  // nope, add it to the list
  if(record_type_count < MAX_RECORD_TYPES) {
    record_types[record_type_count] = type;
    record_type_count++;
  } else {
    // TODO: throw error here
  }
}

static void dump_record_type_decl(tree type) {

  json_start_object();
  json_int_field("id", DEBUG_TEMP_UID (type));
  
  //if(DECL_NAME(type)) {
  //  json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(type)));
  //}
  
  tree field =  TYPE_FIELDS(type);
  json_array_field("fields");
  while(field) {
    json_start_object();
    if(DECL_NAME(field)) {
      json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(field)));
    }
    json_field("type");
    dump_type(TREE_TYPE(field));

    json_end_object();
    
    field = TREE_CHAIN(field);
  }
  json_end_array();
  json_end_object();
}

static void dump_type(tree type) {
  printf("dump_type: entering\n");
  printf("dump_type: code: %s\n", tree_code_name[TREE_CODE(type)]);
  json_start_object();
  json_string_field("type", tree_code_name[TREE_CODE(type)]);
    
  if(TYPE_SIZE(type)) {
    json_int_field("size", TREE_INT_CST_LOW(TYPE_SIZE(type)));
  }
  
  switch(TREE_CODE(type)) {
  case INTEGER_TYPE:
    json_int_field("precision", TYPE_PRECISION(type));
    json_bool_field("unsigned", TYPE_UNSIGNED(type));
    break;
  case REAL_TYPE:
    json_int_field("precision", TYPE_PRECISION(type));
    break;
  case POINTER_TYPE:
  case REFERENCE_TYPE:
    json_field("baseType");
    dump_type(TREE_TYPE(type));
    break;

  case ARRAY_TYPE:
    json_field("componentType");
    dump_type(TREE_TYPE(type));
    
    if(TYPE_DOMAIN(type)) {
      tree domain = TYPE_DOMAIN(type);
      json_field("lbound");
      json_int(TREE_INT_CST_LOW(TYPE_MIN_VALUE(domain)));

      if(TYPE_MAX_VALUE(domain)) {      
        json_field("ubound");
        json_int(TREE_INT_CST_LOW(TYPE_MAX_VALUE(domain)));
      }
      
    }
    break;
    
  case FUNCTION_TYPE:
    dump_function_type(type);
    break;
    
  case RECORD_TYPE:
    dump_record_type(type);
    break;
    
  }
  json_end_object();
  printf("dump_type: exiting\n");
}

static void dump_op(tree op) {
 	REAL_VALUE_TYPE d;
 	 	
  if(op) {
    json_start_object();
    json_string_field("type", tree_code_name[TREE_CODE(op)]);
    
   
    switch(TREE_CODE(op)) {
    case FUNCTION_DECL:
    case PARM_DECL:
    case VAR_DECL:
      json_int_field("id", DEBUG_TEMP_UID (op));
      if(DECL_NAME(op)) {
        json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(op)));
      } 
      break;  

    case CONST_DECL:
      json_field("value");
      dump_op(DECL_INITIAL(op));
      break;
          
    case INTEGER_CST:
      json_int_field("value", TREE_INT_CST_LOW (op));
      json_field("type");
      dump_type(TREE_TYPE(op));
      break;
      
    case REAL_CST:
      json_real_field("value", TREE_REAL_CST(op));
      json_field("type");
      dump_type(TREE_TYPE(op));
	    break;
	    
	  case STRING_CST:
	    json_string_field2("value", 
	      TREE_STRING_POINTER(op),
        TREE_STRING_LENGTH (op));
      json_field("type");
      dump_type(TREE_TYPE(op));
      break;
	    
	  case MEM_REF:
	    json_field("pointer");
	    dump_op(TREE_OPERAND(op, 0));
	    break;
	    
	  case ARRAY_REF:
	    json_field("array");
 	    dump_op(TREE_OPERAND(op, 0));
 	    
 	    json_field("index");
 	    dump_op(TREE_OPERAND(op, 1));
      break;
      
    case ADDR_EXPR:
      json_field("value");
 	    dump_op(TREE_OPERAND(op, 0));
 	    
 	  //  json_field("offset");
 	 //   dump_op(TREE_OPERAND(op, 1));
      break;
    case COMPONENT_REF:
      json_field("value");
      dump_op(TREE_OPERAND(op, 0));
      if(TREE_CODE(TREE_OPERAND(op, 1)) == FIELD_DECL) {
        json_string_field("member",
          IDENTIFIER_POINTER(DECL_NAME(TREE_OPERAND(op, 1))));
      }
      break;
	    
    }
    
        
    json_end_object();
  } else {
    json_null();
  }
}

static void dump_ops(gimple stmt) {
  int numops = gimple_num_ops(stmt);
  if(numops > 0) {
    json_array_field("operands");
    int i;
    for(i=0;i<numops;++i) {
      tree op = gimple_op(stmt, i);
      if(op) {
        dump_op(op);
      }
    }
    json_end_array();  
  }
}

static void dump_assignment(gimple stmt) {
  json_start_object();
  json_string_field("type", "assign");

  json_string_field("operator", tree_code_name[gimple_assign_rhs_code(stmt)]);

  json_field("lhs");
  dump_op(gimple_assign_lhs(stmt));

  tree rhs1 = gimple_assign_rhs1(stmt);
  tree rhs2 = gimple_assign_rhs2(stmt);
  tree rhs3 = gimple_assign_rhs3(stmt);
    
  json_array_field("operands");
  if(rhs1) dump_op(rhs1);
  if(rhs2) dump_op(rhs2);
  if(rhs3) dump_op(rhs3);
  json_end_array();
  json_end_object();
}

static void dump_cond(basic_block bb, gimple stmt) {
  json_start_object();
  json_string_field("type", "conditional");
  json_string_field("operator", tree_code_name[gimple_assign_rhs_code(stmt)]);
  
  dump_ops(stmt);
      
  edge true_edge, false_edge;
  extract_true_false_edges_from_block (bb, &true_edge, &false_edge);
  
  json_int_field("trueLabel", true_edge->dest->index);
  json_int_field("falseLabel", false_edge->dest->index);
  json_end_object();
}

static void dump_nop(gimple stmt) {
  json_start_object();
  json_string_field("type", "nop");
  json_end_object();
}

static void dump_return(gimple stmt) {
  json_start_object();
  json_string_field("type", "return");
  
  tree retval = gimple_return_retval(stmt);
  if(retval) {
    json_field("value");
    dump_op(retval);
  }
  json_end_object();
}

static void dump_call(gimple stmt) {
  json_start_object();
  json_string_field("type", "call");
  
  json_field("lhs");
  dump_op(gimple_call_lhs(stmt));
  
  json_field("function");
  dump_op(gimple_call_fn(stmt));
  
  int numargs = gimple_call_num_args(stmt);
  if(numargs > 0) {
    json_array_field("arguments");
    int i;
    for(i=0;i<numargs;++i) {
      dump_op(gimple_call_arg(stmt,i));
    }
    json_end_array();
  }
  json_end_object();
}

static void dump_switch(gimple stmt) {

  json_start_object();

  int num_ops = gimple_num_ops(stmt);
  
  json_string_field("type", "switch");
  
  json_field("value");
  dump_op(gimple_op(stmt, 0));
  
  json_array_field("cases");
  
  int default_case = -1;
  
  int i;
  for(i=1;i<num_ops;++i) {
    tree t = gimple_op(stmt, i);

	  basic_block bb = label_to_block (CASE_LABEL (t));
    
    if(!CASE_LOW(t)) {
      // this is the default case
      default_case = bb->index;
      
    } else { 
      // proper case
      json_start_object();
      
      json_int_field("low",
        TREE_INT_CST_LOW(CASE_LOW(t)));
    
      if (CASE_HIGH (t)) {
        json_int_field("high",
          TREE_INT_CST_LOW(CASE_HIGH(t)));
	    } else {
        json_int_field("high",
          TREE_INT_CST_LOW(CASE_LOW(t)));	    
	    }
      json_int_field("basicBlockIndex", bb->index);
	    json_end_object();
    } 
  }
  json_end_array();  
  
  json_field("defaultCase");
  json_start_object();
  json_int_field("basicBlockIndex", default_case);
  json_end_object();
  
  json_end_object();

}

static void dump_statement(basic_block bb, gimple stmt) {
  
  switch(gimple_code(stmt)) {
  case GIMPLE_ASSIGN:
    dump_assignment(stmt);
    break;
  case GIMPLE_CALL:
    dump_call(stmt);
    break;
  case GIMPLE_COND:
    dump_cond(bb, stmt);
    break;
  case GIMPLE_NOP:
    dump_nop(stmt);
    break;
  case GIMPLE_RETURN:
    dump_return(stmt);
    break;
  case OFFSET_TYPE:
    dump_switch(stmt);
    break;
  case BLOCK:
    // this represents a label expression, usually generated
    // in conjunction with a switch statement. We do the 
    // label to basic block translation there, so we don't need
    // these nodes
    break;
  default:
    json_start_object();
    json_string_field("type", 
	tree_code_name[gimple_code(stmt)]);
	  dump_ops(stmt);
	  json_end_object();
  }
  
}


static void dump_argument(tree arg) {
  json_start_object();
  
  json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(arg)));
  json_int_field("id", DEBUG_TEMP_UID (arg));
  
  json_field("type");
  dump_type(TREE_TYPE(arg));
  
  json_end_object();

}

static void dump_arguments(tree decl) {
  
  tree arg = DECL_ARGUMENTS(decl);
  
  if(arg) {
    json_array_field("parameters");
    
    while(arg) {
      dump_argument(arg);
      arg = TREE_CHAIN(arg);
    }  
    
    json_end_array();
  }
}

static void dump_local_decl(tree decl) {

  json_start_object();  
  if(DECL_NAME(decl)) {
    json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(decl)));
  } 

  json_int_field("id", DEBUG_TEMP_UID (decl));
  if(TREE_TYPE(decl)) {
    json_field("type");
    dump_type(TREE_TYPE(decl));
  }
  if(DECL_INITIAL(decl)) {
    json_field("value");
    dump_op(DECL_INITIAL(decl));
  }
  
  json_end_object();
}

static void dump_local_decls(struct function *fun) {
  unsigned ix;
  tree var;
  
  json_array_field("variableDeclarations");

  FOR_EACH_LOCAL_DECL (fun, ix, var)
    {
      dump_local_decl(var);
    }
	json_end_array();
}

static void dump_basic_block(basic_block bb) {

  json_start_object();
  json_int_field("index", bb->index);
  json_array_field("instructions");
      
  gimple_stmt_iterator gsi;
  
  for (gsi = gsi_start_bb (bb); !gsi_end_p (gsi); gsi_next (&gsi))
    {
      dump_statement(bb, gsi_stmt (gsi));
    }
   
  edge e = find_fallthru_edge (bb->succs);

  if (e && e->dest != bb->next_bb)
    {
      json_start_object();
      json_string_field("type", "goto");
      json_int_field("target", e->dest->index);
      json_end_object();
    }
    
  json_end_array();
  json_end_object();
}


static unsigned int dump_function (void)
{
  if(errorcount > 0) {
   return 0;
  }
  
  basic_block bb;
  
  json_start_object();
  json_int_field("id", DEBUG_TEMP_UID (cfun->decl));
  json_string_field("name", IDENTIFIER_POINTER(DECL_NAME(cfun->decl)));
  dump_arguments(cfun->decl);
  dump_local_decls(cfun);
    
  json_array_field("basicBlocks");
  FOR_EACH_BB (bb)
    {
      dump_basic_block(bb);  
    }
  json_end_array();
  
  json_field("returnType");
  dump_type(TREE_TYPE(DECL_RESULT(cfun->decl)));
  
  json_end_object();
 
  return 0;
}

static void dump_type_decl (void *event_data, void *data)
{
  tree type = (tree) event_data;

  json_start_object();
  if(TYPE_NAME(type)) {
    json_string_field("name", IDENTIFIER_POINTER (TYPE_NAME (type)));
  }
  json_string_field("type", tree_code_name[TREE_CODE (type)]);
  json_end_object();
}


static void start_unit_callback (void *gcc_data, void *user_data)
{
  json_start_object();
  json_array_field("functions");
  
}

static void finish_unit_callback (void *gcc_data, void *user_data)
{
  int i;
  json_end_array();

  json_array_field("recordTypes");
  for(i=0;i<record_type_count;++i) {
    dump_record_type_decl(record_types[i]);
  }
  json_end_array();

  json_end_object();
}

static struct gimple_opt_pass dump_functions_pass =
{
    {
      GIMPLE_PASS,
      "json", 	      /* pass name */
      NULL,	          /* gate */
      dump_function,	/* execute */
      NULL,		        /* sub */
      NULL,		        /* next */
      0,		          /* static_pass_number */
      0,		          /* tv_id */
      PROP_cfg,   		/* properties_required */
      0,		          /* properties_provided */
      0,		          /* properties_destroyed */
      0,		          /* todo_flags_start */
      0		            /* todo_flags_finish */
    }
};


/* Plugin initialization.  */

int
plugin_init (struct plugin_name_args *plugin_info,
             struct plugin_gcc_version *version)
{
  struct register_pass_info pass_info;
  const char *plugin_name = plugin_info->base_name;

  pass_info.pass = (struct opt_pass*)(&dump_functions_pass);
  pass_info.reference_pass_name = "cfg";
  pass_info.ref_pass_instance_number = 1;
  pass_info.pos_op = PASS_POS_INSERT_AFTER;
  
  /* find the output file */
  
  json_f = stdout;
  
  int argi;
  for(argi=0;argi!=plugin_info->argc;++argi) {
    if(strcmp(plugin_info->argv[argi].key, "json-output-file") == 0) {
      json_f = fopen(plugin_info->argv[argi].value, "w");
    } 
  }


  /* Register this new pass with GCC */
  register_callback (plugin_name, PLUGIN_PASS_MANAGER_SETUP, NULL,
                     &pass_info);
                     
  //register_callback (plugin_name, PLUGIN_FINISH_TYPE, &dump_type_decl, NULL);

  register_callback ("start_unit", PLUGIN_START_UNIT, &start_unit_callback, NULL);
  register_callback ("finish_unit", PLUGIN_FINISH_UNIT, &finish_unit_callback, NULL);

  
  return 0;
}

