/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
// Initial template generated from Defn.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.sexp.SEXP;

@SuppressWarnings("unused")
public final class Defn {

  private Defn() { }



  public static void Rf_CoercionWarning(int p0) {
    throw new UnimplementedGnuApiMethod("Rf_CoercionWarning");
  }

  public static int Rf_LogicalFromInteger(int p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_LogicalFromInteger");
  }

  public static int Rf_LogicalFromReal(double p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_LogicalFromReal");
  }

  // int Rf_LogicalFromComplex (Rcomplex, int *)

  public static int Rf_IntegerFromLogical(int p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_IntegerFromLogical");
  }

  public static int Rf_IntegerFromReal(double p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_IntegerFromReal");
  }

  // int Rf_IntegerFromComplex (Rcomplex, int *)

  public static double Rf_RealFromLogical(int p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_RealFromLogical");
  }

  public static double Rf_RealFromInteger(int p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_RealFromInteger");
  }

  // double Rf_RealFromComplex (Rcomplex, int *)

  // Rcomplex Rf_ComplexFromLogical (int, int *)

  // Rcomplex Rf_ComplexFromInteger (int, int *)

  // Rcomplex Rf_ComplexFromReal (double, int *)

  public static SEXP SET_CXTAIL(SEXP x, SEXP y) {
    throw new UnimplementedGnuApiMethod("SET_CXTAIL");
  }

  public static void R_ProcessEvents() {
    throw new UnimplementedGnuApiMethod("R_ProcessEvents");
  }

  public static void R_setupHistory() {
    throw new UnimplementedGnuApiMethod("R_setupHistory");
  }

  // int Rf_initEmbeddedR (int argc, char **argv)

  public static void resetTimeLimits() {
    throw new UnimplementedGnuApiMethod("resetTimeLimits");
  }

  public static SEXP R_cmpfun(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_cmpfun");
  }

  public static void R_init_jit_enabled() {
    throw new UnimplementedGnuApiMethod("R_init_jit_enabled");
  }

  public static void R_initAsignSymbols() {
    throw new UnimplementedGnuApiMethod("R_initAsignSymbols");
  }

  // R_stdGen_ptr_t R_set_standardGeneric_ptr (R_stdGen_ptr_t, SEXP)

  public static SEXP R_deferred_default_method() {
    throw new UnimplementedGnuApiMethod("R_deferred_default_method");
  }

  public static SEXP R_set_prim_method(SEXP fname, SEXP op, SEXP code_vec, SEXP fundef, SEXP mlist) {
    throw new UnimplementedGnuApiMethod("R_set_prim_method");
  }

  public static SEXP do_set_prim_method(SEXP op, CharPtr code_string, SEXP fundef, SEXP mlist) {
    throw new UnimplementedGnuApiMethod("do_set_prim_method");
  }

  // void R_set_quick_method_check (R_stdGen_ptr_t)

  public static SEXP R_primitive_methods(SEXP op) {
    throw new UnimplementedGnuApiMethod("R_primitive_methods");
  }

  public static SEXP R_primitive_generic(SEXP op) {
    throw new UnimplementedGnuApiMethod("R_primitive_generic");
  }

  // int R_ReadConsole (const char *, unsigned char *, int, int)

  public static void R_WriteConsole(CharPtr p0, int p1) {
    throw new UnimplementedGnuApiMethod("R_WriteConsole");
  }

  public static void R_WriteConsoleEx(CharPtr p0, int p1, int p2) {
    throw new UnimplementedGnuApiMethod("R_WriteConsoleEx");
  }

  public static void R_ResetConsole() {
    throw new UnimplementedGnuApiMethod("R_ResetConsole");
  }

  public static void R_FlushConsole() {
    throw new UnimplementedGnuApiMethod("R_FlushConsole");
  }

  public static void R_ClearerrConsole() {
    throw new UnimplementedGnuApiMethod("R_ClearerrConsole");
  }

  public static void R_Busy(int p0) {
    throw new UnimplementedGnuApiMethod("R_Busy");
  }

