/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from Internal.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.primitives.Native;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;

@SuppressWarnings({
    "unused",
    "squid:S1172", /* Unused method parameters */
    "squid:S00100" /* Method naming convention */
})
public final class Internal {

  private Internal() { }

  private static SEXP invokeInternal(String name, SEXP call, SEXP op, SEXP args, SEXP env) {
    return Primitives.getPrimitive(Symbol.get(name)).apply(Native.currentContext(), (Environment)env, (FunctionCall)call, (PairList)args);
  }

  public static SEXP do_X11(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_X11");
  }

  public static SEXP do_abbrev(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_abbrev");
  }

  public static SEXP do_abs(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_abs");
  }

  public static SEXP do_addCondHands(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_addCondHands");
  }

  public static SEXP do_address(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_address");
  }

  public static SEXP do_addRestart(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_addRestart");
  }

  public static SEXP do_addTryHandlers(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_addTryHandlers");
  }

  public static SEXP do_adist(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_adist");
  }

  public static SEXP do_agrep(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_agrep");
  }

  public static SEXP do_allnames(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_allnames");
  }

  public static SEXP do_anyNA(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_anyNA");
  }

  public static SEXP do_aperm(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_aperm");
  }

  public static SEXP do_aregexec(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_aregexec");
  }

  public static SEXP do_args(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_args");
  }

  public static SEXP do_arith(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_arith");
  }

  public static SEXP do_array(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_array");
  }

  public static SEXP do_asPOSIXct(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_asPOSIXct");
  }

