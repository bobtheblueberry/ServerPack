/*
 * Copyright (C) 2007, 2009, 2010 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2006, 2007 Clam <clamisgood@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package me.toxiccoke.io;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

public class StreamDecoder extends InputStream {
	protected int originalPos = -1;
	protected InputStream in;
	protected int pos = 0;
	protected int markPos = 0;

	public StreamDecoder(File f) throws FileNotFoundException {
		in = new BufferedInputStream(new FileInputStream(f));
	}

	public int read() throws IOException {
		int t = in.read();
		if (t == -1) {
			String error = "StreamDecoder.UNEXPECTED_EOF"; //$NON-NLS-1$
			throw new IOException(error);
		}
		pos++;
		return t;
	}

	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		int read = in.read(b, off, len);
		if (read != len) {
			String error = "StreamDecoder.UNEXPECTED_EOF"; //$NON-NLS-1$
			throw new IOException(error);
		}
		pos += len;
		return read;
	}

	/**
	 * ISO-8859-1 was the fixed charset in earlier LGM versions, so those parts
	 * of the code which have not been updated to set the charset explicitly
	 * should continue to use it to avoid regressions.
	 */
	private Charset charset = Charset.forName("ISO-8859-1");

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public String readStr() throws IOException {
		byte data[] = new byte[read4()];
		read(data);
		return new String(data, charset);
	}

	public String readStr1() throws IOException {
		byte data[] = new byte[read()];
		read(data);
		return new String(data, charset);
	}

	public boolean readBool() throws IOException {
		int val = read4();
		if (val != 0 && val != 1) {
			String error = "GmStreamDecoder.INVALID_BOOLEAN"; //$NON-NLS-1$
			throw new IOException(error);
		}
		return val == 0 ? false : true;
	}


	/**
	 * Convenience method to retrieve whether the given bit is masked in bits,
	 * That is, if given flag is set. E.g.: to find out if the 3rd flag from
	 * right is set in 00011*0*10, use mask(26,4);
	 * 
	 * @param bits
	 *            - A cluster of flags/bits
	 * @param bit
	 *            - The desired (and already shifted) bit or bits to mask
	 * @return Whether bit is masked in bits
	 */
	public static boolean mask(int bits, int bit) {
		return (bits & bit) == bit;
	}

	/**
	 * If the stream is currently reading zlib data, this returns a string in
	 * the format:
	 * <code>&lt;file offset&gt;[&lt;decompressed data offset&gt;]</code><br/>
	 * Otherwise just the file offset is returned.
	 */
	protected String getPosString() {
		if (originalPos != -1)
			return originalPos + "[" + pos + "]";
		return Integer.toString(pos);
	}

	public int read2() throws IOException {
		int a = read();
		int b = read();
		return (a | (b << 8));
	}

	public int read3() throws IOException {
		int a = read();
		int b = read();
		int c = read();
		return (a | (b << 8) | (c << 16));
	}

	public int read4() throws IOException {
		int a = read();
		int b = read();
		int c = read();
		int d = read();
		return (a | (b << 8) | (c << 16) | (d << 24));
	}

	public double readD() throws IOException {
		byte[] b = new byte[8];
		read(b);
		long r = b[0] & 0xFF;
		for (int i = 1; i < 8; i++)
			r |= (b[i] & 0xFFL) << (8 * i);
		return Double.longBitsToDouble(r);
	}

	public void close() throws IOException {
		in.close();
	}

	public long skip(long length) throws IOException {
		long total = in.skip(length);
		while (total < length) {
			total += in.skip(length - total);
		}
		pos += (int) length;
		return total;
	}

	public InputStream getInputStream() {
		return in;
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
		markPos = pos;
	}

	public synchronized void reset() throws IOException {
		in.reset();
		pos = markPos;
	}

	public long getPos() {
		return this.pos;
	}

	public void seek(final long pBytes) throws IOException {
		final long toSkip = pBytes - getPos();
		if (toSkip >= 0) {
			final long lBytesSkipped = skip(toSkip);
			if (lBytesSkipped != toSkip) {
				throw new IOException("StreamDecoder.SEEK_SHORT"); //$NON-NLS-1$
			}
		} else {
			throw new IllegalArgumentException("StreamDecoder.SEEK_PASSED");
		}
	}
}