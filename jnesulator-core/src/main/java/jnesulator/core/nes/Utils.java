package jnesulator.core.nes;

import java.util.Locale;

public class Utils {
	public static final int BIT0 = 1, BIT1 = 2, BIT2 = 4, BIT3 = 8, BIT4 = 16, BIT5 = 32, BIT6 = 64, BIT7 = 128,
			BIT8 = 256, BIT9 = 512, BIT10 = 1024, BIT11 = 2048, BIT12 = 4096, BIT13 = 8192, BIT14 = 16384,
			BIT15 = 32768;

	public static String hex(int num) {
		String s = Integer.toHexString(num).toUpperCase(Locale.US);
		if ((s.length() & 1) == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static String hex(long num) {
		String s = Long.toHexString(num).toUpperCase(Locale.US);
		if ((s.length() & 1) == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static int max(int[] array) {
		int m = array[0];
		for (Integer i : array) {
			if (i > m) {
				m = i;
			}
		}
		return m;
	}

	public static void printarray(boolean[] a) {
		StringBuilder s = new StringBuilder();
		for (boolean i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(double[] a) {
		StringBuilder s = new StringBuilder();
		for (double i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(float[] a) {
		StringBuilder s = new StringBuilder();
		for (float i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(int[] a) {
		StringBuilder s = new StringBuilder();
		for (int i : a) {
			s.append(i);
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static void printarray(Object[] a) {
		StringBuilder s = new StringBuilder();
		for (Object i : a) {
			s.append(i.toString());
			s.append(", ");
		}
		if (s.length() >= 1) {
			s.deleteCharAt(s.length() - 1);
		}
		s.append("\n");
		System.err.print(s.toString());
	}

	public static int reverseByte(int nibble) {
		// reverses 8 bits packed into int.
		return (Integer.reverse(nibble) >> 24) & 0xff;
	}

	public static int setbit(int num, int bitnum, boolean state) {
		return (state) ? (num | (1 << bitnum)) : (num & ~(1 << bitnum));
	}

	private Utils() {
	}
}