  // int R_ShowFiles (int, const char **, const char **, const char *, Rboolean, const char *)

  // int R_EditFiles (int, const char **, const char **, const char *)

  public static int R_ChooseFile(int p0, CharPtr p1, int p2) {
    throw new UnimplementedGnuApiMethod("R_ChooseFile");
  }

  public static CharPtr R_HomeDir() {
    throw new UnimplementedGnuApiMethod("R_HomeDir");
  }

  public static boolean R_FileExists(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_FileExists");
  }

  public static boolean R_HiddenFile(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_HiddenFile");
  }

  public static double R_FileMtime(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_FileMtime");
  }

  // R_varloc_t R_findVarLocInFrame (SEXP, SEXP)

  // SEXP R_GetVarLocValue (R_varloc_t)

  // SEXP R_GetVarLocSymbol (R_varloc_t)

  // Rboolean R_GetVarLocMISSING (R_varloc_t)

  // void R_SetVarLocValue (R_varloc_t, SEXP)

  public static int Rf_LogicalFromString(SEXP p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_LogicalFromString");
  }

  public static int Rf_IntegerFromString(SEXP p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_IntegerFromString");
  }

  public static double Rf_RealFromString(SEXP p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_RealFromString");
  }

  // Rcomplex Rf_ComplexFromString (SEXP, int *)

  public static SEXP Rf_StringFromLogical(int p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_StringFromLogical");
  }

  public static SEXP Rf_StringFromInteger(int p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_StringFromInteger");
  }

  public static SEXP Rf_StringFromReal(double p0, IntPtr p1) {
    throw new UnimplementedGnuApiMethod("Rf_StringFromReal");
  }

  // SEXP Rf_StringFromComplex (Rcomplex, int *)

  public static SEXP Rf_EnsureString(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_EnsureString");
  }

  // SEXP Rf_allocCharsxp (R_len_t)

  public static SEXP Rf_append(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_append");
  }

  public static /*R_xlen_t*/ int Rf_asVecSize(SEXP x) {
    throw new UnimplementedGnuApiMethod("Rf_asVecSize");
  }

  public static void Rf_check1arg(SEXP p0, SEXP p1, CharPtr p2) {
    throw new UnimplementedGnuApiMethod("Rf_check1arg");
  }

  public static void Rf_checkArityCall(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_checkArityCall");
  }

  public static void Rf_CheckFormals(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_CheckFormals");
  }

  public static void R_check_locale() {
    throw new UnimplementedGnuApiMethod("R_check_locale");
  }

  public static void Rf_check_stack_balance(SEXP op, int save) {
    throw new UnimplementedGnuApiMethod("Rf_check_stack_balance");
  }

  public static void Rf_CleanEd() {
    throw new UnimplementedGnuApiMethod("Rf_CleanEd");
  }

  public static void Rf_copyMostAttribNoTs(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_copyMostAttribNoTs");
  }

  public static SEXP Rf_createS3Vars(SEXP p0, SEXP p1, SEXP p2, SEXP p3, SEXP p4, SEXP p5) {
    throw new UnimplementedGnuApiMethod("Rf_createS3Vars");
  }

  public static void Rf_CustomPrintValue(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_CustomPrintValue");
  }

  public static double Rf_currentTime() {
    throw new UnimplementedGnuApiMethod("Rf_currentTime");
  }

  public static void Rf_DataFrameClass(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_DataFrameClass");
  }

  public static SEXP Rf_ddfindVar(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_ddfindVar");
  }

  public static SEXP Rf_deparse1(SEXP p0, boolean p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_deparse1");
  }

  public static SEXP Rf_deparse1w(SEXP p0, boolean p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_deparse1w");
  }

  public static SEXP Rf_deparse1line(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("Rf_deparse1line");
  }

  public static SEXP Rf_deparse1s(SEXP call) {
    throw new UnimplementedGnuApiMethod("Rf_deparse1s");
  }

  // int Rf_DispatchAnyOrEval (SEXP, SEXP, const char *, SEXP, SEXP, SEXP *, int, int)

