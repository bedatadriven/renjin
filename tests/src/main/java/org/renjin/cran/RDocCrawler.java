package org.renjin.cran;

import java.io.IOException;
import java.io.Reader;

import r.lang.ListVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.SexpVisitor;
import r.lang.StringVector;
import r.lang.Symbol;
import r.parser.RdParser;

import com.google.common.io.InputSupplier;

public class RDocCrawler extends SexpVisitor<Void> {

	private static final Symbol RD_TAG_NAME = Symbol.get("Rd_tag");

	private RDocVisitor docVisitor;
	
	public RDocCrawler(RDocVisitor docVisitor) {
		this.docVisitor = docVisitor;
	}

	@Override
	public void visit(ListVector list) {
//		System.out.println("START LIST");
//		for(PairList.Node node : list.getAttributes().nodes()) {
//			System.out.println(" " + node.getRawTag() + " = " + node.getValue());
//		}
		
		if(list.getS3Class().indexOf("Rd") != -1) {
			// descend
			for(SEXP element : list) {
				element.accept(this);
			}
		} else if(list.getAttribute(RD_TAG_NAME) != Null.INSTANCE) {
			String tagName = ((StringVector)list.getAttribute(RD_TAG_NAME)).getElementAsString(0);
			if(tagName.equals("\\name")) {
				docVisitor.visitNameTag(list.getElementAsString(0));
			} else if(tagName.equals("\\alias")) {
				docVisitor.visitAliasTag(list.getElementAsString(0));
			} else if(tagName.equals("\\examples")) {
				dispatchExamplesTag(list);
			} else {
				// TODO: handle other tags
			}
		} else {
			// TODO: other structures ?
		}
	}

	private void dispatchExamplesTag(ListVector list) {
		StringBuilder code = new StringBuilder();
		for(SEXP exp : list) {
			if(exp instanceof StringVector) {
				code.append(((StringVector) exp).getElementAsString(0)).append("\n");
			}
		}
		docVisitor.visitExamples(code.toString());
	}

	public static void crawl(InputSupplier<Reader> in, RDocVisitor visitor) throws IOException {
		RdParser parser = new RdParser();
	    ListVector result = (ListVector) parser.R_ParseRd(in.getInput(), Null.INSTANCE, false);
	    result.accept(new RDocCrawler(visitor));
	}
	
}
