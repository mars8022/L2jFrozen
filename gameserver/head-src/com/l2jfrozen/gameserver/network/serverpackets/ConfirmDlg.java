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
package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Vector;

/**
 * @author kombat Format: cd d[d s/d/dd/ddd]
 */
public class ConfirmDlg extends L2GameServerPacket
{
        private static final String _S__ED_CONFIRMDLG = "[S] ed ConfirmDlg";
        private int _messageId;
        private int _skillLvL = 1;
        private static final int TYPE_ZONE_NAME = 7;
        private static final int TYPE_SKILL_NAME = 4;
        private static final int TYPE_ITEM_NAME = 3;
        private static final int TYPE_NPC_NAME = 2;
        private static final int TYPE_NUMBER = 1;
        private static final int TYPE_TEXT = 0;
        private Vector<Integer> _types = new Vector<Integer>();
        private Vector<Object> _values = new Vector<Object>();
        private int _time = 0;
        private int _requesterId = 0;

        public ConfirmDlg(int messageId)
        {
                _messageId = messageId;
        }

        public ConfirmDlg addString(String text)
        {
                _types.add(new Integer(TYPE_TEXT));
                _values.add(text);
                return this;
        }

        public ConfirmDlg addNumber(int number)
        {
                _types.add(new Integer(TYPE_NUMBER));
                _values.add(new Integer(number));
                return this;
        }

        public ConfirmDlg addNpcName(int id)
        {
                _types.add(new Integer(TYPE_NPC_NAME));
                _values.add(new Integer(1000000 + id));
                return this;
        }

        public ConfirmDlg addItemName(int id)
        {
                _types.add(new Integer(TYPE_ITEM_NAME));
                _values.add(new Integer(id));
                return this;
        }

        public ConfirmDlg addZoneName(int x, int y, int z)
        {
                _types.add(new Integer(TYPE_ZONE_NAME));
                int[] coord = { x, y, z };
                _values.add(coord);
                return this;
        }

        public ConfirmDlg addSkillName(int id)
        {
                return addSkillName(id, 1);
        }

        public ConfirmDlg addSkillName(int id, int lvl)
        {
                _types.add(new Integer(TYPE_SKILL_NAME));
                _values.add(new Integer(id));
                _skillLvL = lvl;
                return this;
        }

        public ConfirmDlg addTime(int time)
        {
                _time = time;
                return this;
        }

        public ConfirmDlg addRequesterId(int id)
        {
                _requesterId = id;
                return this;
        }

        @Override
        protected final void writeImpl()
        {
                writeC(0xed);
                writeD(_messageId);
                if (_types != null && _types.size() > 0)
                {
                        writeD(_types.size());
                        for (int i = 0; i < _types.size(); i++)
                        {
                                int t = _types.get(i).intValue();
                                writeD(t);
                                switch (t)
                                {
                                        case TYPE_TEXT:
                                        {
                                                writeS((String) _values.get(i));
                                                break;
                                        }
                                        case TYPE_NUMBER:
                                        case TYPE_NPC_NAME:
                                        case TYPE_ITEM_NAME:
                                        {
                                                int t1 = ((Integer) _values.get(i)).intValue();
                                                writeD(t1);
                                                break;
                                        }
                                        case TYPE_SKILL_NAME:
                                        {
                                                int t1 = ((Integer) _values.get(i)).intValue();
                                                writeD(t1); // Skill Id
                                                writeD(_skillLvL); // Skill lvl
                                                break;
                                        }
                                        case TYPE_ZONE_NAME:
                                        {
                                                int t1 = ((int[]) _values.get(i))[0];
                                                int t2 = ((int[]) _values.get(i))[1];
                                                int t3 = ((int[]) _values.get(i))[2];
                                                writeD(t1);
                                                writeD(t2);
                                                writeD(t3);
                                                break;
                                        }
                                }
                        }
                        // timed dialog (Summon Friend skill request)
                        if (_time != 0)
                                writeD(_time);
                        if (_requesterId != 0)
                                writeD(_requesterId);
                }
                else
                {
                        writeD(0x00);
                        writeD(0x00);
                        writeD(0x00);
                }
        }

        /*
         * (non-Javadoc)
         * @see com.l2jfrozen.gameserver.network.serverpackets.ServerBasePacket#getType()
         */
        @Override
        public String getType()
        {
                return _S__ED_CONFIRMDLG;
        }
}