  // int Rf_DispatchOrEval (SEXP, SEXP, const char *, SEXP, SEXP, SEXP *, int, int)

  // int Rf_DispatchGroup (const char *, SEXP, SEXP, SEXP, SEXP, SEXP *)

  public static SEXP duplicated(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("duplicated");
  }

  public static /*R_xlen_t*/ int any_duplicated(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("any_duplicated");
  }

  public static /*R_xlen_t*/ int any_duplicated3(SEXP p0, SEXP p1, boolean p2) {
    throw new UnimplementedGnuApiMethod("any_duplicated3");
  }

  public static int Rf_envlength(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_envlength");
  }

  public static SEXP Rf_evalList(SEXP p0, SEXP p1, SEXP p2, int p3) {
    throw new UnimplementedGnuApiMethod("Rf_evalList");
  }

  public static SEXP Rf_evalListKeepMissing(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_evalListKeepMissing");
  }

  public static int Rf_factorsConform(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_factorsConform");
  }

  public static void /*NORET*/ Rf_findcontext(int p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_findcontext");
  }

  public static SEXP Rf_findVar1(SEXP p0, SEXP p1, /*SEXPTYPE*/ int p2, int p3) {
    throw new UnimplementedGnuApiMethod("Rf_findVar1");
  }

  public static void Rf_FrameClassFix(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_FrameClassFix");
  }

  public static SEXP Rf_frameSubscript(int p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_frameSubscript");
  }

  public static /*R_xlen_t*/ int Rf_get1index(SEXP p0, SEXP p1, /*R_xlen_t*/ int p2, int p3, int p4, SEXP p5) {
    throw new UnimplementedGnuApiMethod("Rf_get1index");
  }

  public static int Rf_GetOptionCutoff() {
    throw new UnimplementedGnuApiMethod("Rf_GetOptionCutoff");
  }

  public static SEXP Rf_getVar(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_getVar");
  }

  public static SEXP Rf_getVarInFrame(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_getVarInFrame");
  }

  public static void Rf_InitArithmetic() {
    throw new UnimplementedGnuApiMethod("Rf_InitArithmetic");
  }

  public static void Rf_InitConnections() {
    throw new UnimplementedGnuApiMethod("Rf_InitConnections");
  }

  public static void Rf_InitEd() {
    throw new UnimplementedGnuApiMethod("Rf_InitEd");
  }

  public static void Rf_InitFunctionHashing() {
    throw new UnimplementedGnuApiMethod("Rf_InitFunctionHashing");
  }

  public static void Rf_InitBaseEnv() {
    throw new UnimplementedGnuApiMethod("Rf_InitBaseEnv");
  }

  public static void Rf_InitGlobalEnv() {
    throw new UnimplementedGnuApiMethod("Rf_InitGlobalEnv");
  }

  public static boolean R_current_trace_state() {
    throw new UnimplementedGnuApiMethod("R_current_trace_state");
  }

  public static boolean R_current_debug_state() {
    throw new UnimplementedGnuApiMethod("R_current_debug_state");
  }

  public static boolean R_has_methods(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_has_methods");
  }

  public static void R_InitialData() {
    throw new UnimplementedGnuApiMethod("R_InitialData");
  }

  public static SEXP R_possible_dispatch(SEXP p0, SEXP p1, SEXP p2, SEXP p3, boolean p4) {
    throw new UnimplementedGnuApiMethod("R_possible_dispatch");
  }

  public static void Rf_InitGraphics() {
    throw new UnimplementedGnuApiMethod("Rf_InitGraphics");
  }

  public static void Rf_InitMemory() {
    throw new UnimplementedGnuApiMethod("Rf_InitMemory");
  }

  public static void Rf_InitNames() {
    throw new UnimplementedGnuApiMethod("Rf_InitNames");
  }

  public static void Rf_InitOptions() {
    throw new UnimplementedGnuApiMethod("Rf_InitOptions");
  }

  public static void Rf_InitStringHash() {
    throw new UnimplementedGnuApiMethod("Rf_InitStringHash");
  }

  public static void Init_R_Variables(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Init_R_Variables");
  }

