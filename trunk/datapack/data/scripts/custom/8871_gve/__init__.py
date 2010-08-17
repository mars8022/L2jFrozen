# Author ProGramMoS, Scoria Dev
# Version 0.2b
import sys
from com.l2jfrozen.gameserver.model.actor.instance import L2PcInstance
from com.l2jfrozen.util.database import L2DatabaseFactory
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest

qn = "8871_gve"

# Этот скрипт показывает как легко можно управлять системой GvE.
# Здесь мы используем такие параметры:
#  st.getPlayer.setGood(true) - делает нас за "добро"
#  st.getPlayer.setEvil(true) - делает нас за "зло"
# Если true заменить false, то мы выходим из фракции.
#
# Так же еше можно использовать такие вещи:
#  st.getPlayer.isGood() - возврашает true если мы за "добро"
#  st.getPlayer.isEvil() - возврашает true если мы за "зло"
#
# Для запуска этого квеста используется этот класс:
#  com.l2jfrozen.gameserver.mode -> GvE -> CreateCharacter(L2PcInstance player)
#
# Так же не забываем, что у нас сохраняется статус фракции в БД
#
# ДОПОЛНИТЕЛЬНО:
#  Убийства:
#   st.getPlayer.getKills() - получаем кол-во убийств
#   st.getPlayer.setKills(int count) - устанавливаем кол-во убийств
#   st.getPlayer.updateKills() - увеличиваем счетчик убийств на 1
#
# Это можно использовать все вместе. Например:
#  if(st.getPlayer.getKills() == 10)
#  		st.getPlayer.addSkill(skill, true)
#
# Так же возможно сделать повышение счетчика убийств за выполнение квестов.

class Quest (JQuest) :

	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

	def onEvent(self,event,st):
		st.getPlayer().setTarget(st.getPlayer())
		
		if event == "1": #good
			st.getPlayer.setGood(true)
			st.setState(COMPLETED)
			
		if event == "2": #evil
			st.getPlayer.setEvil(true)
			st.setState(COMPLETED)
			
		if event == "3": #unfact good
			st.getPlayer.setGood(false)
			st.setState(COMPLETED)
			
		if event == "4": #unfact evil
			st.getPlayer.setEvil(false)
			st.setState(COMPLETED)
		return
			
# wtf?
QUEST       = Quest(8871,qn,"custom")
CREATED		= State('Start',QUEST)
STARTED		= State('Started',QUEST)
COMPLETED	= State('Completed',QUEST)

QUEST.setInitialState(CREATED)