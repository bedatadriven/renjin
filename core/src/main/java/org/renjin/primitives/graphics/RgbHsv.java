package org.renjin.primitives.graphics;

import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.Recycle;
import org.renjin.sexp.*;

import java.awt.*;
import java.util.ArrayList;


public class RgbHsv {

	private final static double DEG2RAD = 0.01745329251994329576;
	private final static double WHITE_X = 95.047;
	private final static double WHITE_Y = 100.000;
	private final static double WHITE_Z = 108.883;
	private final static double WHITE_u = 0.1978398;
	private final static double WHITE_v = 0.4683363;
	private final static double GAMMA = 2.4;

	private static ArrayList<ColorDataBaseEntry> colorDataBase = null;
	private static String[] defaultPalette = new String[] { "black", "red",
			"green3", "blue", "cyan", "magenta", "yellow", "grey", };

	private static void initColorDataBase() {
		if (colorDataBase == null) {
			colorDataBase = new ArrayList<ColorDataBaseEntry>();
			colorDataBase.add(new ColorDataBaseEntry("white", "#FFFFFF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("aliceblue", "#F0F8FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("antiquewhite", "#FAEBD7",
					0));
			colorDataBase.add(new ColorDataBaseEntry("antiquewhite1",
					"#FFEFDB", 0));
			colorDataBase.add(new ColorDataBaseEntry("antiquewhite2",
					"#EEDFCC", 0));
			colorDataBase.add(new ColorDataBaseEntry("antiquewhite3",
					"#CDC0B0", 0));
			colorDataBase.add(new ColorDataBaseEntry("antiquewhite4",
					"#8B8378", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("aquamarine", "#7FFFD4", 0));
			colorDataBase.add(new ColorDataBaseEntry("aquamarine1", "#7FFFD4",
					0));
			colorDataBase.add(new ColorDataBaseEntry("aquamarine2", "#76EEC6",
					0));
			colorDataBase.add(new ColorDataBaseEntry("aquamarine3", "#66CDAA",
					0));
			colorDataBase.add(new ColorDataBaseEntry("aquamarine4", "#458B74",
					0));
			colorDataBase.add(new ColorDataBaseEntry("azure", "#F0FFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("azure1", "#F0FFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("azure2", "#E0EEEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("azure3", "#C1CDCD", 0));
			colorDataBase.add(new ColorDataBaseEntry("azure4", "#838B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("beige", "#F5F5DC", 0));
			colorDataBase.add(new ColorDataBaseEntry("bisque", "#FFE4C4", 0));
			colorDataBase.add(new ColorDataBaseEntry("bisque1", "#FFE4C4", 0));
			colorDataBase.add(new ColorDataBaseEntry("bisque2", "#EED5B7", 0));
			colorDataBase.add(new ColorDataBaseEntry("bisque3", "#CDB79E", 0));
			colorDataBase.add(new ColorDataBaseEntry("bisque4", "#8B7D6B", 0));
			colorDataBase.add(new ColorDataBaseEntry("black", "#000000", 0));
			colorDataBase.add(new ColorDataBaseEntry("blanchedalmond",
					"#FFEBCD", 0));
			colorDataBase.add(new ColorDataBaseEntry("blue", "#0000FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("blue1", "#0000FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("blue2", "#0000EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("blue3", "#0000CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("blue4", "#00008B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("blueviolet", "#8A2BE2", 0));
			colorDataBase.add(new ColorDataBaseEntry("brown", "#A52A2A", 0));
			colorDataBase.add(new ColorDataBaseEntry("brown1", "#FF4040", 0));
			colorDataBase.add(new ColorDataBaseEntry("brown2", "#EE3B3B", 0));
			colorDataBase.add(new ColorDataBaseEntry("brown3", "#CD3333", 0));
			colorDataBase.add(new ColorDataBaseEntry("brown4", "#8B2323", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("burlywood", "#DEB887", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("burlywood1", "#FFD39B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("burlywood2", "#EEC591", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("burlywood3", "#CDAA7D", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("burlywood4", "#8B7355", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cadetblue", "#5F9EA0", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cadetblue1", "#98F5FF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cadetblue2", "#8EE5EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cadetblue3", "#7AC5CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cadetblue4", "#53868B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("chartreuse", "#7FFF00", 0));
			colorDataBase.add(new ColorDataBaseEntry("chartreuse1", "#7FFF00",
					0));
			colorDataBase.add(new ColorDataBaseEntry("chartreuse2", "#76EE00",
					0));
			colorDataBase.add(new ColorDataBaseEntry("chartreuse3", "#66CD00",
					0));
			colorDataBase.add(new ColorDataBaseEntry("chartreuse4", "#458B00",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("chocolate", "#D2691E", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("chocolate1", "#FF7F24", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("chocolate2", "#EE7621", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("chocolate3", "#CD661D", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("chocolate4", "#8B4513", 0));
			colorDataBase.add(new ColorDataBaseEntry("coral", "#FF7F50", 0));
			colorDataBase.add(new ColorDataBaseEntry("coral1", "#FF7256", 0));
			colorDataBase.add(new ColorDataBaseEntry("coral2", "#EE6A50", 0));
			colorDataBase.add(new ColorDataBaseEntry("coral3", "#CD5B45", 0));
			colorDataBase.add(new ColorDataBaseEntry("coral4", "#8B3E2F", 0));
			colorDataBase.add(new ColorDataBaseEntry("cornflowerblue",
					"#6495ED", 0));
			colorDataBase.add(new ColorDataBaseEntry("cornsilk", "#FFF8DC", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cornsilk1", "#FFF8DC", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cornsilk2", "#EEE8CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cornsilk3", "#CDC8B1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("cornsilk4", "#8B8878", 0));
			colorDataBase.add(new ColorDataBaseEntry("cyan", "#00FFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("cyan1", "#00FFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("cyan2", "#00EEEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("cyan3", "#00CDCD", 0));
			colorDataBase.add(new ColorDataBaseEntry("cyan4", "#008B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkblue", "#00008B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkcyan", "#008B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgoldenrod",
					"#B8860B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgoldenrod1",
					"#FFB90F", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgoldenrod2",
					"#EEAD0E", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgoldenrod3",
					"#CD950C", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgoldenrod4",
					"#8B6508", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgray", "#A9A9A9", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("darkgreen", "#006400", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkgrey", "#A9A9A9", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("darkkhaki", "#BDB76B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkmagenta", "#8B008B",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkolivegreen",
					"#556B2F", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkolivegreen1",
					"#CAFF70", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkolivegreen2",
					"#BCEE68", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkolivegreen3",
					"#A2CD5A", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkolivegreen4",
					"#6E8B3D", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("darkorange", "#FF8C00", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkorange1", "#FF7F00",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkorange2", "#EE7600",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkorange3", "#CD6600",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkorange4", "#8B4500",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("darkorchid", "#9932CC", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkorchid1", "#BF3EFF",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkorchid2", "#B23AEE",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkorchid3", "#9A32CD",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkorchid4", "#68228B",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkred", "#8B0000", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("darksalmon", "#E9967A", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkseagreen", "#8FBC8F",
					0));
			colorDataBase.add(new ColorDataBaseEntry("darkseagreen1",
					"#C1FFC1", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkseagreen2",
					"#B4EEB4", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkseagreen3",
					"#9BCD9B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkseagreen4",
					"#698B69", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslateblue",
					"#483D8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslategray",
					"#2F4F4F", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslategray1",
					"#97FFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslategray2",
					"#8DEEEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslategray3",
					"#79CDCD", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslategray4",
					"#528B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkslategrey",
					"#2F4F4F", 0));
			colorDataBase.add(new ColorDataBaseEntry("darkturquoise",
					"#00CED1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("darkviolet", "#9400D3", 0));
			colorDataBase.add(new ColorDataBaseEntry("deeppink", "#FF1493", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("deeppink1", "#FF1493", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("deeppink2", "#EE1289", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("deeppink3", "#CD1076", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("deeppink4", "#8B0A50", 0));
			colorDataBase.add(new ColorDataBaseEntry("deepskyblue", "#00BFFF",
					0));
			colorDataBase.add(new ColorDataBaseEntry("deepskyblue1", "#00BFFF",
					0));
			colorDataBase.add(new ColorDataBaseEntry("deepskyblue2", "#00B2EE",
					0));
			colorDataBase.add(new ColorDataBaseEntry("deepskyblue3", "#009ACD",
					0));
			colorDataBase.add(new ColorDataBaseEntry("deepskyblue4", "#00688B",
					0));
			colorDataBase.add(new ColorDataBaseEntry("dimgray", "#696969", 0));
			colorDataBase.add(new ColorDataBaseEntry("dimgrey", "#696969", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("dodgerblue", "#1E90FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("dodgerblue1", "#1E90FF",
					0));
			colorDataBase.add(new ColorDataBaseEntry("dodgerblue2", "#1C86EE",
					0));
			colorDataBase.add(new ColorDataBaseEntry("dodgerblue3", "#1874CD",
					0));
			colorDataBase.add(new ColorDataBaseEntry("dodgerblue4", "#104E8B",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("firebrick", "#B22222", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("firebrick1", "#FF3030", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("firebrick2", "#EE2C2C", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("firebrick3", "#CD2626", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("firebrick4", "#8B1A1A", 0));
			colorDataBase.add(new ColorDataBaseEntry("floralwhite", "#FFFAF0",
					0));
			colorDataBase.add(new ColorDataBaseEntry("forestgreen", "#228B22",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("gainsboro", "#DCDCDC", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("ghostwhite", "#F8F8FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("gold", "#FFD700", 0));
			colorDataBase.add(new ColorDataBaseEntry("gold1", "#FFD700", 0));
			colorDataBase.add(new ColorDataBaseEntry("gold2", "#EEC900", 0));
			colorDataBase.add(new ColorDataBaseEntry("gold3", "#CDAD00", 0));
			colorDataBase.add(new ColorDataBaseEntry("gold4", "#8B7500", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("goldenrod", "#DAA520", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("goldenrod1", "#FFC125", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("goldenrod2", "#EEB422", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("goldenrod3", "#CD9B1D", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("goldenrod4", "#8B6914", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray", "#BEBEBE", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray0", "#000000", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray1", "#030303", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray2", "#050505", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray3", "#080808", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray4", "#0A0A0A", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray5", "#0D0D0D", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray6", "#0F0F0F", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray7", "#121212", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray8", "#141414", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray9", "#171717", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray10", "#1A1A1A", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray11", "#1C1C1C", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray12", "#1F1F1F", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray13", "#212121", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray14", "#242424", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray15", "#262626", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray16", "#292929", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray17", "#2B2B2B", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray18", "#2E2E2E", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray19", "#303030", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray20", "#333333", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray21", "#363636", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray22", "#383838", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray23", "#3B3B3B", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray24", "#3D3D3D", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray25", "#404040", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray26", "#424242", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray27", "#454545", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray28", "#474747", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray29", "#4A4A4A", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray30", "#4D4D4D", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray31", "#4F4F4F", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray32", "#525252", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray33", "#545454", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray34", "#575757", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray35", "#595959", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray36", "#5C5C5C", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray37", "#5E5E5E", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray38", "#616161", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray39", "#636363", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray40", "#666666", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray41", "#696969", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray42", "#6B6B6B", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray43", "#6E6E6E", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray44", "#707070", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray45", "#737373", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray46", "#757575", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray47", "#787878", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray48", "#7A7A7A", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray49", "#7D7D7D", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray50", "#7F7F7F", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray51", "#828282", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray52", "#858585", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray53", "#878787", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray54", "#8A8A8A", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray55", "#8C8C8C", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray56", "#8F8F8F", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray57", "#919191", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray58", "#949494", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray59", "#969696", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray60", "#999999", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray61", "#9C9C9C", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray62", "#9E9E9E", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray63", "#A1A1A1", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray64", "#A3A3A3", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray65", "#A6A6A6", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray66", "#A8A8A8", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray67", "#ABABAB", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray68", "#ADADAD", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray69", "#B0B0B0", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray70", "#B3B3B3", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray71", "#B5B5B5", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray72", "#B8B8B8", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray73", "#BABABA", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray74", "#BDBDBD", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray75", "#BFBFBF", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray76", "#C2C2C2", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray77", "#C4C4C4", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray78", "#C7C7C7", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray79", "#C9C9C9", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray80", "#CCCCCC", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray81", "#CFCFCF", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray82", "#D1D1D1", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray83", "#D4D4D4", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray84", "#D6D6D6", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray85", "#D9D9D9", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray86", "#DBDBDB", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray87", "#DEDEDE", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray88", "#E0E0E0", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray89", "#E3E3E3", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray90", "#E5E5E5", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray91", "#E8E8E8", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray92", "#EBEBEB", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray93", "#EDEDED", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray94", "#F0F0F0", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray95", "#F2F2F2", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray96", "#F5F5F5", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray97", "#F7F7F7", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray98", "#FAFAFA", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray99", "#FCFCFC", 0));
			colorDataBase.add(new ColorDataBaseEntry("gray100", "#FFFFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("green", "#00FF00", 0));
			colorDataBase.add(new ColorDataBaseEntry("green1", "#00FF00", 0));
			colorDataBase.add(new ColorDataBaseEntry("green2", "#00EE00", 0));
			colorDataBase.add(new ColorDataBaseEntry("green3", "#00CD00", 0));
			colorDataBase.add(new ColorDataBaseEntry("green4", "#008B00", 0));
			colorDataBase.add(new ColorDataBaseEntry("greenyellow", "#ADFF2F",
					0));
			colorDataBase.add(new ColorDataBaseEntry("grey", "#BEBEBE", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey0", "#000000", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey1", "#030303", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey2", "#050505", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey3", "#080808", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey4", "#0A0A0A", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey5", "#0D0D0D", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey6", "#0F0F0F", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey7", "#121212", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey8", "#141414", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey9", "#171717", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey10", "#1A1A1A", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey11", "#1C1C1C", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey12", "#1F1F1F", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey13", "#212121", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey14", "#242424", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey15", "#262626", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey16", "#292929", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey17", "#2B2B2B", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey18", "#2E2E2E", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey19", "#303030", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey20", "#333333", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey21", "#363636", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey22", "#383838", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey23", "#3B3B3B", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey24", "#3D3D3D", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey25", "#404040", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey26", "#424242", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey27", "#454545", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey28", "#474747", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey29", "#4A4A4A", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey30", "#4D4D4D", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey31", "#4F4F4F", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey32", "#525252", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey33", "#545454", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey34", "#575757", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey35", "#595959", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey36", "#5C5C5C", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey37", "#5E5E5E", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey38", "#616161", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey39", "#636363", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey40", "#666666", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey41", "#696969", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey42", "#6B6B6B", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey43", "#6E6E6E", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey44", "#707070", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey45", "#737373", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey46", "#757575", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey47", "#787878", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey48", "#7A7A7A", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey49", "#7D7D7D", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey50", "#7F7F7F", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey51", "#828282", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey52", "#858585", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey53", "#878787", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey54", "#8A8A8A", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey55", "#8C8C8C", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey56", "#8F8F8F", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey57", "#919191", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey58", "#949494", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey59", "#969696", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey60", "#999999", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey61", "#9C9C9C", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey62", "#9E9E9E", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey63", "#A1A1A1", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey64", "#A3A3A3", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey65", "#A6A6A6", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey66", "#A8A8A8", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey67", "#ABABAB", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey68", "#ADADAD", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey69", "#B0B0B0", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey70", "#B3B3B3", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey71", "#B5B5B5", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey72", "#B8B8B8", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey73", "#BABABA", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey74", "#BDBDBD", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey75", "#BFBFBF", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey76", "#C2C2C2", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey77", "#C4C4C4", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey78", "#C7C7C7", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey79", "#C9C9C9", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey80", "#CCCCCC", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey81", "#CFCFCF", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey82", "#D1D1D1", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey83", "#D4D4D4", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey84", "#D6D6D6", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey85", "#D9D9D9", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey86", "#DBDBDB", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey87", "#DEDEDE", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey88", "#E0E0E0", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey89", "#E3E3E3", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey90", "#E5E5E5", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey91", "#E8E8E8", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey92", "#EBEBEB", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey93", "#EDEDED", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey94", "#F0F0F0", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey95", "#F2F2F2", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey96", "#F5F5F5", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey97", "#F7F7F7", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey98", "#FAFAFA", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey99", "#FCFCFC", 0));
			colorDataBase.add(new ColorDataBaseEntry("grey100", "#FFFFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("honeydew", "#F0FFF0", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("honeydew1", "#F0FFF0", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("honeydew2", "#E0EEE0", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("honeydew3", "#C1CDC1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("honeydew4", "#838B83", 0));
			colorDataBase.add(new ColorDataBaseEntry("hotpink", "#FF69B4", 0));
			colorDataBase.add(new ColorDataBaseEntry("hotpink1", "#FF6EB4", 0));
			colorDataBase.add(new ColorDataBaseEntry("hotpink2", "#EE6AA7", 0));
			colorDataBase.add(new ColorDataBaseEntry("hotpink3", "#CD6090", 0));
			colorDataBase.add(new ColorDataBaseEntry("hotpink4", "#8B3A62", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("indianred", "#CD5C5C", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("indianred1", "#FF6A6A", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("indianred2", "#EE6363", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("indianred3", "#CD5555", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("indianred4", "#8B3A3A", 0));
			colorDataBase.add(new ColorDataBaseEntry("ivory", "#FFFFF0", 0));
			colorDataBase.add(new ColorDataBaseEntry("ivory1", "#FFFFF0", 0));
			colorDataBase.add(new ColorDataBaseEntry("ivory2", "#EEEEE0", 0));
			colorDataBase.add(new ColorDataBaseEntry("ivory3", "#CDCDC1", 0));
			colorDataBase.add(new ColorDataBaseEntry("ivory4", "#8B8B83", 0));
			colorDataBase.add(new ColorDataBaseEntry("khaki", "#F0E68C", 0));
			colorDataBase.add(new ColorDataBaseEntry("khaki1", "#FFF68F", 0));
			colorDataBase.add(new ColorDataBaseEntry("khaki2", "#EEE685", 0));
			colorDataBase.add(new ColorDataBaseEntry("khaki3", "#CDC673", 0));
			colorDataBase.add(new ColorDataBaseEntry("khaki4", "#8B864E", 0));
			colorDataBase.add(new ColorDataBaseEntry("lavender", "#E6E6FA", 0));
			colorDataBase.add(new ColorDataBaseEntry("lavenderblush",
					"#FFF0F5", 0));
			colorDataBase.add(new ColorDataBaseEntry("lavenderblush1",
					"#FFF0F5", 0));
			colorDataBase.add(new ColorDataBaseEntry("lavenderblush2",
					"#EEE0E5", 0));
			colorDataBase.add(new ColorDataBaseEntry("lavenderblush3",
					"#CDC1C5", 0));
			colorDataBase.add(new ColorDataBaseEntry("lavenderblush4",
					"#8B8386", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lawngreen", "#7CFC00", 0));
			colorDataBase.add(new ColorDataBaseEntry("lemonchiffon", "#FFFACD",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lemonchiffon1",
					"#FFFACD", 0));
			colorDataBase.add(new ColorDataBaseEntry("lemonchiffon2",
					"#EEE9BF", 0));
			colorDataBase.add(new ColorDataBaseEntry("lemonchiffon3",
					"#CDC9A5", 0));
			colorDataBase.add(new ColorDataBaseEntry("lemonchiffon4",
					"#8B8970", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightblue", "#ADD8E6", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightblue1", "#BFEFFF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightblue2", "#B2DFEE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightblue3", "#9AC0CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightblue4", "#68838B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightcoral", "#F08080", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightcyan", "#E0FFFF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightcyan1", "#E0FFFF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightcyan2", "#D1EEEE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightcyan3", "#B4CDCD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightcyan4", "#7A8B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightgoldenrod",
					"#EEDD82", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightgoldenrod1",
					"#FFEC8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightgoldenrod2",
					"#EEDC82", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightgoldenrod3",
					"#CDBE70", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightgoldenrod4",
					"#8B814C", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightgoldenrodyellow",
					"#FAFAD2", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightgray", "#D3D3D3", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightgreen", "#90EE90", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightgrey", "#D3D3D3", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightpink", "#FFB6C1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightpink1", "#FFAEB9", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightpink2", "#EEA2AD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightpink3", "#CD8C95", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("lightpink4", "#8B5F65", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightsalmon", "#FFA07A",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightsalmon1", "#FFA07A",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightsalmon2", "#EE9572",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightsalmon3", "#CD8162",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightsalmon4", "#8B5742",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightseagreen",
					"#20B2AA", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightskyblue", "#87CEFA",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightskyblue1",
					"#B0E2FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightskyblue2",
					"#A4D3EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightskyblue3",
					"#8DB6CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightskyblue4",
					"#607B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightslateblue",
					"#8470FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightslategray",
					"#778899", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightslategrey",
					"#778899", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightsteelblue",
					"#B0C4DE", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightsteelblue1",
					"#CAE1FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightsteelblue2",
					"#BCD2EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightsteelblue3",
					"#A2B5CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightsteelblue4",
					"#6E7B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("lightyellow", "#FFFFE0",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightyellow1", "#FFFFE0",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightyellow2", "#EEEED1",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightyellow3", "#CDCDB4",
					0));
			colorDataBase.add(new ColorDataBaseEntry("lightyellow4", "#8B8B7A",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("limegreen", "#32CD32", 0));
			colorDataBase.add(new ColorDataBaseEntry("linen", "#FAF0E6", 0));
			colorDataBase.add(new ColorDataBaseEntry("magenta", "#FF00FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("magenta1", "#FF00FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("magenta2", "#EE00EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("magenta3", "#CD00CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("magenta4", "#8B008B", 0));
			colorDataBase.add(new ColorDataBaseEntry("maroon", "#B03060", 0));
			colorDataBase.add(new ColorDataBaseEntry("maroon1", "#FF34B3", 0));
			colorDataBase.add(new ColorDataBaseEntry("maroon2", "#EE30A7", 0));
			colorDataBase.add(new ColorDataBaseEntry("maroon3", "#CD2990", 0));
			colorDataBase.add(new ColorDataBaseEntry("maroon4", "#8B1C62", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumaquamarine",
					"#66CDAA", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("mediumblue", "#0000CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumorchid", "#BA55D3",
					0));
			colorDataBase.add(new ColorDataBaseEntry("mediumorchid1",
					"#E066FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumorchid2",
					"#D15FEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumorchid3",
					"#B452CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumorchid4",
					"#7A378B", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumpurple", "#9370DB",
					0));
			colorDataBase.add(new ColorDataBaseEntry("mediumpurple1",
					"#AB82FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumpurple2",
					"#9F79EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumpurple3",
					"#8968CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumpurple4",
					"#5D478B", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumseagreen",
					"#3CB371", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumslateblue",
					"#7B68EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumspringgreen",
					"#00FA9A", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumturquoise",
					"#48D1CC", 0));
			colorDataBase.add(new ColorDataBaseEntry("mediumvioletred",
					"#C71585", 0));
			colorDataBase.add(new ColorDataBaseEntry("midnightblue", "#191970",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("mintcream", "#F5FFFA", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("mistyrose", "#FFE4E1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("mistyrose1", "#FFE4E1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("mistyrose2", "#EED5D2", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("mistyrose3", "#CDB7B5", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("mistyrose4", "#8B7D7B", 0));
			colorDataBase.add(new ColorDataBaseEntry("moccasin", "#FFE4B5", 0));
			colorDataBase.add(new ColorDataBaseEntry("navajowhite", "#FFDEAD",
					0));
			colorDataBase.add(new ColorDataBaseEntry("navajowhite1", "#FFDEAD",
					0));
			colorDataBase.add(new ColorDataBaseEntry("navajowhite2", "#EECFA1",
					0));
			colorDataBase.add(new ColorDataBaseEntry("navajowhite3", "#CDB38B",
					0));
			colorDataBase.add(new ColorDataBaseEntry("navajowhite4", "#8B795E",
					0));
			colorDataBase.add(new ColorDataBaseEntry("navy", "#000080", 0));
			colorDataBase.add(new ColorDataBaseEntry("navyblue", "#000080", 0));
			colorDataBase.add(new ColorDataBaseEntry("oldlace", "#FDF5E6", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("olivedrab", "#6B8E23", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("olivedrab1", "#C0FF3E", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("olivedrab2", "#B3EE3A", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("olivedrab3", "#9ACD32", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("olivedrab4", "#698B22", 0));
			colorDataBase.add(new ColorDataBaseEntry("orange", "#FFA500", 0));
			colorDataBase.add(new ColorDataBaseEntry("orange1", "#FFA500", 0));
			colorDataBase.add(new ColorDataBaseEntry("orange2", "#EE9A00", 0));
			colorDataBase.add(new ColorDataBaseEntry("orange3", "#CD8500", 0));
			colorDataBase.add(new ColorDataBaseEntry("orange4", "#8B5A00", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("orangered", "#FF4500", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("orangered1", "#FF4500", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("orangered2", "#EE4000", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("orangered3", "#CD3700", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("orangered4", "#8B2500", 0));
			colorDataBase.add(new ColorDataBaseEntry("orchid", "#DA70D6", 0));
			colorDataBase.add(new ColorDataBaseEntry("orchid1", "#FF83FA", 0));
			colorDataBase.add(new ColorDataBaseEntry("orchid2", "#EE7AE9", 0));
			colorDataBase.add(new ColorDataBaseEntry("orchid3", "#CD69C9", 0));
			colorDataBase.add(new ColorDataBaseEntry("orchid4", "#8B4789", 0));
			colorDataBase.add(new ColorDataBaseEntry("palegoldenrod",
					"#EEE8AA", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("palegreen", "#98FB98", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("palegreen1", "#9AFF9A", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("palegreen2", "#90EE90", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("palegreen3", "#7CCD7C", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("palegreen4", "#548B54", 0));
			colorDataBase.add(new ColorDataBaseEntry("paleturquoise",
					"#AFEEEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("paleturquoise1",
					"#BBFFFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("paleturquoise2",
					"#AEEEEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("paleturquoise3",
					"#96CDCD", 0));
			colorDataBase.add(new ColorDataBaseEntry("paleturquoise4",
					"#668B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("palevioletred",
					"#DB7093", 0));
			colorDataBase.add(new ColorDataBaseEntry("palevioletred1",
					"#FF82AB", 0));
			colorDataBase.add(new ColorDataBaseEntry("palevioletred2",
					"#EE799F", 0));
			colorDataBase.add(new ColorDataBaseEntry("palevioletred3",
					"#CD6889", 0));
			colorDataBase.add(new ColorDataBaseEntry("palevioletred4",
					"#8B475D", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("papayawhip", "#FFEFD5", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("peachpuff", "#FFDAB9", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("peachpuff1", "#FFDAB9", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("peachpuff2", "#EECBAD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("peachpuff3", "#CDAF95", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("peachpuff4", "#8B7765", 0));
			colorDataBase.add(new ColorDataBaseEntry("peru", "#CD853F", 0));
			colorDataBase.add(new ColorDataBaseEntry("pink", "#FFC0CB", 0));
			colorDataBase.add(new ColorDataBaseEntry("pink1", "#FFB5C5", 0));
			colorDataBase.add(new ColorDataBaseEntry("pink2", "#EEA9B8", 0));
			colorDataBase.add(new ColorDataBaseEntry("pink3", "#CD919E", 0));
			colorDataBase.add(new ColorDataBaseEntry("pink4", "#8B636C", 0));
			colorDataBase.add(new ColorDataBaseEntry("plum", "#DDA0DD", 0));
			colorDataBase.add(new ColorDataBaseEntry("plum1", "#FFBBFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("plum2", "#EEAEEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("plum3", "#CD96CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("plum4", "#8B668B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("powderblue", "#B0E0E6", 0));
			colorDataBase.add(new ColorDataBaseEntry("purple", "#A020F0", 0));
			colorDataBase.add(new ColorDataBaseEntry("purple1", "#9B30FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("purple2", "#912CEE", 0));
			colorDataBase.add(new ColorDataBaseEntry("purple3", "#7D26CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("purple4", "#551A8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("red", "#FF0000", 0));
			colorDataBase.add(new ColorDataBaseEntry("red1", "#FF0000", 0));
			colorDataBase.add(new ColorDataBaseEntry("red2", "#EE0000", 0));
			colorDataBase.add(new ColorDataBaseEntry("red3", "#CD0000", 0));
			colorDataBase.add(new ColorDataBaseEntry("red4", "#8B0000", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("rosybrown", "#BC8F8F", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("rosybrown1", "#FFC1C1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("rosybrown2", "#EEB4B4", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("rosybrown3", "#CD9B9B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("rosybrown4", "#8B6969", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("royalblue", "#4169E1", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("royalblue1", "#4876FF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("royalblue2", "#436EEE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("royalblue3", "#3A5FCD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("royalblue4", "#27408B", 0));
			colorDataBase.add(new ColorDataBaseEntry("saddlebrown", "#8B4513",
					0));
			colorDataBase.add(new ColorDataBaseEntry("salmon", "#FA8072", 0));
			colorDataBase.add(new ColorDataBaseEntry("salmon1", "#FF8C69", 0));
			colorDataBase.add(new ColorDataBaseEntry("salmon2", "#EE8262", 0));
			colorDataBase.add(new ColorDataBaseEntry("salmon3", "#CD7054", 0));
			colorDataBase.add(new ColorDataBaseEntry("salmon4", "#8B4C39", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("sandybrown", "#F4A460", 0));
			colorDataBase.add(new ColorDataBaseEntry("seagreen", "#2E8B57", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seagreen1", "#54FF9F", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seagreen2", "#4EEE94", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seagreen3", "#43CD80", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seagreen4", "#2E8B57", 0));
			colorDataBase.add(new ColorDataBaseEntry("seashell", "#FFF5EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seashell1", "#FFF5EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seashell2", "#EEE5DE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seashell3", "#CDC5BF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("seashell4", "#8B8682", 0));
			colorDataBase.add(new ColorDataBaseEntry("sienna", "#A0522D", 0));
			colorDataBase.add(new ColorDataBaseEntry("sienna1", "#FF8247", 0));
			colorDataBase.add(new ColorDataBaseEntry("sienna2", "#EE7942", 0));
			colorDataBase.add(new ColorDataBaseEntry("sienna3", "#CD6839", 0));
			colorDataBase.add(new ColorDataBaseEntry("sienna4", "#8B4726", 0));
			colorDataBase.add(new ColorDataBaseEntry("skyblue", "#87CEEB", 0));
			colorDataBase.add(new ColorDataBaseEntry("skyblue1", "#87CEFF", 0));
			colorDataBase.add(new ColorDataBaseEntry("skyblue2", "#7EC0EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("skyblue3", "#6CA6CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("skyblue4", "#4A708B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slateblue", "#6A5ACD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slateblue1", "#836FFF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slateblue2", "#7A67EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slateblue3", "#6959CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slateblue4", "#473C8B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slategray", "#708090", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slategray1", "#C6E2FF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slategray2", "#B9D3EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slategray3", "#9FB6CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slategray4", "#6C7B8B", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("slategrey", "#708090", 0));
			colorDataBase.add(new ColorDataBaseEntry("snow", "#FFFAFA", 0));
			colorDataBase.add(new ColorDataBaseEntry("snow1", "#FFFAFA", 0));
			colorDataBase.add(new ColorDataBaseEntry("snow2", "#EEE9E9", 0));
			colorDataBase.add(new ColorDataBaseEntry("snow3", "#CDC9C9", 0));
			colorDataBase.add(new ColorDataBaseEntry("snow4", "#8B8989", 0));
			colorDataBase.add(new ColorDataBaseEntry("springgreen", "#00FF7F",
					0));
			colorDataBase.add(new ColorDataBaseEntry("springgreen1", "#00FF7F",
					0));
			colorDataBase.add(new ColorDataBaseEntry("springgreen2", "#00EE76",
					0));
			colorDataBase.add(new ColorDataBaseEntry("springgreen3", "#00CD66",
					0));
			colorDataBase.add(new ColorDataBaseEntry("springgreen4", "#008B45",
					0));
			colorDataBase
					.add(new ColorDataBaseEntry("steelblue", "#4682B4", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("steelblue1", "#63B8FF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("steelblue2", "#5CACEE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("steelblue3", "#4F94CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("steelblue4", "#36648B", 0));
			colorDataBase.add(new ColorDataBaseEntry("tan", "#D2B48C", 0));
			colorDataBase.add(new ColorDataBaseEntry("tan1", "#FFA54F", 0));
			colorDataBase.add(new ColorDataBaseEntry("tan2", "#EE9A49", 0));
			colorDataBase.add(new ColorDataBaseEntry("tan3", "#CD853F", 0));
			colorDataBase.add(new ColorDataBaseEntry("tan4", "#8B5A2B", 0));
			colorDataBase.add(new ColorDataBaseEntry("thistle", "#D8BFD8", 0));
			colorDataBase.add(new ColorDataBaseEntry("thistle1", "#FFE1FF", 0));
			colorDataBase.add(new ColorDataBaseEntry("thistle2", "#EED2EE", 0));
			colorDataBase.add(new ColorDataBaseEntry("thistle3", "#CDB5CD", 0));
			colorDataBase.add(new ColorDataBaseEntry("thistle4", "#8B7B8B", 0));
			colorDataBase.add(new ColorDataBaseEntry("tomato", "#FF6347", 0));
			colorDataBase.add(new ColorDataBaseEntry("tomato1", "#FF6347", 0));
			colorDataBase.add(new ColorDataBaseEntry("tomato2", "#EE5C42", 0));
			colorDataBase.add(new ColorDataBaseEntry("tomato3", "#CD4F39", 0));
			colorDataBase.add(new ColorDataBaseEntry("tomato4", "#8B3626", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("turquoise", "#40E0D0", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("turquoise1", "#00F5FF", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("turquoise2", "#00E5EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("turquoise3", "#00C5CD", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("turquoise4", "#00868B", 0));
			colorDataBase.add(new ColorDataBaseEntry("violet", "#EE82EE", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("violetred", "#D02090", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("violetred1", "#FF3E96", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("violetred2", "#EE3A8C", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("violetred3", "#CD3278", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("violetred4", "#8B2252", 0));
			colorDataBase.add(new ColorDataBaseEntry("wheat", "#F5DEB3", 0));
			colorDataBase.add(new ColorDataBaseEntry("wheat1", "#FFE7BA", 0));
			colorDataBase.add(new ColorDataBaseEntry("wheat2", "#EED8AE", 0));
			colorDataBase.add(new ColorDataBaseEntry("wheat3", "#CDBA96", 0));
			colorDataBase.add(new ColorDataBaseEntry("wheat4", "#8B7E66", 0));
			colorDataBase
					.add(new ColorDataBaseEntry("whitesmoke", "#F5F5F5", 0));
			colorDataBase.add(new ColorDataBaseEntry("yellow", "#FFFF00", 0));
			colorDataBase.add(new ColorDataBaseEntry("yellow1", "#FFFF00", 0));
			colorDataBase.add(new ColorDataBaseEntry("yellow2", "#EEEE00", 0));
			colorDataBase.add(new ColorDataBaseEntry("yellow3", "#CDCD00", 0));
			colorDataBase.add(new ColorDataBaseEntry("yellow4", "#8B8B00", 0));
			colorDataBase.add(new ColorDataBaseEntry("yellowgreen", "#9ACD32",
					0));
		}
	}

	private static String getHexRgb(double red, double green, double blue,
			double alpha, double maxColorValue, boolean useAlpha) {
		if (red < 0 || green < 0 || blue < 0 || alpha < 0
				|| red > maxColorValue || green > maxColorValue
				|| blue > maxColorValue || alpha > maxColorValue) {
			throw new EvalException(
					"One of the color intensities is not in [0,"
							+ maxColorValue + "]");
		}
		StringBuilder hex = new StringBuilder();
		hex.append('#');
		appendHexDouble(red, maxColorValue, hex);
		appendHexDouble(green, maxColorValue, hex);
		appendHexDouble(blue, maxColorValue, hex);
		
		if(useAlpha) {
		  appendHexDouble(alpha, maxColorValue, hex);
		}
    return hex.toString();
	}

  private static void appendHexDouble(double value, double maxValue, StringBuilder sb) {
    String hex = Integer
				.toHexString((int) Math.round((value * 255.0 / maxValue)) )
				.toUpperCase();
    if(hex.length() == 1) {
      sb.append('0');
    }
    sb.append(hex);
  }

	@Primitive("rgb")
	public static StringVector rgb(DoubleVector red, DoubleVector green,
			DoubleVector blue, DoubleVector alpha, DoubleVector maxcolorvalue,
			StringVector names) {
		int maxindex = Math
				.max(Math.max(Math.max(red.length(), green.length()),
						blue.length()), alpha.length());
		StringVector.Builder builder = new StringVector.Builder();
		for (int i = 0; i < maxindex; i++) {
			double cred = red.get(i % red.length());
			double cgreen = green.get(i % green.length());
			double cblue = blue.get(i % blue.length());
			double calpha = alpha.get(i % alpha.length());
			double cmax = maxcolorvalue.get(i % maxcolorvalue.length());
			builder.add(getHexRgb(cred, cgreen, cblue, calpha, cmax, true));
		}
		builder.setAttribute(Symbols.NAMES, names);
		StringVector vect = builder.build();
		return (vect);
	}

	@Primitive("rgb256")
	public static StringVector rgb(DoubleVector red, DoubleVector green,
			DoubleVector blue, DoubleVector alpha, StringVector names) {
		int maxindex = Math
				.max(Math.max(Math.max(red.length(), green.length()),
						blue.length()), alpha.length());
		StringVector.Builder builder = new StringVector.Builder();
		for (int i = 0; i < maxindex; i++) {
			double cred = red.get(i % red.length());
			double cgreen = green.get(i % green.length());
			double cblue = blue.get(i % blue.length());
			double calpha = alpha.get(i % alpha.length());
			double cmax = 255;
			builder.add(getHexRgb(cred, cgreen, cblue, calpha, cmax, true));
		}
		builder.setAttribute(Symbols.NAMES, names);
		StringVector vect = builder.build();
		return (vect);
	}

	@Primitive("gray")
	public static String gray(@Recycle double level) {
		String color = RgbHsv.getHexRgb(level, level, level, 1.0, 1.0, false);
		return (color);
	}

	@Primitive("hsv")
	public static String hsv(@Recycle double h, @Recycle double s,
			@Recycle double v,  @Recycle double alpha) {
		Color clr = Color.getHSBColor((float) h, (float) s, (float) v);
		String sclr = getHexRgb(clr.getRed(), clr.getGreen(), clr.getBlue(),
				alpha * 255, 255, true);
		return (sclr);
	}

	private static String getColorStrFromName(String name) {
		String result = null;
		String n = name.replace('\"', ' ').trim();
		initColorDataBase();
		for (int i = 0; i < colorDataBase.size(); i++) {
			if (colorDataBase.get(i).getName().equals(n)) {
				result = colorDataBase.get(i).rgb;
				break;
			}
		}
		return (result);
	}

	private static int[] getRGBComponentsFromCode(String sharpcode) {
		String red = sharpcode.substring(1, 3);
		String green = sharpcode.substring(3, 5);
		String blue = sharpcode.substring(5);
		int[] result = new int[3];
		result[0] = Integer.parseInt(red, 16);
		result[1] = Integer.parseInt(green, 16);
		result[2] = Integer.parseInt(blue, 16);
		return (result);
	}

	@Primitive("col2rgb")
	public static IntVector col2rgb(Vector s) {
		int[] result;
		IntArrayVector.Builder ib = new IntArrayVector.Builder();

		for (int j = 0; j < s.length(); j++) {
			String name = "white", rgb = "#FFFFFF";
			if (s.getTypeName().equals("character")) {
				name = s.getElementAsString(j);
				rgb = getColorStrFromName(name);
			} else if (s.getTypeName().equals("double")) {
				name = defaultPalette[(int) (s.getElementAsDouble(j) % defaultPalette.length)];
				rgb = getColorStrFromName(name);
			} else {
				throw new EvalException(
						"Parameter must be a color index or valid color name");
			}
			if (rgb == null) {
				throw new EvalException("Invalid color name: " + name);
			}
			result = getRGBComponentsFromCode(rgb);

			for (int i = 0; i < result.length; i++) {
				ib.add(result[i]);
			}
		}
		ib.setAttribute(Symbols.DIM, new IntArrayVector(3, s.length()));
		ib.setAttribute(Symbols.ROW_NAMES, new StringArrayVector(new String[] {
				"red", "green", "blue" }));
		return (ib.build());
	}

	@Primitive("rgb2hsv")
	public static DoubleVector rgb2hsv(DoubleVector rgb) {
		DoubleArrayVector.Builder result = new DoubleArrayVector.Builder();
		float[] hsvvals = new float[3];
		for (int i = 0; i < rgb.length(); i += 3) {
			Color.RGBtoHSB((int) (rgb.get(i) * 255),
					(int) (rgb.get(i + 1) * 255), (int) (rgb.get(i + 2) * 255),
					hsvvals);
			result.add(hsvvals[0]);
			result.add(hsvvals[1]);
			result.add(hsvvals[2]);
		}
		result.setAttribute(Symbols.DIM, new IntArrayVector(3, rgb.length() / 3));
		result.setAttribute(Symbols.ROW_NAMES, new StringArrayVector(new String[] {
				"h", "s", "v" }));
		return (result.build());
	}

	private static double gtrans(double u) {
		if (u > 0.00304) {
			return 1.055 * Math.pow(u, (1 / GAMMA)) - 0.055;
		} else {
			return 12.92 * u;
		}
	}

	private static double[] hcl2rgb(double h, double c, double l) {
		double L, U, V;
		double u, v;
		double X, Y, Z;
		double R, G, B;

		/* Step 1 : Convert to CIE-LUV */

		h = DEG2RAD * h;
		L = l;
		U = c * Math.cos(h);
		V = c * Math.sin(h);

		/* Step 2 : Convert to CIE-XYZ */

		if (L <= 0 && U == 0 && V == 0) {
			X = 0;
			Y = 0;
			Z = 0;
		} else {
			Y = WHITE_Y
					* ((L > 7.999592) ? Math.pow((L + 16) / 116, 3) : L / 903.3);
			u = U / (13 * L) + WHITE_u;
			v = V / (13 * L) + WHITE_v;
			X = 9.0 * Y * u / (4 * v);
			Z = -X / 3 - 5 * Y + 3 * Y / v;
		}

		/* Step 4 : CIE-XYZ to sRGB */

		R = gtrans((3.240479 * X - 1.537150 * Y - 0.498535 * Z) / WHITE_Y);
		G = gtrans((-0.969256 * X + 1.875992 * Y + 0.041556 * Z) / WHITE_Y);
		B = gtrans((0.055648 * X - 0.204043 * Y + 1.057311 * Z) / WHITE_Y);

		return (new double[] { R, G, B });
	}

	private static int ScaleAlpha(double x) {
		if (!DoubleVector.isFinite(x) || x < 0.0 || x > 1.0)
			throw new EvalException("alpha level " + x + ", not in [0,1]");
		return (int) (255 * x + 0.5);
	}

	public static String RGBA2rgb(int r, int g, int b, int a) {
		char[] ColBuf = new char[8];
		char[] HexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		ColBuf[0] = '#';
		ColBuf[1] = HexDigits[(r >> 4) & 15];
		ColBuf[2] = HexDigits[r & 15];
		ColBuf[3] = HexDigits[(g >> 4) & 15];
		ColBuf[4] = HexDigits[g & 15];
		ColBuf[5] = HexDigits[(b >> 4) & 15];
		ColBuf[6] = HexDigits[b & 15];
		ColBuf[7] = HexDigits[(a >> 4) & 15];
		ColBuf[8] = HexDigits[a & 15];
		return new String(ColBuf);
	}

}
