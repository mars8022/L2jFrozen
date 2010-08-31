package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

public class EffectMeditation extends L2Effect
{

	public EffectMeditation(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.MEDITATION;
	}

	@Override
	public void onStart()
	{
		_effected.block();
		_effected.setMeditated(true);
	}

	@Override
	public void onExit()
	{
		_effected.unblock();
		_effected.setMeditated(false);
	}

	@Override
	public boolean onActionTime()
	{
		//stop effect
		return false;
	}
}
