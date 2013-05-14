/*
 * Copyright 2013 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.student.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.kuali.student.common.io.exceptions.InvalidKeyLineException;

/**
 * @author Kuali Student Team
 * 
 *         The code in this class was copied from
 *         org.tmatesoft.svn.core.internal.wc.SVNUtil version 1.7.8 and was
 *         originally written by: TMate Software Ltd., Peter Skoog
 * 
 *         it is subject to the <a
 *         href="http://svnkit.com/license.html">svnkit.com license</a>.
 * 
 */
public final class IOUtils {

	/**
	 * 
	 */
	private IOUtils() {
		// TODO Auto-generated constructor stub
	}

	private static CharsetDecoder decoder = Charset.forName("UTF-8")
			.newDecoder();

	public static String readLine(InputStream in, String charsetName)
			throws IOException {

		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		int r = -1;

		while ((r = in.read()) != '\n') {

			if (r == -1) {
				// String out = decode(decoder, byteBuffer.toByteArray());

				return null;

			}

			byteBuffer.write(r);

		}

		String out = decode(decoder, byteBuffer.toByteArray());

		return out;

	}

	/**
	 * @return true where 'PROPS-END' hasn't been encountered yet
	 * @throws IOException 
	 * @throws InvalidKeyLineException
	 */
	public static boolean readKeyAndValuePair(FileInputStream inputStream,
			Map<String, String> nodeProperties) throws InvalidKeyLineException, IOException {

		String key = readKey(inputStream);

		if (key == null) {
			// end of props section reached
			return false;
		}

		String value = readValue(inputStream);

		nodeProperties.put(key, value);

		return true;
	}

	private static String readValue(FileInputStream inputStream)
			throws InvalidKeyLineException, IOException {

		String value = readLinePair("V", inputStream);

		if (value == null)
			throw new InvalidKeyLineException("no value");
		else
			return value;

	}

	private static String readKey(FileInputStream inputStream)
			throws InvalidKeyLineException, IOException {
		return readLinePair("K", inputStream);
	}

	private static String readLinePair(String startsWithCharacter,
			FileInputStream inputStream) throws InvalidKeyLineException,
			IOException {

		String lengthLine = readLine(inputStream, "UTF-8");

		if (lengthLine != null && lengthLine.equals("PROPS-END")) {
			// if it looks like there is a line gap between the last property and the end it it really because 
			// a V 0 is realized as a \n.  e.g. zero length text plus line ending.
			// reading the V 0 value takes care of the line ending and we just read the PROPS-END.
			return null;
		}
		else if (!lengthLine.startsWith(startsWithCharacter)) {
			throw new InvalidKeyLineException(lengthLine + " is invalid");
		}

		String parts[] = lengthLine.trim().split(" ");

		if (parts.length != 2)
			throw new InvalidKeyLineException(lengthLine
					+ " does not contain two parts");

		String lengthString = parts[1];

		int length = Integer.parseInt(lengthString);

		// we also need the null byte

		byte[] valueBuffer = new byte[length + 1];

		org.apache.commons.io.IOUtils.readFully(inputStream, valueBuffer);

		String value = new String(valueBuffer).trim();

		return value;
	}

	private static String decode(CharsetDecoder decoder, byte[] in) {

		ByteBuffer inBuf = ByteBuffer.wrap(in);

		CharBuffer outBuf = CharBuffer.allocate(inBuf.capacity()
				* Math.round(decoder.maxCharsPerByte() + 0.5f));

		decoder.decode(inBuf, outBuf, true);

		decoder.flush(outBuf);

		decoder.reset();

		return outBuf.flip().toString();

	}

}
