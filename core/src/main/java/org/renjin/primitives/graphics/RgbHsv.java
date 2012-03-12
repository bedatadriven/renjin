package org.renjin.primitives.graphics;

import java.awt.Color;

import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.Recycle;

import r.lang.DoubleVector;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.exception.EvalException;

public class RgbHsv {

	private static String getHexRgb(double red, double green, double blue,
			double alpha, double maxcolorvalue, boolean useAlpha) {
		if (red < 0 || green < 0 || blue < 0 || alpha < 0
				|| red > maxcolorvalue || green > maxcolorvalue
				|| blue > maxcolorvalue || alpha > maxcolorvalue) {
			throw new EvalException(
					"One of the color intensities is not in [0,"
							+ maxcolorvalue + "]");
		}
		String sred = Integer.toHexString((int) (red * 255.0 / maxcolorvalue))
				.toUpperCase();
		String sgreen = Integer.toHexString(
				(int) (green * 255.0 / maxcolorvalue)).toUpperCase();
		String sblue = Integer
				.toHexString((int) (blue * 255.0 / maxcolorvalue))
				.toUpperCase();
		String salpha = Integer.toHexString(
				(int) (alpha * 255.0 / maxcolorvalue)).toUpperCase();
		if (sred.length() < 2) {
			sred = "0" + sred;
		}
		if (sgreen.length() < 2) {
			sgreen = "0" + sgreen;
		}
		if (sblue.length() < 2) {
			sblue = "0" + sblue;
		}
		if (salpha.length() < 2) {
			salpha = "0" + salpha;
		}
		if (useAlpha) {
			return ("#" + sred + sgreen + sblue + salpha);
		} else {
			return ("#" + sred + sgreen + sblue);
		}
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
			@Recycle double v, @Recycle double gamma, @Recycle double alpha) {
		// R 2.14 do not use gamma...
		Color clr = Color.getHSBColor((float) h, (float) s, (float) v);
		String sclr = getHexRgb(clr.getRed(), clr.getGreen(), clr.getBlue(),
				alpha * 255, 255, true);
		return (sclr);
	}
}
