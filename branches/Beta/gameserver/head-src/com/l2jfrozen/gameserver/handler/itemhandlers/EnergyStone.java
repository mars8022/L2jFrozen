package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillCharge;

public class EnergyStone implements IItemHandler
{

    public EnergyStone()
    {
    }

    @Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        L2PcInstance activeChar;
        if(playable instanceof L2PcInstance)
            activeChar = (L2PcInstance)playable;
        else
        if(playable instanceof L2PetInstance)
            activeChar = ((L2PetInstance)playable).getOwner();
        else
            return;
        if(item.getItemId() != 5589)
            return;
        if(activeChar.isAllSkillsDisabled())
        {
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }        
        if(activeChar.isSitting())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
            return;
        }
        _skill = getChargeSkill(activeChar);
        if(_skill == null)
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addItemName(5589);
            activeChar.sendPacket(sm);
            return;
        }
        
        SystemMessage sm1 = new SystemMessage(SystemMessageId.USE_S1_);
		sm1.addItemName(5589);
		activeChar.sendPacket(sm1);
        
        _effect = activeChar.getChargeEffect();
        if(_effect == null)
        {
            L2Skill dummy = SkillTable.getInstance().getInfo(_skill.getId(), _skill.getLevel());
            if(dummy != null)
            {
                dummy.getEffects(activeChar, activeChar);
                MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, _skill.getId(), 1, 1, 0);
                activeChar.sendPacket(MSU);
                activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
            }
			return;
        }
		
        if(_effect.numCharges < 2)
        {
            _effect.addNumCharges(1);
            SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
    	    sm.addNumber(_effect.getLevel());
    	    activeChar.sendPacket(sm);
        } 
        else
        {
          if(_effect.numCharges == 2)
            activeChar.sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
        }
        
        MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, _skill.getId(), 1, 1, 0);
        activeChar.sendPacket(MSU);
        activeChar.broadcastPacket(MSU);
        activeChar.sendPacket(new EtcStatusUpdate(activeChar));
        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
    }
    private L2SkillCharge getChargeSkill(L2PcInstance activeChar)
    {
        L2Skill skills[] = activeChar.getAllSkills();
        L2Skill arr$[] = skills;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            L2Skill s = arr$[i$];
            if(s.getId() == 50 || s.getId() == 8)
                return (L2SkillCharge)s;
        }

        return null;
    }

    @Override
	public int[] getItemIds()
    {
        return ITEM_IDS;
    }

    private static final int ITEM_IDS[] = {
        5589
    };
    private EffectCharge _effect;
    private L2SkillCharge _skill;

}