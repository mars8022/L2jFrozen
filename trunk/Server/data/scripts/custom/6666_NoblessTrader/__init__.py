import sys
from interlude.gameserver.model.actor.instance import L2PcInstance
from interlude.gameserver.model.actor.instance import L2NpcInstance
from java.util import Iterator
from interlude import L2DatabaseFactory
from interlude.gameserver.model.quest import State
from interlude.gameserver.model.quest import QuestState
from interlude.gameserver.model.quest.jython import QuestJython as JQuest

qn = "6666_NoblessTrader"

NPC=[66666]
NOBLESS_TIARA=7694
QuestId     = 6666
QuestName   = "NoblessTrade"
QuestDesc   = "custom"
InitialHtml = "66666-1.htm"

print "importing custom: 6666_NoblessTrader"

class Quest (JQuest) :

	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

	def onEvent(self,event,st):
               htmltext = "<html><head><body>I have nothing to say you</body></html>"
               cond = st.getInt("cond")
               if event == "66666-3.htm" :
                   if cond == 0 and st.getPlayer().isSubClassActive() :
                       if st.getPlayer().getLevel() >= 50 :
                            htmltext=event
                            st.set("cond","2")
                            st.getPlayer().setNoble(True)
                            st.giveItems(NOBLESS_TIARA,1)
                            st.playSound("ItemSound.quest_finish")
                            st.setState(COMPLETED)
                       else :
                            htmltext="66666-2.htm"
                            st.exitQuest(1)
                   else :
                       htmltext="66666-2.htm"
                       st.exitQuest(1)
               return htmltext

	def onTalk (self,npc,player):
	       st = player.getQuestState(qn)
               if not st : 
                  st = self.newQuestState(player)
               id = st.getState()
               if id == CREATED :
                  st.set("cond","0")
                  htmltext="66666-1.htm"
               elif id == COMPLETED :
                  htmltext = "<html><head><body>This quest have already been completed.</body></html>"
               else :
                  htmltext="<html><head><body>I've nothing to say you..</body></html>"
                  st.exitQuest(1)
               return htmltext


QUEST = Quest(6666,qn,"custom")
CREATED     = State.CREATED
STARTED     = State.STARTED
COMPLETED   = State.COMPLETED

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 QUEST.addTalkId(npcId)