  public static void Rf_InitTempDir() {
    throw new UnimplementedGnuApiMethod("Rf_InitTempDir");
  }

  public static void Rf_InitTypeTables() {
    throw new UnimplementedGnuApiMethod("Rf_InitTypeTables");
  }

  public static void Rf_initStack() {
    throw new UnimplementedGnuApiMethod("Rf_initStack");
  }

  public static void Rf_InitS3DefaultTypes() {
    throw new UnimplementedGnuApiMethod("Rf_InitS3DefaultTypes");
  }

  public static void Rf_internalTypeCheck(SEXP p0, SEXP p1, /*SEXPTYPE*/ int p2) {
    throw new UnimplementedGnuApiMethod("Rf_internalTypeCheck");
  }

  public static boolean isMethodsDispatchOn() {
    throw new UnimplementedGnuApiMethod("isMethodsDispatchOn");
  }

  public static int Rf_isValidName(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_isValidName");
  }

  public static void /*NORET*/ Rf_jump_to_toplevel() {
    throw new UnimplementedGnuApiMethod("Rf_jump_to_toplevel");
  }

  public static void Rf_KillAllDevices() {
    throw new UnimplementedGnuApiMethod("Rf_KillAllDevices");
  }

  public static SEXP Rf_levelsgets(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_levelsgets");
  }

  public static void Rf_mainloop() {
    throw new UnimplementedGnuApiMethod("Rf_mainloop");
  }

  // SEXP Rf_makeSubscript (SEXP, SEXP, R_xlen_t *, SEXP)

  public static SEXP Rf_markKnown(CharPtr p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_markKnown");
  }

  public static SEXP Rf_mat2indsub(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_mat2indsub");
  }

  // SEXP Rf_matchArg (SEXP, SEXP *)

  // SEXP Rf_matchArgExact (SEXP, SEXP *)

  public static SEXP Rf_matchArgs(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_matchArgs");
  }

  // SEXP Rf_matchPar (const char *, SEXP *)

  public static void memtrace_report(Object p0, Object p1) {
    throw new UnimplementedGnuApiMethod("memtrace_report");
  }

  public static SEXP Rf_mkCLOSXP(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_mkCLOSXP");
  }

  public static SEXP Rf_mkFalse() {
    throw new UnimplementedGnuApiMethod("Rf_mkFalse");
  }

  public static SEXP mkPRIMSXP(int p0, int p1) {
    throw new UnimplementedGnuApiMethod("mkPRIMSXP");
  }

  public static SEXP Rf_mkPROMISE(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_mkPROMISE");
  }

  public static SEXP R_mkEVPROMISE(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("R_mkEVPROMISE");
  }

  public static SEXP R_mkEVPROMISE_NR(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("R_mkEVPROMISE_NR");
  }

  public static SEXP Rf_mkQUOTE(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_mkQUOTE");
  }

  public static SEXP Rf_mkSYMSXP(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_mkSYMSXP");
  }

  public static SEXP Rf_mkTrue() {
    throw new UnimplementedGnuApiMethod("Rf_mkTrue");
  }

  public static SEXP Rf_NewEnvironment(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_NewEnvironment");
  }

  public static void Rf_onintr() {
    throw new UnimplementedGnuApiMethod("Rf_onintr");
  }

  // RETSIGTYPE Rf_onsigusr1 (int)

  // RETSIGTYPE Rf_onsigusr2 (int)

  // R_xlen_t Rf_OneIndex (SEXP, SEXP, R_xlen_t, int, SEXP *, int, SEXP)

  // SEXP Rf_parse (FILE *, int)

  public static SEXP Rf_patchArgsByActuals(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_patchArgsByActuals");
  }

  public static void Rf_PrintDefaults() {
    throw new UnimplementedGnuApiMethod("Rf_PrintDefaults");
  }

  public static void Rf_PrintGreeting() {
    throw new UnimplementedGnuApiMethod("Rf_PrintGreeting");
  }

  public static void Rf_PrintValueEnv(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_PrintValueEnv");
  }

  public static void Rf_PrintValueRec(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_PrintValueRec");
  }

