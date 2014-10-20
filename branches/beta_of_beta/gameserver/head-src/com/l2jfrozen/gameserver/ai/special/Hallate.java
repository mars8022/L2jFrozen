package com.l2jfrozen.gameserver.ai.special;

import com.l2jfrozen.gameserver.model.quest.Quest;

/*
 * @author m095 (L2EmuRT)
 */

public class Hallate extends Quest implements Runnable
{
	// Hallate NpcID
	private static final int HALLATE = 25220;
	
	public Hallate(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addEventId(HALLATE, Quest.QuestEventType.ON_ATTACK);
	}

	@Override
	public void run()
	{}
}
