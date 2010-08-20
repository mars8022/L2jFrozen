# Author ProGramMoS, Scoria Dev
# Version 0.2b
import sys
from com.l2jfrozen.gameserver.model.actor.instance import L2PcInstance
from com.l2jfrozen.util.database import L2DatabaseFactory
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest

qn = "8871_gve"


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
			

QUEST       = Quest(8871,qn,"custom")
CREATED		= State('Start',QUEST)
STARTED		= State('Started',QUEST)
COMPLETED	= State('Completed',QUEST)

QUEST.setInitialState(CREATED)