  public static void Rf_PrintVersion(CharPtr p0, /*size_t*/ int len) {
    throw new UnimplementedGnuApiMethod("Rf_PrintVersion");
  }

  public static void Rf_PrintVersion_part_1(CharPtr p0, /*size_t*/ int len) {
    throw new UnimplementedGnuApiMethod("Rf_PrintVersion_part_1");
  }

  public static void Rf_PrintVersionString(CharPtr p0, /*size_t*/ int len) {
    throw new UnimplementedGnuApiMethod("Rf_PrintVersionString");
  }

  public static void Rf_PrintWarnings() {
    throw new UnimplementedGnuApiMethod("Rf_PrintWarnings");
  }

  public static void process_site_Renviron() {
    throw new UnimplementedGnuApiMethod("process_site_Renviron");
  }

  public static void process_system_Renviron() {
    throw new UnimplementedGnuApiMethod("process_system_Renviron");
  }

  public static void process_user_Renviron() {
    throw new UnimplementedGnuApiMethod("process_user_Renviron");
  }

  public static SEXP Rf_promiseArgs(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_promiseArgs");
  }

  // void Rcons_vprintf (const char *, va_list)

  public static SEXP R_data_class(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("R_data_class");
  }

  public static SEXP R_data_class2(SEXP p0) {
    throw new UnimplementedGnuApiMethod("R_data_class2");
  }

  public static CharPtr R_LibraryFileName(CharPtr p0, CharPtr p1, /*size_t*/ int p2) {
    throw new UnimplementedGnuApiMethod("R_LibraryFileName");
  }

  // SEXP R_LoadFromFile (FILE *, int)

  public static SEXP R_NewHashedEnv(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("R_NewHashedEnv");
  }

  public static int R_Newhashpjw(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_Newhashpjw");
  }

  // FILE* R_OpenLibraryFile (const char *)

  public static SEXP R_Primitive(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_Primitive");
  }

  public static void R_RestoreGlobalEnv() {
    throw new UnimplementedGnuApiMethod("R_RestoreGlobalEnv");
  }

  public static void R_RestoreGlobalEnvFromFile(CharPtr p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("R_RestoreGlobalEnvFromFile");
  }

  public static void R_SaveGlobalEnv() {
    throw new UnimplementedGnuApiMethod("R_SaveGlobalEnv");
  }

  public static void R_SaveGlobalEnvToFile(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_SaveGlobalEnvToFile");
  }

  // void R_SaveToFile (SEXP, FILE *, int)

  // void R_SaveToFileV (SEXP, FILE *, int, int)

  public static boolean R_seemsOldStyleS4Object(SEXP object) {
    throw new UnimplementedGnuApiMethod("R_seemsOldStyleS4Object");
  }

  public static int R_SetOptionWarn(int p0) {
    throw new UnimplementedGnuApiMethod("R_SetOptionWarn");
  }

  public static int R_SetOptionWidth(int p0) {
    throw new UnimplementedGnuApiMethod("R_SetOptionWidth");
  }

  public static void R_Suicide(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("R_Suicide");
  }

  public static void R_getProcTime(DoublePtr data) {
    throw new UnimplementedGnuApiMethod("R_getProcTime");
  }

  public static int R_isMissing(SEXP symbol, SEXP rho) {
    throw new UnimplementedGnuApiMethod("R_isMissing");
  }

  public static CharPtr Rf_sexptype2char(/*SEXPTYPE*/ int type) {
    throw new UnimplementedGnuApiMethod("Rf_sexptype2char");
  }

  public static void Rf_sortVector(SEXP p0, boolean p1) {
    throw new UnimplementedGnuApiMethod("Rf_sortVector");
  }

  public static void Rf_SrcrefPrompt(CharPtr p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_SrcrefPrompt");
  }

  // void Rf_ssort (SEXP *, int)

  public static int Rf_StrToInternal(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_StrToInternal");
  }

  public static SEXP Rf_strmat2intmat(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("Rf_strmat2intmat");
  }

  public static SEXP Rf_substituteList(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_substituteList");
  }

