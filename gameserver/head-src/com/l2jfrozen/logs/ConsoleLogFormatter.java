/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.logs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;

import com.l2jfrozen.Config;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:08 $
 * @author ProGramMoS
 */

public class ConsoleLogFormatter extends Formatter
{
	
	/*
	 * (non-Javadoc)
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	private static final String CRLF = "\r\n";
	
	@Override
	public String format(final LogRecord record)
	{
		final TextBuilder output = new TextBuilder();
		output.append(record.getMessage());
		output.append(CRLF);
		if (record.getThrown() != null)
		{
			try
			{
				final StringWriter sw = new StringWriter();
				final PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				output.append(sw.toString());
				output.append(CRLF);
			}
			catch (final Exception ex)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
					ex.printStackTrace();
				
			}
		}
		
		return output.toString();
	}
}
