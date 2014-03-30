/*
 * Copyright (C) 2007, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package me.toxiccoke.io;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

public class StreamEncoder extends OutputStream {
	protected int originalPos = -1;

	public StreamEncoder(File f) throws FileNotFoundException {

		out = new BufferedOutputStream(new FileOutputStream(f));

	}

	/**
	 * ISO-8859-1 was the fixed charset in earlier LGM versions, so those parts
	 * of the code which have not been updated to set the charset explicitly
	 * should continue to use it to avoid regressions.
	 */
	private Charset charset = Charset.forName("ISO-8859-1");
	protected OutputStream out;
	protected int pos = 0;

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public void writeStr(String str) throws IOException {
		byte[] encoded = str.getBytes(charset);
		write4(encoded.length);
		write(encoded);
	}

	public void writeStr1(String str) throws IOException {
		byte[] encoded = str.getBytes(charset);
		int writeSize = Math.min(encoded.length, 255);
		write(writeSize);
		write(encoded, 0, writeSize);
	}

	public void writeBool(boolean val) throws IOException {
		write4(val ? 1 : 0);
	}

	public void compress(byte[] data) throws IOException {
		Deflater compresser = new Deflater();
		compresser.setInput(data);
		compresser.finish();
		byte[] buffer = new byte[131072];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (!compresser.finished()) {
			int len = compresser.deflate(buffer);
			baos.write(buffer, 0, len);
		}
		write4(baos.size());
		write(baos.toByteArray());
	}

	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		pos += len;
	}

	public void write(int b) throws IOException {
		out.write(b);
		pos++;
	}

	public void write2(int val) throws IOException {
		short i = (short) val;
		write(i & 255);
		write((i >>> 8) & 255);
	}

	public void write3(int val) throws IOException {
		write(val & 255);
		write((val >>> 8) & 255);
		write((val >>> 16) & 255);
	}

	public void write4(int val) throws IOException {
		write(val & 255);
		write((val >>> 8) & 255);
		write((val >>> 16) & 255);
		write((val >>> 24) & 255);
	}

	public void writeD(double val) throws IOException {
		long num = Double.doubleToLongBits(val);
		byte[] b = new byte[8];
		b[0] = (byte) (num & 0xFF);
		for (int i = 1; i < 8; i++)
			b[i] = (byte) ((num >>> (8 * i)) & 0xFF);
		write(b);
	}

	public void close() throws IOException {
		out.close();
	}

	public void fill(int count) throws IOException {
		for (int i = 0; i < count; i++) {
			write4(0);
		}
	}

	public void flush() throws IOException {
		out.flush();
	}

	protected static int[] makeEncodeTable(int seed) {
		int[] table = new int[256];
		int a = 6 + (seed % 250);
		int b = seed / 250;
		for (int i = 0; i < 256; i++)
			table[i] = i;
		for (int i = 1; i < 10001; i++) {
			int j = 1 + ((i * a + b) % 254);
			int t = table[j];
			table[j] = table[j + 1];
			table[j + 1] = t;
		}
		return table;
	}
}