  // unsigned int Rf_TimeToSeed (void)

  public static boolean Rf_tsConform(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_tsConform");
  }

  public static SEXP Rf_tspgets(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_tspgets");
  }

  public static SEXP Rf_type2symbol(/*SEXPTYPE*/ int p0) {
    throw new UnimplementedGnuApiMethod("Rf_type2symbol");
  }

  public static void Rf_unbindVar(SEXP p0, SEXP p1) {
    throw new UnimplementedGnuApiMethod("Rf_unbindVar");
  }

  public static SEXP R_LookupMethod(SEXP p0, SEXP p1, SEXP p2, SEXP p3) {
    throw new UnimplementedGnuApiMethod("R_LookupMethod");
  }

  // int Rf_usemethod (const char *, SEXP, SEXP, SEXP, SEXP, SEXP, SEXP, SEXP *)

  public static SEXP Rf_vectorIndex(SEXP p0, SEXP p1, int p2, int p3, int p4, SEXP p5, boolean p6) {
    throw new UnimplementedGnuApiMethod("Rf_vectorIndex");
  }

  public static SEXP Rf_ItemName(SEXP p0, /*R_xlen_t*/ int p1) {
    throw new UnimplementedGnuApiMethod("Rf_ItemName");
  }

  // void NORET Rf_ErrorMessage (SEXP, int,...)

  // void Rf_WarningMessage (SEXP, R_WARNING,...)

  public static SEXP R_GetTraceback(int p0) {
    throw new UnimplementedGnuApiMethod("R_GetTraceback");
  }

  public static /*R_size_t*/ int R_GetMaxVSize() {
    throw new UnimplementedGnuApiMethod("R_GetMaxVSize");
  }

  public static void R_SetMaxVSize(/*R_size_t*/ int p0) {
    throw new UnimplementedGnuApiMethod("R_SetMaxVSize");
  }

  public static /*R_size_t*/ int R_GetMaxNSize() {
    throw new UnimplementedGnuApiMethod("R_GetMaxNSize");
  }

  public static void R_SetMaxNSize(/*R_size_t*/ int p0) {
    throw new UnimplementedGnuApiMethod("R_SetMaxNSize");
  }

  public static /*R_size_t*/ int R_Decode2Long(CharPtr p, IntPtr ierr) {
    throw new UnimplementedGnuApiMethod("R_Decode2Long");
  }

  public static void R_SetPPSize(/*R_size_t*/ int p0) {
    throw new UnimplementedGnuApiMethod("R_SetPPSize");
  }

  public static int Rstrlen(SEXP p0, int p1) {
    throw new UnimplementedGnuApiMethod("Rstrlen");
  }

  // const char* Rf_EncodeRaw (Rbyte, const char *)

  // const char* Rf_EncodeString (SEXP, int, int, Rprt_adj)

