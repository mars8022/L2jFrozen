package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */

public class Golkonda extends Quest implements Runnable
{
	// Golkonda NpcID
	private static final int GOLKONDA = 25126;
	
	
	public Golkonda(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addEventId(GOLKONDA, Quest.QuestEventType.ON_ATTACK);
	}


	@Override
	public void run()
	{}
}
