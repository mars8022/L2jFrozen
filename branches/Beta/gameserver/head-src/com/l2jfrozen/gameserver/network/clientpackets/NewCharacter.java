/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.network.serverpackets.CharTemplates;
import com.l2jfrozen.gameserver.templates.L2PcTemplate;

public final class NewCharacter extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(NewCharacter.class.getName());

	@Override
	protected void readImpl() { }

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
			_log.fine("DEBUG "+getType()+": Create New Char");

		CharTemplates ct = new CharTemplates();

		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(0);
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.fighter); // Human Fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.mage); // Human Mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter); // Elf Fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage); // Elf Mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter); // DE Fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkMage); // DE Mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter); // Orc Fighter
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcMage); // Orc Mage
		ct.addChar(template);

		template = CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter); // Dwarf Fighter
		ct.addChar(template);

		// Finally
		sendPacket(ct);
	}

	@Override
	public String getType()
	{
		return "[C] 0E NewCharacter";
	}
}