  public static CharPtr Rf_EncodeReal2(double p0, int p1, int p2, int p3) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeReal2");
  }

  public static CharPtr Rf_EncodeChar(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_EncodeChar");
  }

  public static void orderVector1(IntPtr indx, int n, SEXP key, boolean nalast, boolean decreasing, SEXP rho) {
    throw new UnimplementedGnuApiMethod("orderVector1");
  }

  public static SEXP R_subset3_dflt(SEXP p0, SEXP p1, SEXP p2) {
    throw new UnimplementedGnuApiMethod("R_subset3_dflt");
  }

  public static SEXP R_subassign3_dflt(SEXP p0, SEXP p1, SEXP p2, SEXP p3) {
    throw new UnimplementedGnuApiMethod("R_subassign3_dflt");
  }

  public static void /*NORET*/ UNIMPLEMENTED_TYPE(CharPtr s, SEXP x) {
    throw new UnimplementedGnuApiMethod("UNIMPLEMENTED_TYPE");
  }

  public static void /*NORET*/ UNIMPLEMENTED_TYPEt(CharPtr s, /*SEXPTYPE*/ int t) {
    throw new UnimplementedGnuApiMethod("UNIMPLEMENTED_TYPEt");
  }

  public static boolean Rf_strIsASCII(CharPtr str) {
    throw new UnimplementedGnuApiMethod("Rf_strIsASCII");
  }

  // int utf8clen (char c)

  public static int Rf_AdobeSymbol2ucs2(int n) {
    throw new UnimplementedGnuApiMethod("Rf_AdobeSymbol2ucs2");
  }

  // double R_strtod5 (const char *str, char **endptr, char dec, Rboolean NA, int exact)

  // size_t mbcsToUcs2 (const char *in, ucs2_t *out, int nout, int enc)

  // size_t Rf_utf8toucs (wchar_t *wc, const char *s)

  // size_t Rf_utf8towcs (wchar_t *wc, const char *s, size_t n)

  // size_t Rf_ucstomb (char *s, const unsigned int wc)

  // size_t Rf_ucstoutf8 (char *s, const unsigned int wc)

  // size_t Rf_mbtoucs (unsigned int *wc, const char *s, size_t n)

  // size_t Rf_wcstoutf8 (char *s, const wchar_t *wc, size_t n)

  public static SEXP Rf_installTrChar(SEXP p0) {
    throw new UnimplementedGnuApiMethod("Rf_installTrChar");
  }

  // const wchar_t* Rf_wtransChar (SEXP x)

  // size_t Rf_mbrtowc (wchar_t *wc, const char *s, size_t n, mbstate_t *ps)

  public static boolean mbcsValid(CharPtr str) {
    throw new UnimplementedGnuApiMethod("mbcsValid");
  }

  public static boolean utf8Valid(CharPtr str) {
    throw new UnimplementedGnuApiMethod("utf8Valid");
  }

  public static CharPtr Rf_strchr(CharPtr s, int c) {
    throw new UnimplementedGnuApiMethod("Rf_strchr");
  }

  public static CharPtr Rf_strrchr(CharPtr s, int c) {
    throw new UnimplementedGnuApiMethod("Rf_strrchr");
  }

  public static SEXP fixup_NaRm(SEXP args) {
    throw new UnimplementedGnuApiMethod("fixup_NaRm");
  }

  public static void invalidate_cached_recodings() {
    throw new UnimplementedGnuApiMethod("invalidate_cached_recodings");
  }

  public static void resetICUcollator() {
    throw new UnimplementedGnuApiMethod("resetICUcollator");
  }

  public static void dt_invalidate_locale() {
    throw new UnimplementedGnuApiMethod("dt_invalidate_locale");
  }

  // void get_current_mem (size_t *, size_t *, size_t *)

  // unsigned long get_duplicate_counter (void)

  public static void reset_duplicate_counter() {
    throw new UnimplementedGnuApiMethod("reset_duplicate_counter");
  }

  public static void Rf_BindDomain(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_BindDomain");
  }

  public static double R_getClockIncrement() {
    throw new UnimplementedGnuApiMethod("R_getClockIncrement");
  }

  public static void InitDynload() {
    throw new UnimplementedGnuApiMethod("InitDynload");
  }

  public static void R_CleanTempDir() {
    throw new UnimplementedGnuApiMethod("R_CleanTempDir");
  }

  // FILE* RC_fopen (const SEXP fn, const char *mode, const Rboolean expand)

  public static int Rf_Seql(SEXP a, SEXP b) {
    throw new UnimplementedGnuApiMethod("Rf_Seql");
  }

  public static int Rf_Scollate(SEXP a, SEXP b) {
    throw new UnimplementedGnuApiMethod("Rf_Scollate");
  }

  // double R_strtod4 (const char *str, char **endptr, char dec, Rboolean NA)

  // double R_strtod (const char *str, char **endptr)

  public static double R_atof(CharPtr str) {
    throw new UnimplementedGnuApiMethod("R_atof");
  }

  public static void set_rl_word_breaks(CharPtr str) {
    throw new UnimplementedGnuApiMethod("set_rl_word_breaks");
  }

  public static CharPtr locale2charset(CharPtr p0) {
    throw new UnimplementedGnuApiMethod("locale2charset");
  }
  
}