  public static SEXP do_asPOSIXlt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_asPOSIXlt");
  }

  public static SEXP do_ascall(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_ascall");
  }

  public static SEXP do_as_environment(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_as_environment");
  }

  public static SEXP do_asatomic(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_asatomic");
  }

  public static SEXP do_asfunction(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_asfunction");
  }

  public static SEXP do_asmatrixdf(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_asmatrixdf");
  }

  public static SEXP do_assign(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_assign");
  }

  public static SEXP do_asvector(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_asvector");
  }

  public static SEXP do_AT(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_AT");
  }

  public static SEXP do_attach(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_attach");
  }

  public static SEXP do_attr(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_attr");
  }

  public static SEXP do_attrgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_attrgets");
  }

  public static SEXP do_attributes(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_attributes");
  }

  public static SEXP do_attributesgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_attributesgets");
  }

  public static SEXP do_backsolve(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_backsolve");
  }

  public static SEXP do_baseenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_baseenv");
  }

  public static SEXP do_basename(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_basename");
  }

  public static SEXP do_bcprofcounts(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bcprofcounts");
  }

  public static SEXP do_bcprofstart(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bcprofstart");
  }

  public static SEXP do_bcprofstop(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bcprofstop");
  }

  public static SEXP do_begin(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_begin");
  }

  public static SEXP do_bincode(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bincode");
  }

  public static SEXP do_bind(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bind");
  }

  public static SEXP do_bindtextdomain(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bindtextdomain");
  }

  public static SEXP do_bitwise(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bitwise");
  }

  public static SEXP do_body(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_body");
  }

  public static SEXP do_bodyCode(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bodyCode");
  }

  public static SEXP /*NORET*/ do_break(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_break");
  }

  public static SEXP do_browser(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_browser");
  }

  public static SEXP do_builtins(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_builtins");
  }

  public static SEXP do_c(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_c");
  }

  public static SEXP do_c_dflt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_c_dflt");
  }

  public static SEXP do_call(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_call");
  }

  public static SEXP do_capabilities(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_capabilities");
  }

  public static SEXP do_capabilitiesX11(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_capabilitiesX11");
  }

  public static SEXP do_cat(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_cat");
  }

  public static SEXP do_charmatch(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_charmatch");
  }

  public static SEXP do_charToRaw(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_charToRaw");
  }

  public static SEXP do_chartr(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_chartr");
  }

  public static SEXP do_class(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_class");
  }

  public static SEXP do_classgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_classgets");
  }

  public static SEXP do_colon(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_colon");
  }

  public static SEXP do_colsum(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_colsum");
  }

  public static SEXP do_commandArgs(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_commandArgs");
  }

  public static SEXP do_comment(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_comment");
  }

  public static SEXP do_commentgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_commentgets");
  }

  public static SEXP do_complex(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_complex");
  }

  public static SEXP do_contourLines(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_contourLines");
  }

  public static SEXP do_copyDFattr(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_copyDFattr");
  }

  public static SEXP do_crc64(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_crc64");
  }

  public static SEXP do_Cstack_info(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_Cstack_info");
  }

  public static SEXP do_cum(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_cum");
  }

  public static SEXP do_curlDownload(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_curlDownload");
  }

  public static SEXP do_curlGetHeaders(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_curlGetHeaders");
  }

  public static SEXP do_curlVersion(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_curlVersion");
  }

  public static SEXP do_D2POSIXlt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_D2POSIXlt");
  }

  public static SEXP do_dataentry(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dataentry");
  }

  public static SEXP do_dataframe(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dataframe");
  }

  public static SEXP do_dataviewer(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dataviewer");
  }

  public static SEXP do_date(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_date");
  }

  public static SEXP do_debug(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_debug");
  }

  public static SEXP do_devAskNewPage(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_devAskNewPage");
  }

  public static SEXP do_delayed(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_delayed");
  }

  public static SEXP do_deparse(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_deparse");
  }

  public static SEXP do_detach(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_detach");
  }

  public static SEXP /*NORET*/ do_dfltStop(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dfltStop");
  }

  public static SEXP do_dfltWarn(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dfltWarn");
  }

  public static SEXP do_diag(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_diag");
  }

  public static SEXP do_dim(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dim");
  }

  public static SEXP do_dimgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dimgets");
  }

  public static SEXP do_dimnames(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dimnames");
  }

  public static SEXP do_dimnamesgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dimnamesgets");
  }

  public static SEXP do_dircreate(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dircreate");
  }

  public static SEXP do_direxists(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_direxists");
  }

  public static SEXP do_dirname(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dirname");
  }

  public static SEXP do_docall(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_docall");
  }

  public static SEXP do_dotcall(SEXP call, SEXP op, SEXP args, SEXP env) {
    return invokeInternal(".Call", call, op, args, env);
  }

  public static SEXP do_dotCode(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dotCode");
  }

  public static SEXP do_dput(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dput");
  }

  public static SEXP do_drop(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_drop");
  }

  public static SEXP do_dump(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dump");
  }

  public static SEXP do_duplicated(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_duplicated");
  }

  public static SEXP do_dynload(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dynload");
  }

  public static SEXP do_dynunload(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_dynunload");
  }

  public static SEXP do_eapply(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_eapply");
  }

  public static SEXP do_edit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_edit");
  }

  public static SEXP do_emptyenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_emptyenv");
  }

  public static SEXP do_encoding(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_encoding");
  }

  public static SEXP do_encodeString(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_encodeString");
  }

  public static SEXP do_enc2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_enc2");
  }

  public static SEXP do_envir(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_envir");
  }

  public static SEXP do_envirgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_envirgets");
  }

  public static SEXP do_envirName(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_envirName");
  }

  public static SEXP do_env2list(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_env2list");
  }

  public static SEXP do_eSoftVersion(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_eSoftVersion");
  }

  public static SEXP do_External(SEXP call, SEXP op, SEXP args, SEXP env) {
    return invokeInternal(".External", call, op, args, env);
  }

  public static SEXP do_eval(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_eval");
  }

  public static SEXP do_expression(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_expression");
  }

  public static SEXP do_fileaccess(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileaccess");
  }

  public static SEXP do_fileappend(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileappend");
  }

  public static SEXP do_filechoose(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filechoose");
  }

  public static SEXP do_filecopy(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filecopy");
  }

  public static SEXP do_filecreate(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filecreate");
  }

  public static SEXP do_fileedit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileedit");
  }

  public static SEXP do_fileexists(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileexists");
  }

  public static SEXP do_fileinfo(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileinfo");
  }

  public static SEXP do_filelink(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filelink");
  }

  public static SEXP do_filepath(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filepath");
  }

  public static SEXP do_fileremove(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileremove");
  }

  public static SEXP do_filerename(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filerename");
  }

  public static SEXP do_fileshow(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fileshow");
  }

  public static SEXP do_filesymlink(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_filesymlink");
  }

  public static SEXP do_findinterval(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_findinterval");
  }

  public static SEXP do_first_min(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_first_min");
  }

  public static SEXP do_flush(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_flush");
  }

  public static SEXP do_flushconsole(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_flushconsole");
  }

  public static SEXP do_for(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_for");
  }

  public static SEXP do_forceAndCall(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_forceAndCall");
  }

  public static SEXP do_format(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_format");
  }

  public static SEXP do_formatC(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_formatC");
  }

  public static SEXP do_formatinfo(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_formatinfo");
  }

  public static SEXP do_formatPOSIXlt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_formatPOSIXlt");
  }

  public static SEXP do_formals(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_formals");
  }

  public static SEXP do_function(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_function");
  }

  public static SEXP do_gc(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gc");
  }

  public static SEXP do_gcinfo(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gcinfo");
  }

  public static SEXP do_gctime(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gctime");
  }

  public static SEXP do_gctorture(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gctorture");
  }

  public static SEXP do_gctorture2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gctorture2");
  }

  public static SEXP do_get(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_get");
  }

  public static SEXP do_getDllTable(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getDllTable");
  }

  public static SEXP do_getVarsFromFrame(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getVarsFromFrame");
  }

  public static SEXP do_getenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getenv");
  }

  public static SEXP do_geterrmessage(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_geterrmessage");
  }

  public static SEXP do_getGraphicsEvent(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getGraphicsEvent");
  }

  public static SEXP do_getGraphicsEventEnv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getGraphicsEventEnv");
  }

  public static SEXP do_getlocale(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getlocale");
  }

  public static SEXP do_getOption(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getOption");
  }

  public static SEXP do_getRegisteredRoutines(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getRegisteredRoutines");
  }

  public static SEXP do_getSymbolInfo(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getSymbolInfo");
  }

  public static SEXP do_getRestart(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getRestart");
  }

  public static SEXP do_gettext(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gettext");
  }

  public static SEXP do_getwd(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getwd");
  }

  public static SEXP do_glob(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_glob");
  }

  public static SEXP do_globalenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_globalenv");
  }

  public static SEXP do_gray(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gray");
  }

  public static SEXP do_grep(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_grep");
  }

  public static SEXP do_grepraw(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_grepraw");
  }

  public static SEXP do_gsub(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gsub");
  }

  public static SEXP do_hsv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_hsv");
  }

  public static SEXP do_hcl(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_hcl");
  }

  public static SEXP do_iconv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_iconv");
  }

  public static SEXP do_ICUget(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_ICUget");
  }

  public static SEXP do_ICUset(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_ICUset");
  }

  public static SEXP do_identical(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_identical");
  }

  public static SEXP do_if(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_if");
  }

  public static SEXP do_inherits(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_inherits");
  }

  public static SEXP do_inspect(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_inspect");
  }

  public static SEXP do_intToUtf8(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_intToUtf8");
  }

  public static SEXP do_interactive(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_interactive");
  }

  public static SEXP do_internal(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_internal");
  }

  public static SEXP do_interruptsSuspended(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_interruptsSuspended");
  }

  public static SEXP do_intToBits(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_intToBits");
  }

  public static SEXP do_invisible(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_invisible");
  }

  public static SEXP /*NORET*/ do_invokeRestart(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_invokeRestart");
  }

  public static SEXP do_is(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_is");
  }

  public static SEXP do_isatty(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isatty");
  }

  public static SEXP do_isfinite(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isfinite");
  }

  public static SEXP do_isinfinite(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isinfinite");
  }

  public static SEXP do_islistfactor(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_islistfactor");
  }

  public static SEXP do_isloaded(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isloaded");
  }

  public static SEXP do_isna(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isna");
  }

  public static SEXP do_isnan(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isnan");
  }

  public static SEXP do_isunsorted(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isunsorted");
  }

  public static SEXP do_isvector(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isvector");
  }

  public static SEXP do_lapack(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lapack");
  }

  public static SEXP do_lapply(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lapply");
  }

  public static SEXP do_lazyLoadDBfetch(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lazyLoadDBfetch");
  }

  public static SEXP do_lazyLoadDBflush(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lazyLoadDBflush");
  }

  public static SEXP do_lazyLoadDBinsertValue(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lazyLoadDBinsertValue");
  }

  public static SEXP do_length(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_length");
  }

  public static SEXP do_lengthgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lengthgets");
  }

  public static SEXP do_lengths(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lengths");
  }

  public static SEXP do_levelsgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_levelsgets");
  }

  public static SEXP do_listdirs(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_listdirs");
  }

  public static SEXP do_listfiles(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_listfiles");
  }

  public static SEXP do_list2env(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_list2env");
  }

  public static SEXP do_load(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_load");
  }

  public static SEXP do_loadFromConn2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_loadFromConn2");
  }

  public static SEXP do_localeconv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_localeconv");
  }

  public static SEXP do_log(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_log");
  }

  public static SEXP do_log1arg(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_log1arg");
  }

  public static SEXP do_logic(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_logic");
  }

  public static SEXP do_logic2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_logic2");
  }

  public static SEXP do_logic3(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_logic3");
  }

  public static SEXP do_ls(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_ls");
  }

  public static SEXP do_l10n_info(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_l10n_info");
  }

  public static SEXP do_machine(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_machine");
  }

  public static SEXP do_makelazy(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_makelazy");
  }

  public static SEXP do_makelist(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_makelist");
  }

  public static SEXP do_makenames(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_makenames");
  }

  public static SEXP do_makeunique(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_makeunique");
  }

  public static SEXP do_makevector(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_makevector");
  }

  public static SEXP do_mapply(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_mapply");
  }

  public static SEXP do_match(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_match");
  }

  public static SEXP do_matchcall(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_matchcall");
  }

  public static SEXP do_matprod(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_matprod");
  }

  public static SEXP do_Math2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_Math2");
  }

  public static SEXP do_matrix(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_matrix");
  }

  public static SEXP do_maxcol(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_maxcol");
  }

  public static SEXP do_memlimits(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_memlimits");
  }

  public static SEXP do_memoryprofile(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_memoryprofile");
  }

  public static SEXP do_merge(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_merge");
  }

  public static SEXP do_mget(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_mget");
  }

  public static SEXP do_missing(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_missing");
  }

  public static SEXP do_names(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_names");
  }

  public static SEXP do_namesgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_namesgets");
  }

  public static SEXP do_nargs(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_nargs");
  }

  public static SEXP do_nchar(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_nchar");
  }

  public static SEXP do_newenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_newenv");
  }

  public static SEXP do_nextmethod(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_nextmethod");
  }

  public static SEXP do_ngettext(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_ngettext");
  }

  public static SEXP do_normalizepath(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_normalizepath");
  }

  public static SEXP do_nzchar(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_nzchar");
  }

  public static SEXP do_onexit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_onexit");
  }

  public static SEXP do_options(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_options");
  }

  public static SEXP do_order(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_order");
  }

  public static SEXP do_pack(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pack");
  }

  public static SEXP do_packBits(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_packBits");
  }

  public static SEXP do_paren(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_paren");
  }

  public static SEXP do_parentenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_parentenv");
  }

  public static SEXP do_parentenvgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_parentenvgets");
  }

  public static SEXP do_parentframe(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_parentframe");
  }

  public static SEXP do_parse(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_parse");
  }

  public static SEXP do_paste(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_paste");
  }

  public static SEXP do_pathexpand(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pathexpand");
  }

  public static SEXP do_pcre_config(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pcre_config");
  }

  public static SEXP do_pmatch(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pmatch");
  }

  public static SEXP do_pmin(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pmin");
  }

  public static SEXP do_polyroot(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_polyroot");
  }

  public static SEXP do_pos2env(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pos2env");
  }

  public static SEXP do_POSIXlt2D(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_POSIXlt2D");
  }

  public static SEXP do_pretty(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pretty");
  }

  public static SEXP do_primitive(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_primitive");
  }

  public static SEXP do_printdefault(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_printdefault");
  }

  public static SEXP do_printDeferredWarnings(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_printDeferredWarnings");
  }

  public static SEXP do_printfunction(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_printfunction");
  }

  public static SEXP do_prmatrix(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_prmatrix");
  }

  public static SEXP do_proctime(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_proctime");
  }

  public static SEXP do_psort(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_psort");
  }

  public static SEXP do_qsort(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_qsort");
  }

  public static SEXP do_quit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_quit");
  }

  public static SEXP do_quote(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_quote");
  }

  public static SEXP do_radixsort(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_radixsort");
  }

  public static SEXP do_random1(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_random1");
  }

  public static SEXP do_random2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_random2");
  }

  public static SEXP do_random3(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_random3");
  }

  public static SEXP do_range(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_range");
  }

  public static SEXP do_rank(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rank");
  }

  public static SEXP do_rapply(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rapply");
  }

  public static SEXP do_rawShift(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rawShift");
  }

  public static SEXP do_rawToBits(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rawToBits");
  }

  public static SEXP do_rawToChar(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rawToChar");
  }

  public static SEXP do_readDCF(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readDCF");
  }

  public static SEXP do_readEnviron(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readEnviron");
  }

  public static SEXP do_readlink(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readlink");
  }

  public static SEXP do_readLines(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readLines");
  }

  public static SEXP do_readln(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readln");
  }

  public static SEXP do_recall(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_recall");
  }

  public static SEXP do_refcnt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_refcnt");
  }

  public static SEXP do_recordGraphics(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_recordGraphics");
  }

  public static SEXP do_regexec(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_regexec");
  }

  public static SEXP do_regexpr(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_regexpr");
  }

  public static SEXP do_regFinaliz(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_regFinaliz");
  }

  public static SEXP do_relop(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_relop");
  }

  public static SEXP do_relop_dflt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_relop_dflt");
  }

  public static SEXP do_remove(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_remove");
  }

  public static SEXP do_rep(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rep");
  }

  public static SEXP do_rep_int(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rep_int");
  }

  public static SEXP do_rep_len(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rep_len");
  }

  public static SEXP do_repeat(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_repeat");
  }

  public static SEXP do_resetCondHands(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_resetCondHands");
  }

  public static SEXP do_restart(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_restart");
  }

  public static SEXP /*NORET*/ do_return(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_return");
  }

  public static SEXP do_returnValue(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_returnValue");
  }

  public static SEXP do_rgb(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rgb");
  }

  public static SEXP do_RGB2hsv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_RGB2hsv");
  }

  public static SEXP do_Rhome(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_Rhome");
  }

  public static SEXP do_RNGkind(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_RNGkind");
  }

  public static SEXP do_rowsum(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rowsum");
  }

  public static SEXP do_rownames(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rownames");
  }

  public static SEXP do_rowscols(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rowscols");
  }

  public static SEXP do_S4on(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_S4on");
  }

  public static SEXP do_sample(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sample");
  }

  public static SEXP do_sample2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sample2");
  }

  public static SEXP do_save(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_save");
  }

  public static SEXP do_saveToConn(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_saveToConn");
  }

  public static SEXP do_saveplot(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_saveplot");
  }

  public static SEXP do_scan(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_scan");
  }

  public static SEXP do_search(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_search");
  }

  public static SEXP do_selectlist(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_selectlist");
  }

  public static SEXP do_seq(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_seq");
  }

  public static SEXP do_seq_along(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_seq_along");
  }

  public static SEXP do_seq_len(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_seq_len");
  }

  public static SEXP do_serialize(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_serialize");
  }

  public static SEXP do_serializeToConn(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_serializeToConn");
  }

  public static SEXP do_set(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_set");
  }

  public static SEXP do_setS4Object(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setS4Object");
  }

  public static SEXP do_setFileTime(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setFileTime");
  }

  public static SEXP do_setencoding(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setencoding");
  }

  public static SEXP do_setenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setenv");
  }

  public static SEXP do_seterrmessage(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_seterrmessage");
  }

  public static SEXP do_setmaxnumthreads(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setmaxnumthreads");
  }

  public static SEXP do_setnumthreads(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setnumthreads");
  }

  public static SEXP do_setGraphicsEventEnv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setGraphicsEventEnv");
  }

  public static SEXP do_setlocale(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setlocale");
  }

  public static SEXP do_setseed(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setseed");
  }

  public static SEXP do_setSessionTimeLimit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setSessionTimeLimit");
  }

  public static SEXP do_setTimeLimit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setTimeLimit");
  }

  public static SEXP do_setwd(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_setwd");
  }

  public static SEXP do_shortRowNames(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_shortRowNames");
  }

  public static SEXP do_signalCondition(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_signalCondition");
  }

  public static SEXP do_sink(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sink");
  }

  public static SEXP do_sinknumber(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sinknumber");
  }

  public static SEXP do_sort(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sort");
  }

  public static SEXP do_split(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_split");
  }

  public static SEXP do_sprintf(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sprintf");
  }

  public static SEXP do_standardGeneric(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_standardGeneric");
  }

  public static SEXP /*NORET*/ do_stop(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_stop");
  }

  public static SEXP do_storage_mode(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_storage_mode");
  }

  public static SEXP do_strsplit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_strsplit");
  }

  public static SEXP do_strptime(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_strptime");
  }

  public static SEXP do_strtrim(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_strtrim");
  }

  public static SEXP do_strtoi(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_strtoi");
  }

  public static SEXP do_syschmod(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_syschmod");
  }

  public static SEXP do_sysinfo(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sysinfo");
  }

  public static SEXP do_syssleep(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_syssleep");
  }

  public static SEXP do_sysumask(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sysumask");
  }

  public static SEXP do_subassign(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassign");
  }

  public static SEXP do_subassign_dflt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassign_dflt");
  }

  public static SEXP do_subassign2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassign2");
  }

  public static SEXP do_subassign2_dflt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassign2_dflt");
  }

  public static SEXP do_subassign3(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassign3");
  }

  public static SEXP do_subassigndf(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassigndf");
  }

  public static SEXP do_subassigndf2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subassigndf2");
  }

  public static SEXP do_subset(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subset");
  }

  public static SEXP do_subset_dflt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subset_dflt");
  }

  public static SEXP do_subset2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subset2");
  }

  public static SEXP do_subset2_dflt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subset2_dflt");
  }

  public static SEXP do_subset3(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subset3");
  }

  public static SEXP do_subsetdf(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subsetdf");
  }

  public static SEXP do_subsetdf2(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_subsetdf2");
  }

  public static SEXP do_substitute(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_substitute");
  }

  public static SEXP do_substr(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_substr");
  }

  public static SEXP do_substrgets(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_substrgets");
  }

  public static SEXP do_summary(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_summary");
  }

  public static SEXP do_switch(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_switch");
  }

  public static SEXP do_sys(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sys");
  }

  public static SEXP do_sysbrowser(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sysbrowser");
  }

  public static SEXP do_sysgetpid(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sysgetpid");
  }

  public static SEXP do_system(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_system");
  }

  public static SEXP do_systime(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_systime");
  }

  public static SEXP do_tabulate(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_tabulate");
  }

  public static SEXP do_tempdir(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_tempdir");
  }

  public static SEXP do_tempfile(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_tempfile");
  }

  public static SEXP do_tilde(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_tilde");
  }

  public static SEXP do_tolower(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_tolower");
  }

  public static SEXP do_topenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_topenv");
  }

  public static SEXP do_trace(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_trace");
  }

  public static SEXP do_traceOnOff(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_traceOnOff");
  }

  public static SEXP do_traceback(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_traceback");
  }

  public static SEXP do_transpose(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_transpose");
  }

  public static SEXP do_trunc(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_trunc");
  }

  public static SEXP do_typeof(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_typeof");
  }

  public static SEXP do_unclass(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unclass");
  }

  public static SEXP do_unlink(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unlink");
  }

  public static SEXP do_unlist(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unlist");
  }

  public static SEXP do_unserializeFromConn(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unserializeFromConn");
  }

  public static SEXP do_unsetenv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unsetenv");
  }

  public static SEXP do_unzip(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unzip");
  }

  public static SEXP /*NORET*/ do_usemethod(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_usemethod");
  }

  public static SEXP do_utf8ToInt(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_utf8ToInt");
  }

  public static SEXP do_vapply(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_vapply");
  }

  public static SEXP do_version(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_version");
  }

  public static SEXP do_warning(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_warning");
  }

  public static SEXP do_while(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_while");
  }

  public static SEXP do_which(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_which");
  }

  public static SEXP do_withVisible(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_withVisible");
  }

  public static SEXP do_xtfrm(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_xtfrm");
  }

  public static SEXP do_getSnapshot(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getSnapshot");
  }

  public static SEXP do_playSnapshot(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_playSnapshot");
  }

  public static SEXP R_do_data_class(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_do_data_class");
  }

  public static SEXP R_do_set_class(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("R_do_set_class");
  }

  public static SEXP R_getS4DataSlot(SEXP obj, /*SEXPTYPE*/ int type) {
    throw new UnimplementedGnuApiMethod("R_getS4DataSlot");
  }

  public static SEXP do_mkcode(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_mkcode");
  }

  public static SEXP do_bcclose(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bcclose");
  }

  public static SEXP do_is_builtin_internal(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_is_builtin_internal");
  }

  public static SEXP do_disassemble(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_disassemble");
  }

  public static SEXP do_bcversion(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bcversion");
  }

  public static SEXP do_loadfile(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_loadfile");
  }

  public static SEXP do_savefile(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_savefile");
  }

  public static SEXP do_growconst(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_growconst");
  }

  public static SEXP do_putconst(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_putconst");
  }

  public static SEXP do_getconst(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getconst");
  }

  public static SEXP do_enablejit(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_enablejit");
  }

  public static SEXP do_compilepkgs(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_compilepkgs");
  }

  public static SEXP do_stdin(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_stdin");
  }

  public static SEXP do_stdout(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_stdout");
  }

  public static SEXP do_stderr(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_stderr");
  }

  public static SEXP do_readlines(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readlines");
  }

  public static SEXP do_writelines(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_writelines");
  }

  public static SEXP do_readbin(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readbin");
  }

  public static SEXP do_writebin(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_writebin");
  }

  public static SEXP do_readchar(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_readchar");
  }

  public static SEXP do_writechar(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_writechar");
  }

  public static SEXP do_open(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_open");
  }

  public static SEXP do_isopen(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isopen");
  }

  public static SEXP do_isincomplete(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isincomplete");
  }

  public static SEXP do_isseekable(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_isseekable");
  }

  public static SEXP do_close(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_close");
  }

  public static SEXP do_fifo(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_fifo");
  }

  public static SEXP do_pipe(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pipe");
  }

  public static SEXP do_url(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_url");
  }

  public static SEXP do_gzfile(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gzfile");
  }

  public static SEXP do_unz(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_unz");
  }

  public static SEXP do_seek(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_seek");
  }

  public static SEXP do_truncate(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_truncate");
  }

  public static SEXP do_pushback(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pushback");
  }

  public static SEXP do_pushbacklength(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_pushbacklength");
  }

  public static SEXP do_clearpushback(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_clearpushback");
  }

  public static SEXP do_rawconnection(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rawconnection");
  }

  public static SEXP do_rawconvalue(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_rawconvalue");
  }

  public static SEXP do_textconnection(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_textconnection");
  }

  public static SEXP do_textconvalue(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_textconvalue");
  }

  public static SEXP do_getconnection(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getconnection");
  }

  public static SEXP do_getallconnections(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_getallconnections");
  }

  public static SEXP do_sumconnection(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sumconnection");
  }

  public static SEXP do_download(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_download");
  }

  public static SEXP do_sockconn(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sockconn");
  }

  public static SEXP do_sockselect(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_sockselect");
  }

  public static SEXP do_gzcon(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_gzcon");
  }

  public static SEXP do_memCompress(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_memCompress");
  }

  public static SEXP do_memDecompress(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_memDecompress");
  }

  public static SEXP do_lockEnv(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lockEnv");
  }

  public static SEXP do_envIsLocked(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_envIsLocked");
  }

  public static SEXP do_lockBnd(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_lockBnd");
  }

  public static SEXP do_bndIsLocked(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bndIsLocked");
  }

  public static SEXP do_mkActiveBnd(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_mkActiveBnd");
  }

  public static SEXP do_bndIsActive(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_bndIsActive");
  }

  public static SEXP do_mkUnbound(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_mkUnbound");
  }

  public static SEXP do_isNSEnv(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_isNSEnv");
  }

  public static SEXP do_regNS(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_regNS");
  }

  public static SEXP do_unregNS(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_unregNS");
  }

  public static SEXP do_getRegNS(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_getRegNS");
  }

  public static SEXP do_getNSRegistry(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_getNSRegistry");
  }

  public static SEXP do_importIntoEnv(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_importIntoEnv");
  }

  public static SEXP do_envprofile(SEXP call, SEXP op, SEXP args, SEXP rho) {
    throw new UnimplementedGnuApiMethod("do_envprofile");
  }

  public static SEXP do_tracemem(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_tracemem");
  }

  public static SEXP do_retracemem(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_retracemem");
  }

  public static SEXP do_untracemem(SEXP call, SEXP op, SEXP args, SEXP env) {
    throw new UnimplementedGnuApiMethod("do_untracemem");
  }
}
