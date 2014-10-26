/*
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
package com.l2jfrozen.gameserver.skills.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.conditions.Condition;
import com.l2jfrozen.gameserver.skills.funcs.FuncTemplate;
import com.l2jfrozen.gameserver.skills.funcs.Lambda;

/**
 * @author mkizub
 */
public final class EffectTemplate
{
	static Logger LOGGER = Logger.getLogger(EffectTemplate.class);
	
	private final Class<?> _func;
	private final Constructor<?> _constructor;
	
	public final Condition attachCond;
	public final Condition applayCond;
	public final Lambda lambda;
	public final int counter;
	public int period; // in seconds
	public final int abnormalEffect;
	public FuncTemplate[] funcTemplates;
	public boolean showIcon;
	
	public final String stackType;
	public final float stackOrder;
	public final double effectPower; // to thandle chance
	public final SkillType effectType; // to handle resistences etc...
	
	public EffectTemplate(final Condition pAttachCond, final Condition pApplayCond, final String func, final Lambda pLambda, final int pCounter, final int pPeriod, final int pAbnormalEffect, final String pStackType, final float pStackOrder, final int pShowIcon, final SkillType eType, final double ePower)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		lambda = pLambda;
		counter = pCounter;
		period = pPeriod;
		abnormalEffect = pAbnormalEffect;
		stackType = pStackType;
		stackOrder = pStackOrder;
		showIcon = pShowIcon == 0;
		effectType = eType;
		effectPower = ePower;
		
		try
		{
			_func = Class.forName("com.l2jfrozen.gameserver.skills.effects.Effect" + func);
		}
		catch (final ClassNotFoundException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		}
		catch (final NoSuchMethodException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
				e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public L2Effect getEffect(final Env env)
	{
		if (attachCond != null && !attachCond.test(env))
			return null;
		try
		{
			final L2Effect effect = (L2Effect) _constructor.newInstance(env, this);
			// if (_applayCond != null)
			// effect.setCondition(_applayCond);
			return effect;
		}
		catch (final IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (final InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (final InvocationTargetException e)
		{
			LOGGER.warn("Error creating new instance of Class " + _func + " Exception was:");
			e.getTargetException().printStackTrace();
			return null;
		}
		
	}
	
	public void attach(final FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
	
}